/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.BasicEList;
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
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Sequence</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getFilename <em>Filename</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getRegions <em>Regions</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getRunModeIndex <em>Run Mode Index</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#isRepeatUntilStopped <em>Repeat Until Stopped</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#isConfirmAfterEachIteration <em>Confirm After Each Iteration</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl#getSpectrum <em>Spectrum</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SequenceImpl extends EObjectImpl implements Sequence {
	/**
	 * The default value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_EDEFAULT = "user.seq";

	/**
	 * The cached value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
	protected String filename = FILENAME_EDEFAULT;

	/**
	 * This is true if the Filename attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean filenameESet;

	/**
	 * The cached value of the '{@link #getRegions() <em>Regions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRegions()
	 * @generated
	 * @ordered
	 */
	protected EList<Region> regions;

	/**
	 * The default value of the '{@link #getRunMode() <em>Run Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRunMode()
	 * @generated
	 * @ordered
	 */
	protected static final RUN_MODES RUN_MODE_EDEFAULT = RUN_MODES.NORMAL_LITERAL;

	/**
	 * The cached value of the '{@link #getRunMode() <em>Run Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRunMode()
	 * @generated
	 * @ordered
	 */
	protected RUN_MODES runMode = RUN_MODE_EDEFAULT;

	/**
	 * This is true if the Run Mode attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean runModeESet;

	/**
	 * The default value of the '{@link #getRunModeIndex() <em>Run Mode Index</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getRunModeIndex()
	 * @generated
	 * @ordered
	 */
	protected static final int RUN_MODE_INDEX_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getRunModeIndex() <em>Run Mode Index</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getRunModeIndex()
	 * @generated
	 * @ordered
	 */
	protected int runModeIndex = RUN_MODE_INDEX_EDEFAULT;

	/**
	 * This is true if the Run Mode Index attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	protected boolean runModeIndexESet;

	/**
	 * The default value of the '{@link #getNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getNumIterations()
	 * @generated
	 * @ordered
	 */
	protected static final int NUM_ITERATIONS_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getNumIterations()
	 * @generated
	 * @ordered
	 */
	protected int numIterations = NUM_ITERATIONS_EDEFAULT;

	/**
	 * This is true if the Num Iterations attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	protected boolean numIterationsESet;

	/**
	 * The default value of the '{@link #isRepeatUntilStopped() <em>Repeat Until Stopped</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #isRepeatUntilStopped()
	 * @generated
	 * @ordered
	 */
	protected static final boolean REPEAT_UNTIL_STOPPED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isRepeatUntilStopped() <em>Repeat Until Stopped</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #isRepeatUntilStopped()
	 * @generated
	 * @ordered
	 */
	protected boolean repeatUntilStopped = REPEAT_UNTIL_STOPPED_EDEFAULT;

	/**
	 * This is true if the Repeat Until Stopped attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	protected boolean repeatUntilStoppedESet;

	/**
	 * The default value of the '{@link #isConfirmAfterEachIteration() <em>Confirm After Each Iteration</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #isConfirmAfterEachIteration()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONFIRM_AFTER_EACH_ITERATION_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isConfirmAfterEachIteration() <em>Confirm After Each Iteration</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #isConfirmAfterEachIteration()
	 * @generated
	 * @ordered
	 */
	protected boolean confirmAfterEachIteration = CONFIRM_AFTER_EACH_ITERATION_EDEFAULT;

	/**
	 * This is true if the Confirm After Each Iteration attribute has been set.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean confirmAfterEachIterationESet;

	/**
	 * The cached value of the '{@link #getSpectrum() <em>Spectrum</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getSpectrum()
	 * @generated
	 * @ordered
	 */
	protected Spectrum spectrum;

	/**
	 * This is true if the Spectrum containment reference has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	protected boolean spectrumESet;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected SequenceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.SEQUENCE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public RUN_MODES getRunMode() {
		return runMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRunMode() {
		return runModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public int getRunModeIndex() {
		return runModeIndex;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setRunModeIndex(int newRunModeIndex) {
		int oldRunModeIndex = runModeIndex;
		runModeIndex = newRunModeIndex;
		boolean oldRunModeIndexESet = runModeIndexESet;
		runModeIndexESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX, oldRunModeIndex, runModeIndex, !oldRunModeIndexESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRunModeIndex() {
		int oldRunModeIndex = runModeIndex;
		boolean oldRunModeIndexESet = runModeIndexESet;
		runModeIndex = RUN_MODE_INDEX_EDEFAULT;
		runModeIndexESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX, oldRunModeIndex, RUN_MODE_INDEX_EDEFAULT, oldRunModeIndexESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRunModeIndex() {
		return runModeIndexESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public int getNumIterations() {
		return numIterations;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetNumIterations() {
		return numIterationsESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isRepeatUntilStopped() {
		return repeatUntilStopped;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setRepeatUntilStopped(boolean newRepeatUntilStopped) {
		boolean oldRepeatUntilStopped = repeatUntilStopped;
		repeatUntilStopped = newRepeatUntilStopped;
		boolean oldRepeatUntilStoppedESet = repeatUntilStoppedESet;
		repeatUntilStoppedESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED, oldRepeatUntilStopped, repeatUntilStopped, !oldRepeatUntilStoppedESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRepeatUntilStopped() {
		boolean oldRepeatUntilStopped = repeatUntilStopped;
		boolean oldRepeatUntilStoppedESet = repeatUntilStoppedESet;
		repeatUntilStopped = REPEAT_UNTIL_STOPPED_EDEFAULT;
		repeatUntilStoppedESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED, oldRepeatUntilStopped, REPEAT_UNTIL_STOPPED_EDEFAULT, oldRepeatUntilStoppedESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRepeatUntilStopped() {
		return repeatUntilStoppedESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isConfirmAfterEachIteration() {
		return confirmAfterEachIteration;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setConfirmAfterEachIteration(
			boolean newConfirmAfterEachIteration) {
		boolean oldConfirmAfterEachIteration = confirmAfterEachIteration;
		confirmAfterEachIteration = newConfirmAfterEachIteration;
		boolean oldConfirmAfterEachIterationESet = confirmAfterEachIterationESet;
		confirmAfterEachIterationESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION, oldConfirmAfterEachIteration, confirmAfterEachIteration, !oldConfirmAfterEachIterationESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetConfirmAfterEachIteration() {
		boolean oldConfirmAfterEachIteration = confirmAfterEachIteration;
		boolean oldConfirmAfterEachIterationESet = confirmAfterEachIterationESet;
		confirmAfterEachIteration = CONFIRM_AFTER_EACH_ITERATION_EDEFAULT;
		confirmAfterEachIterationESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION, oldConfirmAfterEachIteration, CONFIRM_AFTER_EACH_ITERATION_EDEFAULT, oldConfirmAfterEachIterationESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetConfirmAfterEachIteration() {
		return confirmAfterEachIterationESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Spectrum getSpectrum() {
		return spectrum;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSpectrum(Spectrum newSpectrum,
			NotificationChain msgs) {
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetSpectrum() {
		return spectrumESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	public Region getRegionById(String regionId) {
		for (Region region : getRegions()) {
			if (region.getRegionId().equals(regionId)) {
				return region;
			}
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	public EList<Region> getRegionsByName(String regionName) {
		BasicEList<Region> regions = new BasicEList<Region>();
		for (Region region : getRegions()) {
			if (region.getRegionId().equals(regionName)) {
				regions.add(region);
			}
		}
		return regions;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFilename() {
		return filenameESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Region> getRegions() {
		if (regions == null) {
			regions = new EObjectContainmentEList.Unsettable<Region>(Region.class, this, RegiondefinitionPackage.SEQUENCE__REGIONS);
		}
		return regions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRegions() {
		if (regions != null) ((InternalEList.Unsettable<?>)regions).unset();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRegions() {
		return regions != null && ((InternalEList.Unsettable<?>)regions).isSet();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RegiondefinitionPackage.SEQUENCE__REGIONS:
				return ((InternalEList<?>)getRegions()).basicRemove(otherEnd, msgs);
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				return basicUnsetSpectrum(msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				return getFilename();
			case RegiondefinitionPackage.SEQUENCE__REGIONS:
				return getRegions();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				return getRunMode();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX:
				return getRunModeIndex();
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				return getNumIterations();
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED:
				return isRepeatUntilStopped();
			case RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION:
				return isConfirmAfterEachIteration();
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				return getSpectrum();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				setFilename((String)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__REGIONS:
				getRegions().clear();
				getRegions().addAll((Collection<? extends Region>)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				setRunMode((RUN_MODES)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX:
				setRunModeIndex((Integer)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				setNumIterations((Integer)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED:
				setRepeatUntilStopped((Boolean)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION:
				setConfirmAfterEachIteration((Boolean)newValue);
				return;
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				setSpectrum((Spectrum)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				unsetFilename();
				return;
			case RegiondefinitionPackage.SEQUENCE__REGIONS:
				unsetRegions();
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				unsetRunMode();
				return;
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX:
				unsetRunModeIndex();
				return;
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				unsetNumIterations();
				return;
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED:
				unsetRepeatUntilStopped();
				return;
			case RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION:
				unsetConfirmAfterEachIteration();
				return;
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				unsetSpectrum();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
				return isSetFilename();
			case RegiondefinitionPackage.SEQUENCE__REGIONS:
				return isSetRegions();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
				return isSetRunMode();
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX:
				return isSetRunModeIndex();
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
				return isSetNumIterations();
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED:
				return isSetRepeatUntilStopped();
			case RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION:
				return isSetConfirmAfterEachIteration();
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
				return isSetSpectrum();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (filename: "); //$NON-NLS-1$
		if (filenameESet) result.append(filename); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", runMode: "); //$NON-NLS-1$
		if (runModeESet) result.append(runMode); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", runModeIndex: "); //$NON-NLS-1$
		if (runModeIndexESet) result.append(runModeIndex); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", numIterations: "); //$NON-NLS-1$
		if (numIterationsESet) result.append(numIterations); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", repeatUntilStopped: "); //$NON-NLS-1$
		if (repeatUntilStoppedESet) result.append(repeatUntilStopped); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", confirmAfterEachIteration: "); //$NON-NLS-1$
		if (confirmAfterEachIterationESet) result.append(confirmAfterEachIteration); else result.append("<unset>"); //$NON-NLS-1$
		result.append(')');
		return result.toString();
	}

} // SequenceImpl
