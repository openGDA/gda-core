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
import uk.ac.gda.excalibur.config.model.MasterModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Master Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MasterModelImpl#getFrameDivisor <em>Frame Divisor</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MasterModelImpl extends EObjectImpl implements MasterModel {
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MasterModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.MASTER_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getFrameDivisor() {
		return frameDivisor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFrameDivisor(int newFrameDivisor) {
		int oldFrameDivisor = frameDivisor;
		frameDivisor = newFrameDivisor;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MASTER_MODEL__FRAME_DIVISOR, oldFrameDivisor, frameDivisor));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.MASTER_MODEL__FRAME_DIVISOR:
				return getFrameDivisor();
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
			case ExcaliburConfigPackage.MASTER_MODEL__FRAME_DIVISOR:
				setFrameDivisor((Integer)newValue);
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
			case ExcaliburConfigPackage.MASTER_MODEL__FRAME_DIVISOR:
				setFrameDivisor(FRAME_DIVISOR_EDEFAULT);
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
			case ExcaliburConfigPackage.MASTER_MODEL__FRAME_DIVISOR:
				return frameDivisor != FRAME_DIVISOR_EDEFAULT;
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
		result.append(')');
		return result.toString();
	}

} //MasterModelImpl
