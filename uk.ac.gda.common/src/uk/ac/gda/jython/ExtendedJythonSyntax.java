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

package uk.ac.gda.jython;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.gda.jython.commands.AliasCommand;
import uk.ac.gda.jython.commands.GenericExtendedCommand;
import uk.ac.gda.jython.commands.PosCommand;
import uk.ac.gda.jython.commands.ScanCommand;

/**
 * Class to deal with commands pos and *scan and any alised commands.
 * 
 * Could do with extending in line with Jython extended systax in GDA.
 */
public class ExtendedJythonSyntax {
	

	
	/**
	 * Extended Jython commands and a pattern detailing their current
	 * syntax. This is a bit of a bodge as you have to change the patterns here
	 * to have the extended commands not show as an error in the builder.
	 */
	public static final List<ExtendedCommand>  COMMANDS;
	static {
		// Groups must be 1=command 2=scannable 3=auxilary information
		final List<ExtendedCommand> tmp1 = new ArrayList<ExtendedCommand>(3);
		
		tmp1.add(new PosCommand());
		tmp1.add(new AliasCommand());
		tmp1.add(new ScanCommand());
		
		COMMANDS = Collections.unmodifiableList(tmp1);
		
	}
	
	/**
	 * 
	 * @param line
	 * @return true if line looks like a Jython command line.
	 */
	public static boolean isCommand(final String line, final List<String> commands) {
		for (ExtendedCommand cmd : COMMANDS) {
			if (cmd.matches(line)) return true;
		}
		if (GenericExtendedCommand.matches(line, commands)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param line
	 * @return a suggestion to fix the syntax
	 */
	public static String getCorrectionMessage(final String line, final List<String> commands) {
		
		String message = null;
		for (ExtendedCommand cmd : COMMANDS) {		
			if (cmd.matches(line)) {
				message = cmd.getCorrectionMessage();
			}
		}
		
		if (GenericExtendedCommand.matches(line, commands)) {
			message = GenericExtendedCommand.getCorrectionMessage(line, commands);
		}
		if (message==null) return null;
		return message+" Please right click and choose quick fix (in Problems view).";
	}

	public static String getResolution(final String line, final List<String> commands) {
		for (ExtendedCommand cmd : COMMANDS) {		
			if (cmd.matches(line)) {
				return cmd.getResolution();
			}
		}
		if (GenericExtendedCommand.matches(line, commands)) {
			return GenericExtendedCommand.getResolution(line, commands);
		}
		return null;
		
	}
	

}
