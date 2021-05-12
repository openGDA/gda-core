/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.jython;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.python.core.PyObject;
import org.python.util.InteractiveInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Context manager for running PyTest in Jython</h1>
 * PyTest assumes {@code sys.stdout} is an output stream. This runner can be used to wrap the
 * current {@code stdout} as an OutputStream (instead of a Writer) so that Jython wraps is as a
 * {@code PyFile} instead of a {@code PyFileWriter} which does not have the required methods.
 * The original writer is restored when the context exits.
 */
public class PyTestRunner {
	private static final Logger logger = LoggerFactory.getLogger(PyTestRunner.class);
	private JythonServer server;

	public PyTestRunner(JythonServer server) {
		this.server = server;
	}

	public void __enter__() { // NOSONAR Python naming conventions
		logger.debug("Wrapping stdout for PyTest");
		server.getInterp().setOut(new PyTestOut(server.getTerminalWriter()));
	}

	@SuppressWarnings("unused") // signature has to match Python method
	public void __exit__(PyObject exc_type, PyObject exc_value, PyObject traceback) { // NOSONAR python naming conventions
		logger.debug("Restoring original stdout");
		server.getInterp().setOut(server.getTerminalWriter());
	}

	/**
	 * Wrap the terminal output as an OutputStream
	 * <br>
	 * {@link InteractiveInterpreter#setOut} creates different internal wrappers for
	 * {@link Writer}s (such as the JythonServer terminal writer) and OutputStreams.
	 * Pytest uses methods that rely on the OutputStream implementation so this
	 * wrapper allows the standard output to be used.
	 */
	private final class PyTestOut extends OutputStream {
		private Writer terminalWriter;
		public PyTestOut(Writer terminalWriter) {
			this.terminalWriter = terminalWriter;
		}
		@Override
		public void write(int b) throws IOException {
			terminalWriter.append((char)b);
		}
		@Override
		public void write(byte[] b) throws IOException {
			terminalWriter.append(new String(b));
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			terminalWriter.append(new String(b, off, len));
		}
	}
}
