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

import com.sun.sgs.internal.InternalContext;
import com.sun.sgs.internal.ManagerLocator;

import java.io.Serializable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link ScalableDeque} class.
 */
public class TestScalableDeque extends Assert {
    
    private ManagerLocator managerLocator;
    private MockManagerLocator.MockDataManager dataManager;
    private MockManagerLocator.MockTaskManager taskManager;

    /** A fixed random number generator for use in the test. */
    private static final Random RANDOM = new Random(1337);

    /**
     * Test management.
     */
    
    @Before public void setUp() throws Exception {
        managerLocator = new MockManagerLocator();
        dataManager = (MockManagerLocator.MockDataManager)
                managerLocator.getDataManager();
        taskManager = (MockManagerLocator.MockTaskManager)
                managerLocator.getTaskManager();
        InternalContext.setManagerLocator(managerLocator);
    }

    @After public void tearDownClass() throws Exception {
        InternalContext.setManagerLocator(null);
    }
    
    /*
     * Test constructors
     */

    @Test
    public void testNoArgConstructor() throws Exception {
        ScalableDeque<Integer> deque = new ScalableDeque<Integer>();
    }

    @Test
    public void testOneArgConstructorTrue() throws Exception {
        new ScalableDeque<Integer>(true);
    }

    @Test
    public void testOneArgConstructorFalse() throws Exception {
        new ScalableDeque<Integer>(false);
    }

    @Test
    public void testCopyConstructor() throws Exception {

        final Deque<Integer> control = new ArrayDeque<Integer>();

        for (int i = 0; i < 32; i++) {
            control.offer(i);
        }
        ScalableDeque<Integer> test =
                new ScalableDeque<Integer>(control);
        assertTrue(control.containsAll(test));
        assertTrue(test.containsAll(control));
    }

    @Test
    public void testNullCopyConstructor() throws Exception {
        try {
            new ScalableDeque<Integer>(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }


    /*
     * Test size
     */
    @Test
    public void testSizeOnEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertEquals(0, d.size());
    }

    @Test
    public void testSizeOnNonEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        assertEquals(1, d.size());
    }

