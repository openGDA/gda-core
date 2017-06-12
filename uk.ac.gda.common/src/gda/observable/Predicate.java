/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.observable;

/**
 * An interface implemented by objects that perform an (if statement like) test on an object.
 * <p>
 * Note that there are library implementations of Predicate that we could choose:
 * <UL>
 * <li>http://commons.apache.org/collections/apidocs/org/apache/commons/collections/Predicate.html (Not generic)
 * <li>http://guava-libraries.googlecode.com/svn-history/r13/trunk/javadoc/com/google/common/base/Predicate.html (Not
 * part of gda)
 * </UL>
 *
 * @param <T> The type of object tested by an implementation
 */
@FunctionalInterface
// TODO This should be replaced by java.util.function.Predicate<T> in Java 8
public interface Predicate<T> {

	/**
	 * Apply the implementations test to an object.
	 *
	 * @param object
	 * @return true if the Object meets some requirement
	 */
	boolean apply(T object);

}
