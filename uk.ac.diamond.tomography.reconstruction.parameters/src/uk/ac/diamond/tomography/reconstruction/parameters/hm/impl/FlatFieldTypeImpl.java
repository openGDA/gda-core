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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Flat Field Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getValueBefore <em>Value Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getValueAfter <em>Value After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getFileBefore <em>File Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getFileAfter <em>File After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getProfileType <em>Profile Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl#getFileProfile <em>File Profile</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FlatFieldTypeImpl extends EObjectImpl implements FlatFieldType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType15 type;

	/**
	 * The default value of the '{@link #getValueBefore() <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueBefore()
	 * @generated
	 * @ordered
	 */
	protected static final int VALUE_BEFORE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getValueBefore() <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueBefore()
	 * @generated
	 * @ordered
	 */
	protected int valueBefore = VALUE_BEFORE_EDEFAULT;

	/**
	 * This is true if the Value Before attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean valueBeforeESet;

	/**
	 * The default value of the '{@link #getValueAfter() <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueAfter()
	 * @generated
	 * @ordered
	 */
	protected static final int VALUE_AFTER_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getValueAfter() <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueAfter()
	 * @generated
	 * @ordered
	 */
	protected int valueAfter = VALUE_AFTER_EDEFAULT;

	/**
	 * This is true if the Value After attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean valueAfterESet;

	/**
	 * The default value of the '{@link #getFileBefore() <em>File Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileBefore()
	 * @generated
	 * @ordered
	 */
	protected static final String FILE_BEFORE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFileBefore() <em>File Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileBefore()
	 * @generated
	 * @ordered
	 */
	protected String fileBefore = FILE_BEFORE_EDEFAULT;

	/**
	 * The default value of the '{@link #getFileAfter() <em>File After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileAfter()
	 * @generated
	 * @ordered
	 */
	protected static final String FILE_AFTER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFileAfter() <em>File After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileAfter()
	 * @generated
	 * @ordered
	 */
	protected String fileAfter = FILE_AFTER_EDEFAULT;

	/**
	 * The cached value of the '{@link #getProfileType() <em>Profile Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProfileType()
	 * @generated
	 * @ordered
	 */
	protected ProfileTypeType1 profileType;

	/**
	 * The default value of the '{@link #getFileProfile() <em>File Profile</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileProfile()
	 * @generated
	 * @ordered
	 */
	protected static final String FILE_PROFILE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFileProfile() <em>File Profile</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileProfile()
	 * @generated
	 * @ordered
	 */
	protected String fileProfile = FILE_PROFILE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FlatFieldTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.FLAT_FIELD_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType15 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType15 newType, NotificationChain msgs) {
		TypeType15 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType15 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_FIELD_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_FIELD_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValueBefore() {
		return valueBefore;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueBefore(int newValueBefore) {
		int oldValueBefore = valueBefore;
		valueBefore = newValueBefore;
		boolean oldValueBeforeESet = valueBeforeESet;
		valueBeforeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__VALUE_BEFORE, oldValueBefore, valueBefore, !oldValueBeforeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetValueBefore() {
		int oldValueBefore = valueBefore;
		boolean oldValueBeforeESet = valueBeforeESet;
		valueBefore = VALUE_BEFORE_EDEFAULT;
		valueBeforeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.FLAT_FIELD_TYPE__VALUE_BEFORE, oldValueBefore, VALUE_BEFORE_EDEFAULT, oldValueBeforeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetValueBefore() {
		return valueBeforeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValueAfter() {
		return valueAfter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueAfter(int newValueAfter) {
		int oldValueAfter = valueAfter;
		valueAfter = newValueAfter;
		boolean oldValueAfterESet = valueAfterESet;
		valueAfterESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__VALUE_AFTER, oldValueAfter, valueAfter, !oldValueAfterESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetValueAfter() {
		int oldValueAfter = valueAfter;
		boolean oldValueAfterESet = valueAfterESet;
		valueAfter = VALUE_AFTER_EDEFAULT;
		valueAfterESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.FLAT_FIELD_TYPE__VALUE_AFTER, oldValueAfter, VALUE_AFTER_EDEFAULT, oldValueAfterESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetValueAfter() {
		return valueAfterESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFileBefore() {
		return fileBefore;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileBefore(String newFileBefore) {
		String oldFileBefore = fileBefore;
		fileBefore = newFileBefore;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__FILE_BEFORE, oldFileBefore, fileBefore));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFileAfter() {
		return fileAfter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileAfter(String newFileAfter) {
		String oldFileAfter = fileAfter;
		fileAfter = newFileAfter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__FILE_AFTER, oldFileAfter, fileAfter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProfileTypeType1 getProfileType() {
		return profileType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProfileType(ProfileTypeType1 newProfileType, NotificationChain msgs) {
		ProfileTypeType1 oldProfileType = profileType;
		profileType = newProfileType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE, oldProfileType, newProfileType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProfileType(ProfileTypeType1 newProfileType) {
		if (newProfileType != profileType) {
			NotificationChain msgs = null;
			if (profileType != null)
				msgs = ((InternalEObject)profileType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE, null, msgs);
			if (newProfileType != null)
				msgs = ((InternalEObject)newProfileType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE, null, msgs);
			msgs = basicSetProfileType(newProfileType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE, newProfileType, newProfileType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFileProfile() {
		return fileProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileProfile(String newFileProfile) {
		String oldFileProfile = fileProfile;
		fileProfile = newFileProfile;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FLAT_FIELD_TYPE__FILE_PROFILE, oldFileProfile, fileProfile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.FLAT_FIELD_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE:
				return basicSetProfileType(null, msgs);
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
			case HmPackage.FLAT_FIELD_TYPE__TYPE:
				return getType();
			case HmPackage.FLAT_FIELD_TYPE__VALUE_BEFORE:
				return getValueBefore();
			case HmPackage.FLAT_FIELD_TYPE__VALUE_AFTER:
				return getValueAfter();
			case HmPackage.FLAT_FIELD_TYPE__FILE_BEFORE:
				return getFileBefore();
			case HmPackage.FLAT_FIELD_TYPE__FILE_AFTER:
				return getFileAfter();
			case HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE:
				return getProfileType();
			case HmPackage.FLAT_FIELD_TYPE__FILE_PROFILE:
				return getFileProfile();
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
			case HmPackage.FLAT_FIELD_TYPE__TYPE:
				setType((TypeType15)newValue);
				return;
			case HmPackage.FLAT_FIELD_TYPE__VALUE_BEFORE:
				setValueBefore((Integer)newValue);
				return;
			case HmPackage.FLAT_FIELD_TYPE__VALUE_AFTER:
				setValueAfter((Integer)newValue);
				return;
			case HmPackage.FLAT_FIELD_TYPE__FILE_BEFORE:
				setFileBefore((String)newValue);
				return;
			case HmPackage.FLAT_FIELD_TYPE__FILE_AFTER:
				setFileAfter((String)newValue);
				return;
			case HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE:
				setProfileType((ProfileTypeType1)newValue);
				return;
			case HmPackage.FLAT_FIELD_TYPE__FILE_PROFILE:
				setFileProfile((String)newValue);
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
			case HmPackage.FLAT_FIELD_TYPE__TYPE:
				setType((TypeType15)null);
				return;
			case HmPackage.FLAT_FIELD_TYPE__VALUE_BEFORE:
				unsetValueBefore();
				return;
			case HmPackage.FLAT_FIELD_TYPE__VALUE_AFTER:
				unsetValueAfter();
				return;
			case HmPackage.FLAT_FIELD_TYPE__FILE_BEFORE:
				setFileBefore(FILE_BEFORE_EDEFAULT);
				return;
			case HmPackage.FLAT_FIELD_TYPE__FILE_AFTER:
				setFileAfter(FILE_AFTER_EDEFAULT);
				return;
			case HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE:
				setProfileType((ProfileTypeType1)null);
				return;
			case HmPackage.FLAT_FIELD_TYPE__FILE_PROFILE:
				setFileProfile(FILE_PROFILE_EDEFAULT);
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
			case HmPackage.FLAT_FIELD_TYPE__TYPE:
				return type != null;
			case HmPackage.FLAT_FIELD_TYPE__VALUE_BEFORE:
				return isSetValueBefore();
			case HmPackage.FLAT_FIELD_TYPE__VALUE_AFTER:
				return isSetValueAfter();
			case HmPackage.FLAT_FIELD_TYPE__FILE_BEFORE:
				return FILE_BEFORE_EDEFAULT == null ? fileBefore != null : !FILE_BEFORE_EDEFAULT.equals(fileBefore);
			case HmPackage.FLAT_FIELD_TYPE__FILE_AFTER:
				return FILE_AFTER_EDEFAULT == null ? fileAfter != null : !FILE_AFTER_EDEFAULT.equals(fileAfter);
			case HmPackage.FLAT_FIELD_TYPE__PROFILE_TYPE:
				return profileType != null;
			case HmPackage.FLAT_FIELD_TYPE__FILE_PROFILE:
				return FILE_PROFILE_EDEFAULT == null ? fileProfile != null : !FILE_PROFILE_EDEFAULT.equals(fileProfile);
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
		result.append(" (valueBefore: ");
		if (valueBeforeESet) result.append(valueBefore); else result.append("<unset>");
		result.append(", valueAfter: ");
		if (valueAfterESet) result.append(valueAfter); else result.append("<unset>");
		result.append(", fileBefore: ");
		result.append(fileBefore);
		result.append(", fileAfter: ");
		result.append(fileAfter);
		result.append(", fileProfile: ");
		result.append(fileProfile);
		result.append(')');
		return result.toString();
	}

} //FlatFieldTypeImpl
