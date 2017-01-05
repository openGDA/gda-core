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
 * A representation of the model object '<em><b>Calibration Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getFileName <em>File Name</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getLastCalibrated <em>Last Calibrated</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getCalibrationConfig()
 * @model
 * @generated
 */
public interface CalibrationConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>File Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Name</em>' attribute.
	 * @see #setFileName(String)
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getCalibrationConfig_FileName()
	 * @model
	 * @generated
	 */
	String getFileName();

	/**
	 * Sets the value of the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getFileName <em>File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Name</em>' attribute.
	 * @see #getFileName()
	 * @generated
	 */
	void setFileName(String value);

	/**
	 * Returns the value of the '<em><b>Last Calibrated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last Calibrated</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Calibrated</em>' attribute.
	 * @see #setLastCalibrated(String)
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getCalibrationConfig_LastCalibrated()
	 * @model
	 * @generated
	 */
	String getLastCalibrated();

	/**
	 * Sets the value of the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getLastCalibrated <em>Last Calibrated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Calibrated</em>' attribute.
	 * @see #getLastCalibrated()
	 * @generated
	 */
	void setLastCalibrated(String value);

} // CalibrationConfig
