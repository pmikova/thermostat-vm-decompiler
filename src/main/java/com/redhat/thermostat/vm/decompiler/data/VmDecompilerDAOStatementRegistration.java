package com.redhat.thermostat.vm.decompiler.data;

import com.redhat.thermostat.storage.core.auth.StatementDescriptorRegistration;
import java.util.HashSet;
import java.util.Set;

/**
 * Registers statement descriptors.
 */
public class VmDecompilerDAOStatementRegistration implements StatementDescriptorRegistration {

    @Override
    public Set<String> getStatementDescriptors() {
        Set<String> sd = new HashSet<>(2);
        sd.add(VmDecompilerDAOImpl.REPLACE_OR_ADD_STATUS_DESC);
        sd.add(VmDecompilerDAOImpl.QUERY_VM_DECOMPILER_STATUS);
        return sd;
    }



}
