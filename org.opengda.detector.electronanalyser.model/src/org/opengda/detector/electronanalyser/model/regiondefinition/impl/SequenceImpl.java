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

import org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sequence</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getRegion <em>Region</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getSpectrum <em>Spectrum</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SequenceImpl extends EObjectImpl implements Sequence {
	/**
	 * The cached value of the '{@link #getRegion() <em>Region</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRegion()
	 * @generated
	 * @ordered
	 */
	protected EList<Region> region;

	/**
	 * The default value of the '{@link #getRunMode() <em>Run Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRunMode()
	 * @generated
	 * @ordered
	 */
	protected static final RUN_MODES RUN_MODE_EDEFAULT = RUN_MODES.NORMAL;

	/**
	 * The cached value of the '{@link #getRunMode() <em>Run Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRunMode()
	 * @generated
	 * @ordered
	 */
	protected RUN_MODES runMode = RUN_MODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumIterations()
	 * @generated
	 * @ordered
	 */
	protected static final int NUM_ITERATIONS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumIterations()
	 * @generated
	 * @ordered
	 */
	protected int numIterations = NUM_ITERATIONS_EDEFAULT;

	/**
	 * The default value of the '{@link #isRepeatUnitilStopped() <em>Repeat Unitil Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isRepeatUnitilStopped()
	 * @generated
	 * @ordered
	 */
	protected static final boolean REPEAT_UNITIL_STOPPED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isRepeatUnitilStopped() <em>Repeat Unitil Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isRepeatUnitilStopped()
	 * @generated
	 * @ordered
	 */
	protected boolean repeatUnitilStopped = REPEAT_UNITIL_STOPPED_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSpectrum() <em>Spectrum</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSpectrum()
	 * @generated
	 * @ordered
	 */
	protected Spectrum spectrum;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SequenceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.SEQUENCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Region> getRegion() {
		if (region == null) {
			region = new EObjectContainmentEList<Region>(Region.class, this, RegiondefinitionPackage.SEQUENCE__REGION);
		}
		return region;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RUN_MODES getRunMode() {
		return runMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRunMode(RUN_MODES newRunMode) {
		RUN_MODES oldRunMode = runMode;
		runMode = newRunMode == null ? RUN_MODE_EDEFAULT : newRunMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__RUN_MODE, oldRunMode, runMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getNumIterations() {
		return numIterations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumIterations(int newNumIterations) {
		int oldNumIterations = numIterations;
		numIterations = newNumIterations;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS, oldNumIterations, numIterations));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isRepeatUnitilStopped() {
		return repeatUnitilStopped;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRepeatUnitilStopped(boolean newRepeatUnitilStopped) {
		boolean oldRepeatUnitilStopped = repeatUnitilStopped;
		repeatUnitilStopped = newRepeatUnitilStopped;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED, oldRepeatUnitilStopped, repeatUnitilStopped));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Spectrum getSpectrum() {
		if (spectrum != null && spectrum.eIsProxy()) {
			InternalEObject oldSpectrum = (InternalEObject)spectrum;
			spectrum = (Spectrum)eResolveProxy(oldSpectrum);
			if (spectrum != oldSpectrum) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RegiondefinitionPackage.SEQUENCE__SPECTRUM, oldSpectrum, spectrum));
			}
		}
		return spectrum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Spectrum basicGetSpectrum() {
		return spectrum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSpectrum(Spectrum newSpectrum) {
		Spectrum oldSpectrum = spectrum;
		spectrum = newSpectrum;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__SPECTRUM, oldSpectrum, spectrum));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RegiondefinitionPackage.SEQUENCE__REGION:
				return ((InternalEList<?>)getRegion()).basicRemove(otherEnd, msgs);
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
			case RegiondefinitionPackage.SEQUENCE__REGION:
				return getRegion();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				return getRunMode();
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				return getNumIterations();
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED:
				return isRepeatUnitilStopped();
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				if (resolve) return getSpectrum();
				return basicGetSpectrum();
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
			case RegiondefinitionPackage.SEQUENCE__REGION:
				getRegion().clear();
				getRegion().addAll((Collection<? extends Region>)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				setRunMode((RUN_MODES)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				setNumIterations((Integer)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED:
				setRepeatUnitilStopped((Boolean)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				setSpectrum((Spectrum)newValue);
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
			case RegiondefinitionPackage.SEQUENCE__REGION:
				getRegion().clear();
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				setRunMode(RUN_MODE_EDEFAULT);
				return;
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				setNumIterations(NUM_ITERATIONS_EDEFAULT);
				return;
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED:
				setRepeatUnitilStopped(REPEAT_UNITIL_STOPPED_EDEFAULT);
				return;
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				setSpectrum((Spectrum)null);
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
			case RegiondefinitionPackage.SEQUENCE__REGION:
				return region != null && !region.isEmpty();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				return runMode != RUN_MODE_EDEFAULT;
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				return numIterations != NUM_ITERATIONS_EDEFAULT;
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED:
				return repeatUnitilStopped != REPEAT_UNITIL_STOPPED_EDEFAULT;
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				return spectrum != null;
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
		result.append(" (runMode: ");
		result.append(runMode);
		result.append(", numIterations: ");
		result.append(numIterations);
		result.append(", repeatUnitilStopped: ");
		result.append(repeatUnitilStopped);
		result.append(')');
		return result.toString();
	}

} //SequenceImpl
