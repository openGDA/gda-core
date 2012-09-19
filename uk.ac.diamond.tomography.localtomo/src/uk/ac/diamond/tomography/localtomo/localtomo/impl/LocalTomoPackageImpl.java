/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType;
import uk.ac.diamond.tomography.localtomo.localtomo.ClusterType;
import uk.ac.diamond.tomography.localtomo.localtomo.DocumentRoot;
import uk.ac.diamond.tomography.localtomo.localtomo.FilenameFmtType;
import uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType;
import uk.ac.diamond.tomography.localtomo.localtomo.ImgkeyNXSPathType;
import uk.ac.diamond.tomography.localtomo.localtomo.IxxType;
import uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoFactory;
import uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType;
import uk.ac.diamond.tomography.localtomo.localtomo.NexusfileType;
import uk.ac.diamond.tomography.localtomo.localtomo.QsubType;
import uk.ac.diamond.tomography.localtomo.localtomo.SettingsfileType;
import uk.ac.diamond.tomography.localtomo.localtomo.ShutterClosedPhysType;
import uk.ac.diamond.tomography.localtomo.localtomo.ShutterNXSPathType;
import uk.ac.diamond.tomography.localtomo.localtomo.ShutterOpenPhysType;
import uk.ac.diamond.tomography.localtomo.localtomo.ShutterType;
import uk.ac.diamond.tomography.localtomo.localtomo.StagePosNXSPathType;
import uk.ac.diamond.tomography.localtomo.localtomo.StageRotNXSPathType;
import uk.ac.diamond.tomography.localtomo.localtomo.TifNXSPathType;
import uk.ac.diamond.tomography.localtomo.localtomo.TifimageType;
import uk.ac.diamond.tomography.localtomo.localtomo.TomodoType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class LocalTomoPackageImpl extends EPackageImpl implements LocalTomoPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass beamlineTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clusterTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass documentRootEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass filenameFmtTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imagekeyencodingTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imgkeyNXSPathTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ixxTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass localTomoTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass nexusfileTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass qsubTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass settingsfileTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shutterClosedPhysTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shutterNXSPathTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shutterOpenPhysTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shutterTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stagePosNXSPathTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stageRotNXSPathTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tifimageTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tifNXSPathTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tomodoTypeEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private LocalTomoPackageImpl() {
		super(eNS_URI, LocalTomoFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link LocalTomoPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static LocalTomoPackage init() {
		if (isInited) return (LocalTomoPackage)EPackage.Registry.INSTANCE.getEPackage(LocalTomoPackage.eNS_URI);

		// Obtain or create and register package
		LocalTomoPackageImpl theLocalTomoPackage = (LocalTomoPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof LocalTomoPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new LocalTomoPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theLocalTomoPackage.createPackageContents();

		// Initialize created meta-data
		theLocalTomoPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theLocalTomoPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(LocalTomoPackage.eNS_URI, theLocalTomoPackage);
		return theLocalTomoPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBeamlineType() {
		return beamlineTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBeamlineType_Ixx() {
		return (EReference)beamlineTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getClusterType() {
		return clusterTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getClusterType_Qsub() {
		return (EReference)clusterTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDocumentRoot() {
		return documentRootEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDocumentRoot_Mixed() {
		return (EAttribute)documentRootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_XMLNSPrefixMap() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_XSISchemaLocation() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_LocalTomo() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFilenameFmtType() {
		return filenameFmtTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFilenameFmtType_Value() {
		return (EAttribute)filenameFmtTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFilenameFmtType_Info() {
		return (EAttribute)filenameFmtTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImagekeyencodingType() {
		return imagekeyencodingTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImagekeyencodingType_Darkfield() {
		return (EAttribute)imagekeyencodingTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImagekeyencodingType_Flatfield() {
		return (EAttribute)imagekeyencodingTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImagekeyencodingType_Projection() {
		return (EAttribute)imagekeyencodingTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImgkeyNXSPathType() {
		return imgkeyNXSPathTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImgkeyNXSPathType_Value() {
		return (EAttribute)imgkeyNXSPathTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImgkeyNXSPathType_Info() {
		return (EAttribute)imgkeyNXSPathTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIxxType() {
		return ixxTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIxxType_Value() {
		return (EAttribute)ixxTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIxxType_Info() {
		return (EAttribute)ixxTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getLocalTomoType() {
		return localTomoTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getLocalTomoType_Beamline() {
		return (EReference)localTomoTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getLocalTomoType_Tomodo() {
		return (EReference)localTomoTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNexusfileType() {
		return nexusfileTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNexusfileType_ShutterNXSPath() {
		return (EReference)nexusfileTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNexusfileType_StagePosNXSPath() {
		return (EReference)nexusfileTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNexusfileType_StageRotNXSPath() {
		return (EReference)nexusfileTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNexusfileType_TifNXSPath() {
		return (EReference)nexusfileTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNexusfileType_ImgkeyNXSPath() {
		return (EReference)nexusfileTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getQsubType() {
		return qsubTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getQsubType_Projectname() {
		return (EAttribute)qsubTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getQsubType_Args() {
		return (EAttribute)qsubTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getQsubType_Sinoqueue() {
		return (EAttribute)qsubTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getQsubType_Reconqueue() {
		return (EAttribute)qsubTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSettingsfileType() {
		return settingsfileTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSettingsfileType_Blueprint() {
		return (EAttribute)settingsfileTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getShutterClosedPhysType() {
		return shutterClosedPhysTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShutterClosedPhysType_Value() {
		return (EAttribute)shutterClosedPhysTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShutterClosedPhysType_Info() {
		return (EAttribute)shutterClosedPhysTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getShutterNXSPathType() {
		return shutterNXSPathTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShutterNXSPathType_Value() {
		return (EAttribute)shutterNXSPathTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShutterNXSPathType_Info() {
		return (EAttribute)shutterNXSPathTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getShutterOpenPhysType() {
		return shutterOpenPhysTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShutterOpenPhysType_Value() {
		return (EAttribute)shutterOpenPhysTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShutterOpenPhysType_Info() {
		return (EAttribute)shutterOpenPhysTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getShutterType() {
		return shutterTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getShutterType_ShutterOpenPhys() {
		return (EReference)shutterTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getShutterType_ShutterClosedPhys() {
		return (EReference)shutterTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getStagePosNXSPathType() {
		return stagePosNXSPathTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStagePosNXSPathType_Value() {
		return (EAttribute)stagePosNXSPathTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStagePosNXSPathType_Info() {
		return (EAttribute)stagePosNXSPathTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getStageRotNXSPathType() {
		return stageRotNXSPathTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStageRotNXSPathType_Value() {
		return (EAttribute)stageRotNXSPathTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStageRotNXSPathType_Info() {
		return (EAttribute)stageRotNXSPathTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTifimageType() {
		return tifimageTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTifimageType_FilenameFmt() {
		return (EReference)tifimageTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTifNXSPathType() {
		return tifNXSPathTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTifNXSPathType_Value() {
		return (EAttribute)tifNXSPathTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTifNXSPathType_Info() {
		return (EAttribute)tifNXSPathTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTomodoType() {
		return tomodoTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTomodoType_Shutter() {
		return (EReference)tomodoTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTomodoType_Tifimage() {
		return (EReference)tomodoTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTomodoType_Nexusfile() {
		return (EReference)tomodoTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTomodoType_Settingsfile() {
		return (EReference)tomodoTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTomodoType_Imagekeyencoding() {
		return (EReference)tomodoTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTomodoType_Cluster() {
		return (EReference)tomodoTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTomoFactory getLocalTomoFactory() {
		return (LocalTomoFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		beamlineTypeEClass = createEClass(BEAMLINE_TYPE);
		createEReference(beamlineTypeEClass, BEAMLINE_TYPE__IXX);

		clusterTypeEClass = createEClass(CLUSTER_TYPE);
		createEReference(clusterTypeEClass, CLUSTER_TYPE__QSUB);

		documentRootEClass = createEClass(DOCUMENT_ROOT);
		createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
		createEReference(documentRootEClass, DOCUMENT_ROOT__LOCAL_TOMO);

		filenameFmtTypeEClass = createEClass(FILENAME_FMT_TYPE);
		createEAttribute(filenameFmtTypeEClass, FILENAME_FMT_TYPE__VALUE);
		createEAttribute(filenameFmtTypeEClass, FILENAME_FMT_TYPE__INFO);

		imagekeyencodingTypeEClass = createEClass(IMAGEKEYENCODING_TYPE);
		createEAttribute(imagekeyencodingTypeEClass, IMAGEKEYENCODING_TYPE__DARKFIELD);
		createEAttribute(imagekeyencodingTypeEClass, IMAGEKEYENCODING_TYPE__FLATFIELD);
		createEAttribute(imagekeyencodingTypeEClass, IMAGEKEYENCODING_TYPE__PROJECTION);

		imgkeyNXSPathTypeEClass = createEClass(IMGKEY_NXS_PATH_TYPE);
		createEAttribute(imgkeyNXSPathTypeEClass, IMGKEY_NXS_PATH_TYPE__VALUE);
		createEAttribute(imgkeyNXSPathTypeEClass, IMGKEY_NXS_PATH_TYPE__INFO);

		ixxTypeEClass = createEClass(IXX_TYPE);
		createEAttribute(ixxTypeEClass, IXX_TYPE__VALUE);
		createEAttribute(ixxTypeEClass, IXX_TYPE__INFO);

		localTomoTypeEClass = createEClass(LOCAL_TOMO_TYPE);
		createEReference(localTomoTypeEClass, LOCAL_TOMO_TYPE__BEAMLINE);
		createEReference(localTomoTypeEClass, LOCAL_TOMO_TYPE__TOMODO);

		nexusfileTypeEClass = createEClass(NEXUSFILE_TYPE);
		createEReference(nexusfileTypeEClass, NEXUSFILE_TYPE__SHUTTER_NXS_PATH);
		createEReference(nexusfileTypeEClass, NEXUSFILE_TYPE__STAGE_POS_NXS_PATH);
		createEReference(nexusfileTypeEClass, NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH);
		createEReference(nexusfileTypeEClass, NEXUSFILE_TYPE__TIF_NXS_PATH);
		createEReference(nexusfileTypeEClass, NEXUSFILE_TYPE__IMGKEY_NXS_PATH);

		qsubTypeEClass = createEClass(QSUB_TYPE);
		createEAttribute(qsubTypeEClass, QSUB_TYPE__PROJECTNAME);
		createEAttribute(qsubTypeEClass, QSUB_TYPE__ARGS);
		createEAttribute(qsubTypeEClass, QSUB_TYPE__SINOQUEUE);
		createEAttribute(qsubTypeEClass, QSUB_TYPE__RECONQUEUE);

		settingsfileTypeEClass = createEClass(SETTINGSFILE_TYPE);
		createEAttribute(settingsfileTypeEClass, SETTINGSFILE_TYPE__BLUEPRINT);

		shutterClosedPhysTypeEClass = createEClass(SHUTTER_CLOSED_PHYS_TYPE);
		createEAttribute(shutterClosedPhysTypeEClass, SHUTTER_CLOSED_PHYS_TYPE__VALUE);
		createEAttribute(shutterClosedPhysTypeEClass, SHUTTER_CLOSED_PHYS_TYPE__INFO);

		shutterNXSPathTypeEClass = createEClass(SHUTTER_NXS_PATH_TYPE);
		createEAttribute(shutterNXSPathTypeEClass, SHUTTER_NXS_PATH_TYPE__VALUE);
		createEAttribute(shutterNXSPathTypeEClass, SHUTTER_NXS_PATH_TYPE__INFO);

		shutterOpenPhysTypeEClass = createEClass(SHUTTER_OPEN_PHYS_TYPE);
		createEAttribute(shutterOpenPhysTypeEClass, SHUTTER_OPEN_PHYS_TYPE__VALUE);
		createEAttribute(shutterOpenPhysTypeEClass, SHUTTER_OPEN_PHYS_TYPE__INFO);

		shutterTypeEClass = createEClass(SHUTTER_TYPE);
		createEReference(shutterTypeEClass, SHUTTER_TYPE__SHUTTER_OPEN_PHYS);
		createEReference(shutterTypeEClass, SHUTTER_TYPE__SHUTTER_CLOSED_PHYS);

		stagePosNXSPathTypeEClass = createEClass(STAGE_POS_NXS_PATH_TYPE);
		createEAttribute(stagePosNXSPathTypeEClass, STAGE_POS_NXS_PATH_TYPE__VALUE);
		createEAttribute(stagePosNXSPathTypeEClass, STAGE_POS_NXS_PATH_TYPE__INFO);

		stageRotNXSPathTypeEClass = createEClass(STAGE_ROT_NXS_PATH_TYPE);
		createEAttribute(stageRotNXSPathTypeEClass, STAGE_ROT_NXS_PATH_TYPE__VALUE);
		createEAttribute(stageRotNXSPathTypeEClass, STAGE_ROT_NXS_PATH_TYPE__INFO);

		tifimageTypeEClass = createEClass(TIFIMAGE_TYPE);
		createEReference(tifimageTypeEClass, TIFIMAGE_TYPE__FILENAME_FMT);

		tifNXSPathTypeEClass = createEClass(TIF_NXS_PATH_TYPE);
		createEAttribute(tifNXSPathTypeEClass, TIF_NXS_PATH_TYPE__VALUE);
		createEAttribute(tifNXSPathTypeEClass, TIF_NXS_PATH_TYPE__INFO);

		tomodoTypeEClass = createEClass(TOMODO_TYPE);
		createEReference(tomodoTypeEClass, TOMODO_TYPE__SHUTTER);
		createEReference(tomodoTypeEClass, TOMODO_TYPE__TIFIMAGE);
		createEReference(tomodoTypeEClass, TOMODO_TYPE__NEXUSFILE);
		createEReference(tomodoTypeEClass, TOMODO_TYPE__SETTINGSFILE);
		createEReference(tomodoTypeEClass, TOMODO_TYPE__IMAGEKEYENCODING);
		createEReference(tomodoTypeEClass, TOMODO_TYPE__CLUSTER);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(beamlineTypeEClass, BeamlineType.class, "BeamlineType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBeamlineType_Ixx(), this.getIxxType(), null, "ixx", null, 0, 1, BeamlineType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clusterTypeEClass, ClusterType.class, "ClusterType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getClusterType_Qsub(), this.getQsubType(), null, "qsub", null, 0, 1, ClusterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_LocalTomo(), this.getLocalTomoType(), null, "localTomo", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(filenameFmtTypeEClass, FilenameFmtType.class, "FilenameFmtType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFilenameFmtType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, FilenameFmtType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFilenameFmtType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, FilenameFmtType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(imagekeyencodingTypeEClass, ImagekeyencodingType.class, "ImagekeyencodingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImagekeyencodingType_Darkfield(), theXMLTypePackage.getInt(), "darkfield", null, 0, 1, ImagekeyencodingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImagekeyencodingType_Flatfield(), theXMLTypePackage.getInt(), "flatfield", null, 0, 1, ImagekeyencodingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImagekeyencodingType_Projection(), theXMLTypePackage.getInt(), "projection", null, 0, 1, ImagekeyencodingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(imgkeyNXSPathTypeEClass, ImgkeyNXSPathType.class, "ImgkeyNXSPathType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImgkeyNXSPathType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ImgkeyNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImgkeyNXSPathType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ImgkeyNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ixxTypeEClass, IxxType.class, "IxxType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIxxType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, IxxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIxxType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, IxxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(localTomoTypeEClass, LocalTomoType.class, "LocalTomoType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getLocalTomoType_Beamline(), this.getBeamlineType(), null, "beamline", null, 0, 1, LocalTomoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getLocalTomoType_Tomodo(), this.getTomodoType(), null, "tomodo", null, 0, 1, LocalTomoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(nexusfileTypeEClass, NexusfileType.class, "NexusfileType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getNexusfileType_ShutterNXSPath(), this.getShutterNXSPathType(), null, "shutterNXSPath", null, 0, 1, NexusfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getNexusfileType_StagePosNXSPath(), this.getStagePosNXSPathType(), null, "stagePosNXSPath", null, 0, 1, NexusfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getNexusfileType_StageRotNXSPath(), this.getStageRotNXSPathType(), null, "stageRotNXSPath", null, 0, 1, NexusfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getNexusfileType_TifNXSPath(), this.getTifNXSPathType(), null, "tifNXSPath", null, 0, 1, NexusfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getNexusfileType_ImgkeyNXSPath(), this.getImgkeyNXSPathType(), null, "imgkeyNXSPath", null, 0, 1, NexusfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(qsubTypeEClass, QsubType.class, "QsubType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getQsubType_Projectname(), theXMLTypePackage.getNormalizedString(), "projectname", null, 0, 1, QsubType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQsubType_Args(), theXMLTypePackage.getString(), "args", null, 0, 1, QsubType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQsubType_Sinoqueue(), theXMLTypePackage.getNormalizedString(), "sinoqueue", null, 0, 1, QsubType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQsubType_Reconqueue(), theXMLTypePackage.getNormalizedString(), "reconqueue", null, 0, 1, QsubType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(settingsfileTypeEClass, SettingsfileType.class, "SettingsfileType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSettingsfileType_Blueprint(), theXMLTypePackage.getNormalizedString(), "blueprint", null, 0, 1, SettingsfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shutterClosedPhysTypeEClass, ShutterClosedPhysType.class, "ShutterClosedPhysType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getShutterClosedPhysType_Value(), theXMLTypePackage.getDecimal(), "value", null, 0, 1, ShutterClosedPhysType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getShutterClosedPhysType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ShutterClosedPhysType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shutterNXSPathTypeEClass, ShutterNXSPathType.class, "ShutterNXSPathType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getShutterNXSPathType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ShutterNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getShutterNXSPathType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ShutterNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shutterOpenPhysTypeEClass, ShutterOpenPhysType.class, "ShutterOpenPhysType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getShutterOpenPhysType_Value(), theXMLTypePackage.getDecimal(), "value", null, 0, 1, ShutterOpenPhysType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getShutterOpenPhysType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ShutterOpenPhysType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shutterTypeEClass, ShutterType.class, "ShutterType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getShutterType_ShutterOpenPhys(), this.getShutterOpenPhysType(), null, "shutterOpenPhys", null, 0, 1, ShutterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getShutterType_ShutterClosedPhys(), this.getShutterClosedPhysType(), null, "shutterClosedPhys", null, 0, 1, ShutterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stagePosNXSPathTypeEClass, StagePosNXSPathType.class, "StagePosNXSPathType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStagePosNXSPathType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, StagePosNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStagePosNXSPathType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, StagePosNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stageRotNXSPathTypeEClass, StageRotNXSPathType.class, "StageRotNXSPathType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStageRotNXSPathType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, StageRotNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageRotNXSPathType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, StageRotNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tifimageTypeEClass, TifimageType.class, "TifimageType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTifimageType_FilenameFmt(), this.getFilenameFmtType(), null, "filenameFmt", null, 0, 1, TifimageType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tifNXSPathTypeEClass, TifNXSPathType.class, "TifNXSPathType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTifNXSPathType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TifNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTifNXSPathType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TifNXSPathType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tomodoTypeEClass, TomodoType.class, "TomodoType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTomodoType_Shutter(), this.getShutterType(), null, "shutter", null, 0, 1, TomodoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTomodoType_Tifimage(), this.getTifimageType(), null, "tifimage", null, 0, 1, TomodoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTomodoType_Nexusfile(), this.getNexusfileType(), null, "nexusfile", null, 0, 1, TomodoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTomodoType_Settingsfile(), this.getSettingsfileType(), null, "settingsfile", null, 0, 1, TomodoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTomodoType_Imagekeyencoding(), this.getImagekeyencodingType(), null, "imagekeyencoding", null, 0, 1, TomodoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTomodoType_Cluster(), this.getClusterType(), null, "cluster", null, 0, 1, TomodoType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";		
		addAnnotation
		  (this, 
		   source, 
		   new String[] {
			 "qualified", "false"
		   });		
		addAnnotation
		  (beamlineTypeEClass, 
		   source, 
		   new String[] {
			 "name", "beamline_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getBeamlineType_Ixx(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ixx",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (clusterTypeEClass, 
		   source, 
		   new String[] {
			 "name", "cluster_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getClusterType_Qsub(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "qsub",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (documentRootEClass, 
		   source, 
		   new String[] {
			 "name", "",
			 "kind", "mixed"
		   });		
		addAnnotation
		  (getDocumentRoot_Mixed(), 
		   source, 
		   new String[] {
			 "kind", "elementWildcard",
			 "name", ":mixed"
		   });		
		addAnnotation
		  (getDocumentRoot_XMLNSPrefixMap(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "xmlns:prefix"
		   });		
		addAnnotation
		  (getDocumentRoot_XSISchemaLocation(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "xsi:schemaLocation"
		   });		
		addAnnotation
		  (getDocumentRoot_LocalTomo(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "LocalTomo",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (filenameFmtTypeEClass, 
		   source, 
		   new String[] {
			 "name", "filenameFmt_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getFilenameFmtType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getFilenameFmtType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (imagekeyencodingTypeEClass, 
		   source, 
		   new String[] {
			 "name", "imagekeyencoding_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getImagekeyencodingType_Darkfield(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "darkfield",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getImagekeyencodingType_Flatfield(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "flatfield",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getImagekeyencodingType_Projection(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "projection",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (imgkeyNXSPathTypeEClass, 
		   source, 
		   new String[] {
			 "name", "imgkeyNXSPath_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImgkeyNXSPathType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImgkeyNXSPathType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (ixxTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ixx_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getIxxType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getIxxType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (localTomoTypeEClass, 
		   source, 
		   new String[] {
			 "name", "LocalTomo_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getLocalTomoType_Beamline(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "beamline",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getLocalTomoType_Tomodo(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "tomodo",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (nexusfileTypeEClass, 
		   source, 
		   new String[] {
			 "name", "nexusfile_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getNexusfileType_ShutterNXSPath(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "shutterNXSPath",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getNexusfileType_StagePosNXSPath(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "stagePosNXSPath",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getNexusfileType_StageRotNXSPath(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "stageRotNXSPath",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getNexusfileType_TifNXSPath(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "tifNXSPath",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getNexusfileType_ImgkeyNXSPath(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "imgkeyNXSPath",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (qsubTypeEClass, 
		   source, 
		   new String[] {
			 "name", "qsub_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getQsubType_Projectname(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "projectname",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getQsubType_Args(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "args",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getQsubType_Sinoqueue(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "sinoqueue",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getQsubType_Reconqueue(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "reconqueue",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (settingsfileTypeEClass, 
		   source, 
		   new String[] {
			 "name", "settingsfile_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getSettingsfileType_Blueprint(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "blueprint",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (shutterClosedPhysTypeEClass, 
		   source, 
		   new String[] {
			 "name", "shutterClosedPhys_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShutterClosedPhysType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShutterClosedPhysType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (shutterNXSPathTypeEClass, 
		   source, 
		   new String[] {
			 "name", "shutterNXSPath_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShutterNXSPathType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShutterNXSPathType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (shutterOpenPhysTypeEClass, 
		   source, 
		   new String[] {
			 "name", "shutterOpenPhys_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShutterOpenPhysType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShutterOpenPhysType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (shutterTypeEClass, 
		   source, 
		   new String[] {
			 "name", "shutter_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getShutterType_ShutterOpenPhys(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "shutterOpenPhys",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getShutterType_ShutterClosedPhys(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "shutterClosedPhys",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (stagePosNXSPathTypeEClass, 
		   source, 
		   new String[] {
			 "name", "stagePosNXSPath_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getStagePosNXSPathType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getStagePosNXSPathType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (stageRotNXSPathTypeEClass, 
		   source, 
		   new String[] {
			 "name", "stageRotNXSPath_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getStageRotNXSPathType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getStageRotNXSPathType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (tifimageTypeEClass, 
		   source, 
		   new String[] {
			 "name", "tifimage_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getTifimageType_FilenameFmt(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "filenameFmt",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (tifNXSPathTypeEClass, 
		   source, 
		   new String[] {
			 "name", "tifNXSPath_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTifNXSPathType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTifNXSPathType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (tomodoTypeEClass, 
		   source, 
		   new String[] {
			 "name", "tomodo_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getTomodoType_Shutter(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "shutter",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTomodoType_Tifimage(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "tifimage",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTomodoType_Nexusfile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "nexusfile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTomodoType_Settingsfile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "settingsfile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTomodoType_Imagekeyencoding(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "imagekeyencoding",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTomodoType_Cluster(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "cluster",
			 "namespace", "##targetNamespace"
		   });
	}

} //LocalTomoPackageImpl
