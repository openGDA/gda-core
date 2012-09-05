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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>HMxml Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HMxmlTypeImpl#getFBP <em>FBP</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class HMxmlTypeImpl extends EObjectImpl implements HMxmlType {
	/**
	 * The cached value of the '{@link #getFBP() <em>FBP</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFBP()
	 * @generated
	 * @ordered
	 */
	protected FBPType fBP;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HMxmlTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.HMXML_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FBPType getFBP() {
		return fBP;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFBP(FBPType newFBP, NotificationChain msgs) {
		FBPType oldFBP = fBP;
		fBP = newFBP;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.HMXML_TYPE__FBP, oldFBP, newFBP);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFBP(FBPType newFBP) {
		if (newFBP != fBP) {
			NotificationChain msgs = null;
			if (fBP != null)
				msgs = ((InternalEObject)fBP).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.HMXML_TYPE__FBP, null, msgs);
			if (newFBP != null)
				msgs = ((InternalEObject)newFBP).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.HMXML_TYPE__FBP, null, msgs);
			msgs = basicSetFBP(newFBP, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.HMXML_TYPE__FBP, newFBP, newFBP));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.HMXML_TYPE__FBP:
				return basicSetFBP(null, msgs);
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
			case HmPackage.HMXML_TYPE__FBP:
				return getFBP();
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
			case HmPackage.HMXML_TYPE__FBP:
				setFBP((FBPType)newValue);
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
			case HmPackage.HMXML_TYPE__FBP:
				setFBP((FBPType)null);
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
			case HmPackage.HMXML_TYPE__FBP:
				return fBP != null;
		}
		return super.eIsSet(featureID);
	}

} //HMxmlTypeImpl
