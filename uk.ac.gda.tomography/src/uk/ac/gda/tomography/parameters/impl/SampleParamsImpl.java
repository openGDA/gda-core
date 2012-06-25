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

import uk.ac.gda.tomography.parameters.SampleParams;
import uk.ac.gda.tomography.parameters.SamplePosition;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sample Params</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleParamsImpl#getPosition <em>Position</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleParamsImpl#getWeight <em>Weight</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SampleParamsImpl extends EObjectImpl implements SampleParams {
	/**
	 * The cached value of the '{@link #getPosition() <em>Position</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPosition()
	 * @generated
	 * @ordered
	 */
	protected SamplePosition position;

	/**
	 * This is true if the Position containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean positionESet;

	/**
	 * The default value of the '{@link #getWeight() <em>Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeight()
	 * @generated
	 * @ordered
	 */
	protected static final double WEIGHT_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getWeight() <em>Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeight()
	 * @generated
	 * @ordered
	 */
	protected double weight = WEIGHT_EDEFAULT;

	/**
	 * This is true if the Weight attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean weightESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SampleParamsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.SAMPLE_PARAMS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SamplePosition getPosition() {
		return position;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPosition(SamplePosition newPosition, NotificationChain msgs) {
		SamplePosition oldPosition = position;
		position = newPosition;
		boolean oldPositionESet = positionESet;
		positionESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_PARAMS__POSITION, oldPosition, newPosition, !oldPositionESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPosition(SamplePosition newPosition) {
		if (newPosition != position) {
			NotificationChain msgs = null;
			if (position != null)
				msgs = ((InternalEObject)position).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_PARAMS__POSITION, null, msgs);
			if (newPosition != null)
				msgs = ((InternalEObject)newPosition).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_PARAMS__POSITION, null, msgs);
			msgs = basicSetPosition(newPosition, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldPositionESet = positionESet;
			positionESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_PARAMS__POSITION, newPosition, newPosition, !oldPositionESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetPosition(NotificationChain msgs) {
		SamplePosition oldPosition = position;
		position = null;
		boolean oldPositionESet = positionESet;
		positionESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_PARAMS__POSITION, oldPosition, null, oldPositionESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetPosition() {
		if (position != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)position).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_PARAMS__POSITION, null, msgs);
			msgs = basicUnsetPosition(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldPositionESet = positionESet;
			positionESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_PARAMS__POSITION, null, null, oldPositionESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetPosition() {
		return positionESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getWeight() {
		return weight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWeight(double newWeight) {
		double oldWeight = weight;
		weight = newWeight;
		boolean oldWeightESet = weightESet;
		weightESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_PARAMS__WEIGHT, oldWeight, weight, !oldWeightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetWeight() {
		double oldWeight = weight;
		boolean oldWeightESet = weightESet;
		weight = WEIGHT_EDEFAULT;
		weightESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_PARAMS__WEIGHT, oldWeight, WEIGHT_EDEFAULT, oldWeightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetWeight() {
		return weightESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.SAMPLE_PARAMS__POSITION:
				return basicUnsetPosition(msgs);
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
			case TomoParametersPackage.SAMPLE_PARAMS__POSITION:
				return getPosition();
			case TomoParametersPackage.SAMPLE_PARAMS__WEIGHT:
				return getWeight();
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
			case TomoParametersPackage.SAMPLE_PARAMS__POSITION:
				setPosition((SamplePosition)newValue);
				return;
			case TomoParametersPackage.SAMPLE_PARAMS__WEIGHT:
				setWeight((Double)newValue);
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
			case TomoParametersPackage.SAMPLE_PARAMS__POSITION:
				unsetPosition();
				return;
			case TomoParametersPackage.SAMPLE_PARAMS__WEIGHT:
				unsetWeight();
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
			case TomoParametersPackage.SAMPLE_PARAMS__POSITION:
				return isSetPosition();
			case TomoParametersPackage.SAMPLE_PARAMS__WEIGHT:
				return isSetWeight();
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
		result.append(" (weight: ");
		if (weightESet) result.append(weight); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //SampleParamsImpl
