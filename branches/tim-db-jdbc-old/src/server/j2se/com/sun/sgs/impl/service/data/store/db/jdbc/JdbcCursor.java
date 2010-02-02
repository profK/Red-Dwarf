/*
 * Copyright 2007 Sun Microsystems, Inc.
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

package com.sun.sgs.impl.service.data.store.db.jdbc;

import com.sun.sgs.impl.service.data.store.db.DbCursor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Provides a cursor implementation using JDBC. */
public class JdbcCursor implements DbCursor {

    /** The JDBC connection associated with this cursor. */
    private final Connection connection;

    /** The name of the JDBC table associated with this cursor. */
    private final String table;

    /** The query results, or null if none were found. */
    private ResultSet results = null;

    /**
     * The statement used to produce the value in results, or null if results
     * is null.
     */
    private Statement statement = null;

    /**
     * Creates an instance of this class.
     *
     * @param connection the JDBC connection
     * @param table the name of the JDBC table
     */
    JdbcCursor(Connection connection, String table) {
	this.connection = connection;
	this.table = table;
    }

    /** {@inheritDoc} */
    public byte[] getKey() {
	try {
	    return (results == null) ? null : results.getBytes(1);
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public byte[] getValue() {
	try {
	    return (results == null) ? null : results.getBytes(2);
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean findFirst() {
	try {
	    Statement newStatement = connection.createStatement();
	    try {
		ResultSet newResults = newStatement.executeQuery(
		    "SELECT k, v FROM " + table + " WHERE k = ''");
		if (newResults.first()) {
		    results = newResults;
		    statement = newStatement;
		    newStatement = null;
		    return true;
		} else {
		    return false;
		}
	    } finally {
		if (newStatement != null) {
		    newStatement.close();
		}
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean findNext() {
	try {
	    if (results == null) {
		return false;
	    } else if (!results.next()) {
		statement.close();
		results = null;
		statement = null;
		return false;
	    } else {
		return true;
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean findNext(byte[] key) {
	try {
	    PreparedStatement prepared = connection.prepareStatement(
		"SELECT k, v FROM " + table + " WHERE k = ?");
	    try {
		prepared.setBytes(1, key);
		ResultSet newResults = prepared.executeQuery();
		if (newResults.first()) {
		    statement = prepared;
		    results = newResults;
		    prepared = null;
		    return true;
		} else {
		    return false;
		}
	    } finally {
		if (prepared != null) {
		    prepared.close();
		}
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean findLast() {
	try {
	    if (results == null) {
		return false;
	    } else if (!results.last()) {
		statement.close();
		results = null;
		statement = null;
		return false;
	    } else {
		return true;
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean putNoOverwrite(byte[] key, byte[] value) {
	try {
	    PreparedStatement prepared = connection.prepareStatement(
		"SELECT k, v FROM " + table + " WHERE k = ?",
		ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    try {
		prepared.setBytes(1, key);
		ResultSet newResults = prepared.executeQuery();
		if (newResults.first()) {
		    return false;
		}
		newResults.moveToInsertRow();
		newResults.updateBytes(1, key);
		newResults.updateBytes(2, value);
		newResults.insertRow();
		results = newResults;
		statement = prepared;
		prepared = null;
		return true;
	    } finally {
		if (prepared != null) {
		    prepared.close();
		}
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public void close() {
	try {
	    if (statement != null) {
		statement.close();
		statement = null;
		results = null;
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }
}
    
