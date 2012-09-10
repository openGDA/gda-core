/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmFactory;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class HmPackageImpl extends EPackageImpl implements HmPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass backprojectionTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass beamlineUserTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bitsTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass byteOrderTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass circlesTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clockwiseRotationTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass coordinateSystemTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass darkFieldTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass defaultXmlTypeEClass = null;

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
	private EClass extrapolationTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fbpTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass filterTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass firstImageIndexTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass flatDarkFieldsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass flatFieldTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass gapTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass highPeaksAfterColumnsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass highPeaksAfterRowsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass highPeaksBeforeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass hMxmlTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imageFirstTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imageLastTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imageStepTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass inputDataTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass intensityTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass interpolationTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass memorySizeMaxTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass memorySizeMinTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass missedProjectionsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass missedProjectionsTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass nameTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass nodTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass normalisationTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass numSeriesTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass offsetTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass orientationTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass outputDataTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass outputWidthTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass polarCartesianInterpolationTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass preprocessingTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass profileTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass profileTypeType1EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rawTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass restrictionsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass restrictionsType1EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ringArtefactsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass roiTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rotationAngleEndPointsTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rotationAngleTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass scaleTypeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shapeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shapeType1EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stateTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tiltTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass transformTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType1EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType2EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType3EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType4EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType5EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType6EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType7EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType8EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType9EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType10EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType11EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType12EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType13EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType14EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType15EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType16EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeType17EClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass valueMaxTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass valueMinTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass valueStepTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass windowNameTypeEClass = null;

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
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private HmPackageImpl() {
		super(eNS_URI, HmFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link HmPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static HmPackage init() {
		if (isInited) return (HmPackage)EPackage.Registry.INSTANCE.getEPackage(HmPackage.eNS_URI);

		// Obtain or create and register package
		HmPackageImpl theHmPackage = (HmPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof HmPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new HmPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theHmPackage.createPackageContents();

		// Initialize created meta-data
		theHmPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theHmPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(HmPackage.eNS_URI, theHmPackage);
		return theHmPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBackprojectionType() {
		return backprojectionTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_Filter() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBackprojectionType_ImageCentre() {
		return (EAttribute)backprojectionTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_ClockwiseRotation() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_Tilt() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_CoordinateSystem() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_Circles() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_ROI() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBackprojectionType_PolarCartesianInterpolation() {
		return (EReference)backprojectionTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBeamlineUserType() {
		return beamlineUserTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBeamlineUserType_Type() {
		return (EReference)beamlineUserTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_BeamlineName() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_Year() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_Month() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_Date() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_VisitNumber() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_InputDataFolder() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_InputScanFolder() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_OutputDataFolder() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_OutputScanFolder() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBeamlineUserType_Done() {
		return (EAttribute)beamlineUserTypeEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBitsTypeType() {
		return bitsTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBitsTypeType_Value() {
		return (EAttribute)bitsTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBitsTypeType_Info() {
		return (EAttribute)bitsTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getByteOrderType() {
		return byteOrderTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getByteOrderType_Value() {
		return (EAttribute)byteOrderTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getByteOrderType_Info() {
		return (EAttribute)byteOrderTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCirclesType() {
		return circlesTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCirclesType_ValueMin() {
		return (EReference)circlesTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCirclesType_ValueMax() {
		return (EReference)circlesTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCirclesType_ValueStep() {
		return (EReference)circlesTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCirclesType_Comm() {
		return (EAttribute)circlesTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getClockwiseRotationType() {
		return clockwiseRotationTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getClockwiseRotationType_Value() {
		return (EAttribute)clockwiseRotationTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getClockwiseRotationType_Done() {
		return (EAttribute)clockwiseRotationTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getClockwiseRotationType_Info() {
		return (EAttribute)clockwiseRotationTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCoordinateSystemType() {
		return coordinateSystemTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCoordinateSystemType_Type() {
		return (EReference)coordinateSystemTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCoordinateSystemType_Slice() {
		return (EAttribute)coordinateSystemTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCoordinateSystemType_Done() {
		return (EAttribute)coordinateSystemTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDarkFieldType() {
		return darkFieldTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDarkFieldType_Type() {
		return (EReference)darkFieldTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDarkFieldType_ValueBefore() {
		return (EAttribute)darkFieldTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDarkFieldType_ValueAfter() {
		return (EAttribute)darkFieldTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDarkFieldType_FileBefore() {
		return (EAttribute)darkFieldTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDarkFieldType_FileAfter() {
		return (EAttribute)darkFieldTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDarkFieldType_ProfileType() {
		return (EReference)darkFieldTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDarkFieldType_FileProfile() {
		return (EAttribute)darkFieldTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDefaultXmlType() {
		return defaultXmlTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDefaultXmlType_Value() {
		return (EAttribute)defaultXmlTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDefaultXmlType_Done() {
		return (EAttribute)defaultXmlTypeEClass.getEStructuralFeatures().get(1);
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
	public EReference getDocumentRoot_HMxml() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getExtrapolationTypeType() {
		return extrapolationTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getExtrapolationTypeType_Value() {
		return (EAttribute)extrapolationTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getExtrapolationTypeType_Info() {
		return (EAttribute)extrapolationTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFBPType() {
		return fbpTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_DefaultXml() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFBPType_GPUDeviceNumber() {
		return (EAttribute)fbpTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_BeamlineUser() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFBPType_LogFile() {
		return (EAttribute)fbpTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_InputData() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_FlatDarkFields() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_Preprocessing() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_Transform() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_Backprojection() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFBPType_OutputData() {
		return (EReference)fbpTypeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFilterType() {
		return filterTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFilterType_Type() {
		return (EReference)filterTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFilterType_Name() {
		return (EReference)filterTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFilterType_Bandwidth() {
		return (EAttribute)filterTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFilterType_WindowName() {
		return (EReference)filterTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFilterType_Normalisation() {
		return (EReference)filterTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFilterType_PixelSize() {
		return (EAttribute)filterTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFirstImageIndexType() {
		return firstImageIndexTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFirstImageIndexType_Value() {
		return (EAttribute)firstImageIndexTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFirstImageIndexType_Info() {
		return (EAttribute)firstImageIndexTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFlatDarkFieldsType() {
		return flatDarkFieldsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFlatDarkFieldsType_FlatField() {
		return (EReference)flatDarkFieldsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFlatDarkFieldsType_DarkField() {
		return (EReference)flatDarkFieldsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFlatFieldType() {
		return flatFieldTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFlatFieldType_Type() {
		return (EReference)flatFieldTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFlatFieldType_ValueBefore() {
		return (EAttribute)flatFieldTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFlatFieldType_ValueAfter() {
		return (EAttribute)flatFieldTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFlatFieldType_FileBefore() {
		return (EAttribute)flatFieldTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFlatFieldType_FileAfter() {
		return (EAttribute)flatFieldTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFlatFieldType_ProfileType() {
		return (EReference)flatFieldTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFlatFieldType_FileProfile() {
		return (EAttribute)flatFieldTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getGapType() {
		return gapTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGapType_Value() {
		return (EAttribute)gapTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGapType_Info() {
		return (EAttribute)gapTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHighPeaksAfterColumnsType() {
		return highPeaksAfterColumnsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHighPeaksAfterColumnsType_Type() {
		return (EReference)highPeaksAfterColumnsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHighPeaksAfterColumnsType_NumberPixels() {
		return (EAttribute)highPeaksAfterColumnsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHighPeaksAfterColumnsType_Jump() {
		return (EAttribute)highPeaksAfterColumnsTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHighPeaksAfterRowsType() {
		return highPeaksAfterRowsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHighPeaksAfterRowsType_Type() {
		return (EReference)highPeaksAfterRowsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHighPeaksAfterRowsType_NumberPixels() {
		return (EAttribute)highPeaksAfterRowsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHighPeaksAfterRowsType_Jump() {
		return (EAttribute)highPeaksAfterRowsTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHighPeaksBeforeType() {
		return highPeaksBeforeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHighPeaksBeforeType_Type() {
		return (EReference)highPeaksBeforeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHighPeaksBeforeType_NumberPixels() {
		return (EAttribute)highPeaksBeforeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHighPeaksBeforeType_Jump() {
		return (EAttribute)highPeaksBeforeTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHMxmlType() {
		return hMxmlTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHMxmlType_FBP() {
		return (EReference)hMxmlTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImageFirstType() {
		return imageFirstTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImageFirstType_Value() {
		return (EAttribute)imageFirstTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImageFirstType_Done() {
		return (EAttribute)imageFirstTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImageLastType() {
		return imageLastTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImageLastType_Value() {
		return (EAttribute)imageLastTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImageLastType_Done() {
		return (EAttribute)imageLastTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImageStepType() {
		return imageStepTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImageStepType_Value() {
		return (EAttribute)imageStepTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImageStepType_Done() {
		return (EAttribute)imageStepTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getInputDataType() {
		return inputDataTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_Folder() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_Prefix() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_Suffix() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_Extension() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_NOD() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_MemorySizeMax() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_MemorySizeMin() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_Orientation() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_FileFirst() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_FileLast() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_FileStep() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_ImageFirst() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_ImageLast() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_ImageStep() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_Raw() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_FirstImageIndex() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_ImagesPerFile() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_Restrictions() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_ValueMin() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_ValueMax() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_Type() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getInputDataType_Shape() {
		return (EReference)inputDataTypeEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInputDataType_PixelParam() {
		return (EAttribute)inputDataTypeEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIntensityType() {
		return intensityTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIntensityType_Type() {
		return (EReference)intensityTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIntensityType_ColumnLeft() {
		return (EAttribute)intensityTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIntensityType_ColumnRight() {
		return (EAttribute)intensityTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIntensityType_ZeroLeft() {
		return (EAttribute)intensityTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIntensityType_ZeroRight() {
		return (EAttribute)intensityTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getInterpolationType() {
		return interpolationTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInterpolationType_Value() {
		return (EAttribute)interpolationTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInterpolationType_Info() {
		return (EAttribute)interpolationTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMemorySizeMaxType() {
		return memorySizeMaxTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMemorySizeMaxType_Value() {
		return (EAttribute)memorySizeMaxTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMemorySizeMaxType_Info() {
		return (EAttribute)memorySizeMaxTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMemorySizeMinType() {
		return memorySizeMinTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMemorySizeMinType_Value() {
		return (EAttribute)memorySizeMinTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMemorySizeMinType_Info() {
		return (EAttribute)memorySizeMinTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMissedProjectionsType() {
		return missedProjectionsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMissedProjectionsType_Value() {
		return (EAttribute)missedProjectionsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMissedProjectionsType_Info() {
		return (EAttribute)missedProjectionsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMissedProjectionsTypeType() {
		return missedProjectionsTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMissedProjectionsTypeType_Value() {
		return (EAttribute)missedProjectionsTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMissedProjectionsTypeType_Info() {
		return (EAttribute)missedProjectionsTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNameType() {
		return nameTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNameType_Value() {
		return (EAttribute)nameTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNameType_Info() {
		return (EAttribute)nameTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNODType() {
		return nodTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNODType_Value() {
		return (EAttribute)nodTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNODType_Info() {
		return (EAttribute)nodTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNormalisationType() {
		return normalisationTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNormalisationType_Value() {
		return (EAttribute)normalisationTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNormalisationType_Info() {
		return (EAttribute)normalisationTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNumSeriesType() {
		return numSeriesTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNumSeriesType_Value() {
		return (EAttribute)numSeriesTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNumSeriesType_Info() {
		return (EAttribute)numSeriesTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getOffsetType() {
		return offsetTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOffsetType_Value() {
		return (EAttribute)offsetTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOffsetType_Info() {
		return (EAttribute)offsetTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getOrientationType() {
		return orientationTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrientationType_Value() {
		return (EAttribute)orientationTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrientationType_Done() {
		return (EAttribute)orientationTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrientationType_Info() {
		return (EAttribute)orientationTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getOutputDataType() {
		return outputDataTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getOutputDataType_Type() {
		return (EReference)outputDataTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getOutputDataType_State() {
		return (EReference)outputDataTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_Folder() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_Prefix() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_Suffix() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_Extension() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_NOD() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_FileFirst() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_FileStep() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getOutputDataType_BitsType() {
		return (EReference)outputDataTypeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_Bits() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getOutputDataType_Restrictions() {
		return (EReference)outputDataTypeEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_ValueMin() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputDataType_ValueMax() {
		return (EAttribute)outputDataTypeEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getOutputDataType_Shape() {
		return (EReference)outputDataTypeEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getOutputWidthTypeType() {
		return outputWidthTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputWidthTypeType_Value() {
		return (EAttribute)outputWidthTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOutputWidthTypeType_Info() {
		return (EAttribute)outputWidthTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPolarCartesianInterpolationType() {
		return polarCartesianInterpolationTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPolarCartesianInterpolationType_Value() {
		return (EAttribute)polarCartesianInterpolationTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPolarCartesianInterpolationType_Done() {
		return (EAttribute)polarCartesianInterpolationTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPolarCartesianInterpolationType_Info() {
		return (EAttribute)polarCartesianInterpolationTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPreprocessingType() {
		return preprocessingTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPreprocessingType_HighPeaksBefore() {
		return (EReference)preprocessingTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPreprocessingType_RingArtefacts() {
		return (EReference)preprocessingTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPreprocessingType_Intensity() {
		return (EReference)preprocessingTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPreprocessingType_HighPeaksAfterRows() {
		return (EReference)preprocessingTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPreprocessingType_HighPeaksAfterColumns() {
		return (EReference)preprocessingTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getProfileTypeType() {
		return profileTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getProfileTypeType_Value() {
		return (EAttribute)profileTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getProfileTypeType_Info() {
		return (EAttribute)profileTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getProfileTypeType1() {
		return profileTypeType1EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getProfileTypeType1_Value() {
		return (EAttribute)profileTypeType1EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getProfileTypeType1_Info() {
		return (EAttribute)profileTypeType1EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRawType() {
		return rawTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRawType_Type() {
		return (EReference)rawTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRawType_Bits() {
		return (EAttribute)rawTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRawType_Offset() {
		return (EReference)rawTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRawType_ByteOrder() {
		return (EReference)rawTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRawType_Xlen() {
		return (EAttribute)rawTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRawType_Ylen() {
		return (EAttribute)rawTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRawType_Zlen() {
		return (EAttribute)rawTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRawType_Gap() {
		return (EReference)rawTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRawType_Done() {
		return (EAttribute)rawTypeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRestrictionsType() {
		return restrictionsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRestrictionsType_Value() {
		return (EAttribute)restrictionsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRestrictionsType_Info() {
		return (EAttribute)restrictionsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRestrictionsType1() {
		return restrictionsType1EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRestrictionsType1_Value() {
		return (EAttribute)restrictionsType1EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRestrictionsType1_Info() {
		return (EAttribute)restrictionsType1EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRingArtefactsType() {
		return ringArtefactsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRingArtefactsType_Type() {
		return (EReference)ringArtefactsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRingArtefactsType_ParameterN() {
		return (EAttribute)ringArtefactsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRingArtefactsType_ParameterR() {
		return (EAttribute)ringArtefactsTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRingArtefactsType_NumSeries() {
		return (EReference)ringArtefactsTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getROIType() {
		return roiTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getROIType_Type() {
		return (EReference)roiTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getROIType_Xmin() {
		return (EAttribute)roiTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getROIType_Xmax() {
		return (EAttribute)roiTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getROIType_Ymin() {
		return (EAttribute)roiTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getROIType_Ymax() {
		return (EAttribute)roiTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getROIType_OutputWidthType() {
		return (EReference)roiTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getROIType_OutputWidth() {
		return (EAttribute)roiTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getROIType_Angle() {
		return (EAttribute)roiTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRotationAngleEndPointsType() {
		return rotationAngleEndPointsTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRotationAngleEndPointsType_Value() {
		return (EAttribute)rotationAngleEndPointsTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRotationAngleEndPointsType_Info() {
		return (EAttribute)rotationAngleEndPointsTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRotationAngleTypeType() {
		return rotationAngleTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRotationAngleTypeType_Value() {
		return (EAttribute)rotationAngleTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRotationAngleTypeType_Info() {
		return (EAttribute)rotationAngleTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getScaleTypeType() {
		return scaleTypeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getScaleTypeType_Value() {
		return (EAttribute)scaleTypeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getScaleTypeType_Info() {
		return (EAttribute)scaleTypeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getShapeType() {
		return shapeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShapeType_Value() {
		return (EAttribute)shapeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShapeType_Done() {
		return (EAttribute)shapeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShapeType_Info() {
		return (EAttribute)shapeTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getShapeType1() {
		return shapeType1EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShapeType1_Value() {
		return (EAttribute)shapeType1EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getShapeType1_Info() {
		return (EAttribute)shapeType1EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getStateType() {
		return stateTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStateType_Value() {
		return (EAttribute)stateTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStateType_Info() {
		return (EAttribute)stateTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTiltType() {
		return tiltTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTiltType_Type() {
		return (EReference)tiltTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTiltType_XTilt() {
		return (EAttribute)tiltTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTiltType_ZTilt() {
		return (EAttribute)tiltTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTiltType_Done() {
		return (EAttribute)tiltTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTransformType() {
		return transformTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_MissedProjections() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_MissedProjectionsType() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_RotationAngleType() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_RotationAngle() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_RotationAngleEndPoints() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_ReCentreAngle() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_ReCentreRadius() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_CropTop() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_CropBottom() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_CropLeft() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_CropRight() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_ScaleType() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_ScaleWidth() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_ScaleHeight() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_ExtrapolationType() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_ExtrapolationPixels() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTransformType_ExtrapolationWidth() {
		return (EAttribute)transformTypeEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTransformType_Interpolation() {
		return (EReference)transformTypeEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType() {
		return typeTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType_Value() {
		return (EAttribute)typeTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType_Info() {
		return (EAttribute)typeTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType1() {
		return typeType1EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType1_Value() {
		return (EAttribute)typeType1EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType1_Info() {
		return (EAttribute)typeType1EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType2() {
		return typeType2EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType2_Value() {
		return (EAttribute)typeType2EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType2_Info() {
		return (EAttribute)typeType2EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType3() {
		return typeType3EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType3_Value() {
		return (EAttribute)typeType3EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType3_Info() {
		return (EAttribute)typeType3EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType4() {
		return typeType4EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType4_Value() {
		return (EAttribute)typeType4EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType4_Info() {
		return (EAttribute)typeType4EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType5() {
		return typeType5EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType5_Value() {
		return (EAttribute)typeType5EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType5_Info() {
		return (EAttribute)typeType5EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType6() {
		return typeType6EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType6_Value() {
		return (EAttribute)typeType6EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType6_Info() {
		return (EAttribute)typeType6EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType7() {
		return typeType7EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType7_Value() {
		return (EAttribute)typeType7EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType7_Info() {
		return (EAttribute)typeType7EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType8() {
		return typeType8EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType8_Value() {
		return (EAttribute)typeType8EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType8_Info() {
		return (EAttribute)typeType8EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType9() {
		return typeType9EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType9_Value() {
		return (EAttribute)typeType9EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType9_Info() {
		return (EAttribute)typeType9EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType10() {
		return typeType10EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType10_Value() {
		return (EAttribute)typeType10EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType10_Info() {
		return (EAttribute)typeType10EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType11() {
		return typeType11EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType11_Value() {
		return (EAttribute)typeType11EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType11_Info() {
		return (EAttribute)typeType11EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType12() {
		return typeType12EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType12_Value() {
		return (EAttribute)typeType12EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType12_Info() {
		return (EAttribute)typeType12EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType13() {
		return typeType13EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType13_Value() {
		return (EAttribute)typeType13EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType13_Info() {
		return (EAttribute)typeType13EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType14() {
		return typeType14EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType14_Value() {
		return (EAttribute)typeType14EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType14_Info() {
		return (EAttribute)typeType14EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType15() {
		return typeType15EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType15_Value() {
		return (EAttribute)typeType15EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType15_Info() {
		return (EAttribute)typeType15EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType16() {
		return typeType16EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType16_Value() {
		return (EAttribute)typeType16EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType16_Info() {
		return (EAttribute)typeType16EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTypeType17() {
		return typeType17EClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType17_Value() {
		return (EAttribute)typeType17EClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTypeType17_Info() {
		return (EAttribute)typeType17EClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getValueMaxType() {
		return valueMaxTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getValueMaxType_Type() {
		return (EReference)valueMaxTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueMaxType_Percent() {
		return (EAttribute)valueMaxTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueMaxType_Pixel() {
		return (EAttribute)valueMaxTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getValueMinType() {
		return valueMinTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getValueMinType_Type() {
		return (EReference)valueMinTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueMinType_Percent() {
		return (EAttribute)valueMinTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueMinType_Pixel() {
		return (EAttribute)valueMinTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getValueStepType() {
		return valueStepTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getValueStepType_Type() {
		return (EReference)valueStepTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueStepType_Percent() {
		return (EAttribute)valueStepTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getValueStepType_Pixel() {
		return (EAttribute)valueStepTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getWindowNameType() {
		return windowNameTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWindowNameType_Value() {
		return (EAttribute)windowNameTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWindowNameType_Info() {
		return (EAttribute)windowNameTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HmFactory getHmFactory() {
		return (HmFactory)getEFactoryInstance();
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
		backprojectionTypeEClass = createEClass(BACKPROJECTION_TYPE);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__FILTER);
		createEAttribute(backprojectionTypeEClass, BACKPROJECTION_TYPE__IMAGE_CENTRE);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__CLOCKWISE_ROTATION);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__TILT);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__COORDINATE_SYSTEM);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__CIRCLES);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__ROI);
		createEReference(backprojectionTypeEClass, BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION);

		beamlineUserTypeEClass = createEClass(BEAMLINE_USER_TYPE);
		createEReference(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__TYPE);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__BEAMLINE_NAME);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__YEAR);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__MONTH);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__DATE);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__VISIT_NUMBER);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER);
		createEAttribute(beamlineUserTypeEClass, BEAMLINE_USER_TYPE__DONE);

		bitsTypeTypeEClass = createEClass(BITS_TYPE_TYPE);
		createEAttribute(bitsTypeTypeEClass, BITS_TYPE_TYPE__VALUE);
		createEAttribute(bitsTypeTypeEClass, BITS_TYPE_TYPE__INFO);

		byteOrderTypeEClass = createEClass(BYTE_ORDER_TYPE);
		createEAttribute(byteOrderTypeEClass, BYTE_ORDER_TYPE__VALUE);
		createEAttribute(byteOrderTypeEClass, BYTE_ORDER_TYPE__INFO);

		circlesTypeEClass = createEClass(CIRCLES_TYPE);
		createEReference(circlesTypeEClass, CIRCLES_TYPE__VALUE_MIN);
		createEReference(circlesTypeEClass, CIRCLES_TYPE__VALUE_MAX);
		createEReference(circlesTypeEClass, CIRCLES_TYPE__VALUE_STEP);
		createEAttribute(circlesTypeEClass, CIRCLES_TYPE__COMM);

		clockwiseRotationTypeEClass = createEClass(CLOCKWISE_ROTATION_TYPE);
		createEAttribute(clockwiseRotationTypeEClass, CLOCKWISE_ROTATION_TYPE__VALUE);
		createEAttribute(clockwiseRotationTypeEClass, CLOCKWISE_ROTATION_TYPE__DONE);
		createEAttribute(clockwiseRotationTypeEClass, CLOCKWISE_ROTATION_TYPE__INFO);

		coordinateSystemTypeEClass = createEClass(COORDINATE_SYSTEM_TYPE);
		createEReference(coordinateSystemTypeEClass, COORDINATE_SYSTEM_TYPE__TYPE);
		createEAttribute(coordinateSystemTypeEClass, COORDINATE_SYSTEM_TYPE__SLICE);
		createEAttribute(coordinateSystemTypeEClass, COORDINATE_SYSTEM_TYPE__DONE);

		darkFieldTypeEClass = createEClass(DARK_FIELD_TYPE);
		createEReference(darkFieldTypeEClass, DARK_FIELD_TYPE__TYPE);
		createEAttribute(darkFieldTypeEClass, DARK_FIELD_TYPE__VALUE_BEFORE);
		createEAttribute(darkFieldTypeEClass, DARK_FIELD_TYPE__VALUE_AFTER);
		createEAttribute(darkFieldTypeEClass, DARK_FIELD_TYPE__FILE_BEFORE);
		createEAttribute(darkFieldTypeEClass, DARK_FIELD_TYPE__FILE_AFTER);
		createEReference(darkFieldTypeEClass, DARK_FIELD_TYPE__PROFILE_TYPE);
		createEAttribute(darkFieldTypeEClass, DARK_FIELD_TYPE__FILE_PROFILE);

		defaultXmlTypeEClass = createEClass(DEFAULT_XML_TYPE);
		createEAttribute(defaultXmlTypeEClass, DEFAULT_XML_TYPE__VALUE);
		createEAttribute(defaultXmlTypeEClass, DEFAULT_XML_TYPE__DONE);

		documentRootEClass = createEClass(DOCUMENT_ROOT);
		createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
		createEReference(documentRootEClass, DOCUMENT_ROOT__HMXML);

		extrapolationTypeTypeEClass = createEClass(EXTRAPOLATION_TYPE_TYPE);
		createEAttribute(extrapolationTypeTypeEClass, EXTRAPOLATION_TYPE_TYPE__VALUE);
		createEAttribute(extrapolationTypeTypeEClass, EXTRAPOLATION_TYPE_TYPE__INFO);

		fbpTypeEClass = createEClass(FBP_TYPE);
		createEReference(fbpTypeEClass, FBP_TYPE__DEFAULT_XML);
		createEAttribute(fbpTypeEClass, FBP_TYPE__GPU_DEVICE_NUMBER);
		createEReference(fbpTypeEClass, FBP_TYPE__BEAMLINE_USER);
		createEAttribute(fbpTypeEClass, FBP_TYPE__LOG_FILE);
		createEReference(fbpTypeEClass, FBP_TYPE__INPUT_DATA);
		createEReference(fbpTypeEClass, FBP_TYPE__FLAT_DARK_FIELDS);
		createEReference(fbpTypeEClass, FBP_TYPE__PREPROCESSING);
		createEReference(fbpTypeEClass, FBP_TYPE__TRANSFORM);
		createEReference(fbpTypeEClass, FBP_TYPE__BACKPROJECTION);
		createEReference(fbpTypeEClass, FBP_TYPE__OUTPUT_DATA);

		filterTypeEClass = createEClass(FILTER_TYPE);
		createEReference(filterTypeEClass, FILTER_TYPE__TYPE);
		createEReference(filterTypeEClass, FILTER_TYPE__NAME);
		createEAttribute(filterTypeEClass, FILTER_TYPE__BANDWIDTH);
		createEReference(filterTypeEClass, FILTER_TYPE__WINDOW_NAME);
		createEReference(filterTypeEClass, FILTER_TYPE__NORMALISATION);
		createEAttribute(filterTypeEClass, FILTER_TYPE__PIXEL_SIZE);

		firstImageIndexTypeEClass = createEClass(FIRST_IMAGE_INDEX_TYPE);
		createEAttribute(firstImageIndexTypeEClass, FIRST_IMAGE_INDEX_TYPE__VALUE);
		createEAttribute(firstImageIndexTypeEClass, FIRST_IMAGE_INDEX_TYPE__INFO);

		flatDarkFieldsTypeEClass = createEClass(FLAT_DARK_FIELDS_TYPE);
		createEReference(flatDarkFieldsTypeEClass, FLAT_DARK_FIELDS_TYPE__FLAT_FIELD);
		createEReference(flatDarkFieldsTypeEClass, FLAT_DARK_FIELDS_TYPE__DARK_FIELD);

		flatFieldTypeEClass = createEClass(FLAT_FIELD_TYPE);
		createEReference(flatFieldTypeEClass, FLAT_FIELD_TYPE__TYPE);
		createEAttribute(flatFieldTypeEClass, FLAT_FIELD_TYPE__VALUE_BEFORE);
		createEAttribute(flatFieldTypeEClass, FLAT_FIELD_TYPE__VALUE_AFTER);
		createEAttribute(flatFieldTypeEClass, FLAT_FIELD_TYPE__FILE_BEFORE);
		createEAttribute(flatFieldTypeEClass, FLAT_FIELD_TYPE__FILE_AFTER);
		createEReference(flatFieldTypeEClass, FLAT_FIELD_TYPE__PROFILE_TYPE);
		createEAttribute(flatFieldTypeEClass, FLAT_FIELD_TYPE__FILE_PROFILE);

		gapTypeEClass = createEClass(GAP_TYPE);
		createEAttribute(gapTypeEClass, GAP_TYPE__VALUE);
		createEAttribute(gapTypeEClass, GAP_TYPE__INFO);

		highPeaksAfterColumnsTypeEClass = createEClass(HIGH_PEAKS_AFTER_COLUMNS_TYPE);
		createEReference(highPeaksAfterColumnsTypeEClass, HIGH_PEAKS_AFTER_COLUMNS_TYPE__TYPE);
		createEAttribute(highPeaksAfterColumnsTypeEClass, HIGH_PEAKS_AFTER_COLUMNS_TYPE__NUMBER_PIXELS);
		createEAttribute(highPeaksAfterColumnsTypeEClass, HIGH_PEAKS_AFTER_COLUMNS_TYPE__JUMP);

		highPeaksAfterRowsTypeEClass = createEClass(HIGH_PEAKS_AFTER_ROWS_TYPE);
		createEReference(highPeaksAfterRowsTypeEClass, HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE);
		createEAttribute(highPeaksAfterRowsTypeEClass, HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS);
		createEAttribute(highPeaksAfterRowsTypeEClass, HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP);

		highPeaksBeforeTypeEClass = createEClass(HIGH_PEAKS_BEFORE_TYPE);
		createEReference(highPeaksBeforeTypeEClass, HIGH_PEAKS_BEFORE_TYPE__TYPE);
		createEAttribute(highPeaksBeforeTypeEClass, HIGH_PEAKS_BEFORE_TYPE__NUMBER_PIXELS);
		createEAttribute(highPeaksBeforeTypeEClass, HIGH_PEAKS_BEFORE_TYPE__JUMP);

		hMxmlTypeEClass = createEClass(HMXML_TYPE);
		createEReference(hMxmlTypeEClass, HMXML_TYPE__FBP);

		imageFirstTypeEClass = createEClass(IMAGE_FIRST_TYPE);
		createEAttribute(imageFirstTypeEClass, IMAGE_FIRST_TYPE__VALUE);
		createEAttribute(imageFirstTypeEClass, IMAGE_FIRST_TYPE__DONE);

		imageLastTypeEClass = createEClass(IMAGE_LAST_TYPE);
		createEAttribute(imageLastTypeEClass, IMAGE_LAST_TYPE__VALUE);
		createEAttribute(imageLastTypeEClass, IMAGE_LAST_TYPE__DONE);

		imageStepTypeEClass = createEClass(IMAGE_STEP_TYPE);
		createEAttribute(imageStepTypeEClass, IMAGE_STEP_TYPE__VALUE);
		createEAttribute(imageStepTypeEClass, IMAGE_STEP_TYPE__DONE);

		inputDataTypeEClass = createEClass(INPUT_DATA_TYPE);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__FOLDER);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__PREFIX);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__SUFFIX);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__EXTENSION);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__NOD);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__MEMORY_SIZE_MAX);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__MEMORY_SIZE_MIN);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__ORIENTATION);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__FILE_FIRST);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__FILE_LAST);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__FILE_STEP);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__IMAGE_FIRST);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__IMAGE_LAST);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__IMAGE_STEP);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__RAW);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__FIRST_IMAGE_INDEX);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__IMAGES_PER_FILE);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__RESTRICTIONS);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__VALUE_MIN);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__VALUE_MAX);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__TYPE);
		createEReference(inputDataTypeEClass, INPUT_DATA_TYPE__SHAPE);
		createEAttribute(inputDataTypeEClass, INPUT_DATA_TYPE__PIXEL_PARAM);

		intensityTypeEClass = createEClass(INTENSITY_TYPE);
		createEReference(intensityTypeEClass, INTENSITY_TYPE__TYPE);
		createEAttribute(intensityTypeEClass, INTENSITY_TYPE__COLUMN_LEFT);
		createEAttribute(intensityTypeEClass, INTENSITY_TYPE__COLUMN_RIGHT);
		createEAttribute(intensityTypeEClass, INTENSITY_TYPE__ZERO_LEFT);
		createEAttribute(intensityTypeEClass, INTENSITY_TYPE__ZERO_RIGHT);

		interpolationTypeEClass = createEClass(INTERPOLATION_TYPE);
		createEAttribute(interpolationTypeEClass, INTERPOLATION_TYPE__VALUE);
		createEAttribute(interpolationTypeEClass, INTERPOLATION_TYPE__INFO);

		memorySizeMaxTypeEClass = createEClass(MEMORY_SIZE_MAX_TYPE);
		createEAttribute(memorySizeMaxTypeEClass, MEMORY_SIZE_MAX_TYPE__VALUE);
		createEAttribute(memorySizeMaxTypeEClass, MEMORY_SIZE_MAX_TYPE__INFO);

		memorySizeMinTypeEClass = createEClass(MEMORY_SIZE_MIN_TYPE);
		createEAttribute(memorySizeMinTypeEClass, MEMORY_SIZE_MIN_TYPE__VALUE);
		createEAttribute(memorySizeMinTypeEClass, MEMORY_SIZE_MIN_TYPE__INFO);

		missedProjectionsTypeEClass = createEClass(MISSED_PROJECTIONS_TYPE);
		createEAttribute(missedProjectionsTypeEClass, MISSED_PROJECTIONS_TYPE__VALUE);
		createEAttribute(missedProjectionsTypeEClass, MISSED_PROJECTIONS_TYPE__INFO);

		missedProjectionsTypeTypeEClass = createEClass(MISSED_PROJECTIONS_TYPE_TYPE);
		createEAttribute(missedProjectionsTypeTypeEClass, MISSED_PROJECTIONS_TYPE_TYPE__VALUE);
		createEAttribute(missedProjectionsTypeTypeEClass, MISSED_PROJECTIONS_TYPE_TYPE__INFO);

		nameTypeEClass = createEClass(NAME_TYPE);
		createEAttribute(nameTypeEClass, NAME_TYPE__VALUE);
		createEAttribute(nameTypeEClass, NAME_TYPE__INFO);

		nodTypeEClass = createEClass(NOD_TYPE);
		createEAttribute(nodTypeEClass, NOD_TYPE__VALUE);
		createEAttribute(nodTypeEClass, NOD_TYPE__INFO);

		normalisationTypeEClass = createEClass(NORMALISATION_TYPE);
		createEAttribute(normalisationTypeEClass, NORMALISATION_TYPE__VALUE);
		createEAttribute(normalisationTypeEClass, NORMALISATION_TYPE__INFO);

		numSeriesTypeEClass = createEClass(NUM_SERIES_TYPE);
		createEAttribute(numSeriesTypeEClass, NUM_SERIES_TYPE__VALUE);
		createEAttribute(numSeriesTypeEClass, NUM_SERIES_TYPE__INFO);

		offsetTypeEClass = createEClass(OFFSET_TYPE);
		createEAttribute(offsetTypeEClass, OFFSET_TYPE__VALUE);
		createEAttribute(offsetTypeEClass, OFFSET_TYPE__INFO);

		orientationTypeEClass = createEClass(ORIENTATION_TYPE);
		createEAttribute(orientationTypeEClass, ORIENTATION_TYPE__VALUE);
		createEAttribute(orientationTypeEClass, ORIENTATION_TYPE__DONE);
		createEAttribute(orientationTypeEClass, ORIENTATION_TYPE__INFO);

		outputDataTypeEClass = createEClass(OUTPUT_DATA_TYPE);
		createEReference(outputDataTypeEClass, OUTPUT_DATA_TYPE__TYPE);
		createEReference(outputDataTypeEClass, OUTPUT_DATA_TYPE__STATE);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__FOLDER);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__PREFIX);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__SUFFIX);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__EXTENSION);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__NOD);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__FILE_FIRST);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__FILE_STEP);
		createEReference(outputDataTypeEClass, OUTPUT_DATA_TYPE__BITS_TYPE);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__BITS);
		createEReference(outputDataTypeEClass, OUTPUT_DATA_TYPE__RESTRICTIONS);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__VALUE_MIN);
		createEAttribute(outputDataTypeEClass, OUTPUT_DATA_TYPE__VALUE_MAX);
		createEReference(outputDataTypeEClass, OUTPUT_DATA_TYPE__SHAPE);

		outputWidthTypeTypeEClass = createEClass(OUTPUT_WIDTH_TYPE_TYPE);
		createEAttribute(outputWidthTypeTypeEClass, OUTPUT_WIDTH_TYPE_TYPE__VALUE);
		createEAttribute(outputWidthTypeTypeEClass, OUTPUT_WIDTH_TYPE_TYPE__INFO);

		polarCartesianInterpolationTypeEClass = createEClass(POLAR_CARTESIAN_INTERPOLATION_TYPE);
		createEAttribute(polarCartesianInterpolationTypeEClass, POLAR_CARTESIAN_INTERPOLATION_TYPE__VALUE);
		createEAttribute(polarCartesianInterpolationTypeEClass, POLAR_CARTESIAN_INTERPOLATION_TYPE__DONE);
		createEAttribute(polarCartesianInterpolationTypeEClass, POLAR_CARTESIAN_INTERPOLATION_TYPE__INFO);

		preprocessingTypeEClass = createEClass(PREPROCESSING_TYPE);
		createEReference(preprocessingTypeEClass, PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE);
		createEReference(preprocessingTypeEClass, PREPROCESSING_TYPE__RING_ARTEFACTS);
		createEReference(preprocessingTypeEClass, PREPROCESSING_TYPE__INTENSITY);
		createEReference(preprocessingTypeEClass, PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS);
		createEReference(preprocessingTypeEClass, PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS);

		profileTypeTypeEClass = createEClass(PROFILE_TYPE_TYPE);
		createEAttribute(profileTypeTypeEClass, PROFILE_TYPE_TYPE__VALUE);
		createEAttribute(profileTypeTypeEClass, PROFILE_TYPE_TYPE__INFO);

		profileTypeType1EClass = createEClass(PROFILE_TYPE_TYPE1);
		createEAttribute(profileTypeType1EClass, PROFILE_TYPE_TYPE1__VALUE);
		createEAttribute(profileTypeType1EClass, PROFILE_TYPE_TYPE1__INFO);

		rawTypeEClass = createEClass(RAW_TYPE);
		createEReference(rawTypeEClass, RAW_TYPE__TYPE);
		createEAttribute(rawTypeEClass, RAW_TYPE__BITS);
		createEReference(rawTypeEClass, RAW_TYPE__OFFSET);
		createEReference(rawTypeEClass, RAW_TYPE__BYTE_ORDER);
		createEAttribute(rawTypeEClass, RAW_TYPE__XLEN);
		createEAttribute(rawTypeEClass, RAW_TYPE__YLEN);
		createEAttribute(rawTypeEClass, RAW_TYPE__ZLEN);
		createEReference(rawTypeEClass, RAW_TYPE__GAP);
		createEAttribute(rawTypeEClass, RAW_TYPE__DONE);

		restrictionsTypeEClass = createEClass(RESTRICTIONS_TYPE);
		createEAttribute(restrictionsTypeEClass, RESTRICTIONS_TYPE__VALUE);
		createEAttribute(restrictionsTypeEClass, RESTRICTIONS_TYPE__INFO);

		restrictionsType1EClass = createEClass(RESTRICTIONS_TYPE1);
		createEAttribute(restrictionsType1EClass, RESTRICTIONS_TYPE1__VALUE);
		createEAttribute(restrictionsType1EClass, RESTRICTIONS_TYPE1__INFO);

		ringArtefactsTypeEClass = createEClass(RING_ARTEFACTS_TYPE);
		createEReference(ringArtefactsTypeEClass, RING_ARTEFACTS_TYPE__TYPE);
		createEAttribute(ringArtefactsTypeEClass, RING_ARTEFACTS_TYPE__PARAMETER_N);
		createEAttribute(ringArtefactsTypeEClass, RING_ARTEFACTS_TYPE__PARAMETER_R);
		createEReference(ringArtefactsTypeEClass, RING_ARTEFACTS_TYPE__NUM_SERIES);

		roiTypeEClass = createEClass(ROI_TYPE);
		createEReference(roiTypeEClass, ROI_TYPE__TYPE);
		createEAttribute(roiTypeEClass, ROI_TYPE__XMIN);
		createEAttribute(roiTypeEClass, ROI_TYPE__XMAX);
		createEAttribute(roiTypeEClass, ROI_TYPE__YMIN);
		createEAttribute(roiTypeEClass, ROI_TYPE__YMAX);
		createEReference(roiTypeEClass, ROI_TYPE__OUTPUT_WIDTH_TYPE);
		createEAttribute(roiTypeEClass, ROI_TYPE__OUTPUT_WIDTH);
		createEAttribute(roiTypeEClass, ROI_TYPE__ANGLE);

		rotationAngleEndPointsTypeEClass = createEClass(ROTATION_ANGLE_END_POINTS_TYPE);
		createEAttribute(rotationAngleEndPointsTypeEClass, ROTATION_ANGLE_END_POINTS_TYPE__VALUE);
		createEAttribute(rotationAngleEndPointsTypeEClass, ROTATION_ANGLE_END_POINTS_TYPE__INFO);

		rotationAngleTypeTypeEClass = createEClass(ROTATION_ANGLE_TYPE_TYPE);
		createEAttribute(rotationAngleTypeTypeEClass, ROTATION_ANGLE_TYPE_TYPE__VALUE);
		createEAttribute(rotationAngleTypeTypeEClass, ROTATION_ANGLE_TYPE_TYPE__INFO);

		scaleTypeTypeEClass = createEClass(SCALE_TYPE_TYPE);
		createEAttribute(scaleTypeTypeEClass, SCALE_TYPE_TYPE__VALUE);
		createEAttribute(scaleTypeTypeEClass, SCALE_TYPE_TYPE__INFO);

		shapeTypeEClass = createEClass(SHAPE_TYPE);
		createEAttribute(shapeTypeEClass, SHAPE_TYPE__VALUE);
		createEAttribute(shapeTypeEClass, SHAPE_TYPE__DONE);
		createEAttribute(shapeTypeEClass, SHAPE_TYPE__INFO);

		shapeType1EClass = createEClass(SHAPE_TYPE1);
		createEAttribute(shapeType1EClass, SHAPE_TYPE1__VALUE);
		createEAttribute(shapeType1EClass, SHAPE_TYPE1__INFO);

		stateTypeEClass = createEClass(STATE_TYPE);
		createEAttribute(stateTypeEClass, STATE_TYPE__VALUE);
		createEAttribute(stateTypeEClass, STATE_TYPE__INFO);

		tiltTypeEClass = createEClass(TILT_TYPE);
		createEReference(tiltTypeEClass, TILT_TYPE__TYPE);
		createEAttribute(tiltTypeEClass, TILT_TYPE__XTILT);
		createEAttribute(tiltTypeEClass, TILT_TYPE__ZTILT);
		createEAttribute(tiltTypeEClass, TILT_TYPE__DONE);

		transformTypeEClass = createEClass(TRANSFORM_TYPE);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__MISSED_PROJECTIONS);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__ROTATION_ANGLE_TYPE);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__ROTATION_ANGLE);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__RE_CENTRE_ANGLE);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__RE_CENTRE_RADIUS);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__CROP_TOP);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__CROP_BOTTOM);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__CROP_LEFT);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__CROP_RIGHT);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__SCALE_TYPE);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__SCALE_WIDTH);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__SCALE_HEIGHT);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__EXTRAPOLATION_TYPE);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__EXTRAPOLATION_PIXELS);
		createEAttribute(transformTypeEClass, TRANSFORM_TYPE__EXTRAPOLATION_WIDTH);
		createEReference(transformTypeEClass, TRANSFORM_TYPE__INTERPOLATION);

		typeTypeEClass = createEClass(TYPE_TYPE);
		createEAttribute(typeTypeEClass, TYPE_TYPE__VALUE);
		createEAttribute(typeTypeEClass, TYPE_TYPE__INFO);

		typeType1EClass = createEClass(TYPE_TYPE1);
		createEAttribute(typeType1EClass, TYPE_TYPE1__VALUE);
		createEAttribute(typeType1EClass, TYPE_TYPE1__INFO);

		typeType2EClass = createEClass(TYPE_TYPE2);
		createEAttribute(typeType2EClass, TYPE_TYPE2__VALUE);
		createEAttribute(typeType2EClass, TYPE_TYPE2__INFO);

		typeType3EClass = createEClass(TYPE_TYPE3);
		createEAttribute(typeType3EClass, TYPE_TYPE3__VALUE);
		createEAttribute(typeType3EClass, TYPE_TYPE3__INFO);

		typeType4EClass = createEClass(TYPE_TYPE4);
		createEAttribute(typeType4EClass, TYPE_TYPE4__VALUE);
		createEAttribute(typeType4EClass, TYPE_TYPE4__INFO);

		typeType5EClass = createEClass(TYPE_TYPE5);
		createEAttribute(typeType5EClass, TYPE_TYPE5__VALUE);
		createEAttribute(typeType5EClass, TYPE_TYPE5__INFO);

		typeType6EClass = createEClass(TYPE_TYPE6);
		createEAttribute(typeType6EClass, TYPE_TYPE6__VALUE);
		createEAttribute(typeType6EClass, TYPE_TYPE6__INFO);

		typeType7EClass = createEClass(TYPE_TYPE7);
		createEAttribute(typeType7EClass, TYPE_TYPE7__VALUE);
		createEAttribute(typeType7EClass, TYPE_TYPE7__INFO);

		typeType8EClass = createEClass(TYPE_TYPE8);
		createEAttribute(typeType8EClass, TYPE_TYPE8__VALUE);
		createEAttribute(typeType8EClass, TYPE_TYPE8__INFO);

		typeType9EClass = createEClass(TYPE_TYPE9);
		createEAttribute(typeType9EClass, TYPE_TYPE9__VALUE);
		createEAttribute(typeType9EClass, TYPE_TYPE9__INFO);

		typeType10EClass = createEClass(TYPE_TYPE10);
		createEAttribute(typeType10EClass, TYPE_TYPE10__VALUE);
		createEAttribute(typeType10EClass, TYPE_TYPE10__INFO);

		typeType11EClass = createEClass(TYPE_TYPE11);
		createEAttribute(typeType11EClass, TYPE_TYPE11__VALUE);
		createEAttribute(typeType11EClass, TYPE_TYPE11__INFO);

		typeType12EClass = createEClass(TYPE_TYPE12);
		createEAttribute(typeType12EClass, TYPE_TYPE12__VALUE);
		createEAttribute(typeType12EClass, TYPE_TYPE12__INFO);

		typeType13EClass = createEClass(TYPE_TYPE13);
		createEAttribute(typeType13EClass, TYPE_TYPE13__VALUE);
		createEAttribute(typeType13EClass, TYPE_TYPE13__INFO);

		typeType14EClass = createEClass(TYPE_TYPE14);
		createEAttribute(typeType14EClass, TYPE_TYPE14__VALUE);
		createEAttribute(typeType14EClass, TYPE_TYPE14__INFO);

		typeType15EClass = createEClass(TYPE_TYPE15);
		createEAttribute(typeType15EClass, TYPE_TYPE15__VALUE);
		createEAttribute(typeType15EClass, TYPE_TYPE15__INFO);

		typeType16EClass = createEClass(TYPE_TYPE16);
		createEAttribute(typeType16EClass, TYPE_TYPE16__VALUE);
		createEAttribute(typeType16EClass, TYPE_TYPE16__INFO);

		typeType17EClass = createEClass(TYPE_TYPE17);
		createEAttribute(typeType17EClass, TYPE_TYPE17__VALUE);
		createEAttribute(typeType17EClass, TYPE_TYPE17__INFO);

		valueMaxTypeEClass = createEClass(VALUE_MAX_TYPE);
		createEReference(valueMaxTypeEClass, VALUE_MAX_TYPE__TYPE);
		createEAttribute(valueMaxTypeEClass, VALUE_MAX_TYPE__PERCENT);
		createEAttribute(valueMaxTypeEClass, VALUE_MAX_TYPE__PIXEL);

		valueMinTypeEClass = createEClass(VALUE_MIN_TYPE);
		createEReference(valueMinTypeEClass, VALUE_MIN_TYPE__TYPE);
		createEAttribute(valueMinTypeEClass, VALUE_MIN_TYPE__PERCENT);
		createEAttribute(valueMinTypeEClass, VALUE_MIN_TYPE__PIXEL);

		valueStepTypeEClass = createEClass(VALUE_STEP_TYPE);
		createEReference(valueStepTypeEClass, VALUE_STEP_TYPE__TYPE);
		createEAttribute(valueStepTypeEClass, VALUE_STEP_TYPE__PERCENT);
		createEAttribute(valueStepTypeEClass, VALUE_STEP_TYPE__PIXEL);

		windowNameTypeEClass = createEClass(WINDOW_NAME_TYPE);
		createEAttribute(windowNameTypeEClass, WINDOW_NAME_TYPE__VALUE);
		createEAttribute(windowNameTypeEClass, WINDOW_NAME_TYPE__INFO);
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
		initEClass(backprojectionTypeEClass, BackprojectionType.class, "BackprojectionType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBackprojectionType_Filter(), this.getFilterType(), null, "filter", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBackprojectionType_ImageCentre(), theXMLTypePackage.getDecimal(), "imageCentre", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBackprojectionType_ClockwiseRotation(), this.getClockwiseRotationType(), null, "clockwiseRotation", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBackprojectionType_Tilt(), this.getTiltType(), null, "tilt", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBackprojectionType_CoordinateSystem(), this.getCoordinateSystemType(), null, "coordinateSystem", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBackprojectionType_Circles(), this.getCirclesType(), null, "circles", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBackprojectionType_ROI(), this.getROIType(), null, "rOI", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBackprojectionType_PolarCartesianInterpolation(), this.getPolarCartesianInterpolationType(), null, "polarCartesianInterpolation", null, 0, 1, BackprojectionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(beamlineUserTypeEClass, BeamlineUserType.class, "BeamlineUserType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBeamlineUserType_Type(), this.getTypeType17(), null, "type", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_BeamlineName(), theXMLTypePackage.getString(), "beamlineName", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_Year(), theXMLTypePackage.getString(), "year", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_Month(), theXMLTypePackage.getString(), "month", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_Date(), theXMLTypePackage.getString(), "date", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_VisitNumber(), theXMLTypePackage.getString(), "visitNumber", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_InputDataFolder(), theXMLTypePackage.getString(), "inputDataFolder", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_InputScanFolder(), theXMLTypePackage.getString(), "inputScanFolder", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_OutputDataFolder(), theXMLTypePackage.getString(), "outputDataFolder", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_OutputScanFolder(), theXMLTypePackage.getString(), "outputScanFolder", null, 0, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBeamlineUserType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, BeamlineUserType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(bitsTypeTypeEClass, BitsTypeType.class, "BitsTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBitsTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, BitsTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBitsTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, BitsTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(byteOrderTypeEClass, ByteOrderType.class, "ByteOrderType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getByteOrderType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ByteOrderType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getByteOrderType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ByteOrderType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(circlesTypeEClass, CirclesType.class, "CirclesType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCirclesType_ValueMin(), this.getValueMinType(), null, "valueMin", null, 0, 1, CirclesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCirclesType_ValueMax(), this.getValueMaxType(), null, "valueMax", null, 0, 1, CirclesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCirclesType_ValueStep(), this.getValueStepType(), null, "valueStep", null, 0, 1, CirclesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCirclesType_Comm(), theXMLTypePackage.getString(), "comm", null, 1, 1, CirclesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clockwiseRotationTypeEClass, ClockwiseRotationType.class, "ClockwiseRotationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getClockwiseRotationType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ClockwiseRotationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClockwiseRotationType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, ClockwiseRotationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClockwiseRotationType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ClockwiseRotationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(coordinateSystemTypeEClass, CoordinateSystemType.class, "CoordinateSystemType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCoordinateSystemType_Type(), this.getTypeType9(), null, "type", null, 0, 1, CoordinateSystemType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCoordinateSystemType_Slice(), theXMLTypePackage.getString(), "slice", null, 0, 1, CoordinateSystemType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCoordinateSystemType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, CoordinateSystemType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(darkFieldTypeEClass, DarkFieldType.class, "DarkFieldType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDarkFieldType_Type(), this.getTypeType13(), null, "type", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDarkFieldType_ValueBefore(), ecorePackage.getEDoubleObject(), "valueBefore", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDarkFieldType_ValueAfter(), ecorePackage.getEDoubleObject(), "valueAfter", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDarkFieldType_FileBefore(), theXMLTypePackage.getNormalizedString(), "fileBefore", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDarkFieldType_FileAfter(), theXMLTypePackage.getNormalizedString(), "fileAfter", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDarkFieldType_ProfileType(), this.getProfileTypeType(), null, "profileType", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDarkFieldType_FileProfile(), theXMLTypePackage.getString(), "fileProfile", null, 0, 1, DarkFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(defaultXmlTypeEClass, DefaultXmlType.class, "DefaultXmlType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDefaultXmlType_Value(), theXMLTypePackage.getString(), "value", null, 0, 1, DefaultXmlType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDefaultXmlType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, DefaultXmlType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_HMxml(), this.getHMxmlType(), null, "hMxml", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(extrapolationTypeTypeEClass, ExtrapolationTypeType.class, "ExtrapolationTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getExtrapolationTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ExtrapolationTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExtrapolationTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ExtrapolationTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fbpTypeEClass, FBPType.class, "FBPType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFBPType_DefaultXml(), this.getDefaultXmlType(), null, "defaultXml", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFBPType_GPUDeviceNumber(), theXMLTypePackage.getInt(), "gPUDeviceNumber", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_BeamlineUser(), this.getBeamlineUserType(), null, "beamlineUser", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFBPType_LogFile(), theXMLTypePackage.getNormalizedString(), "logFile", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_InputData(), this.getInputDataType(), null, "inputData", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_FlatDarkFields(), this.getFlatDarkFieldsType(), null, "flatDarkFields", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_Preprocessing(), this.getPreprocessingType(), null, "preprocessing", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_Transform(), this.getTransformType(), null, "transform", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_Backprojection(), this.getBackprojectionType(), null, "backprojection", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFBPType_OutputData(), this.getOutputDataType(), null, "outputData", null, 0, 1, FBPType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(filterTypeEClass, FilterType.class, "FilterType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFilterType_Type(), this.getTypeType7(), null, "type", null, 0, 1, FilterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFilterType_Name(), this.getNameType(), null, "name", null, 0, 1, FilterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFilterType_Bandwidth(), theXMLTypePackage.getDecimal(), "bandwidth", null, 0, 1, FilterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFilterType_WindowName(), this.getWindowNameType(), null, "windowName", null, 0, 1, FilterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFilterType_Normalisation(), this.getNormalisationType(), null, "normalisation", null, 0, 1, FilterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFilterType_PixelSize(), theXMLTypePackage.getDecimal(), "pixelSize", null, 0, 1, FilterType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(firstImageIndexTypeEClass, FirstImageIndexType.class, "FirstImageIndexType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFirstImageIndexType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, FirstImageIndexType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFirstImageIndexType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, FirstImageIndexType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(flatDarkFieldsTypeEClass, FlatDarkFieldsType.class, "FlatDarkFieldsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFlatDarkFieldsType_FlatField(), this.getFlatFieldType(), null, "flatField", null, 0, 1, FlatDarkFieldsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFlatDarkFieldsType_DarkField(), this.getDarkFieldType(), null, "darkField", null, 0, 1, FlatDarkFieldsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(flatFieldTypeEClass, FlatFieldType.class, "FlatFieldType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFlatFieldType_Type(), this.getTypeType15(), null, "type", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlatFieldType_ValueBefore(), ecorePackage.getEDoubleObject(), "valueBefore", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlatFieldType_ValueAfter(), ecorePackage.getEDoubleObject(), "valueAfter", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlatFieldType_FileBefore(), theXMLTypePackage.getNormalizedString(), "fileBefore", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlatFieldType_FileAfter(), theXMLTypePackage.getString(), "fileAfter", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFlatFieldType_ProfileType(), this.getProfileTypeType1(), null, "profileType", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlatFieldType_FileProfile(), theXMLTypePackage.getString(), "fileProfile", null, 0, 1, FlatFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(gapTypeEClass, GapType.class, "GapType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGapType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, GapType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGapType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, GapType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(highPeaksAfterColumnsTypeEClass, HighPeaksAfterColumnsType.class, "HighPeaksAfterColumnsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHighPeaksAfterColumnsType_Type(), this.getTypeType10(), null, "type", null, 0, 1, HighPeaksAfterColumnsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHighPeaksAfterColumnsType_NumberPixels(), theXMLTypePackage.getInt(), "numberPixels", null, 0, 1, HighPeaksAfterColumnsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHighPeaksAfterColumnsType_Jump(), theXMLTypePackage.getDecimal(), "jump", null, 0, 1, HighPeaksAfterColumnsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(highPeaksAfterRowsTypeEClass, HighPeaksAfterRowsType.class, "HighPeaksAfterRowsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHighPeaksAfterRowsType_Type(), this.getTypeType12(), null, "type", null, 0, 1, HighPeaksAfterRowsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHighPeaksAfterRowsType_NumberPixels(), theXMLTypePackage.getInt(), "numberPixels", null, 0, 1, HighPeaksAfterRowsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHighPeaksAfterRowsType_Jump(), theXMLTypePackage.getDecimal(), "jump", null, 0, 1, HighPeaksAfterRowsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(highPeaksBeforeTypeEClass, HighPeaksBeforeType.class, "HighPeaksBeforeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHighPeaksBeforeType_Type(), this.getTypeType1(), null, "type", null, 0, 1, HighPeaksBeforeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHighPeaksBeforeType_NumberPixels(), theXMLTypePackage.getInt(), "numberPixels", null, 0, 1, HighPeaksBeforeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHighPeaksBeforeType_Jump(), theXMLTypePackage.getDecimal(), "jump", null, 0, 1, HighPeaksBeforeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(hMxmlTypeEClass, HMxmlType.class, "HMxmlType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHMxmlType_FBP(), this.getFBPType(), null, "fBP", null, 0, 1, HMxmlType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(imageFirstTypeEClass, ImageFirstType.class, "ImageFirstType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImageFirstType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, ImageFirstType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImageFirstType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, ImageFirstType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(imageLastTypeEClass, ImageLastType.class, "ImageLastType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImageLastType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, ImageLastType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImageLastType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, ImageLastType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(imageStepTypeEClass, ImageStepType.class, "ImageStepType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImageStepType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, ImageStepType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImageStepType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, ImageStepType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(inputDataTypeEClass, InputDataType.class, "InputDataType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getInputDataType_Folder(), theXMLTypePackage.getNormalizedString(), "folder", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_Prefix(), theXMLTypePackage.getNormalizedString(), "prefix", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_Suffix(), theXMLTypePackage.getString(), "suffix", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_Extension(), theXMLTypePackage.getNormalizedString(), "extension", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_NOD(), this.getNODType(), null, "nOD", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_MemorySizeMax(), this.getMemorySizeMaxType(), null, "memorySizeMax", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_MemorySizeMin(), this.getMemorySizeMinType(), null, "memorySizeMin", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_Orientation(), this.getOrientationType(), null, "orientation", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_FileFirst(), theXMLTypePackage.getInt(), "fileFirst", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_FileLast(), theXMLTypePackage.getInt(), "fileLast", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_FileStep(), theXMLTypePackage.getInt(), "fileStep", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_ImageFirst(), this.getImageFirstType(), null, "imageFirst", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_ImageLast(), this.getImageLastType(), null, "imageLast", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_ImageStep(), this.getImageStepType(), null, "imageStep", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_Raw(), this.getRawType(), null, "raw", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_FirstImageIndex(), this.getFirstImageIndexType(), null, "firstImageIndex", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_ImagesPerFile(), theXMLTypePackage.getInt(), "imagesPerFile", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_Restrictions(), this.getRestrictionsType1(), null, "restrictions", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_ValueMin(), theXMLTypePackage.getDecimal(), "valueMin", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_ValueMax(), theXMLTypePackage.getDecimal(), "valueMax", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_Type(), this.getTypeType14(), null, "type", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getInputDataType_Shape(), this.getShapeType1(), null, "shape", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInputDataType_PixelParam(), theXMLTypePackage.getDecimal(), "pixelParam", null, 0, 1, InputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(intensityTypeEClass, IntensityType.class, "IntensityType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIntensityType_Type(), this.getTypeType6(), null, "type", null, 0, 1, IntensityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntensityType_ColumnLeft(), theXMLTypePackage.getString(), "columnLeft", null, 0, 1, IntensityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntensityType_ColumnRight(), theXMLTypePackage.getString(), "columnRight", null, 0, 1, IntensityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntensityType_ZeroLeft(), theXMLTypePackage.getInt(), "zeroLeft", null, 0, 1, IntensityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntensityType_ZeroRight(), theXMLTypePackage.getInt(), "zeroRight", null, 0, 1, IntensityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(interpolationTypeEClass, InterpolationType.class, "InterpolationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getInterpolationType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, InterpolationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInterpolationType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, InterpolationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(memorySizeMaxTypeEClass, MemorySizeMaxType.class, "MemorySizeMaxType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMemorySizeMaxType_Value(), theXMLTypePackage.getDecimal(), "value", null, 0, 1, MemorySizeMaxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMemorySizeMaxType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, MemorySizeMaxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(memorySizeMinTypeEClass, MemorySizeMinType.class, "MemorySizeMinType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMemorySizeMinType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, MemorySizeMinType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMemorySizeMinType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, MemorySizeMinType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(missedProjectionsTypeEClass, MissedProjectionsType.class, "MissedProjectionsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMissedProjectionsType_Value(), theXMLTypePackage.getString(), "value", null, 0, 1, MissedProjectionsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMissedProjectionsType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, MissedProjectionsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(missedProjectionsTypeTypeEClass, MissedProjectionsTypeType.class, "MissedProjectionsTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMissedProjectionsTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, MissedProjectionsTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMissedProjectionsTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, MissedProjectionsTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(nameTypeEClass, NameType.class, "NameType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNameType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, NameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getNameType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, NameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(nodTypeEClass, NODType.class, "NODType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNODType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, NODType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getNODType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, NODType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(normalisationTypeEClass, NormalisationType.class, "NormalisationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNormalisationType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, NormalisationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getNormalisationType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, NormalisationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(numSeriesTypeEClass, NumSeriesType.class, "NumSeriesType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNumSeriesType_Value(), theXMLTypePackage.getDecimal(), "value", null, 0, 1, NumSeriesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getNumSeriesType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, NumSeriesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(offsetTypeEClass, OffsetType.class, "OffsetType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOffsetType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, OffsetType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOffsetType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, OffsetType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(orientationTypeEClass, OrientationType.class, "OrientationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOrientationType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, OrientationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrientationType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, OrientationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrientationType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, OrientationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(outputDataTypeEClass, OutputDataType.class, "OutputDataType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getOutputDataType_Type(), this.getTypeType2(), null, "type", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOutputDataType_State(), this.getStateType(), null, "state", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_Folder(), theXMLTypePackage.getNormalizedString(), "folder", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_Prefix(), theXMLTypePackage.getNormalizedString(), "prefix", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_Suffix(), theXMLTypePackage.getString(), "suffix", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_Extension(), theXMLTypePackage.getNormalizedString(), "extension", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_NOD(), theXMLTypePackage.getInt(), "nOD", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_FileFirst(), theXMLTypePackage.getInt(), "fileFirst", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_FileStep(), theXMLTypePackage.getInt(), "fileStep", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOutputDataType_BitsType(), this.getBitsTypeType(), null, "bitsType", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_Bits(), theXMLTypePackage.getInt(), "bits", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOutputDataType_Restrictions(), this.getRestrictionsType(), null, "restrictions", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_ValueMin(), theXMLTypePackage.getDecimal(), "valueMin", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputDataType_ValueMax(), theXMLTypePackage.getDecimal(), "valueMax", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOutputDataType_Shape(), this.getShapeType(), null, "shape", null, 0, 1, OutputDataType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(outputWidthTypeTypeEClass, OutputWidthTypeType.class, "OutputWidthTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOutputWidthTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, OutputWidthTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOutputWidthTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, OutputWidthTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(polarCartesianInterpolationTypeEClass, PolarCartesianInterpolationType.class, "PolarCartesianInterpolationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPolarCartesianInterpolationType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, PolarCartesianInterpolationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolarCartesianInterpolationType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, PolarCartesianInterpolationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolarCartesianInterpolationType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, PolarCartesianInterpolationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(preprocessingTypeEClass, PreprocessingType.class, "PreprocessingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPreprocessingType_HighPeaksBefore(), this.getHighPeaksBeforeType(), null, "highPeaksBefore", null, 0, 1, PreprocessingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPreprocessingType_RingArtefacts(), this.getRingArtefactsType(), null, "ringArtefacts", null, 0, 1, PreprocessingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPreprocessingType_Intensity(), this.getIntensityType(), null, "intensity", null, 0, 1, PreprocessingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPreprocessingType_HighPeaksAfterRows(), this.getHighPeaksAfterRowsType(), null, "highPeaksAfterRows", null, 0, 1, PreprocessingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPreprocessingType_HighPeaksAfterColumns(), this.getHighPeaksAfterColumnsType(), null, "highPeaksAfterColumns", null, 0, 1, PreprocessingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(profileTypeTypeEClass, ProfileTypeType.class, "ProfileTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProfileTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ProfileTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProfileTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ProfileTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(profileTypeType1EClass, ProfileTypeType1.class, "ProfileTypeType1", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProfileTypeType1_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ProfileTypeType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProfileTypeType1_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ProfileTypeType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(rawTypeEClass, RawType.class, "RawType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRawType_Type(), this.getTypeType16(), null, "type", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRawType_Bits(), theXMLTypePackage.getInt(), "bits", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRawType_Offset(), this.getOffsetType(), null, "offset", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRawType_ByteOrder(), this.getByteOrderType(), null, "byteOrder", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRawType_Xlen(), theXMLTypePackage.getInt(), "xlen", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRawType_Ylen(), theXMLTypePackage.getInt(), "ylen", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRawType_Zlen(), theXMLTypePackage.getInt(), "zlen", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRawType_Gap(), this.getGapType(), null, "gap", null, 0, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRawType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, RawType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(restrictionsTypeEClass, RestrictionsType.class, "RestrictionsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRestrictionsType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, RestrictionsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRestrictionsType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, RestrictionsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(restrictionsType1EClass, RestrictionsType1.class, "RestrictionsType1", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRestrictionsType1_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, RestrictionsType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRestrictionsType1_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, RestrictionsType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ringArtefactsTypeEClass, RingArtefactsType.class, "RingArtefactsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRingArtefactsType_Type(), this.getTypeType5(), null, "type", null, 0, 1, RingArtefactsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRingArtefactsType_ParameterN(), theXMLTypePackage.getDecimal(), "parameterN", null, 0, 1, RingArtefactsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRingArtefactsType_ParameterR(), theXMLTypePackage.getDecimal(), "parameterR", null, 0, 1, RingArtefactsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRingArtefactsType_NumSeries(), this.getNumSeriesType(), null, "numSeries", null, 0, 1, RingArtefactsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(roiTypeEClass, ROIType.class, "ROIType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getROIType_Type(), this.getTypeType3(), null, "type", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getROIType_Xmin(), theXMLTypePackage.getInt(), "xmin", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getROIType_Xmax(), theXMLTypePackage.getInt(), "xmax", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getROIType_Ymin(), theXMLTypePackage.getInt(), "ymin", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getROIType_Ymax(), theXMLTypePackage.getInt(), "ymax", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getROIType_OutputWidthType(), this.getOutputWidthTypeType(), null, "outputWidthType", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getROIType_OutputWidth(), theXMLTypePackage.getInt(), "outputWidth", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getROIType_Angle(), theXMLTypePackage.getDecimal(), "angle", null, 0, 1, ROIType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(rotationAngleEndPointsTypeEClass, RotationAngleEndPointsType.class, "RotationAngleEndPointsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRotationAngleEndPointsType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, RotationAngleEndPointsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRotationAngleEndPointsType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, RotationAngleEndPointsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(rotationAngleTypeTypeEClass, RotationAngleTypeType.class, "RotationAngleTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRotationAngleTypeType_Value(), theXMLTypePackage.getInt(), "value", null, 0, 1, RotationAngleTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRotationAngleTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, RotationAngleTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(scaleTypeTypeEClass, ScaleTypeType.class, "ScaleTypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getScaleTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ScaleTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getScaleTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ScaleTypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shapeTypeEClass, ShapeType.class, "ShapeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getShapeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ShapeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getShapeType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, ShapeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getShapeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ShapeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shapeType1EClass, ShapeType1.class, "ShapeType1", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getShapeType1_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, ShapeType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getShapeType1_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, ShapeType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stateTypeEClass, StateType.class, "StateType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStateType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, StateType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStateType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, StateType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tiltTypeEClass, TiltType.class, "TiltType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTiltType_Type(), this.getTypeType8(), null, "type", null, 0, 1, TiltType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTiltType_XTilt(), theXMLTypePackage.getString(), "xTilt", null, 0, 1, TiltType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTiltType_ZTilt(), theXMLTypePackage.getString(), "zTilt", null, 0, 1, TiltType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTiltType_Done(), theXMLTypePackage.getNormalizedString(), "done", null, 1, 1, TiltType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(transformTypeEClass, TransformType.class, "TransformType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTransformType_MissedProjections(), this.getMissedProjectionsType(), null, "missedProjections", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTransformType_MissedProjectionsType(), this.getMissedProjectionsTypeType(), null, "missedProjectionsType", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTransformType_RotationAngleType(), this.getRotationAngleTypeType(), null, "rotationAngleType", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_RotationAngle(), theXMLTypePackage.getInt(), "rotationAngle", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTransformType_RotationAngleEndPoints(), this.getRotationAngleEndPointsType(), null, "rotationAngleEndPoints", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_ReCentreAngle(), theXMLTypePackage.getDecimal(), "reCentreAngle", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_ReCentreRadius(), theXMLTypePackage.getDecimal(), "reCentreRadius", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_CropTop(), theXMLTypePackage.getInt(), "cropTop", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_CropBottom(), theXMLTypePackage.getInt(), "cropBottom", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_CropLeft(), theXMLTypePackage.getInt(), "cropLeft", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_CropRight(), theXMLTypePackage.getInt(), "cropRight", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTransformType_ScaleType(), this.getScaleTypeType(), null, "scaleType", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_ScaleWidth(), theXMLTypePackage.getInt(), "scaleWidth", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_ScaleHeight(), theXMLTypePackage.getInt(), "scaleHeight", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTransformType_ExtrapolationType(), this.getExtrapolationTypeType(), null, "extrapolationType", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_ExtrapolationPixels(), theXMLTypePackage.getInt(), "extrapolationPixels", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTransformType_ExtrapolationWidth(), theXMLTypePackage.getInt(), "extrapolationWidth", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTransformType_Interpolation(), this.getInterpolationType(), null, "interpolation", null, 0, 1, TransformType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeTypeEClass, TypeType.class, "TypeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType1EClass, TypeType1.class, "TypeType1", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType1_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType1_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType2EClass, TypeType2.class, "TypeType2", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType2_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType2.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType2_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType2.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType3EClass, TypeType3.class, "TypeType3", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType3_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType3.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType3_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType3.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType4EClass, TypeType4.class, "TypeType4", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType4_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType4.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType4_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType4.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType5EClass, TypeType5.class, "TypeType5", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType5_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType5.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType5_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType5.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType6EClass, TypeType6.class, "TypeType6", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType6_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType6.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType6_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType6.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType7EClass, TypeType7.class, "TypeType7", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType7_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType7.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType7_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType7.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType8EClass, TypeType8.class, "TypeType8", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType8_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType8.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType8_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType8.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType9EClass, TypeType9.class, "TypeType9", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType9_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType9.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType9_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType9.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType10EClass, TypeType10.class, "TypeType10", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType10_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType10.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType10_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType10.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType11EClass, TypeType11.class, "TypeType11", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType11_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType11.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType11_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType11.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType12EClass, TypeType12.class, "TypeType12", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType12_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType12.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType12_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType12.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType13EClass, TypeType13.class, "TypeType13", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType13_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType13.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType13_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType13.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType14EClass, TypeType14.class, "TypeType14", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType14_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType14.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType14_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType14.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType15EClass, TypeType15.class, "TypeType15", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType15_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType15.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType15_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType15.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType16EClass, TypeType16.class, "TypeType16", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType16_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType16.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType16_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType16.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeType17EClass, TypeType17.class, "TypeType17", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTypeType17_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, TypeType17.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeType17_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, TypeType17.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(valueMaxTypeEClass, ValueMaxType.class, "ValueMaxType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getValueMaxType_Type(), this.getTypeType(), null, "type", null, 0, 1, ValueMaxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueMaxType_Percent(), theXMLTypePackage.getInt(), "percent", null, 0, 1, ValueMaxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueMaxType_Pixel(), theXMLTypePackage.getInt(), "pixel", null, 0, 1, ValueMaxType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(valueMinTypeEClass, ValueMinType.class, "ValueMinType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getValueMinType_Type(), this.getTypeType11(), null, "type", null, 0, 1, ValueMinType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueMinType_Percent(), theXMLTypePackage.getInt(), "percent", null, 0, 1, ValueMinType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueMinType_Pixel(), theXMLTypePackage.getInt(), "pixel", null, 0, 1, ValueMinType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(valueStepTypeEClass, ValueStepType.class, "ValueStepType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getValueStepType_Type(), this.getTypeType4(), null, "type", null, 0, 1, ValueStepType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueStepType_Percent(), theXMLTypePackage.getInt(), "percent", null, 0, 1, ValueStepType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValueStepType_Pixel(), theXMLTypePackage.getInt(), "pixel", null, 0, 1, ValueStepType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(windowNameTypeEClass, WindowNameType.class, "WindowNameType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getWindowNameType_Value(), theXMLTypePackage.getNormalizedString(), "value", null, 0, 1, WindowNameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWindowNameType_Info(), theXMLTypePackage.getString(), "info", null, 1, 1, WindowNameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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
		  (backprojectionTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Backprojection_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getBackprojectionType_Filter(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Filter",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_ImageCentre(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ImageCentre",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_ClockwiseRotation(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ClockwiseRotation",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_Tilt(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Tilt",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_CoordinateSystem(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "CoordinateSystem",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_Circles(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Circles",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_ROI(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ROI",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBackprojectionType_PolarCartesianInterpolation(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "PolarCartesianInterpolation",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (beamlineUserTypeEClass, 
		   source, 
		   new String[] {
			 "name", "BeamlineUser_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getBeamlineUserType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_BeamlineName(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "BeamlineName",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_Year(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Year",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_Month(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Month",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_Date(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Date",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_VisitNumber(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "VisitNumber",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_InputDataFolder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "InputDataFolder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_InputScanFolder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "InputScanFolder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_OutputDataFolder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "OutputDataFolder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_OutputScanFolder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "OutputScanFolder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getBeamlineUserType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (bitsTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "BitsType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getBitsTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getBitsTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (byteOrderTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ByteOrder_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getByteOrderType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getByteOrderType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (circlesTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Circles_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getCirclesType_ValueMin(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueMin",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getCirclesType_ValueMax(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueMax",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getCirclesType_ValueStep(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueStep",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getCirclesType_Comm(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "comm",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (clockwiseRotationTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ClockwiseRotation_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getClockwiseRotationType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getClockwiseRotationType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getClockwiseRotationType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (coordinateSystemTypeEClass, 
		   source, 
		   new String[] {
			 "name", "CoordinateSystem_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getCoordinateSystemType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getCoordinateSystemType_Slice(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Slice",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getCoordinateSystemType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (darkFieldTypeEClass, 
		   source, 
		   new String[] {
			 "name", "DarkField_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getDarkFieldType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getDarkFieldType_ValueBefore(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueBefore",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getDarkFieldType_ValueAfter(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueAfter",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getDarkFieldType_FileBefore(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileBefore",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getDarkFieldType_FileAfter(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileAfter",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getDarkFieldType_ProfileType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ProfileType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getDarkFieldType_FileProfile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileProfile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (defaultXmlTypeEClass, 
		   source, 
		   new String[] {
			 "name", "DefaultXml_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getDefaultXmlType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getDefaultXmlType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
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
		  (getDocumentRoot_HMxml(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "HMxml",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (extrapolationTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ExtrapolationType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getExtrapolationTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getExtrapolationTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (fbpTypeEClass, 
		   source, 
		   new String[] {
			 "name", "FBP_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getFBPType_DefaultXml(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "DefaultXml",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_GPUDeviceNumber(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "GPUDeviceNumber",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_BeamlineUser(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "BeamlineUser",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_LogFile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "LogFile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_InputData(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "InputData",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_FlatDarkFields(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FlatDarkFields",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_Preprocessing(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Preprocessing",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_Transform(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Transform",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_Backprojection(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Backprojection",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFBPType_OutputData(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "OutputData",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (filterTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Filter_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getFilterType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFilterType_Name(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Name",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFilterType_Bandwidth(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Bandwidth",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFilterType_WindowName(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "WindowName",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFilterType_Normalisation(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Normalisation",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFilterType_PixelSize(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "PixelSize",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (firstImageIndexTypeEClass, 
		   source, 
		   new String[] {
			 "name", "FirstImageIndex_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getFirstImageIndexType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getFirstImageIndexType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (flatDarkFieldsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "FlatDarkFields_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getFlatDarkFieldsType_FlatField(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FlatField",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatDarkFieldsType_DarkField(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "DarkField",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (flatFieldTypeEClass, 
		   source, 
		   new String[] {
			 "name", "FlatField_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getFlatFieldType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatFieldType_ValueBefore(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueBefore",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatFieldType_ValueAfter(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueAfter",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatFieldType_FileBefore(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileBefore",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatFieldType_FileAfter(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileAfter",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatFieldType_ProfileType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ProfileType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getFlatFieldType_FileProfile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileProfile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (gapTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Gap_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getGapType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getGapType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (highPeaksAfterColumnsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "HighPeaksAfterColumns_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getHighPeaksAfterColumnsType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getHighPeaksAfterColumnsType_NumberPixels(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "NumberPixels",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getHighPeaksAfterColumnsType_Jump(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Jump",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (highPeaksAfterRowsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "HighPeaksAfterRows_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getHighPeaksAfterRowsType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getHighPeaksAfterRowsType_NumberPixels(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "NumberPixels",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getHighPeaksAfterRowsType_Jump(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Jump",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (highPeaksBeforeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "HighPeaksBefore_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getHighPeaksBeforeType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getHighPeaksBeforeType_NumberPixels(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "NumberPixels",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getHighPeaksBeforeType_Jump(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Jump",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (hMxmlTypeEClass, 
		   source, 
		   new String[] {
			 "name", "HMxml_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getHMxmlType_FBP(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FBP",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (imageFirstTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ImageFirst_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImageFirstType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImageFirstType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (imageLastTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ImageLast_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImageLastType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImageLastType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (imageStepTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ImageStep_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImageStepType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getImageStepType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (inputDataTypeEClass, 
		   source, 
		   new String[] {
			 "name", "InputData_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getInputDataType_Folder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Folder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Prefix(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Prefix",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Suffix(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Suffix",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Extension(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Extension",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_NOD(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "NOD",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_MemorySizeMax(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "MemorySizeMax",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_MemorySizeMin(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "MemorySizeMin",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Orientation(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Orientation",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_FileFirst(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileFirst",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_FileLast(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileLast",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_FileStep(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileStep",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_ImageFirst(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ImageFirst",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_ImageLast(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ImageLast",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_ImageStep(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ImageStep",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Raw(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Raw",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_FirstImageIndex(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FirstImageIndex",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_ImagesPerFile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ImagesPerFile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Restrictions(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Restrictions",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_ValueMin(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueMin",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_ValueMax(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueMax",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_Shape(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Shape",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getInputDataType_PixelParam(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "PixelParam",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (intensityTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Intensity_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getIntensityType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getIntensityType_ColumnLeft(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ColumnLeft",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getIntensityType_ColumnRight(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ColumnRight",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getIntensityType_ZeroLeft(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ZeroLeft",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getIntensityType_ZeroRight(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ZeroRight",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (interpolationTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Interpolation_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getInterpolationType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getInterpolationType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (memorySizeMaxTypeEClass, 
		   source, 
		   new String[] {
			 "name", "MemorySizeMax_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMemorySizeMaxType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMemorySizeMaxType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (memorySizeMinTypeEClass, 
		   source, 
		   new String[] {
			 "name", "MemorySizeMin_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMemorySizeMinType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMemorySizeMinType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (missedProjectionsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "MissedProjections_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMissedProjectionsType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMissedProjectionsType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (missedProjectionsTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "MissedProjectionsType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMissedProjectionsTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getMissedProjectionsTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (nameTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Name_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNameType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNameType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (nodTypeEClass, 
		   source, 
		   new String[] {
			 "name", "NOD_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNODType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNODType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (normalisationTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Normalisation_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNormalisationType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNormalisationType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (numSeriesTypeEClass, 
		   source, 
		   new String[] {
			 "name", "NumSeries_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNumSeriesType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getNumSeriesType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (offsetTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Offset_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getOffsetType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getOffsetType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (orientationTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Orientation_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getOrientationType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getOrientationType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOrientationType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (outputDataTypeEClass, 
		   source, 
		   new String[] {
			 "name", "OutputData_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getOutputDataType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_State(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "State",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Folder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Folder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Prefix(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Prefix",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Suffix(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Suffix",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Extension(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Extension",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_NOD(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "NOD",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_FileFirst(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileFirst",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_FileStep(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "FileStep",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_BitsType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "BitsType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Bits(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Bits",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Restrictions(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Restrictions",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_ValueMin(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueMin",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_ValueMax(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ValueMax",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getOutputDataType_Shape(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Shape",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (outputWidthTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "OutputWidthType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getOutputWidthTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getOutputWidthTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (polarCartesianInterpolationTypeEClass, 
		   source, 
		   new String[] {
			 "name", "PolarCartesianInterpolation_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getPolarCartesianInterpolationType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getPolarCartesianInterpolationType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getPolarCartesianInterpolationType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (preprocessingTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Preprocessing_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getPreprocessingType_HighPeaksBefore(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "HighPeaksBefore",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getPreprocessingType_RingArtefacts(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "RingArtefacts",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getPreprocessingType_Intensity(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Intensity",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getPreprocessingType_HighPeaksAfterRows(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "HighPeaksAfterRows",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getPreprocessingType_HighPeaksAfterColumns(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "HighPeaksAfterColumns",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (profileTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ProfileType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getProfileTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getProfileTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (profileTypeType1EClass, 
		   source, 
		   new String[] {
			 "name", "ProfileType_._1_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getProfileTypeType1_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getProfileTypeType1_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (rawTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Raw_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getRawType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Bits(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Bits",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Offset(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Offset",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_ByteOrder(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ByteOrder",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Xlen(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Xlen",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Ylen(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Ylen",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Zlen(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Zlen",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Gap(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Gap",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRawType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (restrictionsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Restrictions_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRestrictionsType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRestrictionsType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (restrictionsType1EClass, 
		   source, 
		   new String[] {
			 "name", "Restrictions_._1_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRestrictionsType1_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRestrictionsType1_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (ringArtefactsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "RingArtefacts_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getRingArtefactsType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRingArtefactsType_ParameterN(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ParameterN",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRingArtefactsType_ParameterR(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ParameterR",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getRingArtefactsType_NumSeries(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "NumSeries",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (roiTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ROI_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getROIType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_Xmin(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Xmin",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_Xmax(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Xmax",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_Ymin(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Ymin",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_Ymax(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Ymax",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_OutputWidthType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "OutputWidthType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_OutputWidth(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "OutputWidth",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getROIType_Angle(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Angle",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (rotationAngleEndPointsTypeEClass, 
		   source, 
		   new String[] {
			 "name", "RotationAngleEndPoints_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRotationAngleEndPointsType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRotationAngleEndPointsType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (rotationAngleTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "RotationAngleType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRotationAngleTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getRotationAngleTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (scaleTypeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ScaleType_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getScaleTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getScaleTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (shapeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Shape_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShapeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShapeType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getShapeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (shapeType1EClass, 
		   source, 
		   new String[] {
			 "name", "Shape_._1_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShapeType1_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getShapeType1_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (stateTypeEClass, 
		   source, 
		   new String[] {
			 "name", "State_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getStateType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getStateType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (tiltTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Tilt_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getTiltType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTiltType_XTilt(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "X-tilt",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTiltType_ZTilt(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Z-tilt",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTiltType_Done(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "done",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (transformTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Transform_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getTransformType_MissedProjections(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "MissedProjections",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_MissedProjectionsType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "MissedProjectionsType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_RotationAngleType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "RotationAngleType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_RotationAngle(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "RotationAngle",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_RotationAngleEndPoints(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "RotationAngleEndPoints",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ReCentreAngle(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ReCentreAngle",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ReCentreRadius(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ReCentreRadius",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_CropTop(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "CropTop",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_CropBottom(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "CropBottom",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_CropLeft(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "CropLeft",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_CropRight(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "CropRight",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ScaleType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ScaleType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ScaleWidth(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ScaleWidth",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ScaleHeight(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ScaleHeight",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ExtrapolationType(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ExtrapolationType",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ExtrapolationPixels(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ExtrapolationPixels",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_ExtrapolationWidth(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "ExtrapolationWidth",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getTransformType_Interpolation(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Interpolation",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeTypeEClass, 
		   source, 
		   new String[] {
			 "name", "Type_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType1EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._1_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType1_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType1_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType2EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._2_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType2_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType2_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType3EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._3_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType3_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType3_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType4EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._4_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType4_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType4_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType5EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._5_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType5_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType5_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType6EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._6_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType6_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType6_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType7EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._7_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType7_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType7_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType8EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._8_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType8_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType8_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType9EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._9_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType9_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType9_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType10EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._10_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType10_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType10_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType11EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._11_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType11_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType11_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType12EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._12_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType12_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType12_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType13EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._13_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType13_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType13_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType14EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._14_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType14_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType14_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType15EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._15_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType15_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType15_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType16EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._16_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType16_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType16_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (typeType17EClass, 
		   source, 
		   new String[] {
			 "name", "Type_._17_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType17_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getTypeType17_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (valueMaxTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ValueMax_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getValueMaxType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getValueMaxType_Percent(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Percent",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getValueMaxType_Pixel(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Pixel",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (valueMinTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ValueMin_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getValueMinType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getValueMinType_Percent(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Percent",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getValueMinType_Pixel(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Pixel",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (valueStepTypeEClass, 
		   source, 
		   new String[] {
			 "name", "ValueStep_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getValueStepType_Type(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Type",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getValueStepType_Percent(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Percent",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getValueStepType_Pixel(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "Pixel",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (windowNameTypeEClass, 
		   source, 
		   new String[] {
			 "name", "WindowName_._type",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getWindowNameType_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getWindowNameType_Info(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "info",
			 "namespace", "##targetNamespace"
		   });
	}

} //HmPackageImpl
