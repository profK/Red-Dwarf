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

package com.sun.sgs.test.app.util;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.util.ScalableHashMap;
import com.sun.sgs.app.util.ManagedSerializable;
import com.sun.sgs.auth.Identity;
import static com.sun.sgs.impl.sharedutil.Objects.uncheckedCast;
import com.sun.sgs.kernel.TransactionScheduler;
import com.sun.sgs.service.DataService;
import com.sun.sgs.test.util.NameRunner;
import com.sun.sgs.test.util.SgsTestNode;
import com.sun.sgs.test.util.TestAbstractKernelRunnable;
import static com.sun.sgs.test.util.UtilReflection.getConstructor;
import static com.sun.sgs.test.util.UtilReflection.getMethod;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the {@link ScalableHashMap} class.
 */
@RunWith(NameRunner.class)
public class TestScalableHashMapIntegration extends Assert {

    private static SgsTestNode serverNode;
    private static TransactionScheduler txnScheduler;
    private static Identity taskOwner;
    private static DataService dataService;

    /** A fixed random number generator for use in the test. */
    private static final Random RANDOM = new Random(1337);

    /** The findMinDepthFor method. */
    private static final Method findMinDepthFor =
	getMethod(ScalableHashMap.class, "findMinDepthFor", int.class);

    /** The getMinTreeDepth method. */
    private static final Method getMinTreeDepth =
	getMethod(ScalableHashMap.class, "getMinTreeDepth");

    /** The getMaxTreeDepth method. */
    private static final Method getMaxTreeDepth =
	getMethod(ScalableHashMap.class, "getMaxTreeDepth");

    /** The getAvgTreeDepth method. */
    private static final Method getAvgTreeDepth =
	getMethod(ScalableHashMap.class, "getAvgTreeDepth");

    /**
     * Test management.
     */

    @BeforeClass public static void setUpClass() throws Exception {
	serverNode = new SgsTestNode("TestScalableHashMap", null,
				     createProps("TestScalableHashMap"));
        txnScheduler = serverNode.getSystemRegistry().
            getComponent(TransactionScheduler.class);
        taskOwner = serverNode.getProxy().getCurrentOwner();
        dataService = serverNode.getDataService();
    }

    @AfterClass public static void tearDownClass() throws Exception {
	serverNode.shutdown(true);
    }

    /*
     * Test no arg constructor
     */

