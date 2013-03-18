/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reconstruction Detail</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileName <em>Nexus File Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileLocation <em>Nexus File Location</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getReconstructedLocation <em>Reconstructed Location</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getTimeReconStarted <em>Time Recon Started</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconstructionDetail()
 * @model
 * @generated
 */
public interface ReconstructionDetail extends EObject {
	/**
	 * Returns the value of the '<em><b>Nexus File Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Nexus File Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nexus File Name</em>' attribute.
	 * @see #setNexusFileName(String)
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconstructionDetail_NexusFileName()
	 * @model
	 * @generated
	 */
	String getNexusFileName();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileName <em>Nexus File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nexus File Name</em>' attribute.
	 * @see #getNexusFileName()
	 * @generated
	 */
	void setNexusFileName(String value);

	/**
	 * Returns the value of the '<em><b>Nexus File Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Nexus File Location</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nexus File Location</em>' attribute.
	 * @see #setNexusFileLocation(String)
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconstructionDetail_NexusFileLocation()
	 * @model
	 * @generated
	 */
	String getNexusFileLocation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getNexusFileLocation <em>Nexus File Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nexus File Location</em>' attribute.
	 * @see #getNexusFileLocation()
	 * @generated
	 */
	void setNexusFileLocation(String value);

	/**
	 * Returns the value of the '<em><b>Reconstructed Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reconstructed Location</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reconstructed Location</em>' attribute.
	 * @see #setReconstructedLocation(String)
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconstructionDetail_ReconstructedLocation()
	 * @model
	 * @generated
	 */
	String getReconstructedLocation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getReconstructedLocation <em>Reconstructed Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reconstructed Location</em>' attribute.
	 * @see #getReconstructedLocation()
	 * @generated
	 */
	void setReconstructedLocation(String value);

	/**
	 * Returns the value of the '<em><b>Time Recon Started</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time Recon Started</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time Recon Started</em>' attribute.
	 * @see #setTimeReconStarted(String)
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconstructionDetail_TimeReconStarted()
	 * @model
	 * @generated
	 */
	String getTimeReconStarted();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail#getTimeReconStarted <em>Time Recon Started</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time Recon Started</em>' attribute.
	 * @see #getTimeReconStarted()
	 * @generated
	 */
	void setTimeReconStarted(String value);

} // ReconstructionDetail
