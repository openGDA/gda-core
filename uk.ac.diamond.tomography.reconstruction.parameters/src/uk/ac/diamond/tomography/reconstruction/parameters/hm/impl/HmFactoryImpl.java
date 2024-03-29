/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class HmFactoryImpl extends EFactoryImpl implements HmFactory {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(HmFactoryImpl.class);
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static HmFactory init() {
		try {
			HmFactory theHmFactory = (HmFactory)EPackage.Registry.INSTANCE.getEFactory("platform:/resource/uk.ac.diamond.tomography.reconstruction.parameters/model/hm.xsd");
			if (theHmFactory != null) {
				return theHmFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new HmFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HmFactoryImpl() {
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
			case HmPackage.BACKPROJECTION_TYPE: return createBackprojectionType();
			case HmPackage.BEAMLINE_USER_TYPE: return createBeamlineUserType();
			case HmPackage.BITS_TYPE_TYPE: return createBitsTypeType();
			case HmPackage.BYTE_ORDER_TYPE: return createByteOrderType();
			case HmPackage.CIRCLES_TYPE: return createCirclesType();
			case HmPackage.CLOCKWISE_ROTATION_TYPE: return createClockwiseRotationType();
			case HmPackage.COORDINATE_SYSTEM_TYPE: return createCoordinateSystemType();
			case HmPackage.DARK_FIELD_TYPE: return createDarkFieldType();
			case HmPackage.DEFAULT_XML_TYPE: return createDefaultXmlType();
			case HmPackage.DOCUMENT_ROOT: return createDocumentRoot();
			case HmPackage.EXTRAPOLATION_TYPE_TYPE: return createExtrapolationTypeType();
			case HmPackage.FBP_TYPE: return createFBPType();
			case HmPackage.FILTER_TYPE: return createFilterType();
			case HmPackage.FIRST_IMAGE_INDEX_TYPE: return createFirstImageIndexType();
			case HmPackage.FLAT_DARK_FIELDS_TYPE: return createFlatDarkFieldsType();
			case HmPackage.FLAT_FIELD_TYPE: return createFlatFieldType();
			case HmPackage.GAP_TYPE: return createGapType();
			case HmPackage.HIGH_PEAKS_AFTER_COLUMNS_TYPE: return createHighPeaksAfterColumnsType();
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE: return createHighPeaksAfterRowsType();
			case HmPackage.HIGH_PEAKS_BEFORE_TYPE: return createHighPeaksBeforeType();
			case HmPackage.HMXML_TYPE: return createHMxmlType();
			case HmPackage.IMAGE_FIRST_TYPE: return createImageFirstType();
			case HmPackage.IMAGE_LAST_TYPE: return createImageLastType();
			case HmPackage.IMAGE_STEP_TYPE: return createImageStepType();
			case HmPackage.INPUT_DATA_TYPE: return createInputDataType();
			case HmPackage.INTENSITY_TYPE: return createIntensityType();
			case HmPackage.INTERPOLATION_TYPE: return createInterpolationType();
			case HmPackage.MEMORY_SIZE_MAX_TYPE: return createMemorySizeMaxType();
			case HmPackage.MEMORY_SIZE_MIN_TYPE: return createMemorySizeMinType();
			case HmPackage.MISSED_PROJECTIONS_TYPE: return createMissedProjectionsType();
			case HmPackage.MISSED_PROJECTIONS_TYPE_TYPE: return createMissedProjectionsTypeType();
			case HmPackage.NAME_TYPE: return createNameType();
			case HmPackage.NOD_TYPE: return createNODType();
			case HmPackage.NORMALISATION_TYPE: return createNormalisationType();
			case HmPackage.NUM_SERIES_TYPE: return createNumSeriesType();
			case HmPackage.OFFSET_TYPE: return createOffsetType();
			case HmPackage.ORIENTATION_TYPE: return createOrientationType();
			case HmPackage.OUTPUT_DATA_TYPE: return createOutputDataType();
			case HmPackage.OUTPUT_WIDTH_TYPE_TYPE: return createOutputWidthTypeType();
			case HmPackage.POLAR_CARTESIAN_INTERPOLATION_TYPE: return createPolarCartesianInterpolationType();
			case HmPackage.PREPROCESSING_TYPE: return createPreprocessingType();
			case HmPackage.PROFILE_TYPE_TYPE: return createProfileTypeType();
			case HmPackage.PROFILE_TYPE_TYPE1: return createProfileTypeType1();
			case HmPackage.RAW_TYPE: return createRawType();
			case HmPackage.RESTRICTIONS_TYPE: return createRestrictionsType();
			case HmPackage.RESTRICTIONS_TYPE1: return createRestrictionsType1();
			case HmPackage.RING_ARTEFACTS_TYPE: return createRingArtefactsType();
			case HmPackage.ROI_TYPE: return createROIType();
			case HmPackage.ROTATION_ANGLE_END_POINTS_TYPE: return createRotationAngleEndPointsType();
			case HmPackage.ROTATION_ANGLE_TYPE_TYPE: return createRotationAngleTypeType();
			case HmPackage.SCALE_TYPE_TYPE: return createScaleTypeType();
			case HmPackage.SHAPE_TYPE: return createShapeType();
			case HmPackage.SHAPE_TYPE1: return createShapeType1();
			case HmPackage.STATE_TYPE: return createStateType();
			case HmPackage.TILT_TYPE: return createTiltType();
			case HmPackage.TRANSFORM_TYPE: return createTransformType();
			case HmPackage.TYPE_TYPE: return createTypeType();
			case HmPackage.TYPE_TYPE1: return createTypeType1();
			case HmPackage.TYPE_TYPE2: return createTypeType2();
			case HmPackage.TYPE_TYPE3: return createTypeType3();
			case HmPackage.TYPE_TYPE4: return createTypeType4();
			case HmPackage.TYPE_TYPE5: return createTypeType5();
			case HmPackage.TYPE_TYPE6: return createTypeType6();
			case HmPackage.TYPE_TYPE7: return createTypeType7();
			case HmPackage.TYPE_TYPE8: return createTypeType8();
			case HmPackage.TYPE_TYPE9: return createTypeType9();
			case HmPackage.TYPE_TYPE10: return createTypeType10();
			case HmPackage.TYPE_TYPE11: return createTypeType11();
			case HmPackage.TYPE_TYPE12: return createTypeType12();
			case HmPackage.TYPE_TYPE13: return createTypeType13();
			case HmPackage.TYPE_TYPE14: return createTypeType14();
			case HmPackage.TYPE_TYPE15: return createTypeType15();
			case HmPackage.TYPE_TYPE16: return createTypeType16();
			case HmPackage.TYPE_TYPE17: return createTypeType17();
			case HmPackage.VALUE_MAX_TYPE: return createValueMaxType();
			case HmPackage.VALUE_MIN_TYPE: return createValueMinType();
			case HmPackage.VALUE_STEP_TYPE: return createValueStepType();
			case HmPackage.WINDOW_NAME_TYPE: return createWindowNameType();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BackprojectionType createBackprojectionType() {
		BackprojectionTypeImpl backprojectionType = new BackprojectionTypeImpl();
		return backprojectionType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BeamlineUserType createBeamlineUserType() {
		BeamlineUserTypeImpl beamlineUserType = new BeamlineUserTypeImpl();
		return beamlineUserType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BitsTypeType createBitsTypeType() {
		BitsTypeTypeImpl bitsTypeType = new BitsTypeTypeImpl();
		return bitsTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ByteOrderType createByteOrderType() {
		ByteOrderTypeImpl byteOrderType = new ByteOrderTypeImpl();
		return byteOrderType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CirclesType createCirclesType() {
		CirclesTypeImpl circlesType = new CirclesTypeImpl();
		return circlesType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ClockwiseRotationType createClockwiseRotationType() {
		ClockwiseRotationTypeImpl clockwiseRotationType = new ClockwiseRotationTypeImpl();
		return clockwiseRotationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CoordinateSystemType createCoordinateSystemType() {
		CoordinateSystemTypeImpl coordinateSystemType = new CoordinateSystemTypeImpl();
		return coordinateSystemType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DarkFieldType createDarkFieldType() {
		DarkFieldTypeImpl darkFieldType = new DarkFieldTypeImpl();
		return darkFieldType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DefaultXmlType createDefaultXmlType() {
		DefaultXmlTypeImpl defaultXmlType = new DefaultXmlTypeImpl();
		return defaultXmlType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DocumentRoot createDocumentRoot() {
		DocumentRootImpl documentRoot = new DocumentRootImpl();
		return documentRoot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExtrapolationTypeType createExtrapolationTypeType() {
		ExtrapolationTypeTypeImpl extrapolationTypeType = new ExtrapolationTypeTypeImpl();
		return extrapolationTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FBPType createFBPType() {
		FBPTypeImpl fbpType = new FBPTypeImpl();
		return fbpType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FilterType createFilterType() {
		FilterTypeImpl filterType = new FilterTypeImpl();
		return filterType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FirstImageIndexType createFirstImageIndexType() {
		FirstImageIndexTypeImpl firstImageIndexType = new FirstImageIndexTypeImpl();
		return firstImageIndexType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FlatDarkFieldsType createFlatDarkFieldsType() {
		FlatDarkFieldsTypeImpl flatDarkFieldsType = new FlatDarkFieldsTypeImpl();
		return flatDarkFieldsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FlatFieldType createFlatFieldType() {
		FlatFieldTypeImpl flatFieldType = new FlatFieldTypeImpl();
		return flatFieldType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GapType createGapType() {
		GapTypeImpl gapType = new GapTypeImpl();
		return gapType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HighPeaksAfterColumnsType createHighPeaksAfterColumnsType() {
		HighPeaksAfterColumnsTypeImpl highPeaksAfterColumnsType = new HighPeaksAfterColumnsTypeImpl();
		return highPeaksAfterColumnsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HighPeaksAfterRowsType createHighPeaksAfterRowsType() {
		HighPeaksAfterRowsTypeImpl highPeaksAfterRowsType = new HighPeaksAfterRowsTypeImpl();
		return highPeaksAfterRowsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HighPeaksBeforeType createHighPeaksBeforeType() {
		HighPeaksBeforeTypeImpl highPeaksBeforeType = new HighPeaksBeforeTypeImpl();
		return highPeaksBeforeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HMxmlType createHMxmlType() {
		HMxmlTypeImpl hMxmlType = new HMxmlTypeImpl();
		return hMxmlType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ImageFirstType createImageFirstType() {
		ImageFirstTypeImpl imageFirstType = new ImageFirstTypeImpl();
		return imageFirstType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ImageLastType createImageLastType() {
		ImageLastTypeImpl imageLastType = new ImageLastTypeImpl();
		return imageLastType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ImageStepType createImageStepType() {
		ImageStepTypeImpl imageStepType = new ImageStepTypeImpl();
		return imageStepType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InputDataType createInputDataType() {
		InputDataTypeImpl inputDataType = new InputDataTypeImpl();
		return inputDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IntensityType createIntensityType() {
		IntensityTypeImpl intensityType = new IntensityTypeImpl();
		return intensityType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InterpolationType createInterpolationType() {
		InterpolationTypeImpl interpolationType = new InterpolationTypeImpl();
		return interpolationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MemorySizeMaxType createMemorySizeMaxType() {
		MemorySizeMaxTypeImpl memorySizeMaxType = new MemorySizeMaxTypeImpl();
		return memorySizeMaxType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MemorySizeMinType createMemorySizeMinType() {
		MemorySizeMinTypeImpl memorySizeMinType = new MemorySizeMinTypeImpl();
		return memorySizeMinType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MissedProjectionsType createMissedProjectionsType() {
		MissedProjectionsTypeImpl missedProjectionsType = new MissedProjectionsTypeImpl();
		return missedProjectionsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MissedProjectionsTypeType createMissedProjectionsTypeType() {
		MissedProjectionsTypeTypeImpl missedProjectionsTypeType = new MissedProjectionsTypeTypeImpl();
		return missedProjectionsTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NameType createNameType() {
		NameTypeImpl nameType = new NameTypeImpl();
		return nameType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NODType createNODType() {
		NODTypeImpl nodType = new NODTypeImpl();
		return nodType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NormalisationType createNormalisationType() {
		NormalisationTypeImpl normalisationType = new NormalisationTypeImpl();
		return normalisationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NumSeriesType createNumSeriesType() {
		NumSeriesTypeImpl numSeriesType = new NumSeriesTypeImpl();
		return numSeriesType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OffsetType createOffsetType() {
		OffsetTypeImpl offsetType = new OffsetTypeImpl();
		return offsetType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OrientationType createOrientationType() {
		OrientationTypeImpl orientationType = new OrientationTypeImpl();
		return orientationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OutputDataType createOutputDataType() {
		OutputDataTypeImpl outputDataType = new OutputDataTypeImpl();
		return outputDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OutputWidthTypeType createOutputWidthTypeType() {
		OutputWidthTypeTypeImpl outputWidthTypeType = new OutputWidthTypeTypeImpl();
		return outputWidthTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolarCartesianInterpolationType createPolarCartesianInterpolationType() {
		PolarCartesianInterpolationTypeImpl polarCartesianInterpolationType = new PolarCartesianInterpolationTypeImpl();
		return polarCartesianInterpolationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PreprocessingType createPreprocessingType() {
		PreprocessingTypeImpl preprocessingType = new PreprocessingTypeImpl();
		return preprocessingType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProfileTypeType createProfileTypeType() {
		ProfileTypeTypeImpl profileTypeType = new ProfileTypeTypeImpl();
		return profileTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProfileTypeType1 createProfileTypeType1() {
		ProfileTypeType1Impl profileTypeType1 = new ProfileTypeType1Impl();
		return profileTypeType1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RawType createRawType() {
		RawTypeImpl rawType = new RawTypeImpl();
		return rawType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RestrictionsType createRestrictionsType() {
		RestrictionsTypeImpl restrictionsType = new RestrictionsTypeImpl();
		return restrictionsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RestrictionsType1 createRestrictionsType1() {
		RestrictionsType1Impl restrictionsType1 = new RestrictionsType1Impl();
		return restrictionsType1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RingArtefactsType createRingArtefactsType() {
		RingArtefactsTypeImpl ringArtefactsType = new RingArtefactsTypeImpl();
		return ringArtefactsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ROIType createROIType() {
		ROITypeImpl roiType = new ROITypeImpl();
		return roiType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RotationAngleEndPointsType createRotationAngleEndPointsType() {
		RotationAngleEndPointsTypeImpl rotationAngleEndPointsType = new RotationAngleEndPointsTypeImpl();
		return rotationAngleEndPointsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RotationAngleTypeType createRotationAngleTypeType() {
		RotationAngleTypeTypeImpl rotationAngleTypeType = new RotationAngleTypeTypeImpl();
		return rotationAngleTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ScaleTypeType createScaleTypeType() {
		ScaleTypeTypeImpl scaleTypeType = new ScaleTypeTypeImpl();
		return scaleTypeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ShapeType createShapeType() {
		ShapeTypeImpl shapeType = new ShapeTypeImpl();
		return shapeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ShapeType1 createShapeType1() {
		ShapeType1Impl shapeType1 = new ShapeType1Impl();
		return shapeType1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public StateType createStateType() {
		StateTypeImpl stateType = new StateTypeImpl();
		return stateType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TiltType createTiltType() {
		TiltTypeImpl tiltType = new TiltTypeImpl();
		return tiltType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TransformType createTransformType() {
		TransformTypeImpl transformType = new TransformTypeImpl();
		return transformType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType createTypeType() {
		TypeTypeImpl typeType = new TypeTypeImpl();
		return typeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType1 createTypeType1() {
		TypeType1Impl typeType1 = new TypeType1Impl();
		return typeType1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType2 createTypeType2() {
		TypeType2Impl typeType2 = new TypeType2Impl();
		return typeType2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType3 createTypeType3() {
		TypeType3Impl typeType3 = new TypeType3Impl();
		return typeType3;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType4 createTypeType4() {
		TypeType4Impl typeType4 = new TypeType4Impl();
		return typeType4;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType5 createTypeType5() {
		TypeType5Impl typeType5 = new TypeType5Impl();
		return typeType5;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType6 createTypeType6() {
		TypeType6Impl typeType6 = new TypeType6Impl();
		return typeType6;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType7 createTypeType7() {
		TypeType7Impl typeType7 = new TypeType7Impl();
		return typeType7;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType8 createTypeType8() {
		TypeType8Impl typeType8 = new TypeType8Impl();
		return typeType8;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType9 createTypeType9() {
		TypeType9Impl typeType9 = new TypeType9Impl();
		return typeType9;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType10 createTypeType10() {
		TypeType10Impl typeType10 = new TypeType10Impl();
		return typeType10;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType11 createTypeType11() {
		TypeType11Impl typeType11 = new TypeType11Impl();
		return typeType11;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType12 createTypeType12() {
		TypeType12Impl typeType12 = new TypeType12Impl();
		return typeType12;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType13 createTypeType13() {
		TypeType13Impl typeType13 = new TypeType13Impl();
		return typeType13;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType14 createTypeType14() {
		TypeType14Impl typeType14 = new TypeType14Impl();
		return typeType14;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType15 createTypeType15() {
		TypeType15Impl typeType15 = new TypeType15Impl();
		return typeType15;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType16 createTypeType16() {
		TypeType16Impl typeType16 = new TypeType16Impl();
		return typeType16;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType17 createTypeType17() {
		TypeType17Impl typeType17 = new TypeType17Impl();
		return typeType17;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValueMaxType createValueMaxType() {
		ValueMaxTypeImpl valueMaxType = new ValueMaxTypeImpl();
		return valueMaxType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValueMinType createValueMinType() {
		ValueMinTypeImpl valueMinType = new ValueMinTypeImpl();
		return valueMinType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValueStepType createValueStepType() {
		ValueStepTypeImpl valueStepType = new ValueStepTypeImpl();
		return valueStepType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public WindowNameType createWindowNameType() {
		WindowNameTypeImpl windowNameType = new WindowNameTypeImpl();
		return windowNameType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HmPackage getHmPackage() {
		return (HmPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated(since="GDA 8.26")
	public static HmPackage getPackage() {
		logger.deprecatedMethod("getPackage()");
		return HmPackage.eINSTANCE;
	}

} //HmFactoryImpl
