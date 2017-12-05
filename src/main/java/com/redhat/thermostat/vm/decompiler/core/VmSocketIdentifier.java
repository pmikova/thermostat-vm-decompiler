package com.redhat.thermostat.vm.decompiler.core;

import java.util.Objects;

class VmSocketIdentifier {

    private static final int AGENT_ID_PART_LENGTH = 8;
    private static final int VM_ID_PART_LENGTH = 4;
    private static final String SOCKET_FORMAT = "%s_%s_%06d";
    private final int vmPid;
    private final String vmId;
    private final String agentId;
    
    VmSocketIdentifier(String vmId, int pid, String agentId) {
        this.vmId = Objects.requireNonNull(vmId);
        this.vmPid = pid;
        this.agentId = Objects.requireNonNull(agentId);
    }
    
    String getName() {
        int agentIdLength = Math.min(agentId.length(), AGENT_ID_PART_LENGTH);
        int vmIdLength = Math.min(vmId.length(), VM_ID_PART_LENGTH);
        String agentIdPart = agentId.substring(0, agentIdLength);
        String vmIdPart = vmId.substring(0, vmIdLength);
        return String.format(SOCKET_FORMAT, agentIdPart, vmIdPart, vmPid);
    }

    String getVmId() {
        return vmId;
    }

    String getAgentId() {
        return agentId;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() != VmSocketIdentifier.class) {
            return false;
        }
        VmSocketIdentifier o = (VmSocketIdentifier) other;
        return Objects.equals(vmPid, o.vmPid) &&
                Objects.equals(getAgentId(), o.getAgentId()) &&
                Objects.equals(getVmId(), o.getVmId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getVmId(), getAgentId(), vmPid);
    }
    
}
