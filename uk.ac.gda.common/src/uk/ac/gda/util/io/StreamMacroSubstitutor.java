/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.util.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


/**
 * This parses an XML file (and applies string interpolation (a.k.a. variable substitution. Every
 * XML field in the XML document is examined during processing. Fields which contain a pattern beginning with "${",
 * followed by a property key name and terminating with a "}", will trigger string interpolation.
 */
public class StreamMacroSubstitutor {

	/**
	 * @param in  input xml containing macros of the form ${macroname} to be substituted
	 * @param out the source with macros found in macroSupplier change to the suppled values
	 * @param macroSupplier - supplier of macros for substition
	 * @throws IOException
	 */
	static public void process(Reader in, Writer out, MacroSupplier macroSupplier) throws IOException{
		(new StreamMacroSubstitutor()).doProcessing(in, out, macroSupplier);
		
	}
	/**
	 * Reads byte from in and write to out until start pattern found.
	 * 
	 * @return true if next header found
	 * @throws IOException
	 */
	private boolean processToNextHeader(Reader in, Writer out) throws IOException {
		// two characters read in - so can read ahead to detect "${" pair
		int i1 = in.read();
		int i2 = in.read();

		char c1 = (char) i1;
		char c2 = (char) i2;

		while (!(c1 == '$' && c2 == '{') && (i1 != -1 && i2 != -1)) {
			out.write(c1);

			i1 = i2;
			c1 = c2;

			i2 = in.read();
			c2 = (char) i2;

			if (i2 == -1) {
				// flush previous character read to output
				if (i1 != -1) {
					out.write(c1);
				}

				return false;
			}
		}

		return true;
	}

	/**
	 * Reads in text into a string until a terminator character found, thus forming a property key to be substituted
	 * with its value.
	 * 
	 * @return property key name if found. null if not found.
	 * @throws IOException
	 */
	private String readPropertyKeyUntilTerminator(Reader in) throws IOException {
		String key = "";

		int i = in.read();
		char c = (char) i;

		if (i == -1) {
			return null;
		}

		while (c != '}') {
			key += c;

			i = in.read();
			c = (char) i;

			if (i == -1) {
				return null;
			}
		}

		return key;
	}

	/**
	 * Scans through XML instance file looking for Property keys inside ${} patterns. Each instance should contain a
	 * reference to a named java property (e.g. "${gda.src.java}"). If any found, look for a matching java property of
	 * same name and replace the pattern instance with the value of that property using string interpolation. N.B.
	 * Should handle multiple pattern instances per property value, but doesnt handle recursive interpolation yet (i.e.
	 * a pattern containing a property with a value containing further patterns).
	 * 
	 * @throws IOException
	 */
	private void doProcessing(Reader in, Writer out, MacroSupplier macroSupplier) throws IOException {
		boolean done = false;

		while (done == false) {
			// scan for pattern start in buffer
			boolean rval = processToNextHeader(in, out);

			if (rval == false) {
				return;
			}

			String propertyName = readPropertyKeyUntilTerminator(in);

			if (propertyName == null) {
				return;
			}

			String propertyValue = macroSupplier.get(propertyName);

			if (propertyValue == null) {
				propertyValue = "${" + propertyName + "}";
			}

			out.write(propertyValue.toCharArray());
		}
	}
}
