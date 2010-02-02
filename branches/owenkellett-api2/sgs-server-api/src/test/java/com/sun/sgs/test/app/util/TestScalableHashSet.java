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
import com.sun.sgs.app.Task;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.TransactionNotActiveException;
import com.sun.sgs.app.util.ScalableHashSet;
import com.sun.sgs.app.util.ManagedSerializable;
import com.sun.sgs.internal.ManagerLocator;
import com.sun.sgs.internal.InternalContext;
import java.io.Serializable;
import java.util.Queue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Test the {@link ScalableHashSet} class. */
public class TestScalableHashSet extends Assert {
    
    private ManagerLocator managerLocator;
    private MockManagerLocator.MockDataManager dataManager;
    private MockManagerLocator.MockTaskManager taskManager;

    /** A fixed random number generator. */
    private static final Random random = new Random(1111961);

    /** A set to test. */
    private ScalableHashSet<Object> set;

    /** An object to use in tests. */
    private Int one;
    
    @Before public void setUp() throws Exception {
        managerLocator = new MockManagerLocator();
        dataManager = (MockManagerLocator.MockDataManager)
                managerLocator.getDataManager();
        taskManager = (MockManagerLocator.MockTaskManager)
                managerLocator.getTaskManager();
        InternalContext.setManagerLocator(managerLocator);
        
        set = new ScalableHashSet<Object>();
        one = new Int(1);
        setBindings();
    }

    @After public void tearDownClass() throws Exception {
        InternalContext.setManagerLocator(null);
    }

    /* -- Tests -- */

    /* Test no-arg constructor */

    @Test public void testConstructorNoArg() throws Exception {
        getBindings();
        assertTrue(set.isEmpty());
    }

    /* Test one-arg constructor */

