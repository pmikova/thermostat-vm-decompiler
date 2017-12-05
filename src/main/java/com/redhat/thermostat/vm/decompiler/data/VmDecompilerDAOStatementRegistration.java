/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.data;

import com.redhat.thermostat.storage.core.auth.StatementDescriptorRegistration;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author pmikova
 */
public class VmDecompilerDAOStatementRegistration implements StatementDescriptorRegistration {

    @Override
    public Set<String> getStatementDescriptors() {
        Set<String> descs = new HashSet<>(2);
        descs.add(VmDecompilerDAOImpl.REPLACE_OR_ADD_STATUS_DESC);
        descs.add(VmDecompilerDAOImpl.QUERY_VM_DECOMPILER_STATUS);
        return descs;
    }



}
