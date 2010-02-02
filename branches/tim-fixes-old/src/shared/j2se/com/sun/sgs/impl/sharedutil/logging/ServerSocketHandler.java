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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;

/**
 * Defines a logging {@link Handler} that creates a socket server and provides
 * logging output to any clients that connect to the server. <p>
 *
 * This class recognizes the following {@code LogManager} configuration
 * properties, using the default values if the properties specified have
 * invalid values:
 *
 * <dl style="margin-left: 1em">
 *
 * <dt> <i>Property:</i> <b>{@value #LEVEL_PROPERTY}</b> <br>
 *	<i>Default:</i> {@link Level#ALL Level.ALL}
 * <dd style="padding-top: .5em">
 *	Specifies the logging level for the handler. <p>
 *
 * <dt> <i>Property:</I> <b>{@value #FILTER_PROPERTY}</b> <br>
 *	<i>Default:</i> <i>No filter</i>
 * <dd style="padding-top: .5em">
 *	Specifies the name of the logging {@link Filter} class for the
 *	handler. <p>
 *
 * <dt> <i>Property:</i> <b>{@value #FORMATTER_PROPERTY}</b> <br>
 *	<i>Default:</i> {@link XMLFormatter java.util.logging.XMLFormatter}
 * <dd style="padding-top: .5em">
 *	Specifies the name of the {@link Formatter} class for the handler. <p>
 *
 * <dt> <i>Property:</i> <b>{@value #ENCODING_PROPERTY}</b> <br>
 *	<i>Default:</i> <i>the default platform encoding</i>
 * <dd style="padding-top: .5em">
 *	Specifies the name of the character set encoding for the handler. <p>
 *
 * <dt> <i>Property:</i> <b>{@value #HOST_PROPERTY}</b> <br>
 *	<i>Default:</i> <i>all local addresses</i>
 * <dd style="padding-top: .5em">
 *	Specifies the host name for creating the server socket that listens for
 *	connections. <p>
 *
 * <dt> <i>Property:</i> <b>{@value #PORT_PROPERTY}</b> <br>
 *	<i>No default &mdash; required</i>
 * <dd style="padding-top: .5em">
 *	Specifies the TCP port for creating the server socket that listens for
 *	connections.
 *
 * </dl> <p>
 *
 * The output IO stream is buffered, but is flushed after each {@code
 * LogRecord} is written.
 */
public class ServerSocketHandler extends StreamHandler {

    /** The logging property for specifying the logging level. */
    public static final String LEVEL_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ServerSocketHandler.level";

    /** The logging property for specifying the filter. */
    public static final String FILTER_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ServerSocketHandler.filter";

    /** The logging property for specifying the formatter. */
    public static final String FORMATTER_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ServerSocketHandler.formatter";

    /** The logging property for specifying the encoding. */
    public static final String ENCODING_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ServerSocketHandler.encoding";

    /** The logging property for specifying the host. */
    public static final String HOST_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ServerSocketHandler.host";

    /** The logging property for specifying the port. */
    public static final String PORT_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ServerSocketHandler.port";

    /** The output stream that writes to all connected sockets. */
    private final MultipleSocketOutputStream multiOut =
	new MultipleSocketOutputStream();

    private final Object lock = new Object();

    /** The host for listening. */
    private String host;

    /** The port for listening. */
    private int port;

    /** The server socket for listening. */
    private ServerSocket serverSocket;

    /** The listening thread. */
    private Thread listenerThread;

    /**
     * Creates an instance of this class, configured using {@code LogManager}
     * properties.
     *
     * @throws IllegalArgumentException if the port is not specified
     * @throws IOException if a problem occurs creating the listening socket
     */
    public ServerSocketHandler() throws IOException {
	setOutputStream(new BufferedOutputStream(multiOut));
	configure();
	listen();
    }

    /**
     * Creates an instance of this class with the specified host and port.
     *
     * @throws IllegalArgumentException if the port is invalid
     * @throws IOException if a problem occurs creating the listening socket
     */
    public ServerSocketHandler(String host, int port) throws IOException {
	if (host == null) {
	    throw new NullPointerException("The host must not be null");
	} else if (port == 0) {
	    throw new IllegalArgumentException("The port must not be zero");
	}
	setOutputStream(new BufferedOutputStream(multiOut));
	configure();
	this.host = host;
	this.port = port;
	listen();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
	super.close();
	synchronized (lock) {
	    if (listenerThread != null) {
		listenerThread.interrupt();
		listenerThread = null;
	    }
	    if (serverSocket != null) {
		try {
		    serverSocket.close();
		    serverSocket = null;
		} catch (IOException e) {
		}
	    }
	}
    }

    /** {@inheritDoc} */
    @Override
    public void publish(LogRecord record) {
	if (!isLoggable(record)) {
	    return;
	}
	super.publish(record);
	flush();
    }

    /** Processes log manager properties. */
    private void configure() {
        LogManager manager = LogManager.getLogManager();
	String cname = getClass().getName();
	setLevel(getLevelProperty(manager, cname + ".level"));
	setFilter(
	    getInstanceProperty(
		manager, cname + ".filter", Filter.class, null));
	setFormatter(
	    getInstanceProperty(
		manager, cname + ".formatter", Formatter.class,
		new XMLFormatter()));
	try {
	    setEncoding(manager.getProperty(cname +".encoding"));
	} catch (Exception e) {
	    try {
	        setEncoding(null);
	    } catch (Exception ex2) {
	    }
	}
	String portString = manager.getProperty(cname + ".port");
	port = (portString != null) ? Integer.parseInt(portString) : 0;
	host = manager.getProperty(cname + ".host");
    }

    /** Gets a logging level from a log manager property. */
    private static Level getLevelProperty(LogManager manager, String name) {
	String value = manager.getProperty(name);
	if (value != null) {
	    try {
		return Level.parse(value.trim());
	    } catch (Exception e) {
	    }
	}
	return Level.ALL;
    }

    /** Gets a class instance from a log manager property. */
    private static <T> T getInstanceProperty(
	LogManager manager, String name, Class<T> type, T defaultValue)
    {
	String value = manager.getProperty(name);
	System.err.println("getInstanceProperty name:" + name + ", value:" +
			   value);
	if (value != null) {
	    try {
		Class<?> cl = Class.forName(
		    value, false, ClassLoader.getSystemClassLoader());
		return cl.asSubclass(type).newInstance();
	    } catch (Exception ex) {
		System.err.println(ex);
	    }
	}
	return defaultValue;
    }	

    /** Starts listening. */
    private void listen() throws IOException {
	synchronized (lock) {
	    if (port == 0) {
		throw new IllegalArgumentException("Port must not be 0");
	    }
	    serverSocket = new ServerSocket(
		port, 0, host == null ? null : InetAddress.getByName(host));
	    listenerThread = new Thread(this + ": listen") {
		public void run() {
		    doListen();
		}
	    };
	}
	listenerThread.setDaemon(true);
	listenerThread.start();
    }

    /** The listening loop. */
    private void doListen() {
	while (true) {
	    ServerSocket ss;
	    synchronized (lock) {
		ss = serverSocket;
	    }
	    if (ss == null) {
		break;
	    }
	    try {
		Socket socket = ss.accept();
		multiOut.addSocket(socket);
	    } catch (Exception e) {
		if (ss.isClosed()) {
		    break;
		}
		reportError(null, e, ErrorManager.OPEN_FAILURE);
	    }
	}
    }
}
