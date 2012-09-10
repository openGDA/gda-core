/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

import java.math.BigDecimal;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Transform Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjections <em>Missed Projections</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjectionsType <em>Missed Projections Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleType <em>Rotation Angle Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngle <em>Rotation Angle</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleEndPoints <em>Rotation Angle End Points</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreAngle <em>Re Centre Angle</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreRadius <em>Re Centre Radius</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropTop <em>Crop Top</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropBottom <em>Crop Bottom</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropLeft <em>Crop Left</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropRight <em>Crop Right</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleType <em>Scale Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleWidth <em>Scale Width</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleHeight <em>Scale Height</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationType <em>Extrapolation Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationPixels <em>Extrapolation Pixels</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationWidth <em>Extrapolation Width</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getInterpolation <em>Interpolation</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType()
 * @model extendedMetaData="name='Transform_._type' kind='elementOnly'"
 * @generated
 */
public interface TransformType extends EObject {
	/**
	 * Returns the value of the '<em><b>Missed Projections</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Missed Projections</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Missed Projections</em>' containment reference.
	 * @see #setMissedProjections(MissedProjectionsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_MissedProjections()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='MissedProjections' namespace='##targetNamespace'"
	 * @generated
	 */
	MissedProjectionsType getMissedProjections();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjections <em>Missed Projections</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Missed Projections</em>' containment reference.
	 * @see #getMissedProjections()
	 * @generated
	 */
	void setMissedProjections(MissedProjectionsType value);

	/**
	 * Returns the value of the '<em><b>Missed Projections Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Missed Projections Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Missed Projections Type</em>' containment reference.
	 * @see #setMissedProjectionsType(MissedProjectionsTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_MissedProjectionsType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='MissedProjectionsType' namespace='##targetNamespace'"
	 * @generated
	 */
	MissedProjectionsTypeType getMissedProjectionsType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getMissedProjectionsType <em>Missed Projections Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Missed Projections Type</em>' containment reference.
	 * @see #getMissedProjectionsType()
	 * @generated
	 */
	void setMissedProjectionsType(MissedProjectionsTypeType value);

	/**
	 * Returns the value of the '<em><b>Rotation Angle Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rotation Angle Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rotation Angle Type</em>' containment reference.
	 * @see #setRotationAngleType(RotationAngleTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_RotationAngleType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='RotationAngleType' namespace='##targetNamespace'"
	 * @generated
	 */
	RotationAngleTypeType getRotationAngleType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleType <em>Rotation Angle Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rotation Angle Type</em>' containment reference.
	 * @see #getRotationAngleType()
	 * @generated
	 */
	void setRotationAngleType(RotationAngleTypeType value);

