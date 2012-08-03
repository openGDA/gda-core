/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.ValueUnit;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Module</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.ModuleImpl#getModuleNumber <em>Module Number</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.ModuleImpl#getHorizontalFieldOfView <em>Horizontal Field Of View</em>}</li>
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
	 * The cached value of the '{@link #getHorizontalFieldOfView() <em>Horizontal Field Of View</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHorizontalFieldOfView()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit horizontalFieldOfView;

	/**
	 * This is true if the Horizontal Field Of View containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean horizontalFieldOfViewESet;

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
	public Integer getModuleNumber() {
		return moduleNumber;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
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
	public boolean isSetModuleNumber() {
		return moduleNumberESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getHorizontalFieldOfView() {
		return horizontalFieldOfView;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHorizontalFieldOfView(ValueUnit newHorizontalFieldOfView, NotificationChain msgs) {
		ValueUnit oldHorizontalFieldOfView = horizontalFieldOfView;
		horizontalFieldOfView = newHorizontalFieldOfView;
		boolean oldHorizontalFieldOfViewESet = horizontalFieldOfViewESet;
		horizontalFieldOfViewESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, oldHorizontalFieldOfView, newHorizontalFieldOfView, !oldHorizontalFieldOfViewESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHorizontalFieldOfView(ValueUnit newHorizontalFieldOfView) {
		if (newHorizontalFieldOfView != horizontalFieldOfView) {
			NotificationChain msgs = null;
			if (horizontalFieldOfView != null)
				msgs = ((InternalEObject)horizontalFieldOfView).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, null, msgs);
			if (newHorizontalFieldOfView != null)
				msgs = ((InternalEObject)newHorizontalFieldOfView).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, null, msgs);
			msgs = basicSetHorizontalFieldOfView(newHorizontalFieldOfView, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldHorizontalFieldOfViewESet = horizontalFieldOfViewESet;
			horizontalFieldOfViewESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, newHorizontalFieldOfView, newHorizontalFieldOfView, !oldHorizontalFieldOfViewESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetHorizontalFieldOfView(NotificationChain msgs) {
		ValueUnit oldHorizontalFieldOfView = horizontalFieldOfView;
		horizontalFieldOfView = null;
		boolean oldHorizontalFieldOfViewESet = horizontalFieldOfViewESet;
		horizontalFieldOfViewESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, oldHorizontalFieldOfView, null, oldHorizontalFieldOfViewESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetHorizontalFieldOfView() {
		if (horizontalFieldOfView != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)horizontalFieldOfView).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, null, msgs);
			msgs = basicUnsetHorizontalFieldOfView(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldHorizontalFieldOfViewESet = horizontalFieldOfViewESet;
			horizontalFieldOfViewESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW, null, null, oldHorizontalFieldOfViewESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetHorizontalFieldOfView() {
		return horizontalFieldOfViewESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW:
				return basicUnsetHorizontalFieldOfView(msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
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
			case TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW:
				return getHorizontalFieldOfView();
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
			case TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW:
				setHorizontalFieldOfView((ValueUnit)newValue);
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
			case TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW:
				unsetHorizontalFieldOfView();
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
			case TomoParametersPackage.MODULE__HORIZONTAL_FIELD_OF_VIEW:
				return isSetHorizontalFieldOfView();
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
		result.append(')');
		return result.toString();
	}

} //ModuleImpl
