package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EObject;

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


/**
 * Interface that corresponds to $excalibur_ioc/excaliburApp/Db/mpxiiiGlobalReg.template
 * 
 * @model
 */
public interface MpxiiiGlobalRegModel extends EObject {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * @model
	 */
	int getColourMode();

	/**
	 * @model
	 */
	String getColourModeAsString();

	/**
	 * 
	 */
	public void setColourMode(int index);

	/**
	 * @model
	 */
	String[] getColourModeLabels();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourModeLabels <em>Colour Mode Labels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Colour Mode Labels</em>' attribute.
	 * @see #getColourModeLabels()
	 * @generated
	 */
	void setColourModeLabels(String[] value);

	/**
	 * @model
	 */
	double getDacNumber();

	/**
	 * 
	 */
	public void setDacNumber(double dacNumber);

	/**
	 * @model
	 */
	double getDacNameCalc1();

	/**
	 * 
	 */
	public void setDacNameCalc1(double dacNameCalc1);

	/**
	 * @model
	 */
	double getDacNameCalc2();

	/**
	 * 
	 */
	public void setDacNameCalc2(double dacNameCalc2);

	/**
	 * @model
	 */
	double getDacNameCalc3();

	/**
	 * 
	 */
	public void setDacNameCalc3(double dacNameCalc3);

	/**
	 * @model
	 */
	int getDacNameSel1();

	/**
	 * 
	 */
	public void setDacNameSel1(int dacNameSel1);

	/**
	 * @model
	 */
	int getDacNameSel2();

	/**
	 * 
	 */
	public void setDacNameSel2(int dacNameSel2);

	/**
	 * @model
	 */
	int getDacNameSel3();

	/**
	 * 
	 */
	public void setDacNameSel3(int dacNameSel3);

	/**
	 * @model
	 */
	String getDacName();

	/**
	 * 
	 */
	public void setDacName(String dacName);

	/**
	 * 
	 */
	public void setColourModeAsString(String colourMode);

	/**
	 * @model
	 */
	String[] getCounterDepthLabels();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepthLabels <em>Counter Depth Labels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Counter Depth Labels</em>' attribute.
	 * @see #getCounterDepthLabels()
	 * @generated
	 */
	void setCounterDepthLabels(String[] value);

	/**
	 * @model
	 */
	int getCounterDepth();

	/**
	 * @model
	 */
	String getCounterDepthAsString();

	/**
	 * 
	 */
	public void setCounterDepth(int counterDepth);

	/**
	 * 
	 */
	public void setCounterDepthAsString(String counterDepth);

}
