/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Recon Results</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconResultsImpl#getReconresult <em>
 * Reconresult</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class ReconResultsImpl extends EObjectImpl implements ReconResults {
	/**
	 * The cached value of the '{@link #getReconresult() <em>Reconresult</em>}' containment reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getReconresult()
	 * @generated
	 * @ordered
	 */
	protected EList<ReconstructionDetail> reconresult;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ReconResultsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ReconresultsPackage.Literals.RECON_RESULTS;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<ReconstructionDetail> getReconresult() {
		if (reconresult == null) {
			reconresult = new EObjectContainmentEList<ReconstructionDetail>(ReconstructionDetail.class, this,
					ReconresultsPackage.RECON_RESULTS__RECONRESULT);
		}
		return reconresult;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ReconstructionDetail getReconstructionDetail(String nexusFullFileLocation) {
		for (ReconstructionDetail reconDetail : reconresult) {
			if (reconDetail.getNexusFileLocation().equals(nexusFullFileLocation)) {
				return reconDetail;
			}
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case ReconresultsPackage.RECON_RESULTS__RECONRESULT:
			return ((InternalEList<?>) getReconresult()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case ReconresultsPackage.RECON_RESULTS__RECONRESULT:
			return getReconresult();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case ReconresultsPackage.RECON_RESULTS__RECONRESULT:
			getReconresult().clear();
			getReconresult().addAll((Collection<? extends ReconstructionDetail>) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case ReconresultsPackage.RECON_RESULTS__RECONRESULT:
			getReconresult().clear();
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case ReconresultsPackage.RECON_RESULTS__RECONRESULT:
			return reconresult != null && !reconresult.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} // ReconResultsImpl
