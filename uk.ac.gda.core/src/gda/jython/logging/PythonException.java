/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.jython.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyTraceback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A subclass of RuntimeException to wrap Python Exceptions in a way that can be used by the slf4j
 * logging framework. The stacktrace is amended to reflect the Python traceback and and internal Jython
 * code is filtered out.
 * <p>
 * Instances should not be created directly but via the {@link #from(PyException)} static method as wrapped Java
 * exceptions can be used as they are. This provides a single way of handling both Python and Java exceptions.
 * <p>
 * Also note that the from methods are not guaranteed to return an instance of this class, only that the returned
 * Exception is usable.
 *
 * @see PyException
 * @since 9.8
 */
public class PythonException extends RuntimeException {
	private static final Logger logger = LoggerFactory.getLogger(PythonException.class);

	/** A constant marker used for the method name of Jython frames to mark them as non Java */
	private static final String JYTHON_STACK_MARKER = "(script)";
	/** A constant {@link StackTraceElement} to replace a section that has been removed */
	private static final StackTraceElement MODIFICATION_MARKER = new StackTraceElement("...Jython stack frames removed for clarity..", "", "PythonException.java", 0);

	/**
	 * Create a new Java Exception to represent a Jython exception in a way that can be handled by
	 * the main logging system.
	 *
	 * @param py The PyException thrown by Jython code
	 */
	private PythonException(PyException py) {
		super(Py.formatException(py.type, py.value));
		setStackTrace(extractPythonStack(py).toArray(new StackTraceElement[] {}));
	}

	/**
	 * Convert a {@link PyException} into one that can be used by the slf4j logging system.
	 * <p>
	 * If the PyException is wrapping a Java exception, append the Jython traceback to it and
	 * return the Java exception. If it is a Jython builtin Exception (or extension of one) wrap
	 * in a {@link PythonException} with a fixed stacktrace.
	 * <p>
	 * <code>null</code> is returned if <code>null</code> is passed in.
	 *
	 * @param py The {@link PyException} to wrap
	 * @return An exception usable by the Java side logging framework.
	 */
	public static Throwable from(PyException py) {
		if (py == null) {
			return null;
		}
		Object value = py.value.__tojava__(Throwable.class);
		if (value == Py.NoConversion || value == null) {
			return new PythonException(py);
		}
		return from((Throwable) value, py);
	}

	/**
	 * Wrap a builtin Python exception in a PyException
	 *
	 * @param exc builtin Python exception (TypeError, ValueError etc).
	 * @return A {@link PyException} wrapping this exception.
	 */
	public static Throwable from(PyObject exc) {
		return from(new PyException(exc.getType(), exc));
	}

	/**
	 * Filter internal Jython code from a Java Throwable's stacktrace
	 *
	 * @param exc
	 * @return The same instance of {@link Throwable} but with a doctored stacktrace to remove internal
	 *     Jython frames
	 */
	public static Throwable from(Throwable exc) {
		return from(exc, null);
	}

	/**
	 * Merge the traceback of a PyException with the stacktrace of a Java throwable
	 * @param exc The Java throwable
	 * @param py The PyException with the Python side traceback
	 * @return The <em>same</em> Java throwable but with an amended stacktrace
	 *
	 * @see Throwable#getStackTrace()
	 */
	private static Throwable from(Throwable exc, PyException py) {
		logger.trace("Filtering stacktrace. Original exception", exc);
		filterStackTrace(exc, extractPythonStack(py));
		return exc;
	}

	/**
	 * Remove internal Jython frames from stacktrace of exceptions
	 * <p>
	 * As calls cross from Jython to Java and back, a lot of generated code is run that is of no interest when
	 * debugging the cause of exceptions. This filters the internal frames from the stack trace and also appends a
	 * given list of {@link StackTraceElement}s (to include python code stack as well).
	 * <p>
	 * Be aware that this modifies the given exception and does not return a copy
	 *
	 * @param throwable The throwable to modify the stacktrace of
	 * @param additionalStack A list of {@link StackTraceElement}s to append to throwables
	 */
	private static void filterStackTrace(Throwable throwable, List<StackTraceElement> additionalStack) {
		List<StackTraceElement> stack = new ArrayList<>();
		for (StackTraceElement ste: throwable.getStackTrace()) {
			if (ste.isNativeMethod()) {
				// If this is native assume it's most likely to be from Jython internal code
				stack.add(MODIFICATION_MARKER);
				break;
			}
			stack.add(ste);
		}
		// If this exception is passed through this method multiple times, we don't want the python trace
		// appended multiple times so remove any Jython frames before adding them again.
		stack.removeIf(ste -> JYTHON_STACK_MARKER.equals(ste.getMethodName()));
		stack.addAll(additionalStack);
		throwable.setStackTrace(stack.toArray(new StackTraceElement[] {}));
		Throwable cause = throwable.getCause();
		if (cause != null) {
			filterStackTrace(cause, additionalStack);
		}
	}

	/**
	 * Extract the traceback of a {@link PyException} as a {@link List} of {@link StackTraceElement}s
	 * that can be used in Java Exceptions.
	 * <p>
	 * Returns an empty list is PyException is <code>null</code>.
	 *
	 * @param py The PyException to take the traceback from
	 * @return List of stacktrace elements
	 */
	private static List<StackTraceElement> extractPythonStack(PyException py) {
		List<StackTraceElement> stack = new ArrayList<>();
		if (py == null) return stack;
		for (PyTraceback tb = py.traceback; tb != null; tb = (PyTraceback) tb.tb_next) {
			// Construct StackTraceElement from Python traceback frame
			// It doesn't quite map directly but close enough to make trace useful
			stack.add(new StackTraceElement(
					tb.tb_frame.f_code.co_filename, // path of script instead of qualified class name
					JYTHON_STACK_MARKER, // Marker string to mark STE as jython instead of method name
					tb.tb_frame.f_code.co_name, // Method name instead of filename
					tb.tb_lineno)); // Line number (as intended)
		}
		// Python traceback is in reverse order to Java stack trace so reverse elements here.
		Collections.reverse(stack);
		return stack;
	}

}