	/**
	 * Returns the value of the '<em><b>Rotation Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rotation Angle</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rotation Angle</em>' attribute.
	 * @see #isSetRotationAngle()
	 * @see #unsetRotationAngle()
	 * @see #setRotationAngle(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_RotationAngle()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='RotationAngle' namespace='##targetNamespace'"
	 * @generated
	 */
	int getRotationAngle();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngle <em>Rotation Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rotation Angle</em>' attribute.
	 * @see #isSetRotationAngle()
	 * @see #unsetRotationAngle()
	 * @see #getRotationAngle()
	 * @generated
	 */
	void setRotationAngle(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngle <em>Rotation Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRotationAngle()
	 * @see #getRotationAngle()
	 * @see #setRotationAngle(int)
	 * @generated
	 */
	void unsetRotationAngle();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngle <em>Rotation Angle</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Rotation Angle</em>' attribute is set.
	 * @see #unsetRotationAngle()
	 * @see #getRotationAngle()
	 * @see #setRotationAngle(int)
	 * @generated
	 */
	boolean isSetRotationAngle();

	/**
	 * Returns the value of the '<em><b>Rotation Angle End Points</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rotation Angle End Points</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rotation Angle End Points</em>' containment reference.
	 * @see #setRotationAngleEndPoints(RotationAngleEndPointsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_RotationAngleEndPoints()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='RotationAngleEndPoints' namespace='##targetNamespace'"
	 * @generated
	 */
	RotationAngleEndPointsType getRotationAngleEndPoints();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getRotationAngleEndPoints <em>Rotation Angle End Points</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rotation Angle End Points</em>' containment reference.
	 * @see #getRotationAngleEndPoints()
	 * @generated
	 */
	void setRotationAngleEndPoints(RotationAngleEndPointsType value);

	/**
	 * Returns the value of the '<em><b>Re Centre Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Re Centre Angle</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Re Centre Angle</em>' attribute.
	 * @see #setReCentreAngle(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ReCentreAngle()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ReCentreAngle' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getReCentreAngle();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreAngle <em>Re Centre Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Re Centre Angle</em>' attribute.
	 * @see #getReCentreAngle()
	 * @generated
	 */
	void setReCentreAngle(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Re Centre Radius</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Re Centre Radius</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Re Centre Radius</em>' attribute.
	 * @see #setReCentreRadius(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ReCentreRadius()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ReCentreRadius' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getReCentreRadius();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getReCentreRadius <em>Re Centre Radius</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Re Centre Radius</em>' attribute.
	 * @see #getReCentreRadius()
	 * @generated
	 */
	void setReCentreRadius(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Crop Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Crop Top</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Crop Top</em>' attribute.
	 * @see #isSetCropTop()
	 * @see #unsetCropTop()
	 * @see #setCropTop(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_CropTop()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='CropTop' namespace='##targetNamespace'"
	 * @generated
	 */
	int getCropTop();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropTop <em>Crop Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Crop Top</em>' attribute.
	 * @see #isSetCropTop()
	 * @see #unsetCropTop()
	 * @see #getCropTop()
	 * @generated
	 */
	void setCropTop(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropTop <em>Crop Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCropTop()
	 * @see #getCropTop()
	 * @see #setCropTop(int)
	 * @generated
	 */
	void unsetCropTop();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropTop <em>Crop Top</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Crop Top</em>' attribute is set.
	 * @see #unsetCropTop()
	 * @see #getCropTop()
	 * @see #setCropTop(int)
	 * @generated
	 */
	boolean isSetCropTop();

	/**
	 * Returns the value of the '<em><b>Crop Bottom</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Crop Bottom</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Crop Bottom</em>' attribute.
	 * @see #isSetCropBottom()
	 * @see #unsetCropBottom()
	 * @see #setCropBottom(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_CropBottom()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='CropBottom' namespace='##targetNamespace'"
	 * @generated
	 */
	int getCropBottom();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropBottom <em>Crop Bottom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Crop Bottom</em>' attribute.
	 * @see #isSetCropBottom()
	 * @see #unsetCropBottom()
	 * @see #getCropBottom()
	 * @generated
	 */
	void setCropBottom(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropBottom <em>Crop Bottom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCropBottom()
	 * @see #getCropBottom()
	 * @see #setCropBottom(int)
	 * @generated
	 */
	void unsetCropBottom();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropBottom <em>Crop Bottom</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Crop Bottom</em>' attribute is set.
	 * @see #unsetCropBottom()
	 * @see #getCropBottom()
	 * @see #setCropBottom(int)
	 * @generated
	 */
	boolean isSetCropBottom();

	/**
	 * Returns the value of the '<em><b>Crop Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Crop Left</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Crop Left</em>' attribute.
	 * @see #isSetCropLeft()
	 * @see #unsetCropLeft()
	 * @see #setCropLeft(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_CropLeft()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='CropLeft' namespace='##targetNamespace'"
	 * @generated
	 */
	int getCropLeft();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropLeft <em>Crop Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Crop Left</em>' attribute.
	 * @see #isSetCropLeft()
	 * @see #unsetCropLeft()
	 * @see #getCropLeft()
	 * @generated
	 */
	void setCropLeft(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropLeft <em>Crop Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCropLeft()
	 * @see #getCropLeft()
	 * @see #setCropLeft(int)
	 * @generated
	 */
	void unsetCropLeft();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropLeft <em>Crop Left</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Crop Left</em>' attribute is set.
	 * @see #unsetCropLeft()
	 * @see #getCropLeft()
	 * @see #setCropLeft(int)
	 * @generated
	 */
	boolean isSetCropLeft();

	/**
	 * Returns the value of the '<em><b>Crop Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Crop Right</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Crop Right</em>' attribute.
	 * @see #isSetCropRight()
	 * @see #unsetCropRight()
	 * @see #setCropRight(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_CropRight()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='CropRight' namespace='##targetNamespace'"
	 * @generated
	 */
	int getCropRight();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropRight <em>Crop Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Crop Right</em>' attribute.
	 * @see #isSetCropRight()
	 * @see #unsetCropRight()
	 * @see #getCropRight()
	 * @generated
	 */
	void setCropRight(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropRight <em>Crop Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCropRight()
	 * @see #getCropRight()
	 * @see #setCropRight(int)
	 * @generated
	 */
	void unsetCropRight();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getCropRight <em>Crop Right</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Crop Right</em>' attribute is set.
	 * @see #unsetCropRight()
	 * @see #getCropRight()
	 * @see #setCropRight(int)
	 * @generated
	 */
	boolean isSetCropRight();

	/**
	 * Returns the value of the '<em><b>Scale Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scale Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scale Type</em>' containment reference.
	 * @see #setScaleType(ScaleTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ScaleType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ScaleType' namespace='##targetNamespace'"
	 * @generated
	 */
	ScaleTypeType getScaleType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleType <em>Scale Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scale Type</em>' containment reference.
	 * @see #getScaleType()
	 * @generated
	 */
	void setScaleType(ScaleTypeType value);

	/**
	 * Returns the value of the '<em><b>Scale Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scale Width</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scale Width</em>' attribute.
	 * @see #isSetScaleWidth()
	 * @see #unsetScaleWidth()
	 * @see #setScaleWidth(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ScaleWidth()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ScaleWidth' namespace='##targetNamespace'"
	 * @generated
	 */
	int getScaleWidth();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleWidth <em>Scale Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scale Width</em>' attribute.
	 * @see #isSetScaleWidth()
	 * @see #unsetScaleWidth()
	 * @see #getScaleWidth()
	 * @generated
	 */
	void setScaleWidth(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleWidth <em>Scale Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetScaleWidth()
	 * @see #getScaleWidth()
	 * @see #setScaleWidth(int)
	 * @generated
	 */
	void unsetScaleWidth();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleWidth <em>Scale Width</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Scale Width</em>' attribute is set.
	 * @see #unsetScaleWidth()
	 * @see #getScaleWidth()
	 * @see #setScaleWidth(int)
	 * @generated
	 */
	boolean isSetScaleWidth();

	/**
	 * Returns the value of the '<em><b>Scale Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scale Height</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scale Height</em>' attribute.
	 * @see #isSetScaleHeight()
	 * @see #unsetScaleHeight()
	 * @see #setScaleHeight(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ScaleHeight()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ScaleHeight' namespace='##targetNamespace'"
	 * @generated
	 */
	int getScaleHeight();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleHeight <em>Scale Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scale Height</em>' attribute.
	 * @see #isSetScaleHeight()
	 * @see #unsetScaleHeight()
	 * @see #getScaleHeight()
	 * @generated
	 */
	void setScaleHeight(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleHeight <em>Scale Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetScaleHeight()
	 * @see #getScaleHeight()
	 * @see #setScaleHeight(int)
	 * @generated
	 */
	void unsetScaleHeight();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getScaleHeight <em>Scale Height</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Scale Height</em>' attribute is set.
	 * @see #unsetScaleHeight()
	 * @see #getScaleHeight()
	 * @see #setScaleHeight(int)
	 * @generated
	 */
	boolean isSetScaleHeight();

	/**
	 * Returns the value of the '<em><b>Extrapolation Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extrapolation Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extrapolation Type</em>' containment reference.
	 * @see #setExtrapolationType(ExtrapolationTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ExtrapolationType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ExtrapolationType' namespace='##targetNamespace'"
	 * @generated
	 */
	ExtrapolationTypeType getExtrapolationType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationType <em>Extrapolation Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extrapolation Type</em>' containment reference.
	 * @see #getExtrapolationType()
	 * @generated
	 */
	void setExtrapolationType(ExtrapolationTypeType value);

	/**
	 * Returns the value of the '<em><b>Extrapolation Pixels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extrapolation Pixels</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extrapolation Pixels</em>' attribute.
	 * @see #isSetExtrapolationPixels()
	 * @see #unsetExtrapolationPixels()
	 * @see #setExtrapolationPixels(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ExtrapolationPixels()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ExtrapolationPixels' namespace='##targetNamespace'"
	 * @generated
	 */
	int getExtrapolationPixels();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationPixels <em>Extrapolation Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extrapolation Pixels</em>' attribute.
	 * @see #isSetExtrapolationPixels()
	 * @see #unsetExtrapolationPixels()
	 * @see #getExtrapolationPixels()
	 * @generated
	 */
	void setExtrapolationPixels(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationPixels <em>Extrapolation Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetExtrapolationPixels()
	 * @see #getExtrapolationPixels()
	 * @see #setExtrapolationPixels(int)
	 * @generated
	 */
	void unsetExtrapolationPixels();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationPixels <em>Extrapolation Pixels</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Extrapolation Pixels</em>' attribute is set.
	 * @see #unsetExtrapolationPixels()
	 * @see #getExtrapolationPixels()
	 * @see #setExtrapolationPixels(int)
	 * @generated
	 */
	boolean isSetExtrapolationPixels();

	/**
	 * Returns the value of the '<em><b>Extrapolation Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extrapolation Width</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extrapolation Width</em>' attribute.
	 * @see #isSetExtrapolationWidth()
	 * @see #unsetExtrapolationWidth()
	 * @see #setExtrapolationWidth(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_ExtrapolationWidth()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ExtrapolationWidth' namespace='##targetNamespace'"
	 * @generated
	 */
	int getExtrapolationWidth();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationWidth <em>Extrapolation Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extrapolation Width</em>' attribute.
	 * @see #isSetExtrapolationWidth()
	 * @see #unsetExtrapolationWidth()
	 * @see #getExtrapolationWidth()
	 * @generated
	 */
	void setExtrapolationWidth(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationWidth <em>Extrapolation Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetExtrapolationWidth()
	 * @see #getExtrapolationWidth()
	 * @see #setExtrapolationWidth(int)
	 * @generated
	 */
	void unsetExtrapolationWidth();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getExtrapolationWidth <em>Extrapolation Width</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Extrapolation Width</em>' attribute is set.
	 * @see #unsetExtrapolationWidth()
	 * @see #getExtrapolationWidth()
	 * @see #setExtrapolationWidth(int)
	 * @generated
	 */
	boolean isSetExtrapolationWidth();

	/**
	 * Returns the value of the '<em><b>Interpolation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Interpolation</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Interpolation</em>' containment reference.
	 * @see #setInterpolation(InterpolationType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTransformType_Interpolation()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Interpolation' namespace='##targetNamespace'"
	 * @generated
	 */
	InterpolationType getInterpolation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType#getInterpolation <em>Interpolation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Interpolation</em>' containment reference.
	 * @see #getInterpolation()
	 * @generated
	 */
	void setInterpolation(InterpolationType value);

} // TransformType
