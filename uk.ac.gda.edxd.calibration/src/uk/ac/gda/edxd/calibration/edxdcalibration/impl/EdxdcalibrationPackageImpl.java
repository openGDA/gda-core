/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig;
import uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationFactory;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class EdxdcalibrationPackageImpl extends EPackageImpl implements EdxdcalibrationPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass edxdCalibrationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass calibrationConfigEClass = null;

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
	private EEnum hutchEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum collimatorEEnum = null;

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
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private EdxdcalibrationPackageImpl() {
		super(eNS_URI, EdxdcalibrationFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link EdxdcalibrationPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static EdxdcalibrationPackage init() {
		if (isInited) return (EdxdcalibrationPackage)EPackage.Registry.INSTANCE.getEPackage(EdxdcalibrationPackage.eNS_URI);

		// Obtain or create and register package
		EdxdcalibrationPackageImpl theEdxdcalibrationPackage = (EdxdcalibrationPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof EdxdcalibrationPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new EdxdcalibrationPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theEdxdcalibrationPackage.createPackageContents();

		// Initialize created meta-data
		theEdxdcalibrationPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theEdxdcalibrationPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(EdxdcalibrationPackage.eNS_URI, theEdxdcalibrationPackage);
		return theEdxdcalibrationPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEdxdCalibration() {
		return edxdCalibrationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEdxdCalibration_Hutch() {
		return (EAttribute)edxdCalibrationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEdxdCalibration_Collimator() {
		return (EAttribute)edxdCalibrationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEdxdCalibration_EnergyCalibration() {
		return (EReference)edxdCalibrationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEdxdCalibration_QCalibration() {
		return (EReference)edxdCalibrationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCalibrationConfig() {
		return calibrationConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCalibrationConfig_FileName() {
		return (EAttribute)calibrationConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCalibrationConfig_LastCalibrated() {
		return (EAttribute)calibrationConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDocumentRoot() {
		return documentRootEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentRoot_EdxdCalibration() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getHUTCH() {
		return hutchEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getCOLLIMATOR() {
		return collimatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EdxdcalibrationFactory getEdxdcalibrationFactory() {
		return (EdxdcalibrationFactory)getEFactoryInstance();
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
		calibrationConfigEClass = createEClass(CALIBRATION_CONFIG);
		createEAttribute(calibrationConfigEClass, CALIBRATION_CONFIG__FILE_NAME);
		createEAttribute(calibrationConfigEClass, CALIBRATION_CONFIG__LAST_CALIBRATED);

		documentRootEClass = createEClass(DOCUMENT_ROOT);
		createEReference(documentRootEClass, DOCUMENT_ROOT__EDXD_CALIBRATION);

		edxdCalibrationEClass = createEClass(EDXD_CALIBRATION);
		createEAttribute(edxdCalibrationEClass, EDXD_CALIBRATION__HUTCH);
		createEAttribute(edxdCalibrationEClass, EDXD_CALIBRATION__COLLIMATOR);
		createEReference(edxdCalibrationEClass, EDXD_CALIBRATION__ENERGY_CALIBRATION);
		createEReference(edxdCalibrationEClass, EDXD_CALIBRATION__QCALIBRATION);

		// Create enums
		hutchEEnum = createEEnum(HUTCH);
		collimatorEEnum = createEEnum(COLLIMATOR);
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
		initEClass(calibrationConfigEClass, CalibrationConfig.class, "CalibrationConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCalibrationConfig_FileName(), ecorePackage.getEString(), "fileName", null, 0, 1, CalibrationConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCalibrationConfig_LastCalibrated(), ecorePackage.getEString(), "lastCalibrated", null, 0, 1, CalibrationConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDocumentRoot_EdxdCalibration(), this.getEdxdCalibration(), null, "edxdCalibration", null, 0, -1, DocumentRoot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(edxdCalibrationEClass, EdxdCalibration.class, "EdxdCalibration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEdxdCalibration_Hutch(), this.getHUTCH(), "hutch", null, 0, 1, EdxdCalibration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEdxdCalibration_Collimator(), this.getCOLLIMATOR(), "collimator", null, 0, 1, EdxdCalibration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEdxdCalibration_EnergyCalibration(), this.getCalibrationConfig(), null, "energyCalibration", null, 0, 1, EdxdCalibration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEdxdCalibration_QCalibration(), this.getCalibrationConfig(), null, "qCalibration", null, 0, 1, EdxdCalibration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(hutchEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH.class, "HUTCH");
		addEEnumLiteral(hutchEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH.HUTCH1);
		addEEnumLiteral(hutchEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH.HUTCH2);

		initEEnum(collimatorEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR.class, "COLLIMATOR");
		addEEnumLiteral(collimatorEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR.COLLIMATOR1);
		addEEnumLiteral(collimatorEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR.COLLIMATOR2);
		addEEnumLiteral(collimatorEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR.COLLIMATOR3);
		addEEnumLiteral(collimatorEEnum, uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR.COLLIMATOR4);

		// Create resource
		createResource(eNS_URI);
	}

} //EdxdcalibrationPackageImpl
