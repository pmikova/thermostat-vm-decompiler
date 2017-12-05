/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.vm.decompiler.communication.InstallAgent;
import com.redhat.thermostat.agent.ipc.server.AgentIPCService;
import com.redhat.thermostat.common.portability.ProcessChecker;
import com.redhat.thermostat.common.utils.LoggingUtils;

import com.sun.tools.attach.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pmikova
 */
public class AgentLoader {

    private static final Logger logger = LoggingUtils.getLogger(AgentLoader.class);
    private static final int PORT_MIN = 13300;
    private static final int MAX_PORT_SLOTS = 300;
    private static final int PORT_MAX = PORT_MIN + MAX_PORT_SLOTS;
    private final AgentLoader.AgentInstallHelper installer;
    private final ProcessChecker processChecker;
    private final AgentIPCService ipcService;
    private static final String AGENT_LOADED_PROPERTY = "com.redhat.decompiler.thermostat.loaded";
    private static final String AGENT_PORT_PROPERTY = "com.redhat.decompiler.thermostat..port";
    private static final String IPC_CONFIG_NAME_PROPERTY = "com.redhat.decompiler.thermostat.ipcConfig";
    private static final String HELPER_SOCKET_NAME_PROPERTY = "com.redhat.decompiler.thermostat.socketName";

    AgentLoader(AgentLoader.AgentInstallHelper installer, ProcessChecker processChecker, AgentIPCService ipcService) {
        this.installer = installer;
        this.processChecker = processChecker;
        this.ipcService = ipcService;
    }
     AgentLoader(AgentIPCService ipcProps) {
        this(new AgentInstallHelper(), new ProcessChecker(), ipcProps);
    }

    public AgentInfo attach(String vmId, int pid, String agentId) throws Exception {
        int port = findPort();
        logger.finest("Attempting to attach decompiler agent for VM '" + pid + "' on port '" + port + "'");
        try {
            VmSocketIdentifier sockIdentifier = new VmSocketIdentifier(vmId, pid, agentId);
            String[] installProps = buildInstallProps(sockIdentifier, port);
            boolean agentJarToBootClassPath = true;
            AgentLoader.InstallResult result = installer.install(Integer.toString(pid), agentJarToBootClassPath, false, null, port, installProps);
            int actualPort = result.getPort();
            // Port might have changed here if agent rebooted and targed jvm
            // stayed alive
            if (actualPort > 0) {
                return new AgentInfo(pid, actualPort, null, vmId, agentId, false, result.isOldAttach());
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return failAndLog(e, vmId, port, pid);
        }
    }

    private AgentInfo failAndLog(Throwable cause, String vmId, int port, int pid) {
        logger.log(Level.INFO, "Unable to attach to decompiler agent.", cause);
        logger.log(Level.WARNING, "Unable to attach decompiler agent to VM '" + pid + "' on port '" + port + "'");
        return null;
    }

    private int findPort() {
        for (int i = PORT_MIN; i <= PORT_MAX; i++) {
            try {
                try (ServerSocket s = new ServerSocket(i)) {
                    s.close();
                    return i;
                }
            } catch (Exception e) {
                // ignore. try next port
            }
        }
        throw new IllegalStateException("No ports available in range [" + PORT_MIN + "," + PORT_MAX + "]");
    }

    static class InstallResult {

        private final int port;
        private final boolean isOldAttach;

        InstallResult(int port, boolean isOldAttach) {
            this.port = port;
            this.isOldAttach = isOldAttach;
        }

        boolean isOldAttach() {
            return isOldAttach;
        }

        int getPort() {
            return port;
        }
    }

    static class AgentInstallHelper {

        private static final int UNKNOWN_PORT = -1;

        AgentLoader.InstallResult install(String vmPid, boolean addToBoot, boolean setPolicy, String hostname, int port, String[] properties)
                throws IllegalArgumentException, FileNotFoundException,
                IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
            String propVal = InstallAgent.getProperty(vmPid, AGENT_LOADED_PROPERTY);
            boolean loaded = Boolean.parseBoolean(propVal);
            if (!loaded) {
                InstallAgent.install(vmPid, addToBoot, hostname, port, properties);
                return new AgentLoader.InstallResult(port, false);
            } else {
                try {
                    int oldPort = Integer.parseInt(InstallAgent.getProperty(vmPid, AGENT_PORT_PROPERTY));
                    logger.finest("VM (pid: " + vmPid + "): Not installing agent since one is already attached on port "+ oldPort);
                    return new AgentLoader.InstallResult(oldPort, true);
                } catch (NumberFormatException e) {
                    logger.info("VM (pid: " + vmPid + "): Has an agent already attached, but it wasn't thermostat that attached it");
                    return new AgentLoader.InstallResult(UNKNOWN_PORT, true);
                }
            }
        }
    }
    
    
     private String[] buildInstallProps(VmSocketIdentifier sockIdentifier, int port) throws IOException {
        List<String> properties = new ArrayList<>();
        String socketNameProperty = HELPER_SOCKET_NAME_PROPERTY + "=" + sockIdentifier.getName();
        File ipcConfig = ipcService.getConfigurationFile();
        String ipcSocketDirProperty = IPC_CONFIG_NAME_PROPERTY + "=" + ipcConfig.getAbsolutePath();
        String agentPortProperty = AGENT_PORT_PROPERTY + "=" + Integer.valueOf(port).toString();
        properties.add(socketNameProperty);
        properties.add(ipcSocketDirProperty);
        properties.add(agentPortProperty);
        return properties.toArray(new String[] {});
    }
}
