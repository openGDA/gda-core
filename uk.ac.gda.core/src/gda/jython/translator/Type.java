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

/** The type of syntax token - whether it is a symbol, a word, whitespace etc */
public enum Type {
	/** Whitespace - new line (within parentheses) space, tab, line continuation etc */
	WS,
	/** Any word - function name, variable, builtin, field etc */
	WORD,
	/** Operator - +,-,*,/,= etc */
	OP,
	/** A comma */
	COMMA,
	/** A bracket - ([{}]) */
	BRACKET,
	/** A newline - or a semi-colon, anything that divides statements */
	NL,
	/** A string of any kind - single, double, raw, etc */
	STRING,
	/** Comment - From a # to the end of the line */
	COMMENT;
	public Token token(String text) {
		return new Token(this, text);
	}
	public Token token(int ch) {
		return new Token(this, Character.toString(ch));
	}
	public boolean matches(Token t) {
		return t != null && t.type == this;
	}
	public boolean matchesOrNull(Token t) {
		return t == null || t.type == this;
	}
}