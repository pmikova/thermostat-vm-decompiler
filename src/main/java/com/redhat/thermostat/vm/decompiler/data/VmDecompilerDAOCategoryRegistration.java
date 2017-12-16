package com.redhat.thermostat.vm.decompiler.data;

import com.redhat.thermostat.storage.core.auth.CategoryRegistration;
import java.util.HashSet;
import java.util.Set;

/**
 * Registers VM Decompiler Status Category.
 */
public class VmDecompilerDAOCategoryRegistration implements CategoryRegistration{


    @Override
    public Set<String> getCategoryNames() {
        Set<String> categories = new HashSet<>(1);
        categories.add(VmDecompilerDAOImpl.VM_DECOMPILER_STATUS_CATEGORY.getName());
        return categories;
    }
    

}

