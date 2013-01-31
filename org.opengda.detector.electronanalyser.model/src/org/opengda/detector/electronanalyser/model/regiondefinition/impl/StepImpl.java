/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Step;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Step</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl#getFrames <em>Frames</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl#getTime <em>Time</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl#getSize <em>Size</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl#getTotalTime <em>Total Time</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl#getTotalSteps <em>Total Steps</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StepImpl extends EObjectImpl implements Step {
	/**
	 * The default value of the '{@link #getFrames() <em>Frames</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFrames()
	 * @generated
	 * @ordered
	 */
	protected static final int FRAMES_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFrames() <em>Frames</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFrames()
	 * @generated
	 * @ordered
	 */
	protected int frames = FRAMES_EDEFAULT;

	/**
	 * The default value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected static final double TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected double time = TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected static final double SIZE_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected double size = SIZE_EDEFAULT;

	/**
	 * The default value of the '{@link #getTotalTime() <em>Total Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalTime()
	 * @generated
	 * @ordered
	 */
	protected static final double TOTAL_TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getTotalTime() <em>Total Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalTime()
	 * @generated
	 * @ordered
	 */
	protected double totalTime = TOTAL_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getTotalSteps() <em>Total Steps</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalSteps()
	 * @generated
	 * @ordered
	 */
	protected static final int TOTAL_STEPS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTotalSteps() <em>Total Steps</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalSteps()
	 * @generated
	 * @ordered
	 */
	protected int totalSteps = TOTAL_STEPS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StepImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.STEP;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFrames() {
		return frames;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFrames(int newFrames) {
		int oldFrames = frames;
		frames = newFrames;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.STEP__FRAMES, oldFrames, frames));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getTime() {
		return time;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTime(double newTime) {
		double oldTime = time;
		time = newTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.STEP__TIME, oldTime, time));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getSize() {
		return size;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSize(double newSize) {
		double oldSize = size;
		size = newSize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.STEP__SIZE, oldSize, size));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getTotalTime() {
		return totalTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTotalTime(double newTotalTime) {
		double oldTotalTime = totalTime;
		totalTime = newTotalTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.STEP__TOTAL_TIME, oldTotalTime, totalTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTotalSteps() {
		return totalSteps;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTotalSteps(int newTotalSteps) {
		int oldTotalSteps = totalSteps;
		totalSteps = newTotalSteps;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.STEP__TOTAL_STEPS, oldTotalSteps, totalSteps));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegiondefinitionPackage.STEP__FRAMES:
				return getFrames();
			case RegiondefinitionPackage.STEP__TIME:
				return getTime();
			case RegiondefinitionPackage.STEP__SIZE:
				return getSize();
			case RegiondefinitionPackage.STEP__TOTAL_TIME:
				return getTotalTime();
			case RegiondefinitionPackage.STEP__TOTAL_STEPS:
				return getTotalSteps();
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
			case RegiondefinitionPackage.STEP__FRAMES:
				setFrames((Integer)newValue);
				return;
			case RegiondefinitionPackage.STEP__TIME:
				setTime((Double)newValue);
				return;
			case RegiondefinitionPackage.STEP__SIZE:
				setSize((Double)newValue);
				return;
			case RegiondefinitionPackage.STEP__TOTAL_TIME:
				setTotalTime((Double)newValue);
				return;
			case RegiondefinitionPackage.STEP__TOTAL_STEPS:
				setTotalSteps((Integer)newValue);
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
			case RegiondefinitionPackage.STEP__FRAMES:
				setFrames(FRAMES_EDEFAULT);
				return;
			case RegiondefinitionPackage.STEP__TIME:
				setTime(TIME_EDEFAULT);
				return;
			case RegiondefinitionPackage.STEP__SIZE:
				setSize(SIZE_EDEFAULT);
				return;
			case RegiondefinitionPackage.STEP__TOTAL_TIME:
				setTotalTime(TOTAL_TIME_EDEFAULT);
				return;
			case RegiondefinitionPackage.STEP__TOTAL_STEPS:
				setTotalSteps(TOTAL_STEPS_EDEFAULT);
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
			case RegiondefinitionPackage.STEP__FRAMES:
				return frames != FRAMES_EDEFAULT;
			case RegiondefinitionPackage.STEP__TIME:
				return time != TIME_EDEFAULT;
			case RegiondefinitionPackage.STEP__SIZE:
				return size != SIZE_EDEFAULT;
			case RegiondefinitionPackage.STEP__TOTAL_TIME:
				return totalTime != TOTAL_TIME_EDEFAULT;
			case RegiondefinitionPackage.STEP__TOTAL_STEPS:
				return totalSteps != TOTAL_STEPS_EDEFAULT;
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
		result.append(" (frames: ");
		result.append(frames);
		result.append(", time: ");
		result.append(time);
		result.append(", size: ");
		result.append(size);
		result.append(", totalTime: ");
		result.append(totalTime);
		result.append(", totalSteps: ");
		result.append(totalSteps);
		result.append(')');
		return result.toString();
	}

} //StepImpl
