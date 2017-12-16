package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.storage.core.Entity;
import com.redhat.thermostat.storage.core.Persist;
import com.redhat.thermostat.storage.model.BasePojo;
import com.redhat.thermostat.storage.model.TimeStampedPojo;

/**
 * This class stores all information about the state of decompiler plugin on 
 * each VM.
 */
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

    /**
     * We need to initiate these arguments, so they are never null.
     */
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
