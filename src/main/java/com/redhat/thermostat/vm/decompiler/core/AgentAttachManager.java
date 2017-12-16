package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.common.portability.ProcessUserInfo;
import com.redhat.thermostat.common.portability.ProcessUserInfoBuilder;
import com.redhat.thermostat.common.utils.LoggingUtils;
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
 * Attach manager for agent contains utility methods and information about 
 * attach.
 */
public class AgentAttachManager {
 
    private static final Logger logger = LoggingUtils.getLogger(AgentAttachManager.class); 
    private final FileSystemUtils fsUtils;
    private AgentLoader loader;
    private WriterID writerId;
    private ProcessUserInfoBuilder userInfoBuilder;
    private VmDecompilerDAO vmDecompilerDao;

      
    AgentAttachManager() {
        this.fsUtils = new FileSystemUtils();
    }

     void setAttacher(AgentLoader attacher) {
        this.loader = attacher;
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
        UserPrincipal owner = getUserPrincipalForPid(vmPid);
        if (owner == null) {
            return null;
        }
         int attachedPort = AgentLoader.INVALID_PORT;
        try {
            attachedPort = loader.attach(vmId.get(), vmPid, writerId.getWriterID());
        } catch (Exception ex) {
            Logger.getLogger(AgentAttachManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (attachedPort == AgentLoader.INVALID_PORT) {
            logger.warning("Failed to attach agent for VM '" + vmPid);
            return null;
        }
        VmDecompilerStatus status = new VmDecompilerStatus(writerId.getWriterID());
        status.setListenPort(attachedPort);
        status.setVmId(vmId.get());
        status.setTimeStamp(System.currentTimeMillis());
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

    

