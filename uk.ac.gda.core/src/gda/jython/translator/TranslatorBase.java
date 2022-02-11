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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all classes using the Translator interface. This supplies the functionality for the translate method.
 */
public abstract class TranslatorBase extends AliasingBase implements Translator {

	private static final Pattern NOT_A_NEW_LINE_PATTERN = Pattern.compile("([^\n;]+)");
	private static final Pattern NEW_LINE_SEMI_COLON_PATTERN = Pattern.compile("\\n;");
	private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\R");

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
	 * @param command The command to be translated
	 * @return The GDA syntax command translated to Jython
	 */
	@Override
	public String translate(String command) {
		return stream(NEW_LINE_PATTERN.split(command, -1))
				.map(this::translateLine)
				.collect(joining("\n"));
	}

	private String translateLine(String original_command) {
		// take a copy. This will be returned in case of any errors.
		String full_command = original_command;
		try {
			// Use regex to identify parts of the string between line breaks. These parts will be translated
			// individually and then replaced into the command string.
			// TODO Not sure multi-line strings can actually get here so this might be redundant
			Matcher m = NOT_A_NEW_LINE_PATTERN.matcher(full_command);

			int endOfPreviousGroup = 0;
			StringBuilder newCommandBuilder = new StringBuilder();
			boolean ignoreRestOfLine=false;
			while (m.find() && !ignoreRestOfLine) {
				String thisGroup = m.group(1);
				int startOfGroup = m.start();
				int endOfGroup = m.end();

				// translate thisGroup
				ignoreRestOfLine = ignoreRestOfLine(thisGroup);
				thisGroup = translateGroup(thisGroup);
				if( ignoreRestOfLine){
					newCommandBuilder.append(thisGroup);
					endOfPreviousGroup = full_command.length();
				} else {
					// rebuild
					if (startOfGroup == endOfPreviousGroup) {
						newCommandBuilder.append(thisGroup);
					} else {
						newCommandBuilder.append(full_command.substring(endOfPreviousGroup, startOfGroup));
						newCommandBuilder.append(thisGroup);
					}
					endOfPreviousGroup = endOfGroup;
				}
			}
			if (endOfPreviousGroup != full_command.length()) {
				newCommandBuilder.append(full_command.substring(endOfPreviousGroup));
			}

			String newCommand = newCommandBuilder.toString();

			// The translate group methods may have added extra lines, rather than making a simple translation of a
			// single line. So the \n and ;'s may get confused. Tidy up any problem here. Make sure that there are no
			// ;'s at the beginning of a line
			Matcher slashNSemiColonMatchToNewCommand = NEW_LINE_SEMI_COLON_PATTERN
					.matcher(newCommand);
			newCommand = slashNSemiColonMatchToNewCommand.replaceAll("\n");

			// remove all multi ;'s
			newCommand = newCommand.replaceAll(";+", ";");
			return newCommand;
		} catch (Exception e) {
			return original_command;
		}
	}

	/**
	 * Called from within the translate method. This assumes that the group is a single
	 * jython command - no line breaks or ;'s.
	 *
	 * @param group Original command - Single line with no ';' splitting commands
	 * @return String Translated command
	 */
	protected abstract String translateGroup(String group);
	protected abstract boolean ignoreRestOfLine(String line);
}
