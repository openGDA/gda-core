/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Step;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Region</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getLensmode <em>Lensmode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getPassEnergy <em>Pass Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getAcquisitionMode <em>Acquisition Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getEnergyMode <em>Energy Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getEnergy <em>Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getStep <em>Step</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getDetector <em>Detector</em>}</li>
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
	 * The default value of the '{@link #getAcquisitionMode() <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAcquisitionMode()
	 * @generated
	 * @ordered
	 */
	protected static final ACQUIAITION_MODE ACQUISITION_MODE_EDEFAULT = ACQUIAITION_MODE.SWEPT;

	/**
	 * The cached value of the '{@link #getAcquisitionMode() <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAcquisitionMode()
	 * @generated
	 * @ordered
	 */
	protected ACQUIAITION_MODE acquisitionMode = ACQUISITION_MODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getEnergyMode() <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergyMode()
	 * @generated
	 * @ordered
	 */
	protected static final ENERGY_MODE ENERGY_MODE_EDEFAULT = ENERGY_MODE.KINETIC;

	/**
	 * The cached value of the '{@link #getEnergyMode() <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergyMode()
	 * @generated
	 * @ordered
	 */
	protected ENERGY_MODE energyMode = ENERGY_MODE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEnergy() <em>Energy</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergy()
	 * @generated
	 * @ordered
	 */
	protected Energy energy;

	/**
	 * The cached value of the '{@link #getStep() <em>Step</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStep()
	 * @generated
	 * @ordered
	 */
	protected Step step;

	/**
	 * The cached value of the '{@link #getDetector() <em>Detector</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector()
	 * @generated
	 * @ordered
	 */
	protected Detector detector;

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
	public ACQUIAITION_MODE getAcquisitionMode() {
		return acquisitionMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAcquisitionMode(ACQUIAITION_MODE newAcquisitionMode) {
		ACQUIAITION_MODE oldAcquisitionMode = acquisitionMode;
		acquisitionMode = newAcquisitionMode == null ? ACQUISITION_MODE_EDEFAULT : newAcquisitionMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ACQUISITION_MODE, oldAcquisitionMode, acquisitionMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ENERGY_MODE getEnergyMode() {
		return energyMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnergyMode(ENERGY_MODE newEnergyMode) {
		ENERGY_MODE oldEnergyMode = energyMode;
		energyMode = newEnergyMode == null ? ENERGY_MODE_EDEFAULT : newEnergyMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENERGY_MODE, oldEnergyMode, energyMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Energy getEnergy() {
		if (energy != null && energy.eIsProxy()) {
			InternalEObject oldEnergy = (InternalEObject)energy;
			energy = (Energy)eResolveProxy(oldEnergy);
			if (energy != oldEnergy) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RegiondefinitionPackage.REGION__ENERGY, oldEnergy, energy));
			}
		}
		return energy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Energy basicGetEnergy() {
		return energy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnergy(Energy newEnergy) {
		Energy oldEnergy = energy;
		energy = newEnergy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENERGY, oldEnergy, energy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Step getStep() {
		if (step != null && step.eIsProxy()) {
			InternalEObject oldStep = (InternalEObject)step;
			step = (Step)eResolveProxy(oldStep);
			if (step != oldStep) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RegiondefinitionPackage.REGION__STEP, oldStep, step));
			}
		}
		return step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Step basicGetStep() {
		return step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStep(Step newStep) {
		Step oldStep = step;
		step = newStep;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__STEP, oldStep, step));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Detector getDetector() {
		if (detector != null && detector.eIsProxy()) {
			InternalEObject oldDetector = (InternalEObject)detector;
			detector = (Detector)eResolveProxy(oldDetector);
			if (detector != oldDetector) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RegiondefinitionPackage.REGION__DETECTOR, oldDetector, detector));
			}
		}
		return detector;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Detector basicGetDetector() {
		return detector;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector(Detector newDetector) {
		Detector oldDetector = detector;
		detector = newDetector;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__DETECTOR, oldDetector, detector));
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
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				return getAcquisitionMode();
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				return getEnergyMode();
			case RegiondefinitionPackage.REGION__ENERGY:
				if (resolve) return getEnergy();
				return basicGetEnergy();
			case RegiondefinitionPackage.REGION__STEP:
				if (resolve) return getStep();
				return basicGetStep();
			case RegiondefinitionPackage.REGION__DETECTOR:
				if (resolve) return getDetector();
				return basicGetDetector();
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
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				setAcquisitionMode((ACQUIAITION_MODE)newValue);
				return;
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				setEnergyMode((ENERGY_MODE)newValue);
				return;
			case RegiondefinitionPackage.REGION__ENERGY:
				setEnergy((Energy)newValue);
				return;
			case RegiondefinitionPackage.REGION__STEP:
				setStep((Step)newValue);
				return;
			case RegiondefinitionPackage.REGION__DETECTOR:
				setDetector((Detector)newValue);
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
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				setAcquisitionMode(ACQUISITION_MODE_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				setEnergyMode(ENERGY_MODE_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__ENERGY:
				setEnergy((Energy)null);
				return;
			case RegiondefinitionPackage.REGION__STEP:
				setStep((Step)null);
				return;
			case RegiondefinitionPackage.REGION__DETECTOR:
				setDetector((Detector)null);
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
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				return acquisitionMode != ACQUISITION_MODE_EDEFAULT;
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				return energyMode != ENERGY_MODE_EDEFAULT;
			case RegiondefinitionPackage.REGION__ENERGY:
				return energy != null;
			case RegiondefinitionPackage.REGION__STEP:
				return step != null;
			case RegiondefinitionPackage.REGION__DETECTOR:
				return detector != null;
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
		result.append(", acquisitionMode: ");
		result.append(acquisitionMode);
		result.append(", energyMode: ");
		result.append(energyMode);
		result.append(')');
		return result.toString();
	}

} //RegionImpl
