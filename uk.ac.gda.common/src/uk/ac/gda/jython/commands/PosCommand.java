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

import uk.ac.gda.jython.AbstractExtendedCommand;

public class PosCommand extends AbstractExtendedCommand{

	private static final String ERR_REP = "Instead of '%s %s %s', use '%s(%s)'.";
	private static final String RES_REP = "%s(%s)";
	
	public PosCommand() {
		super("(pos)( \\w+)?( "+AbstractExtendedCommand.NUMBER+")?");
	}

	@Override
	public String getCorrectionMessage() {
		
		String message = null;
		String ec = currentMatcher.group(1);
		if (ec==null) ec = "scan";
		String x = currentMatcher.group(2);
		if (x==null) x = "x";
		String y = currentMatcher.group(3);
		if (y==null) y = "y";
		try {
			message = String.format(ERR_REP, ec.trim(), x.trim(),y.trim(),
					                         x.trim(), y.trim());
		} catch (Exception ne) {
			message = String.format(ERR_REP, "pos", "x", "y", "x", "y");
		}
		return message;
	}


	@Override
	public String getResolution() {
		
		String name = currentMatcher.group(2);
		if (name==null) return null;
		
		String val = currentMatcher.group(3);
		if (val==null) return null;
		
		try {
			return String.format(RES_REP, name.trim(), val.trim());
		} catch (Exception ne) {
			return null;
		}
	}

}