    @Test public void testConstructorNoArg() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			new ScalableHashMap<Integer,Integer>();
		    assertEquals(6, getMaxTreeDepth(test));
		    assertEquals(6, getMaxTreeDepth(test));
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(6, getMaxTreeDepth(test));
		    assertEquals(6, getMinTreeDepth(test));
		}
	    }, taskOwner);
    }

    /*
     * Test copy constructor
     */

    @Test public void testCopyConstructor() throws Exception {
	final Map<Integer,Integer> control = new HashMap<Integer,Integer>();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    for (int i = 0; i < 32; i++) {
			control.put(i,i);
		    }
		    ScalableHashMap<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>(control);
		    assertEquals(control, test);
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(control, test);
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testPutOldValueNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(Boolean.TRUE, bar);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    try {
			test.put(Boolean.TRUE, Boolean.FALSE);
			fail("Expected ObjectNotFoundException");
		    } catch (ObjectNotFoundException e) {
			assertEquals(1, test.size());
		    }
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    try {
			test.put(Boolean.TRUE, Boolean.FALSE);
			fail("Expected ObjectNotFoundException");
		    } catch (ObjectNotFoundException e) {
			assertEquals(1, test.size());
		    }
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testPutOldKeyNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(bar, Boolean.TRUE);
		}
	    }, taskOwner);
	try {
	    txnScheduler.runTask(
	        new TestAbstractKernelRunnable() {
		    public void run() throws Exception {
			dataService.removeObject(
			    dataService.getBinding("bar"));
			ScalableHashMap test =
			    (ScalableHashMap) dataService.getBinding("test");
			assertEquals(null, test.put(new Bar(1), Boolean.FALSE));
			assertEquals(Boolean.FALSE, test.get(new Bar(1)));
			throw new RuntimeException("Intentional Abort");
		    }
		}, taskOwner);
	} catch (RuntimeException re) {}
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(null, test.put(new Bar(1), Boolean.FALSE));
		    assertEquals(Boolean.FALSE, test.get(new Bar(1)));
		}
	    }, taskOwner);
    }

    @Test public void testPutNullKey() throws Exception {
	final Map<String,Integer> control = new HashMap<String,Integer>();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<String,Integer> test =
			new ScalableHashMap<String,Integer>(16);
		    test.put(null, 0);
		    control.put(null, 0);
		    assertEquals(control, test);
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    assertEquals(control, dataService.getBinding("test"));
		}
	    }, taskOwner);	    
    }

    @Test public void testPutNullValue() throws Exception {
	final Map<Integer,String> control = new HashMap<Integer,String>();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,String> test =
			new ScalableHashMap<Integer,String>(16);
		    test.put(0, null);
		    control.put(0, null);
		    assertEquals(control, test);
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    assertEquals(control, dataService.getBinding("test"));
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testGetValueNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(Boolean.TRUE, bar);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    try {
			test.get(Boolean.TRUE);
			fail("Expected ObjectNotFoundException");
		    } catch (ObjectNotFoundException e) {
			assertEquals(1, test.size());
		    }
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    try {
			test.get(Boolean.TRUE);
			fail("Expected ObjectNotFoundException");
		    } catch (ObjectNotFoundException e) {
			assertEquals(1, test.size());
		    }
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testGetKeyNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(bar, Boolean.TRUE);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(null, test.get(new Bar(1)));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(null, test.get(new Bar(1)));
		}
	    }, taskOwner);
    }

    @Test public void testGetNullKey() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<String,Integer> test =
			new ScalableHashMap<String,Integer>(16);
		    test.put(null, 0);
		    assertEquals(new Integer(0), test.get(null));
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    assertEquals(
			new Integer(0),
			((Map) dataService.getBinding("test")).get(null));
		}
	    }, taskOwner);
    }

    @Test public void testGetNullValue() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,String> test =
			new ScalableHashMap<Integer,String>(16);
		    test.put(0, null);
		    assertEquals(null, test.get(0));
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    assertEquals(null,
				 ((Map) dataService.getBinding(
				     "test")).get(0));
		}
	    }, taskOwner);
    }

    

    @Test public void testContainsKeyNull() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<String,Integer> test =
			new ScalableHashMap<String,Integer>(16);
		    test.put(null, 0);
		    assertTrue(test.containsKey(null));
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    assertTrue(((Map) dataService.getBinding(
				    "test")).containsKey(null));
		}
	    }, taskOwner);
    }

    @Test public void testContainsKeyNullOnEmptyMap() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<String,Integer> test =
			new ScalableHashMap<String,Integer>(16);
		    assertFalse(test.containsKey(null));
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    assertFalse(((Map) dataService.getBinding(
				     "test")).containsKey(null));
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testContainsKeyKeyNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap(16);
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(bar, 1);
		    test.put(new Bar(2), 2);
		    }
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertFalse(test.containsKey(new Bar(1)));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertFalse(test.containsKey(new Bar(1)));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testContainsKeyValueNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap(16);
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(1, bar);
		    test.put(2, new Bar(2));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertTrue(test.containsKey(1));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertTrue(test.containsKey(1));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
    }


    /*
     * Test containsValue
     */

    @SuppressWarnings("unchecked")
    @Test public void testContainsValueNull() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>();
		    test.put(0, null);
		    dataService.setBinding("test", test);
		    assertTrue(test.containsValue(null));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			uncheckedCast(dataService.getBinding("test"));
		    assertTrue(test.containsValue(null));
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testContainsValueNullEmptyMap() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>();
		    dataService.setBinding("test", test);
		    assertFalse(test.containsValue(null));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			uncheckedCast(dataService.getBinding("test"));
		    assertFalse(test.containsValue(null));
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testContainsValueValueNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(1, bar);
		    test.put(2, new Bar(2));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertFalse(test.containsValue(new Bar(1)));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertFalse(test.containsValue(new Bar(1)));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testContainsValueKeyNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(bar, 1);
		    test.put(new Bar(2), 2);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertTrue(test.containsValue(1));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertTrue(test.containsValue(1));
		    assertEquals(2, test.size());
		}
	    }, taskOwner);
    }

    /*
     * Test values
     */

    @SuppressWarnings("unchecked")
    @Test public void testValues() throws Exception {
	final Map<Integer,Integer> control = new HashMap<Integer,Integer>();
	final Collection<Integer> controlValues = control.values();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    Map<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>();
		    Collection<Integer> values = test.values();
		    
		    assertTrue(values.isEmpty());
		    assertIteratorDone(values.iterator());

		    for (int i = 0; i < 50; i++) {
			int j = RANDOM.nextInt();
			test.put(j,-j);
			control.put(j,-j);
		    }

		    assertEquals(50, values.size());
		    assertTrue(controlValues.containsAll(values));
		    assertIteratorContains(controlValues, values.iterator());

		    dataService.setBinding("values",
					   new ManagedSerializable(values));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ManagedSerializable<Collection<Integer>> ms =
			uncheckedCast(dataService.getBinding("values"));
		    Collection<Integer> values = ms.get();
		    assertEquals(50, values.size());
		    assertTrue(controlValues.containsAll(values));
		    assertIteratorContains(controlValues, values.iterator());
		}
	    }, taskOwner);
    }

    /*
     * Test keySet
     */

    @SuppressWarnings("unchecked")
    @Test public void testKeySet() throws Exception {
	final Map control = new HashMap();
	final Set controlKeys = control.keySet();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    Map test = new ScalableHashMap();
		    Set keys = test.keySet();
		    assertEquals(controlKeys, keys);
		    assertIteratorDone(keys.iterator());
		    assertEquals(controlKeys.hashCode(), keys.hashCode());
		    for (int i = 0; i < 50; i++) {
			int j = RANDOM.nextInt();
			test.put(j,-j);
			control.put(j,-j);
		    }
		    assertEquals(controlKeys, keys);
		    assertIteratorContains(controlKeys, keys.iterator());
		    assertEquals(controlKeys.hashCode(), keys.hashCode());
		    dataService.setBinding("keys",
					   new ManagedSerializable(keys));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ManagedSerializable<Set> ms =
			uncheckedCast(dataService.getBinding("keys"));
		    Set keys = ms.get();
		    assertEquals(controlKeys, keys);
		    assertIteratorContains(controlKeys, keys.iterator());
		    assertEquals(controlKeys.hashCode(), keys.hashCode());
		}
	    }, taskOwner);
    }

    /*
     * Test entrySet
     */

    @SuppressWarnings("unchecked")
    @Test public void testEntrySet() throws Exception {
	final Map control = new HashMap();
	final Set controlEntries = control.entrySet();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    Map test = new ScalableHashMap();
		    Set entries = test.entrySet();
		    assertEquals(controlEntries, entries);
		    assertIteratorDone(entries.iterator());
		    assertEquals(controlEntries.hashCode(), entries.hashCode());
		    for (int i = 0; i < 50; i++) {
			int j = RANDOM.nextInt();
			test.put(j,-j);
			control.put(j,-j);
		    }
		    assertEquals(controlEntries, entries);
		    assertIteratorContains(controlEntries, entries.iterator());
		    assertEquals(controlEntries.hashCode(), entries.hashCode());
		    dataService.setBinding("entries",
					   new ManagedSerializable(entries));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ManagedSerializable<Set> ms =
			uncheckedCast(dataService.getBinding("entries"));
		    Set entries = ms.get();
		    assertEquals(controlEntries, entries);
		    assertEquals(controlEntries.hashCode(), entries.hashCode());
		}
	    }, taskOwner);
    }

    /*
     * Test equals and hashCode
     */

    @SuppressWarnings("unchecked")
    @Test public void testEquals() throws Exception {
	final Map control = new HashMap();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    assertFalse(test.equals(null));
		    assertFalse(test.equals(1));
		    assertTrue(test.equals(control));
		    assertEquals(test.hashCode(), control.hashCode());
		    for (int i = 0; i < 50; i++) {
			int j = RANDOM.nextInt();
			test.put(j,-j);
			control.put(j,-j);
		    }
		    assertTrue(test.equals(control));
		    assertEquals(test.hashCode(), control.hashCode());
		    dataService.setBinding("test", test);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertTrue(test.equals(control));
		    assertEquals(test.hashCode(), control.hashCode());
		}
	    }, taskOwner);
    }

    /*
     * Test remove
     */

    @SuppressWarnings("unchecked")
    @Test public void testRemoveValueNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(1, bar);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    try {
			test.remove(1);
			fail("Expected ObjectNotFoundException");
		    } catch (ObjectNotFoundException e) {
		    }
		    assertEquals(null, test.remove(2));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    try {
			test.remove(1);
			fail("Expected ObjectNotFoundException");
		    } catch (ObjectNotFoundException e) {
		    }
		    assertEquals(null, test.remove(2));
		}
	    }, taskOwner);
    }

    @SuppressWarnings("unchecked")
    @Test public void testRemoveKeyNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(bar, 1);
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    dataService.removeObject(dataService.getBinding("bar"));
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(null, test.remove(new Bar(1)));
		    assertEquals(null, test.remove(1));
		    assertEquals(null, test.remove(new Bar(2)));
		}
	    }, taskOwner);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test =
			(ScalableHashMap) dataService.getBinding("test");
		    assertEquals(null, test.remove(new Bar(1)));
		    assertEquals(null, test.remove(1));
		    assertEquals(null, test.remove(new Bar(2)));
		}
	    }, taskOwner);
    }

    /*
     * Test clear
     */

    @Test public void testClear() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    Map<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>(16);
		    Map<Integer,Integer> control =
			new HashMap<Integer,Integer>();

		    int[] inputs = new int[50];

		    for (int i = 0; i < inputs.length; i++) {
			int j = RANDOM.nextInt();
			inputs[i] = j;
			test.put(j,j);
			control.put(j,j);
		    }
		    assertEquals(control, test);

		    DoneRemoving.init();
		    test.clear();
		    control.clear();

		    /*
		     * XXX: Test that clear does not change the minimum depth.
		     * -tjb@sun.com (10/04/2007)
		     */

		    assertEquals(control, test);
		}
	    }, taskOwner);
	DoneRemoving.await(1);
    }

    @SuppressWarnings("unchecked")
    @Test public void testMultipleClearOperations() throws Exception {
	final Map<Integer,Integer> control = new HashMap<Integer,Integer>();
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>();
		    DoneRemoving.init();
		    test.clear();
		    assertEquals(control, test);

		    dataService.setBinding("test", test);
		    }
	    }, taskOwner);
	DoneRemoving.await(1);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			uncheckedCast(dataService.getBinding("test"));
		    // add just a few elements
		    for (int i = 0; i < 33; i++) {
			int j = RANDOM.nextInt();
			test.put(j,j);
		    }

		    test.clear();
		    assertEquals(control, test);
		}
	    }, taskOwner);
	DoneRemoving.await(1);
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			uncheckedCast(dataService.getBinding("test"));

		    // add just enough elements to force a split
		    for (int i = 0; i < 1024; i++) {
			int j = RANDOM.nextInt();
			test.put(j,j);
		    }

		    test.clear();
		    assertEquals(control, test);
		}
	    }, taskOwner);
	DoneRemoving.await(1);
    }

    /*
     * Miscellaneous tests
     */

    @SuppressWarnings("unchecked")
    @Test public void testIteratorNotFound() throws Exception {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap test = new ScalableHashMap();
		    dataService.setBinding("test", test);
		    Bar bar = new Bar(1);
		    dataService.setBinding("bar", bar);
		    test.put(1, bar);
		    test.put(2, new Bar(2));
		}
	    }, taskOwner);
	for (int i = 0; i < 2; i++) {
	    final int local = i;
	    txnScheduler.runTask(
	        new TestAbstractKernelRunnable() {
		    public void run() throws Exception {
			ScalableHashMap test =
			    (ScalableHashMap) dataService.getBinding("test");
			dataService.setBinding("valuesIter",
			    new ManagedSerializable(test.values().iterator()));
		    }
		}, taskOwner);
	    txnScheduler.runTask(
	        new TestAbstractKernelRunnable() {
		    public void run() throws Exception {
			if (local == 0) {
			    dataService.removeObject(
				dataService.getBinding("bar"));
			}
			ManagedSerializable<Iterator> ms = uncheckedCast(
			    dataService.getBinding("valuesIter"));
			dataService.markForUpdate(ms);
			Iterator valuesIter = ms.get();
			int count = 0;
			while (valuesIter.hasNext()) {
			    count++;
			    try {
				assertEquals(new Bar(2), valuesIter.next());
			    } catch (ObjectNotFoundException e) {
			    }
			}
			assertEquals(2, count);
		    }
		}, taskOwner);
	}
    }


    @SuppressWarnings("unchecked")
    @Test public void testConcurrentIteratorWithReplacements()
	throws Exception
    {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Integer,Integer> test =
			new ScalableHashMap<Integer,Integer>(16);
		    Map<Integer,Integer> control =
			new HashMap<Integer,Integer>();

		    int[] a = new int[128];

		    for (int i = 0; i < a.length; i++) {
			int j = RANDOM.nextInt();
			test.put(j, j);
			control.put(j, j);
			a[i] = j;
		    }

		    Set<Map.Entry<Integer,Integer>> entrySet =
			control.entrySet();
		    int entries = 0;

		    Iterator<Map.Entry<Integer,Integer>> it =
			test.entrySet().iterator();
		    for (int i = 0; i < a.length / 2; i++) {
			Map.Entry<Integer,Integer> e = it.next();
			assertTrue(entrySet.contains(e));
			entries++;
		    }

		    assertEquals(a.length / 2, entries);

		    // serialize the iterator
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(baos);
		    oos.writeObject(it);

		    // now replace all the elements in the map
		    DoneRemoving.init();
		    test.clear();
		    control.clear();
		    for (int i = 0; i < a.length; i++) {
			int j = RANDOM.nextInt();
			test.put(j, j);
			control.put(j, j);
			a[i] = j;
		    }

		    // reserialize the iterator
		    byte[] serializedForm = baos.toByteArray();

		    ByteArrayInputStream bais =
			new ByteArrayInputStream(serializedForm);
		    ObjectInputStream ois = new ObjectInputStream(bais);

		    it = (Iterator<Map.Entry<Integer,Integer>>)
			ois.readObject();

		    while(it.hasNext()) {
			Map.Entry<Integer,Integer> e = it.next();
			assertTrue(entrySet.contains(e));
			entries++;
		    }

		    // due to the random nature of the entries, we can't check
		    // that it read in another half other elements.  However
		    // this should still check that no execptions were thrown.
		}
	    }, taskOwner);
	DoneRemoving.await(1);
    }


     @SuppressWarnings("unchecked")
     @Test public void testConcurrentIteratorWithReplacementsOnEqualHashCodes()
	 throws Exception
    {
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    ScalableHashMap<Equals,Integer> test =
			new ScalableHashMap<Equals,Integer>(16);
		    Map<Equals,Integer> control = new HashMap<Equals,Integer>();

		    int[] a = new int[128];

		    for (int i = 0; i < a.length; i++) {
			int j = RANDOM.nextInt();
			test.put(new Equals(j), j);
			control.put(new Equals(j), j);
			a[i] = j;
		    }

		    Set<Map.Entry<Equals,Integer>> entrySet =
			control.entrySet();
		    int entries = 0;

		    Iterator<Map.Entry<Equals,Integer>> it =
			test.entrySet().iterator();
		    for (int i = 0; i < a.length / 2; i++) {
			Map.Entry<Equals,Integer> e = it.next();
			assertTrue(entrySet.contains(e));
			entries++;
		    }

		    assertEquals(a.length / 2, entries);

		    // serialize the iterator
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(baos);
		    oos.writeObject(it);

		    // now replace all the elements in the map
		    DoneRemoving.init();
		    test.clear();
		    control.clear();

		    for (int i = 0; i < a.length; i++) {
			int j = RANDOM.nextInt();
			test.put(new Equals(j), j);
			control.put(new Equals(j), j);
			a[i] = j;
		    }

		    assertEquals(control.size(), test.size());

		    // reserialize the iterator
		    byte[] serializedForm = baos.toByteArray();

		    ByteArrayInputStream bais =
			new ByteArrayInputStream(serializedForm);
		    ObjectInputStream ois = new ObjectInputStream(bais);

		    it = (Iterator<Map.Entry<Equals,Integer>>) ois.readObject();

		    while (it.hasNext()) {
			Map.Entry<Equals,Integer> e = it.next();
			assertTrue(entrySet.contains(e));
			entries++;
		    }

		    // due to the random nature of the entries, we can't check
		    // that it read in another half other elements.  However
		    // this should still check that no exceptions were thrown.
		}
	    }, taskOwner);
	DoneRemoving.await(1);
    }

    /*
     * Utility routines.
     */

    public boolean checkEquals(Map<Integer,Integer> m1,
			       Map<Integer,Integer> m2) {

	if (m1.size() != m2.size()) {
	    System.out.printf("sizes not equal: %d != %d\n",
			      m1.size(), m2.size());
	    return false;
	}

	Iterator<Entry<Integer,Integer>> i = m1.entrySet().iterator();
	while (i.hasNext()) {
	    Entry<Integer,Integer> e = i.next();
	    Integer key = e.getKey();
	    Integer value = e.getValue();
	    if (value == null) {
		if (!(m2.get(key)==null && m2.containsKey(key))) {
		    System.out.printf("keys not equal, m2 has key: %s? %s\n",
				      key, m2.containsKey(key));
		    return false;
		}
	    } else {
		if (!value.equals(m2.get(key))) {
		    System.out.printf("m1.get(%s) not equal: %s: %s\n",
				      key, value, m2.get(key));
		    System.out.println("m2.containsKey() ? " +
				       m2.containsKey(key));
		    return false;
		}
	    }
	}

	return true;
    }


    /**
     * Returns the minimum depth of the tree necessary to support the requested
     * minimum number of concurrent write operations.
     */
    private static int findMinDepthFor(int minConcurrency) {
	try {
	    return (Integer) findMinDepthFor.invoke(null, minConcurrency);
	} catch (InvocationTargetException e) {
	    throw (RuntimeException) e.getCause();
	} catch (Exception e) {
	    throw new RuntimeException("Unexpected exception: " + e, e);
	}
    }

    /**
     * Returns the minimum depth for any leaf node in the map's backing tree.
     * The root node has a depth of 1.
     */
    private int getMinTreeDepth(ScalableHashMap map) {
	try {
	    return (Integer) getMinTreeDepth.invoke(map);
	} catch (Exception e) {
	    throw new RuntimeException("Unexpected exception: " + e, e);
	}
    }

    /**
     * Returns the maximum depth for any leaf node in the map's backing tree.
     * The root node has a depth of 1.
     */
    private int getMaxTreeDepth(ScalableHashMap map) {
	try {
	    return (Integer) getMaxTreeDepth.invoke(map);
	} catch (Exception e) {
	    throw new RuntimeException("Unexpected exception: " + e, e);
	}
    }

    /**
     * Returns the average of all depth for the leaf nodes in the map's backing
     * tree.  The root node has a depth of 1.
     */
    private double getAvgTreeDepth(ScalableHashMap map) {
	try {
	    return (Integer) getAvgTreeDepth.invoke(map);
	} catch (Exception e) {
	    throw new RuntimeException("Unexpected exception: " + e, e);
	}
    }

    /** Checks that the iterator has no more entries. */
    private static void assertIteratorDone(Iterator<?> iterator) {
	assertFalse(iterator.hasNext());
	try {
	    iterator.next();
	    fail("Expected NoSuchElementException");
	} catch (NoSuchElementException e) {
	}
    }

    /**
     * Checks that the iterator returns objects equal to the contents of the
     * collection.
     */
    private static void assertIteratorContains(
	Collection<?> contents, Iterator<?> iterator)
    {
	Set<?> set = new HashSet<Object>(contents);
	while (iterator.hasNext()) {
	    assertTrue(set.remove(iterator.next()));
	}
	assertTrue(set.isEmpty());
    }

    private static Properties createProps(String appName) throws Exception {
        Properties props = SgsTestNode.getDefaultProperties(appName, null, 
                                           SgsTestNode.DummyAppListener.class);
        props.setProperty("com.sun.sgs.txn.timeout", "1000000");
        return props;
    }

    /*
     * Test classes
     */

    /**
     * A serializable object that is equal to objects of the same type with the
     * same value.
     */
    static class Foo implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int i;

	Foo(int i) {
	    this.i = i;
	}

	public int hashCode() {
	    return i;
	}

	public boolean equals(Object o) {
	    return o != null &&
		getClass() == o.getClass() &&
		((Foo) o).i == i;
	}
    }

    /**
     * A managed object that is equal to objects of the same type with the
     * same value.
     */
    static class Bar extends Foo implements ManagedObject {
	private static final long serialVersionUID = 1L;

	Bar(int i) {
	    super(i);
	}
    }

    /**
     * A serializable object that is equal to objects of the same type with the
     * type, but whose hashCode method always returns zero.
     */
    static class Equals extends Foo {
	private static final long serialVersionUID = 1L;

	Equals(int i) {
	    super(i);
	}

	public int hashCode() {
	    return 0;
	}
    }
}
