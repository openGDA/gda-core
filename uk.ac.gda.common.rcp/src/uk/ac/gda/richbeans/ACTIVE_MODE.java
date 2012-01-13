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

package uk.ac.gda.richbeans;

/**
 * This enum is used to mark IFieldWidget objects in terms of their behaviour when setActive(...) is called. Most
 * widgets default to SET_VISIBLE_AND_ACTIVE type and are not changeable. Those that are will have an activeMode
 * property in RCP developer.
 */
public enum ACTIVE_MODE {
	/**
	 * 
	 */
	SET_VISIBLE_AND_ACTIVE, 
	/**
	 * 
	 */
	SET_ENABLED_AND_ACTIVE, 
	/**
	 * 
	 */
	ACTIVE_ONLY
}
