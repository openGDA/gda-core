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

package gda.analysis;

import gda.analysis.datastructure.DataVector;

import org.python.core.PyObject;

/**
 * PyDataSet Class
 */
public class PyDataSet extends PyObject {
	//
	// Variables and the basic constructor for Multiarray.
	// (These should only be accessed from within the Numeric (java)
	// package.)
	//

	// Class variables.
	// /** Python function that are used to format arrays for str([0]) and
	// repr([1]). */
	// static PyFunction [] str_functions = {null, null};
	/**
	 * 
	 */
	public static int maxLineWidth = 77;

	/**
	 * 
	 */
	public static int precision = 8;

	/**
	 * 
	 */
	public static boolean suppressSmall = false;

	/**
	 * Python class of PyDataSet.
	 * 
	 * @see PyObject
	 */
	//public static PyClass __class__;

	/** The docstring. */
	String docString = "PyDataSet methods:\n" + "  astype(typecode)]\n" + "  itemsize()\n" + "  byteswapped()\n"
			+ "  copy()\n" + "  typecode()\n" + "  iscontiguous()\n" + "  tostring()\n" + "  tolist()\n";

	// Instance variables.

	/** DataVector that holds array data. May be shared between arrays. */
	DataVector data;

}
