/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.common.command.Request;
import com.redhat.thermostat.storage.model.VmInfo;
import java.net.InetSocketAddress;

/**
 *
 * @author pmikova
 */
public class AgentRequestAction {

    public static enum RequestAction {
        CLASSES(0),
        BYTES(1);

        private int intVal;

        private RequestAction(int intVal) {
            this.intVal = intVal;
        }

        /**
         *
         * @param act
         * @return
         */
        public static RequestAction returnAction(String act) {

            int action;
            try {
                action = Integer.parseInt(act);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown action: " + act);
            }
            switch (action) {
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

    /**
     *
     * @param address
     * @param vmInfo
     * @param action
     * @param listenPort
     * @param name
     * @return
     */
    public static Request create(InetSocketAddress address, VmInfo vmInfo, RequestAction action, int listenPort, String name) {
        Request req = create(address, vmInfo, action, listenPort);
        req.setParameter(CLASS_TO_DECOMPILE_NAME, name);
        return req;
    }

    /**
     *
     * @param address
     * @param vmInfo
     * @param action
     * @param listenPort
     * @return
     */
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
