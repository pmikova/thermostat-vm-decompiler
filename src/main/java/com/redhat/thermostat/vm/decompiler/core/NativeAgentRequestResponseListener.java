/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.core;

import com.redhat.thermostat.common.command.Request;
import com.redhat.thermostat.common.command.Response;
import com.redhat.thermostat.shared.locale.Translate;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author pmikova
 */
public class NativeAgentRequestResponseListener implements com.redhat.thermostat.common.command.RequestResponseListener {
    
    private final CountDownLatch latch;
    private String errorMsg = "";
    private boolean isError = false;
    
    public NativeAgentRequestResponseListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void fireComplete(Request request, Response response) {
        switch(response.getType()) {
        case AUTH_FAILED:
            isError = true;
            break;
        case ERROR:
            isError = true;
            break;
        case OK:
            break;
        default:
            isError = true;
        }
        latch.countDown();
    }
    
    public String getErrorMessage() {
        return errorMsg;
    }
    
    public boolean isError() {
        return isError;
    }

}