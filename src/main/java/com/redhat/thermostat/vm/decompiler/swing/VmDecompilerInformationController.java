/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.swing;

import com.redhat.thermostat.storage.core.VmId;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.model.VmInfo;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import com.redhat.thermostat.client.core.controllers.InformationServiceController;
import com.redhat.thermostat.client.core.views.UIComponent;
import com.redhat.thermostat.shared.locale.LocalizedString;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.vm.decompiler.core.AgentRequestAction;
import com.redhat.thermostat.vm.decompiler.core.AgentRequestAction.RequestAction;
import com.redhat.thermostat.common.ActionEvent;
import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.shared.locale.Translate;
import java.io.FileOutputStream;
import java.io.IOException;
import com.redhat.thermostat.vm.decompiler.data.VmDecompilerDAO;
import com.redhat.thermostat.vm.decompiler.core.VmDecompilerStatus;
import java.util.concurrent.TimeUnit;

import com.redhat.thermostat.client.command.RequestQueue;
import com.redhat.thermostat.common.command.Request;
import com.redhat.thermostat.storage.core.AgentId;
import com.redhat.thermostat.storage.model.AgentInformation;
import com.redhat.thermostat.vm.decompiler.core.NativeAgentRequestResponseListener;
import com.redhat.thermostat.vm.decompiler.swing.BytecodeDecompilerView.DoActionBytes;
import com.redhat.thermostat.vm.decompiler.swing.BytecodeDecompilerView.DoActionClasses;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 *
 * @author pmikova
 */
public class VmDecompilerInformationController implements InformationServiceController<VmRef> {

    private final VmRef vm;
    private final AgentInfoDAO agentInfoDao;
    private final VmInfoDAO vmInfoDao;
    private final RequestQueue requestQueue;
    private final VmDecompilerDAO vmDecompilerDao;
    private final BytecodeDecompilerView view;
    private static final Translate<LocaleResources> translateResources = LocaleResources.createLocalizer();
    private String decompilerPath = "/home/pmikova/Desktop/procyon-decompiler-0.5.30.jar";

    VmDecompilerInformationController(VmRef ref, AgentInfoDAO agentInfoDao,
            VmInfoDAO vmInfo, VmDecompilerDAO vmDecompilerDao,
            RequestQueue requestQueue) {
        this(new BytecodeDecompilerView(), ref, agentInfoDao, vmInfo, vmDecompilerDao, requestQueue);
    }

    VmDecompilerInformationController(final BytecodeDecompilerView view, VmRef ref, AgentInfoDAO agentInfoDao, VmInfoDAO vmInfoDao, VmDecompilerDAO vmDecompilerDao, RequestQueue requestQueue) {
        this.vm = ref;
        this.agentInfoDao = agentInfoDao;
        this.vmInfoDao = vmInfoDao;
        this.requestQueue = requestQueue;
        this.vmDecompilerDao = vmDecompilerDao;
        this.view = view;

        view.addDoClassesActionListener(new ActionListener<DoActionClasses>() {

            @Override
            public void actionPerformed(ActionEvent<DoActionClasses> actionEvent) {
                DoActionClasses id = actionEvent.getActionId();

                switch (id) {
                    case CLASSES:
                        loadClassNames();
                        break;
                    default:
                        throw new AssertionError("Invalid action event: " + id);

                }
            }

        });

        view.addDoBytesActionListener(new ActionListener<DoActionBytes>(){
            @Override
            public void actionPerformed(ActionEvent<DoActionBytes> actionEvent) {
                //DoActionBytes id = actionEvent.getActionId();
                PassNameEvent<DoActionBytes> ae = (PassNameEvent<DoActionBytes> )actionEvent;
                //switch (id) {
                    //case BYTES:
                        loadClassBytecode(ae.getClassName());
                    //default:
                        //throw new AssertionError("Invalid action event: " + id);
                }
            //}
            });
    
    }
    private NativeAgentRequestResponseListener loadClassNames() {
        Request request = createRequest("", RequestAction.CLASSES);
        NativeAgentRequestResponseListener listener = submitRequest(request);
        boolean success = !listener.isError();
        if (success) {
            VmId vmId = new VmId(vm.getVmId());
            VmDecompilerStatus vmStatus = vmDecompilerDao.getVmDecompilerStatus(vmId);
            String[] classes = vmStatus.getLoadedClassNames();
            view.reloadClassList(classes);
        } else {
            view.handleError(new LocalizedString(listener.getErrorMessage()));
        }
        return listener;
    }

