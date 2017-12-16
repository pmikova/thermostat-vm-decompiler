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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is handling opening of communication socket and request submitting.
 *  
 */
public class CallDecompilerAgent {
    
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT= 5395;
    
    private final int port;
    private final String address;
    private static final Logger logger = LoggingUtils.getLogger(CallDecompilerAgent.class);
    
    /**
     * Constructor of the object
     * @param port port where to open socket
     * @param host socket host
     */
    public CallDecompilerAgent(int port, String host) {
        if (host == null) {
            host = DEFAULT_ADDRESS;
        }

        if (port <= 0) {
            port = DEFAULT_PORT;
        }

        this.address = host;
        this.port = port;
        logger.log(Level.FINEST, "Port assigned to: " + port + ", host: " + host);
    }

    
    /**
     * Opens a socket and sends the request to the agent via socket. 
     * @param request either "CLASSES" or "BYTES \n className", other formats
     * are refused
     * @return agents response or null
     */
    public String submitRequest(final String request){
        final Communicate comm = new Communicate(this.address, this.port);
        try {
            comm.println(request);
            String results = comm.readResponse();
            return results;
        }catch(IOException ex){
            logger.log(Level.SEVERE, "Communication with the agent failed,"
                    + " could not send a request.");
            return null;
        }finally {
            comm.close();
        }
    
    }
}
