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
 * A representation of the model object '<em><b>Experiment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Experiment#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Experiment#getStage <em>Stage</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Experiment#getDescription <em>Description</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Experiment#getNumberOfStages <em>Number Of Stages</em>}</li>
 * </ul>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperiment()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='NonNegativeQuantity MaximumQuantity'"
 * @generated
 */
public interface Experiment extends EObject {
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperiment_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Experiment#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Stage</b></em>' containment reference list.
	 * The list contents are of type {@link org.opengda.lde.model.ldeexperiment.Stage}.
	 * It is bidirectional and its opposite is '{@link org.opengda.lde.model.ldeexperiment.Stage#getExperiment <em>Experiment</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stage</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stage</em>' containment reference list.
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperiment_Stage()
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getExperiment
	 * @model opposite="experiment" containment="true"
	 * @generated
	 */
	EList<Stage> getStage();

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperiment_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Experiment#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Number Of Stages</b></em>' attribute.
	 * The default value is <code>"13"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number Of Stages</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number Of Stages</em>' attribute.
	 * @see #setNumberOfStages(int)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperiment_NumberOfStages()
	 * @model default="13"
	 * @generated
	 */
	int getNumberOfStages();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Experiment#getNumberOfStages <em>Number Of Stages</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number Of Stages</em>' attribute.
	 * @see #getNumberOfStages()
	 * @generated
	 */
	void setNumberOfStages(int value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Stage getStageByID(String stageId);

} // Experiment
