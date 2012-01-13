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

package uk.ac.gda.doe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark the methods that return the string list of field names that are
 * marking ranges in the bean.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DOEField {
	/**
	 * returns the weighting value which is used to determine
	 * the loop level that the field should be between 0 and 10.
	 * 0 is the lowest looped property and 10 the hightest i.e.
	 * 
	 *  for (DOEField with value==10) {
	 *      for (DOEField with value==9) {
	 *         ...
	 *            for (DOEField with value==0) {
	 *                do experiment
	 *            }
	 *      }
	 *  }
	 */
	public int value() default DEFAULT_LEVEL;
	
	/**
	 * This should be a subclass of java.lang.Number
	 * The default is Double. This is used where the field was and Integer
	 * being replaced by a string to avoid the value '1.0' in the bean instead
	 * of '1' being recorded in the string after the expand.
	 * @return the class
	 */
	Class<? extends Number> type() default java.lang.Double.class; 

	public static final int DEFAULT_LEVEL = 4;
	public static final int MIN_LEVEL     = 0;
	public static final int MAX_LEVEL     = 10;

}
