/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.client.command.RequestQueue;
import com.redhat.thermostat.client.core.InformationService;
import com.redhat.thermostat.client.core.controllers.InformationServiceController;
import com.redhat.thermostat.client.swing.UIDefaults;
import com.redhat.thermostat.common.AllPassFilter;
import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.Filter;
import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAOImpl;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author pmikova
 */
public class VmDecompilerInformationService implements InformationService<VmRef> {

    private AgentInfoDAO agentInfoDao;

    private VmInfoDAO vmInfoDao;

    private VmDecompilerDAOImpl vmDecompilerDao;

    private RequestQueue requestQueue;

    private Map<VmRef, VmDecompilerInformationController> controllers = new ConcurrentHashMap<>();
    private SwingVmDecompilerViewProviderImpl provider;
    //private ApplicationService service;

    public VmDecompilerInformationService(/*ApplicationService service,*/ AgentInfoDAO agentInfoDao, VmInfoDAO vmInfoDao,
            RequestQueue queue, SwingVmDecompilerViewProviderImpl provider
            /*UIDefaults uiDefaults*/) {
        //this.service = service;
        this.agentInfoDao = agentInfoDao;
        this.vmInfoDao = vmInfoDao;
        this.requestQueue = queue;
        this.provider = provider;
    }

    @Override
    public int getOrderValue() {
        return Ordered.ORDER_USER_GROUP + 72;
    }

    @Override
    public Filter<VmRef> getFilter() {
        // dead vms can be useful also
        return new AllPassFilter<>();
    }

    @Override
    public InformationServiceController<VmRef> getInformationServiceController(VmRef ref) {
        if (controllers.get(ref) == null) {
            controllers.put(ref, new VmDecompilerInformationController(ref, agentInfoDao, vmInfoDao, vmDecompilerDao, requestQueue));
        }
        return controllers.get(ref);
    }
}
