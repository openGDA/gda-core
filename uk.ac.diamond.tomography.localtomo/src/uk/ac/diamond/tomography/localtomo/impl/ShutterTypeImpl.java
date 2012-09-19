/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.ShutterClosedPhysType;
import uk.ac.diamond.tomography.localtomo.ShutterOpenPhysType;
import uk.ac.diamond.tomography.localtomo.ShutterType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Shutter Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.ShutterTypeImpl#getShutterOpenPhys <em>Shutter Open Phys</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.ShutterTypeImpl#getShutterClosedPhys <em>Shutter Closed Phys</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ShutterTypeImpl extends EObjectImpl implements ShutterType {
	/**
	 * The cached value of the '{@link #getShutterOpenPhys() <em>Shutter Open Phys</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShutterOpenPhys()
	 * @generated
	 * @ordered
	 */
	protected ShutterOpenPhysType shutterOpenPhys;

	/**
	 * The cached value of the '{@link #getShutterClosedPhys() <em>Shutter Closed Phys</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShutterClosedPhys()
	 * @generated
	 * @ordered
	 */
	protected ShutterClosedPhysType shutterClosedPhys;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ShutterTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.SHUTTER_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterOpenPhysType getShutterOpenPhys() {
		return shutterOpenPhys;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetShutterOpenPhys(ShutterOpenPhysType newShutterOpenPhys, NotificationChain msgs) {
		ShutterOpenPhysType oldShutterOpenPhys = shutterOpenPhys;
		shutterOpenPhys = newShutterOpenPhys;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS, oldShutterOpenPhys, newShutterOpenPhys);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShutterOpenPhys(ShutterOpenPhysType newShutterOpenPhys) {
		if (newShutterOpenPhys != shutterOpenPhys) {
			NotificationChain msgs = null;
			if (shutterOpenPhys != null)
				msgs = ((InternalEObject)shutterOpenPhys).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS, null, msgs);
			if (newShutterOpenPhys != null)
				msgs = ((InternalEObject)newShutterOpenPhys).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS, null, msgs);
			msgs = basicSetShutterOpenPhys(newShutterOpenPhys, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS, newShutterOpenPhys, newShutterOpenPhys));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterClosedPhysType getShutterClosedPhys() {
		return shutterClosedPhys;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetShutterClosedPhys(ShutterClosedPhysType newShutterClosedPhys, NotificationChain msgs) {
		ShutterClosedPhysType oldShutterClosedPhys = shutterClosedPhys;
		shutterClosedPhys = newShutterClosedPhys;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS, oldShutterClosedPhys, newShutterClosedPhys);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShutterClosedPhys(ShutterClosedPhysType newShutterClosedPhys) {
		if (newShutterClosedPhys != shutterClosedPhys) {
			NotificationChain msgs = null;
			if (shutterClosedPhys != null)
				msgs = ((InternalEObject)shutterClosedPhys).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS, null, msgs);
			if (newShutterClosedPhys != null)
				msgs = ((InternalEObject)newShutterClosedPhys).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS, null, msgs);
			msgs = basicSetShutterClosedPhys(newShutterClosedPhys, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS, newShutterClosedPhys, newShutterClosedPhys));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS:
				return basicSetShutterOpenPhys(null, msgs);
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS:
				return basicSetShutterClosedPhys(null, msgs);
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
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS:
				return getShutterOpenPhys();
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS:
				return getShutterClosedPhys();
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
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS:
				setShutterOpenPhys((ShutterOpenPhysType)newValue);
				return;
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS:
				setShutterClosedPhys((ShutterClosedPhysType)newValue);
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
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS:
				setShutterOpenPhys((ShutterOpenPhysType)null);
				return;
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS:
				setShutterClosedPhys((ShutterClosedPhysType)null);
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
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_OPEN_PHYS:
				return shutterOpenPhys != null;
			case LocalTomoPackage.SHUTTER_TYPE__SHUTTER_CLOSED_PHYS:
				return shutterClosedPhys != null;
		}
		return super.eIsSet(featureID);
	}

} //ShutterTypeImpl
