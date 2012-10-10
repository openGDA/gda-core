/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all classes using the Translator interface. This supplies the functionality for the translate method.
 */
public abstract class TranslatorBase implements Translator {
	
	protected Vector<String> aliases = new Vector<String>();

	protected Vector<String> vararg_aliases = new Vector<String>();
	/**
	 * The public function to perform the translation. This translates a complete instruction for the interpreter,
	 * including multi-line or multi-command strings.
	 * <P>
	 * i.e. the command must be in one of the following formats:
	 * <P>
	 * 1. print "hello"
	 * <P>
	 * 2. for i in range(5):\n\tprint "hello"
	 * <P>
	 * 3. print "hello"; print "world"
	 * <P>
	 * New lines in multi-line instructions must begin with one or more tab character's (each \t character is considered
	 * a level of indentation) and end with a \n. Spaces at the start of instuctions or after ;'s will be ignored.
	 * <P>
	 * The full string will be split first by ;'s, then by \n's. Each sub section will be translated individually, then
	 * the translated sub-sections are recombined with the \n's or \t's replaced.
	 * 
	 * @param original_command
	 *            String
	 * @return String
	 */
	@Override
	public String translate(String original_command) {
		// take a copy. This will be returned in case of any errors.
		String full_command = original_command;
		try {
			// use regex to identify parts of the string between
			// tabs and line breaks. These parts will be translated
			// individually and then replaced into the command string.
			Matcher m = Pattern.compile("([^\n;]+)").matcher(full_command.subSequence(0, full_command.length()));
			// for ticket #1675 a \t was removed from the above RE

			int endOfPreviousGroup = 0;
			String newCommand = new String();
			boolean ignoreRestOfLine=false;
			while (m.find() && !ignoreRestOfLine) {
				String thisGroup = m.group(1);
				int startOfGroup = m.start();
				int endOfGroup = m.end();

				// translate thisGroup
				ignoreRestOfLine = ignoreRestOfLine(thisGroup);
				thisGroup = translateGroup(thisGroup);
				if( ignoreRestOfLine){
					newCommand += thisGroup;
					endOfPreviousGroup = full_command.length();
				} else {
					// rebuild
					if (startOfGroup == endOfPreviousGroup) {
						newCommand += thisGroup;
					} else {
						newCommand += full_command.substring(endOfPreviousGroup, startOfGroup) + thisGroup;
					}
					endOfPreviousGroup = endOfGroup;
				}
			}
			if (endOfPreviousGroup != full_command.length()) {
				newCommand += full_command.substring(endOfPreviousGroup);
			}

			// the translate group methods may have added extra lines,
			// rather than
			// making a simple translation of a single line. So the \n and
			// ;'s may
			// get confused. Tidy up any problem here.

			// make sure that there are no ;'s at the beginning of a line
			Pattern slashNSemiColon = Pattern.compile("\\n;");
			Matcher slashNSemiColonMatchToNewCommand = slashNSemiColon.matcher(newCommand.subSequence(0, newCommand
					.length()));
			newCommand = slashNSemiColonMatchToNewCommand.replaceAll("\n");

			// remove all multi ;'s
			newCommand = newCommand.replaceAll(";+", ";");
			return newCommand;
		} catch (Exception e) {
			return original_command;
		}
	}


	@Override
	public Vector<String> getAliasedCommands(){
		return aliases;
	}
	@Override
	public Vector<String> getAliasedVarargCommands(){
		return vararg_aliases;
	}
}
