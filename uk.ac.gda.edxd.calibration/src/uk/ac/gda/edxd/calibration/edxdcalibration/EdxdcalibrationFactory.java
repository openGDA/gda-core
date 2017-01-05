/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage
 * @generated
 */
public interface EdxdcalibrationFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	EdxdcalibrationFactory eINSTANCE = uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Edxd Calibration</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Edxd Calibration</em>'.
	 * @generated
	 */
	EdxdCalibration createEdxdCalibration();

	/**
	 * Returns a new object of class '<em>Calibration Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Calibration Config</em>'.
	 * @generated
	 */
	CalibrationConfig createCalibrationConfig();

	/**
	 * Returns a new object of class '<em>Document Root</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Document Root</em>'.
	 * @generated
	 */
	DocumentRoot createDocumentRoot();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	EdxdcalibrationPackage getEdxdcalibrationPackage();

} //EdxdcalibrationFactory
