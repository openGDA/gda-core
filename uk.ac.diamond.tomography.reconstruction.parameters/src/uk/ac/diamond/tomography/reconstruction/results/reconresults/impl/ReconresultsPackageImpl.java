/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsFactory;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ReconresultsPackageImpl extends EPackageImpl implements ReconresultsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reconResultsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reconstructionDetailEClass = null;

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
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ReconresultsPackageImpl() {
		super(eNS_URI, ReconresultsFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ReconresultsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ReconresultsPackage init() {
		if (isInited) return (ReconresultsPackage)EPackage.Registry.INSTANCE.getEPackage(ReconresultsPackage.eNS_URI);

		// Obtain or create and register package
		ReconresultsPackageImpl theReconresultsPackage = (ReconresultsPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ReconresultsPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ReconresultsPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theReconresultsPackage.createPackageContents();

		// Initialize created meta-data
		theReconresultsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theReconresultsPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ReconresultsPackage.eNS_URI, theReconresultsPackage);
		return theReconresultsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getReconResults() {
		return reconResultsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getReconResults_Reconresult() {
		return (EReference)reconResultsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getReconstructionDetail() {
		return reconstructionDetailEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getReconstructionDetail_NexusFileName() {
		return (EAttribute)reconstructionDetailEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getReconstructionDetail_NexusFileLocation() {
		return (EAttribute)reconstructionDetailEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getReconstructionDetail_ReconstructedLocation() {
		return (EAttribute)reconstructionDetailEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getReconstructionDetail_TimeReconStarted() {
		return (EAttribute)reconstructionDetailEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReconresultsFactory getReconresultsFactory() {
		return (ReconresultsFactory)getEFactoryInstance();
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
		reconResultsEClass = createEClass(RECON_RESULTS);
		createEReference(reconResultsEClass, RECON_RESULTS__RECONRESULT);

		reconstructionDetailEClass = createEClass(RECONSTRUCTION_DETAIL);
		createEAttribute(reconstructionDetailEClass, RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME);
		createEAttribute(reconstructionDetailEClass, RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION);
		createEAttribute(reconstructionDetailEClass, RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION);
		createEAttribute(reconstructionDetailEClass, RECONSTRUCTION_DETAIL__TIME_RECON_STARTED);
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
		initEClass(reconResultsEClass, ReconResults.class, "ReconResults", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getReconResults_Reconresult(), this.getReconstructionDetail(), null, "reconresult", null, 0, -1, ReconResults.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = addEOperation(reconResultsEClass, this.getReconstructionDetail(), "getReconstructionDetail", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "nexusFullFileLocation", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(reconstructionDetailEClass, ReconstructionDetail.class, "ReconstructionDetail", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReconstructionDetail_NexusFileName(), ecorePackage.getEString(), "nexusFileName", null, 0, 1, ReconstructionDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReconstructionDetail_NexusFileLocation(), ecorePackage.getEString(), "nexusFileLocation", null, 0, 1, ReconstructionDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReconstructionDetail_ReconstructedLocation(), ecorePackage.getEString(), "reconstructedLocation", null, 0, 1, ReconstructionDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReconstructionDetail_TimeReconStarted(), ecorePackage.getEString(), "timeReconStarted", null, 0, 1, ReconstructionDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ReconresultsPackageImpl
