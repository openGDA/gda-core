/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

/**
 * A marker class to indicate the implementing class has internally documented Java properties. The method to output
 * these has to be static to avoid any requirement to actually instantiate the classes. This prevents them being listed
 * in the interface! To implement the interface a method of the following signature should be implemented. public static
 * Vector getProperties() An example implementation would be as follows private static Property[] properties = { new
 * Property("gda.params", "Root for directory to store camera status data file.\n" + "If specified will save in var
 * directory under this."), }; public static Vector getProperties() { return Property.toVector(properties); }
 */

public interface UsesProperties {
}