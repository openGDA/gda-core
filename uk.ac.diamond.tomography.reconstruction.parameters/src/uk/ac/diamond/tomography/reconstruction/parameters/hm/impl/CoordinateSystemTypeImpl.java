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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Coordinate System Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl#getSlice <em>Slice</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CoordinateSystemTypeImpl extends EObjectImpl implements CoordinateSystemType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType9 type;

	/**
	 * The default value of the '{@link #getSlice() <em>Slice</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSlice()
	 * @generated
	 * @ordered
	 */
	protected static final String SLICE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSlice() <em>Slice</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSlice()
	 * @generated
	 * @ordered
	 */
	protected String slice = SLICE_EDEFAULT;

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
	protected CoordinateSystemTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.COORDINATE_SYSTEM_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType9 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType9 newType, NotificationChain msgs) {
		TypeType9 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.COORDINATE_SYSTEM_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType9 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.COORDINATE_SYSTEM_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.COORDINATE_SYSTEM_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.COORDINATE_SYSTEM_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSlice() {
		return slice;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSlice(String newSlice) {
		String oldSlice = slice;
		slice = newSlice;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.COORDINATE_SYSTEM_TYPE__SLICE, oldSlice, slice));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.COORDINATE_SYSTEM_TYPE__DONE, oldDone, done));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.COORDINATE_SYSTEM_TYPE__TYPE:
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
			case HmPackage.COORDINATE_SYSTEM_TYPE__TYPE:
				return getType();
			case HmPackage.COORDINATE_SYSTEM_TYPE__SLICE:
				return getSlice();
			case HmPackage.COORDINATE_SYSTEM_TYPE__DONE:
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
			case HmPackage.COORDINATE_SYSTEM_TYPE__TYPE:
				setType((TypeType9)newValue);
				return;
			case HmPackage.COORDINATE_SYSTEM_TYPE__SLICE:
				setSlice((String)newValue);
				return;
			case HmPackage.COORDINATE_SYSTEM_TYPE__DONE:
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
			case HmPackage.COORDINATE_SYSTEM_TYPE__TYPE:
				setType((TypeType9)null);
				return;
			case HmPackage.COORDINATE_SYSTEM_TYPE__SLICE:
				setSlice(SLICE_EDEFAULT);
				return;
			case HmPackage.COORDINATE_SYSTEM_TYPE__DONE:
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
			case HmPackage.COORDINATE_SYSTEM_TYPE__TYPE:
				return type != null;
			case HmPackage.COORDINATE_SYSTEM_TYPE__SLICE:
				return SLICE_EDEFAULT == null ? slice != null : !SLICE_EDEFAULT.equals(slice);
			case HmPackage.COORDINATE_SYSTEM_TYPE__DONE:
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
		result.append(" (slice: ");
		result.append(slice);
		result.append(", done: ");
		result.append(done);
		result.append(')');
		return result.toString();
	}

} //CoordinateSystemTypeImpl
