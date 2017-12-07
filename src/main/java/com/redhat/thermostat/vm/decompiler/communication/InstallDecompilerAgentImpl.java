/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.communication;

/**
 * This is byteman's install library copied, with small modifications. This is
 * only provisional with blessings of Andrew Dinn, author of byteman.
 *
 */
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.jboss.byteman.agent.install.VMInfo;

public class InstallDecompilerAgentImpl {

    public static void install(String pid, boolean addToBoot, String host, int port, String[] properties)
            throws IllegalArgumentException, FileNotFoundException,
            IOException, AttachNotSupportedException,
            AgentLoadException, AgentInitializationException {
        install(pid, addToBoot, false, host, port, properties);
    }

    public static void install(String pid, boolean addToBoot, boolean setPolicy, String host, int port, String[] properties)
            throws IllegalArgumentException, FileNotFoundException,
            IOException, AttachNotSupportedException,
            AgentLoadException, AgentInitializationException {
        install(pid, addToBoot, setPolicy, false, host, port, properties);
    }

    public static void install(String pid, boolean addToBoot, boolean setPolicy, boolean useModuleLoader, String host, int port, String[] properties)
            throws IllegalArgumentException, FileNotFoundException,
            IOException, AttachNotSupportedException,
            AgentLoadException, AgentInitializationException {

        if (port < 0) {
            throw new IllegalArgumentException("Install : port cannot be negative");
        }

        for (int i = 0; i < properties.length; i++) {
            String prop = properties[i];
            if (prop == null || prop.length() == 0) {
                throw new IllegalArgumentException("Install : properties  cannot be null or \"\"");
            }
            if (prop.indexOf(',') >= 0) {
                throw new IllegalArgumentException("Install : properties may not contain ','");
            }
        }

        InstallDecompilerAgentImpl install = new InstallDecompilerAgentImpl(pid, addToBoot, setPolicy, useModuleLoader, host, port, properties);
        install.locateAgent();
        install.attach();
        install.injectAgent();
    }

    public static VMInfo[] availableVMs() {
        List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
        VMInfo[] vmInfo = new VMInfo[vmds.size()];
        int i = 0;
        for (VirtualMachineDescriptor vmd : vmds) {
            vmInfo[i++] = new VMInfo(vmd.id(), vmd.displayName());
        }

        return vmInfo;
    }

    private static final String AGENT_LOADED_PROPERTY = "com.redhat.decompiler.thermostat.loaded";
    private static final String AGENT_PORT_PROPERTY = "com.redhat.decompiler.thermostat.port";
    private static final String IPC_CONFIG_NAME_PROPERTY = "com.redhat.decompiler.thermostat.ipcConfig";
    private static final String HELPER_SOCKET_NAME_PROPERTY = "com.redhat.decompiler.thermostat.socketName";
    private static final String AGENT_HOME_SYSTEM_PROP = "com.redhat.decompiler.thermostat.home";
    private static final String DECOMPILER_HOME_ENV_VARIABLE = "DECOMPILER_HOME";
    private static final String DECOMPILER_PREFIX = "com.redhat.decompiler.thermostat";

    private String agentJar;
    private String modulePluginJar;
    private String id;
    private int port;
    private String host;
    private boolean addToBoot;
    private boolean setPolicy;
    private boolean useModuleLoader;
    private String props;
    private VirtualMachine vm;

    /**
     * Check for system property com.redhat.decompiler.thermostat.home in
     * preference to the environment setting DECOMPILER_HOME and use it to
     * identify the location of the byteman agent jar.
     */
    private void locateAgent() throws IOException {
        // use the current system property in preference to the environment setting

        this.agentJar = System.getProperty(AGENT_HOME_SYSTEM_PROP);
        if (agentJar == null || agentJar.length() == 0) {
            agentJar = System.getenv(DECOMPILER_HOME_ENV_VARIABLE);
        }
    }

    public static boolean isAgentAttached(String id) {
        String value = getProperty(id, AGENT_LOADED_PROPERTY);
        return Boolean.parseBoolean(value);
    }

    public static String getSystemProperty(String id, String property) {
        return getProperty(id, property);
    }

