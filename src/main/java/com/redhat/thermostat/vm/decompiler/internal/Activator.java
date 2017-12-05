package com.redhat.thermostat.vm.decompiler.internal;


import com.redhat.thermostat.client.command.RequestQueue;
import com.redhat.thermostat.client.core.InformationService;
import com.redhat.thermostat.client.swing.UIDefaults;
import com.redhat.thermostat.common.ApplicationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.redhat.thermostat.common.MultipleServiceTracker;
import com.redhat.thermostat.common.MultipleServiceTracker.Action;
import com.redhat.thermostat.storage.core.Storage;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAO;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAOImpl;
import com.redhat.thermostat.vm.decompiler.swing.SwingVmDecompilerViewProviderImpl;
import com.redhat.thermostat.vm.decompiler.swing.VmDecompilerInformationService;


public class Activator implements BundleActivator {

    
private MultipleServiceTracker multiTracker;
private ServiceRegistration<InformationService> reg1;
private static SwingVmDecompilerViewProviderImpl viewProvider;
    @Override
    public void start(final BundleContext context) throws Exception {
         viewProvider = new SwingVmDecompilerViewProviderImpl();
              context.registerService(SwingVmDecompilerViewProvider.class.getName()
                    , viewProvider, null);
        
        Class<?>[] dependentServices = new Class[] {
                Storage.class,
                WriterID.class,
                SwingVmDecompilerViewProvider.class,
                AgentInfoDAO.class,
                VmInfoDAO.class,
                RequestQueue.class,
                VmDecompilerDAO.class
        };
       
            
            
        // Track Storage and WriterID and register our new DAO once services
         // become available.
        ServicesAvailableAction action = new ServicesAvailableAction(context);
       multiTracker = new MultipleServiceTracker(context,
                                                  dependentServices,
                                                  action);
        multiTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        multiTracker.close();
    }
    
    private static final class ServicesAvailableAction implements Action {

        private final BundleContext context;
        private ServiceRegistration reg;
        
        
        private ServicesAvailableAction(BundleContext context) {
            this.context = context;
        }
        
        @Override
        public void dependenciesAvailable(MultipleServiceTracker.DependencyProvider services) {
            //ApplicationService service = services.get(ApplicationService.class);
                AgentInfoDAO agentInfoDao = services.get(AgentInfoDAO.class);
                VmInfoDAO vmInfoDao = services.get(VmInfoDAO.class);
                RequestQueue queue = services.get(RequestQueue.class);
                //UIDefaults uiDefaults = services.get(UIDefaults.class);
              
                //SwingVmDecompilerViewProviderImpl viewProvider = services.get(SwingVmDecompilerViewProviderImpl.class);
                
            Storage storage = services.get(Storage.class);
            VmDecompilerDAO daoImpl = new VmDecompilerDAOImpl(storage);
            reg = context.registerService(VmDecompilerDAO.class.getName(), daoImpl, null);
            
            InformationService<VmRef> provideService = new VmDecompilerInformationService(
                    /*service,*/ agentInfoDao, vmInfoDao, queue, viewProvider/*uiDefaults*/);
           
        }

        @Override
        public void dependenciesUnavailable() {
           
            if (reg != null) {
                reg.unregister();
                
            }



    }

    
    }
    
}
