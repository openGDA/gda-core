/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.jython.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import nvt4j.impl.Terminal;

public class SocketServerWithTelnetNegotiationThread extends SocketServerThreadBase {

	public SocketServerWithTelnetNegotiationThread(Socket socket) throws IOException {

		super(socket);

		// Using this Terminal subclass prevents the screen being cleared and
		// the cursor being moved to the top left.
		final Terminal terminal = new BetterTerminal(socket);

		// Also turn wrapping and the cursor on (these get disabled by the Terminal class).
		terminal.put(Terminal.AUTO_WRAP_ON);
		terminal.setCursor(true);

		final InputStream newInputStream = new InputStream() {
			@Override
			public int read() throws IOException {
				return terminal.get();
			}
		};

		final OutputStream newOutputStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				terminal.put(b);
			}
		};

		setOutputStream(newOutputStream);
		setInputStream(newInputStream);
	}

	/**
	 * When a {@link Terminal} is created, its {@code init()} method clears
	 * the screen and moves the cursor to the top left. This subclass prevents
	 * that happening by making the first calls to {@link #clear()} and
	 * {@link #move(int, int)} do nothing.
	 */
	static class BetterTerminal extends Terminal {

		public BetterTerminal(Socket socket) throws IOException {
			super(socket);
		}

		boolean firstClearHappened;
		boolean firstMoveHappened;

		@Override
		public void clear() throws IOException {
			if (firstClearHappened) {
				super.clear();
			} else {
				firstClearHappened = true;
			}
		}

		@Override
		public void move(int row, int column) throws IOException {
			if (firstMoveHappened) {
				super.move(row, column);
			} else {
				firstMoveHappened = true;
			}
		}
	}

}
