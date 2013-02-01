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
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getFilename <em>Filename</em>}</li>
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
	 * This is true if the Run Mode attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean runModeESet;

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
	 * This is true if the Num Iterations attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean numIterationsESet;

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
	 * This is true if the Repeat Unitil Stopped attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean repeatUnitilStoppedESet;

	/**
	 * The cached value of the '{@link #getSpectrum() <em>Spectrum</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSpectrum()
	 * @generated
	 * @ordered
	 */
	protected Spectrum spectrum;

	/**
	 * This is true if the Spectrum containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean spectrumESet;

	/**
	 * The default value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
	protected String filename = FILENAME_EDEFAULT;

	/**
	 * This is true if the Filename attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean filenameESet;

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
			region = new EObjectContainmentEList.Unsettable<Region>(Region.class, this, RegiondefinitionPackage.SEQUENCE__REGION);
		}
		return region;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRegion() {
		if (region != null) ((InternalEList.Unsettable<?>)region).unset();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRegion() {
		return region != null && ((InternalEList.Unsettable<?>)region).isSet();
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
		boolean oldRunModeESet = runModeESet;
		runModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__RUN_MODE, oldRunMode, runMode, !oldRunModeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRunMode() {
		RUN_MODES oldRunMode = runMode;
		boolean oldRunModeESet = runModeESet;
		runMode = RUN_MODE_EDEFAULT;
		runModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__RUN_MODE, oldRunMode, RUN_MODE_EDEFAULT, oldRunModeESet));
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
		boolean oldNumIterationsESet = numIterationsESet;
		numIterationsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS, oldNumIterations, numIterations, !oldNumIterationsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetNumIterations() {
		int oldNumIterations = numIterations;
		boolean oldNumIterationsESet = numIterationsESet;
		numIterations = NUM_ITERATIONS_EDEFAULT;
		numIterationsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS, oldNumIterations, NUM_ITERATIONS_EDEFAULT, oldNumIterationsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetNumIterations() {
		return numIterationsESet;
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
		boolean oldRepeatUnitilStoppedESet = repeatUnitilStoppedESet;
		repeatUnitilStoppedESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED, oldRepeatUnitilStopped, repeatUnitilStopped, !oldRepeatUnitilStoppedESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRepeatUnitilStopped() {
		boolean oldRepeatUnitilStopped = repeatUnitilStopped;
		boolean oldRepeatUnitilStoppedESet = repeatUnitilStoppedESet;
		repeatUnitilStopped = REPEAT_UNITIL_STOPPED_EDEFAULT;
		repeatUnitilStoppedESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED, oldRepeatUnitilStopped, REPEAT_UNITIL_STOPPED_EDEFAULT, oldRepeatUnitilStoppedESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRepeatUnitilStopped() {
		return repeatUnitilStoppedESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Spectrum getSpectrum() {
		return spectrum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSpectrum(Spectrum newSpectrum, NotificationChain msgs) {
		Spectrum oldSpectrum = spectrum;
		spectrum = newSpectrum;
		boolean oldSpectrumESet = spectrumESet;
		spectrumESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__SPECTRUM, oldSpectrum, newSpectrum, !oldSpectrumESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSpectrum(Spectrum newSpectrum) {
		if (newSpectrum != spectrum) {
			NotificationChain msgs = null;
			if (spectrum != null)
				msgs = ((InternalEObject)spectrum).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.SEQUENCE__SPECTRUM, null, msgs);
			if (newSpectrum != null)
				msgs = ((InternalEObject)newSpectrum).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.SEQUENCE__SPECTRUM, null, msgs);
			msgs = basicSetSpectrum(newSpectrum, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldSpectrumESet = spectrumESet;
			spectrumESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__SPECTRUM, newSpectrum, newSpectrum, !oldSpectrumESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetSpectrum(NotificationChain msgs) {
		Spectrum oldSpectrum = spectrum;
		spectrum = null;
		boolean oldSpectrumESet = spectrumESet;
		spectrumESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__SPECTRUM, oldSpectrum, null, oldSpectrumESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetSpectrum() {
		if (spectrum != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)spectrum).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.SEQUENCE__SPECTRUM, null, msgs);
			msgs = basicUnsetSpectrum(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldSpectrumESet = spectrumESet;
			spectrumESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__SPECTRUM, null, null, oldSpectrumESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetSpectrum() {
		return spectrumESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilename(String newFilename) {
		String oldFilename = filename;
		filename = newFilename;
		boolean oldFilenameESet = filenameESet;
		filenameESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__FILENAME, oldFilename, filename, !oldFilenameESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFilename() {
		String oldFilename = filename;
		boolean oldFilenameESet = filenameESet;
		filename = FILENAME_EDEFAULT;
		filenameESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__FILENAME, oldFilename, FILENAME_EDEFAULT, oldFilenameESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFilename() {
		return filenameESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Region getRegion(String regionName) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
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
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				return basicUnsetSpectrum(msgs);
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
				return getSpectrum();
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				return getFilename();
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
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				setFilename((String)newValue);
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
				unsetRegion();
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				unsetRunMode();
				return;
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				unsetNumIterations();
				return;
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED:
				unsetRepeatUnitilStopped();
				return;
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				unsetSpectrum();
				return;
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				unsetFilename();
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
				return isSetRegion();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				return isSetRunMode();
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				return isSetNumIterations();
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNITIL_STOPPED:
				return isSetRepeatUnitilStopped();
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				return isSetSpectrum();
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				return isSetFilename();
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
		if (runModeESet) result.append(runMode); else result.append("<unset>");
		result.append(", numIterations: ");
		if (numIterationsESet) result.append(numIterations); else result.append("<unset>");
		result.append(", repeatUnitilStopped: ");
		if (repeatUnitilStoppedESet) result.append(repeatUnitilStopped); else result.append("<unset>");
		result.append(", filename: ");
		if (filenameESet) result.append(filename); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //SequenceImpl
