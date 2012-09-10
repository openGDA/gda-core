/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

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
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmFactory
 * @model kind="package"
 *        extendedMetaData="qualified='false'"
 * @generated
 */
public interface HmPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "hm";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "platform:/resource/uk.ac.diamond.tomography.reconstruction.parameters/model/hm.xsd";

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
	HmPackage eINSTANCE = uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl <em>Backprojection Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getBackprojectionType()
	 * @generated
	 */
	int BACKPROJECTION_TYPE = 0;

	/**
	 * The feature id for the '<em><b>Filter</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__FILTER = 0;

	/**
	 * The feature id for the '<em><b>Image Centre</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__IMAGE_CENTRE = 1;

	/**
	 * The feature id for the '<em><b>Clockwise Rotation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__CLOCKWISE_ROTATION = 2;

	/**
	 * The feature id for the '<em><b>Tilt</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__TILT = 3;

	/**
	 * The feature id for the '<em><b>Coordinate System</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__COORDINATE_SYSTEM = 4;

	/**
	 * The feature id for the '<em><b>Circles</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__CIRCLES = 5;

	/**
	 * The feature id for the '<em><b>ROI</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__ROI = 6;

	/**
	 * The feature id for the '<em><b>Polar Cartesian Interpolation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION = 7;

	/**
	 * The number of structural features of the '<em>Backprojection Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BACKPROJECTION_TYPE_FEATURE_COUNT = 8;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl <em>Beamline User Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getBeamlineUserType()
	 * @generated
	 */
	int BEAMLINE_USER_TYPE = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Beamline Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__BEAMLINE_NAME = 1;

	/**
	 * The feature id for the '<em><b>Year</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__YEAR = 2;

	/**
	 * The feature id for the '<em><b>Month</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__MONTH = 3;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__DATE = 4;

	/**
	 * The feature id for the '<em><b>Visit Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__VISIT_NUMBER = 5;

	/**
	 * The feature id for the '<em><b>Input Data Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER = 6;

	/**
	 * The feature id for the '<em><b>Input Scan Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER = 7;

	/**
	 * The feature id for the '<em><b>Output Data Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER = 8;

	/**
	 * The feature id for the '<em><b>Output Scan Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER = 9;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE__DONE = 10;

	/**
	 * The number of structural features of the '<em>Beamline User Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BEAMLINE_USER_TYPE_FEATURE_COUNT = 11;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BitsTypeTypeImpl <em>Bits Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BitsTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getBitsTypeType()
	 * @generated
	 */
	int BITS_TYPE_TYPE = 2;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BITS_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BITS_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Bits Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BITS_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ByteOrderTypeImpl <em>Byte Order Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ByteOrderTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getByteOrderType()
	 * @generated
	 */
	int BYTE_ORDER_TYPE = 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BYTE_ORDER_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BYTE_ORDER_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Byte Order Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BYTE_ORDER_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl <em>Circles Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getCirclesType()
	 * @generated
	 */
	int CIRCLES_TYPE = 4;

	/**
	 * The feature id for the '<em><b>Value Min</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CIRCLES_TYPE__VALUE_MIN = 0;

	/**
	 * The feature id for the '<em><b>Value Max</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CIRCLES_TYPE__VALUE_MAX = 1;

	/**
	 * The feature id for the '<em><b>Value Step</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CIRCLES_TYPE__VALUE_STEP = 2;

	/**
	 * The feature id for the '<em><b>Comm</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CIRCLES_TYPE__COMM = 3;

	/**
	 * The number of structural features of the '<em>Circles Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CIRCLES_TYPE_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ClockwiseRotationTypeImpl <em>Clockwise Rotation Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ClockwiseRotationTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getClockwiseRotationType()
	 * @generated
	 */
	int CLOCKWISE_ROTATION_TYPE = 5;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOCKWISE_ROTATION_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOCKWISE_ROTATION_TYPE__DONE = 1;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOCKWISE_ROTATION_TYPE__INFO = 2;

	/**
	 * The number of structural features of the '<em>Clockwise Rotation Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOCKWISE_ROTATION_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl <em>Coordinate System Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getCoordinateSystemType()
	 * @generated
	 */
	int COORDINATE_SYSTEM_TYPE = 6;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COORDINATE_SYSTEM_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Slice</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COORDINATE_SYSTEM_TYPE__SLICE = 1;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COORDINATE_SYSTEM_TYPE__DONE = 2;

	/**
	 * The number of structural features of the '<em>Coordinate System Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COORDINATE_SYSTEM_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl <em>Dark Field Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getDarkFieldType()
	 * @generated
	 */
	int DARK_FIELD_TYPE = 7;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Value Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__VALUE_BEFORE = 1;

	/**
	 * The feature id for the '<em><b>Value After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__VALUE_AFTER = 2;

	/**
	 * The feature id for the '<em><b>File Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__FILE_BEFORE = 3;

	/**
	 * The feature id for the '<em><b>File After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__FILE_AFTER = 4;

	/**
	 * The feature id for the '<em><b>Profile Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__PROFILE_TYPE = 5;

	/**
	 * The feature id for the '<em><b>File Profile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE__FILE_PROFILE = 6;

	/**
	 * The number of structural features of the '<em>Dark Field Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DARK_FIELD_TYPE_FEATURE_COUNT = 7;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DefaultXmlTypeImpl <em>Default Xml Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DefaultXmlTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getDefaultXmlType()
	 * @generated
	 */
	int DEFAULT_XML_TYPE = 8;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFAULT_XML_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFAULT_XML_TYPE__DONE = 1;

	/**
	 * The number of structural features of the '<em>Default Xml Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFAULT_XML_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DocumentRootImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getDocumentRoot()
	 * @generated
	 */
	int DOCUMENT_ROOT = 9;

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
	 * The feature id for the '<em><b>HMxml</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__HMXML = 3;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ExtrapolationTypeTypeImpl <em>Extrapolation Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ExtrapolationTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getExtrapolationTypeType()
	 * @generated
	 */
	int EXTRAPOLATION_TYPE_TYPE = 10;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTRAPOLATION_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTRAPOLATION_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Extrapolation Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTRAPOLATION_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl <em>FBP Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFBPType()
	 * @generated
	 */
	int FBP_TYPE = 11;

	/**
	 * The feature id for the '<em><b>Default Xml</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__DEFAULT_XML = 0;

	/**
	 * The feature id for the '<em><b>GPU Device Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__GPU_DEVICE_NUMBER = 1;

	/**
	 * The feature id for the '<em><b>Beamline User</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__BEAMLINE_USER = 2;

	/**
	 * The feature id for the '<em><b>Log File</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__LOG_FILE = 3;

	/**
	 * The feature id for the '<em><b>Input Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__INPUT_DATA = 4;

	/**
	 * The feature id for the '<em><b>Flat Dark Fields</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__FLAT_DARK_FIELDS = 5;

	/**
	 * The feature id for the '<em><b>Preprocessing</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__PREPROCESSING = 6;

	/**
	 * The feature id for the '<em><b>Transform</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__TRANSFORM = 7;

	/**
	 * The feature id for the '<em><b>Backprojection</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__BACKPROJECTION = 8;

	/**
	 * The feature id for the '<em><b>Output Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE__OUTPUT_DATA = 9;

	/**
	 * The number of structural features of the '<em>FBP Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FBP_TYPE_FEATURE_COUNT = 10;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl <em>Filter Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFilterType()
	 * @generated
	 */
	int FILTER_TYPE = 12;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE__NAME = 1;

	/**
	 * The feature id for the '<em><b>Bandwidth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE__BANDWIDTH = 2;

	/**
	 * The feature id for the '<em><b>Window Name</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE__WINDOW_NAME = 3;

	/**
	 * The feature id for the '<em><b>Normalisation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE__NORMALISATION = 4;

	/**
	 * The feature id for the '<em><b>Pixel Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE__PIXEL_SIZE = 5;

	/**
	 * The number of structural features of the '<em>Filter Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TYPE_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FirstImageIndexTypeImpl <em>First Image Index Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FirstImageIndexTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFirstImageIndexType()
	 * @generated
	 */
	int FIRST_IMAGE_INDEX_TYPE = 13;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_IMAGE_INDEX_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_IMAGE_INDEX_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>First Image Index Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_IMAGE_INDEX_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatDarkFieldsTypeImpl <em>Flat Dark Fields Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatDarkFieldsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFlatDarkFieldsType()
	 * @generated
	 */
	int FLAT_DARK_FIELDS_TYPE = 14;

	/**
	 * The feature id for the '<em><b>Flat Field</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_DARK_FIELDS_TYPE__FLAT_FIELD = 0;

	/**
	 * The feature id for the '<em><b>Dark Field</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_DARK_FIELDS_TYPE__DARK_FIELD = 1;

	/**
	 * The number of structural features of the '<em>Flat Dark Fields Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_DARK_FIELDS_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl <em>Flat Field Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFlatFieldType()
	 * @generated
	 */
	int FLAT_FIELD_TYPE = 15;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Value Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__VALUE_BEFORE = 1;

	/**
	 * The feature id for the '<em><b>Value After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__VALUE_AFTER = 2;

	/**
	 * The feature id for the '<em><b>File Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__FILE_BEFORE = 3;

	/**
	 * The feature id for the '<em><b>File After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__FILE_AFTER = 4;

	/**
	 * The feature id for the '<em><b>Profile Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__PROFILE_TYPE = 5;

	/**
	 * The feature id for the '<em><b>File Profile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE__FILE_PROFILE = 6;

	/**
	 * The number of structural features of the '<em>Flat Field Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLAT_FIELD_TYPE_FEATURE_COUNT = 7;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.GapTypeImpl <em>Gap Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.GapTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getGapType()
	 * @generated
	 */
	int GAP_TYPE = 16;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Gap Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterColumnsTypeImpl <em>High Peaks After Columns Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterColumnsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHighPeaksAfterColumnsType()
	 * @generated
	 */
	int HIGH_PEAKS_AFTER_COLUMNS_TYPE = 17;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_COLUMNS_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Number Pixels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_COLUMNS_TYPE__NUMBER_PIXELS = 1;

	/**
	 * The feature id for the '<em><b>Jump</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_COLUMNS_TYPE__JUMP = 2;

	/**
	 * The number of structural features of the '<em>High Peaks After Columns Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_COLUMNS_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl <em>High Peaks After Rows Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHighPeaksAfterRowsType()
	 * @generated
	 */
	int HIGH_PEAKS_AFTER_ROWS_TYPE = 18;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Number Pixels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS = 1;

	/**
	 * The feature id for the '<em><b>Jump</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP = 2;

	/**
	 * The number of structural features of the '<em>High Peaks After Rows Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_AFTER_ROWS_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksBeforeTypeImpl <em>High Peaks Before Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksBeforeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHighPeaksBeforeType()
	 * @generated
	 */
	int HIGH_PEAKS_BEFORE_TYPE = 19;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_BEFORE_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Number Pixels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_BEFORE_TYPE__NUMBER_PIXELS = 1;

	/**
	 * The feature id for the '<em><b>Jump</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_BEFORE_TYPE__JUMP = 2;

	/**
	 * The number of structural features of the '<em>High Peaks Before Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HIGH_PEAKS_BEFORE_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HMxmlTypeImpl <em>HMxml Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HMxmlTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHMxmlType()
	 * @generated
	 */
	int HMXML_TYPE = 20;

	/**
	 * The feature id for the '<em><b>FBP</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HMXML_TYPE__FBP = 0;

	/**
	 * The number of structural features of the '<em>HMxml Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HMXML_TYPE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageFirstTypeImpl <em>Image First Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageFirstTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getImageFirstType()
	 * @generated
	 */
	int IMAGE_FIRST_TYPE = 21;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_FIRST_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_FIRST_TYPE__DONE = 1;

	/**
	 * The number of structural features of the '<em>Image First Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_FIRST_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageLastTypeImpl <em>Image Last Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageLastTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getImageLastType()
	 * @generated
	 */
	int IMAGE_LAST_TYPE = 22;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_LAST_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_LAST_TYPE__DONE = 1;

	/**
	 * The number of structural features of the '<em>Image Last Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_LAST_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageStepTypeImpl <em>Image Step Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageStepTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getImageStepType()
	 * @generated
	 */
	int IMAGE_STEP_TYPE = 23;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_STEP_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_STEP_TYPE__DONE = 1;

	/**
	 * The number of structural features of the '<em>Image Step Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMAGE_STEP_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl <em>Input Data Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getInputDataType()
	 * @generated
	 */
	int INPUT_DATA_TYPE = 24;

	/**
	 * The feature id for the '<em><b>Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__FOLDER = 0;

	/**
	 * The feature id for the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__PREFIX = 1;

	/**
	 * The feature id for the '<em><b>Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__SUFFIX = 2;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__EXTENSION = 3;

	/**
	 * The feature id for the '<em><b>NOD</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__NOD = 4;

	/**
	 * The feature id for the '<em><b>Memory Size Max</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__MEMORY_SIZE_MAX = 5;

	/**
	 * The feature id for the '<em><b>Memory Size Min</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__MEMORY_SIZE_MIN = 6;

	/**
	 * The feature id for the '<em><b>Orientation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__ORIENTATION = 7;

	/**
	 * The feature id for the '<em><b>File First</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__FILE_FIRST = 8;

	/**
	 * The feature id for the '<em><b>File Last</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__FILE_LAST = 9;

	/**
	 * The feature id for the '<em><b>File Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__FILE_STEP = 10;

	/**
	 * The feature id for the '<em><b>Image First</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__IMAGE_FIRST = 11;

	/**
	 * The feature id for the '<em><b>Image Last</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__IMAGE_LAST = 12;

	/**
	 * The feature id for the '<em><b>Image Step</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__IMAGE_STEP = 13;

	/**
	 * The feature id for the '<em><b>Raw</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__RAW = 14;

	/**
	 * The feature id for the '<em><b>First Image Index</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__FIRST_IMAGE_INDEX = 15;

	/**
	 * The feature id for the '<em><b>Images Per File</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__IMAGES_PER_FILE = 16;

	/**
	 * The feature id for the '<em><b>Restrictions</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__RESTRICTIONS = 17;

	/**
	 * The feature id for the '<em><b>Value Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__VALUE_MIN = 18;

	/**
	 * The feature id for the '<em><b>Value Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__VALUE_MAX = 19;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__TYPE = 20;

	/**
	 * The feature id for the '<em><b>Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__SHAPE = 21;

	/**
	 * The feature id for the '<em><b>Pixel Param</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE__PIXEL_PARAM = 22;

	/**
	 * The number of structural features of the '<em>Input Data Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TYPE_FEATURE_COUNT = 23;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl <em>Intensity Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getIntensityType()
	 * @generated
	 */
	int INTENSITY_TYPE = 25;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTENSITY_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Column Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTENSITY_TYPE__COLUMN_LEFT = 1;

	/**
	 * The feature id for the '<em><b>Column Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTENSITY_TYPE__COLUMN_RIGHT = 2;

	/**
	 * The feature id for the '<em><b>Zero Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTENSITY_TYPE__ZERO_LEFT = 3;

	/**
	 * The feature id for the '<em><b>Zero Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTENSITY_TYPE__ZERO_RIGHT = 4;

	/**
	 * The number of structural features of the '<em>Intensity Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTENSITY_TYPE_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InterpolationTypeImpl <em>Interpolation Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InterpolationTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getInterpolationType()
	 * @generated
	 */
	int INTERPOLATION_TYPE = 26;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERPOLATION_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERPOLATION_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Interpolation Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERPOLATION_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMaxTypeImpl <em>Memory Size Max Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMaxTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMemorySizeMaxType()
	 * @generated
	 */
	int MEMORY_SIZE_MAX_TYPE = 27;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMORY_SIZE_MAX_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMORY_SIZE_MAX_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Memory Size Max Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMORY_SIZE_MAX_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMinTypeImpl <em>Memory Size Min Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMinTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMemorySizeMinType()
	 * @generated
	 */
	int MEMORY_SIZE_MIN_TYPE = 28;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMORY_SIZE_MIN_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMORY_SIZE_MIN_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Memory Size Min Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMORY_SIZE_MIN_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeImpl <em>Missed Projections Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMissedProjectionsType()
	 * @generated
	 */
	int MISSED_PROJECTIONS_TYPE = 29;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MISSED_PROJECTIONS_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MISSED_PROJECTIONS_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Missed Projections Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MISSED_PROJECTIONS_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeTypeImpl <em>Missed Projections Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMissedProjectionsTypeType()
	 * @generated
	 */
	int MISSED_PROJECTIONS_TYPE_TYPE = 30;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MISSED_PROJECTIONS_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MISSED_PROJECTIONS_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Missed Projections Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MISSED_PROJECTIONS_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NameTypeImpl <em>Name Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NameTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNameType()
	 * @generated
	 */
	int NAME_TYPE = 31;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAME_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAME_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Name Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAME_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NODTypeImpl <em>NOD Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NODTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNODType()
	 * @generated
	 */
	int NOD_TYPE = 32;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NOD_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NOD_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>NOD Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NOD_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NormalisationTypeImpl <em>Normalisation Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NormalisationTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNormalisationType()
	 * @generated
	 */
	int NORMALISATION_TYPE = 33;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NORMALISATION_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NORMALISATION_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Normalisation Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NORMALISATION_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NumSeriesTypeImpl <em>Num Series Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NumSeriesTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNumSeriesType()
	 * @generated
	 */
	int NUM_SERIES_TYPE = 34;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUM_SERIES_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUM_SERIES_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Num Series Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUM_SERIES_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OffsetTypeImpl <em>Offset Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OffsetTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOffsetType()
	 * @generated
	 */
	int OFFSET_TYPE = 35;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OFFSET_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OFFSET_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Offset Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OFFSET_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OrientationTypeImpl <em>Orientation Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OrientationTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOrientationType()
	 * @generated
	 */
	int ORIENTATION_TYPE = 36;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORIENTATION_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORIENTATION_TYPE__DONE = 1;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORIENTATION_TYPE__INFO = 2;

	/**
	 * The number of structural features of the '<em>Orientation Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORIENTATION_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl <em>Output Data Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOutputDataType()
	 * @generated
	 */
	int OUTPUT_DATA_TYPE = 37;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>State</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__STATE = 1;

	/**
	 * The feature id for the '<em><b>Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__FOLDER = 2;

	/**
	 * The feature id for the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__PREFIX = 3;

	/**
	 * The feature id for the '<em><b>Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__SUFFIX = 4;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__EXTENSION = 5;

	/**
	 * The feature id for the '<em><b>NOD</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__NOD = 6;

	/**
	 * The feature id for the '<em><b>File First</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__FILE_FIRST = 7;

	/**
	 * The feature id for the '<em><b>File Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__FILE_STEP = 8;

	/**
	 * The feature id for the '<em><b>Bits Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__BITS_TYPE = 9;

	/**
	 * The feature id for the '<em><b>Bits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__BITS = 10;

	/**
	 * The feature id for the '<em><b>Restrictions</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__RESTRICTIONS = 11;

	/**
	 * The feature id for the '<em><b>Value Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__VALUE_MIN = 12;

	/**
	 * The feature id for the '<em><b>Value Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__VALUE_MAX = 13;

	/**
	 * The feature id for the '<em><b>Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE__SHAPE = 14;

	/**
	 * The number of structural features of the '<em>Output Data Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_DATA_TYPE_FEATURE_COUNT = 15;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputWidthTypeTypeImpl <em>Output Width Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputWidthTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOutputWidthTypeType()
	 * @generated
	 */
	int OUTPUT_WIDTH_TYPE_TYPE = 38;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_WIDTH_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_WIDTH_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Output Width Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OUTPUT_WIDTH_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PolarCartesianInterpolationTypeImpl <em>Polar Cartesian Interpolation Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PolarCartesianInterpolationTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getPolarCartesianInterpolationType()
	 * @generated
	 */
	int POLAR_CARTESIAN_INTERPOLATION_TYPE = 39;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int POLAR_CARTESIAN_INTERPOLATION_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int POLAR_CARTESIAN_INTERPOLATION_TYPE__DONE = 1;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int POLAR_CARTESIAN_INTERPOLATION_TYPE__INFO = 2;

	/**
	 * The number of structural features of the '<em>Polar Cartesian Interpolation Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int POLAR_CARTESIAN_INTERPOLATION_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl <em>Preprocessing Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getPreprocessingType()
	 * @generated
	 */
	int PREPROCESSING_TYPE = 40;

	/**
	 * The feature id for the '<em><b>High Peaks Before</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE = 0;

	/**
	 * The feature id for the '<em><b>Ring Artefacts</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PREPROCESSING_TYPE__RING_ARTEFACTS = 1;

	/**
	 * The feature id for the '<em><b>Intensity</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PREPROCESSING_TYPE__INTENSITY = 2;

	/**
	 * The feature id for the '<em><b>High Peaks After Rows</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS = 3;

	/**
	 * The feature id for the '<em><b>High Peaks After Columns</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS = 4;

	/**
	 * The number of structural features of the '<em>Preprocessing Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PREPROCESSING_TYPE_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeTypeImpl <em>Profile Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getProfileTypeType()
	 * @generated
	 */
	int PROFILE_TYPE_TYPE = 41;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROFILE_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROFILE_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Profile Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROFILE_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeType1Impl <em>Profile Type Type1</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeType1Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getProfileTypeType1()
	 * @generated
	 */
	int PROFILE_TYPE_TYPE1 = 42;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROFILE_TYPE_TYPE1__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROFILE_TYPE_TYPE1__INFO = 1;

	/**
	 * The number of structural features of the '<em>Profile Type Type1</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROFILE_TYPE_TYPE1_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl <em>Raw Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRawType()
	 * @generated
	 */
	int RAW_TYPE = 43;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Bits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__BITS = 1;

	/**
	 * The feature id for the '<em><b>Offset</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__OFFSET = 2;

	/**
	 * The feature id for the '<em><b>Byte Order</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__BYTE_ORDER = 3;

	/**
	 * The feature id for the '<em><b>Xlen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__XLEN = 4;

	/**
	 * The feature id for the '<em><b>Ylen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__YLEN = 5;

	/**
	 * The feature id for the '<em><b>Zlen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__ZLEN = 6;

	/**
	 * The feature id for the '<em><b>Gap</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__GAP = 7;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE__DONE = 8;

	/**
	 * The number of structural features of the '<em>Raw Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RAW_TYPE_FEATURE_COUNT = 9;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsTypeImpl <em>Restrictions Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRestrictionsType()
	 * @generated
	 */
	int RESTRICTIONS_TYPE = 44;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESTRICTIONS_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESTRICTIONS_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Restrictions Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESTRICTIONS_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsType1Impl <em>Restrictions Type1</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsType1Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRestrictionsType1()
	 * @generated
	 */
	int RESTRICTIONS_TYPE1 = 45;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESTRICTIONS_TYPE1__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESTRICTIONS_TYPE1__INFO = 1;

	/**
	 * The number of structural features of the '<em>Restrictions Type1</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESTRICTIONS_TYPE1_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl <em>Ring Artefacts Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRingArtefactsType()
	 * @generated
	 */
	int RING_ARTEFACTS_TYPE = 46;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RING_ARTEFACTS_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Parameter N</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RING_ARTEFACTS_TYPE__PARAMETER_N = 1;

	/**
	 * The feature id for the '<em><b>Parameter R</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RING_ARTEFACTS_TYPE__PARAMETER_R = 2;

	/**
	 * The feature id for the '<em><b>Num Series</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RING_ARTEFACTS_TYPE__NUM_SERIES = 3;

	/**
	 * The number of structural features of the '<em>Ring Artefacts Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RING_ARTEFACTS_TYPE_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl <em>ROI Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getROIType()
	 * @generated
	 */
	int ROI_TYPE = 47;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Xmin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__XMIN = 1;

	/**
	 * The feature id for the '<em><b>Xmax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__XMAX = 2;

	/**
	 * The feature id for the '<em><b>Ymin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__YMIN = 3;

	/**
	 * The feature id for the '<em><b>Ymax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__YMAX = 4;

	/**
	 * The feature id for the '<em><b>Output Width Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__OUTPUT_WIDTH_TYPE = 5;

	/**
	 * The feature id for the '<em><b>Output Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__OUTPUT_WIDTH = 6;

	/**
	 * The feature id for the '<em><b>Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE__ANGLE = 7;

	/**
	 * The number of structural features of the '<em>ROI Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROI_TYPE_FEATURE_COUNT = 8;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleEndPointsTypeImpl <em>Rotation Angle End Points Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleEndPointsTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRotationAngleEndPointsType()
	 * @generated
	 */
	int ROTATION_ANGLE_END_POINTS_TYPE = 48;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROTATION_ANGLE_END_POINTS_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROTATION_ANGLE_END_POINTS_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Rotation Angle End Points Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROTATION_ANGLE_END_POINTS_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleTypeTypeImpl <em>Rotation Angle Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRotationAngleTypeType()
	 * @generated
	 */
	int ROTATION_ANGLE_TYPE_TYPE = 49;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROTATION_ANGLE_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROTATION_ANGLE_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Rotation Angle Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROTATION_ANGLE_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ScaleTypeTypeImpl <em>Scale Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ScaleTypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getScaleTypeType()
	 * @generated
	 */
	int SCALE_TYPE_TYPE = 50;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCALE_TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCALE_TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Scale Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCALE_TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeTypeImpl <em>Shape Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getShapeType()
	 * @generated
	 */
	int SHAPE_TYPE = 51;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE__DONE = 1;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE__INFO = 2;

	/**
	 * The number of structural features of the '<em>Shape Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeType1Impl <em>Shape Type1</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeType1Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getShapeType1()
	 * @generated
	 */
	int SHAPE_TYPE1 = 52;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE1__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE1__INFO = 1;

	/**
	 * The number of structural features of the '<em>Shape Type1</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SHAPE_TYPE1_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.StateTypeImpl <em>State Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.StateTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getStateType()
	 * @generated
	 */
	int STATE_TYPE = 53;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>State Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl <em>Tilt Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTiltType()
	 * @generated
	 */
	int TILT_TYPE = 54;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TILT_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>XTilt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TILT_TYPE__XTILT = 1;

	/**
	 * The feature id for the '<em><b>ZTilt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TILT_TYPE__ZTILT = 2;

	/**
	 * The feature id for the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TILT_TYPE__DONE = 3;

	/**
	 * The number of structural features of the '<em>Tilt Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TILT_TYPE_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl <em>Transform Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTransformType()
	 * @generated
	 */
	int TRANSFORM_TYPE = 55;

	/**
	 * The feature id for the '<em><b>Missed Projections</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__MISSED_PROJECTIONS = 0;

	/**
	 * The feature id for the '<em><b>Missed Projections Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE = 1;

	/**
	 * The feature id for the '<em><b>Rotation Angle Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__ROTATION_ANGLE_TYPE = 2;

	/**
	 * The feature id for the '<em><b>Rotation Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__ROTATION_ANGLE = 3;

	/**
	 * The feature id for the '<em><b>Rotation Angle End Points</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS = 4;

	/**
	 * The feature id for the '<em><b>Re Centre Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__RE_CENTRE_ANGLE = 5;

	/**
	 * The feature id for the '<em><b>Re Centre Radius</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__RE_CENTRE_RADIUS = 6;

	/**
	 * The feature id for the '<em><b>Crop Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__CROP_TOP = 7;

	/**
	 * The feature id for the '<em><b>Crop Bottom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__CROP_BOTTOM = 8;

	/**
	 * The feature id for the '<em><b>Crop Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__CROP_LEFT = 9;

	/**
	 * The feature id for the '<em><b>Crop Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__CROP_RIGHT = 10;

	/**
	 * The feature id for the '<em><b>Scale Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__SCALE_TYPE = 11;

	/**
	 * The feature id for the '<em><b>Scale Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__SCALE_WIDTH = 12;

	/**
	 * The feature id for the '<em><b>Scale Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__SCALE_HEIGHT = 13;

	/**
	 * The feature id for the '<em><b>Extrapolation Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__EXTRAPOLATION_TYPE = 14;

	/**
	 * The feature id for the '<em><b>Extrapolation Pixels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__EXTRAPOLATION_PIXELS = 15;

	/**
	 * The feature id for the '<em><b>Extrapolation Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__EXTRAPOLATION_WIDTH = 16;

	/**
	 * The feature id for the '<em><b>Interpolation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE__INTERPOLATION = 17;

	/**
	 * The number of structural features of the '<em>Transform Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORM_TYPE_FEATURE_COUNT = 18;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeTypeImpl <em>Type Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType()
	 * @generated
	 */
	int TYPE_TYPE = 56;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType1Impl <em>Type Type1</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType1Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType1()
	 * @generated
	 */
	int TYPE_TYPE1 = 57;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE1__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE1__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type1</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE1_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType2Impl <em>Type Type2</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType2Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType2()
	 * @generated
	 */
	int TYPE_TYPE2 = 58;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE2__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE2__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type2</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE2_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType3Impl <em>Type Type3</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType3Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType3()
	 * @generated
	 */
	int TYPE_TYPE3 = 59;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE3__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE3__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type3</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE3_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType4Impl <em>Type Type4</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType4Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType4()
	 * @generated
	 */
	int TYPE_TYPE4 = 60;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE4__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE4__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type4</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE4_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType5Impl <em>Type Type5</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType5Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType5()
	 * @generated
	 */
	int TYPE_TYPE5 = 61;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE5__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE5__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type5</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE5_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType6Impl <em>Type Type6</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType6Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType6()
	 * @generated
	 */
	int TYPE_TYPE6 = 62;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE6__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE6__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type6</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE6_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType7Impl <em>Type Type7</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType7Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType7()
	 * @generated
	 */
	int TYPE_TYPE7 = 63;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE7__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE7__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type7</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE7_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType8Impl <em>Type Type8</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType8Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType8()
	 * @generated
	 */
	int TYPE_TYPE8 = 64;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE8__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE8__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type8</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE8_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType9Impl <em>Type Type9</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType9Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType9()
	 * @generated
	 */
	int TYPE_TYPE9 = 65;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE9__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE9__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type9</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE9_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType10Impl <em>Type Type10</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType10Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType10()
	 * @generated
	 */
	int TYPE_TYPE10 = 66;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE10__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE10__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type10</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE10_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType11Impl <em>Type Type11</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType11Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType11()
	 * @generated
	 */
	int TYPE_TYPE11 = 67;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE11__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE11__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type11</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE11_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType12Impl <em>Type Type12</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType12Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType12()
	 * @generated
	 */
	int TYPE_TYPE12 = 68;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE12__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE12__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type12</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE12_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType13Impl <em>Type Type13</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType13Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType13()
	 * @generated
	 */
	int TYPE_TYPE13 = 69;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE13__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE13__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type13</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE13_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType14Impl <em>Type Type14</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType14Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType14()
	 * @generated
	 */
	int TYPE_TYPE14 = 70;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE14__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE14__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type14</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE14_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType15Impl <em>Type Type15</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType15Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType15()
	 * @generated
	 */
	int TYPE_TYPE15 = 71;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE15__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE15__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type15</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE15_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType16Impl <em>Type Type16</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType16Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType16()
	 * @generated
	 */
	int TYPE_TYPE16 = 72;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE16__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE16__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type16</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE16_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType17Impl <em>Type Type17</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType17Impl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType17()
	 * @generated
	 */
	int TYPE_TYPE17 = 73;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE17__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE17__INFO = 1;

	/**
	 * The number of structural features of the '<em>Type Type17</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPE_TYPE17_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl <em>Value Max Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getValueMaxType()
	 * @generated
	 */
	int VALUE_MAX_TYPE = 74;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MAX_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MAX_TYPE__PERCENT = 1;

	/**
	 * The feature id for the '<em><b>Pixel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MAX_TYPE__PIXEL = 2;

	/**
	 * The number of structural features of the '<em>Value Max Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MAX_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMinTypeImpl <em>Value Min Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMinTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getValueMinType()
	 * @generated
	 */
	int VALUE_MIN_TYPE = 75;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MIN_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MIN_TYPE__PERCENT = 1;

	/**
	 * The feature id for the '<em><b>Pixel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MIN_TYPE__PIXEL = 2;

	/**
	 * The number of structural features of the '<em>Value Min Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_MIN_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueStepTypeImpl <em>Value Step Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueStepTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getValueStepType()
	 * @generated
	 */
	int VALUE_STEP_TYPE = 76;

	/**
	 * The feature id for the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_STEP_TYPE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_STEP_TYPE__PERCENT = 1;

	/**
	 * The feature id for the '<em><b>Pixel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_STEP_TYPE__PIXEL = 2;

	/**
	 * The number of structural features of the '<em>Value Step Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_STEP_TYPE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.WindowNameTypeImpl <em>Window Name Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.WindowNameTypeImpl
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getWindowNameType()
	 * @generated
	 */
	int WINDOW_NAME_TYPE = 77;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_NAME_TYPE__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_NAME_TYPE__INFO = 1;

	/**
	 * The number of structural features of the '<em>Window Name Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_NAME_TYPE_FEATURE_COUNT = 2;


	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType <em>Backprojection Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Backprojection Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType
	 * @generated
	 */
	EClass getBackprojectionType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getFilter <em>Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Filter</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getFilter()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_Filter();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getImageCentre <em>Image Centre</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Image Centre</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getImageCentre()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EAttribute getBackprojectionType_ImageCentre();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getClockwiseRotation <em>Clockwise Rotation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Clockwise Rotation</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getClockwiseRotation()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_ClockwiseRotation();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getTilt <em>Tilt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Tilt</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getTilt()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_Tilt();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCoordinateSystem <em>Coordinate System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Coordinate System</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCoordinateSystem()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_CoordinateSystem();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCircles <em>Circles</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Circles</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCircles()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_Circles();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getROI <em>ROI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>ROI</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getROI()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_ROI();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getPolarCartesianInterpolation <em>Polar Cartesian Interpolation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Polar Cartesian Interpolation</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getPolarCartesianInterpolation()
	 * @see #getBackprojectionType()
	 * @generated
	 */
	EReference getBackprojectionType_PolarCartesianInterpolation();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType <em>Beamline User Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Beamline User Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType
	 * @generated
	 */
	EClass getBeamlineUserType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getType()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EReference getBeamlineUserType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getBeamlineName <em>Beamline Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Beamline Name</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getBeamlineName()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_BeamlineName();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getYear <em>Year</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Year</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getYear()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_Year();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getMonth <em>Month</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Month</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getMonth()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_Month();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDate()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_Date();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getVisitNumber <em>Visit Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visit Number</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getVisitNumber()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_VisitNumber();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputDataFolder <em>Input Data Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input Data Folder</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputDataFolder()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_InputDataFolder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputScanFolder <em>Input Scan Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input Scan Folder</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputScanFolder()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_InputScanFolder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputDataFolder <em>Output Data Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Output Data Folder</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputDataFolder()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_OutputDataFolder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputScanFolder <em>Output Scan Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Output Scan Folder</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputScanFolder()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_OutputScanFolder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDone()
	 * @see #getBeamlineUserType()
	 * @generated
	 */
	EAttribute getBeamlineUserType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType <em>Bits Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bits Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType
	 * @generated
	 */
	EClass getBitsTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType#getValue()
	 * @see #getBitsTypeType()
	 * @generated
	 */
	EAttribute getBitsTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType#getInfo()
	 * @see #getBitsTypeType()
	 * @generated
	 */
	EAttribute getBitsTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType <em>Byte Order Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Byte Order Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType
	 * @generated
	 */
	EClass getByteOrderType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType#getValue()
	 * @see #getByteOrderType()
	 * @generated
	 */
	EAttribute getByteOrderType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType#getInfo()
	 * @see #getByteOrderType()
	 * @generated
	 */
	EAttribute getByteOrderType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType <em>Circles Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Circles Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType
	 * @generated
	 */
	EClass getCirclesType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMin <em>Value Min</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value Min</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMin()
	 * @see #getCirclesType()
	 * @generated
	 */
	EReference getCirclesType_ValueMin();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMax <em>Value Max</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value Max</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMax()
	 * @see #getCirclesType()
	 * @generated
	 */
	EReference getCirclesType_ValueMax();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueStep <em>Value Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value Step</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueStep()
	 * @see #getCirclesType()
	 * @generated
	 */
	EReference getCirclesType_ValueStep();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getComm <em>Comm</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comm</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getComm()
	 * @see #getCirclesType()
	 * @generated
	 */
	EAttribute getCirclesType_Comm();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType <em>Clockwise Rotation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Clockwise Rotation Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType
	 * @generated
	 */
	EClass getClockwiseRotationType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType#getValue()
	 * @see #getClockwiseRotationType()
	 * @generated
	 */
	EAttribute getClockwiseRotationType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType#getDone()
	 * @see #getClockwiseRotationType()
	 * @generated
	 */
	EAttribute getClockwiseRotationType_Done();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType#getInfo()
	 * @see #getClockwiseRotationType()
	 * @generated
	 */
	EAttribute getClockwiseRotationType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType <em>Coordinate System Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Coordinate System Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType
	 * @generated
	 */
	EClass getCoordinateSystemType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType#getType()
	 * @see #getCoordinateSystemType()
	 * @generated
	 */
	EReference getCoordinateSystemType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType#getSlice <em>Slice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Slice</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType#getSlice()
	 * @see #getCoordinateSystemType()
	 * @generated
	 */
	EAttribute getCoordinateSystemType_Slice();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType#getDone()
	 * @see #getCoordinateSystemType()
	 * @generated
	 */
	EAttribute getCoordinateSystemType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType <em>Dark Field Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Dark Field Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType
	 * @generated
	 */
	EClass getDarkFieldType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getType()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EReference getDarkFieldType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueBefore <em>Value Before</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Before</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueBefore()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EAttribute getDarkFieldType_ValueBefore();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueAfter <em>Value After</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value After</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueAfter()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EAttribute getDarkFieldType_ValueAfter();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileBefore <em>File Before</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Before</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileBefore()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EAttribute getDarkFieldType_FileBefore();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileAfter <em>File After</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File After</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileAfter()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EAttribute getDarkFieldType_FileAfter();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getProfileType <em>Profile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Profile Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getProfileType()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EReference getDarkFieldType_ProfileType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileProfile <em>File Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Profile</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileProfile()
	 * @see #getDarkFieldType()
	 * @generated
	 */
	EAttribute getDarkFieldType_FileProfile();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType <em>Default Xml Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Default Xml Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType
	 * @generated
	 */
	EClass getDefaultXmlType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType#getValue()
	 * @see #getDefaultXmlType()
	 * @generated
	 */
	EAttribute getDefaultXmlType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType#getDone()
	 * @see #getDefaultXmlType()
	 * @generated
	 */
	EAttribute getDefaultXmlType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot
	 * @generated
	 */
	EClass getDocumentRoot();

	/**
	 * Returns the meta object for the attribute list '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getMixed <em>Mixed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getMixed()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EAttribute getDocumentRoot_Mixed();

	/**
	 * Returns the meta object for the map '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getXMLNSPrefixMap()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_XMLNSPrefixMap();

	/**
	 * Returns the meta object for the map '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XSI Schema Location</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getXSISchemaLocation()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_XSISchemaLocation();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getHMxml <em>HMxml</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>HMxml</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot#getHMxml()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_HMxml();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType <em>Extrapolation Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Extrapolation Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType
	 * @generated
	 */
	EClass getExtrapolationTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType#getValue()
	 * @see #getExtrapolationTypeType()
	 * @generated
	 */
	EAttribute getExtrapolationTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType#getInfo()
	 * @see #getExtrapolationTypeType()
	 * @generated
	 */
	EAttribute getExtrapolationTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType <em>FBP Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>FBP Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType
	 * @generated
	 */
	EClass getFBPType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getDefaultXml <em>Default Xml</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Default Xml</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getDefaultXml()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_DefaultXml();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getGPUDeviceNumber <em>GPU Device Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>GPU Device Number</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getGPUDeviceNumber()
	 * @see #getFBPType()
	 * @generated
	 */
	EAttribute getFBPType_GPUDeviceNumber();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBeamlineUser <em>Beamline User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Beamline User</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBeamlineUser()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_BeamlineUser();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getLogFile <em>Log File</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Log File</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getLogFile()
	 * @see #getFBPType()
	 * @generated
	 */
	EAttribute getFBPType_LogFile();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getInputData <em>Input Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Input Data</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getInputData()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_InputData();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getFlatDarkFields <em>Flat Dark Fields</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Flat Dark Fields</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getFlatDarkFields()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_FlatDarkFields();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getPreprocessing <em>Preprocessing</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Preprocessing</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getPreprocessing()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_Preprocessing();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getTransform <em>Transform</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Transform</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getTransform()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_Transform();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBackprojection <em>Backprojection</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Backprojection</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBackprojection()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_Backprojection();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getOutputData <em>Output Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Output Data</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getOutputData()
	 * @see #getFBPType()
	 * @generated
	 */
	EReference getFBPType_OutputData();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType <em>Filter Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Filter Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType
	 * @generated
	 */
	EClass getFilterType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getType()
	 * @see #getFilterType()
	 * @generated
	 */
	EReference getFilterType_Type();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Name</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getName()
	 * @see #getFilterType()
	 * @generated
	 */
	EReference getFilterType_Name();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getBandwidth <em>Bandwidth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bandwidth</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getBandwidth()
	 * @see #getFilterType()
	 * @generated
	 */
	EAttribute getFilterType_Bandwidth();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getWindowName <em>Window Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Window Name</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getWindowName()
	 * @see #getFilterType()
	 * @generated
	 */
	EReference getFilterType_WindowName();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getNormalisation <em>Normalisation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Normalisation</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getNormalisation()
	 * @see #getFilterType()
	 * @generated
	 */
	EReference getFilterType_Normalisation();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getPixelSize <em>Pixel Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixel Size</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getPixelSize()
	 * @see #getFilterType()
	 * @generated
	 */
	EAttribute getFilterType_PixelSize();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType <em>First Image Index Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>First Image Index Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType
	 * @generated
	 */
	EClass getFirstImageIndexType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType#getValue()
	 * @see #getFirstImageIndexType()
	 * @generated
	 */
	EAttribute getFirstImageIndexType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType#getInfo()
	 * @see #getFirstImageIndexType()
	 * @generated
	 */
	EAttribute getFirstImageIndexType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType <em>Flat Dark Fields Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Flat Dark Fields Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType
	 * @generated
	 */
	EClass getFlatDarkFieldsType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getFlatField <em>Flat Field</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Flat Field</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getFlatField()
	 * @see #getFlatDarkFieldsType()
	 * @generated
	 */
	EReference getFlatDarkFieldsType_FlatField();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getDarkField <em>Dark Field</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Dark Field</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getDarkField()
	 * @see #getFlatDarkFieldsType()
	 * @generated
	 */
	EReference getFlatDarkFieldsType_DarkField();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType <em>Flat Field Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Flat Field Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType
	 * @generated
	 */
	EClass getFlatFieldType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getType()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EReference getFlatFieldType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueBefore <em>Value Before</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Before</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueBefore()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EAttribute getFlatFieldType_ValueBefore();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueAfter <em>Value After</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value After</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueAfter()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EAttribute getFlatFieldType_ValueAfter();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileBefore <em>File Before</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Before</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileBefore()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EAttribute getFlatFieldType_FileBefore();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileAfter <em>File After</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File After</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileAfter()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EAttribute getFlatFieldType_FileAfter();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getProfileType <em>Profile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Profile Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getProfileType()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EReference getFlatFieldType_ProfileType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileProfile <em>File Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Profile</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileProfile()
	 * @see #getFlatFieldType()
	 * @generated
	 */
	EAttribute getFlatFieldType_FileProfile();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType <em>Gap Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Gap Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType
	 * @generated
	 */
	EClass getGapType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType#getValue()
	 * @see #getGapType()
	 * @generated
	 */
	EAttribute getGapType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType#getInfo()
	 * @see #getGapType()
	 * @generated
	 */
	EAttribute getGapType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType <em>High Peaks After Columns Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>High Peaks After Columns Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType
	 * @generated
	 */
	EClass getHighPeaksAfterColumnsType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getType()
	 * @see #getHighPeaksAfterColumnsType()
	 * @generated
	 */
	EReference getHighPeaksAfterColumnsType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getNumberPixels <em>Number Pixels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number Pixels</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getNumberPixels()
	 * @see #getHighPeaksAfterColumnsType()
	 * @generated
	 */
	EAttribute getHighPeaksAfterColumnsType_NumberPixels();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getJump <em>Jump</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Jump</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getJump()
	 * @see #getHighPeaksAfterColumnsType()
	 * @generated
	 */
	EAttribute getHighPeaksAfterColumnsType_Jump();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType <em>High Peaks After Rows Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>High Peaks After Rows Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType
	 * @generated
	 */
	EClass getHighPeaksAfterRowsType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType#getType()
	 * @see #getHighPeaksAfterRowsType()
	 * @generated
	 */
	EReference getHighPeaksAfterRowsType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType#getNumberPixels <em>Number Pixels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number Pixels</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType#getNumberPixels()
	 * @see #getHighPeaksAfterRowsType()
	 * @generated
	 */
	EAttribute getHighPeaksAfterRowsType_NumberPixels();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType#getJump <em>Jump</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Jump</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType#getJump()
	 * @see #getHighPeaksAfterRowsType()
	 * @generated
	 */
	EAttribute getHighPeaksAfterRowsType_Jump();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType <em>High Peaks Before Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>High Peaks Before Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType
	 * @generated
	 */
	EClass getHighPeaksBeforeType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType#getType()
	 * @see #getHighPeaksBeforeType()
	 * @generated
	 */
	EReference getHighPeaksBeforeType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType#getNumberPixels <em>Number Pixels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number Pixels</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType#getNumberPixels()
	 * @see #getHighPeaksBeforeType()
	 * @generated
	 */
	EAttribute getHighPeaksBeforeType_NumberPixels();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType#getJump <em>Jump</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Jump</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType#getJump()
	 * @see #getHighPeaksBeforeType()
	 * @generated
	 */
	EAttribute getHighPeaksBeforeType_Jump();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType <em>HMxml Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>HMxml Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType
	 * @generated
	 */
	EClass getHMxmlType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType#getFBP <em>FBP</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>FBP</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType#getFBP()
	 * @see #getHMxmlType()
	 * @generated
	 */
	EReference getHMxmlType_FBP();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType <em>Image First Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Image First Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType
	 * @generated
	 */
	EClass getImageFirstType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType#getValue()
	 * @see #getImageFirstType()
	 * @generated
	 */
	EAttribute getImageFirstType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType#getDone()
	 * @see #getImageFirstType()
	 * @generated
	 */
	EAttribute getImageFirstType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType <em>Image Last Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Image Last Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType
	 * @generated
	 */
	EClass getImageLastType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType#getValue()
	 * @see #getImageLastType()
	 * @generated
	 */
	EAttribute getImageLastType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType#getDone()
	 * @see #getImageLastType()
	 * @generated
	 */
	EAttribute getImageLastType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType <em>Image Step Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Image Step Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType
	 * @generated
	 */
	EClass getImageStepType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType#getValue()
	 * @see #getImageStepType()
	 * @generated
	 */
	EAttribute getImageStepType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType#getDone()
	 * @see #getImageStepType()
	 * @generated
	 */
	EAttribute getImageStepType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType <em>Input Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input Data Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType
	 * @generated
	 */
	EClass getInputDataType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFolder <em>Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Folder</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFolder()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_Folder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPrefix <em>Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Prefix</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPrefix()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_Prefix();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getSuffix <em>Suffix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Suffix</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getSuffix()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_Suffix();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getExtension <em>Extension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Extension</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getExtension()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_Extension();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getNOD <em>NOD</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>NOD</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getNOD()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_NOD();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMax <em>Memory Size Max</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Memory Size Max</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMax()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_MemorySizeMax();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMin <em>Memory Size Min</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Memory Size Min</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMin()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_MemorySizeMin();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getOrientation <em>Orientation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Orientation</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getOrientation()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_Orientation();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileFirst <em>File First</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File First</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileFirst()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_FileFirst();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileLast <em>File Last</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Last</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileLast()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_FileLast();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileStep <em>File Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Step</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileStep()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_FileStep();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageFirst <em>Image First</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Image First</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageFirst()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_ImageFirst();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageLast <em>Image Last</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Image Last</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageLast()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_ImageLast();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageStep <em>Image Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Image Step</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageStep()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_ImageStep();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRaw <em>Raw</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Raw</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRaw()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_Raw();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFirstImageIndex <em>First Image Index</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>First Image Index</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFirstImageIndex()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_FirstImageIndex();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImagesPerFile <em>Images Per File</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Images Per File</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImagesPerFile()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_ImagesPerFile();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRestrictions <em>Restrictions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Restrictions</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRestrictions()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_Restrictions();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMin <em>Value Min</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Min</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMin()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_ValueMin();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMax <em>Value Max</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Max</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMax()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_ValueMax();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getType()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_Type();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getShape <em>Shape</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Shape</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getShape()
	 * @see #getInputDataType()
	 * @generated
	 */
	EReference getInputDataType_Shape();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPixelParam <em>Pixel Param</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixel Param</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPixelParam()
	 * @see #getInputDataType()
	 * @generated
	 */
	EAttribute getInputDataType_PixelParam();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType <em>Intensity Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Intensity Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType
	 * @generated
	 */
	EClass getIntensityType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getType()
	 * @see #getIntensityType()
	 * @generated
	 */
	EReference getIntensityType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnLeft <em>Column Left</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Column Left</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnLeft()
	 * @see #getIntensityType()
	 * @generated
	 */
	EAttribute getIntensityType_ColumnLeft();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnRight <em>Column Right</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Column Right</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnRight()
	 * @see #getIntensityType()
	 * @generated
	 */
	EAttribute getIntensityType_ColumnRight();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroLeft <em>Zero Left</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Zero Left</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroLeft()
	 * @see #getIntensityType()
	 * @generated
	 */
	EAttribute getIntensityType_ZeroLeft();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroRight <em>Zero Right</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Zero Right</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroRight()
	 * @see #getIntensityType()
	 * @generated
	 */
	EAttribute getIntensityType_ZeroRight();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType <em>Interpolation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Interpolation Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType
	 * @generated
	 */
	EClass getInterpolationType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType#getValue()
	 * @see #getInterpolationType()
	 * @generated
	 */
	EAttribute getInterpolationType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType#getInfo()
	 * @see #getInterpolationType()
	 * @generated
	 */
	EAttribute getInterpolationType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType <em>Memory Size Max Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Memory Size Max Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType
	 * @generated
	 */
	EClass getMemorySizeMaxType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType#getValue()
	 * @see #getMemorySizeMaxType()
	 * @generated
	 */
	EAttribute getMemorySizeMaxType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType#getInfo()
	 * @see #getMemorySizeMaxType()
	 * @generated
	 */
	EAttribute getMemorySizeMaxType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType <em>Memory Size Min Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Memory Size Min Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType
	 * @generated
	 */
	EClass getMemorySizeMinType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType#getValue()
	 * @see #getMemorySizeMinType()
	 * @generated
	 */
	EAttribute getMemorySizeMinType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType#getInfo()
	 * @see #getMemorySizeMinType()
	 * @generated
	 */
	EAttribute getMemorySizeMinType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType <em>Missed Projections Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Missed Projections Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType
	 * @generated
	 */
	EClass getMissedProjectionsType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getValue()
	 * @see #getMissedProjectionsType()
	 * @generated
	 */
	EAttribute getMissedProjectionsType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getInfo()
	 * @see #getMissedProjectionsType()
	 * @generated
	 */
	EAttribute getMissedProjectionsType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType <em>Missed Projections Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Missed Projections Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType
	 * @generated
	 */
	EClass getMissedProjectionsTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType#getValue()
	 * @see #getMissedProjectionsTypeType()
	 * @generated
	 */
	EAttribute getMissedProjectionsTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType#getInfo()
	 * @see #getMissedProjectionsTypeType()
	 * @generated
	 */
	EAttribute getMissedProjectionsTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType <em>Name Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Name Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType
	 * @generated
	 */
	EClass getNameType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType#getValue()
	 * @see #getNameType()
	 * @generated
	 */
	EAttribute getNameType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType#getInfo()
	 * @see #getNameType()
	 * @generated
	 */
	EAttribute getNameType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType <em>NOD Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>NOD Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType
	 * @generated
	 */
	EClass getNODType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType#getValue()
	 * @see #getNODType()
	 * @generated
	 */
	EAttribute getNODType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType#getInfo()
	 * @see #getNODType()
	 * @generated
	 */
	EAttribute getNODType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType <em>Normalisation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Normalisation Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType
	 * @generated
	 */
	EClass getNormalisationType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType#getValue()
	 * @see #getNormalisationType()
	 * @generated
	 */
	EAttribute getNormalisationType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType#getInfo()
	 * @see #getNormalisationType()
	 * @generated
	 */
	EAttribute getNormalisationType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType <em>Num Series Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Num Series Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType
	 * @generated
	 */
	EClass getNumSeriesType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType#getValue()
	 * @see #getNumSeriesType()
	 * @generated
	 */
	EAttribute getNumSeriesType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType#getInfo()
	 * @see #getNumSeriesType()
	 * @generated
	 */
	EAttribute getNumSeriesType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType <em>Offset Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Offset Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType
	 * @generated
	 */
	EClass getOffsetType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType#getValue()
	 * @see #getOffsetType()
	 * @generated
	 */
	EAttribute getOffsetType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType#getInfo()
	 * @see #getOffsetType()
	 * @generated
	 */
	EAttribute getOffsetType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType <em>Orientation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Orientation Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType
	 * @generated
	 */
	EClass getOrientationType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType#getValue()
	 * @see #getOrientationType()
	 * @generated
	 */
	EAttribute getOrientationType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType#getDone()
	 * @see #getOrientationType()
	 * @generated
	 */
	EAttribute getOrientationType_Done();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType#getInfo()
	 * @see #getOrientationType()
	 * @generated
	 */
	EAttribute getOrientationType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType <em>Output Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Output Data Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType
	 * @generated
	 */
	EClass getOutputDataType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getType()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EReference getOutputDataType_Type();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getState <em>State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>State</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getState()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EReference getOutputDataType_State();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFolder <em>Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Folder</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFolder()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_Folder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getPrefix <em>Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Prefix</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getPrefix()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_Prefix();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getSuffix <em>Suffix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Suffix</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getSuffix()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_Suffix();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getExtension <em>Extension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Extension</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getExtension()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_Extension();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getNOD <em>NOD</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>NOD</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getNOD()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_NOD();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileFirst <em>File First</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File First</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileFirst()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_FileFirst();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileStep <em>File Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Step</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileStep()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_FileStep();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBitsType <em>Bits Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Bits Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBitsType()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EReference getOutputDataType_BitsType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBits <em>Bits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bits</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBits()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_Bits();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getRestrictions <em>Restrictions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Restrictions</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getRestrictions()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EReference getOutputDataType_Restrictions();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMin <em>Value Min</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Min</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMin()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_ValueMin();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMax <em>Value Max</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Max</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMax()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EAttribute getOutputDataType_ValueMax();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getShape <em>Shape</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Shape</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getShape()
	 * @see #getOutputDataType()
	 * @generated
	 */
	EReference getOutputDataType_Shape();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType <em>Output Width Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Output Width Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType
	 * @generated
	 */
	EClass getOutputWidthTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType#getValue()
	 * @see #getOutputWidthTypeType()
	 * @generated
	 */
	EAttribute getOutputWidthTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType#getInfo()
	 * @see #getOutputWidthTypeType()
	 * @generated
	 */
	EAttribute getOutputWidthTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType <em>Polar Cartesian Interpolation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Polar Cartesian Interpolation Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType
	 * @generated
	 */
	EClass getPolarCartesianInterpolationType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType#getValue()
	 * @see #getPolarCartesianInterpolationType()
	 * @generated
	 */
	EAttribute getPolarCartesianInterpolationType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType#getDone()
	 * @see #getPolarCartesianInterpolationType()
	 * @generated
	 */
	EAttribute getPolarCartesianInterpolationType_Done();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType#getInfo()
	 * @see #getPolarCartesianInterpolationType()
	 * @generated
	 */
	EAttribute getPolarCartesianInterpolationType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType <em>Preprocessing Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Preprocessing Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType
	 * @generated
	 */
	EClass getPreprocessingType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksBefore <em>High Peaks Before</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>High Peaks Before</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksBefore()
	 * @see #getPreprocessingType()
	 * @generated
	 */
	EReference getPreprocessingType_HighPeaksBefore();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getRingArtefacts <em>Ring Artefacts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ring Artefacts</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getRingArtefacts()
	 * @see #getPreprocessingType()
	 * @generated
	 */
	EReference getPreprocessingType_RingArtefacts();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getIntensity <em>Intensity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Intensity</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getIntensity()
	 * @see #getPreprocessingType()
	 * @generated
	 */
	EReference getPreprocessingType_Intensity();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterRows <em>High Peaks After Rows</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>High Peaks After Rows</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterRows()
	 * @see #getPreprocessingType()
	 * @generated
	 */
	EReference getPreprocessingType_HighPeaksAfterRows();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterColumns <em>High Peaks After Columns</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>High Peaks After Columns</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterColumns()
	 * @see #getPreprocessingType()
	 * @generated
	 */
	EReference getPreprocessingType_HighPeaksAfterColumns();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType <em>Profile Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Profile Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType
	 * @generated
	 */
	EClass getProfileTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType#getValue()
	 * @see #getProfileTypeType()
	 * @generated
	 */
	EAttribute getProfileTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType#getInfo()
	 * @see #getProfileTypeType()
	 * @generated
	 */
	EAttribute getProfileTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1 <em>Profile Type Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Profile Type Type1</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1
	 * @generated
	 */
	EClass getProfileTypeType1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1#getValue()
	 * @see #getProfileTypeType1()
	 * @generated
	 */
	EAttribute getProfileTypeType1_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1#getInfo()
	 * @see #getProfileTypeType1()
	 * @generated
	 */
	EAttribute getProfileTypeType1_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType <em>Raw Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Raw Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType
	 * @generated
	 */
	EClass getRawType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getType()
	 * @see #getRawType()
	 * @generated
	 */
	EReference getRawType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getBits <em>Bits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bits</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getBits()
	 * @see #getRawType()
	 * @generated
	 */
	EAttribute getRawType_Bits();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getOffset <em>Offset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Offset</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getOffset()
	 * @see #getRawType()
	 * @generated
	 */
	EReference getRawType_Offset();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getByteOrder <em>Byte Order</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Byte Order</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getByteOrder()
	 * @see #getRawType()
	 * @generated
	 */
	EReference getRawType_ByteOrder();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getXlen <em>Xlen</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Xlen</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getXlen()
	 * @see #getRawType()
	 * @generated
	 */
	EAttribute getRawType_Xlen();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getYlen <em>Ylen</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ylen</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getYlen()
	 * @see #getRawType()
	 * @generated
	 */
	EAttribute getRawType_Ylen();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getZlen <em>Zlen</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Zlen</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getZlen()
	 * @see #getRawType()
	 * @generated
	 */
	EAttribute getRawType_Zlen();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getGap <em>Gap</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Gap</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getGap()
	 * @see #getRawType()
	 * @generated
	 */
	EReference getRawType_Gap();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getDone()
	 * @see #getRawType()
	 * @generated
	 */
	EAttribute getRawType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType <em>Restrictions Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Restrictions Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType
	 * @generated
	 */
	EClass getRestrictionsType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType#getValue()
	 * @see #getRestrictionsType()
	 * @generated
	 */
	EAttribute getRestrictionsType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType#getInfo()
	 * @see #getRestrictionsType()
	 * @generated
	 */
	EAttribute getRestrictionsType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1 <em>Restrictions Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Restrictions Type1</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1
	 * @generated
	 */
	EClass getRestrictionsType1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1#getValue()
	 * @see #getRestrictionsType1()
	 * @generated
	 */
	EAttribute getRestrictionsType1_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1#getInfo()
	 * @see #getRestrictionsType1()
	 * @generated
	 */
	EAttribute getRestrictionsType1_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType <em>Ring Artefacts Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ring Artefacts Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType
	 * @generated
	 */
	EClass getRingArtefactsType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getType()
	 * @see #getRingArtefactsType()
	 * @generated
	 */
	EReference getRingArtefactsType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterN <em>Parameter N</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parameter N</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterN()
	 * @see #getRingArtefactsType()
	 * @generated
	 */
	EAttribute getRingArtefactsType_ParameterN();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterR <em>Parameter R</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parameter R</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterR()
	 * @see #getRingArtefactsType()
	 * @generated
	 */
	EAttribute getRingArtefactsType_ParameterR();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getNumSeries <em>Num Series</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Num Series</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getNumSeries()
	 * @see #getRingArtefactsType()
	 * @generated
	 */
	EReference getRingArtefactsType_NumSeries();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType <em>ROI Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ROI Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType
	 * @generated
	 */
	EClass getROIType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getType()
	 * @see #getROIType()
	 * @generated
	 */
	EReference getROIType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmin <em>Xmin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Xmin</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmin()
	 * @see #getROIType()
	 * @generated
	 */
	EAttribute getROIType_Xmin();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmax <em>Xmax</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Xmax</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmax()
	 * @see #getROIType()
	 * @generated
	 */
	EAttribute getROIType_Xmax();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmin <em>Ymin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ymin</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmin()
	 * @see #getROIType()
	 * @generated
	 */
	EAttribute getROIType_Ymin();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmax <em>Ymax</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ymax</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmax()
	 * @see #getROIType()
	 * @generated
	 */
	EAttribute getROIType_Ymax();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidthType <em>Output Width Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Output Width Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidthType()
	 * @see #getROIType()
	 * @generated
	 */
	EReference getROIType_OutputWidthType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidth <em>Output Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Output Width</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidth()
	 * @see #getROIType()
	 * @generated
	 */
	EAttribute getROIType_OutputWidth();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getAngle <em>Angle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Angle</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getAngle()
	 * @see #getROIType()
	 * @generated
	 */
	EAttribute getROIType_Angle();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType <em>Rotation Angle End Points Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rotation Angle End Points Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType
	 * @generated
	 */
	EClass getRotationAngleEndPointsType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType#getValue()
	 * @see #getRotationAngleEndPointsType()
	 * @generated
	 */
	EAttribute getRotationAngleEndPointsType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType#getInfo()
	 * @see #getRotationAngleEndPointsType()
	 * @generated
	 */
	EAttribute getRotationAngleEndPointsType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType <em>Rotation Angle Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rotation Angle Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType
	 * @generated
	 */
	EClass getRotationAngleTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType#getValue()
	 * @see #getRotationAngleTypeType()
	 * @generated
	 */
	EAttribute getRotationAngleTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType#getInfo()
	 * @see #getRotationAngleTypeType()
	 * @generated
	 */
	EAttribute getRotationAngleTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType <em>Scale Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Scale Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType
	 * @generated
	 */
	EClass getScaleTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType#getValue()
	 * @see #getScaleTypeType()
	 * @generated
	 */
	EAttribute getScaleTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType#getInfo()
	 * @see #getScaleTypeType()
	 * @generated
	 */
	EAttribute getScaleTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType <em>Shape Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shape Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType
	 * @generated
	 */
	EClass getShapeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType#getValue()
	 * @see #getShapeType()
	 * @generated
	 */
	EAttribute getShapeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType#getDone()
	 * @see #getShapeType()
	 * @generated
	 */
	EAttribute getShapeType_Done();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType#getInfo()
	 * @see #getShapeType()
	 * @generated
	 */
	EAttribute getShapeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1 <em>Shape Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Shape Type1</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1
	 * @generated
	 */
	EClass getShapeType1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1#getValue()
	 * @see #getShapeType1()
	 * @generated
	 */
	EAttribute getShapeType1_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1#getInfo()
	 * @see #getShapeType1()
	 * @generated
	 */
	EAttribute getShapeType1_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType <em>State Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>State Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType
	 * @generated
	 */
	EClass getStateType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType#getValue()
	 * @see #getStateType()
	 * @generated
	 */
	EAttribute getStateType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType#getInfo()
	 * @see #getStateType()
	 * @generated
	 */
	EAttribute getStateType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType <em>Tilt Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tilt Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType
	 * @generated
	 */
	EClass getTiltType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getType()
	 * @see #getTiltType()
	 * @generated
	 */
	EReference getTiltType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getXTilt <em>XTilt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>XTilt</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getXTilt()
	 * @see #getTiltType()
	 * @generated
	 */
	EAttribute getTiltType_XTilt();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getZTilt <em>ZTilt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>ZTilt</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getZTilt()
	 * @see #getTiltType()
	 * @generated
	 */
	EAttribute getTiltType_ZTilt();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getDone <em>Done</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Done</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getDone()
	 * @see #getTiltType()
	 * @generated
	 */
	EAttribute getTiltType_Done();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType <em>Transform Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Transform Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType
	 * @generated
	 */
	EClass getTransformType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjections <em>Missed Projections</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Missed Projections</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjections()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_MissedProjections();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjectionsType <em>Missed Projections Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Missed Projections Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjectionsType()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_MissedProjectionsType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleType <em>Rotation Angle Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Rotation Angle Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleType()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_RotationAngleType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngle <em>Rotation Angle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Rotation Angle</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngle()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_RotationAngle();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleEndPoints <em>Rotation Angle End Points</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Rotation Angle End Points</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleEndPoints()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_RotationAngleEndPoints();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreAngle <em>Re Centre Angle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Re Centre Angle</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreAngle()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_ReCentreAngle();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreRadius <em>Re Centre Radius</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Re Centre Radius</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreRadius()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_ReCentreRadius();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropTop <em>Crop Top</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Crop Top</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropTop()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_CropTop();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropBottom <em>Crop Bottom</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Crop Bottom</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropBottom()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_CropBottom();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropLeft <em>Crop Left</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Crop Left</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropLeft()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_CropLeft();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropRight <em>Crop Right</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Crop Right</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropRight()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_CropRight();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleType <em>Scale Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Scale Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleType()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_ScaleType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleWidth <em>Scale Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scale Width</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleWidth()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_ScaleWidth();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleHeight <em>Scale Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scale Height</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleHeight()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_ScaleHeight();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationType <em>Extrapolation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Extrapolation Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationType()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_ExtrapolationType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationPixels <em>Extrapolation Pixels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Extrapolation Pixels</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationPixels()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_ExtrapolationPixels();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationWidth <em>Extrapolation Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Extrapolation Width</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationWidth()
	 * @see #getTransformType()
	 * @generated
	 */
	EAttribute getTransformType_ExtrapolationWidth();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getInterpolation <em>Interpolation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Interpolation</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getInterpolation()
	 * @see #getTransformType()
	 * @generated
	 */
	EReference getTransformType_Interpolation();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType <em>Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType
	 * @generated
	 */
	EClass getTypeType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType#getValue()
	 * @see #getTypeType()
	 * @generated
	 */
	EAttribute getTypeType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType#getInfo()
	 * @see #getTypeType()
	 * @generated
	 */
	EAttribute getTypeType_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1 <em>Type Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type1</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1
	 * @generated
	 */
	EClass getTypeType1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1#getValue()
	 * @see #getTypeType1()
	 * @generated
	 */
	EAttribute getTypeType1_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1#getInfo()
	 * @see #getTypeType1()
	 * @generated
	 */
	EAttribute getTypeType1_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2 <em>Type Type2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type2</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2
	 * @generated
	 */
	EClass getTypeType2();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2#getValue()
	 * @see #getTypeType2()
	 * @generated
	 */
	EAttribute getTypeType2_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2#getInfo()
	 * @see #getTypeType2()
	 * @generated
	 */
	EAttribute getTypeType2_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3 <em>Type Type3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type3</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3
	 * @generated
	 */
	EClass getTypeType3();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3#getValue()
	 * @see #getTypeType3()
	 * @generated
	 */
	EAttribute getTypeType3_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3#getInfo()
	 * @see #getTypeType3()
	 * @generated
	 */
	EAttribute getTypeType3_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4 <em>Type Type4</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type4</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4
	 * @generated
	 */
	EClass getTypeType4();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4#getValue()
	 * @see #getTypeType4()
	 * @generated
	 */
	EAttribute getTypeType4_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4#getInfo()
	 * @see #getTypeType4()
	 * @generated
	 */
	EAttribute getTypeType4_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5 <em>Type Type5</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type5</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5
	 * @generated
	 */
	EClass getTypeType5();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5#getValue()
	 * @see #getTypeType5()
	 * @generated
	 */
	EAttribute getTypeType5_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5#getInfo()
	 * @see #getTypeType5()
	 * @generated
	 */
	EAttribute getTypeType5_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6 <em>Type Type6</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type6</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6
	 * @generated
	 */
	EClass getTypeType6();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6#getValue()
	 * @see #getTypeType6()
	 * @generated
	 */
	EAttribute getTypeType6_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6#getInfo()
	 * @see #getTypeType6()
	 * @generated
	 */
	EAttribute getTypeType6_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7 <em>Type Type7</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type7</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7
	 * @generated
	 */
	EClass getTypeType7();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7#getValue()
	 * @see #getTypeType7()
	 * @generated
	 */
	EAttribute getTypeType7_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7#getInfo()
	 * @see #getTypeType7()
	 * @generated
	 */
	EAttribute getTypeType7_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8 <em>Type Type8</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type8</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8
	 * @generated
	 */
	EClass getTypeType8();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8#getValue()
	 * @see #getTypeType8()
	 * @generated
	 */
	EAttribute getTypeType8_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8#getInfo()
	 * @see #getTypeType8()
	 * @generated
	 */
	EAttribute getTypeType8_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9 <em>Type Type9</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type9</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9
	 * @generated
	 */
	EClass getTypeType9();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9#getValue()
	 * @see #getTypeType9()
	 * @generated
	 */
	EAttribute getTypeType9_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9#getInfo()
	 * @see #getTypeType9()
	 * @generated
	 */
	EAttribute getTypeType9_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10 <em>Type Type10</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type10</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10
	 * @generated
	 */
	EClass getTypeType10();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10#getValue()
	 * @see #getTypeType10()
	 * @generated
	 */
	EAttribute getTypeType10_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10#getInfo()
	 * @see #getTypeType10()
	 * @generated
	 */
	EAttribute getTypeType10_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11 <em>Type Type11</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type11</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11
	 * @generated
	 */
	EClass getTypeType11();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11#getValue()
	 * @see #getTypeType11()
	 * @generated
	 */
	EAttribute getTypeType11_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11#getInfo()
	 * @see #getTypeType11()
	 * @generated
	 */
	EAttribute getTypeType11_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12 <em>Type Type12</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type12</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12
	 * @generated
	 */
	EClass getTypeType12();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12#getValue()
	 * @see #getTypeType12()
	 * @generated
	 */
	EAttribute getTypeType12_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12#getInfo()
	 * @see #getTypeType12()
	 * @generated
	 */
	EAttribute getTypeType12_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13 <em>Type Type13</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type13</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13
	 * @generated
	 */
	EClass getTypeType13();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13#getValue()
	 * @see #getTypeType13()
	 * @generated
	 */
	EAttribute getTypeType13_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13#getInfo()
	 * @see #getTypeType13()
	 * @generated
	 */
	EAttribute getTypeType13_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14 <em>Type Type14</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type14</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14
	 * @generated
	 */
	EClass getTypeType14();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14#getValue()
	 * @see #getTypeType14()
	 * @generated
	 */
	EAttribute getTypeType14_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14#getInfo()
	 * @see #getTypeType14()
	 * @generated
	 */
	EAttribute getTypeType14_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15 <em>Type Type15</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type15</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15
	 * @generated
	 */
	EClass getTypeType15();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15#getValue()
	 * @see #getTypeType15()
	 * @generated
	 */
	EAttribute getTypeType15_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15#getInfo()
	 * @see #getTypeType15()
	 * @generated
	 */
	EAttribute getTypeType15_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16 <em>Type Type16</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type16</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16
	 * @generated
	 */
	EClass getTypeType16();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16#getValue()
	 * @see #getTypeType16()
	 * @generated
	 */
	EAttribute getTypeType16_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16#getInfo()
	 * @see #getTypeType16()
	 * @generated
	 */
	EAttribute getTypeType16_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17 <em>Type Type17</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Type Type17</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17
	 * @generated
	 */
	EClass getTypeType17();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17#getValue()
	 * @see #getTypeType17()
	 * @generated
	 */
	EAttribute getTypeType17_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17#getInfo()
	 * @see #getTypeType17()
	 * @generated
	 */
	EAttribute getTypeType17_Info();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType <em>Value Max Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Value Max Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType
	 * @generated
	 */
	EClass getValueMaxType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType#getType()
	 * @see #getValueMaxType()
	 * @generated
	 */
	EReference getValueMaxType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType#getPercent <em>Percent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Percent</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType#getPercent()
	 * @see #getValueMaxType()
	 * @generated
	 */
	EAttribute getValueMaxType_Percent();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType#getPixel <em>Pixel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixel</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType#getPixel()
	 * @see #getValueMaxType()
	 * @generated
	 */
	EAttribute getValueMaxType_Pixel();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType <em>Value Min Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Value Min Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType
	 * @generated
	 */
	EClass getValueMinType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType#getType()
	 * @see #getValueMinType()
	 * @generated
	 */
	EReference getValueMinType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType#getPercent <em>Percent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Percent</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType#getPercent()
	 * @see #getValueMinType()
	 * @generated
	 */
	EAttribute getValueMinType_Percent();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType#getPixel <em>Pixel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixel</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType#getPixel()
	 * @see #getValueMinType()
	 * @generated
	 */
	EAttribute getValueMinType_Pixel();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType <em>Value Step Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Value Step Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType
	 * @generated
	 */
	EClass getValueStepType();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getType()
	 * @see #getValueStepType()
	 * @generated
	 */
	EReference getValueStepType_Type();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPercent <em>Percent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Percent</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPercent()
	 * @see #getValueStepType()
	 * @generated
	 */
	EAttribute getValueStepType_Percent();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPixel <em>Pixel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixel</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPixel()
	 * @see #getValueStepType()
	 * @generated
	 */
	EAttribute getValueStepType_Pixel();

	/**
	 * Returns the meta object for class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType <em>Window Name Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window Name Type</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType
	 * @generated
	 */
	EClass getWindowNameType();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType#getValue()
	 * @see #getWindowNameType()
	 * @generated
	 */
	EAttribute getWindowNameType_Value();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType#getInfo <em>Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Info</em>'.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType#getInfo()
	 * @see #getWindowNameType()
	 * @generated
	 */
	EAttribute getWindowNameType_Info();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	HmFactory getHmFactory();

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
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl <em>Backprojection Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getBackprojectionType()
		 * @generated
		 */
		EClass BACKPROJECTION_TYPE = eINSTANCE.getBackprojectionType();

		/**
		 * The meta object literal for the '<em><b>Filter</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__FILTER = eINSTANCE.getBackprojectionType_Filter();

		/**
		 * The meta object literal for the '<em><b>Image Centre</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BACKPROJECTION_TYPE__IMAGE_CENTRE = eINSTANCE.getBackprojectionType_ImageCentre();

		/**
		 * The meta object literal for the '<em><b>Clockwise Rotation</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__CLOCKWISE_ROTATION = eINSTANCE.getBackprojectionType_ClockwiseRotation();

		/**
		 * The meta object literal for the '<em><b>Tilt</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__TILT = eINSTANCE.getBackprojectionType_Tilt();

		/**
		 * The meta object literal for the '<em><b>Coordinate System</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__COORDINATE_SYSTEM = eINSTANCE.getBackprojectionType_CoordinateSystem();

		/**
		 * The meta object literal for the '<em><b>Circles</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__CIRCLES = eINSTANCE.getBackprojectionType_Circles();

		/**
		 * The meta object literal for the '<em><b>ROI</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__ROI = eINSTANCE.getBackprojectionType_ROI();

		/**
		 * The meta object literal for the '<em><b>Polar Cartesian Interpolation</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION = eINSTANCE.getBackprojectionType_PolarCartesianInterpolation();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl <em>Beamline User Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getBeamlineUserType()
		 * @generated
		 */
		EClass BEAMLINE_USER_TYPE = eINSTANCE.getBeamlineUserType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BEAMLINE_USER_TYPE__TYPE = eINSTANCE.getBeamlineUserType_Type();

		/**
		 * The meta object literal for the '<em><b>Beamline Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__BEAMLINE_NAME = eINSTANCE.getBeamlineUserType_BeamlineName();

		/**
		 * The meta object literal for the '<em><b>Year</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__YEAR = eINSTANCE.getBeamlineUserType_Year();

		/**
		 * The meta object literal for the '<em><b>Month</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__MONTH = eINSTANCE.getBeamlineUserType_Month();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__DATE = eINSTANCE.getBeamlineUserType_Date();

		/**
		 * The meta object literal for the '<em><b>Visit Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__VISIT_NUMBER = eINSTANCE.getBeamlineUserType_VisitNumber();

		/**
		 * The meta object literal for the '<em><b>Input Data Folder</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER = eINSTANCE.getBeamlineUserType_InputDataFolder();

		/**
		 * The meta object literal for the '<em><b>Input Scan Folder</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER = eINSTANCE.getBeamlineUserType_InputScanFolder();

		/**
		 * The meta object literal for the '<em><b>Output Data Folder</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER = eINSTANCE.getBeamlineUserType_OutputDataFolder();

		/**
		 * The meta object literal for the '<em><b>Output Scan Folder</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER = eINSTANCE.getBeamlineUserType_OutputScanFolder();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BEAMLINE_USER_TYPE__DONE = eINSTANCE.getBeamlineUserType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BitsTypeTypeImpl <em>Bits Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BitsTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getBitsTypeType()
		 * @generated
		 */
		EClass BITS_TYPE_TYPE = eINSTANCE.getBitsTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BITS_TYPE_TYPE__VALUE = eINSTANCE.getBitsTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BITS_TYPE_TYPE__INFO = eINSTANCE.getBitsTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ByteOrderTypeImpl <em>Byte Order Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ByteOrderTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getByteOrderType()
		 * @generated
		 */
		EClass BYTE_ORDER_TYPE = eINSTANCE.getByteOrderType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BYTE_ORDER_TYPE__VALUE = eINSTANCE.getByteOrderType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BYTE_ORDER_TYPE__INFO = eINSTANCE.getByteOrderType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl <em>Circles Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CirclesTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getCirclesType()
		 * @generated
		 */
		EClass CIRCLES_TYPE = eINSTANCE.getCirclesType();

		/**
		 * The meta object literal for the '<em><b>Value Min</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CIRCLES_TYPE__VALUE_MIN = eINSTANCE.getCirclesType_ValueMin();

		/**
		 * The meta object literal for the '<em><b>Value Max</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CIRCLES_TYPE__VALUE_MAX = eINSTANCE.getCirclesType_ValueMax();

		/**
		 * The meta object literal for the '<em><b>Value Step</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CIRCLES_TYPE__VALUE_STEP = eINSTANCE.getCirclesType_ValueStep();

		/**
		 * The meta object literal for the '<em><b>Comm</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CIRCLES_TYPE__COMM = eINSTANCE.getCirclesType_Comm();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ClockwiseRotationTypeImpl <em>Clockwise Rotation Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ClockwiseRotationTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getClockwiseRotationType()
		 * @generated
		 */
		EClass CLOCKWISE_ROTATION_TYPE = eINSTANCE.getClockwiseRotationType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOCKWISE_ROTATION_TYPE__VALUE = eINSTANCE.getClockwiseRotationType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOCKWISE_ROTATION_TYPE__DONE = eINSTANCE.getClockwiseRotationType_Done();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOCKWISE_ROTATION_TYPE__INFO = eINSTANCE.getClockwiseRotationType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl <em>Coordinate System Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.CoordinateSystemTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getCoordinateSystemType()
		 * @generated
		 */
		EClass COORDINATE_SYSTEM_TYPE = eINSTANCE.getCoordinateSystemType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COORDINATE_SYSTEM_TYPE__TYPE = eINSTANCE.getCoordinateSystemType_Type();

		/**
		 * The meta object literal for the '<em><b>Slice</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COORDINATE_SYSTEM_TYPE__SLICE = eINSTANCE.getCoordinateSystemType_Slice();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COORDINATE_SYSTEM_TYPE__DONE = eINSTANCE.getCoordinateSystemType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl <em>Dark Field Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DarkFieldTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getDarkFieldType()
		 * @generated
		 */
		EClass DARK_FIELD_TYPE = eINSTANCE.getDarkFieldType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DARK_FIELD_TYPE__TYPE = eINSTANCE.getDarkFieldType_Type();

		/**
		 * The meta object literal for the '<em><b>Value Before</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DARK_FIELD_TYPE__VALUE_BEFORE = eINSTANCE.getDarkFieldType_ValueBefore();

		/**
		 * The meta object literal for the '<em><b>Value After</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DARK_FIELD_TYPE__VALUE_AFTER = eINSTANCE.getDarkFieldType_ValueAfter();

		/**
		 * The meta object literal for the '<em><b>File Before</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DARK_FIELD_TYPE__FILE_BEFORE = eINSTANCE.getDarkFieldType_FileBefore();

		/**
		 * The meta object literal for the '<em><b>File After</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DARK_FIELD_TYPE__FILE_AFTER = eINSTANCE.getDarkFieldType_FileAfter();

		/**
		 * The meta object literal for the '<em><b>Profile Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DARK_FIELD_TYPE__PROFILE_TYPE = eINSTANCE.getDarkFieldType_ProfileType();

		/**
		 * The meta object literal for the '<em><b>File Profile</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DARK_FIELD_TYPE__FILE_PROFILE = eINSTANCE.getDarkFieldType_FileProfile();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DefaultXmlTypeImpl <em>Default Xml Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DefaultXmlTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getDefaultXmlType()
		 * @generated
		 */
		EClass DEFAULT_XML_TYPE = eINSTANCE.getDefaultXmlType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DEFAULT_XML_TYPE__VALUE = eINSTANCE.getDefaultXmlType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DEFAULT_XML_TYPE__DONE = eINSTANCE.getDefaultXmlType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.DocumentRootImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getDocumentRoot()
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
		 * The meta object literal for the '<em><b>HMxml</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__HMXML = eINSTANCE.getDocumentRoot_HMxml();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ExtrapolationTypeTypeImpl <em>Extrapolation Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ExtrapolationTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getExtrapolationTypeType()
		 * @generated
		 */
		EClass EXTRAPOLATION_TYPE_TYPE = eINSTANCE.getExtrapolationTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXTRAPOLATION_TYPE_TYPE__VALUE = eINSTANCE.getExtrapolationTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXTRAPOLATION_TYPE_TYPE__INFO = eINSTANCE.getExtrapolationTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl <em>FBP Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFBPType()
		 * @generated
		 */
		EClass FBP_TYPE = eINSTANCE.getFBPType();

		/**
		 * The meta object literal for the '<em><b>Default Xml</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__DEFAULT_XML = eINSTANCE.getFBPType_DefaultXml();

		/**
		 * The meta object literal for the '<em><b>GPU Device Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FBP_TYPE__GPU_DEVICE_NUMBER = eINSTANCE.getFBPType_GPUDeviceNumber();

		/**
		 * The meta object literal for the '<em><b>Beamline User</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__BEAMLINE_USER = eINSTANCE.getFBPType_BeamlineUser();

		/**
		 * The meta object literal for the '<em><b>Log File</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FBP_TYPE__LOG_FILE = eINSTANCE.getFBPType_LogFile();

		/**
		 * The meta object literal for the '<em><b>Input Data</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__INPUT_DATA = eINSTANCE.getFBPType_InputData();

		/**
		 * The meta object literal for the '<em><b>Flat Dark Fields</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__FLAT_DARK_FIELDS = eINSTANCE.getFBPType_FlatDarkFields();

		/**
		 * The meta object literal for the '<em><b>Preprocessing</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__PREPROCESSING = eINSTANCE.getFBPType_Preprocessing();

		/**
		 * The meta object literal for the '<em><b>Transform</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__TRANSFORM = eINSTANCE.getFBPType_Transform();

		/**
		 * The meta object literal for the '<em><b>Backprojection</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__BACKPROJECTION = eINSTANCE.getFBPType_Backprojection();

		/**
		 * The meta object literal for the '<em><b>Output Data</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FBP_TYPE__OUTPUT_DATA = eINSTANCE.getFBPType_OutputData();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl <em>Filter Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFilterType()
		 * @generated
		 */
		EClass FILTER_TYPE = eINSTANCE.getFilterType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILTER_TYPE__TYPE = eINSTANCE.getFilterType_Type();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILTER_TYPE__NAME = eINSTANCE.getFilterType_Name();

		/**
		 * The meta object literal for the '<em><b>Bandwidth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILTER_TYPE__BANDWIDTH = eINSTANCE.getFilterType_Bandwidth();

		/**
		 * The meta object literal for the '<em><b>Window Name</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILTER_TYPE__WINDOW_NAME = eINSTANCE.getFilterType_WindowName();

		/**
		 * The meta object literal for the '<em><b>Normalisation</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILTER_TYPE__NORMALISATION = eINSTANCE.getFilterType_Normalisation();

		/**
		 * The meta object literal for the '<em><b>Pixel Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILTER_TYPE__PIXEL_SIZE = eINSTANCE.getFilterType_PixelSize();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FirstImageIndexTypeImpl <em>First Image Index Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FirstImageIndexTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFirstImageIndexType()
		 * @generated
		 */
		EClass FIRST_IMAGE_INDEX_TYPE = eINSTANCE.getFirstImageIndexType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIRST_IMAGE_INDEX_TYPE__VALUE = eINSTANCE.getFirstImageIndexType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIRST_IMAGE_INDEX_TYPE__INFO = eINSTANCE.getFirstImageIndexType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatDarkFieldsTypeImpl <em>Flat Dark Fields Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatDarkFieldsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFlatDarkFieldsType()
		 * @generated
		 */
		EClass FLAT_DARK_FIELDS_TYPE = eINSTANCE.getFlatDarkFieldsType();

		/**
		 * The meta object literal for the '<em><b>Flat Field</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FLAT_DARK_FIELDS_TYPE__FLAT_FIELD = eINSTANCE.getFlatDarkFieldsType_FlatField();

		/**
		 * The meta object literal for the '<em><b>Dark Field</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FLAT_DARK_FIELDS_TYPE__DARK_FIELD = eINSTANCE.getFlatDarkFieldsType_DarkField();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl <em>Flat Field Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FlatFieldTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getFlatFieldType()
		 * @generated
		 */
		EClass FLAT_FIELD_TYPE = eINSTANCE.getFlatFieldType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FLAT_FIELD_TYPE__TYPE = eINSTANCE.getFlatFieldType_Type();

		/**
		 * The meta object literal for the '<em><b>Value Before</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FLAT_FIELD_TYPE__VALUE_BEFORE = eINSTANCE.getFlatFieldType_ValueBefore();

		/**
		 * The meta object literal for the '<em><b>Value After</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FLAT_FIELD_TYPE__VALUE_AFTER = eINSTANCE.getFlatFieldType_ValueAfter();

		/**
		 * The meta object literal for the '<em><b>File Before</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FLAT_FIELD_TYPE__FILE_BEFORE = eINSTANCE.getFlatFieldType_FileBefore();

		/**
		 * The meta object literal for the '<em><b>File After</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FLAT_FIELD_TYPE__FILE_AFTER = eINSTANCE.getFlatFieldType_FileAfter();

		/**
		 * The meta object literal for the '<em><b>Profile Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FLAT_FIELD_TYPE__PROFILE_TYPE = eINSTANCE.getFlatFieldType_ProfileType();

		/**
		 * The meta object literal for the '<em><b>File Profile</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FLAT_FIELD_TYPE__FILE_PROFILE = eINSTANCE.getFlatFieldType_FileProfile();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.GapTypeImpl <em>Gap Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.GapTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getGapType()
		 * @generated
		 */
		EClass GAP_TYPE = eINSTANCE.getGapType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAP_TYPE__VALUE = eINSTANCE.getGapType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAP_TYPE__INFO = eINSTANCE.getGapType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterColumnsTypeImpl <em>High Peaks After Columns Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterColumnsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHighPeaksAfterColumnsType()
		 * @generated
		 */
		EClass HIGH_PEAKS_AFTER_COLUMNS_TYPE = eINSTANCE.getHighPeaksAfterColumnsType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HIGH_PEAKS_AFTER_COLUMNS_TYPE__TYPE = eINSTANCE.getHighPeaksAfterColumnsType_Type();

		/**
		 * The meta object literal for the '<em><b>Number Pixels</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HIGH_PEAKS_AFTER_COLUMNS_TYPE__NUMBER_PIXELS = eINSTANCE.getHighPeaksAfterColumnsType_NumberPixels();

		/**
		 * The meta object literal for the '<em><b>Jump</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HIGH_PEAKS_AFTER_COLUMNS_TYPE__JUMP = eINSTANCE.getHighPeaksAfterColumnsType_Jump();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl <em>High Peaks After Rows Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHighPeaksAfterRowsType()
		 * @generated
		 */
		EClass HIGH_PEAKS_AFTER_ROWS_TYPE = eINSTANCE.getHighPeaksAfterRowsType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE = eINSTANCE.getHighPeaksAfterRowsType_Type();

		/**
		 * The meta object literal for the '<em><b>Number Pixels</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS = eINSTANCE.getHighPeaksAfterRowsType_NumberPixels();

		/**
		 * The meta object literal for the '<em><b>Jump</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP = eINSTANCE.getHighPeaksAfterRowsType_Jump();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksBeforeTypeImpl <em>High Peaks Before Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksBeforeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHighPeaksBeforeType()
		 * @generated
		 */
		EClass HIGH_PEAKS_BEFORE_TYPE = eINSTANCE.getHighPeaksBeforeType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HIGH_PEAKS_BEFORE_TYPE__TYPE = eINSTANCE.getHighPeaksBeforeType_Type();

		/**
		 * The meta object literal for the '<em><b>Number Pixels</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HIGH_PEAKS_BEFORE_TYPE__NUMBER_PIXELS = eINSTANCE.getHighPeaksBeforeType_NumberPixels();

		/**
		 * The meta object literal for the '<em><b>Jump</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HIGH_PEAKS_BEFORE_TYPE__JUMP = eINSTANCE.getHighPeaksBeforeType_Jump();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HMxmlTypeImpl <em>HMxml Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HMxmlTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getHMxmlType()
		 * @generated
		 */
		EClass HMXML_TYPE = eINSTANCE.getHMxmlType();

		/**
		 * The meta object literal for the '<em><b>FBP</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HMXML_TYPE__FBP = eINSTANCE.getHMxmlType_FBP();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageFirstTypeImpl <em>Image First Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageFirstTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getImageFirstType()
		 * @generated
		 */
		EClass IMAGE_FIRST_TYPE = eINSTANCE.getImageFirstType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGE_FIRST_TYPE__VALUE = eINSTANCE.getImageFirstType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGE_FIRST_TYPE__DONE = eINSTANCE.getImageFirstType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageLastTypeImpl <em>Image Last Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageLastTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getImageLastType()
		 * @generated
		 */
		EClass IMAGE_LAST_TYPE = eINSTANCE.getImageLastType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGE_LAST_TYPE__VALUE = eINSTANCE.getImageLastType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGE_LAST_TYPE__DONE = eINSTANCE.getImageLastType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageStepTypeImpl <em>Image Step Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ImageStepTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getImageStepType()
		 * @generated
		 */
		EClass IMAGE_STEP_TYPE = eINSTANCE.getImageStepType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGE_STEP_TYPE__VALUE = eINSTANCE.getImageStepType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMAGE_STEP_TYPE__DONE = eINSTANCE.getImageStepType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl <em>Input Data Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getInputDataType()
		 * @generated
		 */
		EClass INPUT_DATA_TYPE = eINSTANCE.getInputDataType();

		/**
		 * The meta object literal for the '<em><b>Folder</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__FOLDER = eINSTANCE.getInputDataType_Folder();

		/**
		 * The meta object literal for the '<em><b>Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__PREFIX = eINSTANCE.getInputDataType_Prefix();

		/**
		 * The meta object literal for the '<em><b>Suffix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__SUFFIX = eINSTANCE.getInputDataType_Suffix();

		/**
		 * The meta object literal for the '<em><b>Extension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__EXTENSION = eINSTANCE.getInputDataType_Extension();

		/**
		 * The meta object literal for the '<em><b>NOD</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__NOD = eINSTANCE.getInputDataType_NOD();

		/**
		 * The meta object literal for the '<em><b>Memory Size Max</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__MEMORY_SIZE_MAX = eINSTANCE.getInputDataType_MemorySizeMax();

		/**
		 * The meta object literal for the '<em><b>Memory Size Min</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__MEMORY_SIZE_MIN = eINSTANCE.getInputDataType_MemorySizeMin();

		/**
		 * The meta object literal for the '<em><b>Orientation</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__ORIENTATION = eINSTANCE.getInputDataType_Orientation();

		/**
		 * The meta object literal for the '<em><b>File First</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__FILE_FIRST = eINSTANCE.getInputDataType_FileFirst();

		/**
		 * The meta object literal for the '<em><b>File Last</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__FILE_LAST = eINSTANCE.getInputDataType_FileLast();

		/**
		 * The meta object literal for the '<em><b>File Step</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__FILE_STEP = eINSTANCE.getInputDataType_FileStep();

		/**
		 * The meta object literal for the '<em><b>Image First</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__IMAGE_FIRST = eINSTANCE.getInputDataType_ImageFirst();

		/**
		 * The meta object literal for the '<em><b>Image Last</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__IMAGE_LAST = eINSTANCE.getInputDataType_ImageLast();

		/**
		 * The meta object literal for the '<em><b>Image Step</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__IMAGE_STEP = eINSTANCE.getInputDataType_ImageStep();

		/**
		 * The meta object literal for the '<em><b>Raw</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__RAW = eINSTANCE.getInputDataType_Raw();

		/**
		 * The meta object literal for the '<em><b>First Image Index</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__FIRST_IMAGE_INDEX = eINSTANCE.getInputDataType_FirstImageIndex();

		/**
		 * The meta object literal for the '<em><b>Images Per File</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__IMAGES_PER_FILE = eINSTANCE.getInputDataType_ImagesPerFile();

		/**
		 * The meta object literal for the '<em><b>Restrictions</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__RESTRICTIONS = eINSTANCE.getInputDataType_Restrictions();

		/**
		 * The meta object literal for the '<em><b>Value Min</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__VALUE_MIN = eINSTANCE.getInputDataType_ValueMin();

		/**
		 * The meta object literal for the '<em><b>Value Max</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__VALUE_MAX = eINSTANCE.getInputDataType_ValueMax();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__TYPE = eINSTANCE.getInputDataType_Type();

		/**
		 * The meta object literal for the '<em><b>Shape</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_TYPE__SHAPE = eINSTANCE.getInputDataType_Shape();

		/**
		 * The meta object literal for the '<em><b>Pixel Param</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TYPE__PIXEL_PARAM = eINSTANCE.getInputDataType_PixelParam();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl <em>Intensity Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getIntensityType()
		 * @generated
		 */
		EClass INTENSITY_TYPE = eINSTANCE.getIntensityType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INTENSITY_TYPE__TYPE = eINSTANCE.getIntensityType_Type();

		/**
		 * The meta object literal for the '<em><b>Column Left</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTENSITY_TYPE__COLUMN_LEFT = eINSTANCE.getIntensityType_ColumnLeft();

		/**
		 * The meta object literal for the '<em><b>Column Right</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTENSITY_TYPE__COLUMN_RIGHT = eINSTANCE.getIntensityType_ColumnRight();

		/**
		 * The meta object literal for the '<em><b>Zero Left</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTENSITY_TYPE__ZERO_LEFT = eINSTANCE.getIntensityType_ZeroLeft();

		/**
		 * The meta object literal for the '<em><b>Zero Right</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTENSITY_TYPE__ZERO_RIGHT = eINSTANCE.getIntensityType_ZeroRight();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InterpolationTypeImpl <em>Interpolation Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InterpolationTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getInterpolationType()
		 * @generated
		 */
		EClass INTERPOLATION_TYPE = eINSTANCE.getInterpolationType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERPOLATION_TYPE__VALUE = eINSTANCE.getInterpolationType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERPOLATION_TYPE__INFO = eINSTANCE.getInterpolationType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMaxTypeImpl <em>Memory Size Max Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMaxTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMemorySizeMaxType()
		 * @generated
		 */
		EClass MEMORY_SIZE_MAX_TYPE = eINSTANCE.getMemorySizeMaxType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MEMORY_SIZE_MAX_TYPE__VALUE = eINSTANCE.getMemorySizeMaxType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MEMORY_SIZE_MAX_TYPE__INFO = eINSTANCE.getMemorySizeMaxType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMinTypeImpl <em>Memory Size Min Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MemorySizeMinTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMemorySizeMinType()
		 * @generated
		 */
		EClass MEMORY_SIZE_MIN_TYPE = eINSTANCE.getMemorySizeMinType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MEMORY_SIZE_MIN_TYPE__VALUE = eINSTANCE.getMemorySizeMinType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MEMORY_SIZE_MIN_TYPE__INFO = eINSTANCE.getMemorySizeMinType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeImpl <em>Missed Projections Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMissedProjectionsType()
		 * @generated
		 */
		EClass MISSED_PROJECTIONS_TYPE = eINSTANCE.getMissedProjectionsType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MISSED_PROJECTIONS_TYPE__VALUE = eINSTANCE.getMissedProjectionsType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MISSED_PROJECTIONS_TYPE__INFO = eINSTANCE.getMissedProjectionsType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeTypeImpl <em>Missed Projections Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.MissedProjectionsTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getMissedProjectionsTypeType()
		 * @generated
		 */
		EClass MISSED_PROJECTIONS_TYPE_TYPE = eINSTANCE.getMissedProjectionsTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MISSED_PROJECTIONS_TYPE_TYPE__VALUE = eINSTANCE.getMissedProjectionsTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MISSED_PROJECTIONS_TYPE_TYPE__INFO = eINSTANCE.getMissedProjectionsTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NameTypeImpl <em>Name Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NameTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNameType()
		 * @generated
		 */
		EClass NAME_TYPE = eINSTANCE.getNameType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NAME_TYPE__VALUE = eINSTANCE.getNameType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NAME_TYPE__INFO = eINSTANCE.getNameType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NODTypeImpl <em>NOD Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NODTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNODType()
		 * @generated
		 */
		EClass NOD_TYPE = eINSTANCE.getNODType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NOD_TYPE__VALUE = eINSTANCE.getNODType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NOD_TYPE__INFO = eINSTANCE.getNODType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NormalisationTypeImpl <em>Normalisation Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NormalisationTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNormalisationType()
		 * @generated
		 */
		EClass NORMALISATION_TYPE = eINSTANCE.getNormalisationType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NORMALISATION_TYPE__VALUE = eINSTANCE.getNormalisationType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NORMALISATION_TYPE__INFO = eINSTANCE.getNormalisationType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NumSeriesTypeImpl <em>Num Series Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.NumSeriesTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getNumSeriesType()
		 * @generated
		 */
		EClass NUM_SERIES_TYPE = eINSTANCE.getNumSeriesType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NUM_SERIES_TYPE__VALUE = eINSTANCE.getNumSeriesType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NUM_SERIES_TYPE__INFO = eINSTANCE.getNumSeriesType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OffsetTypeImpl <em>Offset Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OffsetTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOffsetType()
		 * @generated
		 */
		EClass OFFSET_TYPE = eINSTANCE.getOffsetType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OFFSET_TYPE__VALUE = eINSTANCE.getOffsetType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OFFSET_TYPE__INFO = eINSTANCE.getOffsetType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OrientationTypeImpl <em>Orientation Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OrientationTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOrientationType()
		 * @generated
		 */
		EClass ORIENTATION_TYPE = eINSTANCE.getOrientationType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORIENTATION_TYPE__VALUE = eINSTANCE.getOrientationType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORIENTATION_TYPE__DONE = eINSTANCE.getOrientationType_Done();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORIENTATION_TYPE__INFO = eINSTANCE.getOrientationType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl <em>Output Data Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOutputDataType()
		 * @generated
		 */
		EClass OUTPUT_DATA_TYPE = eINSTANCE.getOutputDataType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OUTPUT_DATA_TYPE__TYPE = eINSTANCE.getOutputDataType_Type();

		/**
		 * The meta object literal for the '<em><b>State</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OUTPUT_DATA_TYPE__STATE = eINSTANCE.getOutputDataType_State();

		/**
		 * The meta object literal for the '<em><b>Folder</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__FOLDER = eINSTANCE.getOutputDataType_Folder();

		/**
		 * The meta object literal for the '<em><b>Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__PREFIX = eINSTANCE.getOutputDataType_Prefix();

		/**
		 * The meta object literal for the '<em><b>Suffix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__SUFFIX = eINSTANCE.getOutputDataType_Suffix();

		/**
		 * The meta object literal for the '<em><b>Extension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__EXTENSION = eINSTANCE.getOutputDataType_Extension();

		/**
		 * The meta object literal for the '<em><b>NOD</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__NOD = eINSTANCE.getOutputDataType_NOD();

		/**
		 * The meta object literal for the '<em><b>File First</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__FILE_FIRST = eINSTANCE.getOutputDataType_FileFirst();

		/**
		 * The meta object literal for the '<em><b>File Step</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__FILE_STEP = eINSTANCE.getOutputDataType_FileStep();

		/**
		 * The meta object literal for the '<em><b>Bits Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OUTPUT_DATA_TYPE__BITS_TYPE = eINSTANCE.getOutputDataType_BitsType();

		/**
		 * The meta object literal for the '<em><b>Bits</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__BITS = eINSTANCE.getOutputDataType_Bits();

		/**
		 * The meta object literal for the '<em><b>Restrictions</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OUTPUT_DATA_TYPE__RESTRICTIONS = eINSTANCE.getOutputDataType_Restrictions();

		/**
		 * The meta object literal for the '<em><b>Value Min</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__VALUE_MIN = eINSTANCE.getOutputDataType_ValueMin();

		/**
		 * The meta object literal for the '<em><b>Value Max</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_DATA_TYPE__VALUE_MAX = eINSTANCE.getOutputDataType_ValueMax();

		/**
		 * The meta object literal for the '<em><b>Shape</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OUTPUT_DATA_TYPE__SHAPE = eINSTANCE.getOutputDataType_Shape();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputWidthTypeTypeImpl <em>Output Width Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputWidthTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getOutputWidthTypeType()
		 * @generated
		 */
		EClass OUTPUT_WIDTH_TYPE_TYPE = eINSTANCE.getOutputWidthTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_WIDTH_TYPE_TYPE__VALUE = eINSTANCE.getOutputWidthTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OUTPUT_WIDTH_TYPE_TYPE__INFO = eINSTANCE.getOutputWidthTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PolarCartesianInterpolationTypeImpl <em>Polar Cartesian Interpolation Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PolarCartesianInterpolationTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getPolarCartesianInterpolationType()
		 * @generated
		 */
		EClass POLAR_CARTESIAN_INTERPOLATION_TYPE = eINSTANCE.getPolarCartesianInterpolationType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute POLAR_CARTESIAN_INTERPOLATION_TYPE__VALUE = eINSTANCE.getPolarCartesianInterpolationType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute POLAR_CARTESIAN_INTERPOLATION_TYPE__DONE = eINSTANCE.getPolarCartesianInterpolationType_Done();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute POLAR_CARTESIAN_INTERPOLATION_TYPE__INFO = eINSTANCE.getPolarCartesianInterpolationType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl <em>Preprocessing Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getPreprocessingType()
		 * @generated
		 */
		EClass PREPROCESSING_TYPE = eINSTANCE.getPreprocessingType();

		/**
		 * The meta object literal for the '<em><b>High Peaks Before</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE = eINSTANCE.getPreprocessingType_HighPeaksBefore();

		/**
		 * The meta object literal for the '<em><b>Ring Artefacts</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PREPROCESSING_TYPE__RING_ARTEFACTS = eINSTANCE.getPreprocessingType_RingArtefacts();

		/**
		 * The meta object literal for the '<em><b>Intensity</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PREPROCESSING_TYPE__INTENSITY = eINSTANCE.getPreprocessingType_Intensity();

		/**
		 * The meta object literal for the '<em><b>High Peaks After Rows</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS = eINSTANCE.getPreprocessingType_HighPeaksAfterRows();

		/**
		 * The meta object literal for the '<em><b>High Peaks After Columns</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS = eINSTANCE.getPreprocessingType_HighPeaksAfterColumns();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeTypeImpl <em>Profile Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getProfileTypeType()
		 * @generated
		 */
		EClass PROFILE_TYPE_TYPE = eINSTANCE.getProfileTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROFILE_TYPE_TYPE__VALUE = eINSTANCE.getProfileTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROFILE_TYPE_TYPE__INFO = eINSTANCE.getProfileTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeType1Impl <em>Profile Type Type1</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ProfileTypeType1Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getProfileTypeType1()
		 * @generated
		 */
		EClass PROFILE_TYPE_TYPE1 = eINSTANCE.getProfileTypeType1();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROFILE_TYPE_TYPE1__VALUE = eINSTANCE.getProfileTypeType1_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROFILE_TYPE_TYPE1__INFO = eINSTANCE.getProfileTypeType1_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl <em>Raw Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRawType()
		 * @generated
		 */
		EClass RAW_TYPE = eINSTANCE.getRawType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RAW_TYPE__TYPE = eINSTANCE.getRawType_Type();

		/**
		 * The meta object literal for the '<em><b>Bits</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RAW_TYPE__BITS = eINSTANCE.getRawType_Bits();

		/**
		 * The meta object literal for the '<em><b>Offset</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RAW_TYPE__OFFSET = eINSTANCE.getRawType_Offset();

		/**
		 * The meta object literal for the '<em><b>Byte Order</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RAW_TYPE__BYTE_ORDER = eINSTANCE.getRawType_ByteOrder();

		/**
		 * The meta object literal for the '<em><b>Xlen</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RAW_TYPE__XLEN = eINSTANCE.getRawType_Xlen();

		/**
		 * The meta object literal for the '<em><b>Ylen</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RAW_TYPE__YLEN = eINSTANCE.getRawType_Ylen();

		/**
		 * The meta object literal for the '<em><b>Zlen</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RAW_TYPE__ZLEN = eINSTANCE.getRawType_Zlen();

		/**
		 * The meta object literal for the '<em><b>Gap</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RAW_TYPE__GAP = eINSTANCE.getRawType_Gap();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RAW_TYPE__DONE = eINSTANCE.getRawType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsTypeImpl <em>Restrictions Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRestrictionsType()
		 * @generated
		 */
		EClass RESTRICTIONS_TYPE = eINSTANCE.getRestrictionsType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESTRICTIONS_TYPE__VALUE = eINSTANCE.getRestrictionsType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESTRICTIONS_TYPE__INFO = eINSTANCE.getRestrictionsType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsType1Impl <em>Restrictions Type1</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RestrictionsType1Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRestrictionsType1()
		 * @generated
		 */
		EClass RESTRICTIONS_TYPE1 = eINSTANCE.getRestrictionsType1();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESTRICTIONS_TYPE1__VALUE = eINSTANCE.getRestrictionsType1_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESTRICTIONS_TYPE1__INFO = eINSTANCE.getRestrictionsType1_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl <em>Ring Artefacts Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RingArtefactsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRingArtefactsType()
		 * @generated
		 */
		EClass RING_ARTEFACTS_TYPE = eINSTANCE.getRingArtefactsType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RING_ARTEFACTS_TYPE__TYPE = eINSTANCE.getRingArtefactsType_Type();

		/**
		 * The meta object literal for the '<em><b>Parameter N</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RING_ARTEFACTS_TYPE__PARAMETER_N = eINSTANCE.getRingArtefactsType_ParameterN();

		/**
		 * The meta object literal for the '<em><b>Parameter R</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RING_ARTEFACTS_TYPE__PARAMETER_R = eINSTANCE.getRingArtefactsType_ParameterR();

		/**
		 * The meta object literal for the '<em><b>Num Series</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RING_ARTEFACTS_TYPE__NUM_SERIES = eINSTANCE.getRingArtefactsType_NumSeries();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl <em>ROI Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getROIType()
		 * @generated
		 */
		EClass ROI_TYPE = eINSTANCE.getROIType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ROI_TYPE__TYPE = eINSTANCE.getROIType_Type();

		/**
		 * The meta object literal for the '<em><b>Xmin</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROI_TYPE__XMIN = eINSTANCE.getROIType_Xmin();

		/**
		 * The meta object literal for the '<em><b>Xmax</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROI_TYPE__XMAX = eINSTANCE.getROIType_Xmax();

		/**
		 * The meta object literal for the '<em><b>Ymin</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROI_TYPE__YMIN = eINSTANCE.getROIType_Ymin();

		/**
		 * The meta object literal for the '<em><b>Ymax</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROI_TYPE__YMAX = eINSTANCE.getROIType_Ymax();

		/**
		 * The meta object literal for the '<em><b>Output Width Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ROI_TYPE__OUTPUT_WIDTH_TYPE = eINSTANCE.getROIType_OutputWidthType();

		/**
		 * The meta object literal for the '<em><b>Output Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROI_TYPE__OUTPUT_WIDTH = eINSTANCE.getROIType_OutputWidth();

		/**
		 * The meta object literal for the '<em><b>Angle</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROI_TYPE__ANGLE = eINSTANCE.getROIType_Angle();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleEndPointsTypeImpl <em>Rotation Angle End Points Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleEndPointsTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRotationAngleEndPointsType()
		 * @generated
		 */
		EClass ROTATION_ANGLE_END_POINTS_TYPE = eINSTANCE.getRotationAngleEndPointsType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROTATION_ANGLE_END_POINTS_TYPE__VALUE = eINSTANCE.getRotationAngleEndPointsType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROTATION_ANGLE_END_POINTS_TYPE__INFO = eINSTANCE.getRotationAngleEndPointsType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleTypeTypeImpl <em>Rotation Angle Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RotationAngleTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getRotationAngleTypeType()
		 * @generated
		 */
		EClass ROTATION_ANGLE_TYPE_TYPE = eINSTANCE.getRotationAngleTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROTATION_ANGLE_TYPE_TYPE__VALUE = eINSTANCE.getRotationAngleTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROTATION_ANGLE_TYPE_TYPE__INFO = eINSTANCE.getRotationAngleTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ScaleTypeTypeImpl <em>Scale Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ScaleTypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getScaleTypeType()
		 * @generated
		 */
		EClass SCALE_TYPE_TYPE = eINSTANCE.getScaleTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCALE_TYPE_TYPE__VALUE = eINSTANCE.getScaleTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCALE_TYPE_TYPE__INFO = eINSTANCE.getScaleTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeTypeImpl <em>Shape Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getShapeType()
		 * @generated
		 */
		EClass SHAPE_TYPE = eINSTANCE.getShapeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHAPE_TYPE__VALUE = eINSTANCE.getShapeType_Value();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHAPE_TYPE__DONE = eINSTANCE.getShapeType_Done();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHAPE_TYPE__INFO = eINSTANCE.getShapeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeType1Impl <em>Shape Type1</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ShapeType1Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getShapeType1()
		 * @generated
		 */
		EClass SHAPE_TYPE1 = eINSTANCE.getShapeType1();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHAPE_TYPE1__VALUE = eINSTANCE.getShapeType1_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SHAPE_TYPE1__INFO = eINSTANCE.getShapeType1_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.StateTypeImpl <em>State Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.StateTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getStateType()
		 * @generated
		 */
		EClass STATE_TYPE = eINSTANCE.getStateType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STATE_TYPE__VALUE = eINSTANCE.getStateType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STATE_TYPE__INFO = eINSTANCE.getStateType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl <em>Tilt Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TiltTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTiltType()
		 * @generated
		 */
		EClass TILT_TYPE = eINSTANCE.getTiltType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TILT_TYPE__TYPE = eINSTANCE.getTiltType_Type();

		/**
		 * The meta object literal for the '<em><b>XTilt</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TILT_TYPE__XTILT = eINSTANCE.getTiltType_XTilt();

		/**
		 * The meta object literal for the '<em><b>ZTilt</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TILT_TYPE__ZTILT = eINSTANCE.getTiltType_ZTilt();

		/**
		 * The meta object literal for the '<em><b>Done</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TILT_TYPE__DONE = eINSTANCE.getTiltType_Done();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl <em>Transform Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTransformType()
		 * @generated
		 */
		EClass TRANSFORM_TYPE = eINSTANCE.getTransformType();

		/**
		 * The meta object literal for the '<em><b>Missed Projections</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__MISSED_PROJECTIONS = eINSTANCE.getTransformType_MissedProjections();

		/**
		 * The meta object literal for the '<em><b>Missed Projections Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE = eINSTANCE.getTransformType_MissedProjectionsType();

		/**
		 * The meta object literal for the '<em><b>Rotation Angle Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__ROTATION_ANGLE_TYPE = eINSTANCE.getTransformType_RotationAngleType();

		/**
		 * The meta object literal for the '<em><b>Rotation Angle</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__ROTATION_ANGLE = eINSTANCE.getTransformType_RotationAngle();

		/**
		 * The meta object literal for the '<em><b>Rotation Angle End Points</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS = eINSTANCE.getTransformType_RotationAngleEndPoints();

		/**
		 * The meta object literal for the '<em><b>Re Centre Angle</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__RE_CENTRE_ANGLE = eINSTANCE.getTransformType_ReCentreAngle();

		/**
		 * The meta object literal for the '<em><b>Re Centre Radius</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__RE_CENTRE_RADIUS = eINSTANCE.getTransformType_ReCentreRadius();

		/**
		 * The meta object literal for the '<em><b>Crop Top</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__CROP_TOP = eINSTANCE.getTransformType_CropTop();

		/**
		 * The meta object literal for the '<em><b>Crop Bottom</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__CROP_BOTTOM = eINSTANCE.getTransformType_CropBottom();

		/**
		 * The meta object literal for the '<em><b>Crop Left</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__CROP_LEFT = eINSTANCE.getTransformType_CropLeft();

		/**
		 * The meta object literal for the '<em><b>Crop Right</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__CROP_RIGHT = eINSTANCE.getTransformType_CropRight();

		/**
		 * The meta object literal for the '<em><b>Scale Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__SCALE_TYPE = eINSTANCE.getTransformType_ScaleType();

		/**
		 * The meta object literal for the '<em><b>Scale Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__SCALE_WIDTH = eINSTANCE.getTransformType_ScaleWidth();

		/**
		 * The meta object literal for the '<em><b>Scale Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__SCALE_HEIGHT = eINSTANCE.getTransformType_ScaleHeight();

		/**
		 * The meta object literal for the '<em><b>Extrapolation Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__EXTRAPOLATION_TYPE = eINSTANCE.getTransformType_ExtrapolationType();

		/**
		 * The meta object literal for the '<em><b>Extrapolation Pixels</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__EXTRAPOLATION_PIXELS = eINSTANCE.getTransformType_ExtrapolationPixels();

		/**
		 * The meta object literal for the '<em><b>Extrapolation Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSFORM_TYPE__EXTRAPOLATION_WIDTH = eINSTANCE.getTransformType_ExtrapolationWidth();

		/**
		 * The meta object literal for the '<em><b>Interpolation</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORM_TYPE__INTERPOLATION = eINSTANCE.getTransformType_Interpolation();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeTypeImpl <em>Type Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType()
		 * @generated
		 */
		EClass TYPE_TYPE = eINSTANCE.getTypeType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE__VALUE = eINSTANCE.getTypeType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE__INFO = eINSTANCE.getTypeType_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType1Impl <em>Type Type1</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType1Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType1()
		 * @generated
		 */
		EClass TYPE_TYPE1 = eINSTANCE.getTypeType1();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE1__VALUE = eINSTANCE.getTypeType1_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE1__INFO = eINSTANCE.getTypeType1_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType2Impl <em>Type Type2</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType2Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType2()
		 * @generated
		 */
		EClass TYPE_TYPE2 = eINSTANCE.getTypeType2();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE2__VALUE = eINSTANCE.getTypeType2_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE2__INFO = eINSTANCE.getTypeType2_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType3Impl <em>Type Type3</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType3Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType3()
		 * @generated
		 */
		EClass TYPE_TYPE3 = eINSTANCE.getTypeType3();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE3__VALUE = eINSTANCE.getTypeType3_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE3__INFO = eINSTANCE.getTypeType3_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType4Impl <em>Type Type4</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType4Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType4()
		 * @generated
		 */
		EClass TYPE_TYPE4 = eINSTANCE.getTypeType4();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE4__VALUE = eINSTANCE.getTypeType4_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE4__INFO = eINSTANCE.getTypeType4_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType5Impl <em>Type Type5</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType5Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType5()
		 * @generated
		 */
		EClass TYPE_TYPE5 = eINSTANCE.getTypeType5();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE5__VALUE = eINSTANCE.getTypeType5_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE5__INFO = eINSTANCE.getTypeType5_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType6Impl <em>Type Type6</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType6Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType6()
		 * @generated
		 */
		EClass TYPE_TYPE6 = eINSTANCE.getTypeType6();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE6__VALUE = eINSTANCE.getTypeType6_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE6__INFO = eINSTANCE.getTypeType6_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType7Impl <em>Type Type7</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType7Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType7()
		 * @generated
		 */
		EClass TYPE_TYPE7 = eINSTANCE.getTypeType7();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE7__VALUE = eINSTANCE.getTypeType7_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE7__INFO = eINSTANCE.getTypeType7_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType8Impl <em>Type Type8</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType8Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType8()
		 * @generated
		 */
		EClass TYPE_TYPE8 = eINSTANCE.getTypeType8();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE8__VALUE = eINSTANCE.getTypeType8_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE8__INFO = eINSTANCE.getTypeType8_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType9Impl <em>Type Type9</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType9Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType9()
		 * @generated
		 */
		EClass TYPE_TYPE9 = eINSTANCE.getTypeType9();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE9__VALUE = eINSTANCE.getTypeType9_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE9__INFO = eINSTANCE.getTypeType9_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType10Impl <em>Type Type10</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType10Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType10()
		 * @generated
		 */
		EClass TYPE_TYPE10 = eINSTANCE.getTypeType10();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE10__VALUE = eINSTANCE.getTypeType10_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE10__INFO = eINSTANCE.getTypeType10_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType11Impl <em>Type Type11</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType11Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType11()
		 * @generated
		 */
		EClass TYPE_TYPE11 = eINSTANCE.getTypeType11();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE11__VALUE = eINSTANCE.getTypeType11_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE11__INFO = eINSTANCE.getTypeType11_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType12Impl <em>Type Type12</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType12Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType12()
		 * @generated
		 */
		EClass TYPE_TYPE12 = eINSTANCE.getTypeType12();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE12__VALUE = eINSTANCE.getTypeType12_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE12__INFO = eINSTANCE.getTypeType12_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType13Impl <em>Type Type13</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType13Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType13()
		 * @generated
		 */
		EClass TYPE_TYPE13 = eINSTANCE.getTypeType13();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE13__VALUE = eINSTANCE.getTypeType13_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE13__INFO = eINSTANCE.getTypeType13_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType14Impl <em>Type Type14</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType14Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType14()
		 * @generated
		 */
		EClass TYPE_TYPE14 = eINSTANCE.getTypeType14();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE14__VALUE = eINSTANCE.getTypeType14_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE14__INFO = eINSTANCE.getTypeType14_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType15Impl <em>Type Type15</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType15Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType15()
		 * @generated
		 */
		EClass TYPE_TYPE15 = eINSTANCE.getTypeType15();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE15__VALUE = eINSTANCE.getTypeType15_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE15__INFO = eINSTANCE.getTypeType15_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType16Impl <em>Type Type16</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType16Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType16()
		 * @generated
		 */
		EClass TYPE_TYPE16 = eINSTANCE.getTypeType16();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE16__VALUE = eINSTANCE.getTypeType16_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE16__INFO = eINSTANCE.getTypeType16_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType17Impl <em>Type Type17</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TypeType17Impl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getTypeType17()
		 * @generated
		 */
		EClass TYPE_TYPE17 = eINSTANCE.getTypeType17();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE17__VALUE = eINSTANCE.getTypeType17_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPE_TYPE17__INFO = eINSTANCE.getTypeType17_Info();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl <em>Value Max Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getValueMaxType()
		 * @generated
		 */
		EClass VALUE_MAX_TYPE = eINSTANCE.getValueMaxType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALUE_MAX_TYPE__TYPE = eINSTANCE.getValueMaxType_Type();

		/**
		 * The meta object literal for the '<em><b>Percent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALUE_MAX_TYPE__PERCENT = eINSTANCE.getValueMaxType_Percent();

		/**
		 * The meta object literal for the '<em><b>Pixel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALUE_MAX_TYPE__PIXEL = eINSTANCE.getValueMaxType_Pixel();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMinTypeImpl <em>Value Min Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMinTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getValueMinType()
		 * @generated
		 */
		EClass VALUE_MIN_TYPE = eINSTANCE.getValueMinType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALUE_MIN_TYPE__TYPE = eINSTANCE.getValueMinType_Type();

		/**
		 * The meta object literal for the '<em><b>Percent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALUE_MIN_TYPE__PERCENT = eINSTANCE.getValueMinType_Percent();

		/**
		 * The meta object literal for the '<em><b>Pixel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALUE_MIN_TYPE__PIXEL = eINSTANCE.getValueMinType_Pixel();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueStepTypeImpl <em>Value Step Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueStepTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getValueStepType()
		 * @generated
		 */
		EClass VALUE_STEP_TYPE = eINSTANCE.getValueStepType();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALUE_STEP_TYPE__TYPE = eINSTANCE.getValueStepType_Type();

		/**
		 * The meta object literal for the '<em><b>Percent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALUE_STEP_TYPE__PERCENT = eINSTANCE.getValueStepType_Percent();

		/**
		 * The meta object literal for the '<em><b>Pixel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALUE_STEP_TYPE__PIXEL = eINSTANCE.getValueStepType_Pixel();

		/**
		 * The meta object literal for the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.WindowNameTypeImpl <em>Window Name Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.WindowNameTypeImpl
		 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HmPackageImpl#getWindowNameType()
		 * @generated
		 */
		EClass WINDOW_NAME_TYPE = eINSTANCE.getWindowNameType();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW_NAME_TYPE__VALUE = eINSTANCE.getWindowNameType_Value();

		/**
		 * The meta object literal for the '<em><b>Info</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW_NAME_TYPE__INFO = eINSTANCE.getWindowNameType_Info();

	}

} //HmPackage
