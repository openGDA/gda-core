/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Recon Results</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults#getReconresult <em>Reconresult</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconResults()
 * @model
 * @generated
 */
public interface ReconResults extends EObject {
	/**
	 * Returns the value of the '<em><b>Reconresult</b></em>' containment reference list.
	 * The list contents are of type {@link uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reconresult</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reconresult</em>' containment reference list.
	 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage#getReconResults_Reconresult()
	 * @model containment="true"
	 * @generated
	 */
	EList<ReconstructionDetail> getReconresult();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	ReconstructionDetail getReconstructionDetail(String nexusFullFileLocation);

} // ReconResults
