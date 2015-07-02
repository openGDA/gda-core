/*******************************************************************************
 * Copyright © 2009, 2015 Diamond Light Source Ltd
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

import java.util.Date;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Cell</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getStage <em>Stage</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getSamples <em>Samples</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getEndDate <em>End Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#isEnableAutoEmail <em>Enable Auto Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant <em>Calibrant</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_x <em>Calibrant x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_y <em>Calibrant y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_exposure <em>Calibrant exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getEnvSamplingInterval <em>Env Sampling Interval</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Cell#getEvnScannableNames <em>Evn Scannable Names</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell()
 * @model
 * @generated
 */
public interface Cell extends EObject {
	/**
	 * Returns the value of the '<em><b>Samples</b></em>' containment reference list.
	 * The list contents are of type {@link org.opengda.lde.model.ldeexperiment.Sample}.
	 * It is bidirectional and its opposite is '{@link org.opengda.lde.model.ldeexperiment.Sample#getCell <em>Cell</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Samples</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Samples</em>' containment reference list.
	 * @see #isSetSamples()
	 * @see #unsetSamples()
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Samples()
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCell
	 * @model opposite="cell" containment="true" unsettable="true"
	 * @generated
	 */
	EList<Sample> getSamples();

	/**
	 * Unsets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getSamples <em>Samples</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSamples()
	 * @see #getSamples()
	 * @generated
	 */
	void unsetSamples();

	/**
	 * Returns whether the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getSamples <em>Samples</em>}' containment reference list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Samples</em>' containment reference list is set.
	 * @see #unsetSamples()
	 * @see #getSamples()
	 * @generated
	 */
	boolean isSetSamples();

	/**
	 * Returns the value of the '<em><b>Cell ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cell ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cell ID</em>' attribute.
	 * @see #setCellID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_CellID()
	 * @model
	 * @generated
	 */
	String getCellID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getCellID <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cell ID</em>' attribute.
	 * @see #getCellID()
	 * @generated
	 */
	void setCellID(String value);

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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Visit ID</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visit ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visit ID</em>' attribute.
	 * @see #setVisitID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_VisitID()
	 * @model default=""
	 * @generated
	 */
	String getVisitID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getVisitID <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visit ID</em>' attribute.
	 * @see #getVisitID()
	 * @generated
	 */
	void setVisitID(String value);

	/**
	 * Returns the value of the '<em><b>Email</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Email</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email</em>' attribute.
	 * @see #setEmail(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Email()
	 * @model default="" unique="false"
	 * @generated
	 */
	String getEmail();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getEmail <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Email</em>' attribute.
	 * @see #getEmail()
	 * @generated
	 */
	void setEmail(String value);

	/**
	 * Returns the value of the '<em><b>Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Start Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Date</em>' attribute.
	 * @see #setStartDate(Date)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_StartDate()
	 * @model
	 * @generated
	 */
	Date getStartDate();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getStartDate <em>Start Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Date</em>' attribute.
	 * @see #getStartDate()
	 * @generated
	 */
	void setStartDate(Date value);

	/**
	 * Returns the value of the '<em><b>End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End Date</em>' attribute.
	 * @see #setEndDate(Date)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_EndDate()
	 * @model
	 * @generated
	 */
	Date getEndDate();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getEndDate <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End Date</em>' attribute.
	 * @see #getEndDate()
	 * @generated
	 */
	void setEndDate(Date value);

	/**
	 * Returns the value of the '<em><b>Enable Auto Email</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Enable Auto Email</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Enable Auto Email</em>' attribute.
	 * @see #setEnableAutoEmail(boolean)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_EnableAutoEmail()
	 * @model default="false"
	 * @generated
	 */
	boolean isEnableAutoEmail();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#isEnableAutoEmail <em>Enable Auto Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enable Auto Email</em>' attribute.
	 * @see #isEnableAutoEmail()
	 * @generated
	 */
	void setEnableAutoEmail(boolean value);

	/**
	 * Returns the value of the '<em><b>Calibrant</b></em>' attribute.
	 * The default value is <code>"Si(NIST SRM 640c)"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant</em>' attribute.
	 * @see #setCalibrant(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Calibrant()
	 * @model default="Si(NIST SRM 640c)"
	 * @generated
	 */
	String getCalibrant();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant <em>Calibrant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant</em>' attribute.
	 * @see #getCalibrant()
	 * @generated
	 */
	void setCalibrant(String value);

	/**
	 * Returns the value of the '<em><b>Calibrant x</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant x</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant x</em>' attribute.
	 * @see #setCalibrant_x(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Calibrant_x()
	 * @model default="0"
	 * @generated
	 */
	double getCalibrant_x();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_x <em>Calibrant x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant x</em>' attribute.
	 * @see #getCalibrant_x()
	 * @generated
	 */
	void setCalibrant_x(double value);

	/**
	 * Returns the value of the '<em><b>Calibrant y</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant y</em>' attribute.
	 * @see #setCalibrant_y(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Calibrant_y()
	 * @model default="0"
	 * @generated
	 */
	double getCalibrant_y();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_y <em>Calibrant y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant y</em>' attribute.
	 * @see #getCalibrant_y()
	 * @generated
	 */
	void setCalibrant_y(double value);

	/**
	 * Returns the value of the '<em><b>Calibrant exposure</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant exposure</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant exposure</em>' attribute.
	 * @see #setCalibrant_exposure(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Calibrant_exposure()
	 * @model default="1.0"
	 * @generated
	 */
	double getCalibrant_exposure();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_exposure <em>Calibrant exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant exposure</em>' attribute.
	 * @see #getCalibrant_exposure()
	 * @generated
	 */
	void setCalibrant_exposure(double value);

	/**
	 * Returns the value of the '<em><b>Env Sampling Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Env Sampling Interval</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Env Sampling Interval</em>' attribute.
	 * @see #setEnvSamplingInterval(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_EnvSamplingInterval()
	 * @model
	 * @generated
	 */
	double getEnvSamplingInterval();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getEnvSamplingInterval <em>Env Sampling Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Env Sampling Interval</em>' attribute.
	 * @see #getEnvSamplingInterval()
	 * @generated
	 */
	void setEnvSamplingInterval(double value);

	/**
	 * Returns the value of the '<em><b>Evn Scannable Names</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Evn Scannable Names</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Evn Scannable Names</em>' attribute.
	 * @see #setEvnScannableNames(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_EvnScannableNames()
	 * @model
	 * @generated
	 */
	String getEvnScannableNames();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getEvnScannableNames <em>Evn Scannable Names</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Evn Scannable Names</em>' attribute.
	 * @see #getEvnScannableNames()
	 * @generated
	 */
	void setEvnScannableNames(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Sample getSampleById(String sampleId);

	/**
	 * Returns the value of the '<em><b>Stage</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.opengda.lde.model.ldeexperiment.Stage#getCells <em>Cells</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stage</em>' container reference.
	 * @see #setStage(Stage)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getCell_Stage()
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getCells
	 * @model opposite="cells" required="true" transient="false"
	 * @generated
	 */
	Stage getStage();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Cell#getStage <em>Stage</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage</em>' container reference.
	 * @see #getStage()
	 * @generated
	 */
	void setStage(Stage value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Sample getSampleByName(String sampleName);

} // Cell
