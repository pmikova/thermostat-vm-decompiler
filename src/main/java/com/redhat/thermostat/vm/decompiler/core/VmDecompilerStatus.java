/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

/**
 *
 * @author pmikova
 */
import com.redhat.thermostat.storage.core.Entity;
import com.redhat.thermostat.storage.core.Persist;
import com.redhat.thermostat.storage.model.BasePojo;
import com.redhat.thermostat.storage.model.TimeStampedPojo;

@Entity
public class VmDecompilerStatus extends BasePojo implements TimeStampedPojo {

    private String vmId;
    private long timestamp;
    private int listenPort;
    String[] loadedClassNames;
    String loadedClassBytes;

    public VmDecompilerStatus(String writerId) {
        super(writerId);
        loadedClassNames = new String[]{};
        loadedClassBytes = "";
    }

    public VmDecompilerStatus() {
        super(null);
        loadedClassNames = new String[]{};
        loadedClassBytes = "";

    }

    @Persist
    public String getVmId() {
        return vmId;
    }

    @Persist
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Persist
    public void setListenPort(int port) {
        this.listenPort = port;
    }

    @Persist
    public int getListenPort() {
        return listenPort;
    }

    @Persist
    public void setTimeStamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Persist
    @Override
    public long getTimeStamp() {
        return timestamp;
    }

    @Persist
    public void setLoadedClassNames(String[] loadedClassNames) {
        this.loadedClassNames = loadedClassNames;

    }

    @Persist
    public String getLoadedClassBytes() {
        return loadedClassBytes;
    }

    @Persist
    public String[] getLoadedClassNames() {
        return loadedClassNames;
    }

    @Persist
    public void setLoadedClassBytes(String value) {
        loadedClassBytes = value;
    }

}
