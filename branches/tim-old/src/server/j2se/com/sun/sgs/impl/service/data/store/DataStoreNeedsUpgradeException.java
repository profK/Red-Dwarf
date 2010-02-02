/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved
 */

package com.sun.sgs.impl.service.data.store;

/**
 * Thrown when attempting to open a data store that needs to be upgraded.
 */
public class DataStoreNeedsUpgradeException extends DataStoreException {

    /** The version of the serialized form. */
    private static final long serialVersionUID = 1;

    /**
     * Creates an instance of this class with the specified detail message.
     *
     * @param	message the detail message or <code>null</code>
     */
    public DataStoreNeedsUpgradeException(String message) {
	super(message);
    }

    /**
     * Creates an instance of this class with the specified detail message and
     * cause.
     *
     * @param	message the detail message or <code>null</code>
     * @param	cause the cause or <code>null</code>
     */
    public DataStoreNeedsUpgradeException(String message, Throwable cause) {
	super(message, cause);
    }
}
    
