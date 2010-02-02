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

package com.sun.sgs.impl.profile.listener;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.impl.profile.util.NetworkReporter;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.kernel.ResourceCoordinator;
import com.sun.sgs.kernel.TaskScheduler;
import com.sun.sgs.profile.ProfileListener;
import com.sun.sgs.profile.ProfileOperation;
import com.sun.sgs.profile.ProfileProperties;
import com.sun.sgs.profile.ProfileReport;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Defines a {@code ProfileListener} that provides information about operations
 * performed on average by the last fixed batch of tasks.  Prints information
 * to standard output, or via a socket. <p>
 *
 * This class supports the following configuration properties: <ul>
 *
 * <li> {@value ProfileProperties#WINDOW_SIZE} - the number of tasks for which
 *	to collect and average operations.  If not specified, defaults to
 *	{@value DEFAULT_WINDOW_SIZE}.
 *
 * <li> {@value PORT_PROPERTY} - the port number for receiving requests for
 *	profiling information.  If not specified, prints profiling information
 *	to {@link System#err System.err}.
 *
 * </ul>
 */
public class OperationListener implements ProfileListener {
    public static final int DEFAULT_WINDOW_SIZE = 5000;
    public static final String PORT_PROPERTY =
	OperationListener.class.getName() + ".report.port";
    private final int windowSize;
    private final NetworkReporter networkReporter;
    private final IntegerMap opsSuccess = new IntegerMap();
    private final IntegerMap opsAll = new IntegerMap();
    private int taskCount;
    private long windowStart;
    private final Map<Integer, ProfileOperation> registeredOps =
        new HashMap<Integer, ProfileOperation>();

    public OperationListener(Properties properties, Identity owner,
			     TaskScheduler taskScheduler,
			     ResourceCoordinator resourceCoord)
	throws IOException
    {
	PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
	windowSize = wrappedProps.getIntProperty(
	    ProfileProperties.WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
	windowStart = System.currentTimeMillis();
	String port = wrappedProps.getProperty(PORT_PROPERTY);
	networkReporter = (port != null) 
	    ? new NetworkReporter(Integer.parseInt(port), resourceCoord)
	    : null;
    }

    /* -- Implement ProfileListener -- */

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent event) {
	if (event.getPropertyName().equals("com.sun.sgs.profile.newop")) {
	    ProfileOperation op = (ProfileOperation) event.getNewValue();
	    registeredOps.put(op.getId(), op);
	}
    }
    
    /** {@inheritDoc} */
    public void report(ProfileReport report) {
	taskCount++;
	boolean success = report.wasTaskSuccessful();
	for (ProfileOperation op : report.getReportedOperations()) {
	    if (success) {
		opsSuccess.increment(op.getId());
	    }
	    opsAll.increment(op.getId());
	}
	if (taskCount % windowSize == 0) {
	    long windowEnd = System.currentTimeMillis();
	    Formatter results = new Formatter();
	    results.format("Operations for last %d of %d tasks" +
			   " (average succeeded/total):%n",
		windowSize, taskCount);
	    for (Entry<Integer, ProfileOperation> entry :
		     registeredOps.entrySet())
	    {
		Integer opId = entry.getKey();
		ProfileOperation op = entry.getValue();
		int all = opsAll.get(opId);
		if (all != 0) {
		    results.format("  %s: %1.1f/%1.1f%n",
				   op,
				   opsSuccess.get(opId) / (double) windowSize,
				   all / (double) windowSize);
		    opsSuccess.clear(opId);
		    opsAll.clear(opId);
		}
	    }
	    showOutput(results.toString());
	}
    }

    /** {@inheritDoc} */
    public void shutdown() { }

    /* -- Other methods and classes -- */

    private void showOutput(String output) {
	if (networkReporter != null) {
	    networkReporter.report(output);
	} else {
	    System.err.print(output);
	}
    }

    private static class IntegerMap {
	private final Map<Integer, Integer> map =
	    new HashMap<Integer, Integer>();
	Integer get(Integer key) {
	    Integer value = map.get(key);
	    return (value == null) ? 0 : value;
	}
	void increment(Integer key) {
	    Integer value = map.get(key);
	    map.put(key, (value == null) ? 1 : value + 1);
	}
	void clear(Integer key) {
	    map.put(key, 0);
	}
    }   
}
