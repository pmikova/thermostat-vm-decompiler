/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.shared.locale.Translate;

/**
 *
 * @author pmikova
 */


public enum LocaleResources {

    VM_DECOMPILER_TAB_NAME,
    DECOMPILER_HEADER_TITLE,
    ;
    
    static final String RESOURCE_BUNDLE = "com.redhat.thermostat.vm.decompiler.swing.internal.strings";
    
    public static Translate<LocaleResources> createLocalizer() {
        return new Translate<>(RESOURCE_BUNDLE, LocaleResources.class);
    }
}

    

