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

package com.sun.sgs.impl.service.data.store;

import com.sun.sgs.profile.ProfileConsumer;
import com.sun.sgs.profile.ProfileCounter;
import com.sun.sgs.profile.ProfileOperation;
import com.sun.sgs.profile.ProfileProducer;
import com.sun.sgs.profile.ProfileRegistrar;
import com.sun.sgs.profile.ProfileSample;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionParticipant;

/**
 * Implements a {@link DataStore} that collects profiling information about
 * data store operations, implementing those operations and transaction
 * participant methods by delegating them to another data store.
 */
public class DataStoreProfileProducer
    implements DataStore, TransactionParticipant, ProfileProducer
{
    /** The associated data store. */
    private final DataStore dataStore;

    /** The associated transaction participant. */
    private final TransactionParticipant participant;

    /* -- Profile operations for the DataStore API -- */

    private ProfileOperation createObjectOp = null;
    private ProfileOperation markForUpdateOp = null;
    private ProfileOperation getObjectOp = null;
    private ProfileOperation getObjectForUpdateOp = null;
    private ProfileOperation setObjectOp = null;
    private ProfileOperation setObjectsOp = null;
    private ProfileOperation removeObjectOp = null;
    private ProfileOperation getBindingOp = null;
    private ProfileOperation setBindingOp = null;
    private ProfileOperation removeBindingOp = null;
    private ProfileOperation nextBoundNameOp = null;
    private ProfileOperation getClassIdOp = null;
    private ProfileOperation getClassInfoOp = null;
    private ProfileOperation nextObjectIdOp = null;

    /** Records the number of bytes read by the getObject method. */
    private ProfileCounter readBytesCounter = null;

    /** Records the number of objects read by the getObject method. */
    private ProfileCounter readObjectsCounter = null;

    /**
     * Records the number of bytes written by the setObject and setObjects
     * methods.
     */
    private ProfileCounter writtenBytesCounter = null;

    /**
     * Records the number of objects written by the setObject and setObjects
     * methods.
     */
    private ProfileCounter writtenObjectsCounter = null;

    /**
     * Records a list of the number of bytes read by calls to the getObject
     * method.
     */
    private ProfileSample readBytesSample = null;

    /**
     * Records a list of the number of bytes written by calls to the setObject
     * and setObjects methods.
     */
    private ProfileSample writtenBytesSample = null;

    /**
     * Creates an instance that delegates all {@link DataStore} and {@link
     * TransactionParticipant} methods to {@code dataStore}.
     *
     * @param dataStore the object to delegate to
     * @throws IllegalArgumentException if {@code dataStore} does not implement
     *	       {@code TransactionParticipant}
     */
    public DataStoreProfileProducer(DataStore dataStore) {
	if (dataStore == null) {
	    throw new NullPointerException("The dataStore must not be null");
	} else if (!(dataStore instanceof TransactionParticipant)) {
	    throw new IllegalArgumentException(
		"The dataStore must implement TransactionParticipant");
	}
	this.dataStore = dataStore;
	participant = (TransactionParticipant) dataStore;
    }

    /* -- Implement DataStore -- */

    /** {@inheritDoc} */
    public long createObject(Transaction txn) {
	report(createObjectOp);
	return dataStore.createObject(txn);
    }

    /** {@inheritDoc} */
    public void markForUpdate(Transaction txn, long oid) {
	report(markForUpdateOp);
	dataStore.markForUpdate(txn, oid);
    }

    /** {@inheritDoc} */
    public byte[] getObject(Transaction txn, long oid, boolean forUpdate) {
	report(forUpdate ? getObjectForUpdateOp : getObjectOp);
	byte[] result = dataStore.getObject(txn, oid, forUpdate);
	if (readBytesCounter != null) {
	    readBytesCounter.incrementCount(result.length);
	    readObjectsCounter.incrementCount();
	    readBytesSample.addSample(result.length);
	}
	return result;
    }

    /** {@inheritDoc} */
    public void setObject(Transaction txn, long oid, byte[] data) {
	report(setObjectOp);
	if (writtenBytesCounter != null) {
	    writtenBytesCounter.incrementCount(data.length);
	    writtenObjectsCounter.incrementCount();
	    writtenBytesSample.addSample(data.length);
	}
	dataStore.setObject(txn, oid, data);
    }

    /** {@inheritDoc} */
    public void setObjects(Transaction txn, long[] oids, byte[][] dataArray) {
	report(setObjectsOp);
	if (writtenBytesCounter != null && dataArray != null) {
	    for (byte[] data : dataArray) {
		if (data != null) {
		    writtenBytesCounter.incrementCount(data.length);
		    writtenObjectsCounter.incrementCount();
		    writtenBytesSample.addSample(data.length);
		}
	    }
	}
	dataStore.setObjects(txn, oids, dataArray);
    }

    /** {@inheritDoc} */
    public void removeObject(Transaction txn, long oid) {
	report(removeObjectOp);
	dataStore.removeObject(txn, oid);
    }

    /** {@inheritDoc} */
    public long getBinding(Transaction txn, String name) {
	report(getBindingOp);
	return dataStore.getBinding(txn, name);
    }

    /** {@inheritDoc} */
    public void setBinding(Transaction txn, String name, long oid) {
	report(setBindingOp);
	dataStore.setBinding(txn, name, oid);
    }

    /** {@inheritDoc} */
    public void removeBinding(Transaction txn, String name) {
	report(removeBindingOp);
	dataStore.removeBinding(txn, name);
    }

    /** {@inheritDoc} */
    public String nextBoundName(Transaction txn, String name) {
	report(nextObjectIdOp);
	return dataStore.nextBoundName(txn, name);
    }

    /** {@inheritDoc} */
    public boolean shutdown() {
	/* No profiling for this operation -- it only happens once */
	return dataStore.shutdown();
    }

    /** {@inheritDoc} */
    public int getClassId(Transaction txn, byte[] classInfo) {
	report(getClassIdOp);
	return dataStore.getClassId(txn, classInfo);
    }

    /** {@inheritDoc} */
    public byte[] getClassInfo(Transaction txn, int classId)
	throws ClassInfoNotFoundException
    {
	report(getClassInfoOp);
	return dataStore.getClassInfo(txn, classId);
    }

    /** {@inheritDoc} */
    public long nextObjectId(Transaction txn, long oid) {
	report(nextObjectIdOp);
	return dataStore.nextObjectId(txn, oid);
    }

    /* -- Implement TransactionParticipant -- */

    /** {@inheritDoc} */
    public boolean prepare(Transaction txn) throws Exception {
	return participant.prepare(txn);
    }

    /** {@inheritDoc} */
    public void commit(Transaction txn) {
	participant.commit(txn);
    }

    /** {@inheritDoc} */
    public void prepareAndCommit(Transaction txn) throws Exception {
	participant.prepareAndCommit(txn);
    }

    /** {@inheritDoc} */
    public void abort(Transaction txn) {
	participant.abort(txn);
    }

    /** {@inheritDoc} */
    public String getTypeName() {
	return participant.getTypeName();
    }

    /* -- Implement ProfileProducer -- */

    /** {@inheritDoc} */
    public void setProfileRegistrar(ProfileRegistrar profileRegistrar) {
        ProfileConsumer consumer =
            profileRegistrar.registerProfileProducer(this);

	createObjectOp = consumer.registerOperation("createObject");
	markForUpdateOp = consumer.registerOperation("markForUpdate");
	getObjectOp = consumer.registerOperation("getObject");
	getObjectForUpdateOp = consumer.registerOperation(
	    "getObjectForUpdate");
	setObjectOp = consumer.registerOperation("setObject");
	setObjectsOp = consumer.registerOperation("setObjects");
	removeObjectOp = consumer.registerOperation("removeObject");
	getBindingOp = consumer.registerOperation("getBinding");
	setBindingOp = consumer.registerOperation("setBinding");
	removeBindingOp = consumer.registerOperation("removeBinding");
	nextBoundNameOp = consumer.registerOperation("nextBoundName");
	getClassIdOp = consumer.registerOperation("getClassId");
	getClassInfoOp = consumer.registerOperation("getClassInfo");
	nextObjectIdOp = consumer.registerOperation("nextObjectIdOp");

	readBytesCounter = consumer.registerCounter("readBytes", true);
	readObjectsCounter = consumer.registerCounter("readObjects", true);
	writtenBytesCounter = consumer.registerCounter("writtenBytes", true);
	writtenObjectsCounter = consumer.registerCounter(
	    "writtenObjects", true);
	readBytesSample = consumer.registerSampleSource(
	    "readBytes", true, Integer.MAX_VALUE);
	writtenBytesSample = consumer.registerSampleSource(
	    "writtenBytes", true, Integer.MAX_VALUE);
    }

    /* -- Other public methods -- */
    
    /**
     * Returns a string representation of this object.
     *
     * @return	a string representation of this object
     */
    public String toString() {
	return "DataStoreProfileProducer[" + dataStore + "]";
    }

    /* -- Other methods -- */

    /** Reports a profile operation if profiling is enabled. */
    private void report(ProfileOperation op) {
	if (op != null) {
	    op.report();
	}
    }
}
