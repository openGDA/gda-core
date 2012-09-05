/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tilt Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl#getXTilt <em>XTilt</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl#getZTilt <em>ZTilt</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TiltTypeImpl extends EObjectImpl implements TiltType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType8 type;

	/**
	 * The default value of the '{@link #getXTilt() <em>XTilt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXTilt()
	 * @generated
	 * @ordered
	 */
	protected static final String XTILT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getXTilt() <em>XTilt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXTilt()
	 * @generated
	 * @ordered
	 */
	protected String xTilt = XTILT_EDEFAULT;

	/**
	 * The default value of the '{@link #getZTilt() <em>ZTilt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZTilt()
	 * @generated
	 * @ordered
	 */
	protected static final String ZTILT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getZTilt() <em>ZTilt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZTilt()
	 * @generated
	 * @ordered
	 */
	protected String zTilt = ZTILT_EDEFAULT;

	/**
	 * The default value of the '{@link #getDone() <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDone()
	 * @generated
	 * @ordered
	 */
	protected static final String DONE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDone() <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDone()
	 * @generated
	 * @ordered
	 */
	protected String done = DONE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TiltTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.TILT_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType8 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType8 newType, NotificationChain msgs) {
		TypeType8 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TILT_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType8 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TILT_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TILT_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TILT_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getXTilt() {
		return xTilt;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setXTilt(String newXTilt) {
		String oldXTilt = xTilt;
		xTilt = newXTilt;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TILT_TYPE__XTILT, oldXTilt, xTilt));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getZTilt() {
		return zTilt;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setZTilt(String newZTilt) {
		String oldZTilt = zTilt;
		zTilt = newZTilt;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TILT_TYPE__ZTILT, oldZTilt, zTilt));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDone() {
		return done;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDone(String newDone) {
		String oldDone = done;
		done = newDone;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TILT_TYPE__DONE, oldDone, done));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.TILT_TYPE__TYPE:
				return basicSetType(null, msgs);
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
			case HmPackage.TILT_TYPE__TYPE:
				return getType();
			case HmPackage.TILT_TYPE__XTILT:
				return getXTilt();
			case HmPackage.TILT_TYPE__ZTILT:
				return getZTilt();
			case HmPackage.TILT_TYPE__DONE:
				return getDone();
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
			case HmPackage.TILT_TYPE__TYPE:
				setType((TypeType8)newValue);
				return;
			case HmPackage.TILT_TYPE__XTILT:
				setXTilt((String)newValue);
				return;
			case HmPackage.TILT_TYPE__ZTILT:
				setZTilt((String)newValue);
				return;
			case HmPackage.TILT_TYPE__DONE:
				setDone((String)newValue);
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
			case HmPackage.TILT_TYPE__TYPE:
				setType((TypeType8)null);
				return;
			case HmPackage.TILT_TYPE__XTILT:
				setXTilt(XTILT_EDEFAULT);
				return;
			case HmPackage.TILT_TYPE__ZTILT:
				setZTilt(ZTILT_EDEFAULT);
				return;
			case HmPackage.TILT_TYPE__DONE:
				setDone(DONE_EDEFAULT);
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
			case HmPackage.TILT_TYPE__TYPE:
				return type != null;
			case HmPackage.TILT_TYPE__XTILT:
				return XTILT_EDEFAULT == null ? xTilt != null : !XTILT_EDEFAULT.equals(xTilt);
			case HmPackage.TILT_TYPE__ZTILT:
				return ZTILT_EDEFAULT == null ? zTilt != null : !ZTILT_EDEFAULT.equals(zTilt);
			case HmPackage.TILT_TYPE__DONE:
				return DONE_EDEFAULT == null ? done != null : !DONE_EDEFAULT.equals(done);
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
		result.append(" (xTilt: ");
		result.append(xTilt);
		result.append(", zTilt: ");
		result.append(zTilt);
		result.append(", done: ");
		result.append(done);
		result.append(')');
		return result.toString();
	}

} //TiltTypeImpl
