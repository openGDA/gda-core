/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.*;

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
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage
 * @generated
 */
@SuppressWarnings("unused")
public class HmSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static HmPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HmSwitch() {
		if (modelPackage == null) {
			modelPackage = HmPackage.eINSTANCE;
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
			case HmPackage.BACKPROJECTION_TYPE: {
				BackprojectionType backprojectionType = (BackprojectionType)theEObject;
				T result = caseBackprojectionType(backprojectionType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.BEAMLINE_USER_TYPE: {
				BeamlineUserType beamlineUserType = (BeamlineUserType)theEObject;
				T result = caseBeamlineUserType(beamlineUserType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.BITS_TYPE_TYPE: {
				BitsTypeType bitsTypeType = (BitsTypeType)theEObject;
				T result = caseBitsTypeType(bitsTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.BYTE_ORDER_TYPE: {
				ByteOrderType byteOrderType = (ByteOrderType)theEObject;
				T result = caseByteOrderType(byteOrderType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.CIRCLES_TYPE: {
				CirclesType circlesType = (CirclesType)theEObject;
				T result = caseCirclesType(circlesType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.CLOCKWISE_ROTATION_TYPE: {
				ClockwiseRotationType clockwiseRotationType = (ClockwiseRotationType)theEObject;
				T result = caseClockwiseRotationType(clockwiseRotationType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.COORDINATE_SYSTEM_TYPE: {
				CoordinateSystemType coordinateSystemType = (CoordinateSystemType)theEObject;
				T result = caseCoordinateSystemType(coordinateSystemType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.DARK_FIELD_TYPE: {
				DarkFieldType darkFieldType = (DarkFieldType)theEObject;
				T result = caseDarkFieldType(darkFieldType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.DEFAULT_XML_TYPE: {
				DefaultXmlType defaultXmlType = (DefaultXmlType)theEObject;
				T result = caseDefaultXmlType(defaultXmlType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.DOCUMENT_ROOT: {
				DocumentRoot documentRoot = (DocumentRoot)theEObject;
				T result = caseDocumentRoot(documentRoot);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.EXTRAPOLATION_TYPE_TYPE: {
				ExtrapolationTypeType extrapolationTypeType = (ExtrapolationTypeType)theEObject;
				T result = caseExtrapolationTypeType(extrapolationTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.FBP_TYPE: {
				FBPType fbpType = (FBPType)theEObject;
				T result = caseFBPType(fbpType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.FILTER_TYPE: {
				FilterType filterType = (FilterType)theEObject;
				T result = caseFilterType(filterType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.FIRST_IMAGE_INDEX_TYPE: {
				FirstImageIndexType firstImageIndexType = (FirstImageIndexType)theEObject;
				T result = caseFirstImageIndexType(firstImageIndexType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.FLAT_DARK_FIELDS_TYPE: {
				FlatDarkFieldsType flatDarkFieldsType = (FlatDarkFieldsType)theEObject;
				T result = caseFlatDarkFieldsType(flatDarkFieldsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.FLAT_FIELD_TYPE: {
				FlatFieldType flatFieldType = (FlatFieldType)theEObject;
				T result = caseFlatFieldType(flatFieldType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.GAP_TYPE: {
				GapType gapType = (GapType)theEObject;
				T result = caseGapType(gapType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.HIGH_PEAKS_AFTER_COLUMNS_TYPE: {
				HighPeaksAfterColumnsType highPeaksAfterColumnsType = (HighPeaksAfterColumnsType)theEObject;
				T result = caseHighPeaksAfterColumnsType(highPeaksAfterColumnsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE: {
				HighPeaksAfterRowsType highPeaksAfterRowsType = (HighPeaksAfterRowsType)theEObject;
				T result = caseHighPeaksAfterRowsType(highPeaksAfterRowsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.HIGH_PEAKS_BEFORE_TYPE: {
				HighPeaksBeforeType highPeaksBeforeType = (HighPeaksBeforeType)theEObject;
				T result = caseHighPeaksBeforeType(highPeaksBeforeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.HMXML_TYPE: {
				HMxmlType hMxmlType = (HMxmlType)theEObject;
				T result = caseHMxmlType(hMxmlType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.IMAGE_FIRST_TYPE: {
				ImageFirstType imageFirstType = (ImageFirstType)theEObject;
				T result = caseImageFirstType(imageFirstType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.IMAGE_LAST_TYPE: {
				ImageLastType imageLastType = (ImageLastType)theEObject;
				T result = caseImageLastType(imageLastType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.IMAGE_STEP_TYPE: {
				ImageStepType imageStepType = (ImageStepType)theEObject;
				T result = caseImageStepType(imageStepType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.INPUT_DATA_TYPE: {
				InputDataType inputDataType = (InputDataType)theEObject;
				T result = caseInputDataType(inputDataType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.INTENSITY_TYPE: {
				IntensityType intensityType = (IntensityType)theEObject;
				T result = caseIntensityType(intensityType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.INTERPOLATION_TYPE: {
				InterpolationType interpolationType = (InterpolationType)theEObject;
				T result = caseInterpolationType(interpolationType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.MEMORY_SIZE_MAX_TYPE: {
				MemorySizeMaxType memorySizeMaxType = (MemorySizeMaxType)theEObject;
				T result = caseMemorySizeMaxType(memorySizeMaxType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.MEMORY_SIZE_MIN_TYPE: {
				MemorySizeMinType memorySizeMinType = (MemorySizeMinType)theEObject;
				T result = caseMemorySizeMinType(memorySizeMinType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.MISSED_PROJECTIONS_TYPE: {
				MissedProjectionsType missedProjectionsType = (MissedProjectionsType)theEObject;
				T result = caseMissedProjectionsType(missedProjectionsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.MISSED_PROJECTIONS_TYPE_TYPE: {
				MissedProjectionsTypeType missedProjectionsTypeType = (MissedProjectionsTypeType)theEObject;
				T result = caseMissedProjectionsTypeType(missedProjectionsTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.NAME_TYPE: {
				NameType nameType = (NameType)theEObject;
				T result = caseNameType(nameType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.NOD_TYPE: {
				NODType nodType = (NODType)theEObject;
				T result = caseNODType(nodType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.NORMALISATION_TYPE: {
				NormalisationType normalisationType = (NormalisationType)theEObject;
				T result = caseNormalisationType(normalisationType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.NUM_SERIES_TYPE: {
				NumSeriesType numSeriesType = (NumSeriesType)theEObject;
				T result = caseNumSeriesType(numSeriesType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.OFFSET_TYPE: {
				OffsetType offsetType = (OffsetType)theEObject;
				T result = caseOffsetType(offsetType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.ORIENTATION_TYPE: {
				OrientationType orientationType = (OrientationType)theEObject;
				T result = caseOrientationType(orientationType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.OUTPUT_DATA_TYPE: {
				OutputDataType outputDataType = (OutputDataType)theEObject;
				T result = caseOutputDataType(outputDataType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.OUTPUT_WIDTH_TYPE_TYPE: {
				OutputWidthTypeType outputWidthTypeType = (OutputWidthTypeType)theEObject;
				T result = caseOutputWidthTypeType(outputWidthTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.POLAR_CARTESIAN_INTERPOLATION_TYPE: {
				PolarCartesianInterpolationType polarCartesianInterpolationType = (PolarCartesianInterpolationType)theEObject;
				T result = casePolarCartesianInterpolationType(polarCartesianInterpolationType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.PREPROCESSING_TYPE: {
				PreprocessingType preprocessingType = (PreprocessingType)theEObject;
				T result = casePreprocessingType(preprocessingType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.PROFILE_TYPE_TYPE: {
				ProfileTypeType profileTypeType = (ProfileTypeType)theEObject;
				T result = caseProfileTypeType(profileTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.PROFILE_TYPE_TYPE1: {
				ProfileTypeType1 profileTypeType1 = (ProfileTypeType1)theEObject;
				T result = caseProfileTypeType1(profileTypeType1);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.RAW_TYPE: {
				RawType rawType = (RawType)theEObject;
				T result = caseRawType(rawType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.RESTRICTIONS_TYPE: {
				RestrictionsType restrictionsType = (RestrictionsType)theEObject;
				T result = caseRestrictionsType(restrictionsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.RESTRICTIONS_TYPE1: {
				RestrictionsType1 restrictionsType1 = (RestrictionsType1)theEObject;
				T result = caseRestrictionsType1(restrictionsType1);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.RING_ARTEFACTS_TYPE: {
				RingArtefactsType ringArtefactsType = (RingArtefactsType)theEObject;
				T result = caseRingArtefactsType(ringArtefactsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.ROI_TYPE: {
				ROIType roiType = (ROIType)theEObject;
				T result = caseROIType(roiType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.ROTATION_ANGLE_END_POINTS_TYPE: {
				RotationAngleEndPointsType rotationAngleEndPointsType = (RotationAngleEndPointsType)theEObject;
				T result = caseRotationAngleEndPointsType(rotationAngleEndPointsType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.ROTATION_ANGLE_TYPE_TYPE: {
				RotationAngleTypeType rotationAngleTypeType = (RotationAngleTypeType)theEObject;
				T result = caseRotationAngleTypeType(rotationAngleTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.SCALE_TYPE_TYPE: {
				ScaleTypeType scaleTypeType = (ScaleTypeType)theEObject;
				T result = caseScaleTypeType(scaleTypeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.SHAPE_TYPE: {
				ShapeType shapeType = (ShapeType)theEObject;
				T result = caseShapeType(shapeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.SHAPE_TYPE1: {
				ShapeType1 shapeType1 = (ShapeType1)theEObject;
				T result = caseShapeType1(shapeType1);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.STATE_TYPE: {
				StateType stateType = (StateType)theEObject;
				T result = caseStateType(stateType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TILT_TYPE: {
				TiltType tiltType = (TiltType)theEObject;
				T result = caseTiltType(tiltType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TRANSFORM_TYPE: {
				TransformType transformType = (TransformType)theEObject;
				T result = caseTransformType(transformType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE: {
				TypeType typeType = (TypeType)theEObject;
				T result = caseTypeType(typeType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE1: {
				TypeType1 typeType1 = (TypeType1)theEObject;
				T result = caseTypeType1(typeType1);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE2: {
				TypeType2 typeType2 = (TypeType2)theEObject;
				T result = caseTypeType2(typeType2);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE3: {
				TypeType3 typeType3 = (TypeType3)theEObject;
				T result = caseTypeType3(typeType3);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE4: {
				TypeType4 typeType4 = (TypeType4)theEObject;
				T result = caseTypeType4(typeType4);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE5: {
				TypeType5 typeType5 = (TypeType5)theEObject;
				T result = caseTypeType5(typeType5);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE6: {
				TypeType6 typeType6 = (TypeType6)theEObject;
				T result = caseTypeType6(typeType6);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE7: {
				TypeType7 typeType7 = (TypeType7)theEObject;
				T result = caseTypeType7(typeType7);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE8: {
				TypeType8 typeType8 = (TypeType8)theEObject;
				T result = caseTypeType8(typeType8);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE9: {
				TypeType9 typeType9 = (TypeType9)theEObject;
				T result = caseTypeType9(typeType9);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE10: {
				TypeType10 typeType10 = (TypeType10)theEObject;
				T result = caseTypeType10(typeType10);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE11: {
				TypeType11 typeType11 = (TypeType11)theEObject;
				T result = caseTypeType11(typeType11);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE12: {
				TypeType12 typeType12 = (TypeType12)theEObject;
				T result = caseTypeType12(typeType12);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE13: {
				TypeType13 typeType13 = (TypeType13)theEObject;
				T result = caseTypeType13(typeType13);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE14: {
				TypeType14 typeType14 = (TypeType14)theEObject;
				T result = caseTypeType14(typeType14);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE15: {
				TypeType15 typeType15 = (TypeType15)theEObject;
				T result = caseTypeType15(typeType15);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE16: {
				TypeType16 typeType16 = (TypeType16)theEObject;
				T result = caseTypeType16(typeType16);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.TYPE_TYPE17: {
				TypeType17 typeType17 = (TypeType17)theEObject;
				T result = caseTypeType17(typeType17);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.VALUE_MAX_TYPE: {
				ValueMaxType valueMaxType = (ValueMaxType)theEObject;
				T result = caseValueMaxType(valueMaxType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.VALUE_MIN_TYPE: {
				ValueMinType valueMinType = (ValueMinType)theEObject;
				T result = caseValueMinType(valueMinType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.VALUE_STEP_TYPE: {
				ValueStepType valueStepType = (ValueStepType)theEObject;
				T result = caseValueStepType(valueStepType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case HmPackage.WINDOW_NAME_TYPE: {
				WindowNameType windowNameType = (WindowNameType)theEObject;
				T result = caseWindowNameType(windowNameType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Backprojection Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Backprojection Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBackprojectionType(BackprojectionType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Beamline User Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Beamline User Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBeamlineUserType(BeamlineUserType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Bits Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Bits Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBitsTypeType(BitsTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Byte Order Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Byte Order Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseByteOrderType(ByteOrderType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Circles Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Circles Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCirclesType(CirclesType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Clockwise Rotation Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Clockwise Rotation Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClockwiseRotationType(ClockwiseRotationType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Coordinate System Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Coordinate System Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCoordinateSystemType(CoordinateSystemType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Dark Field Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Dark Field Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDarkFieldType(DarkFieldType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Default Xml Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Default Xml Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDefaultXmlType(DefaultXmlType object) {
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
	 * Returns the result of interpreting the object as an instance of '<em>Extrapolation Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Extrapolation Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExtrapolationTypeType(ExtrapolationTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>FBP Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>FBP Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFBPType(FBPType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Filter Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Filter Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFilterType(FilterType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>First Image Index Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>First Image Index Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFirstImageIndexType(FirstImageIndexType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Flat Dark Fields Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Flat Dark Fields Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFlatDarkFieldsType(FlatDarkFieldsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Flat Field Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Flat Field Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFlatFieldType(FlatFieldType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Gap Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Gap Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGapType(GapType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>High Peaks After Columns Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>High Peaks After Columns Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHighPeaksAfterColumnsType(HighPeaksAfterColumnsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>High Peaks After Rows Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>High Peaks After Rows Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHighPeaksAfterRowsType(HighPeaksAfterRowsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>High Peaks Before Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>High Peaks Before Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHighPeaksBeforeType(HighPeaksBeforeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>HMxml Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>HMxml Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHMxmlType(HMxmlType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Image First Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Image First Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImageFirstType(ImageFirstType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Image Last Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Image Last Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImageLastType(ImageLastType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Image Step Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Image Step Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImageStepType(ImageStepType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Input Data Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Input Data Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseInputDataType(InputDataType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Intensity Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Intensity Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIntensityType(IntensityType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Interpolation Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Interpolation Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseInterpolationType(InterpolationType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Memory Size Max Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Memory Size Max Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMemorySizeMaxType(MemorySizeMaxType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Memory Size Min Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Memory Size Min Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMemorySizeMinType(MemorySizeMinType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Missed Projections Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Missed Projections Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMissedProjectionsType(MissedProjectionsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Missed Projections Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Missed Projections Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMissedProjectionsTypeType(MissedProjectionsTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Name Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Name Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNameType(NameType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>NOD Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>NOD Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNODType(NODType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Normalisation Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Normalisation Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNormalisationType(NormalisationType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Num Series Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Num Series Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNumSeriesType(NumSeriesType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Offset Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Offset Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOffsetType(OffsetType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Orientation Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Orientation Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOrientationType(OrientationType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Output Data Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Output Data Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOutputDataType(OutputDataType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Output Width Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Output Width Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOutputWidthTypeType(OutputWidthTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Polar Cartesian Interpolation Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Polar Cartesian Interpolation Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePolarCartesianInterpolationType(PolarCartesianInterpolationType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Preprocessing Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Preprocessing Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePreprocessingType(PreprocessingType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Profile Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Profile Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseProfileTypeType(ProfileTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Profile Type Type1</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Profile Type Type1</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseProfileTypeType1(ProfileTypeType1 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Raw Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Raw Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRawType(RawType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Restrictions Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Restrictions Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRestrictionsType(RestrictionsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Restrictions Type1</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Restrictions Type1</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRestrictionsType1(RestrictionsType1 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Ring Artefacts Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Ring Artefacts Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRingArtefactsType(RingArtefactsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ROI Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ROI Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseROIType(ROIType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rotation Angle End Points Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rotation Angle End Points Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRotationAngleEndPointsType(RotationAngleEndPointsType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rotation Angle Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rotation Angle Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRotationAngleTypeType(RotationAngleTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Scale Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Scale Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseScaleTypeType(ScaleTypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shape Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shape Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShapeType(ShapeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shape Type1</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shape Type1</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShapeType1(ShapeType1 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>State Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>State Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStateType(StateType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tilt Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tilt Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTiltType(TiltType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Transform Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Transform Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTransformType(TransformType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType(TypeType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type1</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type1</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType1(TypeType1 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type2</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type2</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType2(TypeType2 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type3</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type3</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType3(TypeType3 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type4</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type4</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType4(TypeType4 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type5</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type5</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType5(TypeType5 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type6</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type6</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType6(TypeType6 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type7</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type7</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType7(TypeType7 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type8</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type8</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType8(TypeType8 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type9</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type9</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType9(TypeType9 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type10</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type10</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType10(TypeType10 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type11</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type11</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType11(TypeType11 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type12</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type12</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType12(TypeType12 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type13</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type13</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType13(TypeType13 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type14</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type14</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType14(TypeType14 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type15</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type15</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType15(TypeType15 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type16</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type16</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType16(TypeType16 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Type17</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Type17</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeType17(TypeType17 object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Value Max Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Value Max Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseValueMaxType(ValueMaxType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Value Min Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Value Min Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseValueMinType(ValueMinType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Value Step Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Value Step Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseValueStepType(ValueStepType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Window Name Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Window Name Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseWindowNameType(WindowNameType object) {
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

} //HmSwitch
