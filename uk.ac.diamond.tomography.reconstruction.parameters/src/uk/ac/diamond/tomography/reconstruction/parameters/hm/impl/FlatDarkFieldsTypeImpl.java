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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Flat Dark Fields Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatDarkFieldsTypeImpl#getFlatField <em>Flat Field</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatDarkFieldsTypeImpl#getDarkField <em>Dark Field</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FlatDarkFieldsTypeImpl extends EObjectImpl implements FlatDarkFieldsType {
	/**
	 * The cached value of the '{@link #getFlatField() <em>Flat Field</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatField()
	 * @generated
	 * @ordered
	 */
	protected FlatFieldType flatField;

	/**
	 * The cached value of the '{@link #getDarkField() <em>Dark Field</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDarkField()
	 * @generated
	 * @ordered
	 */
	protected DarkFieldType darkField;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FlatDarkFieldsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.FLAT_DARK_FIELDS_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FlatFieldType getFlatField() {
		return flatField;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFlatField(FlatFieldType newFlatField, NotificationChain msgs) {
		FlatFieldType oldFlatField = flatField;
		flatField = newFlatField;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD, oldFlatField, newFlatField);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFlatField(FlatFieldType newFlatField) {
		if (newFlatField != flatField) {
			NotificationChain msgs = null;
			if (flatField != null)
				msgs = ((InternalEObject)flatField).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD, null, msgs);
			if (newFlatField != null)
				msgs = ((InternalEObject)newFlatField).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD, null, msgs);
			msgs = basicSetFlatField(newFlatField, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD, newFlatField, newFlatField));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DarkFieldType getDarkField() {
		return darkField;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDarkField(DarkFieldType newDarkField, NotificationChain msgs) {
		DarkFieldType oldDarkField = darkField;
		darkField = newDarkField;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD, oldDarkField, newDarkField);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDarkField(DarkFieldType newDarkField) {
		if (newDarkField != darkField) {
			NotificationChain msgs = null;
			if (darkField != null)
				msgs = ((InternalEObject)darkField).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD, null, msgs);
			if (newDarkField != null)
				msgs = ((InternalEObject)newDarkField).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD, null, msgs);
			msgs = basicSetDarkField(newDarkField, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD, newDarkField, newDarkField));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD:
				return basicSetFlatField(null, msgs);
			case HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD:
				return basicSetDarkField(null, msgs);
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
			case HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD:
				return getFlatField();
			case HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD:
				return getDarkField();
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
			case HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD:
				setFlatField((FlatFieldType)newValue);
				return;
			case HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD:
				setDarkField((DarkFieldType)newValue);
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
			case HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD:
				setFlatField((FlatFieldType)null);
				return;
			case HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD:
				setDarkField((DarkFieldType)null);
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
			case HmPackage.FLAT_DARK_FIELDS_TYPE__FLAT_FIELD:
				return flatField != null;
			case HmPackage.FLAT_DARK_FIELDS_TYPE__DARK_FIELD:
				return darkField != null;
		}
		return super.eIsSet(featureID);
	}

} //FlatDarkFieldsTypeImpl
