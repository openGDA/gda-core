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

import uk.ac.diamond.tomography.localtomo.FilenameFmtType;
import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.TifimageType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tifimage Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TifimageTypeImpl#getFilenameFmt <em>Filename Fmt</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TifimageTypeImpl extends EObjectImpl implements TifimageType {
	/**
	 * The cached value of the '{@link #getFilenameFmt() <em>Filename Fmt</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenameFmt()
	 * @generated
	 * @ordered
	 */
	protected FilenameFmtType filenameFmt;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TifimageTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.TIFIMAGE_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FilenameFmtType getFilenameFmt() {
		return filenameFmt;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFilenameFmt(FilenameFmtType newFilenameFmt, NotificationChain msgs) {
		FilenameFmtType oldFilenameFmt = filenameFmt;
		filenameFmt = newFilenameFmt;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT, oldFilenameFmt, newFilenameFmt);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilenameFmt(FilenameFmtType newFilenameFmt) {
		if (newFilenameFmt != filenameFmt) {
			NotificationChain msgs = null;
			if (filenameFmt != null)
				msgs = ((InternalEObject)filenameFmt).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT, null, msgs);
			if (newFilenameFmt != null)
				msgs = ((InternalEObject)newFilenameFmt).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT, null, msgs);
			msgs = basicSetFilenameFmt(newFilenameFmt, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT, newFilenameFmt, newFilenameFmt));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT:
				return basicSetFilenameFmt(null, msgs);
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
			case LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT:
				return getFilenameFmt();
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
			case LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT:
				setFilenameFmt((FilenameFmtType)newValue);
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
			case LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT:
				setFilenameFmt((FilenameFmtType)null);
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
			case LocalTomoPackage.TIFIMAGE_TYPE__FILENAME_FMT:
				return filenameFmt != null;
		}
		return super.eIsSet(featureID);
	}

} //TifimageTypeImpl
