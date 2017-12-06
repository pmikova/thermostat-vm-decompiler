/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.agent.command.RequestReceiver;
import com.redhat.thermostat.agent.ipc.server.AgentIPCService;
import com.redhat.thermostat.vm.decompiler.communication.CallNativeAgent;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.common.command.Request;
import com.redhat.thermostat.common.command.Response;
import com.redhat.thermostat.common.command.Response.ResponseType;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilderFactory;
import com.redhat.thermostat.common.portability.UserNameUtil;
import com.redhat.thermostat.common.portability.linux.ProcDataSource;
import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.decompiler.core.AgentRequestAction.RequestAction;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAO;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 *
 * @author pmikova
 */
@Component
@Service(value = RequestReceiver.class)
@Property(name = "servicename", value = "com.redhat.thermostat.vm.decompiler.core.DecompilerRequestReciever")

public class DecompilerRequestReciever implements RequestReceiver {

    private static final Logger logger = LoggingUtils.getLogger(DecompilerRequestReciever.class);
    
    private final AgentAttachManager attachManager;
    

    private static final Response ERROR_RESPONSE = new Response(ResponseType.ERROR);
    private static final Response OK_RESPONSE = new Response(ResponseType.OK);

    @Reference
    private VmDecompilerDAO vmDecompilerDao;
    @Reference
    private WriterID writerId;
    
    @Reference
    private AgentIPCService agentIpcService;
    
    @Reference
    private UserNameUtil userNameUtil;

    public DecompilerRequestReciever() {
        this(new AgentAttachManager());
    }
    
    
    public DecompilerRequestReciever(AgentAttachManager attachManager) {
        this.attachManager = new AgentAttachManager();
    }

    // DS METHODS
 
    
    protected void bindWriterId(WriterID writerId) {
        this.writerId = writerId;
        attachManager.setWriterId(writerId);
    }
    
    protected void unbindWriterId(WriterID writerId) {
        this.writerId = null;
        attachManager.setWriterId(null);
    }
    
    protected void bindVmBytemanDao(VmDecompilerDAO dao) {
        this.vmDecompilerDao = dao;
        attachManager.setVmDecompilerDao(dao);
    }
    
    protected void unbindVmBytemanDao(VmDecompilerDAO dao) {
        this.vmDecompilerDao = null;
        attachManager.setVmDecompilerDao(null);
    }
    
    
    protected void bindAgentIpcService(AgentIPCService ipcService) {
        IPCManager ipcEndpointsManager = new IPCManager(ipcService);
        attachManager.setIpcManager(ipcEndpointsManager);
        AgentLoader agentLoader = new AgentLoader(ipcService);
        attachManager.setAttacher(agentLoader);
    }
    
    protected void unbindAgentIpcService(AgentIPCService ipcService) {
        attachManager.setIpcManager(null);
        attachManager.setAttacher(null);
    }
    
    protected void bindUserNameUtil(UserNameUtil userNameUtil) {
        ProcessUserInfoBuilder userInfoBuilder = ProcessUserInfoBuilderFactory.createBuilder(new ProcDataSource(), userNameUtil);
        attachManager.setUserInfoBuilder(userInfoBuilder);
    }
    
    protected void unbindUserNameUtil(UserNameUtil userNameUtil) {
        attachManager.setUserInfoBuilder(null);
    }
   
    //END DS
    @Override
    public Response receive(Request request) {        
        String vmId = request.getParameter(AgentRequestAction.VM_ID_PARAM_NAME);
        String actionStr = request.getParameter(AgentRequestAction.ACTION_PARAM_NAME);
        String portStr = request.getParameter(AgentRequestAction.LISTEN_PORT_PARAM_NAME);
        String vmPidStr = request.getParameter(AgentRequestAction.VM_PID_PARAM_NAME);
        RequestAction action;
        int vmPid;
        int port;

        try {
            action = RequestAction.returnAction(actionStr);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Illegal action received", e);
            return ERROR_RESPONSE;
        }
        port = tryParseInt(portStr, "Listen port not an integer!");
        vmPid = tryParseInt(vmPidStr, "VM pid not a number!");

        logger.fine("Processing request for vmId: " + vmId + ", pid: " + vmPid + ", Action: " + action + ", port: " + portStr);
        Response response;
        switch (action) {
            case BYTES:
                String className = request.getParameter(AgentRequestAction.CLASS_TO_DECOMPILE_NAME);
                response = getByteCodeAction(port, new VmId(vmId), vmPid, className);
                break;
            case CLASSES:
                response = getAllLoadedClassesAction(port, new VmId(vmId), vmPid);
                break;
            default:
                logger.warning("Unknown action: " + action);
                return ERROR_RESPONSE;
        }
        return response;

    }

    private int tryParseInt(String intStr, String msg) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, msg + " Param was '" + intStr + "'", e);
            return -1;
        }
    }

    private Response getByteCodeAction(int listenPort, VmId vmId, int vmPid, String className) {
        int actualListenPort;
        try {
            actualListenPort = checkIfAgentIsLoaded(listenPort, vmId, vmPid);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to attach agent.");
            return ERROR_RESPONSE;
        }
        if (actualListenPort == -1) {
            logger.log(Level.WARNING, "Failed to attach agent.");
            return ERROR_RESPONSE;
        }
        CallNativeAgent nativeAgent = new CallNativeAgent(vmPid);
        try {
            String bytes = nativeAgent.submitRequest("BYTES\n" + className);
            if (bytes == "ERROR") {
                return ERROR_RESPONSE;

            }
            byte[] byteArray = parseBytes(bytes);
            
            //vmDecompilerDao.getVmDecompilerStatus(vmId).addClassBytes(className, byteArray);
        } catch (Exception ex) {
            return ERROR_RESPONSE;
        }
        logger.info("Request for bytecode sent");

        return OK_RESPONSE;
    }

    private Response getAllLoadedClassesAction(int listenPort, VmId vmId, int vmPid) {
        int actualListenPort;
        try {
            actualListenPort = checkIfAgentIsLoaded(listenPort, vmId, vmPid);
        } catch (Exception ex) {
            return ERROR_RESPONSE;
        }

        if (actualListenPort == -1) {
            logger.log(Level.WARNING, "Failed to call Agent.");
            return ERROR_RESPONSE;
        }
        CallNativeAgent nativeAgent = new CallNativeAgent(vmPid);
        try {
            String classes = nativeAgent.submitRequest("CLASSES");
            
            if (classes == "ERROR") {
                return ERROR_RESPONSE;
            }
           String[] arrayOfClasses = parseClasses(classes);;
           vmDecompilerDao.getVmDecompilerStatus(vmId).setClassNames(arrayOfClasses);

        } catch (Exception ex) {
            return ERROR_RESPONSE;
        }
        return OK_RESPONSE;

    }

    private int checkIfAgentIsLoaded(int port, VmId vmId, int vmPid) throws Exception {
        int actualListenPort = port;
        VmDecompilerStatus status = attachManager.attachAgentToVm(vmId, vmPid);
        if (status != null) {
            actualListenPort = vmDecompilerDao.getVmDecompilerStatus(vmId).getListenPort();
        }
        AgentInfo agent = attachManager.getAgent();
        

        return actualListenPort;
    }

    private String[] parseClasses(String classes) throws Exception {

        String[] array = classes.split(";");
        return array;
    }
    
    private byte[] parseBytes(String bytes) {
        byte[] decoded = Base64.getDecoder().decode(bytes);
        return decoded;
                }




}
