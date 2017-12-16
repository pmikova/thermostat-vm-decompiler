package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.client.command.RequestQueue;
import com.redhat.thermostat.client.core.InformationService;
import com.redhat.thermostat.client.core.controllers.InformationServiceController;
import com.redhat.thermostat.common.AllPassFilter;
import com.redhat.thermostat.common.Filter;
import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAO;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This implementation of information service provides methods for
 * creating decompiler tab for each VM, where available.
 */
@Component
@Service(value = InformationService.class)
@Properties({
    @Property(name = "GenericClassName",
            value = "com.redhat.thermostat.storage.core.VmRef")
    ,
    @Property(name = "com.redhat.thermostat.client.core.InformationService.serviceID",
            value = "com.redhat.thermostat.vm.decompiler.swing.VmDecompilerInformationService")
})
public class VmDecompilerInformationService implements InformationService<VmRef> {

    @Reference
    private AgentInfoDAO agentInfoDao;
    @Reference
    private VmInfoDAO vmInfoDao;
    @Reference
    private VmDecompilerDAO vmDecompilerDao;
    @Reference
    private RequestQueue requestQueue;

    private Map<VmRef, VmDecompilerInformationController> controllers = new ConcurrentHashMap<>();

    @Override
    public int getOrderValue() {
        return Ordered.ORDER_USER_GROUP + 72;
    }

    @Override
    public Filter<VmRef> getFilter() {
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