    @Test
    public void testSizeOnDequeAfterRemoval() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        d.poll();
        assertEquals(0, d.size());
    }

    @Test
    public void testSizeAfterClear() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        d.clear();
        assertEquals(0, d.size());
    }

    @Test
    public void testSizeWithMultipleSameElements() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.offer(1);
        }
        assertEquals(10, d.size());
    }

    /*
     * Test isEmtpy
     */
    @Test
    public void testIsEmptyTrue() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertTrue(d.isEmpty());
    }

    @Test
    public void testIsEmptyFalse() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        assertFalse(d.isEmpty());
    }

    @Test
    public void testIsEmptyAfterClear() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        d.clear();
        assertTrue(d.isEmpty());
    }

    @Test
    public void testIsEmptyAfterRemoval() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        d.remove();
        assertTrue(d.isEmpty());
    }


    /*
     * Test clear
     */
    @Test
    public void testClear() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        assertFalse(d.isEmpty());
        d.clear();
        assertTrue(d.isEmpty());
        assertEquals(0, d.size());
        assertEquals(null, d.poll());
    }

    @Test
    public void testClearOnEmptyMap() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertTrue(d.isEmpty());
        d.clear();
        assertTrue(d.isEmpty());
        assertEquals(0, d.size());
        assertEquals(null, d.poll());
    }

    @Test
    public void testMultipleClears() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertTrue(d.isEmpty());
        d.clear();
        d.clear();
        d.clear();
        assertTrue(d.isEmpty());
        assertEquals(0, d.size());
        assertEquals(null, d.poll());
    }

    @Test
    public void testClearThenAdd() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertTrue(d.isEmpty());
        d.clear();
        d.add(5);
        assertFalse(d.isEmpty());
        assertEquals(1, d.size());
        assertEquals(5, (int) d.getFirst());
    }


    /*
     * Test contains
     */
    @Test
    public void testContains() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        assertTrue(d.contains(1));
    }

    @Test
    public void testContainsOnEmptyMap() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertFalse(d.contains(1));
    }

    @Test
    public void testContainsWithMultipleElements() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.offer(i);
        }
        for (int i = 0; i < 10; ++i) {
            assertTrue(d.contains(i));
        }
    }

    @Test
    public void testContainsWithMultipleSameElements() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.offer(1);
        }
        assertTrue(d.contains(1));
    }


    /*
     * Test add/offer operations
     */
    @Test
    public void testAdd() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(1);
        assertEquals(1, (int) d.getFirst());
    }

    @Test
    public void testMultipleAdds() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        for (int i = 0; i < 10; ++i) {
            assertEquals(i, (int) d.remove());
        }
    }

    @Test
    public void testAddAll() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        Deque<Integer> control = new ArrayDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            control.add(i);
        }
        d.addAll(control);

        assertEquals(10, d.size());
        for (int i = 0; i < 10; ++i) {
            assertTrue(d.contains(i));
        }
        assertTrue(d.containsAll(control));
        assertTrue(control.containsAll(d));
    }

    @Test
    public void testAddFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.addFirst(1);
        assertEquals(1, (int) d.getFirst());
    }

    @Test
    public void testMultipleAddFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.addFirst(i);
        }
        assertEquals(9, (int) d.getFirst());
    }

    @Test
    public void testAddLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.addLast(1);
        assertEquals(1, (int) d.getLast());
    }

    @Test
    public void testMultipleAddLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.addLast(i);
        }
        assertEquals(9, (int) d.getLast());
    }

    @Test
    public void testOffer() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offer(1);
        assertEquals(1, (int) d.getFirst());
    }

    @Test
    public void testMultipleOffers() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.offer(i);
        }
        for (int i = 0; i < 10; ++i) {
            assertEquals(i, (int) d.remove());
        }
    }

    @Test
    public void testOfferFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offerFirst(1);
        assertEquals(1, (int) d.getFirst());
    }

    @Test
    public void testMultipleOfferFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.offerFirst(i);
        }
        assertEquals(9, (int) d.getFirst());
    }

    @Test
    public void testOfferLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.offerLast(1);
        assertEquals(1, (int) d.getLast());
    }

    @Test
    public void testMultipleOfferLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.offerLast(i);
        }
        assertEquals(9, (int) d.getLast());
    }

    @Test
    public void testAddLastNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.addLast(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testAddFirstNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.addFirst(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    /*
     * Test peek/get/element access operations
     */
    @Test
    public void testElement() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        assertEquals(2, (int) d.element());
    }

    @Test
    public void testElementOnEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.element();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException nsee) {
        }
    }

    @Test
    public void testPeek() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        assertEquals(2, (int) d.peek());
    }

    @Test
    public void testPeekOnEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertEquals(null, d.peek());
    }

    @Test
    public void testPeekFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        d.add(3);
        assertEquals(2, (int) d.peekFirst());
    }

    @Test
    public void testPeekLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        d.add(3);
        assertEquals(3, (int) d.peekLast());
    }

    @Test
    public void testGetFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        d.add(3);
        assertEquals(2, (int) d.getFirst());
    }

    @Test
    public void testGetLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        d.add(3);
        assertEquals(3, (int) d.getLast());
    }

    @Test
    public void testGetFirstOnEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.getFirst();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException nsee) {
        }
    }

    @Test
    public void testGetLastOnEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.getLast();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException nsee) {
        }
    }

    /*
     * Test remove operations
     */
    @Test
    public void testRemove() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        assertEquals(2, (int) d.remove());
    }

    @Test
    public void testRemoveFirst() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        assertEquals(2, (int) d.removeFirst());
    }

    @Test
    public void testRemoveFirstWithMultipleElements() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertEquals(0, (int) d.removeFirst());
    }

    @Test
    public void testRemoveLast() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(2);
        assertEquals(2, (int) d.removeLast());
    }

    @Test
    public void testRemoveLastWithMultipleElements() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertEquals(9, (int) d.removeLast());
    }

    @Test
    public void testRemoveFirstOccurrence() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertTrue(d.removeFirstOccurrence(5));
        assertEquals(9, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testRemoveFirstOccurrenceNotPresent() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertFalse(d.removeFirstOccurrence(10));
        assertEquals(10, d.size());
        assertFalse(d.contains(10));
    }

    @Test
    public void testRemoveFirstOccurrenceWithMultipleOccurrences()
            throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 10; ++i) {
                d.add(i);
            }
        }
        assertTrue(d.removeFirstOccurrence(5));
        assertEquals(29, d.size());
        assertTrue(d.contains(5));

        // the first instance of 5 should be at element 14 now
        int iterCount = 0;
        Iterator<Integer> iter = d.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            if (i == 5) {
                break;
            }
            iterCount++;
        }
        assertEquals(14, iterCount);
        iterCount++;

        // check that we still have the second instance as
        // well
        while (iter.hasNext()) {
            int i = iter.next();
            if (i == 5) {
                break;
            }
            iterCount++;
        }
        // the second instances is at 24
        assertEquals(24, iterCount);
    }

    @Test
    public void testRemoveLastOccurrence() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertTrue(d.removeLastOccurrence(5));
        assertEquals(9, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testRemoveLastOccurrenceWithMultipleOccurrences()
            throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 10; ++i) {
                d.add(i);
            }
        }
        assertTrue(d.removeLastOccurrence(5));
        assertEquals(29, d.size());
        assertTrue(d.contains(5));

        // the first instance of 5 should be at element 5 still
        int iterCount = 0;
        Iterator<Integer> iter = d.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            if (i == 5) {
                break;
            }
            iterCount++;
        }
        assertEquals(5, iterCount);
        iterCount++;

        // check that we still have the second instance as
        // well
        while (iter.hasNext()) {
            int i = iter.next();
            if (i == 5) {
                break;
            }
            iterCount++;
        }
        // the second instances is at 15
        assertEquals(15, iterCount);
    }

    @Test
    public void testRemoveLastOccurrenceNotPresent() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertFalse(d.removeLastOccurrence(10));
        assertEquals(10, d.size());
        assertFalse(d.contains(10));
    }

    @Test
    public void testRemoveAllOccurrences() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertTrue(d.removeAllOccurrences(5));
        assertEquals(9, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testRemoveAllOccurrencesWithMultipleOccurrences()
            throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 10; ++i) {
                d.add(i);
            }
        }
        assertTrue(d.removeAllOccurrences(5));
        assertEquals(27, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testRemoveAllOccurrencesWhenOccurrenceNotPresent() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        assertFalse(d.removeAllOccurrences(10));
        assertEquals(10, d.size());
        assertFalse(d.contains(10));
    }

    @Test
    public void testRemoveAll() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Collection<Integer> c = new LinkedList<Integer>();
        c.add(5);
        assertTrue(d.removeAll(c));
        assertEquals(9, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testRemoveAllWithMultipleOccurrences()
            throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 10; ++i) {
                d.add(i);
            }
        }
        Collection<Integer> c = new LinkedList<Integer>();
        c.add(5);
        assertTrue(d.removeAll(c));
        assertEquals(27, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testRemoveAllWithMultipleRemoves()
            throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 10; ++i) {
                d.add(i);
            }
        }
        Collection<Integer> c = new LinkedList<Integer>();
        c.add(5);
        c.add(6);
        assertTrue(d.removeAll(c));
        assertEquals(24, d.size());
        assertFalse(d.contains(5));
        assertFalse(d.contains(6));
    }

    @Test
    public void testRemoveAllWhenOccurrenceNotPresent() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Collection<Integer> c = new LinkedList<Integer>();
        c.add(10);
        assertFalse(d.removeAll(c));
        assertEquals(10, d.size());
        assertFalse(d.contains(10));
    }

    @Test
    public void testRemoveNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.remove(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testRemoveFirstOccurrenceNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.removeFirstOccurrence(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testRemoveLastOccurrenceNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.removeLastOccurrence(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testRemoveAllOccurrencesNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.removeAllOccurrences(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testRemoveAllNull() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.removeAll(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }

    /*
     * Test push/pop
     */
    @Test
    public void testPush() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.push(1);
        assertEquals(1, (int) d.remove());
    }

    @Test
    public void testPop() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        d.add(1);
        assertEquals(1, (int) d.pop());
    }

    @Test
    public void testPopOnEmptyDeque() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        try {
            d.pop();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException nsee) {
        }
    }

    /*
     * Test iterator
     */
    @Test
    public void testIterator() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Iterator<Integer> iter = d.iterator();
        int count = 0;
        while (iter.hasNext()) {
            assertEquals(count++, (int) iter.next());
        }
        assertEquals(10, count);
    }

    @Test
    public void testIteratorHasNext() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        assertFalse(d.iterator().hasNext());
        d.add(1);
        assertTrue(d.iterator().hasNext());
    }

    @Test
    public void testIteratorRemoval() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Iterator<Integer> iter = d.iterator();
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            if (count++ == 5) {
                iter.remove();
            }
        }

        assertEquals(10, count);
        assertEquals(9, d.size());
        assertFalse(d.contains(5));
    }

    @Test
    public void testIteratorRemovalTwice() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Iterator<Integer> iter = d.iterator();
        iter.next();
        iter.remove();
        try {
            iter.remove();
            fail("expected IllegalStateException");
        } catch (IllegalStateException ise) {
        }
    }

    @Test
    public void testIteratorRemovalBeforeNext() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Iterator<Integer> iter = d.iterator();
        try {
            iter.remove();
            fail("expected IllegalStateException");
        } catch (IllegalStateException ise) {
        }
    }

    @Test
    public void testIteratorNextNotPresent() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        Iterator<Integer> iter = d.iterator();

        try {
            iter.next();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException nsee) {
        }
    }

    @Test
    public void testDescendingIterator() throws Exception {
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Iterator<Integer> iter = d.descendingIterator();
        int count = 9;
        while (iter.hasNext()) {
            assertEquals(count--, (int) iter.next());
        // if we've gone through all 10 elements, count
        // should be 9 - 10 = -1
        }
        assertEquals(-1, count);
    }


    /*
     * Test serializability
     */
    @Test
    public void testDequeSeriazable() throws Exception {
        final String name = "test-deque";

        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        AppContext.getDataManager().setBinding(name, d);
        dataManager.serializeDataStore();

        ScalableDeque<Integer> d2 =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name));
        assertEquals(10, d2.size());
        int i = 0;
        for (Integer inDeque : d2) {
            assertEquals(i++, (int) inDeque);
        }

        AppContext.getDataManager().removeBinding(name);

    }

    @Test
    public void testIteratorSeriazable() throws Exception {
        final String name = "test-iterator";

        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        AppContext.getDataManager().setBinding(name, d.iterator());
        dataManager.serializeDataStore();

        Iterator<Integer> it =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name));

        int i = 0;
        while (it.hasNext()) {
            assertEquals(i++, (int) it.next());
        }
        assertEquals(10, i);

        AppContext.getDataManager().removeBinding(name);
    }

    @Test
    public void testIteratorSeriazableWithRemovals() throws Exception {
        final String name = "test-iterator";
        final String name2 = "test-deque";

        // create the deque
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        AppContext.getDataManager().setBinding(name, d.iterator());
        AppContext.getDataManager().setBinding(name2, d);
        dataManager.serializeDataStore();

        // remove some elements while the iterator is serialized
        ScalableDeque<Integer> d2 =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name2));
        for (int i = 1; i < 10; i += 2) {
            d2.remove(i);        // load the iterator back
        }
        Iterator<Integer> it =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name));

        int i = 0;
        while (it.hasNext()) {
            assertEquals(i, (int) it.next());
            i += 2;
        }
        assertEquals(10, i);

        AppContext.getDataManager().removeBinding(name);
        AppContext.getDataManager().removeBinding(name2);
    }

    @Test
    public void testConcurrentIteratorSeriazableWithRemovalOfNextElements()
            throws Exception {

        final String name = "test-iterator";
        final String name2 = "test-deque";

        // create the deque
        ScalableDeque<Integer> d = new ScalableDeque<Integer>(true);
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        AppContext.getDataManager().setBinding(name, d.iterator());
        AppContext.getDataManager().setBinding(name2, d);
        dataManager.serializeDataStore();

        // remove the iterator's first 5 elements while the
        // iterator is serialized
        ScalableDeque<Integer> d2 =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name2));
        for (int i = 0; i < 5; i++) {
            d2.remove(i);        // load the iterator back
        }
        Iterator<Integer> it =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name));

        int i = 5;
        while (it.hasNext()) {
            assertEquals(i, (int) it.next());
            i++;
        }
        assertEquals(10, i);

        AppContext.getDataManager().removeBinding(name);
        AppContext.getDataManager().removeBinding(name2);
    }

    @Test
    public void testNonConcurrentIteratorSeriazableWithRemovalOfNextElements()
            throws Exception {

        final String name = "test-iterator";
        final String name2 = "test-deque";

        // create the deque
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        AppContext.getDataManager().setBinding(name, d.iterator());
        AppContext.getDataManager().setBinding(name2, d);
        dataManager.serializeDataStore();

        // remove the iterator's first 5 elements while the
        // iterator is serialized
        ScalableDeque<Integer> d2 =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name2));
        for (int i = 0; i < 5; i++) {
            d2.remove();        // load the iterator back
        }
        Iterator<Integer> it =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name));

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

    @Test
    public void testIteratorRemovalWhereCurrentElementWasAlreadyRemoved()
            throws Exception {

        final String name = "test-iterator";
        final String name2 = "test-deque";

        // create the deque
        ScalableDeque<Integer> d = new ScalableDeque<Integer>();
        for (int i = 0; i < 10; ++i) {
            d.add(i);
        }
        Iterator<Integer> it = d.iterator();
        // advance the iterator forward one set
        it.next();
        AppContext.getDataManager().setBinding(name, it);
        AppContext.getDataManager().setBinding(name2, d);
        dataManager.serializeDataStore();

        // remove the iterator's first 5 elements while the
        // iterator is serialized
        ScalableDeque<Integer> d2 =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name2));
        // remove the first element, which the iterator
        // already advanced over
        d2.removeFirst();


        // load the iterator back
        Iterator<Integer> it2 =
                Util.uncheckedCast(AppContext.getDataManager().getBinding(name));

        // now try to remove the already-removed element
        // from the iterator
        it2.remove();

    // the above call shouldn't throw an object not
    // found exception
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
