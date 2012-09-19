/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage
 * @generated
 */
public interface LocalTomoFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LocalTomoFactory eINSTANCE = uk.ac.diamond.tomography.localtomo.impl.LocalTomoFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Beamline Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Beamline Type</em>'.
	 * @generated
	 */
	BeamlineType createBeamlineType();

	/**
	 * Returns a new object of class '<em>Cluster Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Cluster Type</em>'.
	 * @generated
	 */
	ClusterType createClusterType();

	/**
	 * Returns a new object of class '<em>Document Root</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Document Root</em>'.
	 * @generated
	 */
	DocumentRoot createDocumentRoot();

	/**
	 * Returns a new object of class '<em>Filename Fmt Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Filename Fmt Type</em>'.
	 * @generated
	 */
	FilenameFmtType createFilenameFmtType();

	/**
	 * Returns a new object of class '<em>Imagekeyencoding Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Imagekeyencoding Type</em>'.
	 * @generated
	 */
	ImagekeyencodingType createImagekeyencodingType();

	/**
	 * Returns a new object of class '<em>Imgkey NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Imgkey NXS Path Type</em>'.
	 * @generated
	 */
	ImgkeyNXSPathType createImgkeyNXSPathType();

	/**
	 * Returns a new object of class '<em>Ixx Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Ixx Type</em>'.
	 * @generated
	 */
	IxxType createIxxType();

	/**
	 * Returns a new object of class '<em>Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Type</em>'.
	 * @generated
	 */
	LocalTomoType createLocalTomoType();

	/**
	 * Returns a new object of class '<em>Nexusfile Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Nexusfile Type</em>'.
	 * @generated
	 */
	NexusfileType createNexusfileType();

	/**
	 * Returns a new object of class '<em>Qsub Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Qsub Type</em>'.
	 * @generated
	 */
	QsubType createQsubType();

	/**
	 * Returns a new object of class '<em>Settingsfile Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Settingsfile Type</em>'.
	 * @generated
	 */
	SettingsfileType createSettingsfileType();

	/**
	 * Returns a new object of class '<em>Shutter Closed Phys Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Shutter Closed Phys Type</em>'.
	 * @generated
	 */
	ShutterClosedPhysType createShutterClosedPhysType();

	/**
	 * Returns a new object of class '<em>Shutter NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Shutter NXS Path Type</em>'.
	 * @generated
	 */
	ShutterNXSPathType createShutterNXSPathType();

	/**
	 * Returns a new object of class '<em>Shutter Open Phys Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Shutter Open Phys Type</em>'.
	 * @generated
	 */
	ShutterOpenPhysType createShutterOpenPhysType();

	/**
	 * Returns a new object of class '<em>Shutter Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Shutter Type</em>'.
	 * @generated
	 */
	ShutterType createShutterType();

	/**
	 * Returns a new object of class '<em>Stage Pos NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Stage Pos NXS Path Type</em>'.
	 * @generated
	 */
	StagePosNXSPathType createStagePosNXSPathType();

	/**
	 * Returns a new object of class '<em>Stage Rot NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Stage Rot NXS Path Type</em>'.
	 * @generated
	 */
	StageRotNXSPathType createStageRotNXSPathType();

	/**
	 * Returns a new object of class '<em>Tifimage Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tifimage Type</em>'.
	 * @generated
	 */
	TifimageType createTifimageType();

	/**
	 * Returns a new object of class '<em>Tif NXS Path Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tif NXS Path Type</em>'.
	 * @generated
	 */
	TifNXSPathType createTifNXSPathType();

	/**
	 * Returns a new object of class '<em>Tomodo Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tomodo Type</em>'.
	 * @generated
	 */
	TomodoType createTomodoType();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	LocalTomoPackage getLocalTomoPackage();

} //LocalTomoFactory
