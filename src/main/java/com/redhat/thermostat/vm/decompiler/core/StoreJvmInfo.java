/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.storage.core.Entity;
import com.redhat.thermostat.storage.core.Persist;
import com.redhat.thermostat.storage.model.Pojo;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author pmikova
 */
//@Entity
public class StoreJvmInfo implements Pojo{
    
    ArrayList<String> loadedClassNames;
    HashMap<String, byte[]> loadedClassBytes;
    
    public StoreJvmInfo(){
        loadedClassNames = new ArrayList<>();
        loadedClassBytes = new HashMap<>();
             
    }
    //@Persist
    public void addClassBytes(String key, byte[] value){
        loadedClassBytes.put(key, value);
        
    }
    //@Persist
    public void setClassNames(ArrayList<String> names){
        cleanUp();
        loadedClassNames = names;
        
    }
    //@Persist
    public void cleanUp(){
        loadedClassNames = new ArrayList<>();
        loadedClassBytes = new HashMap<>();
    }
    //@Persist
    public ArrayList<String> getClassNames(){
        return loadedClassNames;
    }
    //@Persist
    public byte[] getClassBytes(String name){
        byte[] byteArray = loadedClassBytes.get(name);
        return byteArray;
    }
    
    
    
}
