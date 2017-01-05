/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Document Root</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.DocumentRootImpl#getEdxdCalibration <em>Edxd Calibration</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DocumentRootImpl extends EObjectImpl implements DocumentRoot {
	/**
	 * The cached value of the '{@link #getEdxdCalibration() <em>Edxd Calibration</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEdxdCalibration()
	 * @generated
	 * @ordered
	 */
	protected EList<EdxdCalibration> edxdCalibration;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DocumentRootImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EdxdcalibrationPackage.Literals.DOCUMENT_ROOT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EdxdCalibration> getEdxdCalibration() {
		if (edxdCalibration == null) {
			edxdCalibration = new EObjectContainmentEList<EdxdCalibration>(EdxdCalibration.class, this, EdxdcalibrationPackage.DOCUMENT_ROOT__EDXD_CALIBRATION);
		}
		return edxdCalibration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EdxdcalibrationPackage.DOCUMENT_ROOT__EDXD_CALIBRATION:
				return ((InternalEList<?>)getEdxdCalibration()).basicRemove(otherEnd, msgs);
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
			case EdxdcalibrationPackage.DOCUMENT_ROOT__EDXD_CALIBRATION:
				return getEdxdCalibration();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case EdxdcalibrationPackage.DOCUMENT_ROOT__EDXD_CALIBRATION:
				getEdxdCalibration().clear();
				getEdxdCalibration().addAll((Collection<? extends EdxdCalibration>)newValue);
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
			case EdxdcalibrationPackage.DOCUMENT_ROOT__EDXD_CALIBRATION:
				getEdxdCalibration().clear();
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
			case EdxdcalibrationPackage.DOCUMENT_ROOT__EDXD_CALIBRATION:
				return edxdCalibration != null && !edxdCalibration.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //DocumentRootImpl
