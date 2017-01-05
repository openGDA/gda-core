/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR;
import uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage;
import uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Edxd Calibration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl#getHutch <em>Hutch</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl#getCollimator <em>Collimator</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl#getEnergyCalibration <em>Energy Calibration</em>}</li>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl#getQCalibration <em>QCalibration</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class EdxdCalibrationImpl extends EObjectImpl implements EdxdCalibration {
	/**
	 * The default value of the '{@link #getHutch() <em>Hutch</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHutch()
	 * @generated
	 * @ordered
	 */
	protected static final HUTCH HUTCH_EDEFAULT = HUTCH.HUTCH1;

	/**
	 * The cached value of the '{@link #getHutch() <em>Hutch</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHutch()
	 * @generated
	 * @ordered
	 */
	protected HUTCH hutch = HUTCH_EDEFAULT;

	/**
	 * The default value of the '{@link #getCollimator() <em>Collimator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCollimator()
	 * @generated
	 * @ordered
	 */
	protected static final COLLIMATOR COLLIMATOR_EDEFAULT = COLLIMATOR.COLLIMATOR1;

	/**
	 * The cached value of the '{@link #getCollimator() <em>Collimator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCollimator()
	 * @generated
	 * @ordered
	 */
	protected COLLIMATOR collimator = COLLIMATOR_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEnergyCalibration() <em>Energy Calibration</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergyCalibration()
	 * @generated
	 * @ordered
	 */
	protected CalibrationConfig energyCalibration;

	/**
	 * The cached value of the '{@link #getQCalibration() <em>QCalibration</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQCalibration()
	 * @generated
	 * @ordered
	 */
	protected CalibrationConfig qCalibration;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EdxdCalibrationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EdxdcalibrationPackage.Literals.EDXD_CALIBRATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HUTCH getHutch() {
		return hutch;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHutch(HUTCH newHutch) {
		HUTCH oldHutch = hutch;
		hutch = newHutch == null ? HUTCH_EDEFAULT : newHutch;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.EDXD_CALIBRATION__HUTCH, oldHutch, hutch));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public COLLIMATOR getCollimator() {
		return collimator;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCollimator(COLLIMATOR newCollimator) {
		COLLIMATOR oldCollimator = collimator;
		collimator = newCollimator == null ? COLLIMATOR_EDEFAULT : newCollimator;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.EDXD_CALIBRATION__COLLIMATOR, oldCollimator, collimator));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CalibrationConfig getEnergyCalibration() {
		return energyCalibration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetEnergyCalibration(CalibrationConfig newEnergyCalibration, NotificationChain msgs) {
		CalibrationConfig oldEnergyCalibration = energyCalibration;
		energyCalibration = newEnergyCalibration;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION, oldEnergyCalibration, newEnergyCalibration);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnergyCalibration(CalibrationConfig newEnergyCalibration) {
		if (newEnergyCalibration != energyCalibration) {
			NotificationChain msgs = null;
			if (energyCalibration != null)
				msgs = ((InternalEObject)energyCalibration).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION, null, msgs);
			if (newEnergyCalibration != null)
				msgs = ((InternalEObject)newEnergyCalibration).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION, null, msgs);
			msgs = basicSetEnergyCalibration(newEnergyCalibration, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION, newEnergyCalibration, newEnergyCalibration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CalibrationConfig getQCalibration() {
		return qCalibration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetQCalibration(CalibrationConfig newQCalibration, NotificationChain msgs) {
		CalibrationConfig oldQCalibration = qCalibration;
		qCalibration = newQCalibration;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION, oldQCalibration, newQCalibration);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setQCalibration(CalibrationConfig newQCalibration) {
		if (newQCalibration != qCalibration) {
			NotificationChain msgs = null;
			if (qCalibration != null)
				msgs = ((InternalEObject)qCalibration).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION, null, msgs);
			if (newQCalibration != null)
				msgs = ((InternalEObject)newQCalibration).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION, null, msgs);
			msgs = basicSetQCalibration(newQCalibration, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION, newQCalibration, newQCalibration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION:
				return basicSetEnergyCalibration(null, msgs);
			case EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION:
				return basicSetQCalibration(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EdxdcalibrationPackage.EDXD_CALIBRATION__HUTCH:
				return getHutch();
			case EdxdcalibrationPackage.EDXD_CALIBRATION__COLLIMATOR:
				return getCollimator();
			case EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION:
				return getEnergyCalibration();
			case EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION:
				return getQCalibration();
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
			case EdxdcalibrationPackage.EDXD_CALIBRATION__HUTCH:
				setHutch((HUTCH)newValue);
				return;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__COLLIMATOR:
				setCollimator((COLLIMATOR)newValue);
				return;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION:
				setEnergyCalibration((CalibrationConfig)newValue);
				return;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION:
				setQCalibration((CalibrationConfig)newValue);
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
			case EdxdcalibrationPackage.EDXD_CALIBRATION__HUTCH:
				setHutch(HUTCH_EDEFAULT);
				return;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__COLLIMATOR:
				setCollimator(COLLIMATOR_EDEFAULT);
				return;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION:
				setEnergyCalibration((CalibrationConfig)null);
				return;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION:
				setQCalibration((CalibrationConfig)null);
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
			case EdxdcalibrationPackage.EDXD_CALIBRATION__HUTCH:
				return hutch != HUTCH_EDEFAULT;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__COLLIMATOR:
				return collimator != COLLIMATOR_EDEFAULT;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__ENERGY_CALIBRATION:
				return energyCalibration != null;
			case EdxdcalibrationPackage.EDXD_CALIBRATION__QCALIBRATION:
				return qCalibration != null;
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
		result.append(" (hutch: ");
		result.append(hutch);
		result.append(", collimator: ");
		result.append(collimator);
		result.append(')');
		return result.toString();
	}

} //EdxdCalibrationImpl
