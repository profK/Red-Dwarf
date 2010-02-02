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

package com.sun.sgs.example.request.client;

import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a client that uses the {@code RequestApp} application to simulate a
 * game with wandering players. <p>
 *
 * Each {@code WandererClient} steps randomly around a board, sending messages
 * to the channel associated with its local neighborhood, and reading and
 * writing values associated with its current location.  The resulting
 * application serves as a stress test for the channel and data services,
 * performing operations that each include a channel send, and twice as many
 * data service reads as writes. <p>
 *
 * Clients can handle disconnects, and the {@link #main main} method arranges
 * to run multiple clients. <p>
 *
 * This class supports the following properties:
 * <ul>
 * <li> {@code com.sun.sgs.example.request.client.wanderer.host} - The
 *	application host name, defaults to {@code localhost}
 * <li> {@code com.sun.sgs.example.request.client.wanderer.port} - The
 *	application port; defaults to {@code 11469}, the default port for the
 *	{@code RequestApp} application
 * <li> {@code com.sun.sgs.example.request.client.wanderer.clients} - The
 *	number of clients run by {@code main}, defaults to {@code 10}
 * <li> {@code com.sun.sgs.example.request.client.wanderer.sleep} - The number
 *	of milliseconds to wait between steps, defaults to {@code 200}
 * <li> {@code com.sun.sgs.example.request.client.wanderer.size} - The width
 *	and height of the board, defaults to {@code 1000}
 * <li> {@code com.sun.sgs.example.request.client.wanderer.sector} - The size
 *	of the neighborhood to assign to a single channel for movement
 *	notifications, defaults to {@code 100}
 * <li> {@code com.sun.sgs.example.request.client.wanderer.report} - The number
 *	of seconds between logging performance data, defaults to {@code 5}
 * </ul> <p>
 *
 * This class uses the {@link Logger} named {@code
 * com.sun.sgs.example.request.client.wanderer} to log at the following levels:
 * <ul>
 * <li> {@link Level#INFO Level.INFO} - Initialization, performance data
 * <li> {@link Level#FINE Level.FINE} - Login, disconnect, reconnection
 * <li> {@link Level#FINER Level.FINER} - Send and receive messages
 * <li> {@link Level#FINEST Level.FINEST} - Exceptions
 * </ul>
 */
public class ChatClient implements Runnable, SimpleClientListener {

    /** The prefix for properties. */
    private static final String PREFIX =
	"com.sun.sgs.example.request.client.chat";

    /** The application host name. */
    private static final String HOST =
	System.getProperty(PREFIX + ".host", "localhost");

    /** The application port. */
    private static final int PORT =
	Integer.getInteger(PREFIX + ".port", 11469);

    /** The number of clients run by main. */
    private static final int CLIENTS =
	Integer.getInteger(PREFIX + ".clients", 100);

    /** The number of clients per channel. */
    private static final int CLIENTS_PER_CHANNEL =
	Integer.getInteger(PREFIX + ".clients.per.channel", 10);

    /** The chat message size in bytes. */
    private static final int MESSAGE_SIZE =
	Integer.getInteger(PREFIX + ".message.size", 100);

    /**
     * The number of milliseconds between sending chat messages, give or take
     * the value of {@link #RANDOM}.
     */
    private static final long CHAT_PERIOD =
	Integer.getInteger(PREFIX + ".chat.period", 1000);

    /**
     * The number of milliseconds between switching channels, give or take the
     * value of {@link #RANDOM}.
     */
    private static final long CHANNEL_PERIOD =
	Integer.getInteger(PREFIX + ".channel.period", 60000);

    /** The number of milliseconds to perturb times, to avoid storms. */
    private static final int RANDOM =
	Integer.getInteger(PREFIX + ".random", 50);

    /** The number of seconds between logging performance data. */
    private static final int REPORT =
	Integer.getInteger(PREFIX + ".report", 5);

    /**
     * The minimum number of milliseconds to wait for a login attempt to
     * succeed.
     */
    private static final long LOGIN_MIN_RETRY =
	Long.getLong(PREFIX + ".login.min.retry", 5000);

    /**
     * The maximum number of milliseconds to wait for a login attempt to
     * succeed.
     */
    private static final long LOGIN_MAX_RETRY =
	Long.getLong(PREFIX + ".login.max.retry", 30000);

    /** The logger for this class. */
    private static final Logger logger = Logger.getLogger(PREFIX);

    /** A random number generator used to random behavior. */
    private static final Random random = new Random();

    /** The client used to communicate with the server. */
    private final SimpleClient simpleClient;

    /** The login properties. */
    private final Properties props;

    /** The name of the user. */
    private final String user = "User-" + random.nextInt(Integer.MAX_VALUE);

    /** The number of channels. */
    private final int numChannels = CLIENTS / CLIENTS_PER_CHANNEL;

    /**
     * The number of the current channel, or -1 if not joined to a channel yet.
     */
    private int channel = -1;

    /** The number of messages sent. */
    private int sent = 0;

    /** The number of messages received. */
    private int received = 0;

    /**
     * True if this client is logged in and sending messages without getting
     * exceptions.
     */
    private boolean active = false;

    /** True if this client is logged in but is getting exceptions. */
    private boolean failing = false;

    /** True if this client is not logged in. */
    private boolean disconnected = true;

    private boolean loggedIn = false;

    /**
     * Starts up clients.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
	if (logger.isLoggable(Level.INFO)) {
	    logger.log(Level.INFO,
		       "Creating ChatClients:" +
		       "\n  host: " + HOST +
		       "\n  port: " + PORT +
		       "\n  clients: " + CLIENTS +
		       "\n  clients.per.channel: " + CLIENTS_PER_CHANNEL +
		       "\n  message.size: " + MESSAGE_SIZE +
		       "\n  chat.period: " + CHAT_PERIOD +
		       "\n  channel.period: " + CHANNEL_PERIOD);
	}
	ChatClient[] clients = new ChatClient[CLIENTS];
	for (int i = 0; i < CLIENTS; i++) {
	    clients[i] = new ChatClient();
	}
	long until = System.currentTimeMillis() + (REPORT * 1000);
	until -= until % (REPORT * 1000);
	while (true) {
	    long now = System.currentTimeMillis();
	    if (now < until) {
		try {
		    Thread.sleep(until - now);
		} catch (InterruptedException e) {
		}
		continue;
	    }
	    Stats stats = new Stats();
	    for (ChatClient client : clients) {
		client.tally(stats);
	    }
	    if (logger.isLoggable(Level.INFO)) {
		logger.log(Level.INFO, stats.report());
	    }
	    until += (REPORT * 1000);
	}
    }

    /**
     * Creates an instance, and starts a thread to login and perform actions.
     */
    public ChatClient() {
        props = new Properties();
        props.setProperty("host", HOST);
        props.setProperty("port", String.valueOf(PORT));
        simpleClient = new SimpleClient(this);
	new Thread(this, "ChatClient[" + user + "]").start();
    }

    /* -- Implement Runnable -- */

    /** Performs client actions. */
    public void run() {
	long lastChannelSwitch = 0;
	for (int i = 0; true; i++) {
	    if (getDisconnected()) {
		login();
	    }
	    try {
		long now = System.currentTimeMillis();
		String message = "";
		if (now - lastChannelSwitch > CHANNEL_PERIOD) {
		    message = switchChannels() + "\n";
		    lastChannelSwitch = now;
		}
		send(message + chat());
		sleep();
	    } catch (Exception e) {
		noteFailing();
		if (logger.isLoggable(Level.FINEST)) {
		    logger.log(Level.FINEST, "Exception thrown", e);
		}
	    }
	}
    }
    
    /* -- Implement SimpleClientListener -- */

    /**
     * {@inheritDoc} <p>
     *
     * This implementation returns password authentication for the current
     * user.
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, new char[0]);
    }

    /**
     * {@inheritDoc} <p>
     *
     * This implementation notifies the client thread that the client is
     * active.
     */
    public void loggedIn() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, user + ": Logged in");
        }
        noteActive();
    }

    /**
     * {@inheritDoc}
     */
    public void loginFailed(String reason) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,
                       user + ": Login failed: " + reason);
        }
    }

    /* -- Implement ServerSessionListener -- */

    /** {@inheritDoc} */
    public void receivedMessage(ByteBuffer message) {
        String string = bufferToString(message);
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, user + ": Received: " + string);
        }
        noteReceived();
    }

    /** {@inheritDoc} */
    public void reconnecting() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, user + ": Reconnecting");
        }
    }

    /** {@inheritDoc} */
    public void reconnected() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, user + ": Reconnected");
        }
    }

    /** {@inheritDoc} */
    public void disconnected(boolean graceful, String reason) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,
                       user + ": Disconnected graceful:" + graceful +
                       ", reason:" + reason);
        }
        noteDisconnected();
    }

    /* -- Other methods -- */

    /** Performs a login to the current host, waiting as needed. */
    private void login() {
        long retry = LOGIN_MIN_RETRY;
        while (true) {    
            /* Wait randomly, to avoid login storms. */
            try {
                Thread.sleep(random.nextInt(RANDOM));
            } catch (InterruptedException e) {
            }
            
            long start = System.currentTimeMillis();
            
            try {
                simpleClient.login(props);
                long next = start + retry;
                long wait = next - System.currentTimeMillis();
                while (wait > 0 &&
                       getDisconnected())
                {
                    try {
                        synchronized (this) {
                            wait(wait);
                        }
                    } catch (InterruptedException e) {
                    }
                    wait = next - System.currentTimeMillis();
                }
                if (!getDisconnected()) {
                    return;
                }
            } catch (Exception e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, user + ": Login failed: " + e);
                }
            }
            /* Back off by doubling the wait time, up to the maximum. */
            retry = Math.min(retry * 2, LOGIN_MAX_RETRY);
        }

    }

    private String switchChannels() {
	String request = "";
	int newChannel;
	if (channel != -1) {
	    request = "LeaveChannel channel-" + channel + "\n";
	    channel = (channel + random.nextInt(numChannels)) % numChannels;
	} else {
	    channel = random.nextInt(numChannels);
	}
	request += "JoinChannel channel-" + channel;
	return request;
    }

    private String chat() {
	StringBuilder sb = new StringBuilder(MESSAGE_SIZE);
	for (int i = 0; i < MESSAGE_SIZE; i++) {
	    sb.append(Character.forDigit(i % 10, 10));
	}
	return "SendChannel channel-" + channel + " " + sb;
    }

    /** Sleeps for a random amount of time between moves. */
    private void sleep() throws InterruptedException {
	long n = CHAT_PERIOD - RANDOM + random.nextInt(2 * RANDOM);
	Thread.sleep(n);
    }

    /** Sends a message to the server. */
    private void send(String message) throws IOException {
	if (logger.isLoggable(Level.FINER)) {
	    logger.log(Level.FINER, user + ": Send: " + message);
	}
	simpleClient.send(stringToBuffer(message));
	noteSent();
    }

    /** Records that a message has been sent. */
    synchronized void noteSent() {
	sent++;
    }

    /** Records that a message has been received. */
    synchronized void noteReceived() {
	received++;
    }

    /** Records that the client is active, and notifies waiters. */
    synchronized void noteActive() {
	active = true;
	failing = false;
	disconnected = false;
	loggedIn = true;
	notifyAll();
    }

    /** Records that the client is failing. */
    synchronized void noteFailing() {
	active = false;
	failing = true;
    }

    /** Records that the client is disconnected. */
    synchronized void noteDisconnected() {
	active = false;
	failing = false;
	disconnected = true;
    }

    /** Returns whether the client is disconnected. */
    synchronized boolean getDisconnected() {
	return disconnected;
    }

    /**
     * Updates the argument with statistics from this client, and resets this
     * client's statistics.
     */
    synchronized void tally(Stats stats) {
	stats.sent += sent;
	sent = 0;
	stats.received += received;
	received = 0;
	if (active) { stats.active++; }
	if (failing) { stats.failing++; }
	failing = false;
	if (loggedIn) { stats.logins++; }
	loggedIn = false;
	if (disconnected) { stats.disconnected++; }
    }

    /** Converts a byte buffer into a string using UTF-8 encoding. */
    static String bufferToString(ByteBuffer buffer) {
	byte[] bytes = new byte[buffer.remaining()];
	buffer.get(bytes);
	try {
	    return new String(bytes, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    throw new AssertionError(e);
	}
    }

    /** Converts a string into a byte buffer using UTF-8 encoding. */
    static ByteBuffer stringToBuffer(String string) {
	try {
	    return ByteBuffer.wrap(string.getBytes("UTF-8"));
	} catch (UnsupportedEncodingException e) {
	    throw new AssertionError(e);
	}
    }

    /* -- Nested classes -- */

    /** Records client statistics. */
    private static class Stats {

	/** Messages sent. */
	private int sent = 0;

	/** Messages received. */
	private int received = 0;

	/** Clients active. */
	private int active = 0;

	/** Clients receiving exceptions. */
	private int failing = 0;

	/** Clients disconnected and in the processing of connecting. */
	private int disconnected = 0;

	/** Clients that newly logged in. */
	private int logins = 0;

	/** Creates an empty instance. */
	Stats() { }

	/** Returns a string that describes the client statistics. */
	String report() {
	    return "sent/sec=" + (sent / REPORT) +
		" received/sec=" + (received / REPORT) +
		" active=" + active +
		" failing=" + failing +
		" disconnected=" + disconnected +
		" logins=" + logins;
	}
    }
}
