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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getStageID <em>Stage ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getExperiment <em>Experiment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getCells <em>Cells</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_x <em>Detector x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_y <em>Detector y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_z <em>Detector z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_x <em>Camera x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_y <em>Camera y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_z <em>Camera z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Stage#getNumberOfCells <em>Number Of Cells</em>}</li>
 * </ul>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='ValidStageID'"
 * @generated
 */
public interface Stage extends EObject {
	/**
	 * Returns the value of the '<em><b>Stage ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stage ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stage ID</em>' attribute.
	 * @see #setStageID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_StageID()
	 * @model dataType="org.opengda.lde.model.ldeexperiment.StageIDString"
	 * @generated
	 */
	String getStageID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getStageID <em>Stage ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage ID</em>' attribute.
	 * @see #getStageID()
	 * @generated
	 */
	void setStageID(String value);

	/**
	 * Returns the value of the '<em><b>Cells</b></em>' containment reference list.
	 * The list contents are of type {@link org.opengda.lde.model.ldeexperiment.Cell}.
	 * It is bidirectional and its opposite is '{@link org.opengda.lde.model.ldeexperiment.Cell#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cells</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cells</em>' containment reference list.
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Cells()
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getStage
	 * @model opposite="stage" containment="true"
	 * @generated
	 */
	EList<Cell> getCells();

	/**
	 * Returns the value of the '<em><b>Detector x</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector x</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * 
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Detector x</em>' attribute.
	 * @see #setDetector_x(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Detector_x()
	 * @model default="0.0"
	 * @generated
	 */
	double getDetector_x();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_x <em>Detector x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector x</em>' attribute.
	 * @see #getDetector_x()
	 * @generated
	 */
	void setDetector_x(double value);

	/**
	 * Returns the value of the '<em><b>Detector y</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector y</em>' attribute.
	 * @see #setDetector_y(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Detector_y()
	 * @model default="0.0"
	 * @generated
	 */
	double getDetector_y();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_y <em>Detector y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector y</em>' attribute.
	 * @see #getDetector_y()
	 * @generated
	 */
	void setDetector_y(double value);

	/**
	 * Returns the value of the '<em><b>Detector z</b></em>' attribute.
	 * The default value is <code>"400.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector z</em>' attribute.
	 * @see #setDetector_z(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Detector_z()
	 * @model default="400.0"
	 * @generated
	 */
	double getDetector_z();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_z <em>Detector z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector z</em>' attribute.
	 * @see #getDetector_z()
	 * @generated
	 */
	void setDetector_z(double value);

	/**
	 * Returns the value of the '<em><b>Camera x</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Camera x</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Camera x</em>' attribute.
	 * @see #setCamera_x(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Camera_x()
	 * @model
	 * @generated
	 */
	double getCamera_x();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_x <em>Camera x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Camera x</em>' attribute.
	 * @see #getCamera_x()
	 * @generated
	 */
	void setCamera_x(double value);

	/**
	 * Returns the value of the '<em><b>Camera y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Camera y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Camera y</em>' attribute.
	 * @see #setCamera_y(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Camera_y()
	 * @model
	 * @generated
	 */
	double getCamera_y();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_y <em>Camera y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Camera y</em>' attribute.
	 * @see #getCamera_y()
	 * @generated
	 */
	void setCamera_y(double value);

	/**
	 * Returns the value of the '<em><b>Camera z</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Camera z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Camera z</em>' attribute.
	 * @see #setCamera_z(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Camera_z()
	 * @model
	 * @generated
	 */
	double getCamera_z();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_z <em>Camera z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Camera z</em>' attribute.
	 * @see #getCamera_z()
	 * @generated
	 */
	void setCamera_z(double value);

	/**
	 * Returns the value of the '<em><b>Number Of Cells</b></em>' attribute.
	 * The default value is <code>"3"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number Of Cells</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number Of Cells</em>' attribute.
	 * @see #setNumberOfCells(int)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_NumberOfCells()
	 * @model default="3"
	 * @generated
	 */
	int getNumberOfCells();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getNumberOfCells <em>Number Of Cells</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number Of Cells</em>' attribute.
	 * @see #getNumberOfCells()
	 * @generated
	 */
	void setNumberOfCells(int value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Cell getCellByID(String cellId);

	/**
	 * Returns the value of the '<em><b>Experiment</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.opengda.lde.model.ldeexperiment.Experiment#getStages <em>Stages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Experiment</em>' container reference.
	 * @see #setExperiment(Experiment)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getStage_Experiment()
	 * @see org.opengda.lde.model.ldeexperiment.Experiment#getStages
	 * @model opposite="stages" required="true" transient="false"
	 * @generated
	 */
	Experiment getExperiment();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Stage#getExperiment <em>Experiment</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Experiment</em>' container reference.
	 * @see #getExperiment()
	 * @generated
	 */
	void setExperiment(Experiment value);

} // Stage
