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


import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.richbeans.xml.XMLBeanContentDescriber;

/**
 * @author fcp94556
 *
 */
public class DetectorDescriber extends XMLBeanContentDescriber {

	/**
	 * 
	 */
	public final static String ID = "uk.ac.gda.exafs.ui.DetectorParametersEditor";
	
	@Override
	protected String getBeanName() {
		return DetectorParameters.class.getName();
	}
	@Override
	protected String getEditorId() {
		return ID;
	}

}

	