package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.agent.command.RequestReceiver;
import com.redhat.thermostat.agent.ipc.server.AgentIPCService;
import com.redhat.thermostat.vm.decompiler.communication.CallDecompilerAgent;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This class manages the requests that are put in queue by the controller.
 */
@Component
@Service(value = RequestReceiver.class)
@Property(name = "servicename", value = "com.redhat.thermostat.vm.decompiler.core.DecompilerRequestReciever")

public class DecompilerRequestReciever implements RequestReceiver {

    private static final Logger logger = LoggingUtils.getLogger(DecompilerRequestReciever.class);

    private final AgentAttachManager attachManager;

    private static final Response ERROR_RESPONSE = new Response(ResponseType.ERROR);
    private static final Response OK_RESPONSE = new Response(ResponseType.OK);
    private static final int NOT_ATTACHED = -1;

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
        this.attachManager = attachManager;
    }

    protected void bindWriterId(WriterID writerId) {
        this.writerId = writerId;
        attachManager.setWriterId(writerId);
    }

    protected void unbindWriterId(WriterID writerId) {
        this.writerId = null;
        attachManager.setWriterId(null);
    }

    protected void bindVmDecompilerDao(VmDecompilerDAO vmDecompilerDao) {
        this.vmDecompilerDao = vmDecompilerDao;
        attachManager.setVmDecompilerDao(vmDecompilerDao);
    }

    protected void unbindVmDecompilerDao(VmDecompilerDAO vmDecompilerDao) {
        this.vmDecompilerDao = null;
        attachManager.setVmDecompilerDao(null);
    }

    protected void bindAgentIpcService(AgentIPCService ipcService) {
        AgentLoader agentLoader = new AgentLoader(ipcService);
        attachManager.setAttacher(agentLoader);
    }

    protected void unbindAgentIpcService(AgentIPCService ipcService) {
        attachManager.setAttacher(null);
    }

    protected void bindUserNameUtil(UserNameUtil userNameUtil) {
        ProcessUserInfoBuilder userInfoBuilder = ProcessUserInfoBuilderFactory.createBuilder(new ProcDataSource(), userNameUtil);
        attachManager.setUserInfoBuilder(userInfoBuilder);
    }

    protected void unbindUserNameUtil(UserNameUtil userNameUtil) {
        attachManager.setUserInfoBuilder(null);
    }

    /**
     * This method is invoked once this receiver gets a request and processes it.
     * @param request ACTION or BYTES request
     * @return response: ERROR or OK or AUTH_FAILURE
     */
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
            logger.log(Level.WARNING, "Illegal action in request", e);
            return ERROR_RESPONSE;
        }
        port = tryParseInt(portStr, "Listen port is not an integer!");
        vmPid = tryParseInt(vmPidStr, "VM PID is not a number!");

        logger.log(Level.FINE, "Processing request. VM ID: " + vmId + ", PID: " + vmPid + ", action: " + action + ", port: " + portStr);
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
                logger.warning("Unknown action given: " + action);
                return ERROR_RESPONSE;
        }
        return response;

    }

    private int tryParseInt(String intStr, String msg) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, msg + " Given: " + intStr) ;
            return NOT_ATTACHED;
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
        if (actualListenPort == NOT_ATTACHED) {
            logger.log(Level.WARNING, "Failed to attach agent.");
            return ERROR_RESPONSE;
        }
        CallDecompilerAgent nativeAgent = new CallDecompilerAgent(actualListenPort, null);
        try {
            System.out.println(className);
            String bytes = nativeAgent.submitRequest("BYTES\n" + className);
            if (bytes == "ERROR") {
                return ERROR_RESPONSE;

            }
            VmDecompilerStatus status = new VmDecompilerStatus(writerId.getWriterID());
            status.setListenPort(actualListenPort);
            status.setTimeStamp(System.currentTimeMillis());
            status.setVmId(vmId.get());
            status.setBytesClassName(className);
            status.setLoadedClassBytes(bytes);
            vmDecompilerDao.addOrReplaceVmDecompilerStatus(status);

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

        if (actualListenPort == NOT_ATTACHED) {
            logger.log(Level.WARNING, "Failed to call decompiler agent.");
            return ERROR_RESPONSE;
        }

        try {
            CallDecompilerAgent nativeAgent = new CallDecompilerAgent(actualListenPort, null);
            String classes = nativeAgent.submitRequest("CLASSES");

            if (classes == "ERROR") {
                return ERROR_RESPONSE;
            }
            String[] arrayOfClasses = parseClasses(classes);
            VmDecompilerStatus status = new VmDecompilerStatus(writerId.getWriterID());
            status.setListenPort(actualListenPort);
            status.setTimeStamp(System.currentTimeMillis());
            status.setVmId(vmId.get());
            status.setLoadedClassNames(arrayOfClasses);
            vmDecompilerDao.addOrReplaceVmDecompilerStatus(status);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occured while processing request: " + ex.getMessage());
            return ERROR_RESPONSE;
        }
        return OK_RESPONSE;

    }

    private int checkIfAgentIsLoaded(int port, VmId vmId, int vmPid) {
        if (port != NOT_ATTACHED) {
            return port;
        }
        int actualListenPort = NOT_ATTACHED;
        VmDecompilerStatus status = attachManager.attachAgentToVm(vmId, vmPid);
        if (status != null) {
            actualListenPort = status.getListenPort();
        }

        return actualListenPort;
    }

    private String[] parseClasses(String classes) throws Exception {
        String[] array = classes.split(";");
        List<String> list = new ArrayList<>(Arrays.asList(array));
        list.removeAll(Arrays.asList("", null));
        return list.toArray(new String[]{});    

    }

}
