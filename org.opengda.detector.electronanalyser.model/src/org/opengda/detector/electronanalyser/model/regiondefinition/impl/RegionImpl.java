/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.PassEnergy;
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
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getAcquisitionMode <em>Acquisition Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getEnergyMode <em>Energy Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getEnergy <em>Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getStep <em>Step</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getDetector <em>Detector</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getPassEnergy <em>Pass Energy</em>}</li>
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
	 * This is true if the Lensmode attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean lensmodeESet;

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
	 * This is true if the Run Mode containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean runModeESet;

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
	 * This is true if the Acquisition Mode attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean acquisitionModeESet;

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
	 * The cached value of the '{@link #getEnergy() <em>Energy</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergy()
	 * @generated
	 * @ordered
	 */
	protected Energy energy;

	/**
	 * The cached value of the '{@link #getStep() <em>Step</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStep()
	 * @generated
	 * @ordered
	 */
	protected Step step;

	/**
	 * The cached value of the '{@link #getDetector() <em>Detector</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector()
	 * @generated
	 * @ordered
	 */
	protected Detector detector;

	/**
	 * The default value of the '{@link #getPassEnergy() <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPassEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final PASS_ENERGY PASS_ENERGY_EDEFAULT = PASS_ENERGY._5;

	/**
	 * The cached value of the '{@link #getPassEnergy() <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPassEnergy()
	 * @generated
	 * @ordered
	 */
	protected PASS_ENERGY passEnergy = PASS_ENERGY_EDEFAULT;

	/**
	 * This is true if the Pass Energy attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean passEnergyESet;

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
		boolean oldLensmodeESet = lensmodeESet;
		lensmodeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__LENSMODE, oldLensmode, lensmode, !oldLensmodeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetLensmode() {
		LENS_MODE oldLensmode = lensmode;
		boolean oldLensmodeESet = lensmodeESet;
		lensmode = LENSMODE_EDEFAULT;
		lensmodeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__LENSMODE, oldLensmode, LENSMODE_EDEFAULT, oldLensmodeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetLensmode() {
		return lensmodeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PASS_ENERGY getPassEnergy() {
		return passEnergy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPassEnergy(PASS_ENERGY newPassEnergy) {
		PASS_ENERGY oldPassEnergy = passEnergy;
		passEnergy = newPassEnergy == null ? PASS_ENERGY_EDEFAULT : newPassEnergy;
		boolean oldPassEnergyESet = passEnergyESet;
		passEnergyESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__PASS_ENERGY, oldPassEnergy, passEnergy, !oldPassEnergyESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetPassEnergy() {
		PASS_ENERGY oldPassEnergy = passEnergy;
		boolean oldPassEnergyESet = passEnergyESet;
		passEnergy = PASS_ENERGY_EDEFAULT;
		passEnergyESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__PASS_ENERGY, oldPassEnergy, PASS_ENERGY_EDEFAULT, oldPassEnergyESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetPassEnergy() {
		return passEnergyESet;
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
		boolean oldRunModeESet = runModeESet;
		runModeESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__RUN_MODE, oldRunMode, newRunMode, !oldRunModeESet);
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
		else {
			boolean oldRunModeESet = runModeESet;
			runModeESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__RUN_MODE, newRunMode, newRunMode, !oldRunModeESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetRunMode(NotificationChain msgs) {
		RunMode oldRunMode = runMode;
		runMode = null;
		boolean oldRunModeESet = runModeESet;
		runModeESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__RUN_MODE, oldRunMode, null, oldRunModeESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRunMode() {
		if (runMode != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)runMode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__RUN_MODE, null, msgs);
			msgs = basicUnsetRunMode(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldRunModeESet = runModeESet;
			runModeESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__RUN_MODE, null, null, oldRunModeESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRunMode() {
		return runModeESet;
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
		boolean oldAcquisitionModeESet = acquisitionModeESet;
		acquisitionModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ACQUISITION_MODE, oldAcquisitionMode, acquisitionMode, !oldAcquisitionModeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetAcquisitionMode() {
		ACQUIAITION_MODE oldAcquisitionMode = acquisitionMode;
		boolean oldAcquisitionModeESet = acquisitionModeESet;
		acquisitionMode = ACQUISITION_MODE_EDEFAULT;
		acquisitionModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__ACQUISITION_MODE, oldAcquisitionMode, ACQUISITION_MODE_EDEFAULT, oldAcquisitionModeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetAcquisitionMode() {
		return acquisitionModeESet;
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
		return energy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetEnergy(Energy newEnergy, NotificationChain msgs) {
		Energy oldEnergy = energy;
		energy = newEnergy;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENERGY, oldEnergy, newEnergy);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnergy(Energy newEnergy) {
		if (newEnergy != energy) {
			NotificationChain msgs = null;
			if (energy != null)
				msgs = ((InternalEObject)energy).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__ENERGY, null, msgs);
			if (newEnergy != null)
				msgs = ((InternalEObject)newEnergy).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__ENERGY, null, msgs);
			msgs = basicSetEnergy(newEnergy, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENERGY, newEnergy, newEnergy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Step getStep() {
		return step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStep(Step newStep, NotificationChain msgs) {
		Step oldStep = step;
		step = newStep;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__STEP, oldStep, newStep);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStep(Step newStep) {
		if (newStep != step) {
			NotificationChain msgs = null;
			if (step != null)
				msgs = ((InternalEObject)step).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__STEP, null, msgs);
			if (newStep != null)
				msgs = ((InternalEObject)newStep).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__STEP, null, msgs);
			msgs = basicSetStep(newStep, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__STEP, newStep, newStep));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Detector getDetector() {
		return detector;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDetector(Detector newDetector, NotificationChain msgs) {
		Detector oldDetector = detector;
		detector = newDetector;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__DETECTOR, oldDetector, newDetector);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector(Detector newDetector) {
		if (newDetector != detector) {
			NotificationChain msgs = null;
			if (detector != null)
				msgs = ((InternalEObject)detector).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__DETECTOR, null, msgs);
			if (newDetector != null)
				msgs = ((InternalEObject)newDetector).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__DETECTOR, null, msgs);
			msgs = basicSetDetector(newDetector, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__DETECTOR, newDetector, newDetector));
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
				return basicUnsetRunMode(msgs);
			case RegiondefinitionPackage.REGION__ENERGY:
				return basicSetEnergy(null, msgs);
			case RegiondefinitionPackage.REGION__STEP:
				return basicSetStep(null, msgs);
			case RegiondefinitionPackage.REGION__DETECTOR:
				return basicSetDetector(null, msgs);
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
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return getRunMode();
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				return getAcquisitionMode();
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				return getEnergyMode();
			case RegiondefinitionPackage.REGION__ENERGY:
				return getEnergy();
			case RegiondefinitionPackage.REGION__STEP:
				return getStep();
			case RegiondefinitionPackage.REGION__DETECTOR:
				return getDetector();
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				return getPassEnergy();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__NAME:
				setName((String)newValue);
				return;
			case RegiondefinitionPackage.REGION__LENSMODE:
				setLensmode((LENS_MODE)newValue);
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
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				setPassEnergy((PASS_ENERGY)newValue);
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
				unsetLensmode();
				return;
			case RegiondefinitionPackage.REGION__RUN_MODE:
				unsetRunMode();
				return;
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				unsetAcquisitionMode();
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
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				unsetPassEnergy();
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
				return isSetLensmode();
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return isSetRunMode();
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				return isSetAcquisitionMode();
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				return energyMode != ENERGY_MODE_EDEFAULT;
			case RegiondefinitionPackage.REGION__ENERGY:
				return energy != null;
			case RegiondefinitionPackage.REGION__STEP:
				return step != null;
			case RegiondefinitionPackage.REGION__DETECTOR:
				return detector != null;
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				return isSetPassEnergy();
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
		if (lensmodeESet) result.append(lensmode); else result.append("<unset>");
		result.append(", acquisitionMode: ");
		if (acquisitionModeESet) result.append(acquisitionMode); else result.append("<unset>");
		result.append(", energyMode: ");
		result.append(energyMode);
		result.append(", passEnergy: ");
		if (passEnergyESet) result.append(passEnergy); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //RegionImpl
