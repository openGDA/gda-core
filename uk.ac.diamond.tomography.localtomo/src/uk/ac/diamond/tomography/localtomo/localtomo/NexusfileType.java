/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Nexusfile Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getShutterNXSPath <em>Shutter NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStagePosNXSPath <em>Stage Pos NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStageRotNXSPath <em>Stage Rot NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getTifNXSPath <em>Tif NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getImgkeyNXSPath <em>Imgkey NXS Path</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getNexusfileType()
 * @model extendedMetaData="name='nexusfile_._type' kind='elementOnly'"
 * @generated
 */
public interface NexusfileType extends EObject {
	/**
	 * Returns the value of the '<em><b>Shutter NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shutter NXS Path</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shutter NXS Path</em>' containment reference.
	 * @see #setShutterNXSPath(ShutterNXSPathType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getNexusfileType_ShutterNXSPath()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='shutterNXSPath' namespace='##targetNamespace'"
	 * @generated
	 */
	ShutterNXSPathType getShutterNXSPath();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getShutterNXSPath <em>Shutter NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shutter NXS Path</em>' containment reference.
	 * @see #getShutterNXSPath()
	 * @generated
	 */
	void setShutterNXSPath(ShutterNXSPathType value);

	/**
	 * Returns the value of the '<em><b>Stage Pos NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stage Pos NXS Path</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stage Pos NXS Path</em>' containment reference.
	 * @see #setStagePosNXSPath(StagePosNXSPathType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getNexusfileType_StagePosNXSPath()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='stagePosNXSPath' namespace='##targetNamespace'"
	 * @generated
	 */
	StagePosNXSPathType getStagePosNXSPath();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStagePosNXSPath <em>Stage Pos NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage Pos NXS Path</em>' containment reference.
	 * @see #getStagePosNXSPath()
	 * @generated
	 */
	void setStagePosNXSPath(StagePosNXSPathType value);

	/**
	 * Returns the value of the '<em><b>Stage Rot NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stage Rot NXS Path</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stage Rot NXS Path</em>' containment reference.
	 * @see #setStageRotNXSPath(StageRotNXSPathType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getNexusfileType_StageRotNXSPath()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='stageRotNXSPath' namespace='##targetNamespace'"
	 * @generated
	 */
	StageRotNXSPathType getStageRotNXSPath();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStageRotNXSPath <em>Stage Rot NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage Rot NXS Path</em>' containment reference.
	 * @see #getStageRotNXSPath()
	 * @generated
	 */
	void setStageRotNXSPath(StageRotNXSPathType value);

	/**
	 * Returns the value of the '<em><b>Tif NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tif NXS Path</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tif NXS Path</em>' containment reference.
	 * @see #setTifNXSPath(TifNXSPathType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getNexusfileType_TifNXSPath()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='tifNXSPath' namespace='##targetNamespace'"
	 * @generated
	 */
	TifNXSPathType getTifNXSPath();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getTifNXSPath <em>Tif NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tif NXS Path</em>' containment reference.
	 * @see #getTifNXSPath()
	 * @generated
	 */
	void setTifNXSPath(TifNXSPathType value);

	/**
	 * Returns the value of the '<em><b>Imgkey NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Imgkey NXS Path</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Imgkey NXS Path</em>' containment reference.
	 * @see #setImgkeyNXSPath(ImgkeyNXSPathType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getNexusfileType_ImgkeyNXSPath()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='imgkeyNXSPath' namespace='##targetNamespace'"
	 * @generated
	 */
	ImgkeyNXSPathType getImgkeyNXSPath();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getImgkeyNXSPath <em>Imgkey NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Imgkey NXS Path</em>' containment reference.
	 * @see #getImgkeyNXSPath()
	 * @generated
	 */
	void setImgkeyNXSPath(ImgkeyNXSPathType value);

} // NexusfileType
