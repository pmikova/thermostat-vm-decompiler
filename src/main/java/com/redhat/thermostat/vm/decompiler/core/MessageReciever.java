/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;


import com.redhat.thermostat.agent.ipc.server.IPCMessage;
import com.redhat.thermostat.agent.ipc.server.ThermostatIPCCallbacks;
import com.redhat.thermostat.common.utils.LoggingUtils;
import java.util.logging.Logger;


/**
 *
 * @author pmikova
 */
public class MessageReciever implements ThermostatIPCCallbacks{
    private static final Logger logger = LoggingUtils.getLogger(MessageReciever.class);
    private final VmSocketIdentifier socketId;

    
    MessageReciever(VmSocketIdentifier socketId) {
        this.socketId = socketId;

    }

    /*
    This method does only confirm, that we recieved message from the agent.
    Is called every time the plugin gets message from client.
    */
    @Override
    public void messageReceived(IPCMessage message) {
        logger.fine("Received response from decompiler plugin for socketId: " + socketId.getName() + ".");
        

    }




}
