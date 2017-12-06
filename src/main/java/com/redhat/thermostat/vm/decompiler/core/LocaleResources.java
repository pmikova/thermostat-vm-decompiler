/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.shared.locale.Translate;

/**
 *
 * @author pmikova
 */
public enum LocaleResources {
    
    REQUEST_FAILED_AUTH_ISSUE,
    REQUEST_FAILED_UNKNOWN_ISSUE,
    ERROR_UNKNOWN_RESPONSE,
    ;
    
    static final String RESOURCE_BUNDLE = "com.redhat.thermostat.vm.decompiler.core.strings";
    
    public static Translate<LocaleResources> createLocalizer() {
        return new Translate<>(RESOURCE_BUNDLE, LocaleResources.class);
    }
}
