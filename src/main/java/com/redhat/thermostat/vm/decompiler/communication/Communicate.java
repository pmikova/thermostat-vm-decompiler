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
package com.redhat.thermostat.vm.decompiler.communication;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.decompiler.core.AgentAttachManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class opens a socket and contain methods for read and write to socket
 * IS/OS.
 */
public class Communicate {

    private Socket commSocket;
    private BufferedReader commInput;
    private BufferedWriter commOutput;

    public static final String DEFAULT_ADDRESS = "localhost";
    private static final Logger logger = LoggingUtils.getLogger(Communicate.class);

    /**
     * Constructor creates a socket on given port and saves the streams into
     * class variables.
     *
     * @param host host name
     * @param port port where we open the socket
     */
    public Communicate(String host, int port) {

        try {
            this.commSocket = new Socket(host, port);
        } catch (IOException ex) {
            Logger.getLogger(Communicate.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputStream is;
        try {
            is = this.commSocket.getInputStream();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Opening of input stream of a socket "
                    + "failed: " + e.getMessage());
            try {
                this.commSocket.close();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Error while closing the socket: "
                        + e1.getMessage());

            }
            return;
        }

        OutputStream os;
        try {
            os = this.commSocket.getOutputStream();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Opening of output stream of a socket "
                    + "failed: " + e.getMessage());
            try {
                this.commSocket.close();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Error while closing the socket: "
                        + "" + e1.getMessage());
            }
            return;
        }

        this.commInput = new BufferedReader(new InputStreamReader(is));
        this.commOutput = new BufferedWriter(new OutputStreamWriter(os));

        return;
    }

    /**
     * Closes a socket.
     */
    public void close() {
        try {
            this.commSocket.close(); // also closes the in/out streams
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while closing the socket: "
                    + "" + e.getMessage());

        } finally {
            this.commSocket = null;
            this.commInput = null;
            this.commOutput = null;
        }
    }

    /**
     * Method that reads agent's response.
     * @return "ERROR" in case of fail or corresponding bytes or class names
     */
    public String readResponse(){
        String initLine;
        try {
            initLine = this.commInput.readLine().trim();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Agent did not return anything.");
            return "ERROR";
        }
        
        if (initLine.equals("ERROR")) {
            logger.log(Level.SEVERE, "Agent returned error.");
            return "ERROR";
        } 
        
        else if (initLine.equals("BYTES")) {
            try {
                String s = this.commInput.readLine();
                s = s.trim();
                logger.log(Level.FINE, "Agent returned bytes.");
                return s;
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Can not read line, check "
                        + "agent communication output.");
            }

        } 
        
        else if (initLine.equals("CLASSES")) {
            StringBuilder str = new StringBuilder();
            while (true) {
                try {
                    String s = this.commInput.readLine();
                    if (s == null) {
                        break;
                    }
                    s = s.trim();
                    if (!s.isEmpty()) {
                        str.append(s).append(";");
                    }
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Can not read line, check "
                        + "agent communication output.");
                }
            }
            logger.log(Level.FINE, "Agent returned class names.");
            return str.toString();
        } 
        
        logger.log(Level.SEVERE, "Unknow header of " + initLine);
        return "ERROR";
    }

    /**
     * Sends a line with request to agent.
     * @param line "CLASSES" or "BYTES className"
     * @throws IOException if the write operation fails
     */
    public void println(String line) throws IOException {
        this.commOutput.write(line);
        this.commOutput.newLine();
        this.commOutput.flush();
    }

}
