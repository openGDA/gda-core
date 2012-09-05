/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Circles Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl#getValueMin <em>Value Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl#getValueMax <em>Value Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl#getValueStep <em>Value Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl#getComm <em>Comm</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CirclesTypeImpl extends EObjectImpl implements CirclesType {
	/**
	 * The cached value of the '{@link #getValueMin() <em>Value Min</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueMin()
	 * @generated
	 * @ordered
	 */
	protected ValueMinType valueMin;

	/**
	 * The cached value of the '{@link #getValueMax() <em>Value Max</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueMax()
	 * @generated
	 * @ordered
	 */
	protected ValueMaxType valueMax;

	/**
	 * The cached value of the '{@link #getValueStep() <em>Value Step</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueStep()
	 * @generated
	 * @ordered
	 */
	protected ValueStepType valueStep;

	/**
	 * The default value of the '{@link #getComm() <em>Comm</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComm()
	 * @generated
	 * @ordered
	 */
	protected static final String COMM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComm() <em>Comm</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComm()
	 * @generated
	 * @ordered
	 */
	protected String comm = COMM_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CirclesTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.CIRCLES_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueMinType getValueMin() {
		return valueMin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetValueMin(ValueMinType newValueMin, NotificationChain msgs) {
		ValueMinType oldValueMin = valueMin;
		valueMin = newValueMin;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__VALUE_MIN, oldValueMin, newValueMin);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueMin(ValueMinType newValueMin) {
		if (newValueMin != valueMin) {
			NotificationChain msgs = null;
			if (valueMin != null)
				msgs = ((InternalEObject)valueMin).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.CIRCLES_TYPE__VALUE_MIN, null, msgs);
			if (newValueMin != null)
				msgs = ((InternalEObject)newValueMin).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.CIRCLES_TYPE__VALUE_MIN, null, msgs);
			msgs = basicSetValueMin(newValueMin, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__VALUE_MIN, newValueMin, newValueMin));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueMaxType getValueMax() {
		return valueMax;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetValueMax(ValueMaxType newValueMax, NotificationChain msgs) {
		ValueMaxType oldValueMax = valueMax;
		valueMax = newValueMax;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__VALUE_MAX, oldValueMax, newValueMax);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueMax(ValueMaxType newValueMax) {
		if (newValueMax != valueMax) {
			NotificationChain msgs = null;
			if (valueMax != null)
				msgs = ((InternalEObject)valueMax).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.CIRCLES_TYPE__VALUE_MAX, null, msgs);
			if (newValueMax != null)
				msgs = ((InternalEObject)newValueMax).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.CIRCLES_TYPE__VALUE_MAX, null, msgs);
			msgs = basicSetValueMax(newValueMax, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__VALUE_MAX, newValueMax, newValueMax));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueStepType getValueStep() {
		return valueStep;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetValueStep(ValueStepType newValueStep, NotificationChain msgs) {
		ValueStepType oldValueStep = valueStep;
		valueStep = newValueStep;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__VALUE_STEP, oldValueStep, newValueStep);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueStep(ValueStepType newValueStep) {
		if (newValueStep != valueStep) {
			NotificationChain msgs = null;
			if (valueStep != null)
				msgs = ((InternalEObject)valueStep).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.CIRCLES_TYPE__VALUE_STEP, null, msgs);
			if (newValueStep != null)
				msgs = ((InternalEObject)newValueStep).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.CIRCLES_TYPE__VALUE_STEP, null, msgs);
			msgs = basicSetValueStep(newValueStep, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__VALUE_STEP, newValueStep, newValueStep));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getComm() {
		return comm;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setComm(String newComm) {
		String oldComm = comm;
		comm = newComm;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.CIRCLES_TYPE__COMM, oldComm, comm));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.CIRCLES_TYPE__VALUE_MIN:
				return basicSetValueMin(null, msgs);
			case HmPackage.CIRCLES_TYPE__VALUE_MAX:
				return basicSetValueMax(null, msgs);
			case HmPackage.CIRCLES_TYPE__VALUE_STEP:
				return basicSetValueStep(null, msgs);
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
			case HmPackage.CIRCLES_TYPE__VALUE_MIN:
				return getValueMin();
			case HmPackage.CIRCLES_TYPE__VALUE_MAX:
				return getValueMax();
			case HmPackage.CIRCLES_TYPE__VALUE_STEP:
				return getValueStep();
			case HmPackage.CIRCLES_TYPE__COMM:
				return getComm();
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
			case HmPackage.CIRCLES_TYPE__VALUE_MIN:
				setValueMin((ValueMinType)newValue);
				return;
			case HmPackage.CIRCLES_TYPE__VALUE_MAX:
				setValueMax((ValueMaxType)newValue);
				return;
			case HmPackage.CIRCLES_TYPE__VALUE_STEP:
				setValueStep((ValueStepType)newValue);
				return;
			case HmPackage.CIRCLES_TYPE__COMM:
				setComm((String)newValue);
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
			case HmPackage.CIRCLES_TYPE__VALUE_MIN:
				setValueMin((ValueMinType)null);
				return;
			case HmPackage.CIRCLES_TYPE__VALUE_MAX:
				setValueMax((ValueMaxType)null);
				return;
			case HmPackage.CIRCLES_TYPE__VALUE_STEP:
				setValueStep((ValueStepType)null);
				return;
			case HmPackage.CIRCLES_TYPE__COMM:
				setComm(COMM_EDEFAULT);
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
			case HmPackage.CIRCLES_TYPE__VALUE_MIN:
				return valueMin != null;
			case HmPackage.CIRCLES_TYPE__VALUE_MAX:
				return valueMax != null;
			case HmPackage.CIRCLES_TYPE__VALUE_STEP:
				return valueStep != null;
			case HmPackage.CIRCLES_TYPE__COMM:
				return COMM_EDEFAULT == null ? comm != null : !COMM_EDEFAULT.equals(comm);
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
		result.append(" (comm: ");
		result.append(comm);
		result.append(')');
		return result.toString();
	}

} //CirclesTypeImpl
