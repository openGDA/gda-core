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

public class ScanCommand extends AbstractExtendedCommand{

	
	private static final String ERR_REP = "Use '%s([%s, %s, %s, %s, %s])' instead of '%s %s %s %s %s %s'.";
	private static final String RES_REP = "%s([%s, %s, %s, %s, %s])";

	public ScanCommand() {
		super("([a-z]{0,2}scan) (\\w*) ("+AbstractExtendedCommand.NUMBER+") ("+AbstractExtendedCommand.NUMBER+") ("+AbstractExtendedCommand.NUMBER+")( \\w+)*");
	}

	@Override
	public String getCorrectionMessage() {
		
		String message = null;
		
		String ec = currentMatcher.group(1);
		if (ec==null) ec = "scan";
		
		String x = currentMatcher.group(2);
		if (x==null) x = "x";
		
		String arg1 = currentMatcher.group(3);
		if (arg1==null) arg1 = "arg1";
		
		String arg2 = currentMatcher.group(4);
		if (arg2==null) arg2 = "arg2";

		String arg3 = currentMatcher.group(5);
		if (arg3==null) arg3 = "arg3";
		
		String y = currentMatcher.group(6);
		if (y==null) y = "y";
		try {
			message = String.format(ERR_REP, ec.trim(), x.trim(), arg1.trim(), arg2.trim(),arg3.trim(), y.trim(),
				                                      ec.trim(), x.trim(), arg1.trim(), arg2.trim(),arg3.trim(), y.trim());
		} catch (Exception ne) {
			message = String.format(ERR_REP, "scan", "x", "1.0", "10.0", "1", y, "scan", "x", "1.0", "10.0", "1", "y");
		}
		return message;
	}
	

	@Override
	public String getResolution() {
		
		String ec = currentMatcher.group(1);
		if (ec==null) return null;
		
		String x = currentMatcher.group(2);
		if (x==null) return null;
		
		String arg1 = currentMatcher.group(3);
		if (arg1==null)return null;
		
		String arg2 = currentMatcher.group(4);
		if (arg2==null) return null;

		String arg3 = currentMatcher.group(5);
		if (arg3==null) return null;
		
		String y = currentMatcher.group(6);
		if (y==null) return null;

		
		try {
			return String.format(RES_REP, ec.trim(), x.trim(), arg1.trim(), arg2.trim(),arg3.trim(), y.trim());
		} catch (Exception ne) {
			return null;
		}
	}

}
