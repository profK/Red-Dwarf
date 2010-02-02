/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved
 */

package com.sun.sgs.impl.service.data.store;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.ShortBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;

/**
 * Encapsulates the layout of meta data stored at the start of the info
 * database.  This class cannot be instantiated.
 *
 * The value for key 0 stores a magic number common to all DataStoreImpl
 * databases.
 *
 * Key 1 stores the major version number, which must match the value in the
 * current version of the implementation, or be a value that represents a
 * version of the database that the implementation knows how to upgrade.
 *
 * Key 2 stores the minor version number, which can vary between the database
 * and the implementation.
 *
 * Key 3 stores a sequence of longs that represents blocks of free ID numbers
 * to use for allocating new objects.  Each 8 byte long records the first free
 * ID of a block of IDs.  All IDs beyond the last block are free.  The length
 * of free object ID blocks is determined by key 5.
 *
 * Key 4 stores the ID of the next free transaction ID number for the network
 * version to use in allocating transactions.
 *
 * Key 5 stores the size of blocks of free object ID numbers, represented as an
 * 8 byte number, and always a power of 2.
 *
 * Version history:
 *
 * Version 1.0: Initial version, 11/3/2006
 * Version 2.0: Add NEXT_TXN_ID, 2/15/2007
 * Version 3.0: Add support for multiple free ID blocks, 5/4/2007
 */
final class DataStoreHeader {

    /** The key for the magic number. */
    static final long MAGIC_KEY = 0;

    /** The key for the major version number. */
    static final long MAJOR_KEY = 1;

    /** The key for the minor version number. */
    static final long MINOR_KEY = 2;

    /** The key for information about blocks of free object IDs. */
    static final long FREE_OBJ_IDS_KEY = 3;

    /**
     * The key for the value of the next free transaction ID, used in the
     * network version.
     */
    static final long NEXT_TXN_ID_KEY = 4;

    /** The key for the free object IDs block size. */
    static final long FREE_BLOCK_SIZE_KEY = 5;

    /** The magic number: DaRkStAr. */
    static final long MAGIC = 0x4461526b53744172L;

    /** The major version number. */
    static final short MAJOR_VERSION = 3;

    /** The minor version number. */
    static final short MINOR_VERSION = 0;

    /** The first free object ID. */
    static final long INITIAL_NEXT_OBJ_ID = 1;

    /** The first free transaction ID. */
    static final long INITIAL_NEXT_TXN_ID = 1;

    /**
     * The default free object ID block size, which must be a power of 2 no
     * smaller than 1024.  For testing purposes, the default can be overridden
     * by setting the
     * com.sun.sgs.impl.service.data.store.DataStoreHeader.free.block.size
     * system property.
     */
    static final long DEFAULT_FREE_BLOCK_SIZE = 1L<<32;

    /** This class cannot be instantiated. */
    private DataStoreHeader() {
	throw new AssertionError();
    }

    /**
     * Verifies the header information in the database, and returns its minor
     * version number.
     *
     * @param	db the database
     * @param	bdbTxn the Berkeley DB transaction
     * @param	upgrade whether to upgrade the database to the latest version
     *		if it has an earlier version for which upgrading is supported
     * @return	a string describing the header
     * @throws	DatabaseException if a problem occurs accessing the database
     * @throws	DataStoreNeedsUpgradeException if the database needs to be
     *		upgraded and the upgrade argument is false
     * @throws	DataStoreException if the format of the header information is
     *		incorrect
     */
    static String verify(
	Database db, com.sleepycat.db.Transaction bdbTxn, boolean upgrade)
	throws DatabaseException
    {
	DatabaseEntry key = new DatabaseEntry();
	DatabaseEntry value = new DatabaseEntry();

	LongBinding.longToEntry(MAGIC_KEY, key);
	get(db, bdbTxn, key, value, null);
	long magic = LongBinding.entryToLong(value);
	if (magic != MAGIC) {
	    throw new DataStoreException(
		"Bad magic number in header: expected " +
		toHexString(MAGIC) + ", found " + toHexString(magic));
	}

	LongBinding.longToEntry(MAJOR_KEY, key);
	get(db, bdbTxn, key, value, null);
	int majorVersion = ShortBinding.entryToShort(value);
	if (majorVersion != MAJOR_VERSION) {
	    maybeUpgrade(db, bdbTxn, majorVersion, upgrade);
	}

	LongBinding.longToEntry(MINOR_KEY, key);
	get(db, bdbTxn, key, value, null);
	int minorVersion = ShortBinding.entryToShort(value);

	return headerString(minorVersion);
    }

