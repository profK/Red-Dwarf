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

package com.sun.sgs.impl.sharedutil.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Defines a logging {@code Filter} that permits logging exceptions at a
 * different logging level from other logging. <p>
 *
 * This class recognizes the following {@link LogManager} configuration
 * properties:
 *
 * <dl style="margin-left: 1em">
 *
 * <dt> <i>Property:</i> <b>{@value #EXCEPTION_CLASS_PROPERTY}</b> <br>
 *	<i>Default:</i> {@code java.lang.Throwable}
 *
 * <dd style="padding-top: .5em">
 *	Specifies the exception class that should be logged at a different
 *	logging level.  The value should be the fully qualified name of the
 *	exception class.  All exceptions of the specified class or subclasses
 *	of that class will be considered. <p>
 *
 * <dt> <i>Property:</i> <b>{@value #EXCEPTION_LEVEL_PROPERTY}</b> <br>
 *	<i>Default:</i> {@link Level#ALL ALL}
 *
 * <dd style="padding-top: .5em">
 *	Specifies the logging level for logging exceptions of the specified
 *	class. <p>
 *
 * <dt> <i>Property:</i> <b>{@value #BASE_LEVEL_PROPERTY}</b> <br>
 *	<i>Default:</i> {@link Level#INFO INFO}
 *
 * <dd style="padding-top: .5em">
 *	Specifies the logging level for log messages that do not include an
 *	exception and for exceptions that are not instances of the specified
 *	exception class.  If this property is not for logging messages of the
 *	specified class. <p>
 *
 * </dl>
 */
public class ExceptionFilter implements Filter {

    /** The property that specifies the exception class. */
    public static final String EXCEPTION_CLASS_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ExceptionFilter.exception.class";

    /**
     * The property that specifies the logging level for logging exceptions of
     * the specified class.
     */
    public static final String EXCEPTION_LEVEL_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ExceptionFilter.exception.level";

    /** The property that specifies the logging level for all other logging. */
    public static final String BASE_LEVEL_PROPERTY =
	"com.sun.sgs.impl.sharedutil.logging.ExceptionFilter.base.level";

    /** The exception class. */
    private final Class<? extends Throwable> exceptionClass;

    /** The logging level for the specified exception. */
    private final Level exceptionLevel;

    /** The logging level for other messages. */
    private final Level baseLevel;

    /** Creates an instance of this class. */
    public ExceptionFilter() throws ClassNotFoundException {
	LogManager logManager = LogManager.getLogManager();
	String value = logManager.getProperty(EXCEPTION_CLASS_PROPERTY);
	exceptionClass = (value != null)
	    ? Class.forName(value).asSubclass(Throwable.class)
	    : Throwable.class;
	value = logManager.getProperty(EXCEPTION_LEVEL_PROPERTY);
	exceptionLevel = (value != null) ? Level.parse(value) : Level.ALL;
	value = logManager.getProperty(BASE_LEVEL_PROPERTY);
	baseLevel = (value != null) ? Level.parse(value) : Level.INFO;
    }

    /* -- Implement Filter -- */

    /**
     * {@inheritDoc} <p>
     *
     * This implementation returns true if the level of the record is greater
     * than or equal to {@code level}, or if the record contains an exception
     * that is a subclass of {@code exceptionClass}.
     */
    public boolean isLoggable(LogRecord record) {
	Level level = exceptionClass.isInstance(record.getThrown())
	    ? exceptionLevel : baseLevel;
	return record.getLevel().intValue() >= level.intValue();
    }
}
