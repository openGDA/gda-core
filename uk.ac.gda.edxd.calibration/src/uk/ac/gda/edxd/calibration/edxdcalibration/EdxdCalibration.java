/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Edxd Calibration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getHutch <em>Hutch</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getCollimator <em>Collimator</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getEnergyCalibration <em>Energy Calibration</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getQCalibration <em>QCalibration</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getEdxdCalibration()
 * @model
 * @generated
 */
public interface EdxdCalibration extends EObject {
	/**
	 * Returns the value of the '<em><b>Hutch</b></em>' attribute.
	 * The literals are from the enumeration {@link uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hutch</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Hutch</em>' attribute.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH
	 * @see #setHutch(HUTCH)
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getEdxdCalibration_Hutch()
	 * @model
	 * @generated
	 */
	HUTCH getHutch();

	/**
	 * Sets the value of the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getHutch <em>Hutch</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hutch</em>' attribute.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH
	 * @see #getHutch()
	 * @generated
	 */
	void setHutch(HUTCH value);

	/**
	 * Returns the value of the '<em><b>Collimator</b></em>' attribute.
	 * The literals are from the enumeration {@link uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Collimator</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Collimator</em>' attribute.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR
	 * @see #setCollimator(COLLIMATOR)
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getEdxdCalibration_Collimator()
	 * @model
	 * @generated
	 */
	COLLIMATOR getCollimator();

	/**
	 * Sets the value of the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getCollimator <em>Collimator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Collimator</em>' attribute.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR
	 * @see #getCollimator()
	 * @generated
	 */
	void setCollimator(COLLIMATOR value);

	/**
	 * Returns the value of the '<em><b>Energy Calibration</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy Calibration</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy Calibration</em>' containment reference.
	 * @see #setEnergyCalibration(CalibrationConfig)
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getEdxdCalibration_EnergyCalibration()
	 * @model containment="true"
	 * @generated
	 */
	CalibrationConfig getEnergyCalibration();

	/**
	 * Sets the value of the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getEnergyCalibration <em>Energy Calibration</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy Calibration</em>' containment reference.
	 * @see #getEnergyCalibration()
	 * @generated
	 */
	void setEnergyCalibration(CalibrationConfig value);

	/**
	 * Returns the value of the '<em><b>QCalibration</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>QCalibration</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>QCalibration</em>' containment reference.
	 * @see #setQCalibration(CalibrationConfig)
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getEdxdCalibration_QCalibration()
	 * @model containment="true"
	 * @generated
	 */
	CalibrationConfig getQCalibration();

	/**
	 * Sets the value of the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getQCalibration <em>QCalibration</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>QCalibration</em>' containment reference.
	 * @see #getQCalibration()
	 * @generated
	 */
	void setQCalibration(CalibrationConfig value);

} // EdxdCalibration
