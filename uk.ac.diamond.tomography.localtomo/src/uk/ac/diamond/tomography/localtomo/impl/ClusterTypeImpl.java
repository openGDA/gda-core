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

import uk.ac.diamond.tomography.localtomo.ClusterType;
import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.QsubType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cluster Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.ClusterTypeImpl#getQsub <em>Qsub</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ClusterTypeImpl extends EObjectImpl implements ClusterType {
	/**
	 * The cached value of the '{@link #getQsub() <em>Qsub</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQsub()
	 * @generated
	 * @ordered
	 */
	protected QsubType qsub;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ClusterTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.CLUSTER_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public QsubType getQsub() {
		return qsub;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetQsub(QsubType newQsub, NotificationChain msgs) {
		QsubType oldQsub = qsub;
		qsub = newQsub;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.CLUSTER_TYPE__QSUB, oldQsub, newQsub);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setQsub(QsubType newQsub) {
		if (newQsub != qsub) {
			NotificationChain msgs = null;
			if (qsub != null)
				msgs = ((InternalEObject)qsub).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.CLUSTER_TYPE__QSUB, null, msgs);
			if (newQsub != null)
				msgs = ((InternalEObject)newQsub).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.CLUSTER_TYPE__QSUB, null, msgs);
			msgs = basicSetQsub(newQsub, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.CLUSTER_TYPE__QSUB, newQsub, newQsub));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.CLUSTER_TYPE__QSUB:
				return basicSetQsub(null, msgs);
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
			case LocalTomoPackage.CLUSTER_TYPE__QSUB:
				return getQsub();
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
			case LocalTomoPackage.CLUSTER_TYPE__QSUB:
				setQsub((QsubType)newValue);
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
			case LocalTomoPackage.CLUSTER_TYPE__QSUB:
				setQsub((QsubType)null);
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
			case LocalTomoPackage.CLUSTER_TYPE__QSUB:
				return qsub != null;
		}
		return super.eIsSet(featureID);
	}

} //ClusterTypeImpl
