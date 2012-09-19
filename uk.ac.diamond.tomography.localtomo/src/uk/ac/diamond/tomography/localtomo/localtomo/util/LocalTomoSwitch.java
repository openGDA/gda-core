/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import uk.ac.diamond.tomography.localtomo.localtomo.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage
 * @generated
 */
public class LocalTomoSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static LocalTomoPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTomoSwitch() {
		if (modelPackage == null) {
			modelPackage = LocalTomoPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @parameter ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case LocalTomoPackage.BEAMLINE_TYPE: {
				BeamlineType beamlineType = (BeamlineType)theEObject;
				T result = caseBeamlineType(beamlineType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.CLUSTER_TYPE: {
				ClusterType clusterType = (ClusterType)theEObject;
				T result = caseClusterType(clusterType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.DOCUMENT_ROOT: {
				DocumentRoot documentRoot = (DocumentRoot)theEObject;
				T result = caseDocumentRoot(documentRoot);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.FILENAME_FMT_TYPE: {
				FilenameFmtType filenameFmtType = (FilenameFmtType)theEObject;
				T result = caseFilenameFmtType(filenameFmtType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE: {
				ImagekeyencodingType imagekeyencodingType = (ImagekeyencodingType)theEObject;
				T result = caseImagekeyencodingType(imagekeyencodingType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.IMGKEY_NXS_PATH_TYPE: {
				ImgkeyNXSPathType imgkeyNXSPathType = (ImgkeyNXSPathType)theEObject;
				T result = caseImgkeyNXSPathType(imgkeyNXSPathType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.IXX_TYPE: {
				IxxType ixxType = (IxxType)theEObject;
				T result = caseIxxType(ixxType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.LOCAL_TOMO_TYPE: {
				LocalTomoType localTomoType = (LocalTomoType)theEObject;
				T result = caseLocalTomoType(localTomoType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.NEXUSFILE_TYPE: {
				NexusfileType nexusfileType = (NexusfileType)theEObject;
				T result = caseNexusfileType(nexusfileType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.QSUB_TYPE: {
				QsubType qsubType = (QsubType)theEObject;
				T result = caseQsubType(qsubType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.SETTINGSFILE_TYPE: {
				SettingsfileType settingsfileType = (SettingsfileType)theEObject;
				T result = caseSettingsfileType(settingsfileType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.SHUTTER_CLOSED_PHYS_TYPE: {
				ShutterClosedPhysType shutterClosedPhysType = (ShutterClosedPhysType)theEObject;
				T result = caseShutterClosedPhysType(shutterClosedPhysType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.SHUTTER_NXS_PATH_TYPE: {
				ShutterNXSPathType shutterNXSPathType = (ShutterNXSPathType)theEObject;
				T result = caseShutterNXSPathType(shutterNXSPathType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.SHUTTER_OPEN_PHYS_TYPE: {
				ShutterOpenPhysType shutterOpenPhysType = (ShutterOpenPhysType)theEObject;
				T result = caseShutterOpenPhysType(shutterOpenPhysType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.SHUTTER_TYPE: {
				ShutterType shutterType = (ShutterType)theEObject;
				T result = caseShutterType(shutterType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.STAGE_POS_NXS_PATH_TYPE: {
				StagePosNXSPathType stagePosNXSPathType = (StagePosNXSPathType)theEObject;
				T result = caseStagePosNXSPathType(stagePosNXSPathType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.STAGE_ROT_NXS_PATH_TYPE: {
				StageRotNXSPathType stageRotNXSPathType = (StageRotNXSPathType)theEObject;
				T result = caseStageRotNXSPathType(stageRotNXSPathType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.TIFIMAGE_TYPE: {
				TifimageType tifimageType = (TifimageType)theEObject;
				T result = caseTifimageType(tifimageType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.TIF_NXS_PATH_TYPE: {
				TifNXSPathType tifNXSPathType = (TifNXSPathType)theEObject;
				T result = caseTifNXSPathType(tifNXSPathType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case LocalTomoPackage.TOMODO_TYPE: {
				TomodoType tomodoType = (TomodoType)theEObject;
				T result = caseTomodoType(tomodoType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Beamline Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Beamline Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBeamlineType(BeamlineType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Cluster Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Cluster Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClusterType(ClusterType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Document Root</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Document Root</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDocumentRoot(DocumentRoot object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Filename Fmt Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Filename Fmt Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFilenameFmtType(FilenameFmtType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Imagekeyencoding Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Imagekeyencoding Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImagekeyencodingType(ImagekeyencodingType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Imgkey NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Imgkey NXS Path Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImgkeyNXSPathType(ImgkeyNXSPathType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Ixx Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Ixx Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIxxType(IxxType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseLocalTomoType(LocalTomoType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Nexusfile Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Nexusfile Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNexusfileType(NexusfileType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Qsub Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Qsub Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseQsubType(QsubType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Settingsfile Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Settingsfile Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSettingsfileType(SettingsfileType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shutter Closed Phys Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shutter Closed Phys Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShutterClosedPhysType(ShutterClosedPhysType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shutter NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shutter NXS Path Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShutterNXSPathType(ShutterNXSPathType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shutter Open Phys Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shutter Open Phys Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShutterOpenPhysType(ShutterOpenPhysType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shutter Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shutter Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShutterType(ShutterType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Stage Pos NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Stage Pos NXS Path Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStagePosNXSPathType(StagePosNXSPathType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Stage Rot NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Stage Rot NXS Path Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStageRotNXSPathType(StageRotNXSPathType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tifimage Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tifimage Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTifimageType(TifimageType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tif NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tif NXS Path Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTifNXSPathType(TifNXSPathType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tomodo Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tomodo Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTomodoType(TomodoType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //LocalTomoSwitch
