package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.agent.ipc.server.AgentIPCService;
import com.redhat.thermostat.common.portability.ProcessChecker;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.decompiler.communication.InstallDecompilerAgentImpl;

import com.sun.tools.attach.*;
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
    private static final int PORT_MIN = 10101;
    private static final int MAX_PORT_SLOTS = 200;
    private static final int PORT_MAX = PORT_MIN + MAX_PORT_SLOTS;
    private final AgentLoader.AgentInstallHelper installer;
    private final ProcessChecker processChecker;
    static final String LOCALHOST = "localhost";

    private static final String AGENT_LOADED_PROPERTY = "com.redhat.decompiler.thermostat.loaded";
    private static final String AGENT_PORT_PROPERTY = "com.redhat.decompiler.thermostat.port";
    private static final String HELPER_SOCKET_NAME_PROPERTY = "com.redhat.decompiler.thermostat.socketName";
    private static final String AGENT_HOME_SYSTEM_PROP = "com.redhat.decompiler.thermostat.home";
    private static final String DECOMPILER_HOME_ENV_VARIABLE = "DECOMPILER_HOME";
    private static final String DECOMPILER_PREFIX = "com.redhat.decompiler.thermostat";

    AgentLoader(AgentLoader.AgentInstallHelper installer, ProcessChecker processChecker) {
        this.installer = installer;
        this.processChecker = processChecker;
    }

    AgentLoader(AgentIPCService ipcProps) {
        this(new AgentInstallHelper(), new ProcessChecker());
    }

    public AgentInfo attach(String vmId, int pid, String agentId) {
        int port = findPort();
        logger.finest("Attempting to attach decompiler agent for VM '" + pid + "' on port '" + port + "'");
        try {
            //VmSocketIdentifier sockIdentifier = new VmSocketIdentifier(vmId, pid, agentId);
            String[] installProps = buildInstallProps(port);
            boolean agentJarToBootClassPath = true;
            AgentLoader.InstallResult result = installer.install(Integer.toString(pid), agentJarToBootClassPath, false, LOCALHOST, port, installProps);
            int actualPort = result.getPort();
            // Port might have changed here if agent rebooted and targed jvm
            // stayed alive
            if (actualPort > 0) {
                return new AgentInfo(pid, actualPort, null, vmId, agentId, false, result.isOldAttach());
            } else {
                return null;
            }
        } catch (IllegalArgumentException | IOException e) {
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

        AgentLoader.InstallResult install(String vmPid, boolean addToBoot, boolean setPolicy, String hostname, int port, String[] properties) {
            String propVal = InstallDecompilerAgentImpl.getSystemProperty(vmPid, AGENT_LOADED_PROPERTY);
            boolean loaded = Boolean.parseBoolean(propVal);
            if (!loaded) {
                try {
                    InstallDecompilerAgentImpl.install(vmPid, addToBoot, hostname, port, properties);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(AgentLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(AgentLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AttachNotSupportedException ex) {
                    Logger.getLogger(AgentLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AgentLoadException ex) {
                    Logger.getLogger(AgentLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AgentInitializationException ex) {
                    Logger.getLogger(AgentLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
                return new AgentLoader.InstallResult(port, false);
            } else {
                try {
                    int oldPort = Integer.parseInt(InstallDecompilerAgentImpl.getSystemProperty(vmPid, AGENT_PORT_PROPERTY));
                    logger.finest("VM (pid: " + vmPid + "): Not installing agent since one is already attached on port " + oldPort);
                    return new AgentLoader.InstallResult(oldPort, true);
                } catch (NumberFormatException e) {
                    logger.info("VM (pid: " + vmPid + "): Has an agent already attached, but it wasn't thermostat that attached it");
                    return new AgentLoader.InstallResult(UNKNOWN_PORT, true);
                }
            }
        }
    }

    private String[] buildInstallProps(int port) throws IOException {
        List<String> properties = new ArrayList<>();
        //String socketNameProperty = HELPER_SOCKET_NAME_PROPERTY + "=" + sockIdentifier.getName();
        //File ipcConfig = ipcService.getConfigurationFile();
        //String ipcSocketDirProperty = IPC_CONFIG_NAME_PROPERTY + "=" + ipcConfig.getAbsolutePath();
        String agentPortProperty = AGENT_PORT_PROPERTY + "=" + Integer.valueOf(port).toString();
        //properties.add(socketNameProperty);
        //properties.add(ipcSocketDirProperty);
        properties.add(agentPortProperty);
        return properties.toArray(new String[]{});
    }
}
