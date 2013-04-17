/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsFactory
 * @model kind="package"
 * @generated
 */
public interface ReconresultsPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "reconresults";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://diamond.org/reconresults";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "reconresults";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ReconresultsPackage eINSTANCE = uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconresultsPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconResultsImpl <em>Recon Results</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconResultsImpl
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconresultsPackageImpl#getReconResults()
	 * @generated
	 */
	int RECON_RESULTS = 0;

	/**
	 * The feature id for the '<em><b>Reconresult</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECON_RESULTS__RECONRESULT = 0;

	/**
	 * The number of structural features of the '<em>Recon Results</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECON_RESULTS_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl <em>Reconstruction Detail</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconresultsPackageImpl#getReconstructionDetail()
	 * @generated
	 */
	int RECONSTRUCTION_DETAIL = 1;

	/**
	 * The feature id for the '<em><b>Nexus File Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Nexus File Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION = 1;

	/**
	 * The feature id for the '<em><b>Reconstructed Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION = 2;

	/**
	 * The feature id for the '<em><b>Time Recon Started</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECONSTRUCTION_DETAIL__TIME_RECON_STARTED = 3;

	/**
	 * The number of structural features of the '<em>Reconstruction Detail</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RECONSTRUCTION_DETAIL_FEATURE_COUNT = 4;


	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults <em>Recon Results</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Recon Results</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults
	 * @generated
	 */
	EClass getReconResults();

	/**
	 * Returns the meta object for the containment reference list '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults#getReconresult <em>Reconresult</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Reconresult</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults#getReconresult()
	 * @see #getReconResults()
	 * @generated
	 */
	EReference getReconResults_Reconresult();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail <em>Reconstruction Detail</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Reconstruction Detail</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail
	 * @generated
	 */
	EClass getReconstructionDetail();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileName <em>Nexus File Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nexus File Name</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileName()
	 * @see #getReconstructionDetail()
	 * @generated
	 */
	EAttribute getReconstructionDetail_NexusFileName();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileLocation <em>Nexus File Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nexus File Location</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileLocation()
	 * @see #getReconstructionDetail()
	 * @generated
	 */
	EAttribute getReconstructionDetail_NexusFileLocation();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getReconstructedLocation <em>Reconstructed Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reconstructed Location</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getReconstructedLocation()
	 * @see #getReconstructionDetail()
	 * @generated
	 */
	EAttribute getReconstructionDetail_ReconstructedLocation();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getTimeReconStarted <em>Time Recon Started</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time Recon Started</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getTimeReconStarted()
	 * @see #getReconstructionDetail()
	 * @generated
	 */
	EAttribute getReconstructionDetail_TimeReconStarted();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ReconresultsFactory getReconresultsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconResultsImpl <em>Recon Results</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconResultsImpl
		 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconresultsPackageImpl#getReconResults()
		 * @generated
		 */
		EClass RECON_RESULTS = eINSTANCE.getReconResults();

		/**
		 * The meta object literal for the '<em><b>Reconresult</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RECON_RESULTS__RECONRESULT = eINSTANCE.getReconResults_Reconresult();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl <em>Reconstruction Detail</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl
		 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconresultsPackageImpl#getReconstructionDetail()
		 * @generated
		 */
		EClass RECONSTRUCTION_DETAIL = eINSTANCE.getReconstructionDetail();

		/**
		 * The meta object literal for the '<em><b>Nexus File Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME = eINSTANCE.getReconstructionDetail_NexusFileName();

		/**
		 * The meta object literal for the '<em><b>Nexus File Location</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION = eINSTANCE.getReconstructionDetail_NexusFileLocation();

		/**
		 * The meta object literal for the '<em><b>Reconstructed Location</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION = eINSTANCE.getReconstructionDetail_ReconstructedLocation();

		/**
		 * The meta object literal for the '<em><b>Time Recon Started</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RECONSTRUCTION_DETAIL__TIME_RECON_STARTED = eINSTANCE.getReconstructionDetail_TimeReconStarted();

	}

} //ReconresultsPackage
