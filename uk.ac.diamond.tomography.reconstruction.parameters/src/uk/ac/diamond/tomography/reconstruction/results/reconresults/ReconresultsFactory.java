/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage
 * @generated
 */
public interface ReconresultsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ReconresultsFactory eINSTANCE = uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconresultsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Recon Results</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Recon Results</em>'.
	 * @generated
	 */
	ReconResults createReconResults();

	/**
	 * Returns a new object of class '<em>Reconstruction Detail</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reconstruction Detail</em>'.
	 * @generated
	 */
	ReconstructionDetail createReconstructionDetail();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ReconresultsPackage getReconresultsPackage();

} //ReconresultsFactory