    @Test public void testConstructorOneArg() throws Exception {
        try {
            new ScalableHashSet(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new ScalableHashSet(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        new ScalableHashSet(1);
        new ScalableHashSet(8);
    }

    /* Test copy constructor */

    @Test public void testCopyConstructor() throws Exception {
	final Set<Integer> anotherSet = new HashSet<Integer>();
        try {
            new ScalableHashSet<Object>(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        anotherSet.add(null);
        anotherSet.add(1);
        anotherSet.add(2);
        set = new ScalableHashSet<Object>(anotherSet);
        assertEquals(anotherSet, set);
        setBindings();

        getBindings();
        assertEquals(anotherSet, set);
        setBindings();
    }

    @Test public void testCopyConstructorObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        try {
            new ScalableHashSet<Object>(set);
            fail("Expected ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
        }
        setBindings();

        getBindings();
        try {
            new ScalableHashSet<Object>(set);
            fail("Expected ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
        }
        setBindings();
    }

    /* Test add */

    @Test public void testAdd() throws Exception {
        getBindings();
        assertTrue(set.add(1));
        assertFalse(set.add(1));
        set.remove(1);
        assertTrue(set.add(1));
        assertTrue(set.add(null));
        assertFalse(set.add(null));
        setBindings();

        getBindings();
        assertTrue(set.contains(1));
        assertTrue(set.contains(null));
        assertEquals(2, set.size());
        setBindings();
    }

    @Test public void testAddObjectNotFound() throws Exception {
        getBindings();
        dataManager.removeObject(one);
        one = new Int(1);
        assertTrue(set.add(one));
        dataManager.removeObject(one);
        setBindings();

        getBindings();
        assertTrue(set.add(new Int(1)));
        setBindings();
    }

    /* Test clear */

    @Test public void testClear() throws Exception {
        getBindings();
        set.add(1);
        set.add(null);
        DoneRemoving.init();
        set.clear();
        //run any scheduled removal tasks
        Queue<Task> tasks = taskManager.getScheduledTasks();
        Task t = null;
        while((t = tasks.poll()) != null) {
            t.run();
        }
        assertTrue(set.isEmpty());
        setBindings();
        DoneRemoving.await(1);

        getBindings();
        set.clear();
        //run any scheduled removal tasks
        Queue<Task> tasks2 = taskManager.getScheduledTasks();
        Task t2 = null;
        while((t2 = tasks2.poll()) != null) {
            t2.run();
        }
        assertTrue(set.isEmpty());
        setBindings();
	DoneRemoving.await(1);
    }

    @Test public void testClearObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        DoneRemoving.init();
        set.clear();
        //run any scheduled removal tasks
        Queue<Task> tasks = taskManager.getScheduledTasks();
        Task t = null;
        while((t = tasks.poll()) != null) {
            t.run();
        }
        assertTrue(set.isEmpty());
        one = new Int(1);
        set.add(one);
        setBindings();
        DoneRemoving.await(1);

        getBindings();
        dataManager.removeObject(one);
        setBindings();

        getBindings();
        set.clear();
        //run any scheduled removal tasks
        Queue<Task> tasks2 = taskManager.getScheduledTasks();
        Task t2 = null;
        while((t2 = tasks2.poll()) != null) {
            t2.run();
        }
        assertTrue(set.isEmpty());
        setBindings();
        DoneRemoving.await(1);
    }

    /* Test contains */

    @Test public void testContains() throws Exception {
        getBindings();
        assertFalse(set.contains(1));
        assertFalse(set.contains(null));
        set.add(1);
        set.add(null);
        assertTrue(set.contains(1));
        assertTrue(set.contains(null));
        assertFalse(set.contains(2));
        setBindings();
    }

    @Test public void testContainsObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        assertFalse(set.contains(one));
        setBindings();

        getBindings();
        assertFalse(set.contains(one));
        setBindings();
    }

    /* Test isEmpty */

    @Test public void testIsEmpty() throws Exception {
        getBindings();
        assertTrue(set.isEmpty());
        set.add(null);
        assertFalse(set.isEmpty());
        set.remove(null);
        assertTrue(set.isEmpty());
        set.add(1);
        assertFalse(set.isEmpty());
        set.remove(1);
        assertTrue(set.isEmpty());
        setBindings();
    }

    @Test public void testIsEmptyObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        assertFalse(set.isEmpty());
        setBindings();

        getBindings();
        assertFalse(set.isEmpty());
        setBindings();
    }

    /* Test iterator */

    @SuppressWarnings("unchecked")
    @Test public void testIterator() throws Exception {
        getBindings();
        set.add(null);
        set.add(1);
        set.add(2);
        Iterator<Object> iter = set.iterator();
        dataManager.setBinding("iter",
                               new ManagedSerializable(iter));
        setBindings();

        getBindings();
        ManagedSerializable<Iterator<Object>> msIter =
                Util.uncheckedCast(dataManager.getBinding("iter"));
        dataManager.markForUpdate(msIter);
        Iterator<Object> iter2 = msIter.get();
        int count = 0;
        while (iter2.hasNext()) {
            iter2.next();
            count++;
        }
        assertEquals(3, count);
        setBindings();
    }

    @SuppressWarnings("unchecked")
    @Test public void testIteratorCollectionNotFound() throws Exception {
        getBindings();
        set.add(one);
        Iterator<Object> iter = set.iterator();
        dataManager.setBinding("iter",
                               new ManagedSerializable(iter));
        setBindings();

        getBindings();
        DoneRemoving.init();
        dataManager.removeObject(set);
        //run any scheduled removal tasks
        Queue<Task> tasks = taskManager.getScheduledTasks();
        Task t = null;
        while((t = tasks.poll()) != null) {
            t.run();
        }
        setBindings();

        getBindings();
        ManagedSerializable<Iterator<Object>> msIter =
                Util.uncheckedCast(dataManager.getBinding("iter"));
        dataManager.markForUpdate(msIter);
        Iterator<Object> iter2 = msIter.get();
        try {
            iter2.next();
            fail("Expected ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
            System.err.println(e);
        }
        try {
            iter2.hasNext();
            fail("Expected ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
            System.err.println(e);
        }
        try {
            iter2.remove();
            fail("Expected an exception");
        } catch (ObjectNotFoundException e) {
            System.err.println(e);
        } catch (IllegalStateException e) {
            System.err.println(e);
        }
        setBindings();
    }

    @SuppressWarnings("unchecked")
    @Test public void testIteratorObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        set.add(new Int(2));
        Iterator<Object> iter = set.iterator();
        dataManager.setBinding("iter",
                               new ManagedSerializable(iter));
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        setBindings();

        getBindings();
        ManagedSerializable<Iterator<Object>> msIter =
                Util.uncheckedCast(dataManager.getBinding("iter"));
        dataManager.markForUpdate(msIter);
        Iterator<Object> iter2 = msIter.get();
        int count = 0;
        while (iter2.hasNext()) {
            try {
                assertEquals(new Int(2), iter2.next());
                count++;
            } catch (ObjectNotFoundException e) {
            }
        }
        assertEquals(1, count);
        setBindings();
    }

    @SuppressWarnings("unchecked")
    @Test public void testIteratorRemove() throws Exception {
        getBindings();
        Iterator<Object> iter = set.iterator();
        try {
            iter.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        set.add(one);
        set.add(new Int(2));
        set.add(new Int(3));
        try {
            iter.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        dataManager.setBinding("iter",
                               new ManagedSerializable(iter));
        setBindings();

        getBindings();
        ManagedSerializable<Iterator<Object>> msIter =
                Util.uncheckedCast(dataManager.getBinding("iter"));
        dataManager.markForUpdate(msIter);
        Iterator<Object> iter2 = msIter.get();
        while (iter2.hasNext()) {
            Object next = iter2.next();
            System.out.println("SEEING: " + next);
            if (one.equals(next)) {
                System.out.println("REMOVING");
                iter2.remove();
                try {
                    iter2.remove();
                    fail("Expected IllegalStateException");
                } catch (IllegalStateException e) {
                }
            }
        }
        setBindings();

        getBindings();
        Iterator<Object> iter3 = set.iterator();
        int count = 0;
        while (iter3.hasNext()) {
            assertFalse(one.equals(iter3.next()));
            count++;
        }
        assertEquals(2, count);
        setBindings();
    }

    @SuppressWarnings("unchecked")
    @Test public void testIteratorRetainAcrossTransactions() throws Exception {
	final AtomicReference<Iterator<Object>> iterRef = new AtomicReference();

        getBindings();
        set.add(one);
        Iterator<Object> iter = set.iterator();
        iterRef.set(iter);
        dataManager.setBinding("iter",
                               new ManagedSerializable(iter));
        setBindings();

        getBindings();
        Iterator<Object> iter2 = iterRef.get();
        try {
            iter2.hasNext();
            fail("Expected TransactionNotActiveException");
        } catch (TransactionNotActiveException e) {
        }
        try {
            iter2.next();
            fail("Expected TransactionNotActiveException");
        } catch (TransactionNotActiveException e) {
        }
        try {
            iter2.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        setBindings();
    }

    /* Test remove */

    @Test public void testRemove() throws Exception {
        getBindings();
        assertFalse(set.remove(1));
        assertFalse(set.remove(null));
        set.add(1);
        set.add(null);
        assertTrue(set.remove(1));
        assertFalse(set.contains(1));
        assertFalse(set.remove(1));
        assertTrue(set.remove(null));
        assertFalse(set.contains(null));
        assertFalse(set.remove(null));
        setBindings();
    }

    @Test public void testRemoveAfterSerialization() throws Exception {
	
	final String SET_NAME = "test.remove.after.serialization";
	
	// store the set in the db
        getBindings();
        assertFalse(set.remove(1));
        assertFalse(set.remove(null));
        set.add(1);
        set.add(null);
        dataManager.setBinding(SET_NAME, set);
        setBindings();

        // next reload the set from the db to test for the correct 
        // handling of the Marker flag.
        getBindings();
        ScalableHashSet deserialized =
                (ScalableHashSet) (dataManager.getBinding(SET_NAME));

        assertTrue(deserialized.remove(1));
        assertFalse(deserialized.contains(1));
        assertFalse(deserialized.remove(1));
        assertTrue(deserialized.remove(null));
        assertFalse(deserialized.contains(null));
        assertFalse(deserialized.remove(null));
        setBindings();
    }


    @Test public void testRemoveObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        one = new Int(1);
        assertFalse(set.remove(one));
        setBindings();

        getBindings();
        assertFalse(set.remove(one));
        setBindings();
    }

    /* Test size */

    @Test public void testSize() throws Exception {
        getBindings();
        assertEquals(0, set.size());
        set.add(1);
        assertEquals(1, set.size());
        set.add(2);
        assertEquals(2, set.size());
        set.add(2);
        assertEquals(2, set.size());
        DoneRemoving.init();
        set.clear();
        //run any scheduled removal tasks
        Queue<Task> tasks = taskManager.getScheduledTasks();
        Task t = null;
        while((t = tasks.poll()) != null) {
            t.run();
        }
        assertEquals(0, set.size());
        setBindings();

	DoneRemoving.await(1);
    }

    @Test public void testSizeObjectNotFound() throws Exception {
        getBindings();
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        assertEquals(1, set.size());
        setBindings();

        getBindings();
        assertEquals(1, set.size());
        setBindings();
    }

    /* Test equals and hashCode */

    @Test public void testEquals() throws Exception {
        getBindings();
        Set<Object> control = new HashSet<Object>();
        assertTrue(set.equals(control));
        assertEquals(control.hashCode(), set.hashCode());
        for (int i = 0; i < 50; i++) {
            int n = random.nextInt();
            set.add(n);
            control.add(n);
        }
        assertTrue(set.equals(control));
        assertEquals(control.hashCode(), set.hashCode());
        setBindings();
    }

    @Test public void testEqualsObjectNotFound() throws Exception {
	final Set<Object> empty = new HashSet<Object>();
	final Set<Object> containsOne = new HashSet<Object>();

        getBindings();
        containsOne.add(one);
        set.add(one);
        setBindings();

        getBindings();
        dataManager.removeObject(one);
        assertFalse(set.equals(empty));
        assertFalse(set.equals(containsOne));
        setBindings();

        getBindings();
        assertFalse(set.equals(empty));
        assertFalse(set.equals(containsOne));
        setBindings();
    }

    /* Test removeAll */
 
    @Test public void testRemoveAll() throws Exception {
        getBindings();
        try {
            set.removeAll(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        set.add(null);
        set.add(1);
        set.add(2);
        set.add(3);
        Set<Object> other = new HashSet<Object>();
        other.add(null);
        other.add(2);
        other.add(6);
        set.removeAll(other);
        assertEquals(2, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(3));
        setBindings();
    }

    /* Test addAll */

    @Test public void testAddAll() throws Exception {
        getBindings();
        try {
            set.addAll(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        set.add(1);
        set.add(2);
        set.add(3);
        Set<Object> other = new HashSet<Object>();
        other.add(null);
        other.add(3);
        other.add(4);
        other.add(5);
        set.addAll(other);
        assertEquals(6, set.size());
        set.contains(4);
        set.contains(5);
        setBindings();
    }

    /* Test containsAll */

    @Test public void testContainsAll() throws Exception {
        getBindings();
        try {
            set.containsAll(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Set<Object> other = new HashSet<Object>();
        assertTrue(set.containsAll(other));
        other.add(1);
        assertFalse(set.containsAll(other));
        set.add(null);
        set.add(1);
        assertTrue(set.containsAll(other));
        DoneRemoving.init();
        set.clear();
        //run any scheduled removal tasks
        Queue<Task> tasks = taskManager.getScheduledTasks();
        Task t = null;
        while((t = tasks.poll()) != null) {
            t.run();
        }
        assertFalse(set.containsAll(other));
        setBindings();
	DoneRemoving.await(1);
    }

    /* Test retainAll */

    @Test public void testRetainAll() throws Exception {
        getBindings();
        try {
            set.retainAll(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Set<Object> other = new HashSet<Object>();
        assertFalse(set.retainAll(other));
        other.add(1);
        assertFalse(set.retainAll(other));
        set.add(1);
        set.add(2);
        assertTrue(set.retainAll(other));
        assertEquals(1, set.size());
        assertTrue(set.contains(1));
        setBindings();
    }

    /* Test toArray */

    @Test public void testToArray() throws Exception {
        getBindings();
        assertEquals(0, set.toArray().length);
        set.add(1);
        Object[] result = set.toArray();
        assertEquals(1, result.length);
        assertEquals(new Integer(1), result[0]);
        Integer[] intResult = new Integer[1];
        set.toArray(intResult);
        assertEquals(new Integer(1), intResult[0]);
        setBindings();
    }

    /* Test toString */

    @Test public void testToString() throws Exception {
        getBindings();
        assertEquals("[]", set.toString());
        set.add(1);
        assertEquals("[1]", set.toString());
        setBindings();
    }

    /* Test calling DataManager.removeObject on the set */

    @Test public void testRemoveObjectSet() throws Exception {
        getBindings();
        DoneRemoving.init();
        dataManager.removeObject(set);
        //run any scheduled removal tasks
        Queue<Task> tasks = taskManager.getScheduledTasks();
        Task t = null;
        while((t = tasks.poll()) != null) {
            t.run();
        }
        set = null;
        setBindings();
        DoneRemoving.await(1);
        int count = getObjectCount();

        getBindings();
        set = new ScalableHashSet<Object>();
        for (int i = 0; i < 50; i++) {
            set.add(random.nextInt());
        }
        setBindings();

        getBindings();
        dataManager.removeObject(set);
        //run any scheduled removal tasks
        Queue<Task> tasks2 = taskManager.getScheduledTasks();
        Task t2 = null;
        while((t2 = tasks2.poll()) != null) {
            t2.run();
        }
        set = null;
        setBindings();
	DoneRemoving.await(1);
        
	assertEquals(count, getObjectCount());
    }

    /* -- Utilities -- */
    
    /**
     * Stores fields, if they are not null, into bindings.
     */
    private void setBindings() throws Exception {
	if (set != null) {
	    try {
		dataManager.setBinding("set", set);
	    } catch (ObjectNotFoundException e) {
	    }
	}
	if (one != null) {
	    try {
		dataManager.setBinding("one", one);
	    } catch (ObjectNotFoundException e) {
	    }
	}
        dataManager.serializeDataStore();
    }

    /**
     * Updates fields from bindings, setting the fields to null if the
     * objects are not found.
     */
    @SuppressWarnings("unchecked")
    private void getBindings() throws Exception {
	try {
	    set = (ScalableHashSet) dataManager.getBinding("set");
	} catch (ObjectNotFoundException e) {
	    set = null;
	}
	try {
	    one = (Int) dataManager.getBinding("one");
	} catch (ObjectNotFoundException e) {
	    one = null;
	}
    }

    /**
     * A managed object that is equal to objects of the same type with the
     * same value.
     */
    static class Int implements ManagedObject, Serializable {
	private static final long serialVersionUID = 1L;
	private final int i;
	Int(int i) {
	    this.i = i;
	}
	public int hashCode() {
	    return i;
	}
	public boolean equals(Object o) {
	    return o instanceof Int && i == ((Int) o).i;
	}
    }

    /** Returns the current number of objects */
    private int getObjectCount() throws Exception {
        return dataManager.size();
    }
}