    /**
     * Handles a database with a non-current major version by performing an
     * upgrade if possible and if upgrade is true, and otherwise throwing an
     * exception.
     */
    private static void maybeUpgrade(Database db,
				     com.sleepycat.db.Transaction bdbTxn,
				     int majorVersion,
				     boolean upgrade)
	throws DatabaseException
    {
	if (majorVersion != 2) {
	    throw new DataStoreException(
		"Wrong major version number: expected " + MAJOR_VERSION +
		", found " + majorVersion);
	} else if (!upgrade) {
	    throw new DataStoreNeedsUpgradeException(
		"The data store needs to be upgraded");
	}

	DatabaseEntry key = new DatabaseEntry();
	DatabaseEntry value = new DatabaseEntry();

	/*
	 * The free object ID information itself is OK, and so is the minor
	 * version number, so update the major version and store the default
	 * free object ID block size.
	 */
	LongBinding.longToEntry(MAJOR_KEY, key);
	ShortBinding.shortToEntry(MAJOR_VERSION, value);
	put(db, bdbTxn, key, value);

	LongBinding.longToEntry(FREE_BLOCK_SIZE_KEY, key);
	LongBinding.longToEntry(getFreeBlockSize(), value);
	putNoOverwrite(db, bdbTxn, key, value);
    }

    /** Returns the free block size to use for a new or upgraded database. */
    private static long getFreeBlockSize() {
	long blockSize = Long.getLong(
	    DataStoreHeader.class.getName() + ".free.block.size",
	    DEFAULT_FREE_BLOCK_SIZE);
	if (blockSize < 1024 || !isPowerOfTwo(blockSize)) {
	    throw new IllegalArgumentException(
		"Invalid block size: " + blockSize);
	}
	return blockSize;
    }

    /** Checks if the argument is a power of two greater than zero. */
    static boolean isPowerOfTwo(long n) {
	/*
	 * True if the previous number rolls over to the next place, and so has
	 * no overlap.
	 */
	return (n & (n - 1)) == 0;
    }

    /**
     * Stores header information in the database.
     *
     * @param	db the database
     * @param	bdbTxn the Berkeley DB transaction
     * @return	a string describing the header
     * @throws	DatabaseException if a problem occurs accessing the database
     */
    static String create(Database db, com.sleepycat.db.Transaction bdbTxn)
	throws DatabaseException
    {
	DatabaseEntry key = new DatabaseEntry();
	DatabaseEntry value = new DatabaseEntry();

	LongBinding.longToEntry(MAGIC_KEY, key);
	LongBinding.longToEntry(MAGIC, value);
	putNoOverwrite(db, bdbTxn, key, value);

	LongBinding.longToEntry(MAJOR_KEY, key);
	ShortBinding.shortToEntry(MAJOR_VERSION, value);
	putNoOverwrite(db, bdbTxn, key, value);

	LongBinding.longToEntry(MINOR_KEY, key);
	ShortBinding.shortToEntry(MINOR_VERSION, value);
	putNoOverwrite(db, bdbTxn, key, value);

	setFreeObjectIds(db, bdbTxn, new long[] { INITIAL_NEXT_OBJ_ID });

	LongBinding.longToEntry(NEXT_TXN_ID_KEY, key);
	LongBinding.longToEntry(INITIAL_NEXT_TXN_ID, value);
	putNoOverwrite(db, bdbTxn, key, value);

	LongBinding.longToEntry(FREE_BLOCK_SIZE_KEY, key);
	LongBinding.longToEntry(getFreeBlockSize(), value);
	putNoOverwrite(db, bdbTxn, key, value);

	return headerString(MINOR_VERSION);
    }

    /**
     * Gets information about blocks of free object IDs.
     *
     * @param	db the database
     * @param	bdbTxn the Berkeley DB transaction
     * @return	the free object IDs
     * @throws	DatabaseException if a problem occurs accessing the database
     */
    static long[] getFreeObjectIds(Database db,
				   com.sleepycat.db.Transaction bdbTxn)
	throws DatabaseException
    {
	DatabaseEntry key = new DatabaseEntry();
	LongBinding.longToEntry(FREE_OBJ_IDS_KEY, key);
	DatabaseEntry value = new DatabaseEntry();
	get(db, bdbTxn, key, value, null);
	byte[] bytes = value.getData();
	TupleInput in = new TupleInput(bytes);
	int numBlocks = bytes.length / 8;
	long[] result = new long[numBlocks];
	for (int i = 0; i < numBlocks; i++) {
	    result[i] = in.readLong();
	}
	return result;
    }