    private static String getProperty(String id, String property) {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(id);
            String value = (String) vm.getSystemProperties().get(property);
            return value;
        } catch (AttachNotSupportedException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (IOException e) {
                    // ignore;
                }
            }
        }
    }

    private InstallDecompilerAgentImpl(String pid, boolean addToBoot, boolean setPolicy,
            boolean useModuleLoader, String host, int port, String[] properties) {

        agentJar = null;
        modulePluginJar = null;
        this.id = pid;
        this.port = port;
        this.addToBoot = addToBoot;
        this.setPolicy = setPolicy;
        this.useModuleLoader = useModuleLoader;
        this.host = host;
        if (properties != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < properties.length; i++) {
                builder.append(",prop:");
                builder.append(properties[i]);
            }
            props = builder.toString();
        } else {
            props = "";
        }
        vm = null;
    }

    private InstallDecompilerAgentImpl(String pid, boolean addToBoot, String host, int port, String[] properties) {
        this(pid, addToBoot, false, false, host, port, properties);
    }

    /**
     * attach to the Java process identified by the process id supplied on the
     * command line
     */
    private void attach() throws AttachNotSupportedException, IOException, IllegalArgumentException {

        if (id.matches("[0-9]+")) {
            // integer process id
            int pid = Integer.valueOf(id);
            if (pid <= 0) {
                throw new IllegalArgumentException("Install : invalid pid " + id);
            }
            vm = VirtualMachine.attach(Integer.toString(pid));
        } else {
            // try to search for this VM with an exact match
            List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd : vmds) {
                String displayName = vmd.displayName();
                int spacePos = displayName.indexOf(' ');
                if (spacePos > 0) {
                    displayName = displayName.substring(0, spacePos);
                }
                if (displayName.equals(id)) {
                    String pid = vmd.id();
                    vm = VirtualMachine.attach(vmd);
                    return;
                }
            }
            // hmm, ok, lets see if we can find a trailing match e.g. if the displayName
            // is org.jboss.Main we will accept jboss.Main or Main
            for (VirtualMachineDescriptor vmd : vmds) {
                String displayName = vmd.displayName();
                int spacePos = displayName.indexOf(' ');
                if (spacePos > 0) {
                    displayName = displayName.substring(0, spacePos);
                }

                if (displayName.indexOf('.') >= 0 && displayName.endsWith(id)) {
                    // looking hopeful ensure the preceding char is a '.'
                    int idx = displayName.length() - (id.length() + 1);
                    if (displayName.charAt(idx) == '.') {
                        // yes it's a match
                        String pid = vmd.id();
                        vm = VirtualMachine.attach(vmd);
                        return;
                    }
                }
            }

            // no match so throw an exception
            throw new IllegalArgumentException("Install : invalid pid " + id);
        }

    }

    /**
     * get the attached process to upload and install the agent jar using
     * whatever agent options were configured on the command line
     */
    private void injectAgent() throws AgentLoadException, AgentInitializationException, IOException {
        try {
            // we need at the very least to enable the listener so that scripts can be uploaded
            String agentOptions = "listener:true";
            if (host != null && host.length() != 0) {
                agentOptions += ",address:" + host;
            }
            if (port != 0) {
                agentOptions += ",port:" + port;
            }
            if (addToBoot) {
                agentOptions += ",boot:" + agentJar;
            }
            if (setPolicy) {
                agentOptions += ",policy:true";
            }
            /* if (useModuleLoader) {
                agentOptions += ",modules:org.jboss.byteman.modules.jbossmodules.JBossModulesSystem,sys:" + modulePluginJar;
            }*/
            if (props != null) {
                agentOptions += props;
            }
            vm.loadAgent("/home/pmikova/NetBeansProjects/JavaAgent/target/JavaAgent-1.0.0-SNAPSHOT.jar",
                     agentOptions);
            
        } finally {
            vm.detach();
        }
    }

    private void parseArgs(String[] args) {
        int argCount = args.length;
        int idx = 0;
        if (idx == argCount) {
            //usage(0);
        }

        String nextArg = args[idx];

        while (nextArg.length() != 0
                && nextArg.charAt(0) == '-') {
            if (nextArg.equals("-p")) {
                idx++;
                if (idx == argCount) {
                    //usage(1);
                }
                nextArg = args[idx];
                idx++;
                try {
                    port = Integer.decode(nextArg);
                } catch (NumberFormatException e) {
                    System.out.println("Install : invalid value for port " + nextArg);
                    //usage(1);
                }
            } else if (nextArg.equals("-h")) {
                idx++;
                if (idx == argCount) {
                    //usage(1);
                }
                nextArg = args[idx];
                idx++;
                host = nextArg;
            } else if (nextArg.equals("-b")) {
                idx++;
                addToBoot = true;
            } else if (nextArg.equals("-s")) {
                idx++;
                setPolicy = true;
            } else if (nextArg.equals("-m")) {
                idx++;
                useModuleLoader = true;
            } else if (nextArg.startsWith("-D")) {
                idx++;
                String prop = nextArg.substring(2);
                if (!prop.startsWith(DECOMPILER_PREFIX) || prop.contains(",")) {
                    System.out.println("Install : invalid property setting " + prop);
                    //usage(1);
                }
                props = props + ",prop:" + prop;
            } else if (nextArg.equals("--help")) {
                //usage(0);
            } else {
                System.out.println("Install : invalid option " + args[idx]);
                //usage(1);
            }
            if (idx == argCount) {
                //usage(1);
            }
            nextArg = args[idx];
        }

        if (idx != argCount - 1) {
            //usage(1);
        }

        // we actually allow any string for the process id as we can look up by name also
        id = nextArg;
    }

}
