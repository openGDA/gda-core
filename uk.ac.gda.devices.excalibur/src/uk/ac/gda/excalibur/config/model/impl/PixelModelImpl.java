/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.PixelModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Pixel Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl#getMask <em>Mask</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl#getTest <em>Test</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl#getGainMode <em>Gain Mode</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl#getThresholdA <em>Threshold A</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl#getThresholdB <em>Threshold B</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PixelModelImpl extends EObjectImpl implements PixelModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getMask() <em>Mask</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMask()
	 * @generated
	 * @ordered
	 */
	protected static final short[] MASK_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMask() <em>Mask</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMask()
	 * @generated
	 * @ordered
	 */
	protected short[] mask = MASK_EDEFAULT;

	/**
	 * The default value of the '{@link #getTest() <em>Test</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTest()
	 * @generated
	 * @ordered
	 */
	protected static final short[] TEST_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTest() <em>Test</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTest()
	 * @generated
	 * @ordered
	 */
	protected short[] test = TEST_EDEFAULT;

	/**
	 * The default value of the '{@link #getGainMode() <em>Gain Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGainMode()
	 * @generated
	 * @ordered
	 */
	protected static final short[] GAIN_MODE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGainMode() <em>Gain Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGainMode()
	 * @generated
	 * @ordered
	 */
	protected short[] gainMode = GAIN_MODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getThresholdA() <em>Threshold A</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThresholdA()
	 * @generated
	 * @ordered
	 */
	protected static final short[] THRESHOLD_A_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getThresholdA() <em>Threshold A</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThresholdA()
	 * @generated
	 * @ordered
	 */
	protected short[] thresholdA = THRESHOLD_A_EDEFAULT;

	/**
	 * The default value of the '{@link #getThresholdB() <em>Threshold B</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThresholdB()
	 * @generated
	 * @ordered
	 */
	protected static final short[] THRESHOLD_B_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getThresholdB() <em>Threshold B</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThresholdB()
	 * @generated
	 * @ordered
	 */
	protected short[] thresholdB = THRESHOLD_B_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PixelModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.PIXEL_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public short[] getMask() {
		return mask;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMask(short[] newMask) {
		short[] oldMask = mask;
		mask = newMask;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.PIXEL_MODEL__MASK, oldMask, mask));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public short[] getTest() {
		return test;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTest(short[] newTest) {
		short[] oldTest = test;
		test = newTest;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.PIXEL_MODEL__TEST, oldTest, test));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public short[] getGainMode() {
		return gainMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGainMode(short[] newGainMode) {
		short[] oldGainMode = gainMode;
		gainMode = newGainMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.PIXEL_MODEL__GAIN_MODE, oldGainMode, gainMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public short[] getThresholdA() {
		return thresholdA;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThresholdA(short[] newThresholdA) {
		short[] oldThresholdA = thresholdA;
		thresholdA = newThresholdA;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_A, oldThresholdA, thresholdA));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public short[] getThresholdB() {
		return thresholdB;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThresholdB(short[] newThresholdB) {
		short[] oldThresholdB = thresholdB;
		thresholdB = newThresholdB;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_B, oldThresholdB, thresholdB));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.PIXEL_MODEL__MASK:
				return getMask();
			case ExcaliburConfigPackage.PIXEL_MODEL__TEST:
				return getTest();
			case ExcaliburConfigPackage.PIXEL_MODEL__GAIN_MODE:
				return getGainMode();
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_A:
				return getThresholdA();
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_B:
				return getThresholdB();
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
			case ExcaliburConfigPackage.PIXEL_MODEL__MASK:
				setMask((short[])newValue);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__TEST:
				setTest((short[])newValue);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__GAIN_MODE:
				setGainMode((short[])newValue);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_A:
				setThresholdA((short[])newValue);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_B:
				setThresholdB((short[])newValue);
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
			case ExcaliburConfigPackage.PIXEL_MODEL__MASK:
				setMask(MASK_EDEFAULT);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__TEST:
				setTest(TEST_EDEFAULT);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__GAIN_MODE:
				setGainMode(GAIN_MODE_EDEFAULT);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_A:
				setThresholdA(THRESHOLD_A_EDEFAULT);
				return;
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_B:
				setThresholdB(THRESHOLD_B_EDEFAULT);
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
			case ExcaliburConfigPackage.PIXEL_MODEL__MASK:
				return MASK_EDEFAULT == null ? mask != null : !MASK_EDEFAULT.equals(mask);
			case ExcaliburConfigPackage.PIXEL_MODEL__TEST:
				return TEST_EDEFAULT == null ? test != null : !TEST_EDEFAULT.equals(test);
			case ExcaliburConfigPackage.PIXEL_MODEL__GAIN_MODE:
				return GAIN_MODE_EDEFAULT == null ? gainMode != null : !GAIN_MODE_EDEFAULT.equals(gainMode);
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_A:
				return THRESHOLD_A_EDEFAULT == null ? thresholdA != null : !THRESHOLD_A_EDEFAULT.equals(thresholdA);
			case ExcaliburConfigPackage.PIXEL_MODEL__THRESHOLD_B:
				return THRESHOLD_B_EDEFAULT == null ? thresholdB != null : !THRESHOLD_B_EDEFAULT.equals(thresholdB);
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
		result.append(" (mask: ");
		result.append(mask);
		result.append(", test: ");
		result.append(test);
		result.append(", gainMode: ");
		result.append(gainMode);
		result.append(", thresholdA: ");
		result.append(thresholdA);
		result.append(", thresholdB: ");
		result.append(thresholdB);
		result.append(')');
		return result.toString();
	}

} //PixelModelImpl
