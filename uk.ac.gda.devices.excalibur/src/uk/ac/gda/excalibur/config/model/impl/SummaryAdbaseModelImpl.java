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
import uk.ac.gda.excalibur.config.model.SummaryAdbaseModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Summary Adbase Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl#getFrameDivisor <em>Frame Divisor</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl#getCounterDepth <em>Counter Depth</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl#getGapFillConstant <em>Gap Fill Constant</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SummaryAdbaseModelImpl extends EObjectImpl implements SummaryAdbaseModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getFrameDivisor() <em>Frame Divisor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFrameDivisor()
	 * @generated
	 * @ordered
	 */
	protected static final int FRAME_DIVISOR_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFrameDivisor() <em>Frame Divisor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFrameDivisor()
	 * @generated
	 * @ordered
	 */
	protected int frameDivisor = FRAME_DIVISOR_EDEFAULT;

	/**
	 * The default value of the '{@link #getCounterDepth() <em>Counter Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepth()
	 * @generated
	 * @ordered
	 */
	protected static final int COUNTER_DEPTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCounterDepth() <em>Counter Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepth()
	 * @generated
	 * @ordered
	 */
	protected int counterDepth = COUNTER_DEPTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getGapFillConstant() <em>Gap Fill Constant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGapFillConstant()
	 * @generated
	 * @ordered
	 */
	protected static final int GAP_FILL_CONSTANT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGapFillConstant() <em>Gap Fill Constant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGapFillConstant()
	 * @generated
	 * @ordered
	 */
	protected int gapFillConstant = GAP_FILL_CONSTANT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SummaryAdbaseModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.SUMMARY_ADBASE_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFrameDivisor() {
		return frameDivisor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFrameDivisor(int newFrameDivisor) {
		int oldFrameDivisor = frameDivisor;
		frameDivisor = newFrameDivisor;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__FRAME_DIVISOR, oldFrameDivisor, frameDivisor));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCounterDepth() {
		return counterDepth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCounterDepth(int newCounterDepth) {
		int oldCounterDepth = counterDepth;
		counterDepth = newCounterDepth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__COUNTER_DEPTH, oldCounterDepth, counterDepth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGapFillConstant() {
		return gapFillConstant;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGapFillConstant(int newGapFillConstant) {
		int oldGapFillConstant = gapFillConstant;
		gapFillConstant = newGapFillConstant;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT, oldGapFillConstant, gapFillConstant));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__FRAME_DIVISOR:
				return getFrameDivisor();
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__COUNTER_DEPTH:
				return getCounterDepth();
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT:
				return getGapFillConstant();
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
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__FRAME_DIVISOR:
				setFrameDivisor((Integer)newValue);
				return;
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__COUNTER_DEPTH:
				setCounterDepth((Integer)newValue);
				return;
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT:
				setGapFillConstant((Integer)newValue);
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
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__FRAME_DIVISOR:
				setFrameDivisor(FRAME_DIVISOR_EDEFAULT);
				return;
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__COUNTER_DEPTH:
				setCounterDepth(COUNTER_DEPTH_EDEFAULT);
				return;
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT:
				setGapFillConstant(GAP_FILL_CONSTANT_EDEFAULT);
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
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__FRAME_DIVISOR:
				return frameDivisor != FRAME_DIVISOR_EDEFAULT;
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__COUNTER_DEPTH:
				return counterDepth != COUNTER_DEPTH_EDEFAULT;
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT:
				return gapFillConstant != GAP_FILL_CONSTANT_EDEFAULT;
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
		result.append(" (frameDivisor: ");
		result.append(frameDivisor);
		result.append(", counterDepth: ");
		result.append(counterDepth);
		result.append(", gapFillConstant: ");
		result.append(gapFillConstant);
		result.append(')');
		return result.toString();
	}

} //SummaryAdbaseModelImpl
