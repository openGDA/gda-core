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
 * @model
 */
public interface ReadoutNodeFemModel extends EObject {

	/**
	 * @model
	 */

	public int getCounterDepth();

	/**
	 * 
	 */

	public void setCounterDepth(int counterDepth);

	/**
	 * Returns the value of the '<em><b>Operation Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Operation Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Operation Mode</em>' attribute.
	 * @see #setOperationMode(int)
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage#getReadoutNodeFemModel_OperationMode()
	 * @model
	 * @generated
	 */
	int getOperationMode();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getOperationMode <em>Operation Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Operation Mode</em>' attribute.
	 * @see #getOperationMode()
	 * @generated
	 */
	void setOperationMode(int value);

	/**
	 * Returns the value of the '<em><b>Counter Select</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Counter Select</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Counter Select</em>' attribute.
	 * @see #setCounterSelect(int)
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage#getReadoutNodeFemModel_CounterSelect()
	 * @model
	 * @generated
	 */
	int getCounterSelect();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getCounterSelect <em>Counter Select</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Counter Select</em>' attribute.
	 * @see #getCounterSelect()
	 * @generated
	 */
	void setCounterSelect(int value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg1();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg1 <em>Mpxiii Chip Reg1</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg1</em>' containment reference.
	 * @see #getMpxiiiChipReg1()
	 * @generated
	 */
	void setMpxiiiChipReg1(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg2();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg2 <em>Mpxiii Chip Reg2</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg2</em>' containment reference.
	 * @see #getMpxiiiChipReg2()
	 * @generated
	 */
	void setMpxiiiChipReg2(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg3();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg3 <em>Mpxiii Chip Reg3</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg3</em>' containment reference.
	 * @see #getMpxiiiChipReg3()
	 * @generated
	 */
	void setMpxiiiChipReg3(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg4();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg4 <em>Mpxiii Chip Reg4</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg4</em>' containment reference.
	 * @see #getMpxiiiChipReg4()
	 * @generated
	 */
	void setMpxiiiChipReg4(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg5();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg5 <em>Mpxiii Chip Reg5</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg5</em>' containment reference.
	 * @see #getMpxiiiChipReg5()
	 * @generated
	 */
	void setMpxiiiChipReg5(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg6();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg6 <em>Mpxiii Chip Reg6</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg6</em>' containment reference.
	 * @see #getMpxiiiChipReg6()
	 * @generated
	 */
	void setMpxiiiChipReg6(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg7();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg7 <em>Mpxiii Chip Reg7</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg7</em>' containment reference.
	 * @see #getMpxiiiChipReg7()
	 * @generated
	 */
	void setMpxiiiChipReg7(MpxiiiChipRegModel value);

	/**
	 * @model type="MpxiiiChipRegModel" containment="true"
	 */

	MpxiiiChipRegModel getMpxiiiChipReg8();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg8 <em>Mpxiii Chip Reg8</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mpxiii Chip Reg8</em>' containment reference.
	 * @see #getMpxiiiChipReg8()
	 * @generated
	 */
	void setMpxiiiChipReg8(MpxiiiChipRegModel value);

	/**
	 * Returns the value of the '<em><b>Dac Sense</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dac Sense</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dac Sense</em>' attribute.
	 * @see #setDacSense(int)
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage#getReadoutNodeFemModel_DacSense()
	 * @model
	 * @generated
	 */
	int getDacSense();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getDacSense <em>Dac Sense</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dac Sense</em>' attribute.
	 * @see #getDacSense()
	 * @generated
	 */
	void setDacSense(int value);

	/**
	 * Returns the value of the '<em><b>Dac External</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dac External</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dac External</em>' attribute.
	 * @see #setDacExternal(int)
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage#getReadoutNodeFemModel_DacExternal()
	 * @model
	 * @generated
	 */
	int getDacExternal();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getDacExternal <em>Dac External</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dac External</em>' attribute.
	 * @see #getDacExternal()
	 * @generated
	 */
	void setDacExternal(int value);

}
