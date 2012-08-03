/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage
 * @generated
 */
public interface TomoParametersFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	TomoParametersFactory eINSTANCE = uk.ac.gda.tomography.parameters.impl.TomoParametersFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Tomo Experiment</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tomo Experiment</em>'.
	 * @generated
	 */
	TomoExperiment createTomoExperiment();

	/**
	 * Returns a new object of class '<em>Value Unit</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Value Unit</em>'.
	 * @generated
	 */
	ValueUnit createValueUnit();

	/**
	 * Returns a new object of class '<em>Detector Stage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Detector Stage</em>'.
	 * @generated
	 */
	DetectorStage createDetectorStage();

	/**
	 * Returns a new object of class '<em>Module</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Module</em>'.
	 * @generated
	 */
	Module createModule();

	/**
	 * Returns a new object of class '<em>Detector Bin</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Detector Bin</em>'.
	 * @generated
	 */
	DetectorBin createDetectorBin();

	/**
	 * Returns a new object of class '<em>Detector Properties</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Detector Properties</em>'.
	 * @generated
	 */
	DetectorProperties createDetectorProperties();

	/**
	 * Returns a new object of class '<em>Detector Roi</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Detector Roi</em>'.
	 * @generated
	 */
	DetectorRoi createDetectorRoi();

	/**
	 * Returns a new object of class '<em>Alignment Configuration</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Alignment Configuration</em>'.
	 * @generated
	 */
	AlignmentConfiguration createAlignmentConfiguration();

	/**
	 * Returns a new object of class '<em>Parameters</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameters</em>'.
	 * @generated
	 */
	Parameters createParameters();

	/**
	 * Returns a new object of class '<em>Sample Stage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sample Stage</em>'.
	 * @generated
	 */
	SampleStage createSampleStage();

	/**
	 * Returns a new object of class '<em>Stitch Parameters</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Stitch Parameters</em>'.
	 * @generated
	 */
	StitchParameters createStitchParameters();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	TomoParametersPackage getTomoParametersPackage();

} //TomoParametersFactory
