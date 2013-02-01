/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy1;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.PassEnergy;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Step;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RegiondefinitionPackageImpl extends EPackageImpl implements RegiondefinitionPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass documentRootEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sequenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass regionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass runModeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass energyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stepEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass detectorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass spectrumEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum lenS_MODEEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum ruN_MODESEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum acquiaitioN_MODEEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum energY_MODEEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum detectoR_MODEEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum pasS_ENERGYEEnum = null;

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
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private RegiondefinitionPackageImpl() {
		super(eNS_URI, RegiondefinitionFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link RegiondefinitionPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static RegiondefinitionPackage init() {
		if (isInited) return (RegiondefinitionPackage)EPackage.Registry.INSTANCE.getEPackage(RegiondefinitionPackage.eNS_URI);

		// Obtain or create and register package
		RegiondefinitionPackageImpl theRegiondefinitionPackage = (RegiondefinitionPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof RegiondefinitionPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new RegiondefinitionPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theRegiondefinitionPackage.createPackageContents();

		// Initialize created meta-data
		theRegiondefinitionPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theRegiondefinitionPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(RegiondefinitionPackage.eNS_URI, theRegiondefinitionPackage);
		return theRegiondefinitionPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDocumentRoot() {
		return documentRootEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_Sequence() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSequence() {
		return sequenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSequence_Region() {
		return (EReference)sequenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSequence_RunMode() {
		return (EAttribute)sequenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSequence_NumIterations() {
		return (EAttribute)sequenceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSequence_RepeatUnitilStopped() {
		return (EAttribute)sequenceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSequence_Spectrum() {
		return (EReference)sequenceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSequence_Filename() {
		return (EAttribute)sequenceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRegion() {
		return regionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRegion_Name() {
		return (EAttribute)regionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRegion_Lensmode() {
		return (EAttribute)regionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRegion_PassEnergy() {
		return (EAttribute)regionEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRegion_RunMode() {
		return (EReference)regionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRegion_AcquisitionMode() {
		return (EAttribute)regionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRegion_EnergyMode() {
		return (EAttribute)regionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRegion_Energy() {
		return (EReference)regionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRegion_Step() {
		return (EReference)regionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRegion_Detector() {
		return (EReference)regionEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRunMode() {
		return runModeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRunMode_Mode() {
		return (EAttribute)runModeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRunMode_NumIterations() {
		return (EAttribute)runModeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRunMode_RepeatUnitilStopped() {
		return (EAttribute)runModeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getEnergy() {
		return energyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getEnergy_Low() {
		return (EAttribute)energyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getEnergy_High() {
		return (EAttribute)energyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getEnergy_Center() {
		return (EAttribute)energyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getEnergy_Width() {
		return (EAttribute)energyEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getStep() {
		return stepEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStep_Frames() {
		return (EAttribute)stepEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStep_Time() {
		return (EAttribute)stepEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStep_Size() {
		return (EAttribute)stepEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStep_TotalTime() {
		return (EAttribute)stepEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStep_TotalSteps() {
		return (EAttribute)stepEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDetector() {
		return detectorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDetector_FirstXChannel() {
		return (EAttribute)detectorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDetector_LastXChannel() {
		return (EAttribute)detectorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDetector_FirstYChannel() {
		return (EAttribute)detectorEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDetector_LastYChannel() {
		return (EAttribute)detectorEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDetector_Slices() {
		return (EAttribute)detectorEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDetector_DetectorMode() {
		return (EAttribute)detectorEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSpectrum() {
		return spectrumEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_Location() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_User() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_SampleName() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_FilenamePrefix() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_BaseDirectory() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_FilenameFormet() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_FileExtension() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_NumberOfComments() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSpectrum_Comments() {
		return (EAttribute)spectrumEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getLENS_MODE() {
		return lenS_MODEEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getRUN_MODES() {
		return ruN_MODESEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getACQUIAITION_MODE() {
		return acquiaitioN_MODEEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getENERGY_MODE() {
		return energY_MODEEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDETECTOR_MODE() {
		return detectoR_MODEEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getPASS_ENERGY() {
		return pasS_ENERGYEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegiondefinitionFactory getRegiondefinitionFactory() {
		return (RegiondefinitionFactory)getEFactoryInstance();
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
		documentRootEClass = createEClass(DOCUMENT_ROOT);
		createEReference(documentRootEClass, DOCUMENT_ROOT__SEQUENCE);

		sequenceEClass = createEClass(SEQUENCE);
		createEReference(sequenceEClass, SEQUENCE__REGION);
		createEAttribute(sequenceEClass, SEQUENCE__RUN_MODE);
		createEAttribute(sequenceEClass, SEQUENCE__NUM_ITERATIONS);
		createEAttribute(sequenceEClass, SEQUENCE__REPEAT_UNITIL_STOPPED);
		createEReference(sequenceEClass, SEQUENCE__SPECTRUM);
		createEAttribute(sequenceEClass, SEQUENCE__FILENAME);

		regionEClass = createEClass(REGION);
		createEAttribute(regionEClass, REGION__NAME);
		createEAttribute(regionEClass, REGION__LENSMODE);
		createEReference(regionEClass, REGION__RUN_MODE);
		createEAttribute(regionEClass, REGION__ACQUISITION_MODE);
		createEAttribute(regionEClass, REGION__ENERGY_MODE);
		createEReference(regionEClass, REGION__ENERGY);
		createEReference(regionEClass, REGION__STEP);
		createEReference(regionEClass, REGION__DETECTOR);
		createEAttribute(regionEClass, REGION__PASS_ENERGY);

		runModeEClass = createEClass(RUN_MODE);
		createEAttribute(runModeEClass, RUN_MODE__MODE);
		createEAttribute(runModeEClass, RUN_MODE__NUM_ITERATIONS);
		createEAttribute(runModeEClass, RUN_MODE__REPEAT_UNITIL_STOPPED);

		energyEClass = createEClass(ENERGY);
		createEAttribute(energyEClass, ENERGY__LOW);
		createEAttribute(energyEClass, ENERGY__HIGH);
		createEAttribute(energyEClass, ENERGY__CENTER);
		createEAttribute(energyEClass, ENERGY__WIDTH);

		stepEClass = createEClass(STEP);
		createEAttribute(stepEClass, STEP__FRAMES);
		createEAttribute(stepEClass, STEP__TIME);
		createEAttribute(stepEClass, STEP__SIZE);
		createEAttribute(stepEClass, STEP__TOTAL_TIME);
		createEAttribute(stepEClass, STEP__TOTAL_STEPS);

		detectorEClass = createEClass(DETECTOR);
		createEAttribute(detectorEClass, DETECTOR__FIRST_XCHANNEL);
		createEAttribute(detectorEClass, DETECTOR__LAST_XCHANNEL);
		createEAttribute(detectorEClass, DETECTOR__FIRST_YCHANNEL);
		createEAttribute(detectorEClass, DETECTOR__LAST_YCHANNEL);
		createEAttribute(detectorEClass, DETECTOR__SLICES);
		createEAttribute(detectorEClass, DETECTOR__DETECTOR_MODE);

		spectrumEClass = createEClass(SPECTRUM);
		createEAttribute(spectrumEClass, SPECTRUM__LOCATION);
		createEAttribute(spectrumEClass, SPECTRUM__USER);
		createEAttribute(spectrumEClass, SPECTRUM__SAMPLE_NAME);
		createEAttribute(spectrumEClass, SPECTRUM__FILENAME_PREFIX);
		createEAttribute(spectrumEClass, SPECTRUM__BASE_DIRECTORY);
		createEAttribute(spectrumEClass, SPECTRUM__FILENAME_FORMET);
		createEAttribute(spectrumEClass, SPECTRUM__FILE_EXTENSION);
		createEAttribute(spectrumEClass, SPECTRUM__NUMBER_OF_COMMENTS);
		createEAttribute(spectrumEClass, SPECTRUM__COMMENTS);

		// Create enums
		lenS_MODEEEnum = createEEnum(LENS_MODE);
		ruN_MODESEEnum = createEEnum(RUN_MODES);
		acquiaitioN_MODEEEnum = createEEnum(ACQUIAITION_MODE);
		energY_MODEEEnum = createEEnum(ENERGY_MODE);
		detectoR_MODEEEnum = createEEnum(DETECTOR_MODE);
		pasS_ENERGYEEnum = createEEnum(PASS_ENERGY);
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
		initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDocumentRoot_Sequence(), this.getSequence(), null, "sequence", null, 0, 1, DocumentRoot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sequenceEClass, Sequence.class, "Sequence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSequence_Region(), this.getRegion(), null, "region", null, 0, -1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSequence_RunMode(), this.getRUN_MODES(), "runMode", "", 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSequence_NumIterations(), ecorePackage.getEInt(), "numIterations", null, 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSequence_RepeatUnitilStopped(), ecorePackage.getEBoolean(), "repeatUnitilStopped", null, 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSequence_Spectrum(), this.getSpectrum(), null, "spectrum", null, 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSequence_Filename(), ecorePackage.getEString(), "filename", null, 0, 1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = addEOperation(sequenceEClass, this.getRegion(), "getRegion", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "regionName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(regionEClass, Region.class, "Region", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRegion_Name(), ecorePackage.getEString(), "name", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegion_Lensmode(), this.getLENS_MODE(), "lensmode", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRegion_RunMode(), this.getRunMode(), null, "runMode", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegion_AcquisitionMode(), this.getACQUIAITION_MODE(), "acquisitionMode", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegion_EnergyMode(), this.getENERGY_MODE(), "energyMode", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRegion_Energy(), this.getEnergy(), null, "energy", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRegion_Step(), this.getStep(), null, "step", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRegion_Detector(), this.getDetector(), null, "detector", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegion_PassEnergy(), this.getPASS_ENERGY(), "passEnergy", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(runModeEClass, RunMode.class, "RunMode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRunMode_Mode(), this.getRUN_MODES(), "mode", null, 0, 1, RunMode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRunMode_NumIterations(), ecorePackage.getEInt(), "numIterations", null, 0, 1, RunMode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRunMode_RepeatUnitilStopped(), ecorePackage.getEBoolean(), "repeatUnitilStopped", null, 0, 1, RunMode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(energyEClass, Energy.class, "Energy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEnergy_Low(), ecorePackage.getEDouble(), "low", null, 0, 1, Energy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEnergy_High(), ecorePackage.getEDouble(), "high", null, 0, 1, Energy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEnergy_Center(), ecorePackage.getEDouble(), "center", null, 0, 1, Energy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEnergy_Width(), ecorePackage.getEDouble(), "width", null, 0, 1, Energy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stepEClass, Step.class, "Step", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStep_Frames(), ecorePackage.getEInt(), "frames", null, 0, 1, Step.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStep_Time(), ecorePackage.getEDouble(), "time", null, 0, 1, Step.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStep_Size(), ecorePackage.getEDouble(), "size", null, 0, 1, Step.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStep_TotalTime(), ecorePackage.getEDouble(), "totalTime", null, 0, 1, Step.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStep_TotalSteps(), ecorePackage.getEInt(), "totalSteps", null, 0, 1, Step.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(detectorEClass, Detector.class, "Detector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDetector_FirstXChannel(), ecorePackage.getEInt(), "firstXChannel", null, 0, 1, Detector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetector_LastXChannel(), ecorePackage.getEInt(), "lastXChannel", null, 0, 1, Detector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetector_FirstYChannel(), ecorePackage.getEInt(), "firstYChannel", null, 0, 1, Detector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetector_LastYChannel(), ecorePackage.getEInt(), "lastYChannel", null, 0, 1, Detector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetector_Slices(), ecorePackage.getEInt(), "slices", null, 0, 1, Detector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDetector_DetectorMode(), this.getDETECTOR_MODE(), "detectorMode", null, 0, 1, Detector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(spectrumEClass, Spectrum.class, "Spectrum", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSpectrum_Location(), ecorePackage.getEString(), "location", "Diamond I09", 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_User(), ecorePackage.getEString(), "User", null, 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_SampleName(), ecorePackage.getEString(), "sampleName", null, 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_FilenamePrefix(), ecorePackage.getEString(), "filenamePrefix", null, 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_BaseDirectory(), ecorePackage.getEString(), "baseDirectory", null, 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_FilenameFormet(), ecorePackage.getEString(), "filenameFormet", "%s_%5d_%3d_%s", 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_FileExtension(), ecorePackage.getEString(), "fileExtension", ".txt", 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_NumberOfComments(), ecorePackage.getEInt(), "numberOfComments", null, 0, 1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSpectrum_Comments(), ecorePackage.getEString(), "comments", null, 0, -1, Spectrum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(lenS_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE.class, "LENS_MODE");
		addEEnumLiteral(lenS_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE.TRANSMISSION);
		addEEnumLiteral(lenS_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE.ANGULAR45);
		addEEnumLiteral(lenS_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE.ANGULAR60);

		initEEnum(ruN_MODESEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES.class, "RUN_MODES");
		addEEnumLiteral(ruN_MODESEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES.NORMAL);
		addEEnumLiteral(ruN_MODESEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES.ADD_DIMENSION);

		initEEnum(acquiaitioN_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE.class, "ACQUIAITION_MODE");
		addEEnumLiteral(acquiaitioN_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE.SWEPT);
		addEEnumLiteral(acquiaitioN_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE.FIXED);

		initEEnum(energY_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE.class, "ENERGY_MODE");
		addEEnumLiteral(energY_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE.KINETIC);
		addEEnumLiteral(energY_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE.BINDING);

		initEEnum(detectoR_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE.class, "DETECTOR_MODE");
		addEEnumLiteral(detectoR_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE.ADC);
		addEEnumLiteral(detectoR_MODEEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE.PULSE_COUNTING);

		initEEnum(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY.class, "PASS_ENERGY");
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._5);
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._10);
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._50);
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._75);
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._100);
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._200);
		addEEnumLiteral(pasS_ENERGYEEnum, org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY._500);

		// Create resource
		createResource(eNS_URI);
	}

} //RegiondefinitionPackageImpl
