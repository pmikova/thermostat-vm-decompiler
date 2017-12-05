/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.data;


import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.vm.decompiler.core.VmDecompilerStatus;


/**
 *
 * @author pmikova
 */
public interface VmDecompilerDAO {
  
    
    void addOrReplaceVmDecompilerStatus(VmDecompilerStatus status);
    
    VmDecompilerStatus getVmDecompilerStatus(VmId vmId);
    
}
