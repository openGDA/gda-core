/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.SampleList;

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
	private EClass sampleListEClass = null;

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
	public EClass getExperimentDefinition() {
		return experimentDefinitionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getExperimentDefinition_Samplelist() {
		return (EReference)experimentDefinitionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSampleList() {
		return sampleListEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSampleList_Filename() {
		return (EAttribute)sampleListEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSampleList_Samples() {
		return (EReference)sampleListEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EOperation getSampleList__GetSampleById__String() {
		return sampleListEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EOperation getSampleList__GetSampleByName__String() {
		return sampleListEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSample() {
		return sampleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_SampleID() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Status() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Active() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Name() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_CellID() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_VisitID() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Calibrant() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Calibrant_x() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Calibrant_y() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Calibrant_exposure() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_x_start() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_x_stop() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_x_step() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_y_start() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_y_stop() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_y_step() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Sample_exposure() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Detector_x() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Detector_y() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Detector_z() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Email() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_StartDate() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_EndDate() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Command() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(23);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_DriveID() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(24);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_MailCount() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(25);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_DataFileCount() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(26);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_Comment() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(27);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSample_DataFilePath() {
		return (EAttribute)sampleEClass.getEStructuralFeatures().get(28);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getSTATUS() {
		return statusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
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
		createEReference(experimentDefinitionEClass, EXPERIMENT_DEFINITION__SAMPLELIST);

		sampleListEClass = createEClass(SAMPLE_LIST);
		createEAttribute(sampleListEClass, SAMPLE_LIST__FILENAME);
		createEReference(sampleListEClass, SAMPLE_LIST__SAMPLES);
		createEOperation(sampleListEClass, SAMPLE_LIST___GET_SAMPLE_BY_ID__STRING);
		createEOperation(sampleListEClass, SAMPLE_LIST___GET_SAMPLE_BY_NAME__STRING);

		sampleEClass = createEClass(SAMPLE);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_ID);
		createEAttribute(sampleEClass, SAMPLE__STATUS);
		createEAttribute(sampleEClass, SAMPLE__ACTIVE);
		createEAttribute(sampleEClass, SAMPLE__NAME);
		createEAttribute(sampleEClass, SAMPLE__CELL_ID);
		createEAttribute(sampleEClass, SAMPLE__VISIT_ID);
		createEAttribute(sampleEClass, SAMPLE__CALIBRANT);
		createEAttribute(sampleEClass, SAMPLE__CALIBRANT_X);
		createEAttribute(sampleEClass, SAMPLE__CALIBRANT_Y);
		createEAttribute(sampleEClass, SAMPLE__CALIBRANT_EXPOSURE);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_XSTART);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_XSTOP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_XSTEP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_YSTART);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_YSTOP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_YSTEP);
		createEAttribute(sampleEClass, SAMPLE__SAMPLE_EXPOSURE);
		createEAttribute(sampleEClass, SAMPLE__DETECTOR_X);
		createEAttribute(sampleEClass, SAMPLE__DETECTOR_Y);
		createEAttribute(sampleEClass, SAMPLE__DETECTOR_Z);
		createEAttribute(sampleEClass, SAMPLE__EMAIL);
		createEAttribute(sampleEClass, SAMPLE__START_DATE);
		createEAttribute(sampleEClass, SAMPLE__END_DATE);
		createEAttribute(sampleEClass, SAMPLE__COMMAND);
		createEAttribute(sampleEClass, SAMPLE__DRIVE_ID);
		createEAttribute(sampleEClass, SAMPLE__MAIL_COUNT);
		createEAttribute(sampleEClass, SAMPLE__DATA_FILE_COUNT);
		createEAttribute(sampleEClass, SAMPLE__COMMENT);
		createEAttribute(sampleEClass, SAMPLE__DATA_FILE_PATH);

		// Create enums
		statusEEnum = createEEnum(STATUS);
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
		initEReference(getExperimentDefinition_Samplelist(), this.getSampleList(), null, "samplelist", null, 0, 1, ExperimentDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sampleListEClass, SampleList.class, "SampleList", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSampleList_Filename(), ecorePackage.getEString(), "filename", "samples", 0, 1, SampleList.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSampleList_Samples(), this.getSample(), null, "samples", null, 0, -1, SampleList.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = initEOperation(getSampleList__GetSampleById__String(), this.getSample(), "getSampleById", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "regionID", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getSampleList__GetSampleByName__String(), this.getSample(), "getSampleByName", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "sampleName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(sampleEClass, Sample.class, "Sample", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSample_SampleID(), ecorePackage.getEString(), "sampleID", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Status(), this.getSTATUS(), "status", "READY", 0, 1, Sample.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Active(), ecorePackage.getEBoolean(), "active", "true", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Name(), ecorePackage.getEString(), "name", "new_sample", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_CellID(), ecorePackage.getEString(), "cellID", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_VisitID(), ecorePackage.getEString(), "visitID", "0-0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Calibrant(), ecorePackage.getEString(), "calibrant", "Si", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Calibrant_x(), ecorePackage.getEDouble(), "calibrant_x", "0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Calibrant_y(), ecorePackage.getEDouble(), "calibrant_y", "0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Calibrant_exposure(), ecorePackage.getEDouble(), "calibrant_exposure", "1.0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_x_start(), ecorePackage.getEDoubleObject(), "sample_x_start", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_x_stop(), ecorePackage.getEDoubleObject(), "sample_x_stop", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_x_step(), ecorePackage.getEDoubleObject(), "sample_x_step", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_y_start(), ecorePackage.getEDoubleObject(), "sample_y_start", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_y_stop(), ecorePackage.getEDoubleObject(), "sample_y_stop", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_y_step(), ecorePackage.getEDoubleObject(), "sample_y_step", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Sample_exposure(), ecorePackage.getEDouble(), "sample_exposure", "5.0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Detector_x(), ecorePackage.getEDouble(), "detector_x", "0.0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Detector_y(), ecorePackage.getEDouble(), "detector_y", "0.0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Detector_z(), ecorePackage.getEDouble(), "detector_z", "100.0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Email(), ecorePackage.getEString(), "email", "chiu.tang@diamond.ac.uk", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_StartDate(), ecorePackage.getEDate(), "startDate", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_EndDate(), ecorePackage.getEDate(), "endDate", null, 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Command(), ecorePackage.getEString(), "command", "parkingAll", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_DriveID(), ecorePackage.getEString(), "driveID", "i11-1", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_MailCount(), ecorePackage.getEInt(), "mailCount", "0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_DataFileCount(), ecorePackage.getEInt(), "dataFileCount", "0", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_Comment(), ecorePackage.getEString(), "comment", "Please add your comment here", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSample_DataFilePath(), ecorePackage.getEString(), "dataFilePath", "", 0, 1, Sample.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.class, "STATUS");
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.READY);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.RUNNING);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.PAUSED);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.COMPLETED);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.ABORTED);
		addEEnumLiteral(statusEEnum, org.opengda.lde.model.ldeexperiment.STATUS.ERROR);

		// Create resource
		createResource(eNS_URI);
	}

} //LDEExperimentsPackageImpl
