/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Calibration Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.CalibrationConfigImpl#getFileName <em>File Name</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.CalibrationConfigImpl#getLastCalibrated <em>Last Calibrated</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CalibrationConfigImpl extends EObjectImpl implements CalibrationConfig {
	/**
	 * The default value of the '{@link #getFileName() <em>File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileName()
	 * @generated
	 * @ordered
	 */
	protected static final String FILE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFileName() <em>File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileName()
	 * @generated
	 * @ordered
	 */
	protected String fileName = FILE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastCalibrated() <em>Last Calibrated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastCalibrated()
	 * @generated
	 * @ordered
	 */
	protected static final String LAST_CALIBRATED_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastCalibrated() <em>Last Calibrated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastCalibrated()
	 * @generated
	 * @ordered
	 */
	protected String lastCalibrated = LAST_CALIBRATED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CalibrationConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EdxdcalibrationPackage.Literals.CALIBRATION_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFileName() {
		return fileName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFileName(String newFileName) {
		String oldFileName = fileName;
		fileName = newFileName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.CALIBRATION_CONFIG__FILE_NAME, oldFileName, fileName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLastCalibrated() {
		return lastCalibrated;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastCalibrated(String newLastCalibrated) {
		String oldLastCalibrated = lastCalibrated;
		lastCalibrated = newLastCalibrated;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.CALIBRATION_CONFIG__LAST_CALIBRATED, oldLastCalibrated, lastCalibrated));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__FILE_NAME:
				return getFileName();
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__LAST_CALIBRATED:
				return getLastCalibrated();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__FILE_NAME:
				setFileName((String)newValue);
				return;
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__LAST_CALIBRATED:
				setLastCalibrated((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__FILE_NAME:
				setFileName(FILE_NAME_EDEFAULT);
				return;
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__LAST_CALIBRATED:
				setLastCalibrated(LAST_CALIBRATED_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__FILE_NAME:
				return FILE_NAME_EDEFAULT == null ? fileName != null : !FILE_NAME_EDEFAULT.equals(fileName);
			case EdxdcalibrationPackage.CALIBRATION_CONFIG__LAST_CALIBRATED:
				return LAST_CALIBRATED_EDEFAULT == null ? lastCalibrated != null : !LAST_CALIBRATED_EDEFAULT.equals(lastCalibrated);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (fileName: ");
		result.append(fileName);
		result.append(", lastCalibrated: ");
		result.append(lastCalibrated);
		result.append(')');
		return result.toString();
	}

} //CalibrationConfigImpl
