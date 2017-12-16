package com.redhat.thermostat.vm.decompiler.data;


import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.vm.decompiler.core.VmDecompilerStatus;


/**
 * Interface for DAO object to be registered.
 */
public interface VmDecompilerDAO {
  
    /**
     * This method should contain prepared statement for REPLACE 
     * VmDecompilerStatus 
     * @param status VmDecompilerStatus to replace
     */
    void addOrReplaceVmDecompilerStatus(VmDecompilerStatus status);
    
    /**
     * This method should contain prepared statement for QUERY 
     * VmDecompilerStatus
     * @param vmId serves as unique to find VmDecompilerStatus in storage
     * @return
     */
    VmDecompilerStatus getVmDecompilerStatus(VmId vmId);
    
}
