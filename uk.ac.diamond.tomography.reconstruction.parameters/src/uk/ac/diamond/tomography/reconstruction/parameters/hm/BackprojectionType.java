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
 * A representation of the model object '<em><b>Backprojection Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getFilter <em>Filter</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getImageCentre <em>Image Centre</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getClockwiseRotation <em>Clockwise Rotation</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getTilt <em>Tilt</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCoordinateSystem <em>Coordinate System</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCircles <em>Circles</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getROI <em>ROI</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getPolarCartesianInterpolation <em>Polar Cartesian Interpolation</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType()
 * @model extendedMetaData="name='Backprojection_._type' kind='elementOnly'"
 * @generated
 */
public interface BackprojectionType extends EObject {
	/**
	 * Returns the value of the '<em><b>Filter</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filter</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Filter</em>' containment reference.
	 * @see #setFilter(FilterType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_Filter()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Filter' namespace='##targetNamespace'"
	 * @generated
	 */
	FilterType getFilter();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getFilter <em>Filter</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filter</em>' containment reference.
	 * @see #getFilter()
	 * @generated
	 */
	void setFilter(FilterType value);

	/**
	 * Returns the value of the '<em><b>Image Centre</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image Centre</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image Centre</em>' attribute.
	 * @see #setImageCentre(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_ImageCentre()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ImageCentre' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getImageCentre();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getImageCentre <em>Image Centre</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image Centre</em>' attribute.
	 * @see #getImageCentre()
	 * @generated
	 */
	void setImageCentre(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Clockwise Rotation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Clockwise Rotation</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Clockwise Rotation</em>' containment reference.
	 * @see #setClockwiseRotation(ClockwiseRotationType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_ClockwiseRotation()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ClockwiseRotation' namespace='##targetNamespace'"
	 * @generated
	 */
	ClockwiseRotationType getClockwiseRotation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getClockwiseRotation <em>Clockwise Rotation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Clockwise Rotation</em>' containment reference.
	 * @see #getClockwiseRotation()
	 * @generated
	 */
	void setClockwiseRotation(ClockwiseRotationType value);

	/**
	 * Returns the value of the '<em><b>Tilt</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tilt</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tilt</em>' containment reference.
	 * @see #setTilt(TiltType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_Tilt()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Tilt' namespace='##targetNamespace'"
	 * @generated
	 */
	TiltType getTilt();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getTilt <em>Tilt</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tilt</em>' containment reference.
	 * @see #getTilt()
	 * @generated
	 */
	void setTilt(TiltType value);

	/**
	 * Returns the value of the '<em><b>Coordinate System</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Coordinate System</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Coordinate System</em>' containment reference.
	 * @see #setCoordinateSystem(CoordinateSystemType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_CoordinateSystem()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='CoordinateSystem' namespace='##targetNamespace'"
	 * @generated
	 */
	CoordinateSystemType getCoordinateSystem();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCoordinateSystem <em>Coordinate System</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Coordinate System</em>' containment reference.
	 * @see #getCoordinateSystem()
	 * @generated
	 */
	void setCoordinateSystem(CoordinateSystemType value);

	/**
	 * Returns the value of the '<em><b>Circles</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Circles</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Circles</em>' containment reference.
	 * @see #setCircles(CirclesType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_Circles()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Circles' namespace='##targetNamespace'"
	 * @generated
	 */
	CirclesType getCircles();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getCircles <em>Circles</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Circles</em>' containment reference.
	 * @see #getCircles()
	 * @generated
	 */
	void setCircles(CirclesType value);

	/**
	 * Returns the value of the '<em><b>ROI</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>ROI</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>ROI</em>' containment reference.
	 * @see #setROI(ROIType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_ROI()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ROI' namespace='##targetNamespace'"
	 * @generated
	 */
	ROIType getROI();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getROI <em>ROI</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>ROI</em>' containment reference.
	 * @see #getROI()
	 * @generated
	 */
	void setROI(ROIType value);

	/**
	 * Returns the value of the '<em><b>Polar Cartesian Interpolation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Polar Cartesian Interpolation</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Polar Cartesian Interpolation</em>' containment reference.
	 * @see #setPolarCartesianInterpolation(PolarCartesianInterpolationType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBackprojectionType_PolarCartesianInterpolation()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='PolarCartesianInterpolation' namespace='##targetNamespace'"
	 * @generated
	 */
	PolarCartesianInterpolationType getPolarCartesianInterpolation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType#getPolarCartesianInterpolation <em>Polar Cartesian Interpolation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Polar Cartesian Interpolation</em>' containment reference.
	 * @see #getPolarCartesianInterpolation()
	 * @generated
	 */
	void setPolarCartesianInterpolation(PolarCartesianInterpolationType value);

} // BackprojectionType
