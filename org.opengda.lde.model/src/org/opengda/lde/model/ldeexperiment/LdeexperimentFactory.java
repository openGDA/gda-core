/**
 */
package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage
 * @generated
 */
public interface LdeexperimentFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LdeexperimentFactory eINSTANCE = org.opengda.lde.model.ldeexperiment.impl.LdeexperimentFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Experiment Definition</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Experiment Definition</em>'.
	 * @generated
	 */
	ExperimentDefinition createExperimentDefinition();

	/**
	 * Returns a new object of class '<em>Sample List</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sample List</em>'.
	 * @generated
	 */
	SampleList createSampleList();

	/**
	 * Returns a new object of class '<em>Sample</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sample</em>'.
	 * @generated
	 */
	Sample createSample();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	LdeexperimentPackage getLdeexperimentPackage();

} //LdeexperimentFactory
