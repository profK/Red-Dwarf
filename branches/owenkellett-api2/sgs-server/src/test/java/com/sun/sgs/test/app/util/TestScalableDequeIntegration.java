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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.util.ScalableDeque;

import com.sun.sgs.auth.Identity;

import com.sun.sgs.kernel.TransactionScheduler;
import com.sun.sgs.service.DataService;
import com.sun.sgs.test.util.NameRunner;
import com.sun.sgs.test.util.SgsTestNode;
import com.sun.sgs.test.util.TestAbstractKernelRunnable;

import java.io.Serializable;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.sun.sgs.impl.sharedutil.Objects.uncheckedCast;

/**
 * Test the {@link ScalableDeque} class.
 */
@RunWith(NameRunner.class)
public class TestScalableDequeIntegration extends Assert {

    private static SgsTestNode serverNode;
    private static TransactionScheduler txnScheduler;
    private static Identity taskOwner;
    private static DataService dataService;

    /** A fixed random number generator for use in the test. */
    private static final Random RANDOM = new Random(1337);

    /**
     * Test management.
     */

    @BeforeClass public static void setUpClass() throws Exception {
	serverNode = new SgsTestNode("TestScalableDeque", null,
				     createProps("TestScalableDeque"));
        txnScheduler = serverNode.getSystemRegistry().
            getComponent(TransactionScheduler.class);
        taskOwner = serverNode.getProxy().getCurrentOwner();
        dataService = serverNode.getDataService();
    }

    @AfterClass public static void tearDownClass() throws Exception {
	serverNode.shutdown(true);
    }

    /*
     * Test serializability
     */

