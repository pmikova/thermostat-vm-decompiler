/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.common.ActionEvent;

/**
 *
 * @author pmikova
 */
public class PassNameEvent<T extends Enum<?>> extends ActionEvent{
    
    private String className;
     private T actionId;
    
    public PassNameEvent(Object source, Enum actionId, String className) {
        super(source, actionId);
        this.className = className;
    }
    
    public String getClassName(){
        return className;
    }
    
}
