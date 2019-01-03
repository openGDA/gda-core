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
package org.opengda.lde.model.ldeexperiment.impl;

import java.util.Date;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.model.ldeexperiment.util.LDEExperimentsValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class LDEExperimentsPackageImpl extends EPackageImpl implements LDEExperimentsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass experimentDefinitionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass experimentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass cellEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sampleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum statusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType stageIDStringEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType dateEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType calibrantNameStringEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private LDEExperimentsPackageImpl() {
		super(eNS_URI, LDEExperimentsFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link LDEExperimentsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static LDEExperimentsPackage init() {
		if (isInited) return (LDEExperimentsPackage)EPackage.Registry.INSTANCE.getEPackage(LDEExperimentsPackage.eNS_URI);

		// Obtain or create and register package
		LDEExperimentsPackageImpl theLDEExperimentsPackage = (LDEExperimentsPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof LDEExperimentsPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new LDEExperimentsPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theLDEExperimentsPackage.createPackageContents();

		// Initialize created meta-data
		theLDEExperimentsPackage.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put
			(theLDEExperimentsPackage,
			 new EValidator.Descriptor() {
			@Override
			public EValidator getEValidator() {
					 return LDEExperimentsValidator.INSTANCE;
				 }
			 });

		// Mark meta-data to indicate it can't be changed
		theLDEExperimentsPackage.freeze();


		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(LDEExperimentsPackage.eNS_URI, theLDEExperimentsPackage);
		return theLDEExperimentsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExperimentDefinition() {
		return experimentDefinitionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExperimentDefinition_Experiment() {
		return (EReference)experimentDefinitionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getExperimentDefinition__GetExperiment__String() {
		return experimentDefinitionEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExperiment() {
		return experimentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExperiment_Name() {
		return (EAttribute)experimentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExperiment_Stage() {
		return (EReference)experimentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExperiment_Description() {
		return (EAttribute)experimentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExperiment_NumberOfStages() {
		return (EAttribute)experimentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getExperiment__GetStageByID__String() {
		return experimentEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStage() {
		return stageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_StageID() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Detector_x() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Detector_y() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Detector_z() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Camera_x() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Camera_y() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Camera_z() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_NumberOfCells() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getStage_Experiment() {
		return (EReference)stageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getStage_Cell() {
		return (EReference)stageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStage__GetCellByID__String() {
		return stageEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStage__HasID__DiagnosticChain_Map() {
		return stageEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCell() {
		return cellEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCell_Stage() {
		return (EReference)cellEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCell_Sample() {
		return (EReference)cellEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_CellID() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Name() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_VisitID() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Email() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_StartDate() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_EndDate() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_EnableAutoEmail() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Calibrant() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Calibrant_x() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Calibrant_y() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Calibrant_exposure() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_Spin() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_EnvScannableNames() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCell_NumberOfSamples() {
		return (EAttribute)cellEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getCell__GetSampleById__String() {
		return cellEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getCell__GetSampleByName__String() {
		return cellEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getCell__HasCellID__DiagnosticChain_Map() {
		return cellEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getCell__HasVisitID__DiagnosticChain_Map() {
		return cellEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSample() {
		return sampleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSample_Cell() {
		return (EReference)sampleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_SampleID() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Status() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Active() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Name() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_x_start() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_x_stop() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_x_step() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_y_start() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_y_stop() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_y_step() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Sample_exposure() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Command() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_Comment() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_CalibrationFilePath() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSample_DataFilePath() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSTATUS() {
		return statusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getStageIDString() {
		return stageIDStringEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getDate() {
		return dateEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getCalibrantNameString() {
		return calibrantNameStringEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public LDEExperimentsFactory getLDEExperimentsFactory() {
		return (LDEExperimentsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		experimentDefinitionEClass = createEClass(EXPERIMENT_DEFINITION);
		createEReference(experimentDefinitionEClass, EXPERIMENT_DEFINITION__EXPERIMENT);
		createEOperation(experimentDefinitionEClass, EXPERIMENT_DEFINITION___GET_EXPERIMENT__STRING);

		experimentEClass = createEClass(EXPERIMENT);
		createEAttribute(experimentEClass, EXPERIMENT__NAME);
		createEReference(experimentEClass, EXPERIMENT__STAGE);
		createEAttribute(experimentEClass, EXPERIMENT__DESCRIPTION);
		createEAttribute(experimentEClass, EXPERIMENT__NUMBER_OF_STAGES);
		createEOperation(experimentEClass, EXPERIMENT___GET_STAGE_BY_ID__STRING);

		stageEClass = createEClass(STAGE);
		createEAttribute(stageEClass, STAGE__STAGE_ID);
		createEReference(stageEClass, STAGE__EXPERIMENT);
		createEReference(stageEClass, STAGE__CELL);
		createEAttribute(stageEClass, STAGE__DETECTOR_X);
		createEAttribute(stageEClass, STAGE__DETECTOR_Y);
		createEAttribute(stageEClass, STAGE__DETECTOR_Z);
		createEAttribute(stageEClass, STAGE__CAMERA_X);
		createEAttribute(stageEClass, STAGE__CAMERA_Y);
		createEAttribute(stageEClass, STAGE__CAMERA_Z);
		createEAttribute(stageEClass, STAGE__NUMBER_OF_CELLS);
		createEOperation(stageEClass, STAGE___GET_CELL_BY_ID__STRING);
		createEOperation(stageEClass, STAGE___HAS_ID__DIAGNOSTICCHAIN_MAP);

		cellEClass = createEClass(CELL);
		createEAttribute(cellEClass, CELL__CELL_ID);
		createEReference(cellEClass, CELL__STAGE);
		createEReference(cellEClass, CELL__SAMPLE);
		createEAttribute(cellEClass, CELL__VISIT_ID);
		createEAttribute(cellEClass, CELL__NAME);
		createEAttribute(cellEClass, CELL__CALIBRANT);
		createEAttribute(cellEClass, CELL__CALIBRANT_X);
		createEAttribute(cellEClass, CELL__CALIBRANT_Y);
		createEAttribute(cellEClass, CELL__CALIBRANT_EXPOSURE);
		createEAttribute(cellEClass, CELL__SPIN);
		createEAttribute(cellEClass, CELL__ENV_SCANNABLE_NAMES);
		createEAttribute(cellEClass, CELL__NUMBER_OF_SAMPLES);
		createEAttribute(cellEClass, CELL__EMAIL);
		createEAttribute(cellEClass, CELL__START_DATE);
		createEAttribute(cellEClass, CELL__END_DATE);
		createEAttribute(cellEClass, CELL__ENABLE_AUTO_EMAIL);
		createEOperation(cellEClass, CELL___GET_SAMPLE_BY_ID__STRING);
		createEOperation(cellEClass, CELL___GET_SAMPLE_BY_NAME__STRING);
		createEOperation(cellEClass, CELL___HAS_CELL_ID__DIAGNOSTICCHAIN_MAP);
		createEOperation(cellEClass, CELL___HAS_VISIT_ID__DIAGNOSTICCHAIN_MAP);

		sampleEClass = createEClass(SAMPLE);
		createEReference(sampleEClass, SAMPLE__CELL);
		createEAttribute(sampleEClass, SAMPLE__STATUS);
		createEAttribute(sampleEClass, SAMPLE__ACTIVE);
		createEAttribute(sampleEClass, SAMPLE__NAME);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_ID);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_XSTART);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_XSTOP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_XSTEP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_YSTART);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_YSTOP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_YSTEP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_EXPOSURE);
		createEAttribute(sampleEClass, SAMPLE__COMMAND);
		createEAttribute(sampleEClass, SAMPLE__COMMENT);
		createEAttribute(sampleEClass, SAMPLE__CALIBRATION_FILE_PATH);
		createEAttribute(sampleEClass, SAMPLE__DATA_FILE_PATH);

		// Create enums
		statusEEnum = createEEnum(STATUS);

		// Create data types
		stageIDStringEDataType = createEDataType(STAGE_ID_STRING);
		dateEDataType = createEDataType(DATE);
		calibrantNameStringEDataType = createEDataType(CALIBRANT_NAME_STRING);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(experimentDefinitionEClass, ExperimentDefinition.class, "ExperimentDefinition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getExperimentDefinition_Experiment(), this.getExperiment(), null, "experiment", null, 0, -1, ExperimentDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = initEOperation(getExperimentDefinition__GetExperiment__String(), this.getExperiment(), "getExperiment", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(experimentEClass, Experiment.class, "Experiment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getExperiment_Name(), ecorePackage.getEString(), "name", null, 0, 1, Experiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getExperiment_Stage(), this.getStage(), this.getStage_Experiment(), "stage", null, 0, -1, Experiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExperiment_Description(), ecorePackage.getEString(), "description", null, 0, 1, Experiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExperiment_NumberOfStages(), ecorePackage.getEInt(), "numberOfStages", "13", 0, 1, Experiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = initEOperation(getExperiment__GetStageByID__String(), this.getStage(), "getStageByID", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stageId", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(stageEClass, Stage.class, "Stage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStage_StageID(), this.getStageIDString(), "stageID", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getStage_Experiment(), this.getExperiment(), this.getExperiment_Stage(), "experiment", null, 1, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getStage_Cell(), this.getCell(), this.getCell_Stage(), "cell", null, 0, -1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Detector_x(), ecorePackage.getEDouble(), "detector_x", "0.0", 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Detector_y(), ecorePackage.getEDouble(), "detector_y", "0.0", 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Detector_z(), ecorePackage.getEDouble(), "detector_z", "400.0", 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Camera_x(), ecorePackage.getEDouble(), "camera_x", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Camera_y(), ecorePackage.getEDouble(), "camera_y", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Camera_z(), ecorePackage.getEDouble(), "camera_z", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_NumberOfCells(), ecorePackage.getEInt(), "numberOfCells", "3", 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = initEOperation(getStage__GetCellByID__String(), this.getCell(), "getCellByID", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "cellId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getStage__HasID__DiagnosticChain_Map(), ecorePackage.getEBoolean(), "hasID", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDiagnosticChain(), "diagnostics", 0, 1, IS_UNIQUE, IS_ORDERED);
		EGenericType g1 = createEGenericType(ecorePackage.getEMap());
		EGenericType g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "context", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(cellEClass, Cell.class, "Cell", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCell_CellID(), ecorePackage.getEString(), "cellID", null, 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCell_Stage(), this.getStage(), this.getStage_Cell(), "stage", null, 1, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCell_Sample(), this.getSample(), this.getSample_Cell(), "sample", null, 0, -1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_VisitID(), ecorePackage.getEString(), "visitID", null, 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Name(), ecorePackage.getEString(), "name", null, 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Calibrant(), this.getCalibrantNameString(), "calibrant", "CeO2(NIST-SRM-674b)", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Calibrant_x(), ecorePackage.getEDouble(), "calibrant_x", "0", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Calibrant_y(), ecorePackage.getEDouble(), "calibrant_y", "0", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Calibrant_exposure(), ecorePackage.getEDouble(), "calibrant_exposure", "1.0", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Spin(), ecorePackage.getEBoolean(), "spin", "false", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_EnvScannableNames(), ecorePackage.getEString(), "envScannableNames", null, 0, -1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_NumberOfSamples(), ecorePackage.getEInt(), "numberOfSamples", "1", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_Email(), ecorePackage.getEString(), "email", null, 0, -1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_StartDate(), this.getDate(), "startDate", null, 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_EndDate(), this.getDate(), "endDate", null, 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCell_EnableAutoEmail(), ecorePackage.getEBoolean(), "enableAutoEmail", "false", 0, 1, Cell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = initEOperation(getCell__GetSampleById__String(), this.getSample(), "getSampleById", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "sampleId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getCell__GetSampleByName__String(), this.getSample(), "getSampleByName", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "sampleName", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getCell__HasCellID__DiagnosticChain_Map(), ecorePackage.getEBoolean(), "hasCellID", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDiagnosticChain(), "diagnostics", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "context", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getCell__HasVisitID__DiagnosticChain_Map(), ecorePackage.getEBoolean(), "hasVisitID", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDiagnosticChain(), "daignostics", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "context", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(sampleEClass, Sample.class, "Sample", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSample_Cell(), this.getCell(), this.getCell_Sample(), "cell", null, 1, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Status(), this.getSTATUS(), "status", "READY", 0, 1, Sample.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Active(), ecorePackage.getEBoolean(), "active", "false", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Name(), ecorePackage.getEString(), "name", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_SampleID(), ecorePackage.getEString(), "sampleID", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_x_start(), ecorePackage.getEDoubleObject(), "sample_x_start", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_x_stop(), ecorePackage.getEDoubleObject(), "sample_x_stop", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_x_step(), ecorePackage.getEDoubleObject(), "sample_x_step", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_y_start(), ecorePackage.getEDoubleObject(), "sample_y_start", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_y_stop(), ecorePackage.getEDoubleObject(), "sample_y_stop", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_y_step(), ecorePackage.getEDoubleObject(), "sample_y_step", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_exposure(), ecorePackage.getEDouble(), "sample_exposure", "5.0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Command(), ecorePackage.getEString(), "command", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Comment(), ecorePackage.getEString(), "comment", "comment here", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_CalibrationFilePath(), ecorePackage.getEString(), "calibrationFilePath", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_DataFilePath(), ecorePackage.getEString(), "dataFilePath", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.class, "STATUS");
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.READY);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.RUNNING);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.PAUSED);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.COMPLETED);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.ABORTED);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.ERROR);

		// Initialize data types
		initEDataType(stageIDStringEDataType, String.class, "StageIDString", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(dateEDataType, Date.class, "Date", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(calibrantNameStringEDataType, String.class, "CalibrantNameString", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/Ecore
		createEcoreAnnotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/Ecore</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createEcoreAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/Ecore";
		addAnnotation
		  (experimentEClass,
		   source,
		   new String[] {
			 "constraints", "NonNegativeQuantity MaximumQuantity"
		   });
		addAnnotation
		  (stageEClass,
		   source,
		   new String[] {
			 "constraints", "NonNegativeQuantity MaximumQuantity"
		   });
		addAnnotation
		  (cellEClass,
		   source,
		   new String[] {
			 "constraints", "ValidStartDate ValidEndDate NonNegativeQuantity ValidCellID MaximumQuantity"
		   });
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
		addAnnotation
		  (stageIDStringEDataType,
		   source,
		   new String[] {
			 "enumeration", "LS1 LS2 MS1 MS2 MS3 MS4 SS1 SS2 SS3 SS4 SS5 SS6 ROBOT"
		   });
		addAnnotation
		  (calibrantNameStringEDataType,
		   source,
		   new String[] {
			 "enumeration", "Si(NIST-SRM-640c) CeO2(NIST-SRM-674b)"
		   });
	}

} //LDEExperimentsPackageImpl
