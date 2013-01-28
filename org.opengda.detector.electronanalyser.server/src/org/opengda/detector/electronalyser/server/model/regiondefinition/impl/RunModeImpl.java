/**
 */
package org.opengda.detector.electronalyser.server.model.regiondefinition.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES;
import org.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage;
import org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Run Mode</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl#getMode <em>Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl#isNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}</li>
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
	protected static final RUN_MODES MODE_EDEFAULT = RUN_MODES.NORMAL;

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
	 * The default value of the '{@link #isNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNumIterations()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NUM_ITERATIONS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNumIterations() <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNumIterations()
	 * @generated
	 * @ordered
	 */
	protected boolean numIterations = NUM_ITERATIONS_EDEFAULT;

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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__MODE, oldMode, mode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isNumIterations() {
		return numIterations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumIterations(boolean newNumIterations) {
		boolean oldNumIterations = numIterations;
		numIterations = newNumIterations;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS, oldNumIterations, numIterations));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.RUN_MODE__REPEAT_UNITIL_STOPPED, oldRepeatUnitilStopped, repeatUnitilStopped));
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
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				return isNumIterations();
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNITIL_STOPPED:
				return isRepeatUnitilStopped();
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
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				setNumIterations((Boolean)newValue);
				return;
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNITIL_STOPPED:
				setRepeatUnitilStopped((Boolean)newValue);
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
				setMode(MODE_EDEFAULT);
				return;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				setNumIterations(NUM_ITERATIONS_EDEFAULT);
				return;
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNITIL_STOPPED:
				setRepeatUnitilStopped(REPEAT_UNITIL_STOPPED_EDEFAULT);
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
				return mode != MODE_EDEFAULT;
			case RegiondefinitionPackage.RUN_MODE__NUM_ITERATIONS:
				return numIterations != NUM_ITERATIONS_EDEFAULT;
			case RegiondefinitionPackage.RUN_MODE__REPEAT_UNITIL_STOPPED:
				return repeatUnitilStopped != REPEAT_UNITIL_STOPPED_EDEFAULT;
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
		result.append(" (mode: ");
		result.append(mode);
		result.append(", numIterations: ");
		result.append(numIterations);
		result.append(", repeatUnitilStopped: ");
		result.append(repeatUnitilStopped);
		result.append(')');
		return result.toString();
	}

} //RunModeImpl
