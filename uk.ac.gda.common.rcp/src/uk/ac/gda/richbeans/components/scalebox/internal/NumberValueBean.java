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

package uk.ac.gda.richbeans.components.scalebox.internal;

import java.util.Iterator;
import java.util.List;

public abstract class NumberValueBean {

	public abstract Number getValue();
	
	public static String getString(List<? extends NumberValueBean> vals) {
		
		final StringBuilder buf = new StringBuilder();
		for (Iterator<? extends NumberValueBean> iterator = vals.iterator(); iterator.hasNext();) {
			buf.append(iterator.next().getValue());
			if (iterator.hasNext()) buf.append(", ");
		}
		
		return buf.toString();
	}

}
