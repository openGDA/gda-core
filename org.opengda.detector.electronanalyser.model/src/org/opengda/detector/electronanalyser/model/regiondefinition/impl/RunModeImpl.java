/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Run Mode</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl#getMode <em>Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl#getRunModeIndex <em>Run Mode Index</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl#getNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl#isRepeatUntilStopped <em>Repeat Until Stopped</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl#isConfirmAfterEachIteration <em>Confirm After Each Iteration</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl#isNumIterationOption <em>Num Iteration Option</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RunModeImpl extends EObjectImpl implements RunMode {
	/**
	 * The default value of the '{@link #getMode() <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMode()
	 * @generated
	 * @ordered
	 */
	protected static final RUN_MODES MODE_EDEFAULT = RUN_MODES.NORMAL_LITERAL;

	/**
	 * The cached value of the '{@link #getMode() <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMode()
	 * @generated
	 * @ordered
	 */
	protected RUN_MODES mode = MODE_EDEFAULT;

	/**
	 * This is true if the Mode attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean modeESet;

	/**
	 * The default value of the '{@link #getRunModeIndex() <em>Run Mode Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRunModeIndex()
	 * @generated
	 * @ordered
	 */
	protected static final int RUN_MODE_INDEX_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getRunModeIndex() <em>Run Mode Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRunModeIndex()
	 * @generated
	 * @ordered
	 */
	protected int runModeIndex = RUN_MODE_INDEX_EDEFAULT;

	/**
	 * This is true if the Run Mode Index attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean runModeIndexESet;

	/**
	 * The default value of the '{@link #getNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumIterations()
	 * @generated
	 * @ordered
	 */
	protected static final int NUM_ITERATIONS_EDEFAULT = 1;

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
	 * The default value of the '{@link #isRepeatUntilStopped() <em>Repeat Until Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isRepeatUntilStopped()
	 * @generated
	 * @ordered
	 */
	protected static final boolean REPEAT_UNTIL_STOPPED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isRepeatUntilStopped() <em>Repeat Until Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isRepeatUntilStopped()
	 * @generated
	 * @ordered
	 */
	protected boolean repeatUntilStopped = REPEAT_UNTIL_STOPPED_EDEFAULT;

	/**
	 * This is true if the Repeat Until Stopped attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean repeatUntilStoppedESet;

	/**
	 * The default value of the '{@link #isConfirmAfterEachIteration() <em>Confirm After Each Iteration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isConfirmAfterEachIteration()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONFIRM_AFTER_EACH_ITERATION_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isConfirmAfterEachIteration() <em>Confirm After Each Iteration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isConfirmAfterEachIteration()
	 * @generated
	 * @ordered
	 */
	protected boolean confirmAfterEachIteration = CONFIRM_AFTER_EACH_ITERATION_EDEFAULT;

	/**
	 * The default value of the '{@link #isNumIterationOption() <em>Num Iteration Option</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNumIterationOption()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NUM_ITERATION_OPTION_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isNumIterationOption() <em>Num Iteration Option</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNumIterationOption()
	 * @generated
	 * @ordered
	 */
	protected boolean numIterationOption = NUM_ITERATION_OPTION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RunModeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.RUN_MODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RUN_MODES getMode() {
		return mode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMode(RUN_MODES newMode) {
		RUN_MODES oldMode = mode;
		mode = newMode == null ? MODE_EDEFAULT : newMode;
		boolean oldModeESet = modeESet;
		modeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__MODE, oldMode, mode, !oldModeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetMode() {
		RUN_MODES oldMode = mode;
		boolean oldModeESet = modeESet;
		mode = MODE_EDEFAULT;
		modeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.RUN_MODE__MODE, oldMode, MODE_EDEFAULT, oldModeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetMode() {
		return modeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getRunModeIndex() {
		return runModeIndex;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRunModeIndex(int newRunModeIndex) {
		int oldRunModeIndex = runModeIndex;
		runModeIndex = newRunModeIndex;
		boolean oldRunModeIndexESet = runModeIndexESet;
		runModeIndexESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__RUN_MODE_INDEX, oldRunModeIndex, runModeIndex, !oldRunModeIndexESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRunModeIndex() {
		int oldRunModeIndex = runModeIndex;
		boolean oldRunModeIndexESet = runModeIndexESet;
		runModeIndex = RUN_MODE_INDEX_EDEFAULT;
		runModeIndexESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.RUN_MODE__RUN_MODE_INDEX, oldRunModeIndex, RUN_MODE_INDEX_EDEFAULT, oldRunModeIndexESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRunModeIndex() {
		return runModeIndexESet;
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
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS, oldNumIterations, numIterations, !oldNumIterationsESet));
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
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS, oldNumIterations, NUM_ITERATIONS_EDEFAULT, oldNumIterationsESet));
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
	public boolean isRepeatUntilStopped() {
		return repeatUntilStopped;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRepeatUntilStopped(boolean newRepeatUntilStopped) {
		boolean oldRepeatUntilStopped = repeatUntilStopped;
		repeatUntilStopped = newRepeatUntilStopped;
		boolean oldRepeatUntilStoppedESet = repeatUntilStoppedESet;
		repeatUntilStoppedESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__REPEAT_UNTIL_STOPPED, oldRepeatUntilStopped, repeatUntilStopped, !oldRepeatUntilStoppedESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRepeatUntilStopped() {
		boolean oldRepeatUntilStopped = repeatUntilStopped;
		boolean oldRepeatUntilStoppedESet = repeatUntilStoppedESet;
		repeatUntilStopped = REPEAT_UNTIL_STOPPED_EDEFAULT;
		repeatUntilStoppedESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.RUN_MODE__REPEAT_UNTIL_STOPPED, oldRepeatUntilStopped, REPEAT_UNTIL_STOPPED_EDEFAULT, oldRepeatUntilStoppedESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRepeatUntilStopped() {
		return repeatUntilStoppedESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isConfirmAfterEachIteration() {
		return confirmAfterEachIteration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConfirmAfterEachIteration(boolean newConfirmAfterEachIteration) {
		boolean oldConfirmAfterEachIteration = confirmAfterEachIteration;
		confirmAfterEachIteration = newConfirmAfterEachIteration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__CONFIRM_AFTER_EACH_ITERATION, oldConfirmAfterEachIteration, confirmAfterEachIteration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isNumIterationOption() {
		return numIterationOption;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumIterationOption(boolean newNumIterationOption) {
		boolean oldNumIterationOption = numIterationOption;
		numIterationOption = newNumIterationOption;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__NUM_ITERATION_OPTION, oldNumIterationOption, numIterationOption));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegiondefinitionPackage.RUN_MODE__MODE:
				return getMode();
			case RegiondefinitionPackage.RUN_MODE__RUN_MODE_INDEX:
				return getRunModeIndex();
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				return getNumIterations();
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNTIL_STOPPED:
				return isRepeatUntilStopped();
			case RegiondefinitionPackage.RUN_MODE__CONFIRM_AFTER_EACH_ITERATION:
				return isConfirmAfterEachIteration();
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATION_OPTION:
				return isNumIterationOption();
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
			case RegiondefinitionPackage.RUN_MODE__MODE:
				setMode((RUN_MODES)newValue);
				return;
			case RegiondefinitionPackage.RUN_MODE__RUN_MODE_INDEX:
				setRunModeIndex((Integer)newValue);
				return;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				setNumIterations((Integer)newValue);
				return;
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNTIL_STOPPED:
				setRepeatUntilStopped((Boolean)newValue);
				return;
			case RegiondefinitionPackage.RUN_MODE__CONFIRM_AFTER_EACH_ITERATION:
				setConfirmAfterEachIteration((Boolean)newValue);
				return;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATION_OPTION:
				setNumIterationOption((Boolean)newValue);
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
			case RegiondefinitionPackage.RUN_MODE__MODE:
				unsetMode();
				return;
			case RegiondefinitionPackage.RUN_MODE__RUN_MODE_INDEX:
				unsetRunModeIndex();
				return;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				unsetNumIterations();
				return;
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNTIL_STOPPED:
				unsetRepeatUntilStopped();
				return;
			case RegiondefinitionPackage.RUN_MODE__CONFIRM_AFTER_EACH_ITERATION:
				setConfirmAfterEachIteration(CONFIRM_AFTER_EACH_ITERATION_EDEFAULT);
				return;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATION_OPTION:
				setNumIterationOption(NUM_ITERATION_OPTION_EDEFAULT);
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
			case RegiondefinitionPackage.RUN_MODE__MODE:
				return isSetMode();
			case RegiondefinitionPackage.RUN_MODE__RUN_MODE_INDEX:
				return isSetRunModeIndex();
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				return isSetNumIterations();
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNTIL_STOPPED:
				return isSetRepeatUntilStopped();
			case RegiondefinitionPackage.RUN_MODE__CONFIRM_AFTER_EACH_ITERATION:
				return confirmAfterEachIteration != CONFIRM_AFTER_EACH_ITERATION_EDEFAULT;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATION_OPTION:
				return numIterationOption != NUM_ITERATION_OPTION_EDEFAULT;
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
		result.append(" (mode: "); //$NON-NLS-1$
		if (modeESet) result.append(mode); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", runModeIndex: "); //$NON-NLS-1$
		if (runModeIndexESet) result.append(runModeIndex); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", numIterations: "); //$NON-NLS-1$
		if (numIterationsESet) result.append(numIterations); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", repeatUntilStopped: "); //$NON-NLS-1$
		if (repeatUntilStoppedESet) result.append(repeatUntilStopped); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", confirmAfterEachIteration: "); //$NON-NLS-1$
		result.append(confirmAfterEachIteration);
		result.append(", numIterationOption: "); //$NON-NLS-1$
		result.append(numIterationOption);
		result.append(')');
		return result.toString();
	}

} //RunModeImpl
