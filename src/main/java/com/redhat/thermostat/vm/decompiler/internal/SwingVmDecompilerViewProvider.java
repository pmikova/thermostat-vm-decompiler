/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.internal;

import com.redhat.thermostat.client.core.views.ViewProvider;
import com.redhat.thermostat.vm.decompiler.swing.BytecodeDecompilerView;

/**
 *
 * @author pmikova
 */
public interface SwingVmDecompilerViewProvider extends ViewProvider {

    @Override
    public BytecodeDecompilerView createView() ;
}