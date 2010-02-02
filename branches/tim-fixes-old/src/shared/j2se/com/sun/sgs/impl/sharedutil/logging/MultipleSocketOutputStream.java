/*
 * Copyright 2007-2008 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.impl.sharedutil.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Defines an {@code OutputStream} that sends its output to each of the output
 * streams of a collection of sockets, removing and closing a socket if
 * attempting to perform output to it causes an I/O failure.
 */
public class MultipleSocketOutputStream extends OutputStream {

    /** The sockets. */
    private final List<Socket> sockets = new ArrayList<Socket>();

    /** Creates an instance of this class. */
    MultipleSocketOutputStream() { }
    
    /**
     * Adds a socket.
     *
     * @param socket the socket
     */
    public void addSocket(Socket socket) {
	if (socket == null) {
	    throw new NullPointerException("The argument must not be null");
	}
	synchronized (sockets) {
	    sockets.add(socket);
	}
    }

    /* -- Implement OutputStream -- */

    /** {@inheritDoc} */
    public void write(int b) throws IOException {
	synchronized (sockets) {
	    for (Iterator<Socket> i = sockets.iterator(); i.hasNext(); ) {
		Socket socket = i.next();
		try {
		    socket.getOutputStream().write(b);
		} catch (IOException e) {
		    i.remove();
		    try {
			socket.close();
		    } catch (IOException e2) {
		    }
		}
	    }
	}
    }

    /** {@inheritDoc} */
    public void write(byte b[]) throws IOException {
	write(b, 0, b.length);
    }

    /** {@inheritDoc} */
    public void write(byte b[], int off, int len) throws IOException {
	if ((off | len | (b.length - (len + off)) | (off + len)) < 0) {
	    throw new IndexOutOfBoundsException();
	}
	synchronized (sockets) {
	    for (Iterator<Socket> i = sockets.iterator(); i.hasNext(); ) {
		Socket socket = i.next();
		try {
		    socket.getOutputStream().write(b, off, len);
		} catch (IOException e) {
		    i.remove();
		    try {
			socket.close();
		    } catch (IOException e2) {
		    }
		}
	    }
	}
    }

    /** {@inheritDoc} */
    public void flush() throws IOException {
	synchronized (sockets) {
	    for (Iterator<Socket> i = sockets.iterator(); i.hasNext(); ) {
		Socket socket = i.next();
		try {
		    socket.getOutputStream().flush();
		} catch (IOException e) {
		    i.remove();
		    try {
			socket.close();
		    } catch (IOException e2) {
		    }
		}
	    }
	}
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
	synchronized (sockets) {
	    for (Socket socket : sockets) {
		try {
		    socket.close();
		} catch (IOException e) {
		}
	    }
	    sockets.clear();
	}
    }
}
