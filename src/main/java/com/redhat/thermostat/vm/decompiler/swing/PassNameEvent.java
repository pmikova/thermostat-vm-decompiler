package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.common.ActionEvent;

/**
 * Implementation of ActionEvent is saving class name as its parameter
 * @param <T> enum type of the request
 */
public class PassNameEvent<T extends Enum<?>> extends ActionEvent{
    
    private String className;
     private T actionId;
    
    /**
     * Constructor of the event
     * @param source source of the event
     * @param actionId id of action that will be performed
     * @param className name of class to get bytecode
     */
    public PassNameEvent(Object source, Enum actionId, String className) {
        super(source, actionId);
        this.className = className;
    }
    
    public String getClassName(){
        return className;
    }
    
}
