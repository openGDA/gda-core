/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoFactory
 * @model kind="package"
 *        extendedMetaData="qualified='false'"
 * @generated
 */
public interface LocalTomoPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "localtomo";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "platform:/resource/uk.ac.diamond.tomography.localtomo/model/localTomo.xsd";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LocalTomoPackage eINSTANCE = uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.BeamlineTypeImpl <em>Beamline Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.BeamlineTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getBeamlineType()
	 * @generated
	 */
	int BEAMLINE_TYPE = 0;

	/**
	 * The feature id for the '<em><b>Ixx</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_TYPE__IXX = 0;

	/**
	 * The number of structural features of the '<em>Beamline Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_TYPE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ClusterTypeImpl <em>Cluster Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ClusterTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getClusterType()
	 * @generated
	 */
	int CLUSTER_TYPE = 1;

	/**
	 * The feature id for the '<em><b>Qsub</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLUSTER_TYPE__QSUB = 0;

	/**
	 * The number of structural features of the '<em>Cluster Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLUSTER_TYPE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.DocumentRootImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getDocumentRoot()
	 * @generated
	 */
	int DOCUMENT_ROOT = 2;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__MIXED = 0;

	/**
	 * The feature id for the '<em><b>XMLNS Prefix Map</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__XMLNS_PREFIX_MAP = 1;

	/**
	 * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = 2;

	/**
	 * The feature id for the '<em><b>Local Tomo</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__LOCAL_TOMO = 3;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.FilenameFmtTypeImpl <em>Filename Fmt Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.FilenameFmtTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getFilenameFmtType()
	 * @generated
	 */
	int FILENAME_FMT_TYPE = 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILENAME_FMT_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILENAME_FMT_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Filename Fmt Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILENAME_FMT_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl <em>Imagekeyencoding Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getImagekeyencodingType()
	 * @generated
	 */
	int IMAGEKEYENCODING_TYPE = 4;

	/**
	 * The feature id for the '<em><b>Darkfield</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGEKEYENCODING_TYPE__DARKFIELD = 0;

	/**
	 * The feature id for the '<em><b>Flatfield</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGEKEYENCODING_TYPE__FLATFIELD = 1;

	/**
	 * The feature id for the '<em><b>Projection</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGEKEYENCODING_TYPE__PROJECTION = 2;

	/**
	 * The number of structural features of the '<em>Imagekeyencoding Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGEKEYENCODING_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImgkeyNXSPathTypeImpl <em>Imgkey NXS Path Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ImgkeyNXSPathTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getImgkeyNXSPathType()
	 * @generated
	 */
	int IMGKEY_NXS_PATH_TYPE = 5;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMGKEY_NXS_PATH_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMGKEY_NXS_PATH_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Imgkey NXS Path Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMGKEY_NXS_PATH_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.IxxTypeImpl <em>Ixx Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.IxxTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getIxxType()
	 * @generated
	 */
	int IXX_TYPE = 6;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXX_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXX_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Ixx Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXX_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoTypeImpl <em>Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getLocalTomoType()
	 * @generated
	 */
	int LOCAL_TOMO_TYPE = 7;

	/**
	 * The feature id for the '<em><b>Beamline</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCAL_TOMO_TYPE__BEAMLINE = 0;

	/**
	 * The feature id for the '<em><b>Tomodo</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCAL_TOMO_TYPE__TOMODO = 1;

	/**
	 * The number of structural features of the '<em>Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCAL_TOMO_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.NexusfileTypeImpl <em>Nexusfile Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.NexusfileTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getNexusfileType()
	 * @generated
	 */
	int NEXUSFILE_TYPE = 8;

	/**
	 * The feature id for the '<em><b>Shutter NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NEXUSFILE_TYPE__SHUTTER_NXS_PATH = 0;

	/**
	 * The feature id for the '<em><b>Stage Pos NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NEXUSFILE_TYPE__STAGE_POS_NXS_PATH = 1;

	/**
	 * The feature id for the '<em><b>Stage Rot NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH = 2;

	/**
	 * The feature id for the '<em><b>Tif NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NEXUSFILE_TYPE__TIF_NXS_PATH = 3;

	/**
	 * The feature id for the '<em><b>Imgkey NXS Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NEXUSFILE_TYPE__IMGKEY_NXS_PATH = 4;

	/**
	 * The number of structural features of the '<em>Nexusfile Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NEXUSFILE_TYPE_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.QsubTypeImpl <em>Qsub Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.QsubTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getQsubType()
	 * @generated
	 */
	int QSUB_TYPE = 9;

	/**
	 * The feature id for the '<em><b>Projectname</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int QSUB_TYPE__PROJECTNAME = 0;

	/**
	 * The feature id for the '<em><b>Args</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int QSUB_TYPE__ARGS = 1;

	/**
	 * The feature id for the '<em><b>Sinoqueue</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int QSUB_TYPE__SINOQUEUE = 2;

	/**
	 * The feature id for the '<em><b>Reconqueue</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int QSUB_TYPE__RECONQUEUE = 3;

	/**
	 * The number of structural features of the '<em>Qsub Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int QSUB_TYPE_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.SettingsfileTypeImpl <em>Settingsfile Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.SettingsfileTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getSettingsfileType()
	 * @generated
	 */
	int SETTINGSFILE_TYPE = 10;

	/**
	 * The feature id for the '<em><b>Blueprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SETTINGSFILE_TYPE__BLUEPRINT = 0;

	/**
	 * The number of structural features of the '<em>Settingsfile Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SETTINGSFILE_TYPE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterClosedPhysTypeImpl <em>Shutter Closed Phys Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterClosedPhysTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterClosedPhysType()
	 * @generated
	 */
	int SHUTTER_CLOSED_PHYS_TYPE = 11;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_CLOSED_PHYS_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_CLOSED_PHYS_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Shutter Closed Phys Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_CLOSED_PHYS_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterNXSPathTypeImpl <em>Shutter NXS Path Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterNXSPathTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterNXSPathType()
	 * @generated
	 */
	int SHUTTER_NXS_PATH_TYPE = 12;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_NXS_PATH_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_NXS_PATH_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Shutter NXS Path Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_NXS_PATH_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterOpenPhysTypeImpl <em>Shutter Open Phys Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterOpenPhysTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterOpenPhysType()
	 * @generated
	 */
	int SHUTTER_OPEN_PHYS_TYPE = 13;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_OPEN_PHYS_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_OPEN_PHYS_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Shutter Open Phys Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_OPEN_PHYS_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterTypeImpl <em>Shutter Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterType()
	 * @generated
	 */
	int SHUTTER_TYPE = 14;

	/**
	 * The feature id for the '<em><b>Shutter Open Phys</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_TYPE__SHUTTER_OPEN_PHYS = 0;

	/**
	 * The feature id for the '<em><b>Shutter Closed Phys</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_TYPE__SHUTTER_CLOSED_PHYS = 1;

	/**
	 * The number of structural features of the '<em>Shutter Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHUTTER_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.StagePosNXSPathTypeImpl <em>Stage Pos NXS Path Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.StagePosNXSPathTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getStagePosNXSPathType()
	 * @generated
	 */
	int STAGE_POS_NXS_PATH_TYPE = 15;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_POS_NXS_PATH_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_POS_NXS_PATH_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Stage Pos NXS Path Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_POS_NXS_PATH_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.StageRotNXSPathTypeImpl <em>Stage Rot NXS Path Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.StageRotNXSPathTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getStageRotNXSPathType()
	 * @generated
	 */
	int STAGE_ROT_NXS_PATH_TYPE = 16;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_ROT_NXS_PATH_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_ROT_NXS_PATH_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Stage Rot NXS Path Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_ROT_NXS_PATH_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.TifimageTypeImpl <em>Tifimage Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.TifimageTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getTifimageType()
	 * @generated
	 */
	int TIFIMAGE_TYPE = 17;

	/**
	 * The feature id for the '<em><b>Filename Fmt</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIFIMAGE_TYPE__FILENAME_FMT = 0;

	/**
	 * The number of structural features of the '<em>Tifimage Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIFIMAGE_TYPE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.TifNXSPathTypeImpl <em>Tif NXS Path Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.TifNXSPathTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getTifNXSPathType()
	 * @generated
	 */
	int TIF_NXS_PATH_TYPE = 18;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIF_NXS_PATH_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIF_NXS_PATH_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Tif NXS Path Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIF_NXS_PATH_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.TomodoTypeImpl <em>Tomodo Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.TomodoTypeImpl
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getTomodoType()
	 * @generated
	 */
	int TOMODO_TYPE = 19;

	/**
	 * The feature id for the '<em><b>Shutter</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE__SHUTTER = 0;

	/**
	 * The feature id for the '<em><b>Tifimage</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE__TIFIMAGE = 1;

	/**
	 * The feature id for the '<em><b>Nexusfile</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE__NEXUSFILE = 2;

	/**
	 * The feature id for the '<em><b>Settingsfile</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE__SETTINGSFILE = 3;

	/**
	 * The feature id for the '<em><b>Imagekeyencoding</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE__IMAGEKEYENCODING = 4;

	/**
	 * The feature id for the '<em><b>Cluster</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE__CLUSTER = 5;

	/**
	 * The number of structural features of the '<em>Tomodo Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMODO_TYPE_FEATURE_COUNT = 6;


	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType <em>Beamline Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Beamline Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType
	 * @generated
	 */
	EClass getBeamlineType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType#getIxx <em>Ixx</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ixx</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType#getIxx()
	 * @see #getBeamlineType()
	 * @generated
	 */
	EReference getBeamlineType_Ixx();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ClusterType <em>Cluster Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Cluster Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ClusterType
	 * @generated
	 */
	EClass getClusterType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.ClusterType#getQsub <em>Qsub</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Qsub</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ClusterType#getQsub()
	 * @see #getClusterType()
	 * @generated
	 */
	EReference getClusterType_Qsub();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot
	 * @generated
	 */
	EClass getDocumentRoot();

	/**
	 * Returns the meta object for the attribute list '{@link uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getMixed <em>Mixed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getMixed()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EAttribute getDocumentRoot_Mixed();

	/**
	 * Returns the meta object for the map '{@link uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getXMLNSPrefixMap()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_XMLNSPrefixMap();

	/**
	 * Returns the meta object for the map '{@link uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XSI Schema Location</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getXSISchemaLocation()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_XSISchemaLocation();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getLocalTomo <em>Local Tomo</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Local Tomo</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot#getLocalTomo()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_LocalTomo();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType <em>Filename Fmt Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Filename Fmt Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType
	 * @generated
	 */
	EClass getFilenameFmtType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType#getValue()
	 * @see #getFilenameFmtType()
	 * @generated
	 */
	EAttribute getFilenameFmtType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType#getInfo()
	 * @see #getFilenameFmtType()
	 * @generated
	 */
	EAttribute getFilenameFmtType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType <em>Imagekeyencoding Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Imagekeyencoding Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType
	 * @generated
	 */
	EClass getImagekeyencodingType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getDarkfield <em>Darkfield</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Darkfield</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getDarkfield()
	 * @see #getImagekeyencodingType()
	 * @generated
	 */
	EAttribute getImagekeyencodingType_Darkfield();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getFlatfield <em>Flatfield</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Flatfield</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getFlatfield()
	 * @see #getImagekeyencodingType()
	 * @generated
	 */
	EAttribute getImagekeyencodingType_Flatfield();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getProjection <em>Projection</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Projection</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getProjection()
	 * @see #getImagekeyencodingType()
	 * @generated
	 */
	EAttribute getImagekeyencodingType_Projection();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType <em>Imgkey NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Imgkey NXS Path Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType
	 * @generated
	 */
	EClass getImgkeyNXSPathType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType#getValue()
	 * @see #getImgkeyNXSPathType()
	 * @generated
	 */
	EAttribute getImgkeyNXSPathType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType#getInfo()
	 * @see #getImgkeyNXSPathType()
	 * @generated
	 */
	EAttribute getImgkeyNXSPathType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.IxxType <em>Ixx Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ixx Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.IxxType
	 * @generated
	 */
	EClass getIxxType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.IxxType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.IxxType#getValue()
	 * @see #getIxxType()
	 * @generated
	 */
	EAttribute getIxxType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.IxxType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.IxxType#getInfo()
	 * @see #getIxxType()
	 * @generated
	 */
	EAttribute getIxxType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType
	 * @generated
	 */
	EClass getLocalTomoType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getBeamline <em>Beamline</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Beamline</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getBeamline()
	 * @see #getLocalTomoType()
	 * @generated
	 */
	EReference getLocalTomoType_Beamline();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getTomodo <em>Tomodo</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Tomodo</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getTomodo()
	 * @see #getLocalTomoType()
	 * @generated
	 */
	EReference getLocalTomoType_Tomodo();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType <em>Nexusfile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Nexusfile Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType
	 * @generated
	 */
	EClass getNexusfileType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getShutterNXSPath <em>Shutter NXS Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Shutter NXS Path</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getShutterNXSPath()
	 * @see #getNexusfileType()
	 * @generated
	 */
	EReference getNexusfileType_ShutterNXSPath();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStagePosNXSPath <em>Stage Pos NXS Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Stage Pos NXS Path</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStagePosNXSPath()
	 * @see #getNexusfileType()
	 * @generated
	 */
	EReference getNexusfileType_StagePosNXSPath();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStageRotNXSPath <em>Stage Rot NXS Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Stage Rot NXS Path</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getStageRotNXSPath()
	 * @see #getNexusfileType()
	 * @generated
	 */
	EReference getNexusfileType_StageRotNXSPath();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getTifNXSPath <em>Tif NXS Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Tif NXS Path</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getTifNXSPath()
	 * @see #getNexusfileType()
	 * @generated
	 */
	EReference getNexusfileType_TifNXSPath();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getImgkeyNXSPath <em>Imgkey NXS Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Imgkey NXS Path</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType#getImgkeyNXSPath()
	 * @see #getNexusfileType()
	 * @generated
	 */
	EReference getNexusfileType_ImgkeyNXSPath();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType <em>Qsub Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Qsub Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.QsubType
	 * @generated
	 */
	EClass getQsubType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getProjectname <em>Projectname</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Projectname</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getProjectname()
	 * @see #getQsubType()
	 * @generated
	 */
	EAttribute getQsubType_Projectname();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getArgs <em>Args</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Args</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getArgs()
	 * @see #getQsubType()
	 * @generated
	 */
	EAttribute getQsubType_Args();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getSinoqueue <em>Sinoqueue</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sinoqueue</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getSinoqueue()
	 * @see #getQsubType()
	 * @generated
	 */
	EAttribute getQsubType_Sinoqueue();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getReconqueue <em>Reconqueue</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reconqueue</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getReconqueue()
	 * @see #getQsubType()
	 * @generated
	 */
	EAttribute getQsubType_Reconqueue();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.SettingsfileType <em>Settingsfile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Settingsfile Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.SettingsfileType
	 * @generated
	 */
	EClass getSettingsfileType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.SettingsfileType#getBlueprint <em>Blueprint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Blueprint</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.SettingsfileType#getBlueprint()
	 * @see #getSettingsfileType()
	 * @generated
	 */
	EAttribute getSettingsfileType_Blueprint();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType <em>Shutter Closed Phys Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shutter Closed Phys Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType
	 * @generated
	 */
	EClass getShutterClosedPhysType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType#getValue()
	 * @see #getShutterClosedPhysType()
	 * @generated
	 */
	EAttribute getShutterClosedPhysType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType#getInfo()
	 * @see #getShutterClosedPhysType()
	 * @generated
	 */
	EAttribute getShutterClosedPhysType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType <em>Shutter NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shutter NXS Path Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType
	 * @generated
	 */
	EClass getShutterNXSPathType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType#getValue()
	 * @see #getShutterNXSPathType()
	 * @generated
	 */
	EAttribute getShutterNXSPathType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType#getInfo()
	 * @see #getShutterNXSPathType()
	 * @generated
	 */
	EAttribute getShutterNXSPathType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType <em>Shutter Open Phys Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shutter Open Phys Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType
	 * @generated
	 */
	EClass getShutterOpenPhysType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType#getValue()
	 * @see #getShutterOpenPhysType()
	 * @generated
	 */
	EAttribute getShutterOpenPhysType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType#getInfo()
	 * @see #getShutterOpenPhysType()
	 * @generated
	 */
	EAttribute getShutterOpenPhysType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterType <em>Shutter Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shutter Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterType
	 * @generated
	 */
	EClass getShutterType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterType#getShutterOpenPhys <em>Shutter Open Phys</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Shutter Open Phys</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterType#getShutterOpenPhys()
	 * @see #getShutterType()
	 * @generated
	 */
	EReference getShutterType_ShutterOpenPhys();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.ShutterType#getShutterClosedPhys <em>Shutter Closed Phys</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Shutter Closed Phys</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.ShutterType#getShutterClosedPhys()
	 * @see #getShutterType()
	 * @generated
	 */
	EReference getShutterType_ShutterClosedPhys();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType <em>Stage Pos NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stage Pos NXS Path Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType
	 * @generated
	 */
	EClass getStagePosNXSPathType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType#getValue()
	 * @see #getStagePosNXSPathType()
	 * @generated
	 */
	EAttribute getStagePosNXSPathType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType#getInfo()
	 * @see #getStagePosNXSPathType()
	 * @generated
	 */
	EAttribute getStagePosNXSPathType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType <em>Stage Rot NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stage Rot NXS Path Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType
	 * @generated
	 */
	EClass getStageRotNXSPathType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType#getValue()
	 * @see #getStageRotNXSPathType()
	 * @generated
	 */
	EAttribute getStageRotNXSPathType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType#getInfo()
	 * @see #getStageRotNXSPathType()
	 * @generated
	 */
	EAttribute getStageRotNXSPathType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.TifimageType <em>Tifimage Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tifimage Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TifimageType
	 * @generated
	 */
	EClass getTifimageType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TifimageType#getFilenameFmt <em>Filename Fmt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Filename Fmt</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TifimageType#getFilenameFmt()
	 * @see #getTifimageType()
	 * @generated
	 */
	EReference getTifimageType_FilenameFmt();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType <em>Tif NXS Path Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tif NXS Path Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType
	 * @generated
	 */
	EClass getTifNXSPathType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType#getValue()
	 * @see #getTifNXSPathType()
	 * @generated
	 */
	EAttribute getTifNXSPathType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType#getInfo()
	 * @see #getTifNXSPathType()
	 * @generated
	 */
	EAttribute getTifNXSPathType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType <em>Tomodo Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tomodo Type</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType
	 * @generated
	 */
	EClass getTomodoType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getShutter <em>Shutter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Shutter</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getShutter()
	 * @see #getTomodoType()
	 * @generated
	 */
	EReference getTomodoType_Shutter();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getTifimage <em>Tifimage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Tifimage</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getTifimage()
	 * @see #getTomodoType()
	 * @generated
	 */
	EReference getTomodoType_Tifimage();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getNexusfile <em>Nexusfile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Nexusfile</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getNexusfile()
	 * @see #getTomodoType()
	 * @generated
	 */
	EReference getTomodoType_Nexusfile();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getSettingsfile <em>Settingsfile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Settingsfile</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getSettingsfile()
	 * @see #getTomodoType()
	 * @generated
	 */
	EReference getTomodoType_Settingsfile();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getImagekeyencoding <em>Imagekeyencoding</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Imagekeyencoding</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getImagekeyencoding()
	 * @see #getTomodoType()
	 * @generated
	 */
	EReference getTomodoType_Imagekeyencoding();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getCluster <em>Cluster</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Cluster</em>'.
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.TomodoType#getCluster()
	 * @see #getTomodoType()
	 * @generated
	 */
	EReference getTomodoType_Cluster();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	LocalTomoFactory getLocalTomoFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.BeamlineTypeImpl <em>Beamline Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.BeamlineTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getBeamlineType()
		 * @generated
		 */
		EClass BEAMLINE_TYPE = eINSTANCE.getBeamlineType();

		/**
		 * The meta object literal for the '<em><b>Ixx</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BEAMLINE_TYPE__IXX = eINSTANCE.getBeamlineType_Ixx();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ClusterTypeImpl <em>Cluster Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ClusterTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getClusterType()
		 * @generated
		 */
		EClass CLUSTER_TYPE = eINSTANCE.getClusterType();

		/**
		 * The meta object literal for the '<em><b>Qsub</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLUSTER_TYPE__QSUB = eINSTANCE.getClusterType_Qsub();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.DocumentRootImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getDocumentRoot()
		 * @generated
		 */
		EClass DOCUMENT_ROOT = eINSTANCE.getDocumentRoot();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DOCUMENT_ROOT__MIXED = eINSTANCE.getDocumentRoot_Mixed();

		/**
		 * The meta object literal for the '<em><b>XMLNS Prefix Map</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__XMLNS_PREFIX_MAP = eINSTANCE.getDocumentRoot_XMLNSPrefixMap();

		/**
		 * The meta object literal for the '<em><b>XSI Schema Location</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = eINSTANCE.getDocumentRoot_XSISchemaLocation();

		/**
		 * The meta object literal for the '<em><b>Local Tomo</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__LOCAL_TOMO = eINSTANCE.getDocumentRoot_LocalTomo();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.FilenameFmtTypeImpl <em>Filename Fmt Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.FilenameFmtTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getFilenameFmtType()
		 * @generated
		 */
		EClass FILENAME_FMT_TYPE = eINSTANCE.getFilenameFmtType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILENAME_FMT_TYPE__VALUE = eINSTANCE.getFilenameFmtType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILENAME_FMT_TYPE__INFO = eINSTANCE.getFilenameFmtType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl <em>Imagekeyencoding Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getImagekeyencodingType()
		 * @generated
		 */
		EClass IMAGEKEYENCODING_TYPE = eINSTANCE.getImagekeyencodingType();

		/**
		 * The meta object literal for the '<em><b>Darkfield</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGEKEYENCODING_TYPE__DARKFIELD = eINSTANCE.getImagekeyencodingType_Darkfield();

		/**
		 * The meta object literal for the '<em><b>Flatfield</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGEKEYENCODING_TYPE__FLATFIELD = eINSTANCE.getImagekeyencodingType_Flatfield();

		/**
		 * The meta object literal for the '<em><b>Projection</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGEKEYENCODING_TYPE__PROJECTION = eINSTANCE.getImagekeyencodingType_Projection();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImgkeyNXSPathTypeImpl <em>Imgkey NXS Path Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ImgkeyNXSPathTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getImgkeyNXSPathType()
		 * @generated
		 */
		EClass IMGKEY_NXS_PATH_TYPE = eINSTANCE.getImgkeyNXSPathType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMGKEY_NXS_PATH_TYPE__VALUE = eINSTANCE.getImgkeyNXSPathType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMGKEY_NXS_PATH_TYPE__INFO = eINSTANCE.getImgkeyNXSPathType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.IxxTypeImpl <em>Ixx Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.IxxTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getIxxType()
		 * @generated
		 */
		EClass IXX_TYPE = eINSTANCE.getIxxType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXX_TYPE__VALUE = eINSTANCE.getIxxType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXX_TYPE__INFO = eINSTANCE.getIxxType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoTypeImpl <em>Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getLocalTomoType()
		 * @generated
		 */
		EClass LOCAL_TOMO_TYPE = eINSTANCE.getLocalTomoType();

		/**
		 * The meta object literal for the '<em><b>Beamline</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LOCAL_TOMO_TYPE__BEAMLINE = eINSTANCE.getLocalTomoType_Beamline();

		/**
		 * The meta object literal for the '<em><b>Tomodo</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LOCAL_TOMO_TYPE__TOMODO = eINSTANCE.getLocalTomoType_Tomodo();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.NexusfileTypeImpl <em>Nexusfile Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.NexusfileTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getNexusfileType()
		 * @generated
		 */
		EClass NEXUSFILE_TYPE = eINSTANCE.getNexusfileType();

		/**
		 * The meta object literal for the '<em><b>Shutter NXS Path</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NEXUSFILE_TYPE__SHUTTER_NXS_PATH = eINSTANCE.getNexusfileType_ShutterNXSPath();

		/**
		 * The meta object literal for the '<em><b>Stage Pos NXS Path</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NEXUSFILE_TYPE__STAGE_POS_NXS_PATH = eINSTANCE.getNexusfileType_StagePosNXSPath();

		/**
		 * The meta object literal for the '<em><b>Stage Rot NXS Path</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH = eINSTANCE.getNexusfileType_StageRotNXSPath();

		/**
		 * The meta object literal for the '<em><b>Tif NXS Path</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NEXUSFILE_TYPE__TIF_NXS_PATH = eINSTANCE.getNexusfileType_TifNXSPath();

		/**
		 * The meta object literal for the '<em><b>Imgkey NXS Path</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NEXUSFILE_TYPE__IMGKEY_NXS_PATH = eINSTANCE.getNexusfileType_ImgkeyNXSPath();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.QsubTypeImpl <em>Qsub Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.QsubTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getQsubType()
		 * @generated
		 */
		EClass QSUB_TYPE = eINSTANCE.getQsubType();

		/**
		 * The meta object literal for the '<em><b>Projectname</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute QSUB_TYPE__PROJECTNAME = eINSTANCE.getQsubType_Projectname();

		/**
		 * The meta object literal for the '<em><b>Args</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute QSUB_TYPE__ARGS = eINSTANCE.getQsubType_Args();

		/**
		 * The meta object literal for the '<em><b>Sinoqueue</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute QSUB_TYPE__SINOQUEUE = eINSTANCE.getQsubType_Sinoqueue();

		/**
		 * The meta object literal for the '<em><b>Reconqueue</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute QSUB_TYPE__RECONQUEUE = eINSTANCE.getQsubType_Reconqueue();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.SettingsfileTypeImpl <em>Settingsfile Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.SettingsfileTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getSettingsfileType()
		 * @generated
		 */
		EClass SETTINGSFILE_TYPE = eINSTANCE.getSettingsfileType();

		/**
		 * The meta object literal for the '<em><b>Blueprint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SETTINGSFILE_TYPE__BLUEPRINT = eINSTANCE.getSettingsfileType_Blueprint();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterClosedPhysTypeImpl <em>Shutter Closed Phys Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterClosedPhysTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterClosedPhysType()
		 * @generated
		 */
		EClass SHUTTER_CLOSED_PHYS_TYPE = eINSTANCE.getShutterClosedPhysType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHUTTER_CLOSED_PHYS_TYPE__VALUE = eINSTANCE.getShutterClosedPhysType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHUTTER_CLOSED_PHYS_TYPE__INFO = eINSTANCE.getShutterClosedPhysType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterNXSPathTypeImpl <em>Shutter NXS Path Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterNXSPathTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterNXSPathType()
		 * @generated
		 */
		EClass SHUTTER_NXS_PATH_TYPE = eINSTANCE.getShutterNXSPathType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHUTTER_NXS_PATH_TYPE__VALUE = eINSTANCE.getShutterNXSPathType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHUTTER_NXS_PATH_TYPE__INFO = eINSTANCE.getShutterNXSPathType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterOpenPhysTypeImpl <em>Shutter Open Phys Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterOpenPhysTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterOpenPhysType()
		 * @generated
		 */
		EClass SHUTTER_OPEN_PHYS_TYPE = eINSTANCE.getShutterOpenPhysType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHUTTER_OPEN_PHYS_TYPE__VALUE = eINSTANCE.getShutterOpenPhysType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHUTTER_OPEN_PHYS_TYPE__INFO = eINSTANCE.getShutterOpenPhysType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterTypeImpl <em>Shutter Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.ShutterTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getShutterType()
		 * @generated
		 */
		EClass SHUTTER_TYPE = eINSTANCE.getShutterType();

		/**
		 * The meta object literal for the '<em><b>Shutter Open Phys</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHUTTER_TYPE__SHUTTER_OPEN_PHYS = eINSTANCE.getShutterType_ShutterOpenPhys();

		/**
		 * The meta object literal for the '<em><b>Shutter Closed Phys</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SHUTTER_TYPE__SHUTTER_CLOSED_PHYS = eINSTANCE.getShutterType_ShutterClosedPhys();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.StagePosNXSPathTypeImpl <em>Stage Pos NXS Path Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.StagePosNXSPathTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getStagePosNXSPathType()
		 * @generated
		 */
		EClass STAGE_POS_NXS_PATH_TYPE = eINSTANCE.getStagePosNXSPathType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_POS_NXS_PATH_TYPE__VALUE = eINSTANCE.getStagePosNXSPathType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_POS_NXS_PATH_TYPE__INFO = eINSTANCE.getStagePosNXSPathType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.StageRotNXSPathTypeImpl <em>Stage Rot NXS Path Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.StageRotNXSPathTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getStageRotNXSPathType()
		 * @generated
		 */
		EClass STAGE_ROT_NXS_PATH_TYPE = eINSTANCE.getStageRotNXSPathType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_ROT_NXS_PATH_TYPE__VALUE = eINSTANCE.getStageRotNXSPathType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_ROT_NXS_PATH_TYPE__INFO = eINSTANCE.getStageRotNXSPathType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.TifimageTypeImpl <em>Tifimage Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.TifimageTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getTifimageType()
		 * @generated
		 */
		EClass TIFIMAGE_TYPE = eINSTANCE.getTifimageType();

		/**
		 * The meta object literal for the '<em><b>Filename Fmt</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TIFIMAGE_TYPE__FILENAME_FMT = eINSTANCE.getTifimageType_FilenameFmt();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.TifNXSPathTypeImpl <em>Tif NXS Path Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.TifNXSPathTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getTifNXSPathType()
		 * @generated
		 */
		EClass TIF_NXS_PATH_TYPE = eINSTANCE.getTifNXSPathType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIF_NXS_PATH_TYPE__VALUE = eINSTANCE.getTifNXSPathType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIF_NXS_PATH_TYPE__INFO = eINSTANCE.getTifNXSPathType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.TomodoTypeImpl <em>Tomodo Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.TomodoTypeImpl
		 * @see uk.ac.diamond.tomography.localtomo.localtomo.impl.LocalTomoPackageImpl#getTomodoType()
		 * @generated
		 */
		EClass TOMODO_TYPE = eINSTANCE.getTomodoType();

		/**
		 * The meta object literal for the '<em><b>Shutter</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TOMODO_TYPE__SHUTTER = eINSTANCE.getTomodoType_Shutter();

		/**
		 * The meta object literal for the '<em><b>Tifimage</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TOMODO_TYPE__TIFIMAGE = eINSTANCE.getTomodoType_Tifimage();

		/**
		 * The meta object literal for the '<em><b>Nexusfile</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TOMODO_TYPE__NEXUSFILE = eINSTANCE.getTomodoType_Nexusfile();

		/**
		 * The meta object literal for the '<em><b>Settingsfile</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TOMODO_TYPE__SETTINGSFILE = eINSTANCE.getTomodoType_Settingsfile();

		/**
		 * The meta object literal for the '<em><b>Imagekeyencoding</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TOMODO_TYPE__IMAGEKEYENCODING = eINSTANCE.getTomodoType_Imagekeyencoding();

		/**
		 * The meta object literal for the '<em><b>Cluster</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TOMODO_TYPE__CLUSTER = eINSTANCE.getTomodoType_Cluster();

	}

} //LocalTomoPackage
