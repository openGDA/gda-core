/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Detector</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl#getFirstXChannel <em>First XChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl#getLastXChannel <em>Last XChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl#getFirstYChannel <em>First YChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl#getLastYChannel <em>Last YChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl#getSlices <em>Slices</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl#getDetectorMode <em>Detector Mode</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DetectorImpl extends EObjectImpl implements Detector {
	/**
	 * The default value of the '{@link #getFirstXChannel() <em>First XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstXChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int FIRST_XCHANNEL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFirstXChannel() <em>First XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstXChannel()
	 * @generated
	 * @ordered
	 */
	protected int firstXChannel = FIRST_XCHANNEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastXChannel() <em>Last XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastXChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int LAST_XCHANNEL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLastXChannel() <em>Last XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastXChannel()
	 * @generated
	 * @ordered
	 */
	protected int lastXChannel = LAST_XCHANNEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getFirstYChannel() <em>First YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstYChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int FIRST_YCHANNEL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFirstYChannel() <em>First YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstYChannel()
	 * @generated
	 * @ordered
	 */
	protected int firstYChannel = FIRST_YCHANNEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastYChannel() <em>Last YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastYChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int LAST_YCHANNEL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLastYChannel() <em>Last YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastYChannel()
	 * @generated
	 * @ordered
	 */
	protected int lastYChannel = LAST_YCHANNEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getSlices() <em>Slices</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSlices()
	 * @generated
	 * @ordered
	 */
	protected static final int SLICES_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSlices() <em>Slices</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSlices()
	 * @generated
	 * @ordered
	 */
	protected int slices = SLICES_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetectorMode() <em>Detector Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetectorMode()
	 * @generated
	 * @ordered
	 */
	protected static final DETECTOR_MODE DETECTOR_MODE_EDEFAULT = DETECTOR_MODE.ADC;

	/**
	 * The cached value of the '{@link #getDetectorMode() <em>Detector Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetectorMode()
	 * @generated
	 * @ordered
	 */
	protected DETECTOR_MODE detectorMode = DETECTOR_MODE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DetectorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.DETECTOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFirstXChannel() {
		return firstXChannel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFirstXChannel(int newFirstXChannel) {
		int oldFirstXChannel = firstXChannel;
		firstXChannel = newFirstXChannel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.DETECTOR__FIRST_XCHANNEL, oldFirstXChannel, firstXChannel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getLastXChannel() {
		return lastXChannel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLastXChannel(int newLastXChannel) {
		int oldLastXChannel = lastXChannel;
		lastXChannel = newLastXChannel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.DETECTOR__LAST_XCHANNEL, oldLastXChannel, lastXChannel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFirstYChannel() {
		return firstYChannel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFirstYChannel(int newFirstYChannel) {
		int oldFirstYChannel = firstYChannel;
		firstYChannel = newFirstYChannel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.DETECTOR__FIRST_YCHANNEL, oldFirstYChannel, firstYChannel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getLastYChannel() {
		return lastYChannel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLastYChannel(int newLastYChannel) {
		int oldLastYChannel = lastYChannel;
		lastYChannel = newLastYChannel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.DETECTOR__LAST_YCHANNEL, oldLastYChannel, lastYChannel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getSlices() {
		return slices;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSlices(int newSlices) {
		int oldSlices = slices;
		slices = newSlices;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.DETECTOR__SLICES, oldSlices, slices));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DETECTOR_MODE getDetectorMode() {
		return detectorMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetectorMode(DETECTOR_MODE newDetectorMode) {
		DETECTOR_MODE oldDetectorMode = detectorMode;
		detectorMode = newDetectorMode == null ? DETECTOR_MODE_EDEFAULT : newDetectorMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.DETECTOR__DETECTOR_MODE, oldDetectorMode, detectorMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegiondefinitionPackage.DETECTOR__FIRST_XCHANNEL:
				return getFirstXChannel();
			case RegiondefinitionPackage.DETECTOR__LAST_XCHANNEL:
				return getLastXChannel();
			case RegiondefinitionPackage.DETECTOR__FIRST_YCHANNEL:
				return getFirstYChannel();
			case RegiondefinitionPackage.DETECTOR__LAST_YCHANNEL:
				return getLastYChannel();
			case RegiondefinitionPackage.DETECTOR__SLICES:
				return getSlices();
			case RegiondefinitionPackage.DETECTOR__DETECTOR_MODE:
				return getDetectorMode();
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
			case RegiondefinitionPackage.DETECTOR__FIRST_XCHANNEL:
				setFirstXChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.DETECTOR__LAST_XCHANNEL:
				setLastXChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.DETECTOR__FIRST_YCHANNEL:
				setFirstYChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.DETECTOR__LAST_YCHANNEL:
				setLastYChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.DETECTOR__SLICES:
				setSlices((Integer)newValue);
				return;
			case RegiondefinitionPackage.DETECTOR__DETECTOR_MODE:
				setDetectorMode((DETECTOR_MODE)newValue);
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
			case RegiondefinitionPackage.DETECTOR__FIRST_XCHANNEL:
				setFirstXChannel(FIRST_XCHANNEL_EDEFAULT);
				return;
			case RegiondefinitionPackage.DETECTOR__LAST_XCHANNEL:
				setLastXChannel(LAST_XCHANNEL_EDEFAULT);
				return;
			case RegiondefinitionPackage.DETECTOR__FIRST_YCHANNEL:
				setFirstYChannel(FIRST_YCHANNEL_EDEFAULT);
				return;
			case RegiondefinitionPackage.DETECTOR__LAST_YCHANNEL:
				setLastYChannel(LAST_YCHANNEL_EDEFAULT);
				return;
			case RegiondefinitionPackage.DETECTOR__SLICES:
				setSlices(SLICES_EDEFAULT);
				return;
			case RegiondefinitionPackage.DETECTOR__DETECTOR_MODE:
				setDetectorMode(DETECTOR_MODE_EDEFAULT);
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
			case RegiondefinitionPackage.DETECTOR__FIRST_XCHANNEL:
				return firstXChannel != FIRST_XCHANNEL_EDEFAULT;
			case RegiondefinitionPackage.DETECTOR__LAST_XCHANNEL:
				return lastXChannel != LAST_XCHANNEL_EDEFAULT;
			case RegiondefinitionPackage.DETECTOR__FIRST_YCHANNEL:
				return firstYChannel != FIRST_YCHANNEL_EDEFAULT;
			case RegiondefinitionPackage.DETECTOR__LAST_YCHANNEL:
				return lastYChannel != LAST_YCHANNEL_EDEFAULT;
			case RegiondefinitionPackage.DETECTOR__SLICES:
				return slices != SLICES_EDEFAULT;
			case RegiondefinitionPackage.DETECTOR__DETECTOR_MODE:
				return detectorMode != DETECTOR_MODE_EDEFAULT;
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
		result.append(" (firstXChannel: ");
		result.append(firstXChannel);
		result.append(", lastXChannel: ");
		result.append(lastXChannel);
		result.append(", firstYChannel: ");
		result.append(firstYChannel);
		result.append(", lastYChannel: ");
		result.append(lastYChannel);
		result.append(", slices: ");
		result.append(slices);
		result.append(", detectorMode: ");
		result.append(detectorMode);
		result.append(')');
		return result.toString();
	}

} //DetectorImpl
