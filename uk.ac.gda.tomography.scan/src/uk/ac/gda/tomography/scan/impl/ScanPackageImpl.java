/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.scan.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanFactory;
import uk.ac.gda.tomography.scan.ScanPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ScanPackageImpl extends EPackageImpl implements ScanPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parametersEClass = null;

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
	 * @see uk.ac.gda.tomography.scan.ScanPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ScanPackageImpl() {
		super(eNS_URI, ScanFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ScanPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ScanPackage init() {
		if (isInited)
			return (ScanPackage) EPackage.Registry.INSTANCE
					.getEPackage(ScanPackage.eNS_URI);

		// Obtain or create and register package
		ScanPackageImpl theScanPackage = (ScanPackageImpl) (EPackage.Registry.INSTANCE
				.get(eNS_URI) instanceof ScanPackageImpl ? EPackage.Registry.INSTANCE
				.get(eNS_URI) : new ScanPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theScanPackage.createPackageContents();

		// Initialize created meta-data
		theScanPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theScanPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ScanPackage.eNS_URI, theScanPackage);
		return theScanPackage;
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
	public EAttribute getParameters_InBeamPosition() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__IN_BEAM_POSITION);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_OutOfBeamPosition() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_ExposureTime() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__EXPOSURE_TIME);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_Start() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__START);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_Stop() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__STOP);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_Step() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__STEP);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_DarkFieldInterval() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_FlatFieldInterval() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_ImagesPerDark() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__IMAGES_PER_DARK);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_ImagesPerFlat() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__IMAGES_PER_FLAT);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_MinI() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__MIN_I);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_Title() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__TITLE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameters_FlyScan() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__FLY_SCAN);
	}

	@Override
	public EAttribute getParameters_ExtraFlatsAtEnd() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__EXTRA_FLATS_AT_END);
	}

	@Override
	public EAttribute getParameters_NumFlyScans() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__NUM_FLY_SCANS);
	}

	@Override
	public EAttribute getParameters_FlyScanDelay() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__FLY_SCAN_DELAY);
	}

	@Override
	public EAttribute getParameters_ApproxCentreOfRotation() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__APPROX_CENTRE_OF_ROTATION);
	}


	@Override
	public EAttribute getParameters_DetectorToSampleDistance() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE);
	}

	@Override
	public EAttribute getParameters_DetectorToSampleDistanceUnits() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS);
	}

	@Override
	public EAttribute getParameters_XPixelSize() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__X_PIXEL_SIZE);
	}

	@Override
	public EAttribute getParameters_XPixelSizeUnits() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__X_PIXEL_SIZE_UNITS);
	}

	@Override
	public EAttribute getParameters_YPixelSize() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__Y_PIXEL_SIZE);
	}

	@Override
	public EAttribute getParameters_YPixelSizeUnits() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__Y_PIXEL_SIZE_UNITS);
	}

	@Override
	public EAttribute getParameters_RotationStage() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__ROTATION_STAGE);
	}

	@Override
	public EAttribute getParameters_LinearStage() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__LINEAR_STAGE);
	}

	@Override
	public EAttribute getParameters_CloseShutterAfterLastScan() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN);
	}

	@Override
	public EAttribute getParameters_SendDataToTemporaryDirectory() {
		return (EAttribute) parametersEClass.getEStructuralFeatures().get(ScanPackage.PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ScanFactory getScanFactory() {
		return (ScanFactory) getEFactoryInstance();
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
		if (isCreated)
			return;
		isCreated = true;

		// Create classes and their features
		parametersEClass = createEClass(PARAMETERS);
		createEAttribute(parametersEClass, PARAMETERS__IN_BEAM_POSITION);
		createEAttribute(parametersEClass, PARAMETERS__OUT_OF_BEAM_POSITION);
		createEAttribute(parametersEClass, PARAMETERS__EXPOSURE_TIME);
		createEAttribute(parametersEClass, PARAMETERS__START);
		createEAttribute(parametersEClass, PARAMETERS__STOP);
		createEAttribute(parametersEClass, PARAMETERS__STEP);
		createEAttribute(parametersEClass, PARAMETERS__DARK_FIELD_INTERVAL);
		createEAttribute(parametersEClass, PARAMETERS__FLAT_FIELD_INTERVAL);
		createEAttribute(parametersEClass, PARAMETERS__IMAGES_PER_DARK);
		createEAttribute(parametersEClass, PARAMETERS__IMAGES_PER_FLAT);
		createEAttribute(parametersEClass, PARAMETERS__MIN_I);
		createEAttribute(parametersEClass, PARAMETERS__TITLE);
		createEAttribute(parametersEClass, PARAMETERS__FLY_SCAN);
		createEAttribute(parametersEClass, PARAMETERS__EXTRA_FLATS_AT_END);
		createEAttribute(parametersEClass, PARAMETERS__NUM_FLY_SCANS);
		createEAttribute(parametersEClass, PARAMETERS__FLY_SCAN_DELAY);
		createEAttribute(parametersEClass, PARAMETERS__APPROX_CENTRE_OF_ROTATION);
		createEAttribute(parametersEClass, PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE);
		createEAttribute(parametersEClass, PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS);
		createEAttribute(parametersEClass, PARAMETERS__X_PIXEL_SIZE);
		createEAttribute(parametersEClass, PARAMETERS__X_PIXEL_SIZE_UNITS);
		createEAttribute(parametersEClass, PARAMETERS__Y_PIXEL_SIZE);
		createEAttribute(parametersEClass, PARAMETERS__Y_PIXEL_SIZE_UNITS);
		createEAttribute(parametersEClass, PARAMETERS__ROTATION_STAGE);
		createEAttribute(parametersEClass, PARAMETERS__LINEAR_STAGE);
		createEAttribute(parametersEClass, PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN);
		createEAttribute(parametersEClass, PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY);
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
		if (isInitialized)
			return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(
				parametersEClass,
				Parameters.class,
				"Parameters", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(
				getParameters_InBeamPosition(),
				ecorePackage.getEDouble(),
				"inBeamPosition", "0.", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_OutOfBeamPosition(),
				ecorePackage.getEDouble(),
				"outOfBeamPosition", "0.", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_ExposureTime(),
				ecorePackage.getEDouble(),
				"exposureTime", "1.0", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_Start(),
				ecorePackage.getEDouble(),
				"start", "0.", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_Stop(),
				ecorePackage.getEDouble(),
				"stop", "180.", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_Step(),
				ecorePackage.getEDouble(),
				"step", ".1", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_DarkFieldInterval(),
				ecorePackage.getEInt(),
				"darkFieldInterval", "0", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_FlatFieldInterval(),
				ecorePackage.getEInt(),
				"flatFieldInterval", "0", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_ImagesPerDark(),
				ecorePackage.getEInt(),
				"imagesPerDark", "1", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_ImagesPerFlat(),
				ecorePackage.getEInt(),
				"imagesPerFlat", "1", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_MinI(),
				ecorePackage.getEDouble(),
				"minI", "-1.", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_Title(),
				ecorePackage.getEString(),
				"title", "Unknown", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_FlyScan(),
				ecorePackage.getEBoolean(),
				"flyScan", "false", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getParameters_ExtraFlatsAtEnd(),
				ecorePackage.getEBoolean(),
				"extraFlatsAtEnd", "false", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_NumFlyScans(),
				ecorePackage.getEInt(),
				"numFlyScans", "1", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_FlyScanDelay(),
				ecorePackage.getEDouble(),
				"flyScanDelay", "0.", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
				IS_ORDERED);
		initEAttribute(
				getParameters_ApproxCentreOfRotation(),
				ecorePackage.getEString(),
				"approxCentreOfRotation", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_DetectorToSampleDistance(),
				ecorePackage.getEString(),
				"detectorToSampleDistance", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_DetectorToSampleDistanceUnits(),
				ecorePackage.getEString(),
				"detectorToSampleDistanceUnits", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_XPixelSize(),
				ecorePackage.getEString(),
				"xPixelSize", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_XPixelSizeUnits(),
				ecorePackage.getEString(),
				"xPixelSizeUnits", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_YPixelSize(),
				ecorePackage.getEString(),
				"yPixelSize", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_YPixelSizeUnits(),
				ecorePackage.getEString(),
				"yPixelSizeUnits", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_RotationStage(),
				ecorePackage.getEString(),
				"rotationStage", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_LinearStage(),
				ecorePackage.getEString(),
				"linearStage", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_CloseShutterAfterLastScan(),
				ecorePackage.getEBoolean(),
				"closeShutterAfterLastScan", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(
				getParameters_SendDataToTemporaryDirectory(),
				ecorePackage.getEBoolean(),
				"sendDataToTemporaryDirectory", "", 1, 1, Parameters.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ScanPackageImpl
