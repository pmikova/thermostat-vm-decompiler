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
public class CallDecompilerAgent {
    
    
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT= 5395;
    
    private final int port;
    private final String address;

    private PrintStream out;
    
    public CallDecompilerAgent(int port) {
        this(DEFAULT_ADDRESS, port, System.out);
    }
    
    
    public CallDecompilerAgent(String address, int port, PrintStream out) {
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
    
    public String submitRequest(final String request) throws Exception {
        final Communicate comm = new Communicate(this.address, this.port);
        try {
            comm.println(request);
            String results = comm.readResponse();
            return results;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }finally {
            comm.close();
        }
    
    }
}
