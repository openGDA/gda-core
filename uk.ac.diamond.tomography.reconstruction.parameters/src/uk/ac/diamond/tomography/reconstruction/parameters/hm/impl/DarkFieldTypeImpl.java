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
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dark Field Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getValueBefore <em>Value Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getValueAfter <em>Value After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getFileBefore <em>File Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getFileAfter <em>File After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getProfileType <em>Profile Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl#getFileProfile <em>File Profile</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DarkFieldTypeImpl extends EObjectImpl implements DarkFieldType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType13 type;

	/**
	 * The default value of the '{@link #getValueBefore() <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueBefore()
	 * @generated
	 * @ordered
	 */
	protected static final Double VALUE_BEFORE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValueBefore() <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueBefore()
	 * @generated
	 * @ordered
	 */
	protected Double valueBefore = VALUE_BEFORE_EDEFAULT;

	/**
	 * The default value of the '{@link #getValueAfter() <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueAfter()
	 * @generated
	 * @ordered
	 */
	protected static final Double VALUE_AFTER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValueAfter() <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueAfter()
	 * @generated
	 * @ordered
	 */
	protected Double valueAfter = VALUE_AFTER_EDEFAULT;

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
	protected ProfileTypeType profileType;

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
	protected DarkFieldTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.DARK_FIELD_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType13 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType13 newType, NotificationChain msgs) {
		TypeType13 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__TYPE, oldType, newType);
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
	public void setType(TypeType13 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.DARK_FIELD_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.DARK_FIELD_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Double getValueBefore() {
		return valueBefore;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValueBefore(Double newValueBefore) {
		Double oldValueBefore = valueBefore;
		valueBefore = newValueBefore;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__VALUE_BEFORE, oldValueBefore, valueBefore));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Double getValueAfter() {
		return valueAfter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValueAfter(Double newValueAfter) {
		Double oldValueAfter = valueAfter;
		valueAfter = newValueAfter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__VALUE_AFTER, oldValueAfter, valueAfter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFileBefore() {
		return fileBefore;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFileBefore(String newFileBefore) {
		String oldFileBefore = fileBefore;
		fileBefore = newFileBefore;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__FILE_BEFORE, oldFileBefore, fileBefore));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFileAfter() {
		return fileAfter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFileAfter(String newFileAfter) {
		String oldFileAfter = fileAfter;
		fileAfter = newFileAfter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__FILE_AFTER, oldFileAfter, fileAfter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProfileTypeType getProfileType() {
		return profileType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProfileType(ProfileTypeType newProfileType, NotificationChain msgs) {
		ProfileTypeType oldProfileType = profileType;
		profileType = newProfileType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE, oldProfileType, newProfileType);
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
	public void setProfileType(ProfileTypeType newProfileType) {
		if (newProfileType != profileType) {
			NotificationChain msgs = null;
			if (profileType != null)
				msgs = ((InternalEObject)profileType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE, null, msgs);
			if (newProfileType != null)
				msgs = ((InternalEObject)newProfileType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE, null, msgs);
			msgs = basicSetProfileType(newProfileType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE, newProfileType, newProfileType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFileProfile() {
		return fileProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFileProfile(String newFileProfile) {
		String oldFileProfile = fileProfile;
		fileProfile = newFileProfile;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.DARK_FIELD_TYPE__FILE_PROFILE, oldFileProfile, fileProfile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.DARK_FIELD_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE:
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
			case HmPackage.DARK_FIELD_TYPE__TYPE:
				return getType();
			case HmPackage.DARK_FIELD_TYPE__VALUE_BEFORE:
				return getValueBefore();
			case HmPackage.DARK_FIELD_TYPE__VALUE_AFTER:
				return getValueAfter();
			case HmPackage.DARK_FIELD_TYPE__FILE_BEFORE:
				return getFileBefore();
			case HmPackage.DARK_FIELD_TYPE__FILE_AFTER:
				return getFileAfter();
			case HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE:
				return getProfileType();
			case HmPackage.DARK_FIELD_TYPE__FILE_PROFILE:
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
			case HmPackage.DARK_FIELD_TYPE__TYPE:
				setType((TypeType13)newValue);
				return;
			case HmPackage.DARK_FIELD_TYPE__VALUE_BEFORE:
				setValueBefore((Double)newValue);
				return;
			case HmPackage.DARK_FIELD_TYPE__VALUE_AFTER:
				setValueAfter((Double)newValue);
				return;
			case HmPackage.DARK_FIELD_TYPE__FILE_BEFORE:
				setFileBefore((String)newValue);
				return;
			case HmPackage.DARK_FIELD_TYPE__FILE_AFTER:
				setFileAfter((String)newValue);
				return;
			case HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE:
				setProfileType((ProfileTypeType)newValue);
				return;
			case HmPackage.DARK_FIELD_TYPE__FILE_PROFILE:
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
			case HmPackage.DARK_FIELD_TYPE__TYPE:
				setType((TypeType13)null);
				return;
			case HmPackage.DARK_FIELD_TYPE__VALUE_BEFORE:
				setValueBefore(VALUE_BEFORE_EDEFAULT);
				return;
			case HmPackage.DARK_FIELD_TYPE__VALUE_AFTER:
				setValueAfter(VALUE_AFTER_EDEFAULT);
				return;
			case HmPackage.DARK_FIELD_TYPE__FILE_BEFORE:
				setFileBefore(FILE_BEFORE_EDEFAULT);
				return;
			case HmPackage.DARK_FIELD_TYPE__FILE_AFTER:
				setFileAfter(FILE_AFTER_EDEFAULT);
				return;
			case HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE:
				setProfileType((ProfileTypeType)null);
				return;
			case HmPackage.DARK_FIELD_TYPE__FILE_PROFILE:
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
			case HmPackage.DARK_FIELD_TYPE__TYPE:
				return type != null;
			case HmPackage.DARK_FIELD_TYPE__VALUE_BEFORE:
				return VALUE_BEFORE_EDEFAULT == null ? valueBefore != null : !VALUE_BEFORE_EDEFAULT.equals(valueBefore);
			case HmPackage.DARK_FIELD_TYPE__VALUE_AFTER:
				return VALUE_AFTER_EDEFAULT == null ? valueAfter != null : !VALUE_AFTER_EDEFAULT.equals(valueAfter);
			case HmPackage.DARK_FIELD_TYPE__FILE_BEFORE:
				return FILE_BEFORE_EDEFAULT == null ? fileBefore != null : !FILE_BEFORE_EDEFAULT.equals(fileBefore);
			case HmPackage.DARK_FIELD_TYPE__FILE_AFTER:
				return FILE_AFTER_EDEFAULT == null ? fileAfter != null : !FILE_AFTER_EDEFAULT.equals(fileAfter);
			case HmPackage.DARK_FIELD_TYPE__PROFILE_TYPE:
				return profileType != null;
			case HmPackage.DARK_FIELD_TYPE__FILE_PROFILE:
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
		result.append(valueBefore);
		result.append(", valueAfter: ");
		result.append(valueAfter);
		result.append(", fileBefore: ");
		result.append(fileBefore);
		result.append(", fileAfter: ");
		result.append(fileAfter);
		result.append(", fileProfile: ");
		result.append(fileProfile);
		result.append(')');
		return result.toString();
	}

} //DarkFieldTypeImpl
