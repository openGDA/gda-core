/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage
 * @generated
 */
public interface RegiondefinitionFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RegiondefinitionFactory eINSTANCE = com.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Document Root</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Document Root</em>'.
	 * @generated
	 */
	DocumentRoot createDocumentRoot();

	/**
	 * Returns a new object of class '<em>Region</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Region</em>'.
	 * @generated
	 */
	Region createRegion();

	/**
	 * Returns a new object of class '<em>Run Mode</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Run Mode</em>'.
	 * @generated
	 */
	RunMode createRunMode();

	/**
	 * Returns a new object of class '<em>Sequence</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sequence</em>'.
	 * @generated
	 */
	Sequence createSequence();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	RegiondefinitionPackage getRegiondefinitionPackage();

} //RegiondefinitionFactory
