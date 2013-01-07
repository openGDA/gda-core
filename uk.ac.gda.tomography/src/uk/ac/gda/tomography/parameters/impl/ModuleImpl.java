/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Module</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.ModuleImpl#getModuleNumber <em>Module Number</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.ModuleImpl#getCameraMagnification <em>Camera Magnification</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModuleImpl extends EObjectImpl implements Module {
	/**
	 * The default value of the '{@link #getModuleNumber() <em>Module Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModuleNumber()
	 * @generated
	 * @ordered
	 */
	protected static final Integer MODULE_NUMBER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getModuleNumber() <em>Module Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModuleNumber()
	 * @generated
	 * @ordered
	 */
	protected Integer moduleNumber = MODULE_NUMBER_EDEFAULT;

	/**
	 * This is true if the Module Number attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean moduleNumberESet;

	/**
	 * The default value of the '{@link #getCameraMagnification() <em>Camera Magnification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCameraMagnification()
	 * @generated
	 * @ordered
	 */
	protected static final double CAMERA_MAGNIFICATION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCameraMagnification() <em>Camera Magnification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCameraMagnification()
	 * @generated
	 * @ordered
	 */
	protected double cameraMagnification = CAMERA_MAGNIFICATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModuleImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.MODULE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getModuleNumber() {
		return moduleNumber;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setModuleNumber(Integer newModuleNumber) {
		Integer oldModuleNumber = moduleNumber;
		moduleNumber = newModuleNumber;
		boolean oldModuleNumberESet = moduleNumberESet;
		moduleNumberESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.MODULE__MODULE_NUMBER, oldModuleNumber, moduleNumber, !oldModuleNumberESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetModuleNumber() {
		Integer oldModuleNumber = moduleNumber;
		boolean oldModuleNumberESet = moduleNumberESet;
		moduleNumber = MODULE_NUMBER_EDEFAULT;
		moduleNumberESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.MODULE__MODULE_NUMBER, oldModuleNumber, MODULE_NUMBER_EDEFAULT, oldModuleNumberESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetModuleNumber() {
		return moduleNumberESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCameraMagnification() {
		return cameraMagnification;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCameraMagnification(double newCameraMagnification) {
		double oldCameraMagnification = cameraMagnification;
		cameraMagnification = newCameraMagnification;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.MODULE__CAMERA_MAGNIFICATION, oldCameraMagnification, cameraMagnification));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.MODULE__MODULE_NUMBER:
				return getModuleNumber();
			case TomoParametersPackage.MODULE__CAMERA_MAGNIFICATION:
				return getCameraMagnification();
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
			case TomoParametersPackage.MODULE__MODULE_NUMBER:
				setModuleNumber((Integer)newValue);
				return;
			case TomoParametersPackage.MODULE__CAMERA_MAGNIFICATION:
				setCameraMagnification((Double)newValue);
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
			case TomoParametersPackage.MODULE__MODULE_NUMBER:
				unsetModuleNumber();
				return;
			case TomoParametersPackage.MODULE__CAMERA_MAGNIFICATION:
				setCameraMagnification(CAMERA_MAGNIFICATION_EDEFAULT);
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
			case TomoParametersPackage.MODULE__MODULE_NUMBER:
				return isSetModuleNumber();
			case TomoParametersPackage.MODULE__CAMERA_MAGNIFICATION:
				return cameraMagnification != CAMERA_MAGNIFICATION_EDEFAULT;
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
		result.append(" (moduleNumber: ");
		if (moduleNumberESet) result.append(moduleNumber); else result.append("<unset>");
		result.append(", cameraMagnification: ");
		result.append(cameraMagnification);
		result.append(')');
		return result.toString();
	}

} //ModuleImpl
