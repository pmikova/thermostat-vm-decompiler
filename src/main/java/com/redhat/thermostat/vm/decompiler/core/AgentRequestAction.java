/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.common.command.Request;
import com.redhat.thermostat.storage.model.VmInfo;
import java.net.InetSocketAddress;

/**
 *
 * @author pmikova
 *
 */
public class AgentRequestAction {

    public static enum RequestAction {
        CLASSES(0),
        BYTES(1),;

        private int intVal;

        private RequestAction(int intVal) {
            this.intVal = intVal;
        }

        public static RequestAction returnAction(String act) {

            int action;
            try {
                action = Integer.parseInt(act);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown action: " + act);
            }
            switch(action) {
                case 0:
                    return CLASSES;
                case 1:
                    return BYTES;
                default:
                    throw new IllegalArgumentException("Unknown request: " + action);
            }
        }

        int getActionId() {
            return intVal;
        }

        private String toIntString() {
            return Integer.toString(intVal);
        }

    }
    public static final String VM_ID_PARAM_NAME = "vm-id";
    public static final String VM_PID_PARAM_NAME = "vm-pid";
    public static final String ACTION_PARAM_NAME = "action";
    public static final String LISTEN_PORT_PARAM_NAME = "listen-port";
    public static final int NOT_ATTACHED_PORT = -1;
    public static final String CLASS_TO_DECOMPILE_NAME = "class-to-decompile";
    private static final String CMD_CHANN_ACTION_NAME = "vm-decompiler-get-bytecode";
    
    private static final String RECEIVER = "com.redhat.thermostat.vm.decompiler.core.DecompilerRequestReciever";

    public static Request create(InetSocketAddress address, VmInfo vmInfo, RequestAction action, int listenPort, String name) {
        Request req = create(address, vmInfo, action, listenPort);
        req.setParameter(CLASS_TO_DECOMPILE_NAME, name);
        return req;
    }

    public static Request create(InetSocketAddress address, VmInfo vmInfo, RequestAction action, int listenPort) {
        Request req = new Request(Request.RequestType.RESPONSE_EXPECTED, address);
        req.setReceiver(RECEIVER);
        req.setParameter(Request.ACTION, CMD_CHANN_ACTION_NAME);
        req.setParameter(VM_ID_PARAM_NAME, vmInfo.getVmId());
        req.setParameter(VM_PID_PARAM_NAME, Integer.toString(vmInfo.getVmPid()));
        req.setParameter(ACTION_PARAM_NAME, action.toIntString());
        req.setParameter(LISTEN_PORT_PARAM_NAME, Integer.toString(listenPort));
        return req;
    }
}
