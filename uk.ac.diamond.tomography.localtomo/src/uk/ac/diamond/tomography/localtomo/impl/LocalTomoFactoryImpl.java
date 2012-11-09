/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import uk.ac.diamond.tomography.localtomo.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class LocalTomoFactoryImpl extends EFactoryImpl implements LocalTomoFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static LocalTomoFactory init() {
		try {
			LocalTomoFactory theLocalTomoFactory = (LocalTomoFactory)EPackage.Registry.INSTANCE.getEFactory("platform:/resource/uk.ac.diamond.tomography.localtomo/model/localTomo.xsd"); 
			if (theLocalTomoFactory != null) {
				return theLocalTomoFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new LocalTomoFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTomoFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case LocalTomoPackage.BEAMLINE_TYPE: return createBeamlineType();
			case LocalTomoPackage.CLUSTER_TYPE: return createClusterType();
			case LocalTomoPackage.DOCUMENT_ROOT: return createDocumentRoot();
			case LocalTomoPackage.FILENAME_FMT_TYPE: return createFilenameFmtType();
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE: return createImagekeyencodingType();
			case LocalTomoPackage.IMGKEY_NXS_PATH_TYPE: return createImgkeyNXSPathType();
			case LocalTomoPackage.IXX_TYPE: return createIxxType();
			case LocalTomoPackage.LOCAL_TOMO_TYPE: return createLocalTomoType();
			case LocalTomoPackage.NEXUSFILE_TYPE: return createNexusfileType();
			case LocalTomoPackage.QSUB_TYPE: return createQsubType();
			case LocalTomoPackage.SETTINGSFILE_TYPE: return createSettingsfileType();
			case LocalTomoPackage.SHUTTER_CLOSED_PHYS_TYPE: return createShutterClosedPhysType();
			case LocalTomoPackage.SHUTTER_NXS_PATH_TYPE: return createShutterNXSPathType();
			case LocalTomoPackage.SHUTTER_OPEN_PHYS_TYPE: return createShutterOpenPhysType();
			case LocalTomoPackage.SHUTTER_TYPE: return createShutterType();
			case LocalTomoPackage.STAGE_POS_NXS_PATH_TYPE: return createStagePosNXSPathType();
			case LocalTomoPackage.STAGE_ROT_NXS_PATH_TYPE: return createStageRotNXSPathType();
			case LocalTomoPackage.TIFIMAGE_TYPE: return createTifimageType();
			case LocalTomoPackage.TIF_NXS_PATH_TYPE: return createTifNXSPathType();
			case LocalTomoPackage.TOMODO_TYPE: return createTomodoType();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BeamlineType createBeamlineType() {
		BeamlineTypeImpl beamlineType = new BeamlineTypeImpl();
		return beamlineType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ClusterType createClusterType() {
		ClusterTypeImpl clusterType = new ClusterTypeImpl();
		return clusterType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DocumentRoot createDocumentRoot() {
		DocumentRootImpl documentRoot = new DocumentRootImpl();
		return documentRoot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FilenameFmtType createFilenameFmtType() {
		FilenameFmtTypeImpl filenameFmtType = new FilenameFmtTypeImpl();
		return filenameFmtType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImagekeyencodingType createImagekeyencodingType() {
		ImagekeyencodingTypeImpl imagekeyencodingType = new ImagekeyencodingTypeImpl();
		return imagekeyencodingType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImgkeyNXSPathType createImgkeyNXSPathType() {
		ImgkeyNXSPathTypeImpl imgkeyNXSPathType = new ImgkeyNXSPathTypeImpl();
		return imgkeyNXSPathType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IxxType createIxxType() {
		IxxTypeImpl ixxType = new IxxTypeImpl();
		return ixxType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTomoType createLocalTomoType() {
		LocalTomoTypeImpl localTomoType = new LocalTomoTypeImpl();
		return localTomoType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NexusfileType createNexusfileType() {
		NexusfileTypeImpl nexusfileType = new NexusfileTypeImpl();
		return nexusfileType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public QsubType createQsubType() {
		QsubTypeImpl qsubType = new QsubTypeImpl();
		return qsubType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SettingsfileType createSettingsfileType() {
		SettingsfileTypeImpl settingsfileType = new SettingsfileTypeImpl();
		return settingsfileType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterClosedPhysType createShutterClosedPhysType() {
		ShutterClosedPhysTypeImpl shutterClosedPhysType = new ShutterClosedPhysTypeImpl();
		return shutterClosedPhysType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterNXSPathType createShutterNXSPathType() {
		ShutterNXSPathTypeImpl shutterNXSPathType = new ShutterNXSPathTypeImpl();
		return shutterNXSPathType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterOpenPhysType createShutterOpenPhysType() {
		ShutterOpenPhysTypeImpl shutterOpenPhysType = new ShutterOpenPhysTypeImpl();
		return shutterOpenPhysType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterType createShutterType() {
		ShutterTypeImpl shutterType = new ShutterTypeImpl();
		return shutterType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StagePosNXSPathType createStagePosNXSPathType() {
		StagePosNXSPathTypeImpl stagePosNXSPathType = new StagePosNXSPathTypeImpl();
		return stagePosNXSPathType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StageRotNXSPathType createStageRotNXSPathType() {
		StageRotNXSPathTypeImpl stageRotNXSPathType = new StageRotNXSPathTypeImpl();
		return stageRotNXSPathType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TifimageType createTifimageType() {
		TifimageTypeImpl tifimageType = new TifimageTypeImpl();
		return tifimageType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TifNXSPathType createTifNXSPathType() {
		TifNXSPathTypeImpl tifNXSPathType = new TifNXSPathTypeImpl();
		return tifNXSPathType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TomodoType createTomodoType() {
		TomodoTypeImpl tomodoType = new TomodoTypeImpl();
		return tomodoType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTomoPackage getLocalTomoPackage() {
		return (LocalTomoPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static LocalTomoPackage getPackage() {
		return LocalTomoPackage.eINSTANCE;
	}

} //LocalTomoFactoryImpl
