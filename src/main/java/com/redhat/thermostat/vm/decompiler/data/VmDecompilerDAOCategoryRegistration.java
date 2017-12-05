/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.data;

import com.redhat.thermostat.storage.core.auth.CategoryRegistration;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author pmikova
 */
public class VmDecompilerDAOCategoryRegistration implements CategoryRegistration{


    @Override
    public Set<String> getCategoryNames() {
        Set<String> categories = new HashSet<>(1);
        categories.add(VmDecompilerDAOImpl.VM_DECOMPILER_STATUS_CATEGORY.getName());
        return categories;
    }
    

}

