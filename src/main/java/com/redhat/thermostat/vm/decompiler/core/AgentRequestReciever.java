/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.vm.decompiler.core.VmDecompilerStatus;
import com.redhat.thermostat.agent.command.RequestReceiver;
import com.redhat.thermostat.vm.decompiler.communication.CallNativeAgent;
import com.redhat.thermostat.vm.decompiler.core.StoreJvmInfo;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.common.command.Request;
import com.redhat.thermostat.common.command.Response;
import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.vm.decompiler.core.AgentRequestAction.RequestAction;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pmikova
 */
public class AgentRequestReciever implements RequestReceiver {

    private static final Logger logger = LoggingUtils.getLogger(AgentRequestReciever.class);

    private final AgentAttachManager attachManager;
    private AgentInfo agent;
    private static final Response ERROR_RESPONSE = new Response(Response.ResponseType.ERROR);
    private static final Response OK_RESPONSE = new Response(Response.ResponseType.OK);
    private VmDecompilerStatus status;

    public AgentRequestReciever() {
        this.attachManager = new AgentAttachManager();
    }

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
            StoreJvmInfo storage = status.getStorage();
            storage.addClassBytes(className, byteArray);
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
            ArrayList<String> arrayOfClasses = parseClasses(classes);
            StoreJvmInfo storage = status.getStorage();
            storage.setClassNames(arrayOfClasses);

        } catch (Exception ex) {
            return ERROR_RESPONSE;
        }
        return OK_RESPONSE;

    }

    private int checkIfAgentIsLoaded(int port, VmId vmId, int vmPid) throws Exception {
        int actualListenPort = port;
        status = attachManager.attachAgentToVm(vmId, vmPid);
        if (status != null) {
            actualListenPort = status.getListenPort();
        }
        agent = attachManager.getAgent();
        

        return actualListenPort;
    }

    private ArrayList<String> parseClasses(String classes) throws Exception {

        ArrayList<String> builder = new ArrayList<>();
        String[] array = classes.split(";");
        for (String clazz : array) {
            clazz = clazz.trim();
            builder.add(clazz);

        }
        return builder;
    }
    
    private byte[] parseBytes(String bytes) {
        byte[] decoded = Base64.getDecoder().decode(bytes);
        return decoded;
                }




}
