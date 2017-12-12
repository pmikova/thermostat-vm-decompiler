/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author pmikova
 */
public class Communicate {

    private Socket commSocket;
    private BufferedReader commInput;
    private BufferedWriter commOutput;
    public static final String DEFAULT_ADDRESS = "localhost";

    public Communicate(int port) throws Exception{
        this(DEFAULT_ADDRESS, port);
    }

    public Communicate(String address, int port) throws Exception {

        this.commSocket = new Socket(address, port);
        InputStream is;
        try {
            is = this.commSocket.getInputStream();
        } catch (Exception e) {
            // oops. cannot handle this
            try {
                this.commSocket.close();
            } catch (Exception e1) {
                e1.printStackTrace();
                
            }
            throw e;
        }

        OutputStream os;
        try {
            os = this.commSocket.getOutputStream();
        } catch (Exception e) {
            // oops. cannot handle this
            try {
                this.commSocket.close();
            } catch (Exception e1) {
            }
            throw e;
        }

        this.commInput = new BufferedReader(new InputStreamReader(is));
        this.commOutput = new BufferedWriter(new OutputStreamWriter(os));

        return;
    }

    public void close() {
        try {
            this.commSocket.close(); // also closes the in/out streams
        } catch (Exception e) {
            e.printStackTrace();
            // TODO what should I do here? no need to abort, we are closing this object anyway
        } finally {
            // this object cannot be reused anymore, therefore, null everything out
            // which will force NPEs if attempts to reuse this object occur later
            //this.commSocket = null;
            this.commInput = null;
            this.commOutput = null;
        }
    }

    public String readResponse() throws Exception {
        String initLine = this.commInput.readLine().trim();
        if (initLine.equals("ERROR")) {
            //error log
            return "ERROR";
        } else if (initLine.equals("BYTES")) {
            String s = this.commInput.readLine();
            s = s.trim();
            return s;
        } else if (initLine.equals("CLASSES")) {
            StringBuilder str = new StringBuilder();
            while (true) {
                String s = this.commInput.readLine();
                if (s == null) {
                    break;
                }
                s = s.trim();
                if (!s.isEmpty()) {
                    str.append(s).append(";");
                }
            }
            return str.toString();
        } else {
            throw new RuntimeException("Unknow header of " + initLine);
        }
    }

    public void println(String line) throws IOException {
        this.commOutput.write(line);
        this.commOutput.newLine();
        this.commOutput.flush();
    }

    public void print(String line) throws IOException {
        this.commOutput.write(line);
        this.commOutput.flush();
    }

}
