/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import static java.lang.String.join;

import java.io.Serializable;

public class TerminalInput implements Serializable {
	private static final String NL = "\n";
	private final String input;
	private int index;
	private String user;

	public TerminalInput(String input, String user, int source) {
		this.input = input;
		this.user = user;
		this.index = source;
	}

	public String getInput() {
		return input;
	}

	public String[] lines() {
		return input.split(NL, -1);
	}

	public String format(String ps1, String ps2) {
		String src = "(" + getSource() + ")\t";
		return src + ps1 + join(NL + src + ps2, lines());
	}

	public int getIndex() {
		return index;
	}

	public String getSource() {
		return index == 0 ? "server" : user + "@" + index;
	}

	@Override
	public String toString() {
		return "TerminalInput(from " + getSource() + ") [" + lines()[0] + "]";
	}
}
