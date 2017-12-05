/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.communication;

import java.io.PrintStream;

/**
 *
 * @author pmikova
 */
public class CallNativeAgent {
    
    
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT= 9091;
    
    private final int port;
    private final String address;

    private PrintStream out;
    
    public CallNativeAgent(int port) {
        this(DEFAULT_ADDRESS, port, System.out);
    }
    
    
    public CallNativeAgent(String address, int port, PrintStream out) {
        if (address == null) {
            address = DEFAULT_ADDRESS;
        }

        if (port <= 0) {
            port = DEFAULT_PORT;
        }

        if (out == null) {
            out = System.out;
        }

        this.address = address;
        this.port = port;
        this.out = out;
    }
    
        /**
     * Submits the generic request string to the Byteman agent for processing.
     *
     * @param request
     *            the request to submit
     *
     * @return the response that the Byteman agent replied with
     *
     * @throws Exception
     *             if the request failed
     */
    public String submitRequest(String request) throws Exception {
        Communicate comm = new Communicate(this.address, this.port);
        try {
            comm.print(request);
            String results = comm.readResponse();
            return results;
        } finally {
            comm.close();
        }
    }
    
}
