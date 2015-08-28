/*******************************************************************************
 * Copyright © 2009, 2014 Diamond Light Source Ltd
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
 *
 * Contributors:
 * 	Diamond Light Source Ltd
 *******************************************************************************/
/**
 */
package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sample</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCell <em>Cell</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_start <em>Sample xstart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_stop <em>Sample xstop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_step <em>Sample xstep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_start <em>Sample ystart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_stop <em>Sample ystop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_step <em>Sample ystep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrationFilePath <em>Calibration File Path</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}</li>
 * </ul>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample()
 * @model
 * @generated
 */
public interface Sample extends EObject {
	/**
	 * Returns the value of the '<em><b>Sample ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ID</em>' attribute.
	 * @see #isSetSampleID()
	 * @see #unsetSampleID()
	 * @see #setSampleID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_SampleID()
	 * @model unsettable="true" id="true"
	 * @generated
	 */
	String getSampleID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ID</em>' attribute.
	 * @see #isSetSampleID()
	 * @see #unsetSampleID()
	 * @see #getSampleID()
	 * @generated
	 */
	void setSampleID(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleID()
	 * @see #getSampleID()
	 * @see #setSampleID(String)
	 * @generated
	 */
	void unsetSampleID();

	/**
	 * Returns whether the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample ID</em>' attribute is set.
	 * @see #unsetSampleID()
	 * @see #getSampleID()
	 * @see #setSampleID(String)
	 * @generated
	 */
	boolean isSetSampleID();

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The default value is <code>"READY"</code>.
	 * The literals are from the enumeration {@link org.opengda.lde.model.ldeexperiment.STATUS}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Status</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see #isSetStatus()
	 * @see #unsetStatus()
	 * @see #setStatus(STATUS)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Status()
	 * @model default="READY" unsettable="true" transient="true"
	 * @generated
	 */
	STATUS getStatus();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see #isSetStatus()
	 * @see #unsetStatus()
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(STATUS value);

	/**
	 * Unsets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetStatus()
	 * @see #getStatus()
	 * @see #setStatus(STATUS)
	 * @generated
	 */
	void unsetStatus();

	/**
	 * Returns whether the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Status</em>' attribute is set.
	 * @see #unsetStatus()
	 * @see #getStatus()
	 * @see #setStatus(STATUS)
	 * @generated
	 */
	boolean isSetStatus();

	/**
	 * Returns the value of the '<em><b>Active</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Active</em>' attribute.
	 * @see #setActive(boolean)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Active()
	 * @model default="false"
	 * @generated
	 */
	boolean isActive();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active</em>' attribute.
	 * @see #isActive()
	 * @generated
	 */
	void setActive(boolean value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Sample xstart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample xstart</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample xstart</em>' attribute.
	 * @see #setSample_x_start(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_x_start()
	 * @model
	 * @generated
	 */
	Double getSample_x_start();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_start <em>Sample xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample xstart</em>' attribute.
	 * @see #getSample_x_start()
	 * @generated
	 */
	void setSample_x_start(Double value);

	/**
	 * Returns the value of the '<em><b>Sample xstop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample xstop</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample xstop</em>' attribute.
	 * @see #setSample_x_stop(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_x_stop()
	 * @model
	 * @generated
	 */
	Double getSample_x_stop();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_stop <em>Sample xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample xstop</em>' attribute.
	 * @see #getSample_x_stop()
	 * @generated
	 */
	void setSample_x_stop(Double value);

	/**
	 * Returns the value of the '<em><b>Sample xstep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample xstep</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample xstep</em>' attribute.
	 * @see #setSample_x_step(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_x_step()
	 * @model
	 * @generated
	 */
	Double getSample_x_step();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_step <em>Sample xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample xstep</em>' attribute.
	 * @see #getSample_x_step()
	 * @generated
	 */
	void setSample_x_step(Double value);

	/**
	 * Returns the value of the '<em><b>Sample ystart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ystart</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ystart</em>' attribute.
	 * @see #setSample_y_start(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_y_start()
	 * @model
	 * @generated
	 */
	Double getSample_y_start();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_start <em>Sample ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ystart</em>' attribute.
	 * @see #getSample_y_start()
	 * @generated
	 */
	void setSample_y_start(Double value);

	/**
	 * Returns the value of the '<em><b>Sample ystop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ystop</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ystop</em>' attribute.
	 * @see #setSample_y_stop(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_y_stop()
	 * @model
	 * @generated
	 */
	Double getSample_y_stop();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_stop <em>Sample ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ystop</em>' attribute.
	 * @see #getSample_y_stop()
	 * @generated
	 */
	void setSample_y_stop(Double value);

	/**
	 * Returns the value of the '<em><b>Sample ystep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ystep</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ystep</em>' attribute.
	 * @see #setSample_y_step(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_y_step()
	 * @model
	 * @generated
	 */
	Double getSample_y_step();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_step <em>Sample ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ystep</em>' attribute.
	 * @see #getSample_y_step()
	 * @generated
	 */
	void setSample_y_step(Double value);

	/**
	 * Returns the value of the '<em><b>Sample exposure</b></em>' attribute.
	 * The default value is <code>"5.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample exposure</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample exposure</em>' attribute.
	 * @see #setSample_exposure(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_exposure()
	 * @model default="5.0"
	 * @generated
	 */
	double getSample_exposure();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample exposure</em>' attribute.
	 * @see #getSample_exposure()
	 * @generated
	 */
	void setSample_exposure(double value);

	/**
	 * Returns the value of the '<em><b>Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command</em>' attribute.
	 * @see #setCommand(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Command()
	 * @model
	 * @generated
	 */
	String getCommand();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' attribute.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(String value);

	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * The default value is <code>"comment here"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Comment()
	 * @model default="comment here"
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * Returns the value of the '<em><b>Calibration File Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibration File Path</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibration File Path</em>' attribute.
	 * @see #setCalibrationFilePath(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_CalibrationFilePath()
	 * @model
	 * @generated
	 */
	String getCalibrationFilePath();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrationFilePath <em>Calibration File Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibration File Path</em>' attribute.
	 * @see #getCalibrationFilePath()
	 * @generated
	 */
	void setCalibrationFilePath(String value);

	/**
	 * Returns the value of the '<em><b>Data File Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data File Path</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data File Path</em>' attribute.
	 * @see #setDataFilePath(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_DataFilePath()
	 * @model
	 * @generated
	 */
	String getDataFilePath();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data File Path</em>' attribute.
	 * @see #getDataFilePath()
	 * @generated
	 */
	void setDataFilePath(String value);

	/**
	 * Returns the value of the '<em><b>Cell</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.opengda.lde.model.ldeexperiment.Cell#getSamples <em>Samples</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cell</em>' container reference.
	 * @see #setCell(Cell)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Cell()
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getSamples
	 * @model opposite="samples" required="true" transient="false"
	 * @generated
	 */
	Cell getCell();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCell <em>Cell</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cell</em>' container reference.
	 * @see #getCell()
	 * @generated
	 */
	void setCell(Cell value);

} // Sample
