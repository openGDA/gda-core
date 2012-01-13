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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractExtendedCommand implements ExtendedCommand {

	public static final String NUMBER = "-?\\d+\\.?\\d*";
	
	protected final Pattern regularExpression;

	public AbstractExtendedCommand(String regularExpression) {
		this.regularExpression = Pattern.compile(regularExpression);
	}

	protected Matcher currentMatcher;
	
	@Override
	public boolean matches(String line) {
		currentMatcher = regularExpression.matcher(line.trim());
		return currentMatcher.matches();
	}

}
