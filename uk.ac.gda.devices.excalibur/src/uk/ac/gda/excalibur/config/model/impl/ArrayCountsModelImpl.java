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

import uk.ac.gda.excalibur.config.model.ArrayCountsModel;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Array Counts Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl#getArrayCountFem1 <em>Array Count Fem1</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl#getArrayCountFem2 <em>Array Count Fem2</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl#getArrayCountFem3 <em>Array Count Fem3</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl#getArrayCountFem4 <em>Array Count Fem4</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl#getArrayCountFem5 <em>Array Count Fem5</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl#getArrayCountFem6 <em>Array Count Fem6</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ArrayCountsModelImpl extends EObjectImpl implements ArrayCountsModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getArrayCountFem1() <em>Array Count Fem1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem1()
	 * @generated
	 * @ordered
	 */
	protected static final int ARRAY_COUNT_FEM1_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getArrayCountFem1() <em>Array Count Fem1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem1()
	 * @generated
	 * @ordered
	 */
	protected int arrayCountFem1 = ARRAY_COUNT_FEM1_EDEFAULT;

	/**
	 * The default value of the '{@link #getArrayCountFem2() <em>Array Count Fem2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem2()
	 * @generated
	 * @ordered
	 */
	protected static final int ARRAY_COUNT_FEM2_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getArrayCountFem2() <em>Array Count Fem2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem2()
	 * @generated
	 * @ordered
	 */
	protected int arrayCountFem2 = ARRAY_COUNT_FEM2_EDEFAULT;

	/**
	 * The default value of the '{@link #getArrayCountFem3() <em>Array Count Fem3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem3()
	 * @generated
	 * @ordered
	 */
	protected static final int ARRAY_COUNT_FEM3_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getArrayCountFem3() <em>Array Count Fem3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem3()
	 * @generated
	 * @ordered
	 */
	protected int arrayCountFem3 = ARRAY_COUNT_FEM3_EDEFAULT;

	/**
	 * The default value of the '{@link #getArrayCountFem4() <em>Array Count Fem4</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem4()
	 * @generated
	 * @ordered
	 */
	protected static final int ARRAY_COUNT_FEM4_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getArrayCountFem4() <em>Array Count Fem4</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem4()
	 * @generated
	 * @ordered
	 */
	protected int arrayCountFem4 = ARRAY_COUNT_FEM4_EDEFAULT;

	/**
	 * The default value of the '{@link #getArrayCountFem5() <em>Array Count Fem5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem5()
	 * @generated
	 * @ordered
	 */
	protected static final int ARRAY_COUNT_FEM5_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getArrayCountFem5() <em>Array Count Fem5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem5()
	 * @generated
	 * @ordered
	 */
	protected int arrayCountFem5 = ARRAY_COUNT_FEM5_EDEFAULT;

	/**
	 * The default value of the '{@link #getArrayCountFem6() <em>Array Count Fem6</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem6()
	 * @generated
	 * @ordered
	 */
	protected static final int ARRAY_COUNT_FEM6_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getArrayCountFem6() <em>Array Count Fem6</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArrayCountFem6()
	 * @generated
	 * @ordered
	 */
	protected int arrayCountFem6 = ARRAY_COUNT_FEM6_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ArrayCountsModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.ARRAY_COUNTS_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getArrayCountFem1() {
		return arrayCountFem1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArrayCountFem1(int newArrayCountFem1) {
		int oldArrayCountFem1 = arrayCountFem1;
		arrayCountFem1 = newArrayCountFem1;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1, oldArrayCountFem1, arrayCountFem1));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getArrayCountFem2() {
		return arrayCountFem2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArrayCountFem2(int newArrayCountFem2) {
		int oldArrayCountFem2 = arrayCountFem2;
		arrayCountFem2 = newArrayCountFem2;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2, oldArrayCountFem2, arrayCountFem2));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getArrayCountFem3() {
		return arrayCountFem3;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArrayCountFem3(int newArrayCountFem3) {
		int oldArrayCountFem3 = arrayCountFem3;
		arrayCountFem3 = newArrayCountFem3;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3, oldArrayCountFem3, arrayCountFem3));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getArrayCountFem4() {
		return arrayCountFem4;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArrayCountFem4(int newArrayCountFem4) {
		int oldArrayCountFem4 = arrayCountFem4;
		arrayCountFem4 = newArrayCountFem4;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4, oldArrayCountFem4, arrayCountFem4));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getArrayCountFem5() {
		return arrayCountFem5;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArrayCountFem5(int newArrayCountFem5) {
		int oldArrayCountFem5 = arrayCountFem5;
		arrayCountFem5 = newArrayCountFem5;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5, oldArrayCountFem5, arrayCountFem5));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getArrayCountFem6() {
		return arrayCountFem6;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArrayCountFem6(int newArrayCountFem6) {
		int oldArrayCountFem6 = arrayCountFem6;
		arrayCountFem6 = newArrayCountFem6;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6, oldArrayCountFem6, arrayCountFem6));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1:
				return getArrayCountFem1();
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2:
				return getArrayCountFem2();
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3:
				return getArrayCountFem3();
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4:
				return getArrayCountFem4();
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5:
				return getArrayCountFem5();
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6:
				return getArrayCountFem6();
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
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1:
				setArrayCountFem1((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2:
				setArrayCountFem2((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3:
				setArrayCountFem3((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4:
				setArrayCountFem4((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5:
				setArrayCountFem5((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6:
				setArrayCountFem6((Integer)newValue);
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
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1:
				setArrayCountFem1(ARRAY_COUNT_FEM1_EDEFAULT);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2:
				setArrayCountFem2(ARRAY_COUNT_FEM2_EDEFAULT);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3:
				setArrayCountFem3(ARRAY_COUNT_FEM3_EDEFAULT);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4:
				setArrayCountFem4(ARRAY_COUNT_FEM4_EDEFAULT);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5:
				setArrayCountFem5(ARRAY_COUNT_FEM5_EDEFAULT);
				return;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6:
				setArrayCountFem6(ARRAY_COUNT_FEM6_EDEFAULT);
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
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1:
				return arrayCountFem1 != ARRAY_COUNT_FEM1_EDEFAULT;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2:
				return arrayCountFem2 != ARRAY_COUNT_FEM2_EDEFAULT;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3:
				return arrayCountFem3 != ARRAY_COUNT_FEM3_EDEFAULT;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4:
				return arrayCountFem4 != ARRAY_COUNT_FEM4_EDEFAULT;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5:
				return arrayCountFem5 != ARRAY_COUNT_FEM5_EDEFAULT;
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6:
				return arrayCountFem6 != ARRAY_COUNT_FEM6_EDEFAULT;
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
		result.append(" (arrayCountFem1: ");
		result.append(arrayCountFem1);
		result.append(", arrayCountFem2: ");
		result.append(arrayCountFem2);
		result.append(", arrayCountFem3: ");
		result.append(arrayCountFem3);
		result.append(", arrayCountFem4: ");
		result.append(arrayCountFem4);
		result.append(", arrayCountFem5: ");
		result.append(arrayCountFem5);
		result.append(", arrayCountFem6: ");
		result.append(arrayCountFem6);
		result.append(')');
		return result.toString();
	}

} //ArrayCountsModelImpl
