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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory
 * @model kind="package"
 * @generated
 */
public interface LDEExperimentsPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "ldeexperiment";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.opengda.org/LDEExperiments";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "lde";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LDEExperimentsPackage eINSTANCE = org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl <em>Experiment Definition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getExperimentDefinition()
	 * @generated
	 */
	int EXPERIMENT_DEFINITION = 0;

	/**
	 * The feature id for the '<em><b>Experiments</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION__EXPERIMENTS = 0;

	/**
	 * The number of structural features of the '<em>Experiment Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION_FEATURE_COUNT = 1;

	/**
	 * The operation id for the '<em>Get Experiment</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION___GET_EXPERIMENT__STRING = 0;

	/**
	 * The number of operations of the '<em>Experiment Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION_OPERATION_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentImpl <em>Experiment</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.ExperimentImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getExperiment()
	 * @generated
	 */
	int EXPERIMENT = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Stages</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT__STAGES = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT__DESCRIPTION = 2;

	/**
	 * The number of structural features of the '<em>Experiment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_FEATURE_COUNT = 3;

	/**
	 * The operation id for the '<em>Get Stage By ID</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT___GET_STAGE_BY_ID__STRING = 0;

	/**
	 * The number of operations of the '<em>Experiment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_OPERATION_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl <em>Stage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.StageImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getStage()
	 * @generated
	 */
	int STAGE = 2;

	/**
	 * The feature id for the '<em><b>Stage ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__STAGE_ID = 0;

	/**
	 * The feature id for the '<em><b>Experiment</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__EXPERIMENT = 1;

	/**
	 * The feature id for the '<em><b>Cells</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__CELLS = 2;

	/**
	 * The feature id for the '<em><b>Detector x</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__DETECTOR_X = 3;

	/**
	 * The feature id for the '<em><b>Detector y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__DETECTOR_Y = 4;

	/**
	 * The feature id for the '<em><b>Detector z</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__DETECTOR_Z = 5;

	/**
	 * The feature id for the '<em><b>Camera x</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__CAMERA_X = 6;

	/**
	 * The feature id for the '<em><b>Camera y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__CAMERA_Y = 7;

	/**
	 * The feature id for the '<em><b>Camera z</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE__CAMERA_Z = 8;

	/**
	 * The number of structural features of the '<em>Stage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_FEATURE_COUNT = 9;

	/**
	 * The operation id for the '<em>Get Cell By ID</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE___GET_CELL_BY_ID__STRING = 0;

	/**
	 * The number of operations of the '<em>Stage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_OPERATION_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl <em>Cell</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.CellImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getCell()
	 * @generated
	 */
	int CELL = 3;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__STAGE = 0;

	/**
	 * The feature id for the '<em><b>Samples</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__SAMPLES = 1;

	/**
	 * The feature id for the '<em><b>Cell ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__CELL_ID = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__NAME = 3;

	/**
	 * The feature id for the '<em><b>Visit ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__VISIT_ID = 4;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__EMAIL = 5;

	/**
	 * The feature id for the '<em><b>Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__START_DATE = 6;

	/**
	 * The feature id for the '<em><b>End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__END_DATE = 7;

	/**
	 * The feature id for the '<em><b>Enable Auto Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__ENABLE_AUTO_EMAIL = 8;

	/**
	 * The feature id for the '<em><b>Calibrant</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__CALIBRANT = 9;

	/**
	 * The feature id for the '<em><b>Calibrant x</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__CALIBRANT_X = 10;

	/**
	 * The feature id for the '<em><b>Calibrant y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__CALIBRANT_Y = 11;

	/**
	 * The feature id for the '<em><b>Calibrant exposure</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__CALIBRANT_EXPOSURE = 12;

	/**
	 * The feature id for the '<em><b>Env Sampling Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__ENV_SAMPLING_INTERVAL = 13;

	/**
	 * The feature id for the '<em><b>Evn Scannable Names</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL__EVN_SCANNABLE_NAMES = 14;

	/**
	 * The number of structural features of the '<em>Cell</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL_FEATURE_COUNT = 15;

	/**
	 * The operation id for the '<em>Get Sample By Id</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL___GET_SAMPLE_BY_ID__STRING = 0;

	/**
	 * The operation id for the '<em>Get Sample By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL___GET_SAMPLE_BY_NAME__STRING = 1;

	/**
	 * The number of operations of the '<em>Cell</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CELL_OPERATION_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl <em>Sample</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.SampleImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getSample()
	 * @generated
	 */
	int SAMPLE = 4;

	/**
	 * The feature id for the '<em><b>Cell</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CELL = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__STATUS = 1;

	/**
	 * The feature id for the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__ACTIVE = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__NAME = 3;

	/**
	 * The feature id for the '<em><b>Sample ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_ID = 4;

	/**
	 * The feature id for the '<em><b>Sample xstart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_XSTART = 5;

	/**
	 * The feature id for the '<em><b>Sample xstop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_XSTOP = 6;

	/**
	 * The feature id for the '<em><b>Sample xstep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_XSTEP = 7;

	/**
	 * The feature id for the '<em><b>Sample ystart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_YSTART = 8;

	/**
	 * The feature id for the '<em><b>Sample ystop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_YSTOP = 9;

	/**
	 * The feature id for the '<em><b>Sample ystep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_YSTEP = 10;

	/**
	 * The feature id for the '<em><b>Sample exposure</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_EXPOSURE = 11;

	/**
	 * The feature id for the '<em><b>Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__COMMAND = 12;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__COMMENT = 13;

	/**
	 * The feature id for the '<em><b>Calibration File Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CALIBRATION_FILE_PATH = 14;

	/**
	 * The feature id for the '<em><b>Data File Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__DATA_FILE_PATH = 15;

	/**
	 * The number of structural features of the '<em>Sample</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_FEATURE_COUNT = 16;

	/**
	 * The number of operations of the '<em>Sample</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.STATUS <em>STATUS</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getSTATUS()
	 * @generated
	 */
	int STATUS = 5;


	/**
	 * The meta object id for the '<em>Stage ID String</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.lang.String
	 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getStageIDString()
	 * @generated
	 */
	int STAGE_ID_STRING = 6;


	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition <em>Experiment Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Experiment Definition</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.ExperimentDefinition
	 * @generated
	 */
	EClass getExperimentDefinition();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getExperiments <em>Experiments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Experiments</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getExperiments()
	 * @see #getExperimentDefinition()
	 * @generated
	 */
	EReference getExperimentDefinition_Experiments();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getExperiment(java.lang.String) <em>Get Experiment</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Experiment</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getExperiment(java.lang.String)
	 * @generated
	 */
	EOperation getExperimentDefinition__GetExperiment__String();

	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.Experiment <em>Experiment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Experiment</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Experiment
	 * @generated
	 */
	EClass getExperiment();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Experiment#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Experiment#getName()
	 * @see #getExperiment()
	 * @generated
	 */
	EAttribute getExperiment_Name();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.lde.model.ldeexperiment.Experiment#getStages <em>Stages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Stages</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Experiment#getStages()
	 * @see #getExperiment()
	 * @generated
	 */
	EReference getExperiment_Stages();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Experiment#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Experiment#getDescription()
	 * @see #getExperiment()
	 * @generated
	 */
	EAttribute getExperiment_Description();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.Experiment#getStageByID(java.lang.String) <em>Get Stage By ID</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Stage By ID</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.Experiment#getStageByID(java.lang.String)
	 * @generated
	 */
	EOperation getExperiment__GetStageByID__String();

	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.Stage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stage</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage
	 * @generated
	 */
	EClass getStage();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getStageID <em>Stage ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getStageID()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_StageID();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.lde.model.ldeexperiment.Stage#getCells <em>Cells</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Cells</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getCells()
	 * @see #getStage()
	 * @generated
	 */
	EReference getStage_Cells();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_x <em>Detector x</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Detector x</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getDetector_x()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_Detector_x();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_y <em>Detector y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Detector y</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getDetector_y()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_Detector_y();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getDetector_z <em>Detector z</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Detector z</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getDetector_z()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_Detector_z();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_x <em>Camera x</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Camera x</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getCamera_x()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_Camera_x();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_y <em>Camera y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Camera y</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getCamera_y()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_Camera_y();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Stage#getCamera_z <em>Camera z</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Camera z</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getCamera_z()
	 * @see #getStage()
	 * @generated
	 */
	EAttribute getStage_Camera_z();

	/**
	 * Returns the meta object for the container reference '{@link org.opengda.lde.model.ldeexperiment.Stage#getExperiment <em>Experiment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Experiment</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getExperiment()
	 * @see #getStage()
	 * @generated
	 */
	EReference getStage_Experiment();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.Stage#getCellByID(java.lang.String) <em>Get Cell By ID</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Cell By ID</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.Stage#getCellByID(java.lang.String)
	 * @generated
	 */
	EOperation getStage__GetCellByID__String();

	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.Cell <em>Cell</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Cell</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell
	 * @generated
	 */
	EClass getCell();

	/**
	 * Returns the meta object for the container reference '{@link org.opengda.lde.model.ldeexperiment.Cell#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Stage</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getStage()
	 * @see #getCell()
	 * @generated
	 */
	EReference getCell_Stage();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.lde.model.ldeexperiment.Cell#getSamples <em>Samples</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Samples</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getSamples()
	 * @see #getCell()
	 * @generated
	 */
	EReference getCell_Samples();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getCellID <em>Cell ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Cell ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getCellID()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_CellID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getName()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getVisitID <em>Visit ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visit ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getVisitID()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_VisitID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getEmail <em>Email</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Email</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getEmail()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Email();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getStartDate <em>Start Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start Date</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getStartDate()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_StartDate();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getEndDate <em>End Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>End Date</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getEndDate()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_EndDate();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#isEnableAutoEmail <em>Enable Auto Email</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enable Auto Email</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#isEnableAutoEmail()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_EnableAutoEmail();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant <em>Calibrant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getCalibrant()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Calibrant();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_x <em>Calibrant x</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant x</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_x()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Calibrant_x();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_y <em>Calibrant y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant y</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_y()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Calibrant_y();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_exposure <em>Calibrant exposure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant exposure</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getCalibrant_exposure()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_Calibrant_exposure();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getEnvSamplingInterval <em>Env Sampling Interval</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Env Sampling Interval</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getEnvSamplingInterval()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_EnvSamplingInterval();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Cell#getEvnScannableNames <em>Evn Scannable Names</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Evn Scannable Names</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getEvnScannableNames()
	 * @see #getCell()
	 * @generated
	 */
	EAttribute getCell_EvnScannableNames();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.Cell#getSampleById(java.lang.String) <em>Get Sample By Id</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Sample By Id</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getSampleById(java.lang.String)
	 * @generated
	 */
	EOperation getCell__GetSampleById__String();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.Cell#getSampleByName(java.lang.String) <em>Get Sample By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Sample By Name</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.Cell#getSampleByName(java.lang.String)
	 * @generated
	 */
	EOperation getCell__GetSampleByName__String();

	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.Sample <em>Sample</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sample</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample
	 * @generated
	 */
	EClass getSample();

	/**
	 * Returns the meta object for the container reference '{@link org.opengda.lde.model.ldeexperiment.Sample#getCell <em>Cell</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Cell</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCell()
	 * @see #getSample()
	 * @generated
	 */
	EReference getSample_Cell();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSampleID()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_SampleID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getStatus()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Status();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Active</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#isActive()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Active();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getName()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_start <em>Sample xstart</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample xstart</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_x_start()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_x_start();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_stop <em>Sample xstop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample xstop</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_x_stop()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_x_stop();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_step <em>Sample xstep</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample xstep</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_x_step()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_x_step();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_start <em>Sample ystart</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample ystart</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_y_start()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_y_start();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_stop <em>Sample ystop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample ystop</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_y_stop()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_y_stop();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_step <em>Sample ystep</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample ystep</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_y_step()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_y_step();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample exposure</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_exposure();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCommand()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Command();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comment</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getComment()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Comment();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrationFilePath <em>Calibration File Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibration File Path</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCalibrationFilePath()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_CalibrationFilePath();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data File Path</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_DataFilePath();

	/**
	 * Returns the meta object for enum '{@link org.opengda.lde.model.ldeexperiment.STATUS <em>STATUS</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>STATUS</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @generated
	 */
	EEnum getSTATUS();

	/**
	 * Returns the meta object for data type '{@link java.lang.String <em>Stage ID String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Stage ID String</em>'.
	 * @see java.lang.String
	 * @model instanceClass="java.lang.String"
	 *        extendedMetaData="enumeration='LS1 LS2 MS1 MS2 MS3 MS4 SS1 SS2 SS3 SS4 SS5 SS6 ROBOT'"
	 * @generated
	 */
	EDataType getStageIDString();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	LDEExperimentsFactory getLDEExperimentsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl <em>Experiment Definition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getExperimentDefinition()
		 * @generated
		 */
		EClass EXPERIMENT_DEFINITION = eINSTANCE.getExperimentDefinition();

		/**
		 * The meta object literal for the '<em><b>Experiments</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXPERIMENT_DEFINITION__EXPERIMENTS = eINSTANCE.getExperimentDefinition_Experiments();

		/**
		 * The meta object literal for the '<em><b>Get Experiment</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EXPERIMENT_DEFINITION___GET_EXPERIMENT__STRING = eINSTANCE.getExperimentDefinition__GetExperiment__String();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentImpl <em>Experiment</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.ExperimentImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getExperiment()
		 * @generated
		 */
		EClass EXPERIMENT = eINSTANCE.getExperiment();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXPERIMENT__NAME = eINSTANCE.getExperiment_Name();

		/**
		 * The meta object literal for the '<em><b>Stages</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXPERIMENT__STAGES = eINSTANCE.getExperiment_Stages();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXPERIMENT__DESCRIPTION = eINSTANCE.getExperiment_Description();

		/**
		 * The meta object literal for the '<em><b>Get Stage By ID</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EXPERIMENT___GET_STAGE_BY_ID__STRING = eINSTANCE.getExperiment__GetStageByID__String();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl <em>Stage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.StageImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getStage()
		 * @generated
		 */
		EClass STAGE = eINSTANCE.getStage();

		/**
		 * The meta object literal for the '<em><b>Stage ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__STAGE_ID = eINSTANCE.getStage_StageID();

		/**
		 * The meta object literal for the '<em><b>Cells</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STAGE__CELLS = eINSTANCE.getStage_Cells();

		/**
		 * The meta object literal for the '<em><b>Detector x</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__DETECTOR_X = eINSTANCE.getStage_Detector_x();

		/**
		 * The meta object literal for the '<em><b>Detector y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__DETECTOR_Y = eINSTANCE.getStage_Detector_y();

		/**
		 * The meta object literal for the '<em><b>Detector z</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__DETECTOR_Z = eINSTANCE.getStage_Detector_z();

		/**
		 * The meta object literal for the '<em><b>Camera x</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__CAMERA_X = eINSTANCE.getStage_Camera_x();

		/**
		 * The meta object literal for the '<em><b>Camera y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__CAMERA_Y = eINSTANCE.getStage_Camera_y();

		/**
		 * The meta object literal for the '<em><b>Camera z</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE__CAMERA_Z = eINSTANCE.getStage_Camera_z();

		/**
		 * The meta object literal for the '<em><b>Experiment</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STAGE__EXPERIMENT = eINSTANCE.getStage_Experiment();

		/**
		 * The meta object literal for the '<em><b>Get Cell By ID</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STAGE___GET_CELL_BY_ID__STRING = eINSTANCE.getStage__GetCellByID__String();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl <em>Cell</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.CellImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getCell()
		 * @generated
		 */
		EClass CELL = eINSTANCE.getCell();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CELL__STAGE = eINSTANCE.getCell_Stage();

		/**
		 * The meta object literal for the '<em><b>Samples</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CELL__SAMPLES = eINSTANCE.getCell_Samples();

		/**
		 * The meta object literal for the '<em><b>Cell ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__CELL_ID = eINSTANCE.getCell_CellID();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__NAME = eINSTANCE.getCell_Name();

		/**
		 * The meta object literal for the '<em><b>Visit ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__VISIT_ID = eINSTANCE.getCell_VisitID();

		/**
		 * The meta object literal for the '<em><b>Email</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__EMAIL = eINSTANCE.getCell_Email();

		/**
		 * The meta object literal for the '<em><b>Start Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__START_DATE = eINSTANCE.getCell_StartDate();

		/**
		 * The meta object literal for the '<em><b>End Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__END_DATE = eINSTANCE.getCell_EndDate();

		/**
		 * The meta object literal for the '<em><b>Enable Auto Email</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__ENABLE_AUTO_EMAIL = eINSTANCE.getCell_EnableAutoEmail();

		/**
		 * The meta object literal for the '<em><b>Calibrant</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__CALIBRANT = eINSTANCE.getCell_Calibrant();

		/**
		 * The meta object literal for the '<em><b>Calibrant x</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__CALIBRANT_X = eINSTANCE.getCell_Calibrant_x();

		/**
		 * The meta object literal for the '<em><b>Calibrant y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__CALIBRANT_Y = eINSTANCE.getCell_Calibrant_y();

		/**
		 * The meta object literal for the '<em><b>Calibrant exposure</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__CALIBRANT_EXPOSURE = eINSTANCE.getCell_Calibrant_exposure();

		/**
		 * The meta object literal for the '<em><b>Env Sampling Interval</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__ENV_SAMPLING_INTERVAL = eINSTANCE.getCell_EnvSamplingInterval();

		/**
		 * The meta object literal for the '<em><b>Evn Scannable Names</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CELL__EVN_SCANNABLE_NAMES = eINSTANCE.getCell_EvnScannableNames();

		/**
		 * The meta object literal for the '<em><b>Get Sample By Id</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation CELL___GET_SAMPLE_BY_ID__STRING = eINSTANCE.getCell__GetSampleById__String();

		/**
		 * The meta object literal for the '<em><b>Get Sample By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation CELL___GET_SAMPLE_BY_NAME__STRING = eINSTANCE.getCell__GetSampleByName__String();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl <em>Sample</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.SampleImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getSample()
		 * @generated
		 */
		EClass SAMPLE = eINSTANCE.getSample();

		/**
		 * The meta object literal for the '<em><b>Cell</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SAMPLE__CELL = eINSTANCE.getSample_Cell();

		/**
		 * The meta object literal for the '<em><b>Sample ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_ID = eINSTANCE.getSample_SampleID();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__STATUS = eINSTANCE.getSample_Status();

		/**
		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__ACTIVE = eINSTANCE.getSample_Active();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__NAME = eINSTANCE.getSample_Name();

		/**
		 * The meta object literal for the '<em><b>Sample xstart</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_XSTART = eINSTANCE.getSample_Sample_x_start();

		/**
		 * The meta object literal for the '<em><b>Sample xstop</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_XSTOP = eINSTANCE.getSample_Sample_x_stop();

		/**
		 * The meta object literal for the '<em><b>Sample xstep</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_XSTEP = eINSTANCE.getSample_Sample_x_step();

		/**
		 * The meta object literal for the '<em><b>Sample ystart</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_YSTART = eINSTANCE.getSample_Sample_y_start();

		/**
		 * The meta object literal for the '<em><b>Sample ystop</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_YSTOP = eINSTANCE.getSample_Sample_y_stop();

		/**
		 * The meta object literal for the '<em><b>Sample ystep</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_YSTEP = eINSTANCE.getSample_Sample_y_step();

		/**
		 * The meta object literal for the '<em><b>Sample exposure</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_EXPOSURE = eINSTANCE.getSample_Sample_exposure();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__COMMAND = eINSTANCE.getSample_Command();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__COMMENT = eINSTANCE.getSample_Comment();

		/**
		 * The meta object literal for the '<em><b>Calibration File Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__CALIBRATION_FILE_PATH = eINSTANCE.getSample_CalibrationFilePath();

		/**
		 * The meta object literal for the '<em><b>Data File Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__DATA_FILE_PATH = eINSTANCE.getSample_DataFilePath();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.STATUS <em>STATUS</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.STATUS
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getSTATUS()
		 * @generated
		 */
		EEnum STATUS = eINSTANCE.getSTATUS();

		/**
		 * The meta object literal for the '<em>Stage ID String</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.lang.String
		 * @see org.opengda.lde.model.ldeexperiment.impl.LDEExperimentsPackageImpl#getStageIDString()
		 * @generated
		 */
		EDataType STAGE_ID_STRING = eINSTANCE.getStageIDString();

	}

} //LDEExperimentsPackage
