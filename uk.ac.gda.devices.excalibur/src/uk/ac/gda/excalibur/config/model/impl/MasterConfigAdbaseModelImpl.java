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
import uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Master Config Adbase Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MasterConfigAdbaseModelImpl#getCounterDepth <em>Counter Depth</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MasterConfigAdbaseModelImpl extends EObjectImpl implements MasterConfigAdbaseModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MasterConfigAdbaseModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.MASTER_CONFIG_ADBASE_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getCounterDepth() {
		return counterDepth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCounterDepth(int newCounterDepth) {
		int oldCounterDepth = counterDepth;
		counterDepth = newCounterDepth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH, oldCounterDepth, counterDepth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH:
				return getCounterDepth();
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
			case ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH:
				setCounterDepth((Integer)newValue);
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
			case ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH:
				setCounterDepth(COUNTER_DEPTH_EDEFAULT);
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
			case ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH:
				return counterDepth != COUNTER_DEPTH_EDEFAULT;
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
		result.append(" (counterDepth: ");
		result.append(counterDepth);
		result.append(')');
		return result.toString();
	}

} //MasterConfigAdbaseModelImpl
