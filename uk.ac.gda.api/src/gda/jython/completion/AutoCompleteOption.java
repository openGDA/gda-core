/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.jython.completion;

import java.io.Serializable;

/**
 * Text and type pair for a possible text completion. Implements {@link Comparable} to allow sorting into alphabetical
 * order.
 */
public class AutoCompleteOption implements Serializable, Comparable<AutoCompleteOption> {

	/**
	 * The full version of just the section being completed <br>
	 * eg with an object called foobar, the full completion of {@code print(foob} would be {@code foobar} not
	 * {@code print(foobar}
	 */
	public final String text;

	/** The type of object this completion represents eg. type, function, attribute etc.*/
	public final CompletionType type;

	public AutoCompleteOption(String text, int type) {
		this(text, CompletionType.fromInt(type));
	}

	public AutoCompleteOption(String text, CompletionType type) {
		this.text = text;
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", text, type);
	}

	@Override
	public int compareTo(AutoCompleteOption other) {
		int result = this.text.compareToIgnoreCase(other.text);
		if (result != 0) {
			return result; // Was a different string
		}
		// Was the same string e.g a Java class with a public field abc and a method abc()
		// So compare the type enum which will sort by ordinal
		return this.type.compareTo(other.type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AutoCompleteOption other = (AutoCompleteOption) obj;
		if (text == null) {
			if (other.text != null) {
				return false;
			}
		} else if (!text.equals(other.text)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

}
