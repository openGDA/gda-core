/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.SettingsfileType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Settingsfile Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.SettingsfileTypeImpl#getBlueprint <em>Blueprint</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.SettingsfileTypeImpl#getSettingsDirPostfix <em>Settings Dir Postfix</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SettingsfileTypeImpl extends EObjectImpl implements SettingsfileType {
	/**
	 * The default value of the '{@link #getBlueprint() <em>Blueprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBlueprint()
	 * @generated
	 * @ordered
	 */
	protected static final String BLUEPRINT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBlueprint() <em>Blueprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBlueprint()
	 * @generated
	 * @ordered
	 */
	protected String blueprint = BLUEPRINT_EDEFAULT;

	/**
	 * The default value of the '{@link #getSettingsDirPostfix() <em>Settings Dir Postfix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSettingsDirPostfix()
	 * @generated
	 * @ordered
	 */
	protected static final String SETTINGS_DIR_POSTFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSettingsDirPostfix() <em>Settings Dir Postfix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSettingsDirPostfix()
	 * @generated
	 * @ordered
	 */
	protected String settingsDirPostfix = SETTINGS_DIR_POSTFIX_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SettingsfileTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.SETTINGSFILE_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getBlueprint() {
		return blueprint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBlueprint(String newBlueprint) {
		String oldBlueprint = blueprint;
		blueprint = newBlueprint;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.SETTINGSFILE_TYPE__BLUEPRINT, oldBlueprint, blueprint));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSettingsDirPostfix() {
		return settingsDirPostfix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSettingsDirPostfix(String newSettingsDirPostfix) {
		String oldSettingsDirPostfix = settingsDirPostfix;
		settingsDirPostfix = newSettingsDirPostfix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.SETTINGSFILE_TYPE__SETTINGS_DIR_POSTFIX, oldSettingsDirPostfix, settingsDirPostfix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LocalTomoPackage.SETTINGSFILE_TYPE__BLUEPRINT:
				return getBlueprint();
			case LocalTomoPackage.SETTINGSFILE_TYPE__SETTINGS_DIR_POSTFIX:
				return getSettingsDirPostfix();
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
			case LocalTomoPackage.SETTINGSFILE_TYPE__BLUEPRINT:
				setBlueprint((String)newValue);
				return;
			case LocalTomoPackage.SETTINGSFILE_TYPE__SETTINGS_DIR_POSTFIX:
				setSettingsDirPostfix((String)newValue);
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
			case LocalTomoPackage.SETTINGSFILE_TYPE__BLUEPRINT:
				setBlueprint(BLUEPRINT_EDEFAULT);
				return;
			case LocalTomoPackage.SETTINGSFILE_TYPE__SETTINGS_DIR_POSTFIX:
				setSettingsDirPostfix(SETTINGS_DIR_POSTFIX_EDEFAULT);
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
			case LocalTomoPackage.SETTINGSFILE_TYPE__BLUEPRINT:
				return BLUEPRINT_EDEFAULT == null ? blueprint != null : !BLUEPRINT_EDEFAULT.equals(blueprint);
			case LocalTomoPackage.SETTINGSFILE_TYPE__SETTINGS_DIR_POSTFIX:
				return SETTINGS_DIR_POSTFIX_EDEFAULT == null ? settingsDirPostfix != null : !SETTINGS_DIR_POSTFIX_EDEFAULT.equals(settingsDirPostfix);
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
		result.append(" (blueprint: ");
		result.append(blueprint);
		result.append(", settingsDirPostfix: ");
		result.append(settingsDirPostfix);
		result.append(')');
		return result.toString();
	}

} //SettingsfileTypeImpl
