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

package gda.jython.translator;

import static gda.jython.translator.Type.NL;

import java.util.Deque;
import java.util.LinkedList;

public class CommandReader {
	private final CommandTokenizer tokens;

	private Token next;

	public CommandReader(String command) {
		tokens = new CommandTokenizer(command);
	}

	public Deque<Token> nextStatement() {
		LinkedList<Token> buffer = new LinkedList<>();
		if (next == null) {
			next = tokens.nextToken();
		}
		while (next != null) {
			buffer.add(next);
			if (next.type == NL) {
				next = null;
				break;
			}
			next = tokens.nextToken();
		}
		return buffer;
	}
}
