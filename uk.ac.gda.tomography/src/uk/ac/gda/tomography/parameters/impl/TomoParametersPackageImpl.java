/**
o * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.DetectorStage;
import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.SampleStage;
import uk.ac.gda.tomography.parameters.SampleWeight;
import uk.ac.gda.tomography.parameters.ScanMode;
import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.Unit;
import uk.ac.gda.tomography.parameters.ValueUnit;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TomoParametersPackageImpl extends EPackageImpl implements TomoParametersPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tomoExperimentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass valueUnitEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass detectorStageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass moduleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass detectorBinEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass detectorPropertiesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass detectorRoiEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass alignmentConfigurationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parametersEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sampleStageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stitchParametersEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum scanModeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum resolutionEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum sampleWeightEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum unitEEnum = null;

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
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TomoParametersPackageImpl() {
		super(eNS_URI, TomoParametersFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link TomoParametersPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TomoParametersPackage init() {
		if (isInited) return (TomoParametersPackage)EPackage.Registry.INSTANCE.getEPackage(TomoParametersPackage.eNS_URI);

		// Obtain or create and register package
		TomoParametersPackageImpl theTomoParametersPackage = (TomoParametersPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof TomoParametersPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new TomoParametersPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theTomoParametersPackage.createPackageContents();

		// Initialize created meta-data
		theTomoParametersPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTomoParametersPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TomoParametersPackage.eNS_URI, theTomoParametersPackage);
		return theTomoParametersPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTomoExperiment() {
		return tomoExperimentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTomoExperiment_Parameters() {
		return (EReference)tomoExperimentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTomoExperiment_Description() {
		return (EAttribute)tomoExperimentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTomoExperiment_TotalTimeToRun() {
		return (EAttribute)tomoExperimentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTomoExperiment_Version() {
		return (EAttribute)tomoExperimentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getValueUnit() {
		return valueUnitEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueUnit_Units() {
		return (EAttribute)valueUnitEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueUnit_Value() {
		return (EAttribute)valueUnitEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDetectorStage() {
		return detectorStageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorStage_X() {
		return (EReference)detectorStageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorStage_Y() {
		return (EReference)detectorStageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorStage_Z() {
		return (EReference)detectorStageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getModule() {
		return moduleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getModule_ModuleNumber() {
		return (EAttribute)moduleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getModule_HorizontalFieldOfView() {
		return (EReference)moduleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDetectorBin() {
		return detectorBinEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorBin_BinX() {
		return (EAttribute)detectorBinEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorBin_BinY() {
		return (EAttribute)detectorBinEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDetectorProperties() {
		return detectorPropertiesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorProperties_Desired3DResolution() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorProperties_NumberOfFramerPerProjection() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorProperties_AcquisitionTimeDivider() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorProperties_DetectorRoi() {
		return (EReference)detectorPropertiesEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorProperties_DetectorBin() {
		return (EReference)detectorPropertiesEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorProperties_ModuleParameters() {
		return (EReference)detectorPropertiesEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDetectorRoi() {
		return detectorRoiEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorRoi_MinX() {
		return (EAttribute)detectorRoiEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorRoi_MaxX() {
		return (EAttribute)detectorRoiEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorRoi_MinY() {
		return (EAttribute)detectorRoiEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorRoi_MaxY() {
		return (EAttribute)detectorRoiEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAlignmentConfiguration() {
		return alignmentConfigurationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_Id() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_Energy() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_Description() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAlignmentConfiguration_DetectorProperties() {
		return (EReference)alignmentConfigurationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAlignmentConfiguration_SampleStageParameters() {
		return (EReference)alignmentConfigurationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_ScanMode() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_SampleExposureTime() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_FlatExposureTime() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_CreatedUserId() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_CreatedDateTime() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_SampleWeight() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAlignmentConfiguration_DetectorStageParameters() {
		return (EReference)alignmentConfigurationEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAlignmentConfiguration_ProposalId() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getAlignmentConfiguration_StitchParameters() {
		return (EReference)alignmentConfigurationEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAlignmentConfiguration_SelectedToRun() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getParameters() {
		return parametersEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getParameters_ConfigurationSet() {
		return (EReference)parametersEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSampleStage() {
		return sampleStageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleStage_Vertical() {
		return (EReference)sampleStageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleStage_CenterX() {
		return (EReference)sampleStageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleStage_CenterZ() {
		return (EReference)sampleStageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleStage_TiltX() {
		return (EReference)sampleStageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleStage_TiltZ() {
		return (EReference)sampleStageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleStage_BaseX() {
		return (EReference)sampleStageEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getStitchParameters() {
		return stitchParametersEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStitchParameters_StitchingThetaAngle() {
		return (EAttribute)stitchParametersEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStitchParameters_ImageAtTheta() {
		return (EAttribute)stitchParametersEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStitchParameters_ImageAtThetaPlus90() {
		return (EAttribute)stitchParametersEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getScanMode() {
		return scanModeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getResolution() {
		return resolutionEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSampleWeight() {
		return sampleWeightEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getUnit() {
		return unitEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TomoParametersFactory getTomoParametersFactory() {
		return (TomoParametersFactory)getEFactoryInstance();
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
		alignmentConfigurationEClass = createEClass(ALIGNMENT_CONFIGURATION);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__ID);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__ENERGY);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__DESCRIPTION);
		createEReference(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES);
		createEReference(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SAMPLE_STAGE_PARAMETERS);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SCAN_MODE);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__CREATED_USER_ID);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT);
		createEReference(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__DETECTOR_STAGE_PARAMETERS);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__PROPOSAL_ID);
		createEReference(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN);

		detectorBinEClass = createEClass(DETECTOR_BIN);
		createEAttribute(detectorBinEClass, DETECTOR_BIN__BIN_X);
		createEAttribute(detectorBinEClass, DETECTOR_BIN__BIN_Y);

		detectorPropertiesEClass = createEClass(DETECTOR_PROPERTIES);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER);
		createEReference(detectorPropertiesEClass, DETECTOR_PROPERTIES__DETECTOR_ROI);
		createEReference(detectorPropertiesEClass, DETECTOR_PROPERTIES__DETECTOR_BIN);
		createEReference(detectorPropertiesEClass, DETECTOR_PROPERTIES__MODULE_PARAMETERS);

		detectorRoiEClass = createEClass(DETECTOR_ROI);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MIN_X);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MAX_X);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MIN_Y);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MAX_Y);

		detectorStageEClass = createEClass(DETECTOR_STAGE);
		createEReference(detectorStageEClass, DETECTOR_STAGE__X);
		createEReference(detectorStageEClass, DETECTOR_STAGE__Y);
		createEReference(detectorStageEClass, DETECTOR_STAGE__Z);

		moduleEClass = createEClass(MODULE);
		createEAttribute(moduleEClass, MODULE__MODULE_NUMBER);
		createEReference(moduleEClass, MODULE__HORIZONTAL_FIELD_OF_VIEW);

		parametersEClass = createEClass(PARAMETERS);
		createEReference(parametersEClass, PARAMETERS__CONFIGURATION_SET);

		sampleStageEClass = createEClass(SAMPLE_STAGE);
		createEReference(sampleStageEClass, SAMPLE_STAGE__VERTICAL);
		createEReference(sampleStageEClass, SAMPLE_STAGE__CENTER_X);
		createEReference(sampleStageEClass, SAMPLE_STAGE__CENTER_Z);
		createEReference(sampleStageEClass, SAMPLE_STAGE__TILT_X);
		createEReference(sampleStageEClass, SAMPLE_STAGE__TILT_Z);
		createEReference(sampleStageEClass, SAMPLE_STAGE__BASE_X);

		stitchParametersEClass = createEClass(STITCH_PARAMETERS);
		createEAttribute(stitchParametersEClass, STITCH_PARAMETERS__STITCHING_THETA_ANGLE);
		createEAttribute(stitchParametersEClass, STITCH_PARAMETERS__IMAGE_AT_THETA);
		createEAttribute(stitchParametersEClass, STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90);

		tomoExperimentEClass = createEClass(TOMO_EXPERIMENT);
		createEReference(tomoExperimentEClass, TOMO_EXPERIMENT__PARAMETERS);
		createEAttribute(tomoExperimentEClass, TOMO_EXPERIMENT__DESCRIPTION);
		createEAttribute(tomoExperimentEClass, TOMO_EXPERIMENT__TOTAL_TIME_TO_RUN);
		createEAttribute(tomoExperimentEClass, TOMO_EXPERIMENT__VERSION);

		valueUnitEClass = createEClass(VALUE_UNIT);
		createEAttribute(valueUnitEClass, VALUE_UNIT__UNITS);
		createEAttribute(valueUnitEClass, VALUE_UNIT__VALUE);

		// Create enums
		scanModeEEnum = createEEnum(SCAN_MODE);
		resolutionEEnum = createEEnum(RESOLUTION);
		sampleWeightEEnum = createEEnum(SAMPLE_WEIGHT);
		unitEEnum = createEEnum(UNIT);
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

		// Initialize classes and features; add operations and parameters
		initEClass(alignmentConfigurationEClass, AlignmentConfiguration.class, "AlignmentConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAlignmentConfiguration_Id(), ecorePackage.getEString(), "id", null, 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_Energy(), ecorePackage.getEDouble(), "energy", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_Description(), ecorePackage.getEString(), "description", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAlignmentConfiguration_DetectorProperties(), this.getDetectorProperties(), null, "detectorProperties", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAlignmentConfiguration_SampleStageParameters(), this.getSampleStage(), null, "sampleStageParameters", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_ScanMode(), this.getScanMode(), "scanMode", "Step", 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_SampleExposureTime(), ecorePackage.getEDouble(), "sampleExposureTime", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_FlatExposureTime(), ecorePackage.getEDouble(), "flatExposureTime", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_CreatedUserId(), ecorePackage.getEString(), "createdUserId", null, 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_CreatedDateTime(), ecorePackage.getEDate(), "createdDateTime", null, 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_SampleWeight(), this.getSampleWeight(), "sampleWeight", null, 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAlignmentConfiguration_DetectorStageParameters(), this.getDetectorStage(), null, "detectorStageParameters", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_ProposalId(), ecorePackage.getEString(), "proposalId", null, 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAlignmentConfiguration_StitchParameters(), this.getStitchParameters(), null, "stitchParameters", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_SelectedToRun(), ecorePackage.getEBooleanObject(), "selectedToRun", "false", 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorBinEClass, DetectorBin.class, "DetectorBin", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetectorBin_BinX(), ecorePackage.getEIntegerObject(), "binX", null, 1, 1, DetectorBin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorBin_BinY(), ecorePackage.getEIntegerObject(), "binY", null, 1, 1, DetectorBin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorPropertiesEClass, DetectorProperties.class, "DetectorProperties", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetectorProperties_Desired3DResolution(), this.getResolution(), "desired3DResolution", "Full", 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorProperties_NumberOfFramerPerProjection(), ecorePackage.getEIntegerObject(), "numberOfFramerPerProjection", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorProperties_AcquisitionTimeDivider(), ecorePackage.getEIntegerObject(), "acquisitionTimeDivider", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorProperties_DetectorRoi(), this.getDetectorRoi(), null, "detectorRoi", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorProperties_DetectorBin(), this.getDetectorBin(), null, "detectorBin", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorProperties_ModuleParameters(), this.getModule(), null, "moduleParameters", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorRoiEClass, DetectorRoi.class, "DetectorRoi", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetectorRoi_MinX(), ecorePackage.getEIntegerObject(), "minX", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorRoi_MaxX(), ecorePackage.getEIntegerObject(), "maxX", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorRoi_MinY(), ecorePackage.getEIntegerObject(), "minY", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorRoi_MaxY(), ecorePackage.getEIntegerObject(), "maxY", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorStageEClass, DetectorStage.class, "DetectorStage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDetectorStage_X(), this.getValueUnit(), null, "x", null, 0, 1, DetectorStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorStage_Y(), this.getValueUnit(), null, "y", null, 0, 1, DetectorStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorStage_Z(), this.getValueUnit(), null, "z", null, 0, 1, DetectorStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(moduleEClass, Module.class, "Module", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getModule_ModuleNumber(), ecorePackage.getEIntegerObject(), "moduleNumber", null, 1, 1, Module.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getModule_HorizontalFieldOfView(), this.getValueUnit(), null, "horizontalFieldOfView", null, 0, 1, Module.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(parametersEClass, Parameters.class, "Parameters", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getParameters_ConfigurationSet(), this.getAlignmentConfiguration(), null, "configurationSet", null, 0, -1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = addEOperation(parametersEClass, this.getAlignmentConfiguration(), "getAlignmentConfiguration", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "configurationId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(parametersEClass, ecorePackage.getEIntegerObject(), "getIndex", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getAlignmentConfiguration(), "alignmentConfiguration", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(sampleStageEClass, SampleStage.class, "SampleStage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSampleStage_Vertical(), this.getValueUnit(), null, "vertical", null, 0, 1, SampleStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSampleStage_CenterX(), this.getValueUnit(), null, "centerX", null, 0, 1, SampleStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSampleStage_CenterZ(), this.getValueUnit(), null, "centerZ", null, 0, 1, SampleStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSampleStage_TiltX(), this.getValueUnit(), null, "tiltX", null, 0, 1, SampleStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSampleStage_TiltZ(), this.getValueUnit(), null, "tiltZ", null, 0, 1, SampleStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSampleStage_BaseX(), this.getValueUnit(), null, "baseX", null, 0, 1, SampleStage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stitchParametersEClass, StitchParameters.class, "StitchParameters", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStitchParameters_StitchingThetaAngle(), ecorePackage.getEDouble(), "stitchingThetaAngle", null, 1, 1, StitchParameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStitchParameters_ImageAtTheta(), ecorePackage.getEString(), "imageAtTheta", null, 0, 1, StitchParameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStitchParameters_ImageAtThetaPlus90(), ecorePackage.getEString(), "imageAtThetaPlus90", null, 0, 1, StitchParameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tomoExperimentEClass, TomoExperiment.class, "TomoExperiment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTomoExperiment_Parameters(), this.getParameters(), null, "parameters", null, 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTomoExperiment_Description(), ecorePackage.getEString(), "description", null, 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTomoExperiment_TotalTimeToRun(), ecorePackage.getEDate(), "totalTimeToRun", null, 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTomoExperiment_Version(), ecorePackage.getEInt(), "version", "1", 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(valueUnitEClass, ValueUnit.class, "ValueUnit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getValueUnit_Units(), this.getUnit(), "units", null, 0, 1, ValueUnit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueUnit_Value(), ecorePackage.getEDouble(), "value", null, 0, 1, ValueUnit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(scanModeEEnum, ScanMode.class, "ScanMode");
		addEEnumLiteral(scanModeEEnum, ScanMode.CONTINUOUS);
		addEEnumLiteral(scanModeEEnum, ScanMode.STEP);

		initEEnum(resolutionEEnum, Resolution.class, "Resolution");
		addEEnumLiteral(resolutionEEnum, Resolution.FULL);
		addEEnumLiteral(resolutionEEnum, Resolution.X2);
		addEEnumLiteral(resolutionEEnum, Resolution.X4);
		addEEnumLiteral(resolutionEEnum, Resolution.X8);

		initEEnum(sampleWeightEEnum, SampleWeight.class, "SampleWeight");
		addEEnumLiteral(sampleWeightEEnum, SampleWeight.LESS_THAN_1);
		addEEnumLiteral(sampleWeightEEnum, SampleWeight.ONE_TO_TEN);
		addEEnumLiteral(sampleWeightEEnum, SampleWeight.TEN_TO_TWENTY);
		addEEnumLiteral(sampleWeightEEnum, SampleWeight.TWENTY_TO_FIFTY);

		initEEnum(unitEEnum, Unit.class, "Unit");
		addEEnumLiteral(unitEEnum, Unit.SECONDS);
		addEEnumLiteral(unitEEnum, Unit.MM);
		addEEnumLiteral(unitEEnum, Unit.MICRONS);
		addEEnumLiteral(unitEEnum, Unit.DEGREE);

		// Create resource
		createResource(eNS_URI);
	}

} //TomoParametersPackageImpl
