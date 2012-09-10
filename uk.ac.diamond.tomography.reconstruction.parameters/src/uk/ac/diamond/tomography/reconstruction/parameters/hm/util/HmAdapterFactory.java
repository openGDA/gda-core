/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage
 * @generated
 */
public class HmAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static HmPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HmAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = HmPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HmSwitch<Adapter> modelSwitch =
		new HmSwitch<Adapter>() {
			@Override
			public Adapter caseBackprojectionType(BackprojectionType object) {
				return createBackprojectionTypeAdapter();
			}
			@Override
			public Adapter caseBeamlineUserType(BeamlineUserType object) {
				return createBeamlineUserTypeAdapter();
			}
			@Override
			public Adapter caseBitsTypeType(BitsTypeType object) {
				return createBitsTypeTypeAdapter();
			}
			@Override
			public Adapter caseByteOrderType(ByteOrderType object) {
				return createByteOrderTypeAdapter();
			}
			@Override
			public Adapter caseCirclesType(CirclesType object) {
				return createCirclesTypeAdapter();
			}
			@Override
			public Adapter caseClockwiseRotationType(ClockwiseRotationType object) {
				return createClockwiseRotationTypeAdapter();
			}
			@Override
			public Adapter caseCoordinateSystemType(CoordinateSystemType object) {
				return createCoordinateSystemTypeAdapter();
			}
			@Override
			public Adapter caseDarkFieldType(DarkFieldType object) {
				return createDarkFieldTypeAdapter();
			}
			@Override
			public Adapter caseDefaultXmlType(DefaultXmlType object) {
				return createDefaultXmlTypeAdapter();
			}
			@Override
			public Adapter caseDocumentRoot(DocumentRoot object) {
				return createDocumentRootAdapter();
			}
			@Override
			public Adapter caseExtrapolationTypeType(ExtrapolationTypeType object) {
				return createExtrapolationTypeTypeAdapter();
			}
			@Override
			public Adapter caseFBPType(FBPType object) {
				return createFBPTypeAdapter();
			}
			@Override
			public Adapter caseFilterType(FilterType object) {
				return createFilterTypeAdapter();
			}
			@Override
			public Adapter caseFirstImageIndexType(FirstImageIndexType object) {
				return createFirstImageIndexTypeAdapter();
			}
			@Override
			public Adapter caseFlatDarkFieldsType(FlatDarkFieldsType object) {
				return createFlatDarkFieldsTypeAdapter();
			}
			@Override
			public Adapter caseFlatFieldType(FlatFieldType object) {
				return createFlatFieldTypeAdapter();
			}
			@Override
			public Adapter caseGapType(GapType object) {
				return createGapTypeAdapter();
			}
			@Override
			public Adapter caseHighPeaksAfterColumnsType(HighPeaksAfterColumnsType object) {
				return createHighPeaksAfterColumnsTypeAdapter();
			}
			@Override
			public Adapter caseHighPeaksAfterRowsType(HighPeaksAfterRowsType object) {
				return createHighPeaksAfterRowsTypeAdapter();
			}
			@Override
			public Adapter caseHighPeaksBeforeType(HighPeaksBeforeType object) {
				return createHighPeaksBeforeTypeAdapter();
			}
			@Override
			public Adapter caseHMxmlType(HMxmlType object) {
				return createHMxmlTypeAdapter();
			}
			@Override
			public Adapter caseImageFirstType(ImageFirstType object) {
				return createImageFirstTypeAdapter();
			}
			@Override
			public Adapter caseImageLastType(ImageLastType object) {
				return createImageLastTypeAdapter();
			}
			@Override
			public Adapter caseImageStepType(ImageStepType object) {
				return createImageStepTypeAdapter();
			}
			@Override
			public Adapter caseInputDataType(InputDataType object) {
				return createInputDataTypeAdapter();
			}
			@Override
			public Adapter caseIntensityType(IntensityType object) {
				return createIntensityTypeAdapter();
			}
			@Override
			public Adapter caseInterpolationType(InterpolationType object) {
				return createInterpolationTypeAdapter();
			}
			@Override
			public Adapter caseMemorySizeMaxType(MemorySizeMaxType object) {
				return createMemorySizeMaxTypeAdapter();
			}
			@Override
			public Adapter caseMemorySizeMinType(MemorySizeMinType object) {
				return createMemorySizeMinTypeAdapter();
			}
			@Override
			public Adapter caseMissedProjectionsType(MissedProjectionsType object) {
				return createMissedProjectionsTypeAdapter();
			}
			@Override
			public Adapter caseMissedProjectionsTypeType(MissedProjectionsTypeType object) {
				return createMissedProjectionsTypeTypeAdapter();
			}
			@Override
			public Adapter caseNameType(NameType object) {
				return createNameTypeAdapter();
			}
			@Override
			public Adapter caseNODType(NODType object) {
				return createNODTypeAdapter();
			}
			@Override
			public Adapter caseNormalisationType(NormalisationType object) {
				return createNormalisationTypeAdapter();
			}
			@Override
			public Adapter caseNumSeriesType(NumSeriesType object) {
				return createNumSeriesTypeAdapter();
			}
			@Override
			public Adapter caseOffsetType(OffsetType object) {
				return createOffsetTypeAdapter();
			}
			@Override
			public Adapter caseOrientationType(OrientationType object) {
				return createOrientationTypeAdapter();
			}
			@Override
			public Adapter caseOutputDataType(OutputDataType object) {
				return createOutputDataTypeAdapter();
			}
			@Override
			public Adapter caseOutputWidthTypeType(OutputWidthTypeType object) {
				return createOutputWidthTypeTypeAdapter();
			}
			@Override
			public Adapter casePolarCartesianInterpolationType(PolarCartesianInterpolationType object) {
				return createPolarCartesianInterpolationTypeAdapter();
			}
			@Override
			public Adapter casePreprocessingType(PreprocessingType object) {
				return createPreprocessingTypeAdapter();
			}
			@Override
			public Adapter caseProfileTypeType(ProfileTypeType object) {
				return createProfileTypeTypeAdapter();
			}
			@Override
			public Adapter caseProfileTypeType1(ProfileTypeType1 object) {
				return createProfileTypeType1Adapter();
			}
			@Override
			public Adapter caseRawType(RawType object) {
				return createRawTypeAdapter();
			}
			@Override
			public Adapter caseRestrictionsType(RestrictionsType object) {
				return createRestrictionsTypeAdapter();
			}
			@Override
			public Adapter caseRestrictionsType1(RestrictionsType1 object) {
				return createRestrictionsType1Adapter();
			}
			@Override
			public Adapter caseRingArtefactsType(RingArtefactsType object) {
				return createRingArtefactsTypeAdapter();
			}
			@Override
			public Adapter caseROIType(ROIType object) {
				return createROITypeAdapter();
			}
			@Override
			public Adapter caseRotationAngleEndPointsType(RotationAngleEndPointsType object) {
				return createRotationAngleEndPointsTypeAdapter();
			}
			@Override
			public Adapter caseRotationAngleTypeType(RotationAngleTypeType object) {
				return createRotationAngleTypeTypeAdapter();
			}
			@Override
			public Adapter caseScaleTypeType(ScaleTypeType object) {
				return createScaleTypeTypeAdapter();
			}
			@Override
			public Adapter caseShapeType(ShapeType object) {
				return createShapeTypeAdapter();
			}
			@Override
			public Adapter caseShapeType1(ShapeType1 object) {
				return createShapeType1Adapter();
			}
			@Override
			public Adapter caseStateType(StateType object) {
				return createStateTypeAdapter();
			}
			@Override
			public Adapter caseTiltType(TiltType object) {
				return createTiltTypeAdapter();
			}
			@Override
			public Adapter caseTransformType(TransformType object) {
				return createTransformTypeAdapter();
			}
			@Override
			public Adapter caseTypeType(TypeType object) {
				return createTypeTypeAdapter();
			}
			@Override
			public Adapter caseTypeType1(TypeType1 object) {
				return createTypeType1Adapter();
			}
			@Override
			public Adapter caseTypeType2(TypeType2 object) {
				return createTypeType2Adapter();
			}
			@Override
			public Adapter caseTypeType3(TypeType3 object) {
				return createTypeType3Adapter();
			}
			@Override
			public Adapter caseTypeType4(TypeType4 object) {
				return createTypeType4Adapter();
			}
			@Override
			public Adapter caseTypeType5(TypeType5 object) {
				return createTypeType5Adapter();
			}
			@Override
			public Adapter caseTypeType6(TypeType6 object) {
				return createTypeType6Adapter();
			}
			@Override
			public Adapter caseTypeType7(TypeType7 object) {
				return createTypeType7Adapter();
			}
			@Override
			public Adapter caseTypeType8(TypeType8 object) {
				return createTypeType8Adapter();
			}
			@Override
			public Adapter caseTypeType9(TypeType9 object) {
				return createTypeType9Adapter();
			}
			@Override
			public Adapter caseTypeType10(TypeType10 object) {
				return createTypeType10Adapter();
			}
			@Override
			public Adapter caseTypeType11(TypeType11 object) {
				return createTypeType11Adapter();
			}
			@Override
			public Adapter caseTypeType12(TypeType12 object) {
				return createTypeType12Adapter();
			}
			@Override
			public Adapter caseTypeType13(TypeType13 object) {
				return createTypeType13Adapter();
			}
			@Override
			public Adapter caseTypeType14(TypeType14 object) {
				return createTypeType14Adapter();
			}
			@Override
			public Adapter caseTypeType15(TypeType15 object) {
				return createTypeType15Adapter();
			}
			@Override
			public Adapter caseTypeType16(TypeType16 object) {
				return createTypeType16Adapter();
			}
			@Override
			public Adapter caseTypeType17(TypeType17 object) {
				return createTypeType17Adapter();
			}
			@Override
			public Adapter caseValueMaxType(ValueMaxType object) {
				return createValueMaxTypeAdapter();
			}
			@Override
			public Adapter caseValueMinType(ValueMinType object) {
				return createValueMinTypeAdapter();
			}
			@Override
			public Adapter caseValueStepType(ValueStepType object) {
				return createValueStepTypeAdapter();
			}
			@Override
			public Adapter caseWindowNameType(WindowNameType object) {
				return createWindowNameTypeAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType <em>Backprojection Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType
	 * @generated
	 */
	public Adapter createBackprojectionTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType <em>Beamline User Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType
	 * @generated
	 */
	public Adapter createBeamlineUserTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType <em>Bits Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType
	 * @generated
	 */
	public Adapter createBitsTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType <em>Byte Order Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType
	 * @generated
	 */
	public Adapter createByteOrderTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType <em>Circles Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType
	 * @generated
	 */
	public Adapter createCirclesTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType <em>Clockwise Rotation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType
	 * @generated
	 */
	public Adapter createClockwiseRotationTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType <em>Coordinate System Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType
	 * @generated
	 */
	public Adapter createCoordinateSystemTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType <em>Dark Field Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType
	 * @generated
	 */
	public Adapter createDarkFieldTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType <em>Default Xml Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType
	 * @generated
	 */
	public Adapter createDefaultXmlTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot
	 * @generated
	 */
	public Adapter createDocumentRootAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType <em>Extrapolation Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType
	 * @generated
	 */
	public Adapter createExtrapolationTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType <em>FBP Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType
	 * @generated
	 */
	public Adapter createFBPTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType <em>Filter Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType
	 * @generated
	 */
	public Adapter createFilterTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType <em>First Image Index Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType
	 * @generated
	 */
	public Adapter createFirstImageIndexTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType <em>Flat Dark Fields Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType
	 * @generated
	 */
	public Adapter createFlatDarkFieldsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType <em>Flat Field Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType
	 * @generated
	 */
	public Adapter createFlatFieldTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType <em>Gap Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType
	 * @generated
	 */
	public Adapter createGapTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType <em>High Peaks After Columns Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType
	 * @generated
	 */
	public Adapter createHighPeaksAfterColumnsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType <em>High Peaks After Rows Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType
	 * @generated
	 */
	public Adapter createHighPeaksAfterRowsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType <em>High Peaks Before Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType
	 * @generated
	 */
	public Adapter createHighPeaksBeforeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType <em>HMxml Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType
	 * @generated
	 */
	public Adapter createHMxmlTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType <em>Image First Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType
	 * @generated
	 */
	public Adapter createImageFirstTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType <em>Image Last Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType
	 * @generated
	 */
	public Adapter createImageLastTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType <em>Image Step Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType
	 * @generated
	 */
	public Adapter createImageStepTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType <em>Input Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType
	 * @generated
	 */
	public Adapter createInputDataTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType <em>Intensity Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType
	 * @generated
	 */
	public Adapter createIntensityTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType <em>Interpolation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType
	 * @generated
	 */
	public Adapter createInterpolationTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType <em>Memory Size Max Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType
	 * @generated
	 */
	public Adapter createMemorySizeMaxTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType <em>Memory Size Min Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType
	 * @generated
	 */
	public Adapter createMemorySizeMinTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType <em>Missed Projections Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType
	 * @generated
	 */
	public Adapter createMissedProjectionsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType <em>Missed Projections Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType
	 * @generated
	 */
	public Adapter createMissedProjectionsTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType <em>Name Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType
	 * @generated
	 */
	public Adapter createNameTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType <em>NOD Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType
	 * @generated
	 */
	public Adapter createNODTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType <em>Normalisation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType
	 * @generated
	 */
	public Adapter createNormalisationTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType <em>Num Series Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.NumSeriesType
	 * @generated
	 */
	public Adapter createNumSeriesTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType <em>Offset Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType
	 * @generated
	 */
	public Adapter createOffsetTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType <em>Orientation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType
	 * @generated
	 */
	public Adapter createOrientationTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType <em>Output Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType
	 * @generated
	 */
	public Adapter createOutputDataTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType <em>Output Width Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType
	 * @generated
	 */
	public Adapter createOutputWidthTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType <em>Polar Cartesian Interpolation Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType
	 * @generated
	 */
	public Adapter createPolarCartesianInterpolationTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType <em>Preprocessing Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType
	 * @generated
	 */
	public Adapter createPreprocessingTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType <em>Profile Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType
	 * @generated
	 */
	public Adapter createProfileTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1 <em>Profile Type Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ProfileTypeType1
	 * @generated
	 */
	public Adapter createProfileTypeType1Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType <em>Raw Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType
	 * @generated
	 */
	public Adapter createRawTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType <em>Restrictions Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType
	 * @generated
	 */
	public Adapter createRestrictionsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1 <em>Restrictions Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1
	 * @generated
	 */
	public Adapter createRestrictionsType1Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType <em>Ring Artefacts Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType
	 * @generated
	 */
	public Adapter createRingArtefactsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType <em>ROI Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType
	 * @generated
	 */
	public Adapter createROITypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType <em>Rotation Angle End Points Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType
	 * @generated
	 */
	public Adapter createRotationAngleEndPointsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType <em>Rotation Angle Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType
	 * @generated
	 */
	public Adapter createRotationAngleTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType <em>Scale Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType
	 * @generated
	 */
	public Adapter createScaleTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType <em>Shape Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType
	 * @generated
	 */
	public Adapter createShapeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1 <em>Shape Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1
	 * @generated
	 */
	public Adapter createShapeType1Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType <em>State Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType
	 * @generated
	 */
	public Adapter createStateTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType <em>Tilt Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType
	 * @generated
	 */
	public Adapter createTiltTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType <em>Transform Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType
	 * @generated
	 */
	public Adapter createTransformTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType <em>Type Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType
	 * @generated
	 */
	public Adapter createTypeTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1 <em>Type Type1</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType1
	 * @generated
	 */
	public Adapter createTypeType1Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2 <em>Type Type2</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2
	 * @generated
	 */
	public Adapter createTypeType2Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3 <em>Type Type3</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3
	 * @generated
	 */
	public Adapter createTypeType3Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4 <em>Type Type4</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType4
	 * @generated
	 */
	public Adapter createTypeType4Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5 <em>Type Type5</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType5
	 * @generated
	 */
	public Adapter createTypeType5Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6 <em>Type Type6</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6
	 * @generated
	 */
	public Adapter createTypeType6Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7 <em>Type Type7</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7
	 * @generated
	 */
	public Adapter createTypeType7Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8 <em>Type Type8</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType8
	 * @generated
	 */
	public Adapter createTypeType8Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9 <em>Type Type9</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType9
	 * @generated
	 */
	public Adapter createTypeType9Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10 <em>Type Type10</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType10
	 * @generated
	 */
	public Adapter createTypeType10Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11 <em>Type Type11</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType11
	 * @generated
	 */
	public Adapter createTypeType11Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12 <em>Type Type12</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12
	 * @generated
	 */
	public Adapter createTypeType12Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13 <em>Type Type13</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType13
	 * @generated
	 */
	public Adapter createTypeType13Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14 <em>Type Type14</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14
	 * @generated
	 */
	public Adapter createTypeType14Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15 <em>Type Type15</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType15
	 * @generated
	 */
	public Adapter createTypeType15Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16 <em>Type Type16</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16
	 * @generated
	 */
	public Adapter createTypeType16Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17 <em>Type Type17</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17
	 * @generated
	 */
	public Adapter createTypeType17Adapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType <em>Value Max Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType
	 * @generated
	 */
	public Adapter createValueMaxTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType <em>Value Min Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMinType
	 * @generated
	 */
	public Adapter createValueMinTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType <em>Value Step Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType
	 * @generated
	 */
	public Adapter createValueStepTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType <em>Window Name Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType
	 * @generated
	 */
	public Adapter createWindowNameTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //HmAdapterFactory
