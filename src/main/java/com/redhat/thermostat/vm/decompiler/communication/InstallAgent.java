/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.communication;

import com.sun.tools.attach.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author pmikova
 */
public class InstallAgent {
    
    // must be set
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
    private static final String defaultAgentPath = "/home/pmikova/NetBeansProjects/JavaAgent/target/JavaAgent-1.0-SNAPSHOT.jar";

    private static final String AGENT_LOADED_PROPERTY = "com.redhat.decompiler.thermostat.loaded";

      
    /**
     *  only this class creates instances
     */
    private InstallAgent(String pid, boolean addToBoot, String host, int port, String[] properties)
    {
        agentJar = null;
        modulePluginJar = null;
        this.id = pid;
        this.port = port;
        this.addToBoot = addToBoot;
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
    
    private void setAgent(String agentJar){
        this.agentJar = agentJar; 
    }
        
    
    public static void install(String pid, boolean addToBoot, String host, int  port, String[] properties)
              throws IllegalArgumentException, FileNotFoundException,
               IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException
    {
        
        String propVal = InstallAgent.getProperty(pid, AGENT_LOADED_PROPERTY);
            boolean loaded = Boolean.parseBoolean(propVal);
              
        if (port < 0) {
            throw new IllegalArgumentException("Install : port cannot be negative");
        }
        
        for (int i = 0; i < properties.length; i++) {
            String prop = properties[i];
            if (prop == null || prop.length()  == 0) {
                throw new IllegalArgumentException("Install : properties  cannot be null or \"\"");
            }
            if (prop.indexOf(',') >= 0) {
                throw new IllegalArgumentException("Install : properties may not contain ','");
            }
        }
        
        InstallAgent install = new InstallAgent(pid, addToBoot, host, port, properties);
        install.attach();
        install.setAgent(defaultAgentPath);
        install.injectAgent();
    }
    
      /**
     * attach to the Java process identified by the process id supplied on the command line
     */
    private void attach() throws AttachNotSupportedException, IOException, IllegalArgumentException
    {
        if (id.matches("[0-9]+")) {
            // integer process id
            int pid = Integer.valueOf(id);
            if (pid <= 0) {
                throw new IllegalArgumentException("Install : invalid pid " +id);
            }
            vm = VirtualMachine.attach(Integer.toString(pid));
        ///bit not so sure whether this is even neccessary....
        } else {
            // try to search for this VM with an exact match
            List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd: vmds) {
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
            for (VirtualMachineDescriptor vmd: vmds) {
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
     * get the attached process to upload and install the agent jar using whatever agent options were
     * configured on the command line
     */
    private void injectAgent() throws AgentLoadException, AgentInitializationException, IOException
    {
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
            
            if (props != null) {
                agentOptions += props;
            }
            
            vm.loadAgent(agentJar, agentOptions);
        } finally {
            vm.detach();
        }
    }
      
     public static String getProperty(String id, String property)
    {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(id);
            String value = (String)vm.getSystemProperties().get(property);
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
    
}
