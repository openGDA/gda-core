/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.lang.reflect.Method;

import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Contains utility methods for working with Eclipse views.
 */
public class ViewUtils {

	private static final Logger logger = LoggerFactory.getLogger(ViewUtils.class);
	
	/**
	 * Sets a view's name (using reflection).
	 */
	public static void setViewName(ViewPart view, String name) {
		try {
			Method setPartNameMethod = ViewPart.class.getDeclaredMethod("setPartName", String.class);
			setPartNameMethod.setAccessible(true);
			setPartNameMethod.invoke(view, name);
		} catch (Exception e) {
			logger.error("Unable to set view name to " + StringUtils.quote(name), e);
		}
	}
	
}
