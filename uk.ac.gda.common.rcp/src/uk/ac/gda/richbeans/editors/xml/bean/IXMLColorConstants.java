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

package uk.ac.gda.richbeans.editors.xml.bean;

import org.eclipse.swt.graphics.RGB;

/**
 * @author fcp94556
 *
 */
public interface IXMLColorConstants {
	/**
	 * 
	 */
	RGB XML_COMMENT = new RGB(128, 0, 0);
	/**
	 * 
	 */
	RGB PROC_INSTR = new RGB(128, 128, 128);
	/**
	 * 
	 */
	RGB STRING = new RGB(0, 128, 0);
	/**
	 * 
	 */
	RGB DEFAULT = new RGB(0, 0, 0);
	/**
	 * 
	 */
	RGB TAG = new RGB(0, 0, 128);
}
