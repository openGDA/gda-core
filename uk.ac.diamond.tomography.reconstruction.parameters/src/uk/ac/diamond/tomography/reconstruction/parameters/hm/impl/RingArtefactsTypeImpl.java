/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import java.math.BigDecimal;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Ring Artefacts Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl#getParameterN <em>Parameter N</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl#getParameterR <em>Parameter R</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl#getNumSeries <em>Num Series</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RingArtefactsTypeImpl extends EObjectImpl implements RingArtefactsType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType5 type;

	/**
	 * The default value of the '{@link #getParameterN() <em>Parameter N</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameterN()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal PARAMETER_N_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getParameterN() <em>Parameter N</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameterN()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal parameterN = PARAMETER_N_EDEFAULT;

	/**
	 * The default value of the '{@link #getParameterR() <em>Parameter R</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameterR()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal PARAMETER_R_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getParameterR() <em>Parameter R</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameterR()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal parameterR = PARAMETER_R_EDEFAULT;

	/**
	 * The cached value of the '{@link #getNumSeries() <em>Num Series</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumSeries()
	 * @generated
	 * @ordered
	 */
	protected NumSeriesType numSeries;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RingArtefactsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.RING_ARTEFACTS_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType5 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType5 newType, NotificationChain msgs) {
		TypeType5 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.RING_ARTEFACTS_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType5 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.RING_ARTEFACTS_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.RING_ARTEFACTS_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RING_ARTEFACTS_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getParameterN() {
		return parameterN;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParameterN(BigDecimal newParameterN) {
		BigDecimal oldParameterN = parameterN;
		parameterN = newParameterN;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_N, oldParameterN, parameterN));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getParameterR() {
		return parameterR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParameterR(BigDecimal newParameterR) {
		BigDecimal oldParameterR = parameterR;
		parameterR = newParameterR;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_R, oldParameterR, parameterR));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NumSeriesType getNumSeries() {
		return numSeries;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNumSeries(NumSeriesType newNumSeries, NotificationChain msgs) {
		NumSeriesType oldNumSeries = numSeries;
		numSeries = newNumSeries;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES, oldNumSeries, newNumSeries);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumSeries(NumSeriesType newNumSeries) {
		if (newNumSeries != numSeries) {
			NotificationChain msgs = null;
			if (numSeries != null)
				msgs = ((InternalEObject)numSeries).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES, null, msgs);
			if (newNumSeries != null)
				msgs = ((InternalEObject)newNumSeries).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES, null, msgs);
			msgs = basicSetNumSeries(newNumSeries, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES, newNumSeries, newNumSeries));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.RING_ARTEFACTS_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES:
				return basicSetNumSeries(null, msgs);
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
			case HmPackage.RING_ARTEFACTS_TYPE__TYPE:
				return getType();
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_N:
				return getParameterN();
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_R:
				return getParameterR();
			case HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES:
				return getNumSeries();
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
			case HmPackage.RING_ARTEFACTS_TYPE__TYPE:
				setType((TypeType5)newValue);
				return;
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_N:
				setParameterN((BigDecimal)newValue);
				return;
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_R:
				setParameterR((BigDecimal)newValue);
				return;
			case HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES:
				setNumSeries((NumSeriesType)newValue);
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
			case HmPackage.RING_ARTEFACTS_TYPE__TYPE:
				setType((TypeType5)null);
				return;
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_N:
				setParameterN(PARAMETER_N_EDEFAULT);
				return;
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_R:
				setParameterR(PARAMETER_R_EDEFAULT);
				return;
			case HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES:
				setNumSeries((NumSeriesType)null);
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
			case HmPackage.RING_ARTEFACTS_TYPE__TYPE:
				return type != null;
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_N:
				return PARAMETER_N_EDEFAULT == null ? parameterN != null : !PARAMETER_N_EDEFAULT.equals(parameterN);
			case HmPackage.RING_ARTEFACTS_TYPE__PARAMETER_R:
				return PARAMETER_R_EDEFAULT == null ? parameterR != null : !PARAMETER_R_EDEFAULT.equals(parameterR);
			case HmPackage.RING_ARTEFACTS_TYPE__NUM_SERIES:
				return numSeries != null;
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
		result.append(" (parameterN: ");
		result.append(parameterN);
		result.append(", parameterR: ");
		result.append(parameterR);
		result.append(')');
		return result.toString();
	}

} //RingArtefactsTypeImpl
