/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.jython.logger;

import java.io.UnsupportedEncodingException;

import gda.jython.Terminal;

class TerminalAdapter implements Terminal{

	final LineLogger logger;
	
	public TerminalAdapter(LineLogger logger) {
		this.logger = logger;
	}

	@Override
	public void write(byte[] data) {
		// Copied from JythonTerminal.java
		String msg = "encoding error!!!";
		try {
			msg = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		write(msg);
	}

	@Override
	public void write(String output) {
		logger.log(output);
	}

	@Override
	public void update(Object source, Object arg) {
		// pass
	}

}