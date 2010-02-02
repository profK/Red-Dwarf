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

import com.sun.sgs.impl.service.data.store.Scheduler;
import com.sun.sgs.impl.service.data.store.db.DbDatabase;
import com.sun.sgs.impl.service.data.store.db.DbDatabaseException;
import com.sun.sgs.impl.service.data.store.db.DbEnvironment;
import com.sun.sgs.impl.service.data.store.db.DbTransaction;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a database implementation based on {@link java.sql JDBC}.
 */
public class JdbcEnvironment implements DbEnvironment {

    /** The package name. */
    private static final String PACKAGE =
	"com.sun.sgs.impl.service.data.store.db.jdbc";

    /** The logger for this class. */
    static final LoggerWrapper logger =
	new LoggerWrapper(Logger.getLogger(PACKAGE));

    private static final String URL_PROPERTY_NAME = PACKAGE + ".url";

    private final String url;

    private final Properties properties;

    /** The database driver, for creating connections. */
    private final Driver driver;

    /** A set of free connections. */
    private final List<Connection> connections = new ArrayList<Connection>();

    /**
     * Creates an instance of this class.
     *
     * @param	directory the directory containing database files (ignored)
     * @param	properties the properties to configure this instance
     * @param	scheduler the scheduler for running periodic tasks (ignored)
     * @throws	DbDatabaseException if an unexpected database problem occurs
     */
    public JdbcEnvironment(
	String directory, Properties properties, Scheduler scheduler)
    {
	if (logger.isLoggable(Level.CONFIG)) {
	    logger.log(Level.CONFIG,
		       "JdbcEnvironment directory:{0}, properties:{1}, " +
		       "scheduler:{2}",
		       directory, properties, scheduler);
	}
	url = properties.getProperty(URL_PROPERTY_NAME);
	if (url == null) {
	    throw new IllegalArgumentException(
		"The property must be specified: " + URL_PROPERTY_NAME);
	}
	this.properties = properties;
	try {
	    driver = DriverManager.getDriver(url);
	    returnConnection(getConnection());
	} catch (SQLException e) {
	    throw convertException(e);
	}
    }

    /**
     * Returns the correct exception for a Berkeley DB DatabaseException thrown
     * during an operation.  Throws an Error if recovery is needed.  Only
     * converts Berkeley DB transaction exceptions to the associated exceptions
     * if convertTxnExceptions is true.
     */
    static RuntimeException convertException(SQLException e) {
	throw new DbDatabaseException(
	    "Unexpected database exception: " + e, e);
    }

    /**
     * Obtains a connection, either by taking one from the cache of open
     * connections, or by creating a new one.  Make sure to call {@link
     * returnConnection} to return the connection when done using it.
     */
    Connection getConnection() throws SQLException {
	synchronized (connections) {
	    if (!connections.isEmpty()) {
		return connections.remove(0);
	    }
	}
	Connection connection = DriverManager.getConnection(url, properties);
	DatabaseMetaData metaData = connection.getMetaData();
	boolean supportsSerializable =
	    metaData.supportsTransactionIsolationLevel(
		Connection.TRANSACTION_SERIALIZABLE);
	if (!supportsSerializable) {
	    throw new IllegalArgumentException(
		"The database connection must support serializable" +
		" transactions");
	}
	connection.setTransactionIsolation(
	    Connection.TRANSACTION_SERIALIZABLE);
	connection.setAutoCommit(false);
	return connection;
    }

    /** Returns a connection to the cache of open connections. */
    void returnConnection(Connection connection) {
	synchronized (connections) {
	    connections.add(connection);
	}
    }

    /* -- Implement DbEnvironment -- */

    /** {@inheritDoc} */
    public DbTransaction beginTransaction(long timeout) {
	return new JdbcTransaction(this, timeout);
    }

    /** {@inheritDoc} */
    public DbDatabase openDatabase(
	DbTransaction txn, String fileName, boolean create)
	throws FileNotFoundException
    {
	try {
	    return new JdbcDatabase(getConnection(), fileName, create);
	} catch (SQLException e) {
	    throw convertException(e);
	}
    }

    /** {@inheritDoc} */
    public void close() {
	try {
	    synchronized (connections) {
		for (Connection connection : connections) {
		    connection.close();
		}
	    }
	} catch (SQLException e) {
	    throw convertException(e);
	}
    }
}
