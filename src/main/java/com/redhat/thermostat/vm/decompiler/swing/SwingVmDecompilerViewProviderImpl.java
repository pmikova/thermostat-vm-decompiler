/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.vm.decompiler.internal.SwingVmDecompilerViewProvider;

/**
 *
 * @author pmikova
 */
public class SwingVmDecompilerViewProviderImpl implements SwingVmDecompilerViewProvider {
    @Override
    public BytecodeDecompilerView createView() {
        return new BytecodeDecompilerView();
}
}
