/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.agent.ipc.server.ThermostatIPCCallbacks;
import com.redhat.thermostat.common.portability.ProcessUserInfo;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.vm.decompiler.core.VmDecompilerStatus;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAOImpl;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAO;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author pmikova
 */
public class AgentAttachManager {
 
    private static final Logger logger = LoggingUtils.getLogger(AgentAttachManager.class);

  
    private final FileSystemUtils fsUtils;
    private AgentLoader loader;
    private IPCManager ipcManager;
    private WriterID writerId;
    private ProcessUserInfoBuilder userInfoBuilder;
    private AgentInfo agent;
    private VmDecompilerDAO vmDecompilerDao;

    public AgentInfo getAgent(){
        return this.agent;
    }
        
    AgentAttachManager() {
        this.fsUtils = new FileSystemUtils();
    }

    //SHOULD NOT BE NECCESSARY
    AgentAttachManager(AgentLoader loader, IPCManager ipcManager, 
                              WriterID writerId, ProcessUserInfoBuilder userInfoBuilder, FileSystemUtils fsUtils, VmDecompilerDAO vmDecompilerDao) {
        this.loader = loader;
        this.ipcManager = ipcManager;
        this.writerId = writerId;
        this.userInfoBuilder = userInfoBuilder;
        this.fsUtils = fsUtils;
        this.vmDecompilerDao = vmDecompilerDao;
    }
    
     void setAttacher(AgentLoader attacher) {
        this.loader = attacher;
    }


    void setIpcManager(IPCManager ipcManager) {
        this.ipcManager = ipcManager;
    }

    void setVmDecompilerDao(VmDecompilerDAO vmDecompilerDao) {
        this.vmDecompilerDao = vmDecompilerDao;
    }

    void setWriterId(WriterID writerId) {
        this.writerId = writerId;
    }

    void setUserInfoBuilder(ProcessUserInfoBuilder userInfoBuilder) {
        this.userInfoBuilder = userInfoBuilder;
    }

    VmDecompilerStatus attachAgentToVm(VmId vmId, int vmPid)  {
        logger.fine("Attaching agent to VM '" + vmPid + "'");
        // Fail early if we can't determine process owner
        UserPrincipal owner = getUserPrincipalForPid(vmPid);
        if (owner == null) {
            return null;
        }
        AgentInfo info  = null;
        try {
            info = loader.attach(vmId.get(), vmPid, writerId.getWriterID());
        } catch (Exception ex) {
            Logger.getLogger(AgentAttachManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (info == null) {
            logger.warning("Failed to attach agent for VM '" + vmPid + "'. Skipping IPC channel.");
            return null;
        }
        VmSocketIdentifier socketId = new VmSocketIdentifier(vmId.get(), vmPid, writerId.getWriterID());

        ThermostatIPCCallbacks callback = (ThermostatIPCCallbacks) new MessageReciever(socketId);
        ipcManager.startIPCEndpoint(socketId, callback, owner);
        // Add a status record to storage
        VmDecompilerStatus status = new VmDecompilerStatus(writerId.getWriterID());
        status.setListenPort(info.getAgentListenPort());
        status.setVmId(vmId.get());
        vmDecompilerDao.addOrReplaceVmDecompilerStatus(status);
        return status;
    }

    private UserPrincipal getUserPrincipalForPid(int vmPid) {
        UserPrincipal principal = null;
        ProcessUserInfo info = userInfoBuilder.build(vmPid);
        String username = info.getUsername();
        if (username == null) {
            logger.warning("Unable to determine owner of VM '" + vmPid + "'. Skipping IPC channel.");
        } else {
            UserPrincipalLookupService lookup = fsUtils.getUserPrincipalLookupService();
            try {
                principal = lookup.lookupPrincipalByName(username);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Invalid user name '" + username + "' for VM '" + vmPid + "'. Skipping IPC channel.", e);
            }
        }
        return principal;
    }

    

    static class FileSystemUtils {
        UserPrincipalLookupService getUserPrincipalLookupService() {
            return FileSystems.getDefault().getUserPrincipalLookupService();
        }

    }
}

    