    private NativeAgentRequestResponseListener loadClassBytecode(String name) {   
        Request request = createRequest(name, RequestAction.BYTES);
        NativeAgentRequestResponseListener listener = submitRequest(request);
        String decompiledClass = "";
        boolean success = !listener.isError();
        if (success) {

            VmId vmId = new VmId(vm.getVmId());
            VmDecompilerStatus vmStatus = vmDecompilerDao.getVmDecompilerStatus(vmId);
            String decompiledClassInString = vmStatus.getLoadedClassBytes();

            String bytesInString = vmStatus.getLoadedClassBytes();
            byte[] bytes = parseBytes(bytesInString);
            try {
                String path = bytesToFile("temporary-byte-file", bytes);
                Process proc = Runtime.getRuntime().exec("java -jar " + decompilerPath + " " + path);
                InputStream in = proc.getInputStream();
                decompiledClass = new BufferedReader(new InputStreamReader(in))
  .lines().collect(Collectors.joining("\n"));;

            } catch (Exception e) {
                view.handleError(new LocalizedString(listener.getErrorMessage()));
            }
             
            view.reloadTextField(decompiledClass);
        } else {
            view.handleError(new LocalizedString(listener.getErrorMessage()));
        }

        return listener;
    }

    private Request createRequest(String className, RequestAction action) {
        System.out.println("init of creation");
        VmId vmId = new VmId(vm.getVmId());
        System.out.println("1");
        VmInfo vmInfo = createVmInfo(vm);
        System.out.println("2");
        VmDecompilerStatus status = vmDecompilerDao.getVmDecompilerStatus(vmId);
        System.out.println("3");
        int listenPort = AgentRequestAction.NOT_ATTACHED_PORT;
        if (status != null) {
            System.out.println(status.getListenPort());
            System.out.println(status.toString());
            listenPort = status.getListenPort();
        }
        System.out.println("4");
        AgentInformation agentInfo = agentInfoDao.getAgentInformation(new AgentId(vm.getHostRef().getAgentId()));
        System.out.println("5");
        InetSocketAddress address = agentInfo.getRequestQueueAddress();
        System.out.println("6");
        Request request;
        if (action == RequestAction.CLASSES) {
            request = AgentRequestAction.create(address, vmInfo, action, listenPort);
        } else if (action == RequestAction.BYTES) {
            request = AgentRequestAction.create(address, vmInfo, action, listenPort, className);
        } else {
            throw new AssertionError("Unknown action: " + action);
        }
        System.out.println("7");
        return request;
    }

    private VmInfo createVmInfo(VmRef vmRef) {
        // set up vmInfo with just enough data
        VmInfo vmInfo = new VmInfo();
        vmInfo.setAgentId(vm.getHostRef().getAgentId());
        vmInfo.setVmId(vm.getVmId());
        vmInfo.setVmPid(vm.getPid());
        return vmInfo;
    }

    private NativeAgentRequestResponseListener submitRequest(Request request) {
        CountDownLatch latch = new CountDownLatch(1);
        NativeAgentRequestResponseListener listener = new NativeAgentRequestResponseListener(latch);
        System.out.println("listener created");
        request.addListener(listener);
        System.out.println("added");
        requestQueue.putRequest(request);
        System.out.println("request put");
        try {
            // wait for request to finish
            latch.await(500000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        System.out.println("b4 return");
        return listener;
    }

    public String bytesToFile(String name, byte[] bytes) throws IOException {
        String path = "/tmp/" + name + ".class";
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(bytes);
        fos.close();
        return path;
    }

    @Override
    public UIComponent getView() {
        return view;

    }

    @Override
    public LocalizedString getLocalizedName() {
        return translateResources.localize(LocaleResources.VM_DECOMPILER_TAB_NAME);
    }

    private byte[] parseBytes(String bytes) {
        byte[] decoded = Base64.getDecoder().decode(bytes);
        return decoded;
    }
}
