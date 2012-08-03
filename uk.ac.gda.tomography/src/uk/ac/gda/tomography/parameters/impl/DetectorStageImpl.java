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

import uk.ac.gda.tomography.parameters.DetectorStage;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.ValueUnit;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Detector Stage</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorStageImpl#getX <em>X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorStageImpl#getY <em>Y</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorStageImpl#getZ <em>Z</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DetectorStageImpl extends EObjectImpl implements DetectorStage {
	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit x;

	/**
	 * This is true if the X containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean xESet;

	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit y;

	/**
	 * This is true if the Y containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean yESet;

	/**
	 * The cached value of the '{@link #getZ() <em>Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZ()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit z;

	/**
	 * This is true if the Z containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean zESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DetectorStageImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.DETECTOR_STAGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetX(ValueUnit newX, NotificationChain msgs) {
		ValueUnit oldX = x;
		x = newX;
		boolean oldXESet = xESet;
		xESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_STAGE__X, oldX, newX, !oldXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setX(ValueUnit newX) {
		if (newX != x) {
			NotificationChain msgs = null;
			if (x != null)
				msgs = ((InternalEObject)x).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__X, null, msgs);
			if (newX != null)
				msgs = ((InternalEObject)newX).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__X, null, msgs);
			msgs = basicSetX(newX, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldXESet = xESet;
			xESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_STAGE__X, newX, newX, !oldXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetX(NotificationChain msgs) {
		ValueUnit oldX = x;
		x = null;
		boolean oldXESet = xESet;
		xESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_STAGE__X, oldX, null, oldXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetX() {
		if (x != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)x).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__X, null, msgs);
			msgs = basicUnsetX(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldXESet = xESet;
			xESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_STAGE__X, null, null, oldXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetX() {
		return xESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetY(ValueUnit newY, NotificationChain msgs) {
		ValueUnit oldY = y;
		y = newY;
		boolean oldYESet = yESet;
		yESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_STAGE__Y, oldY, newY, !oldYESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setY(ValueUnit newY) {
		if (newY != y) {
			NotificationChain msgs = null;
			if (y != null)
				msgs = ((InternalEObject)y).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__Y, null, msgs);
			if (newY != null)
				msgs = ((InternalEObject)newY).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__Y, null, msgs);
			msgs = basicSetY(newY, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldYESet = yESet;
			yESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_STAGE__Y, newY, newY, !oldYESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetY(NotificationChain msgs) {
		ValueUnit oldY = y;
		y = null;
		boolean oldYESet = yESet;
		yESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_STAGE__Y, oldY, null, oldYESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetY() {
		if (y != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)y).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__Y, null, msgs);
			msgs = basicUnsetY(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldYESet = yESet;
			yESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_STAGE__Y, null, null, oldYESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetY() {
		return yESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getZ() {
		return z;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetZ(ValueUnit newZ, NotificationChain msgs) {
		ValueUnit oldZ = z;
		z = newZ;
		boolean oldZESet = zESet;
		zESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_STAGE__Z, oldZ, newZ, !oldZESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setZ(ValueUnit newZ) {
		if (newZ != z) {
			NotificationChain msgs = null;
			if (z != null)
				msgs = ((InternalEObject)z).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__Z, null, msgs);
			if (newZ != null)
				msgs = ((InternalEObject)newZ).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__Z, null, msgs);
			msgs = basicSetZ(newZ, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldZESet = zESet;
			zESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_STAGE__Z, newZ, newZ, !oldZESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetZ(NotificationChain msgs) {
		ValueUnit oldZ = z;
		z = null;
		boolean oldZESet = zESet;
		zESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_STAGE__Z, oldZ, null, oldZESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetZ() {
		if (z != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)z).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_STAGE__Z, null, msgs);
			msgs = basicUnsetZ(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldZESet = zESet;
			zESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_STAGE__Z, null, null, oldZESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetZ() {
		return zESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.DETECTOR_STAGE__X:
				return basicUnsetX(msgs);
			case TomoParametersPackage.DETECTOR_STAGE__Y:
				return basicUnsetY(msgs);
			case TomoParametersPackage.DETECTOR_STAGE__Z:
				return basicUnsetZ(msgs);
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
			case TomoParametersPackage.DETECTOR_STAGE__X:
				return getX();
			case TomoParametersPackage.DETECTOR_STAGE__Y:
				return getY();
			case TomoParametersPackage.DETECTOR_STAGE__Z:
				return getZ();
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
			case TomoParametersPackage.DETECTOR_STAGE__X:
				setX((ValueUnit)newValue);
				return;
			case TomoParametersPackage.DETECTOR_STAGE__Y:
				setY((ValueUnit)newValue);
				return;
			case TomoParametersPackage.DETECTOR_STAGE__Z:
				setZ((ValueUnit)newValue);
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
			case TomoParametersPackage.DETECTOR_STAGE__X:
				unsetX();
				return;
			case TomoParametersPackage.DETECTOR_STAGE__Y:
				unsetY();
				return;
			case TomoParametersPackage.DETECTOR_STAGE__Z:
				unsetZ();
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
			case TomoParametersPackage.DETECTOR_STAGE__X:
				return isSetX();
			case TomoParametersPackage.DETECTOR_STAGE__Y:
				return isSetY();
			case TomoParametersPackage.DETECTOR_STAGE__Z:
				return isSetZ();
		}
		return super.eIsSet(featureID);
	}

} //DetectorStageImpl