    @Test public void testDequeSeriazable() throws Exception {
	final String name = "test-deque";

	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = new ScalableDeque<Integer>();
		    for (int i = 0; i < 10; ++i) 
			d.add(i);
		    AppContext.getDataManager().setBinding(name, d);
		}
	    }, taskOwner);

	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    
		    ScalableDeque<Integer> d = 
			uncheckedCast(AppContext.
					getDataManager().getBinding(name));
		    assertEquals(10, d.size());
		    int i = 0;
		    for (Integer inDeque : d) {
			assertEquals(i++, (int) inDeque);
		    }
		    
		    AppContext.getDataManager().removeBinding(name);
		}
	    }, taskOwner);

    }

    @Test public void testIteratorSeriazable() throws Exception {
	final String name = "test-iterator";

	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = new ScalableDeque<Integer>();
		    for (int i = 0; i < 10; ++i) 
			d.add(i);
		    AppContext.getDataManager().setBinding(name, d.iterator());
		}
	    }, taskOwner);

	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    
		    Iterator<Integer> it = 
			uncheckedCast(AppContext.
				      getDataManager().getBinding(name));

		    int i = 0;
		    while (it.hasNext())
			assertEquals(i++, (int) it.next());
		    assertEquals(10, i);

		    AppContext.getDataManager().removeBinding(name);
		}
	    }, taskOwner);
    }

    @Test public void testIteratorSeriazableWithRemovals() throws Exception {
	final String name = "test-iterator";
	final String name2 = "test-deque";

	// create the deque
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = new ScalableDeque<Integer>();
		    for (int i = 0; i < 10; ++i) 
			d.add(i);
		    AppContext.getDataManager().setBinding(name, d.iterator());
		    AppContext.getDataManager().setBinding(name2, d);
		}
	    }, taskOwner);

	// remove some elements while the iterator is serialized
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = 
			uncheckedCast(AppContext.
                                      getDataManager().getBinding(name2));
		    for (int i = 1; i < 10; i+=2) 
			d.remove(i);		    
		}
	    }, taskOwner);


	// load the iterator back
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    
		    Iterator<Integer> it = 
			uncheckedCast(AppContext.
				      getDataManager().getBinding(name));

		    int i = 0;
		    while (it.hasNext()) {
			assertEquals(i, (int) it.next());
			i += 2;
		    }
		    assertEquals(10, i);

		    AppContext.getDataManager().removeBinding(name);
		    AppContext.getDataManager().removeBinding(name2);
		}
	    }, taskOwner);
    }

    @Test public void testConcurrentIteratorSeriazableWithRemovalOfNextElements() 
	throws Exception {

	final String name = "test-iterator";
	final String name2 = "test-deque";

	// create the deque
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = new ScalableDeque<Integer>(true);
		    for (int i = 0; i < 10; ++i) 
			d.add(i);
		    AppContext.getDataManager().setBinding(name, d.iterator());
		    AppContext.getDataManager().setBinding(name2, d);
		}
	    }, taskOwner);

	// remove the iterator's first 5 elements while the
	// iterator is serialized
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = 
			uncheckedCast(AppContext.
                                      getDataManager().getBinding(name2));
		    for (int i = 0; i < 5; i++) 
			d.remove(i);		    
		}
	    }, taskOwner);


	// load the iterator back
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    
		    Iterator<Integer> it = 
			uncheckedCast(AppContext.
				      getDataManager().getBinding(name));

		    int i = 5;
		    while (it.hasNext()) {
			assertEquals(i, (int) it.next());
			i ++;
		    }
		    assertEquals(10, i);

		    AppContext.getDataManager().removeBinding(name);
		    AppContext.getDataManager().removeBinding(name2);
		}
	    }, taskOwner);
    }

    @Test public void testNonConcurrentIteratorSeriazableWithRemovalOfNextElements() 
	throws Exception {

	final String name = "test-iterator";
	final String name2 = "test-deque";

	// create the deque
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = new ScalableDeque<Integer>();
		    for (int i = 0; i < 10; ++i) 
			d.add(i);
		    AppContext.getDataManager().setBinding(name, d.iterator());
		    AppContext.getDataManager().setBinding(name2, d);
		}
	    }, taskOwner);

	// remove the iterator's first 5 elements while the
	// iterator is serialized
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = 
			uncheckedCast(AppContext.
                                      getDataManager().getBinding(name2));
		    for (int i = 0; i < 5; i++) 
			d.remove();		    
		}
	    }, taskOwner);


	// load the iterator back
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    
		    Iterator<Integer> it = 
			uncheckedCast(AppContext.
				      getDataManager().getBinding(name));

		    try {
			it.next();
			fail("expected ConcurrentModificationException"); 
		    } catch (ConcurrentModificationException cme) {
			// it should throw this
		    } finally {
			AppContext.getDataManager().removeBinding(name);
			AppContext.getDataManager().removeBinding(name2);
		    }
		}
	    }, taskOwner);
    }



    @Test public void testIteratorRemovalWhereCurrentElementWasAlreadyRemoved() 
	throws Exception {

	final String name = "test-iterator";
	final String name2 = "test-deque";

	// create the deque
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = new ScalableDeque<Integer>();
		    for (int i = 0; i < 10; ++i) 
			d.add(i);
		    Iterator<Integer> it = d.iterator();
		    // advance the iterator forward one set
		    it.next();		    
		    AppContext.getDataManager().setBinding(name, it);
		    AppContext.getDataManager().setBinding(name2, d);
		}
	    }, taskOwner);

	// remove the iterator's first 5 elements while the
	// iterator is serialized
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {

		    ScalableDeque<Integer> d = 
			uncheckedCast(AppContext.
                                      getDataManager().getBinding(name2));
		    // remove the first element, which the iterator
		    // already advanced over
		    d.removeFirst();
		}
	    }, taskOwner);


	// load the iterator back
	txnScheduler.runTask(
	    new TestAbstractKernelRunnable() {
		public void run() throws Exception {
		    
		    Iterator<Integer> it = 
			uncheckedCast(AppContext.
				      getDataManager().getBinding(name));

		    // now try to remove the already-removed element
		    // from the iterator
		    it.remove();
		    
		    // the above call shouldn't throw an object not
		    // found exception
		}
	    }, taskOwner);
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
    static class DummySerializable implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int i;

	DummySerializable(int i) {
	    this.i = i;
	}

	public int hashCode() {
	    return i;
	}

	public boolean equals(Object o) {
	    return o != null &&
		getClass() == o.getClass() &&
		((DummySerializable) o).i == i;
	}
    }

    /**
     * A managed object that is equal to objects of the same type with the
     * same value.
     */
    static class DummyMO extends DummySerializable implements ManagedObject {
	private static final long serialVersionUID = 1L;

	DummyMO(int i) {
	    super(i);
	}
    }

    /**
     * A serializable object that is equal to objects of the same type with the
     * type, but whose hashCode method always returns zero.
     */
    static class EqualHashObj extends DummySerializable {
	private static final long serialVersionUID = 1L;

	EqualHashObj(int i) {
	    super(i);
	}

	public int hashCode() {
	    return 0;
	}
    }
}
