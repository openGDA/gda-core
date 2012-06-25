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
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.SampleParams;
import uk.ac.gda.tomography.parameters.SamplePosition;
import uk.ac.gda.tomography.parameters.ScanMode;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

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
	private EClass sampleParamsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass samplePositionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum scanModeEEnum = null;

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
	public EAttribute getDetectorProperties_Module() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorProperties_Desired3DResolution() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorProperties_NumberOfFramerPerProjection() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDetectorProperties_AcquisitionTimeDivider() {
		return (EAttribute)detectorPropertiesEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorProperties_DetectorRoi() {
		return (EReference)detectorPropertiesEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDetectorProperties_DetectorBin() {
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
	public EAttribute getAlignmentConfiguration_NumberOfProjections() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_Description() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAlignmentConfiguration_DetectorProperties() {
		return (EReference)alignmentConfigurationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_SampleDetectorDistance() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAlignmentConfiguration_SampleParams() {
		return (EReference)alignmentConfigurationEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_ScanMode() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_SampleExposureTime() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlignmentConfiguration_FlatExposureTime() {
		return (EAttribute)alignmentConfigurationEClass.getEStructuralFeatures().get(9);
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
	public EClass getSampleParams() {
		return sampleParamsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSampleParams_Position() {
		return (EReference)sampleParamsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSampleParams_Weight() {
		return (EAttribute)sampleParamsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSamplePosition() {
		return samplePositionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSamplePosition_Vertical() {
		return (EAttribute)samplePositionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSamplePosition_CenterX() {
		return (EAttribute)samplePositionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSamplePosition_CenterZ() {
		return (EAttribute)samplePositionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSamplePosition_TiltX() {
		return (EAttribute)samplePositionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSamplePosition_TiltZ() {
		return (EAttribute)samplePositionEClass.getEStructuralFeatures().get(4);
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
		tomoExperimentEClass = createEClass(TOMO_EXPERIMENT);
		createEReference(tomoExperimentEClass, TOMO_EXPERIMENT__PARAMETERS);
		createEAttribute(tomoExperimentEClass, TOMO_EXPERIMENT__DESCRIPTION);
		createEAttribute(tomoExperimentEClass, TOMO_EXPERIMENT__TOTAL_TIME_TO_RUN);
		createEAttribute(tomoExperimentEClass, TOMO_EXPERIMENT__VERSION);

		detectorBinEClass = createEClass(DETECTOR_BIN);
		createEAttribute(detectorBinEClass, DETECTOR_BIN__BIN_X);
		createEAttribute(detectorBinEClass, DETECTOR_BIN__BIN_Y);

		detectorPropertiesEClass = createEClass(DETECTOR_PROPERTIES);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__MODULE);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION);
		createEAttribute(detectorPropertiesEClass, DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER);
		createEReference(detectorPropertiesEClass, DETECTOR_PROPERTIES__DETECTOR_ROI);
		createEReference(detectorPropertiesEClass, DETECTOR_PROPERTIES__DETECTOR_BIN);

		detectorRoiEClass = createEClass(DETECTOR_ROI);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MIN_X);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MAX_X);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MIN_Y);
		createEAttribute(detectorRoiEClass, DETECTOR_ROI__MAX_Y);

		alignmentConfigurationEClass = createEClass(ALIGNMENT_CONFIGURATION);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__ID);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__ENERGY);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__DESCRIPTION);
		createEReference(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE);
		createEReference(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SCAN_MODE);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME);
		createEAttribute(alignmentConfigurationEClass, ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME);

		parametersEClass = createEClass(PARAMETERS);
		createEReference(parametersEClass, PARAMETERS__CONFIGURATION_SET);

		sampleParamsEClass = createEClass(SAMPLE_PARAMS);
		createEReference(sampleParamsEClass, SAMPLE_PARAMS__POSITION);
		createEAttribute(sampleParamsEClass, SAMPLE_PARAMS__WEIGHT);

		samplePositionEClass = createEClass(SAMPLE_POSITION);
		createEAttribute(samplePositionEClass, SAMPLE_POSITION__VERTICAL);
		createEAttribute(samplePositionEClass, SAMPLE_POSITION__CENTER_X);
		createEAttribute(samplePositionEClass, SAMPLE_POSITION__CENTER_Z);
		createEAttribute(samplePositionEClass, SAMPLE_POSITION__TILT_X);
		createEAttribute(samplePositionEClass, SAMPLE_POSITION__TILT_Z);

		// Create enums
		scanModeEEnum = createEEnum(SCAN_MODE);
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
		initEClass(tomoExperimentEClass, TomoExperiment.class, "TomoExperiment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTomoExperiment_Parameters(), this.getParameters(), null, "parameters", null, 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTomoExperiment_Description(), ecorePackage.getEString(), "description", null, 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTomoExperiment_TotalTimeToRun(), ecorePackage.getEDate(), "totalTimeToRun", null, 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTomoExperiment_Version(), ecorePackage.getEInt(), "version", "1", 0, 1, TomoExperiment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorBinEClass, DetectorBin.class, "DetectorBin", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetectorBin_BinX(), ecorePackage.getEIntegerObject(), "binX", null, 1, 1, DetectorBin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorBin_BinY(), ecorePackage.getEIntegerObject(), "binY", null, 1, 1, DetectorBin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorPropertiesEClass, DetectorProperties.class, "DetectorProperties", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetectorProperties_Module(), ecorePackage.getEIntegerObject(), "module", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorProperties_Desired3DResolution(), ecorePackage.getEIntegerObject(), "desired3DResolution", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorProperties_NumberOfFramerPerProjection(), ecorePackage.getEIntegerObject(), "numberOfFramerPerProjection", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorProperties_AcquisitionTimeDivider(), ecorePackage.getEIntegerObject(), "acquisitionTimeDivider", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorProperties_DetectorRoi(), this.getDetectorRoi(), null, "detectorRoi", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDetectorProperties_DetectorBin(), this.getDetectorBin(), null, "detectorBin", null, 1, 1, DetectorProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorRoiEClass, DetectorRoi.class, "DetectorRoi", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetectorRoi_MinX(), ecorePackage.getEIntegerObject(), "minX", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorRoi_MaxX(), ecorePackage.getEIntegerObject(), "maxX", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorRoi_MinY(), ecorePackage.getEIntegerObject(), "minY", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetectorRoi_MaxY(), ecorePackage.getEIntegerObject(), "maxY", null, 1, 1, DetectorRoi.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(alignmentConfigurationEClass, AlignmentConfiguration.class, "AlignmentConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAlignmentConfiguration_Id(), ecorePackage.getEString(), "id", null, 0, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_Energy(), ecorePackage.getEDouble(), "energy", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_NumberOfProjections(), ecorePackage.getEIntegerObject(), "numberOfProjections", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_Description(), ecorePackage.getEString(), "description", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAlignmentConfiguration_DetectorProperties(), this.getDetectorProperties(), null, "detectorProperties", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_SampleDetectorDistance(), ecorePackage.getEDouble(), "sampleDetectorDistance", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAlignmentConfiguration_SampleParams(), this.getSampleParams(), null, "sampleParams", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_ScanMode(), this.getScanMode(), "scanMode", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_SampleExposureTime(), ecorePackage.getEDouble(), "sampleExposureTime", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAlignmentConfiguration_FlatExposureTime(), ecorePackage.getEDouble(), "flatExposureTime", null, 1, 1, AlignmentConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(parametersEClass, Parameters.class, "Parameters", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getParameters_ConfigurationSet(), this.getAlignmentConfiguration(), null, "configurationSet", null, 0, -1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sampleParamsEClass, SampleParams.class, "SampleParams", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSampleParams_Position(), this.getSamplePosition(), null, "position", null, 1, 1, SampleParams.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSampleParams_Weight(), ecorePackage.getEDouble(), "weight", null, 1, 1, SampleParams.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(samplePositionEClass, SamplePosition.class, "SamplePosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSamplePosition_Vertical(), ecorePackage.getEDouble(), "vertical", null, 1, 1, SamplePosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSamplePosition_CenterX(), ecorePackage.getEDouble(), "centerX", null, 1, 1, SamplePosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSamplePosition_CenterZ(), ecorePackage.getEDouble(), "centerZ", null, 1, 1, SamplePosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSamplePosition_TiltX(), ecorePackage.getEDouble(), "tiltX", null, 1, 1, SamplePosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSamplePosition_TiltZ(), ecorePackage.getEDouble(), "tiltZ", null, 1, 1, SamplePosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(scanModeEEnum, ScanMode.class, "ScanMode");
		addEEnumLiteral(scanModeEEnum, ScanMode.CONTINUOUS);
		addEEnumLiteral(scanModeEEnum, ScanMode.STEP);

		// Create resource
		createResource(eNS_URI);
	}

} //TomoParametersPackageImpl
