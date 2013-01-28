/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition.impl;

import com.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot;
import com.opengda.detector.electronalyser.server.model.regiondefinition.Region;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionFactory;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode;

import com.opengda.detector.electronalyser.server.model.regiondefinition.Sequence;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

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
	private EClass sequenceEClass = null;

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
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#eNS_URI
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
		return (EAttribute)regionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRegion_RunMode() {
		return (EReference)regionEClass.getEStructuralFeatures().get(3);
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

		regionEClass = createEClass(REGION);
		createEAttribute(regionEClass, REGION__NAME);
		createEAttribute(regionEClass, REGION__LENSMODE);
		createEAttribute(regionEClass, REGION__PASS_ENERGY);
		createEReference(regionEClass, REGION__RUN_MODE);

		runModeEClass = createEClass(RUN_MODE);
		createEAttribute(runModeEClass, RUN_MODE__MODE);
		createEAttribute(runModeEClass, RUN_MODE__NUM_ITERATIONS);
		createEAttribute(runModeEClass, RUN_MODE__REPEAT_UNITIL_STOPPED);

		sequenceEClass = createEClass(SEQUENCE);
		createEReference(sequenceEClass, SEQUENCE__REGION);

		// Create enums
		lenS_MODEEEnum = createEEnum(LENS_MODE);
		ruN_MODESEEnum = createEEnum(RUN_MODES);
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

		initEClass(regionEClass, Region.class, "Region", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRegion_Name(), ecorePackage.getEString(), "name", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegion_Lensmode(), this.getLENS_MODE(), "lensmode", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegion_PassEnergy(), ecorePackage.getEIntegerObject(), "passEnergy", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRegion_RunMode(), this.getRunMode(), null, "runMode", null, 0, 1, Region.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(runModeEClass, RunMode.class, "RunMode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRunMode_Mode(), this.getRUN_MODES(), "mode", null, 0, 1, RunMode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRunMode_NumIterations(), ecorePackage.getEBoolean(), "numIterations", null, 0, 1, RunMode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRunMode_RepeatUnitilStopped(), ecorePackage.getEBoolean(), "repeatUnitilStopped", null, 0, 1, RunMode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sequenceEClass, Sequence.class, "Sequence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSequence_Region(), this.getRegion(), null, "region", null, 0, -1, Sequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(lenS_MODEEEnum, com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE.class, "LENS_MODE");
		addEEnumLiteral(lenS_MODEEEnum, com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE.TRANSMISSION);

		initEEnum(ruN_MODESEEnum, com.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES.class, "RUN_MODES");
		addEEnumLiteral(ruN_MODESEEnum, com.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES.NORMAL);
		addEEnumLiteral(ruN_MODESEEnum, com.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES.ADD_DIMENSION);

		// Create resource
		createResource(eNS_URI);
	}

} //RegiondefinitionPackageImpl
