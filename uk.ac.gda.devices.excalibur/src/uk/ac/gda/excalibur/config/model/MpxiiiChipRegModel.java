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
 * Interface that corresponds to $excalibur_ioc/excaliburApp/Db/mpxiiChipReg.template
 * 
 * @model
 */
public interface MpxiiiChipRegModel extends EObject {
	/**
	 * @model type="AnperModel" containment="true"
	 */

	AnperModel getAnper();

	/**
	 * 
	 */
	public void setAnper(AnperModel anper);

	/**
	 * @model type="PixelModel" containment="true"
	 */

	PixelModel getPixel();

	/**
	 * 
	 */
	public void setPixel(PixelModel pixel);

	/**
	 * Returns the value of the '<em><b>Chip Disable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Chip Disable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Chip Disable</em>' attribute.
	 * @see #isSetChipDisable()
	 * @see #unsetChipDisable()
	 * @see #setChipDisable(boolean)
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage#getMpxiiiChipRegModel_ChipDisable()
	 * @model unsettable="true"
	 * @generated
	 */
	boolean isChipDisable();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#isChipDisable <em>Chip Disable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Chip Disable</em>' attribute.
	 * @see #isSetChipDisable()
	 * @see #unsetChipDisable()
	 * @see #isChipDisable()
	 * @generated
	 */
	void setChipDisable(boolean value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#isChipDisable <em>Chip Disable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetChipDisable()
	 * @see #isChipDisable()
	 * @see #setChipDisable(boolean)
	 * @generated
	 */
	void unsetChipDisable();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#isChipDisable <em>Chip Disable</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Chip Disable</em>' attribute is set.
	 * @see #unsetChipDisable()
	 * @see #isChipDisable()
	 * @see #setChipDisable(boolean)
	 * @generated
	 */
	boolean isSetChipDisable();

	/**
	 * @model 
	 */
	double getDacIntoMpx();
	
	void setDacIntoMpx(double dacIntoMPX);

	/**
	 * @model 
	 */
	double getDacOutFromMpx();
	
	void setDacOutFromMpx(double dacOutFromMPX);
	
}
