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

package uk.ac.gda.jython.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericExtendedCommand {

	/**
	 * Matches any command in a list of them.
	 * @param line
	 * @param commands
	 * @return true if line is a command
	 */
	public static boolean matches(final String line, final List<String> commands) {
		for (String command : commands) {
			if (Pattern.compile(command+" (.*)").matcher(line.trim()).matches()) {
				return true;
			}
		}
		return false;
	}

	public static String getCorrectionMessage(String line, List<String> commands) {
		String message = null;
		for (String command : commands) {
			final Matcher matcher = Pattern.compile("("+command+") (.*)").matcher(line.trim());
			if (matcher.matches()) {
				try {
					final String commaSeparatedArgs = splitSpaces(matcher.group(2).trim());
					message = String.format("The command '%s' should be used with brackets and comma separated arguments in Jython e.g. '%s(%s)'",matcher.group(1).trim(), matcher.group(1).trim(), commaSeparatedArgs);
				} catch (Exception ne) {
					message = "Cammands in scripts should use brackets.";
				}
			}
		}
		return message;
	}

	private static String splitSpaces(String a) {
		final String [] args = a.split(" ");
		final StringBuilder buf = new StringBuilder();
		if (args.length>1) {
			for (int i = 0; i < args.length-1; i++) {
				buf.append(args[i].trim());
				buf.append(",");
				buf.append(" ");
			}
		}
		buf.append(args[args.length-1]);
		return buf.toString();
	}

	public static String getResolution(String line, List<String> commands) {
		String message = null;
		for (String command : commands) {
			final Matcher matcher = Pattern.compile("("+command+") (.*)").matcher(line.trim());
			if (matcher.matches()) {
				try {
					final String commaSeparatedArgs = splitSpaces(matcher.group(2).trim());
					message = String.format("%s(%s)",matcher.group(1).trim(), commaSeparatedArgs);
				} catch (Exception ne) {
					message = null;
				}
			}
		}
		return message;
	}

}
