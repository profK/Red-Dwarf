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
import com.sun.sgs.impl.service.data.store.db.DbDatabase;
import com.sun.sgs.impl.service.data.store.db.DbTransaction;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Provides a database implementation using JDBC. */
public class JdbcDatabase implements DbDatabase {

    /** The name of the associated table. */
    private final String table;

    /**
     * Creates an instance of this class.
     *
     * @param connection a JDBC connection for creating the table
     * @param fileName the name of the file for the database
     * @param create whether to create the database
     */
    JdbcDatabase(Connection connection, String fileName, boolean create)
	throws FileNotFoundException
    {
	table = fileName;
	try {
	    Statement statement = connection.createStatement();
	    try {
		if (create) {
		    statement.executeUpdate(
			"CREATE TABLE " + table +
			" (k BLOB, PRIMARY KEY(k(8)), v LONGBLOB)");
		} else {
		    ResultSet results = statement.executeQuery(
			"SHOW TABLES LIKE '" + table + "'");
		    if (!results.first()) {
			throw new FileNotFoundException(
			    "Table " + table + " was not found");
		    }
		}
	    } finally {
		statement.close();
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /* -- Implement DbDatabase -- */

    /** {@inheritDoc} */
    public byte[] get(DbTransaction txn, byte[] key, boolean forUpdate) {
	Connection connection = JdbcTransaction.getJdbcConnection(txn);
	try {
	    PreparedStatement prepared = connection.prepareStatement(
		"SELECT v FROM " + table + " WHERE k = ?");
	    try {
		prepared.setBytes(1, key);
		ResultSet results = prepared.executeQuery();
		return results.first() ? results.getBytes(1) : null;
	    } finally {
		prepared.close();
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public void put(DbTransaction txn, byte[] key, byte[] value) {
	Connection connection = JdbcTransaction.getJdbcConnection(txn);
	try {
	    PreparedStatement prepared = connection.prepareStatement(
		"REPLACE " + table + " VALUES (?, ?)");
	    try {
		prepared.setBytes(1, key);
		prepared.setBytes(2, value);
		prepared.executeUpdate();
	    } finally {
		prepared.close();
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean putNoOverwrite(
	DbTransaction txn, byte[] key, byte[] value)
    {
	Connection connection = JdbcTransaction.getJdbcConnection(txn);
	try {
	    PreparedStatement prepared = connection.prepareStatement(
		"SELECT k, v FROM " + table + " WHERE k = ?",
		ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    try {
		prepared.setBytes(1, key);
		ResultSet results = prepared.executeQuery();
		if (results.first()) {
		    return false;
		}
		results.moveToInsertRow();
		results.updateBytes(1, key);
		results.updateBytes(2, value);
		results.insertRow();
		return true;
	    } finally {
		prepared.close();
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public boolean delete(DbTransaction txn, byte[] key) {
	Connection connection = JdbcTransaction.getJdbcConnection(txn);
	try {
	    PreparedStatement prepared = connection.prepareStatement(
		"SELECT k FROM " + table + " WHERE k = ?",
		ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
		);
	    try {
		prepared.setBytes(1, key);
		ResultSet results = prepared.executeQuery();
		if (!results.first()) {
		    return false;
		}
		results.deleteRow();
		return true;
	    } finally {
		prepared.close();
	    }
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public DbCursor openCursor(DbTransaction txn) {
	return new JdbcCursor(
	    JdbcTransaction.getJdbcConnection(txn), table);
    }

    /** {@inheritDoc} */
    public void close() { }
}
    
	