    /**
     * Sets information about blocks of free object IDs.
     *
     * @param	db the database
     * @param	bdbTxn the Berkeley DB transaction
     * @param	freeIdBlocks the free object IDs.
     * @return	the free object IDs
     * @throws	DatabaseException if a problem occurs accessing the database
     * @throws	IllegalArgumentException if an element of freeObjectIds is
     *		negative
     */
    static void setFreeObjectIds(
	Database db, com.sleepycat.db.Transaction bdbTxn, long[] freeObjectIds)
	throws DatabaseException
    {
	DatabaseEntry key = new DatabaseEntry();
	LongBinding.longToEntry(FREE_OBJ_IDS_KEY, key);
	byte[] bytes = new byte[8 * freeObjectIds.length];
	TupleOutput out = new TupleOutput(bytes);
	for (long id : freeObjectIds) {
	    if (id < 0) {
		throw new IllegalArgumentException(
		    "Free object IDs must not be negative");
	    }
	    out.writeLong(id);
	}
	DatabaseEntry value = new DatabaseEntry(bytes);
	put(db, bdbTxn, key, value);
    }

    /**
     * Gets the size of the free object ID blocks in the database.
     *
     * @param	db the database
     * @param	bdbTxn the Berkeley DB transaction
     * @return	the size of free object IDs blocks
     * @throws	DatabaseException if a problem occurs accessing the database
     */
    static long getFreeObjectIdsBlockSize(Database db,
					  com.sleepycat.db.Transaction bdbTxn)
	throws DatabaseException
    {
	DatabaseEntry key = new DatabaseEntry();
	LongBinding.longToEntry(FREE_BLOCK_SIZE_KEY, key);
	DatabaseEntry value = new DatabaseEntry();
	get(db, bdbTxn, key, value, null);
	return LongBinding.entryToLong(value);
    }

    /**
     * Returns the next available ID stored under the specified key, and
     * increments the stored value by the specified amount.  The return value
     * will be a positive number.
     *
     * @param	key the key under which the ID is stored
     * @param	db the database
     * @param	bdbTxn the Berkeley DB transaction
     * @param	increment the amount to increment the stored amount
     * @return	the next available ID
     * @throws	DatabaseException if a problem occurs accessing the database
     */
    static long getNextId(long key,
			  Database db,
			  com.sleepycat.db.Transaction bdbTxn,
			  long increment)
	throws DatabaseException
    {
	DatabaseEntry keyEntry = new DatabaseEntry();
	LongBinding.longToEntry(key, keyEntry);
	DatabaseEntry valueEntry = new DatabaseEntry();
	get(db, bdbTxn, keyEntry, valueEntry, LockMode.RMW);
	long result = LongBinding.entryToLong(valueEntry);
	LongBinding.longToEntry(result + increment, valueEntry);
	put(db, bdbTxn, keyEntry, valueEntry);
	return result;
    }

    /**
     * Returns a string that describes the header with the specified minor
     * version number.
     */
    private static String headerString(int minorVersion) {
	return "DataStoreHeader[magic:" + toHexString(MAGIC) +
	    ", version:" + MAJOR_VERSION + "." + minorVersion + "]";
    }

    /**
     * Reads a value from the database, throwing an exception if the key is not
     * present.
     */
    private static void get(Database db,
			    com.sleepycat.db.Transaction bdbTxn,
			    DatabaseEntry key,
			    DatabaseEntry value,
			    LockMode lockMode)
	throws DatabaseException
    {
	OperationStatus status = db.get(bdbTxn, key, value, lockMode);
	if (status == OperationStatus.NOTFOUND) {
	    throw new DataStoreException("Item not found");
	} else if (status != OperationStatus.SUCCESS) {
	    throw new DataStoreException(
		"Problem reading item: " + status);
	}
    }

    /**
     * Writes a value to the database, throwing an exception if the key is
     * already present.
     */
    private static void putNoOverwrite(Database db,
				       com.sleepycat.db.Transaction bdbTxn,
				       DatabaseEntry key,
				       DatabaseEntry value)
	throws DatabaseException
    {
	OperationStatus status = db.putNoOverwrite(bdbTxn, key, value);
	if (status == OperationStatus.KEYEXIST) {
	    throw new DataStoreException("Item already present");
	} else if (status != OperationStatus.SUCCESS) {
	    throw new DataStoreException("Problem writing item: " + status);
	}
    }

    /** Writes a value to the database. */
    private static void put(Database db,
			    com.sleepycat.db.Transaction bdbTxn,
			    DatabaseEntry key,
			    DatabaseEntry value)
	throws DatabaseException
    {
	OperationStatus status = db.put(bdbTxn, key, value);
	if (status != OperationStatus.SUCCESS) {
	    throw new DataStoreException("Problem writing item: " + status);
	}
    }

    /** Converts a long to a string in hexadecimal. */
    private static String toHexString(long l) {
	return String.format("0x%x", l);
    }
}
