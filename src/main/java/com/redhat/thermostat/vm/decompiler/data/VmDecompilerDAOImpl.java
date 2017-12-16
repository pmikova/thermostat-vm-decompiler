/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */
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
//import com.redhat.thermostat.vm.decompiler.core.StoreJvmInfo;
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

    public static final Category<VmDecompilerStatus> VM_DECOMPILER_STATUS_CATEGORY = new Category<>(
            "vm-decompiler-status",
            VmDecompilerStatus.class,
            Key.AGENT_ID, Key.VM_ID, Key.TIMESTAMP, PORT, LOADED_CLASS_NAMES, LOADED_CLASS_BYTES);

    public static final String QUERY_VM_DECOMPILER_STATUS = "QUERY " + VM_DECOMPILER_STATUS_CATEGORY.getName()
            + " WHERE '" + Key.VM_ID.getName() + "' = ?s LIMIT 1";

    public static final String REPLACE_OR_ADD_STATUS_DESC = "REPLACE " + VM_DECOMPILER_STATUS_CATEGORY.getName()
            + " SET '" + Key.AGENT_ID.getName() + "' = ?s , "
            + "'" + Key.VM_ID.getName() + "' = ?s , "
            + "'" + Key.TIMESTAMP.getName() + "' = ?l , "
            + "'" + PORT.getName() + "' = ?i , "
            + "'" + LOADED_CLASS_NAMES.getName() + "' = ?s[ , "
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
                preparedStatement.setString(5, status.getLoadedClassBytes());
                preparedStatement.setString(6, status.getVmId());
                return preparedStatement;
            }
        });

    }

}
