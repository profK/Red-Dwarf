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
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.profile.ProfileListener;
import com.sun.sgs.profile.ProfileOperation;
import com.sun.sgs.profile.ProfileProperties;
import com.sun.sgs.profile.ProfileReport;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a {@code ProfileListener} that provides information about operations
 * performed on average by the last fixed batch of tasks.  Prints information
 * to standard output, or via a socket. <p>
 *
 * This class supports the following configuration properties: <ul>
 *
 * <li> {@value com.sun.sgs.profile.ProfileProperties#WINDOW_SIZE} - the number
 *	of tasks for which to collect and average operations.  If not
 *	specified, defaults to {@value DEFAULT_WINDOW_SIZE}.
 *
 * </ul>
 *
 * This class uses the {@link Logger} named {@code
 * com.sun.sgs.impl.profile.listener.OperationListener} to log at the following
 * levels:
 * <ul>
 * <li> {@link Level#INFO Level.INFO} - Profiling output
 * </ul>
 */
public class OperationListener implements ProfileListener {

    /** The default window size. */
    public static final int DEFAULT_WINDOW_SIZE = 10000;

    /** The logger for this class. */
    public static final Logger logger = Logger.getLogger(
	OperationListener.class.getName());

    private final int windowSize;
    private final IntegerMap opsSuccess = new IntegerMap();
    private final IntegerMap opsAll = new IntegerMap();
    private int taskCount;
    private long windowStart;
    private final Map<String, ProfileOperation> registeredOps =
        new TreeMap<String, ProfileOperation>();
    private final Map<String, Long> aggregateCounterMap =
	new TreeMap<String, Long>();
    private final Map<String, Range> taskCounterMap =
	new TreeMap<String, Range>();
    private final Map<String, Long> aggregateSampleMap =
	new TreeMap<String, Long>();
    private final Map<String, Range> taskSampleMap =
	new TreeMap<String, Range>();

    public OperationListener(Properties properties, Identity owner,
			     ComponentRegistry registry)
	throws IOException
    {
	PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
	windowSize = wrappedProps.getIntProperty(
	    ProfileProperties.WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
	windowStart = System.currentTimeMillis();
    }

    /* -- Implement ProfileListener -- */

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent event) {
	if (event.getPropertyName().equals("com.sun.sgs.profile.newop")) {
	    ProfileOperation op = (ProfileOperation) event.getNewValue();
	    registeredOps.put(op.getOperationName(), op);
	}
    }
    
    /** {@inheritDoc} */
    public void report(ProfileReport report) {
	taskCount++;
	boolean success = report.wasTaskSuccessful();
	for (ProfileOperation op : report.getReportedOperations()) {
	    if (success) {
		opsSuccess.increment(op.getOperationName());
	    }
	    opsAll.increment(op.getOperationName());
	}
	maxCounters(report.getUpdatedAggregateCounters(), aggregateCounterMap);
	sumCounters(report.getUpdatedTaskCounters(), taskCounterMap);
	maxSamples(report.getUpdatedAggregateSamples(), aggregateSampleMap);
	sumSamples(report.getUpdatedTaskSamples(), taskSampleMap);
	if (taskCount % windowSize == 0) {
	    Formatter results = new Formatter();
	    results.format("Values for last %d of %d tasks%n",
			   windowSize, taskCount);
	    long windowEnd = System.currentTimeMillis();
	    results.format("Operations (average succeeded/total):%n");
	    for (Entry<String, ProfileOperation> entry :
		     registeredOps.entrySet())
	    {
		String opName = entry.getKey();
		ProfileOperation op = entry.getValue();
		int all = opsAll.get(opName);
		if (all != 0) {
		    results.format("  %s: %1.1f/%1.1f%n",
				   op,
				   opsSuccess.get(opName) / (double) windowSize,
				   all / (double) windowSize);
		    opsSuccess.clear(opName);
		    opsAll.clear(opName);
		}
	    }
	    if (!aggregateCounterMap.isEmpty()) {
		results.format("Aggregate counters (max):%n");
		reportValues(aggregateCounterMap, results);
	    }
	    if (!taskCounterMap.isEmpty()) {
		results.format("Task counters (average/max):%n");
		reportRanges(taskCounterMap, results);
	    }
	    if (!aggregateSampleMap.isEmpty()) {
		results.format("Aggregate samples (max):%n");
		reportValues(aggregateSampleMap, results);
	    }
	    if (!taskSampleMap.isEmpty()) {
		results.format("Task samples (average/max):%n");
		reportRanges(taskSampleMap, results);
	    }
	    showOutput(results.toString());
	}
    }

    /** {@inheritDoc} */
    public void shutdown() { }

    /* -- Other methods and classes -- */

    private void showOutput(String output) {
	logger.log(Level.INFO, output);
    }

    private void maxCounters(Map<String, Long> updates,
			     Map<String, Long> map)
    {
	if (updates != null) {
	    for (Entry<String, Long> entry : updates.entrySet()) {
		String name = entry.getKey();
		Long value = entry.getValue();
		Long oldValue = map.get(name);
		if (oldValue == null || value > oldValue) {
		    map.put(name, value);
		}
	    }
	}
    }
    
    private void sumCounters(Map<String, Long> updates,
			     Map<String, Range> map)
    {
	if (updates != null) {
	    for (Entry<String, Long> entry : updates.entrySet()) {
		String name = entry.getKey();
		Long value = entry.getValue();
		Range range = map.get(name);
		if (range == null) {
		    range = new Range(value);
		    map.put(name, range);
		} else {
		    range.update(value);
		}
	    }
	}
    }

    private void maxSamples(Map<String, List<Long>> updates,
			    Map<String, Long> map)
    {
	if (updates != null) {
	    for (Entry<String, List<Long>> entry : updates.entrySet()) {
		String name = entry.getKey();
		Long oldValue = map.get(name);
		Long newValue = null;
		for (Long value : entry.getValue()) {
		    if (newValue == null || value > newValue) {
			newValue = value;
		    }
		}
		if (oldValue == null || newValue > oldValue) {
		    map.put(name, newValue);
		}
	    }
	}
    }

    private void sumSamples(Map<String, List<Long>> updates,
			    Map<String, Range> counterMap)
    {
	if (updates != null) {
	    for (Entry<String, List<Long>> entry : updates.entrySet()) {
		String name = entry.getKey();
		Range range = counterMap.get(name);
		for (Long value : entry.getValue()) {
		    if (range == null) {
			range = new Range(value);
			counterMap.put(name, range);
		    } else {
			range.update(value);
		    }
		}
	    }
	}
    }

    private void reportRanges(Map<String, Range> map, Formatter results) {
	for (Entry<String, Range> entry : map.entrySet()) {
	    String name = entry.getKey();
	    Range range = entry.getValue();
	    if (range.max != 0) {
		results.format("  %s: %1.1f/%d%n",
			       name, range.sum / (double) windowSize,
			       range.max);
	    }
	}
	map.clear();
    }

    private void reportValues(Map<String, Long> map, Formatter results) {
	for (Entry<String, Long> entry : map.entrySet()) {
	    Long value = entry.getValue();
	    if (value != 0) {
		results.format("  %s: %d%n", entry.getKey(), value);
	    }
	}
    }

    private static class IntegerMap {
	private final Map<String, Integer> map =
	    new HashMap<String, Integer>();
	Integer get(String key) {
	    Integer value = map.get(key);
	    return (value == null) ? 0 : value;
	}
	void increment(String key) {
	    Integer value = map.get(key);
	    map.put(key, (value == null) ? 1 : value + 1);
	}
	void clear(String key) {
	    map.put(key, 0);
	}
    }   

    private static class Range {
	long sum;
	long max;
	Range(long value) {
	    sum = value;
	    max = value;
	}
	void update(long value) {
	    sum += value;
	    max = Math.max(max, value);
	}
    }
}
