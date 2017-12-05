/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;


import java.util.Objects;

/**
 *
 * @author pmikova
 */
public class AgentInfo {
    private final int vmPid;
    private final int agentListenPort;
    private final String listenHost;
    private final String vmId;
    private final String writerId;
    private final boolean isAttachFailedNoProc;
    private final boolean isOldAttach;
    
    AgentInfo(int vmPid, int agentListenPort, String listenHost, String vmId, String writerId, boolean isAttachFailedNoProc, boolean isOldAttach) {
        this.agentListenPort = agentListenPort;
        this.listenHost = listenHost;
        this.vmPid = vmPid;
        this.vmId = vmId;
        this.writerId = writerId;
        this.isAttachFailedNoProc = isAttachFailedNoProc;
        this.isOldAttach = isOldAttach;
    }

    int getVmPid() {
        return vmPid;
    }

    int getAgentListenPort() {
        return agentListenPort;
    }

    String getListenHost() {
        return listenHost;
    }

    String getVmId() {
        return vmId;
    }
    
    String getWriterId() {
        return writerId;
    }
    
    boolean isAttachFailedNoSuchProcess() {
        return isAttachFailedNoProc;
    }
    
    boolean isOldAttach() {
        return isOldAttach;
    }
   
    
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() != AgentInfo.class) {
            return false;
        }
        AgentInfo o = (AgentInfo) other;
        return Objects.equals(agentListenPort, o.agentListenPort) &&
                Objects.equals(listenHost, o.listenHost) &&
                Objects.equals(vmPid, o.vmPid) &&
                Objects.equals(vmId, o.vmId) &&
                Objects.equals(writerId, o.writerId) &&
                Objects.equals(isAttachFailedNoProc, o.isAttachFailedNoProc) &&
                Objects.equals(isOldAttach, o.isOldAttach);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(agentListenPort, listenHost, vmPid, vmId, writerId, isAttachFailedNoProc, isOldAttach);
    }
    
}
