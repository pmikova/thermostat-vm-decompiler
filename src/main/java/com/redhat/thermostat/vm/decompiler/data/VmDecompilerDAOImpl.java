package com.redhat.thermostat.vm.decompiler.data;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.core.Category;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.core.PreparedStatement;
import com.redhat.thermostat.storage.core.Storage;
import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.storage.dao.AbstractDao;
import com.redhat.thermostat.storage.dao.AbstractDaoQuery;
import com.redhat.thermostat.storage.dao.AbstractDaoStatement;
import com.redhat.thermostat.vm.decompiler.core.VmDecompilerStatus;
import java.util.List;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * Implementation of decompiler DAO to be used to store decompiler status in
 * Thermostat storage.
 */
@Component
@Service(value = VmDecompilerDAO.class)
public class VmDecompilerDAOImpl extends AbstractDao implements VmDecompilerDAO {

    static final Key<Integer> PORT = new Key<>("listenPort");
    static final Key<String[]> LOADED_CLASS_NAMES = new Key<>("loadedClassNames");
    static final Key<String[]> LOADED_CLASS_BYTES = new Key<>("loadedClassBytes");
    static final Key<String> BYTES_CLASS_NAME = new Key<>("bytesClassName");

    public static final Category<VmDecompilerStatus> VM_DECOMPILER_STATUS_CATEGORY = new Category<>(
            "vm-decompiler-status",
            VmDecompilerStatus.class,
            Key.AGENT_ID, Key.VM_ID, Key.TIMESTAMP, PORT, LOADED_CLASS_NAMES, BYTES_CLASS_NAME, LOADED_CLASS_BYTES);

    public static final String QUERY_VM_DECOMPILER_STATUS = "QUERY " + VM_DECOMPILER_STATUS_CATEGORY.getName()
            + " WHERE '" + Key.VM_ID.getName() + "' = ?s LIMIT 1";

    public static final String REPLACE_OR_ADD_STATUS_DESC = "REPLACE " + VM_DECOMPILER_STATUS_CATEGORY.getName()
            + " SET '" + Key.AGENT_ID.getName() + "' = ?s , "
            + "'" + Key.VM_ID.getName() + "' = ?s , "
            + "'" + Key.TIMESTAMP.getName() + "' = ?l , "
            + "'" + PORT.getName() + "' = ?i , "
            + "'" + LOADED_CLASS_NAMES.getName() + "' = ?s[ , "
            + "'" + BYTES_CLASS_NAME.getName() + "' = ?s , "
            + "'" + LOADED_CLASS_BYTES.getName() + "' = ?s WHERE "
            + "'" + Key.VM_ID.getName() + "' = ?s";

    @Reference
    private Storage storage;

    /*
    * Default constructor for declarative services
    */
    public VmDecompilerDAOImpl() {
    }

    @Activate
    private void activate() {
        storage.registerCategory(VM_DECOMPILER_STATUS_CATEGORY);
    }

    @Override
    protected Logger getLogger() {
        return LoggingUtils.getLogger(VmDecompilerDAOImpl.class);
    }

    /**
     * This method creates prepared statement for query and executes it.
     * @param vmId unique parameter to find the status
     * @return VmDecompiler status with given vmId
     */
    @Override
    public VmDecompilerStatus getVmDecompilerStatus(final VmId vmId) {
        List<VmDecompilerStatus> result = executeQuery(new AbstractDaoQuery<VmDecompilerStatus>(storage, VM_DECOMPILER_STATUS_CATEGORY, QUERY_VM_DECOMPILER_STATUS) {

            @Override
            public PreparedStatement<VmDecompilerStatus> customize(PreparedStatement<VmDecompilerStatus> preparedStatement) {
                preparedStatement.setString(0, vmId.get());
                return preparedStatement;
            }
        }).asList();
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    /**
     * This method should creates prepared statement and executes it 
     * @param status VmDecompilerStatus to replace
     */
    @Override
    public void addOrReplaceVmDecompilerStatus(final VmDecompilerStatus status) {
        executeStatement(new AbstractDaoStatement<VmDecompilerStatus>(storage, VM_DECOMPILER_STATUS_CATEGORY, REPLACE_OR_ADD_STATUS_DESC) {

            @Override
            public PreparedStatement<VmDecompilerStatus> customize(PreparedStatement<VmDecompilerStatus> preparedStatement) {
                preparedStatement.setString(0, status.getAgentId());
                preparedStatement.setString(1, status.getVmId());
                preparedStatement.setLong(2, status.getTimeStamp());
                preparedStatement.setInt(3, status.getListenPort());
                preparedStatement.setStringList(4, status.getLoadedClassNames());
                preparedStatement.setString(5, status.getBytesClassName());
                preparedStatement.setString(6, status.getLoadedClassBytes());
                preparedStatement.setString(7, status.getVmId());
                return preparedStatement;
            }
        });

    }

}
