/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType;
import uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType;
import uk.ac.diamond.tomography.localtomo.localtomo.TomodoType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoTypeImpl#getBeamline <em>Beamline</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoTypeImpl#getTomodo <em>Tomodo</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LocalTomoTypeImpl extends EObjectImpl implements LocalTomoType {
	/**
	 * The cached value of the '{@link #getBeamline() <em>Beamline</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBeamline()
	 * @generated
	 * @ordered
	 */
	protected BeamlineType beamline;

	/**
	 * The cached value of the '{@link #getTomodo() <em>Tomodo</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTomodo()
	 * @generated
	 * @ordered
	 */
	protected TomodoType tomodo;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LocalTomoTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.LOCAL_TOMO_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BeamlineType getBeamline() {
		return beamline;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBeamline(BeamlineType newBeamline, NotificationChain msgs) {
		BeamlineType oldBeamline = beamline;
		beamline = newBeamline;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE, oldBeamline, newBeamline);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBeamline(BeamlineType newBeamline) {
		if (newBeamline != beamline) {
			NotificationChain msgs = null;
			if (beamline != null)
				msgs = ((InternalEObject)beamline).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE, null, msgs);
			if (newBeamline != null)
				msgs = ((InternalEObject)newBeamline).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE, null, msgs);
			msgs = basicSetBeamline(newBeamline, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE, newBeamline, newBeamline));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TomodoType getTomodo() {
		return tomodo;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTomodo(TomodoType newTomodo, NotificationChain msgs) {
		TomodoType oldTomodo = tomodo;
		tomodo = newTomodo;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO, oldTomodo, newTomodo);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTomodo(TomodoType newTomodo) {
		if (newTomodo != tomodo) {
			NotificationChain msgs = null;
			if (tomodo != null)
				msgs = ((InternalEObject)tomodo).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO, null, msgs);
			if (newTomodo != null)
				msgs = ((InternalEObject)newTomodo).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO, null, msgs);
			msgs = basicSetTomodo(newTomodo, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO, newTomodo, newTomodo));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE:
				return basicSetBeamline(null, msgs);
			case LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO:
				return basicSetTomodo(null, msgs);
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
			case LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE:
				return getBeamline();
			case LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO:
				return getTomodo();
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
			case LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE:
				setBeamline((BeamlineType)newValue);
				return;
			case LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO:
				setTomodo((TomodoType)newValue);
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
			case LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE:
				setBeamline((BeamlineType)null);
				return;
			case LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO:
				setTomodo((TomodoType)null);
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
			case LocalTomoPackage.LOCAL_TOMO_TYPE__BEAMLINE:
				return beamline != null;
			case LocalTomoPackage.LOCAL_TOMO_TYPE__TOMODO:
				return tomodo != null;
		}
		return super.eIsSet(featureID);
	}

} //LocalTomoTypeImpl
