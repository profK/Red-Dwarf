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

import com.sun.sgs.impl.service.data.store.db.DbTransaction;
import java.sql.Connection;
import java.sql.SQLException;

class JdbcTransaction implements DbTransaction {

    private final JdbcEnvironment env;

    /** The JDBC connection. */
    private Connection connection;

    private final int timeout;

    JdbcTransaction(JdbcEnvironment env, long timeout) {
	if (timeout <= 0) {
	    throw new IllegalArgumentException(
		"Timeout must be greater than 0");
	}
	this.env = env;
	this.timeout = (int) (timeout / 1000);
	try {
	    connection = env.getConnection();
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** Converts the argument to a JDBC connection. */
    static Connection getJdbcConnection(DbTransaction dbTxn) {
	if (dbTxn instanceof JdbcTransaction) {
	    return ((JdbcTransaction) dbTxn).connection;
	} else {
	    throw new IllegalArgumentException(
		"Transaction must be an instance of JdbcTransaction");
	}
    }

    /* -- Implement DbTransaction -- */

    /** {@inheritDoc} */
    public void prepare(byte[] gid) {
	// TBD: Not supported yet
    }

    /** {@inheritDoc} */
    public void commit() {
	try {
	    connection.commit();
	    env.returnConnection(connection);
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }

    /** {@inheritDoc} */
    public void abort() {
	try {
	    connection.rollback();
	} catch (SQLException e) {
	    throw JdbcEnvironment.convertException(e);
	}
    }
}
