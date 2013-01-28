/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition.impl;

import com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE;
import com.opengda.detector.electronalyser.server.model.regiondefinition.Region;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage;
import com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Region</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl#getName <em>Name</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl#getLensmode <em>Lensmode</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl#getPassEnergy <em>Pass Energy</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl#getRunMode <em>Run Mode</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RegionImpl extends EObjectImpl implements Region {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getLensmode() <em>Lensmode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLensmode()
	 * @generated
	 * @ordered
	 */
	protected static final LENS_MODE LENSMODE_EDEFAULT = LENS_MODE.TRANSMISSION;

	/**
	 * The cached value of the '{@link #getLensmode() <em>Lensmode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLensmode()
	 * @generated
	 * @ordered
	 */
	protected LENS_MODE lensmode = LENSMODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getPassEnergy() <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPassEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final Integer PASS_ENERGY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPassEnergy() <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPassEnergy()
	 * @generated
	 * @ordered
	 */
	protected Integer passEnergy = PASS_ENERGY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getRunMode() <em>Run Mode</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRunMode()
	 * @generated
	 * @ordered
	 */
	protected RunMode runMode;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RegionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.REGION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LENS_MODE getLensmode() {
		return lensmode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLensmode(LENS_MODE newLensmode) {
		LENS_MODE oldLensmode = lensmode;
		lensmode = newLensmode == null ? LENSMODE_EDEFAULT : newLensmode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__LENSMODE, oldLensmode, lensmode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Integer getPassEnergy() {
		return passEnergy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPassEnergy(Integer newPassEnergy) {
		Integer oldPassEnergy = passEnergy;
		passEnergy = newPassEnergy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__PASS_ENERGY, oldPassEnergy, passEnergy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RunMode getRunMode() {
		return runMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRunMode(RunMode newRunMode, NotificationChain msgs) {
		RunMode oldRunMode = runMode;
		runMode = newRunMode;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__RUN_MODE, oldRunMode, newRunMode);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRunMode(RunMode newRunMode) {
		if (newRunMode != runMode) {
			NotificationChain msgs = null;
			if (runMode != null)
				msgs = ((InternalEObject)runMode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__RUN_MODE, null, msgs);
			if (newRunMode != null)
				msgs = ((InternalEObject)newRunMode).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__RUN_MODE, null, msgs);
			msgs = basicSetRunMode(newRunMode, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__RUN_MODE, newRunMode, newRunMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return basicSetRunMode(null, msgs);
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
			case RegiondefinitionPackage.REGION__NAME:
				return getName();
			case RegiondefinitionPackage.REGION__LENSMODE:
				return getLensmode();
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				return getPassEnergy();
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return getRunMode();
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
			case RegiondefinitionPackage.REGION__NAME:
				setName((String)newValue);
				return;
			case RegiondefinitionPackage.REGION__LENSMODE:
				setLensmode((LENS_MODE)newValue);
				return;
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				setPassEnergy((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__RUN_MODE:
				setRunMode((RunMode)newValue);
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
			case RegiondefinitionPackage.REGION__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__LENSMODE:
				setLensmode(LENSMODE_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				setPassEnergy(PASS_ENERGY_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__RUN_MODE:
				setRunMode((RunMode)null);
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
			case RegiondefinitionPackage.REGION__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RegiondefinitionPackage.REGION__LENSMODE:
				return lensmode != LENSMODE_EDEFAULT;
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				return PASS_ENERGY_EDEFAULT == null ? passEnergy != null : !PASS_ENERGY_EDEFAULT.equals(passEnergy);
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return runMode != null;
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
		result.append(" (name: ");
		result.append(name);
		result.append(", lensmode: ");
		result.append(lensmode);
		result.append(", passEnergy: ");
		result.append(passEnergy);
		result.append(')');
		return result.toString();
	}

} //RegionImpl
