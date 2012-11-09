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

import uk.ac.diamond.tomography.localtomo.BeamlineType;
import uk.ac.diamond.tomography.localtomo.IxxType;
import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Beamline Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.BeamlineTypeImpl#getIxx <em>Ixx</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BeamlineTypeImpl extends EObjectImpl implements BeamlineType {
	/**
	 * The cached value of the '{@link #getIxx() <em>Ixx</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIxx()
	 * @generated
	 * @ordered
	 */
	protected IxxType ixx;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BeamlineTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.BEAMLINE_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IxxType getIxx() {
		return ixx;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetIxx(IxxType newIxx, NotificationChain msgs) {
		IxxType oldIxx = ixx;
		ixx = newIxx;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.BEAMLINE_TYPE__IXX, oldIxx, newIxx);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIxx(IxxType newIxx) {
		if (newIxx != ixx) {
			NotificationChain msgs = null;
			if (ixx != null)
				msgs = ((InternalEObject)ixx).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.BEAMLINE_TYPE__IXX, null, msgs);
			if (newIxx != null)
				msgs = ((InternalEObject)newIxx).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.BEAMLINE_TYPE__IXX, null, msgs);
			msgs = basicSetIxx(newIxx, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.BEAMLINE_TYPE__IXX, newIxx, newIxx));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.BEAMLINE_TYPE__IXX:
				return basicSetIxx(null, msgs);
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
			case LocalTomoPackage.BEAMLINE_TYPE__IXX:
				return getIxx();
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
			case LocalTomoPackage.BEAMLINE_TYPE__IXX:
				setIxx((IxxType)newValue);
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
			case LocalTomoPackage.BEAMLINE_TYPE__IXX:
				setIxx((IxxType)null);
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
			case LocalTomoPackage.BEAMLINE_TYPE__IXX:
				return ixx != null;
		}
		return super.eIsSet(featureID);
	}

} //BeamlineTypeImpl
