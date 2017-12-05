/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.agent.ipc.server.AgentIPCService;
import com.redhat.thermostat.agent.ipc.server.ThermostatIPCCallbacks;
import com.redhat.thermostat.common.utils.LoggingUtils;

/**
 *
 * @author pmikova
 */
public class IPCManager {
    
    private static final Logger logger = LoggingUtils.getLogger(IPCManager.class);
    
    private final Set<String> sockets = new HashSet<String>();
    private final AgentIPCService ipcService;
    
    IPCManager(AgentIPCService ipcService) {
        this.ipcService = ipcService;
    }
    
    synchronized void startIPCEndpoint(final VmSocketIdentifier socketId, final ThermostatIPCCallbacks callback, 
            final UserPrincipal owner) {
        logger.fine("Starting IPC socket for decompiler.");
        String sId = socketId.getName();
        if (!sockets.contains(sId)) {
            try {
                if (ipcService.serverExists(sId)) {
                    // We create the sockets in a way that's unique per agent/vmId/pid. If we have
                    // two such sockets there is a problem somewhere.
                    logger.warning("Socket with id: " + sId + " already exists. Bug?");
                    return;
                }
                //creating server with callback, that is invoked every time the Thermostat plugin recieves data
                ipcService.createServer(sId, callback, owner);
                sockets.add(sId);
                logger.fine("Created IPC endpoint for id: " + sId);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to start IPC entpoint for id: " + sId);
            }
        }
    }
    
   /* synchronized void stopIPCEndpoint(VmSocketIdentifier socketId) {
        String sId = socketId.getName();
        if (sockets.contains(sId)) {
            logger.fine("Destroying socket for id: " + sId);
            try {
                ipcService.destroyServer(sId);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to destroy socket id: " + sId, e);
            }
            sockets.remove(sId);
        }
    }
    */
}
