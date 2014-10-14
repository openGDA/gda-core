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

package uk.ac.gda.exafs.ui.describers;


import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.richbeans.xml.XMLBeanContentDescriber;

/**
 *
 */
public class XspressDescriber extends XMLBeanContentDescriber {
	
	@Override
	protected String getBeanName() {
		return XspressParameters.class.getName();
	}
	@Override
	public String getEditorId() {
		return "uk.ac.gda.exafs.ui.detector.xspress.XspressParametersEditor";
	}

}
