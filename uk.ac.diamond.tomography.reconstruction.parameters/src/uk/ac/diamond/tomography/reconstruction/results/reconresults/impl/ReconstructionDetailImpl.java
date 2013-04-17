/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.results.reconresults.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reconstruction Detail</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl#getNexusFileName <em>Nexus File Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl#getNexusFileLocation <em>Nexus File Location</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl#getReconstructedLocation <em>Reconstructed Location</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.results.reconresults.impl.ReconstructionDetailImpl#getTimeReconStarted <em>Time Recon Started</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ReconstructionDetailImpl extends EObjectImpl implements ReconstructionDetail {
	/**
	 * The default value of the '{@link #getNexusFileName() <em>Nexus File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNexusFileName()
	 * @generated
	 * @ordered
	 */
	protected static final String NEXUS_FILE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNexusFileName() <em>Nexus File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNexusFileName()
	 * @generated
	 * @ordered
	 */
	protected String nexusFileName = NEXUS_FILE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getNexusFileLocation() <em>Nexus File Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNexusFileLocation()
	 * @generated
	 * @ordered
	 */
	protected static final String NEXUS_FILE_LOCATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNexusFileLocation() <em>Nexus File Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNexusFileLocation()
	 * @generated
	 * @ordered
	 */
	protected String nexusFileLocation = NEXUS_FILE_LOCATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getReconstructedLocation() <em>Reconstructed Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReconstructedLocation()
	 * @generated
	 * @ordered
	 */
	protected static final String RECONSTRUCTED_LOCATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReconstructedLocation() <em>Reconstructed Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReconstructedLocation()
	 * @generated
	 * @ordered
	 */
	protected String reconstructedLocation = RECONSTRUCTED_LOCATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getTimeReconStarted() <em>Time Recon Started</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimeReconStarted()
	 * @generated
	 * @ordered
	 */
	protected static final String TIME_RECON_STARTED_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTimeReconStarted() <em>Time Recon Started</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimeReconStarted()
	 * @generated
	 * @ordered
	 */
	protected String timeReconStarted = TIME_RECON_STARTED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReconstructionDetailImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ReconresultsPackage.Literals.RECONSTRUCTION_DETAIL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getNexusFileName() {
		return nexusFileName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNexusFileName(String newNexusFileName) {
		String oldNexusFileName = nexusFileName;
		nexusFileName = newNexusFileName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME, oldNexusFileName, nexusFileName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getNexusFileLocation() {
		return nexusFileLocation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNexusFileLocation(String newNexusFileLocation) {
		String oldNexusFileLocation = nexusFileLocation;
		nexusFileLocation = newNexusFileLocation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION, oldNexusFileLocation, nexusFileLocation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getReconstructedLocation() {
		return reconstructedLocation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReconstructedLocation(String newReconstructedLocation) {
		String oldReconstructedLocation = reconstructedLocation;
		reconstructedLocation = newReconstructedLocation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReconresultsPackage.RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION, oldReconstructedLocation, reconstructedLocation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTimeReconStarted() {
		return timeReconStarted;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTimeReconStarted(String newTimeReconStarted) {
		String oldTimeReconStarted = timeReconStarted;
		timeReconStarted = newTimeReconStarted;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReconresultsPackage.RECONSTRUCTION_DETAIL__TIME_RECON_STARTED, oldTimeReconStarted, timeReconStarted));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME:
				return getNexusFileName();
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION:
				return getNexusFileLocation();
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION:
				return getReconstructedLocation();
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__TIME_RECON_STARTED:
				return getTimeReconStarted();
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
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME:
				setNexusFileName((String)newValue);
				return;
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION:
				setNexusFileLocation((String)newValue);
				return;
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION:
				setReconstructedLocation((String)newValue);
				return;
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__TIME_RECON_STARTED:
				setTimeReconStarted((String)newValue);
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
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME:
				setNexusFileName(NEXUS_FILE_NAME_EDEFAULT);
				return;
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION:
				setNexusFileLocation(NEXUS_FILE_LOCATION_EDEFAULT);
				return;
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION:
				setReconstructedLocation(RECONSTRUCTED_LOCATION_EDEFAULT);
				return;
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__TIME_RECON_STARTED:
				setTimeReconStarted(TIME_RECON_STARTED_EDEFAULT);
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
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_NAME:
				return NEXUS_FILE_NAME_EDEFAULT == null ? nexusFileName != null : !NEXUS_FILE_NAME_EDEFAULT.equals(nexusFileName);
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__NEXUS_FILE_LOCATION:
				return NEXUS_FILE_LOCATION_EDEFAULT == null ? nexusFileLocation != null : !NEXUS_FILE_LOCATION_EDEFAULT.equals(nexusFileLocation);
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__RECONSTRUCTED_LOCATION:
				return RECONSTRUCTED_LOCATION_EDEFAULT == null ? reconstructedLocation != null : !RECONSTRUCTED_LOCATION_EDEFAULT.equals(reconstructedLocation);
			case ReconresultsPackage.RECONSTRUCTION_DETAIL__TIME_RECON_STARTED:
				return TIME_RECON_STARTED_EDEFAULT == null ? timeReconStarted != null : !TIME_RECON_STARTED_EDEFAULT.equals(timeReconStarted);
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
		result.append(" (nexusFileName: ");
		result.append(nexusFileName);
		result.append(", nexusFileLocation: ");
		result.append(nexusFileLocation);
		result.append(", reconstructedLocation: ");
		result.append(reconstructedLocation);
		result.append(", timeReconStarted: ");
		result.append(timeReconStarted);
		result.append(')');
		return result.toString();
	}

} //ReconstructionDetailImpl
