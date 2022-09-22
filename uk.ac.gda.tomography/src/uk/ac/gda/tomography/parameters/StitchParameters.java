/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stitch Parameters</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.StitchParameters#getStitchingThetaAngle <em>Stitching Theta Angle</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.StitchParameters#getImageAtTheta <em>Image At Theta</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.StitchParameters#getImageAtThetaPlus90 <em>Image At Theta Plus90</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getStitchParameters()
 * @model
 * @generated
 */
public interface StitchParameters extends EObject {
	/**
	 * Returns the value of the '<em><b>Stitching Theta Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stitching Theta Angle</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stitching Theta Angle</em>' attribute.
	 * @see #isSetStitchingThetaAngle()
	 * @see #unsetStitchingThetaAngle()
	 * @see #setStitchingThetaAngle(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getStitchParameters_StitchingThetaAngle()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getStitchingThetaAngle();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.StitchParameters#getStitchingThetaAngle <em>Stitching Theta Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stitching Theta Angle</em>' attribute.
	 * @see #isSetStitchingThetaAngle()
	 * @see #unsetStitchingThetaAngle()
	 * @see #getStitchingThetaAngle()
	 * @generated
	 */
	void setStitchingThetaAngle(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.StitchParameters#getStitchingThetaAngle <em>Stitching Theta Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetStitchingThetaAngle()
	 * @see #getStitchingThetaAngle()
	 * @see #setStitchingThetaAngle(double)
	 * @generated
	 */
	void unsetStitchingThetaAngle();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.StitchParameters#getStitchingThetaAngle <em>Stitching Theta Angle</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Stitching Theta Angle</em>' attribute is set.
	 * @see #unsetStitchingThetaAngle()
	 * @see #getStitchingThetaAngle()
	 * @see #setStitchingThetaAngle(double)
	 * @generated
	 */
	boolean isSetStitchingThetaAngle();

	/**
	 * Returns the value of the '<em><b>Image At Theta</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image At Theta</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image At Theta</em>' attribute.
	 * @see #setImageAtTheta(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getStitchParameters_ImageAtTheta()
	 * @model
	 * @generated
	 */
	String getImageAtTheta();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.StitchParameters#getImageAtTheta <em>Image At Theta</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image At Theta</em>' attribute.
	 * @see #getImageAtTheta()
	 * @generated
	 */
	void setImageAtTheta(String value);

	/**
	 * Returns the value of the '<em><b>Image At Theta Plus90</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image At Theta Plus90</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image At Theta Plus90</em>' attribute.
	 * @see #setImageAtThetaPlus90(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getStitchParameters_ImageAtThetaPlus90()
	 * @model
	 * @generated
	 */
	String getImageAtThetaPlus90();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.StitchParameters#getImageAtThetaPlus90 <em>Image At Theta Plus90</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image At Theta Plus90</em>' attribute.
	 * @see #getImageAtThetaPlus90()
	 * @generated
	 */
	void setImageAtThetaPlus90(String value);

} // StitchParameters
