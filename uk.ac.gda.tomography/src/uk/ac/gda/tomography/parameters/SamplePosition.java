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
 * A representation of the model object '<em><b>Sample Position</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.SamplePosition#getVertical <em>Vertical</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterX <em>Center X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterZ <em>Center Z</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltX <em>Tilt X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltZ <em>Tilt Z</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSamplePosition()
 * @model
 * @generated
 */
public interface SamplePosition extends EObject {
	/**
	 * Returns the value of the '<em><b>Vertical</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Vertical</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Vertical</em>' attribute.
	 * @see #isSetVertical()
	 * @see #unsetVertical()
	 * @see #setVertical(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSamplePosition_Vertical()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getVertical();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getVertical <em>Vertical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Vertical</em>' attribute.
	 * @see #isSetVertical()
	 * @see #unsetVertical()
	 * @see #getVertical()
	 * @generated
	 */
	void setVertical(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getVertical <em>Vertical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetVertical()
	 * @see #getVertical()
	 * @see #setVertical(double)
	 * @generated
	 */
	void unsetVertical();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getVertical <em>Vertical</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Vertical</em>' attribute is set.
	 * @see #unsetVertical()
	 * @see #getVertical()
	 * @see #setVertical(double)
	 * @generated
	 */
	boolean isSetVertical();

	/**
	 * Returns the value of the '<em><b>Center X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Center X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Center X</em>' attribute.
	 * @see #isSetCenterX()
	 * @see #unsetCenterX()
	 * @see #setCenterX(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSamplePosition_CenterX()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getCenterX();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterX <em>Center X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Center X</em>' attribute.
	 * @see #isSetCenterX()
	 * @see #unsetCenterX()
	 * @see #getCenterX()
	 * @generated
	 */
	void setCenterX(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterX <em>Center X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCenterX()
	 * @see #getCenterX()
	 * @see #setCenterX(double)
	 * @generated
	 */
	void unsetCenterX();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterX <em>Center X</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Center X</em>' attribute is set.
	 * @see #unsetCenterX()
	 * @see #getCenterX()
	 * @see #setCenterX(double)
	 * @generated
	 */
	boolean isSetCenterX();

	/**
	 * Returns the value of the '<em><b>Center Z</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Center Z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Center Z</em>' attribute.
	 * @see #isSetCenterZ()
	 * @see #unsetCenterZ()
	 * @see #setCenterZ(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSamplePosition_CenterZ()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getCenterZ();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterZ <em>Center Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Center Z</em>' attribute.
	 * @see #isSetCenterZ()
	 * @see #unsetCenterZ()
	 * @see #getCenterZ()
	 * @generated
	 */
	void setCenterZ(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterZ <em>Center Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCenterZ()
	 * @see #getCenterZ()
	 * @see #setCenterZ(double)
	 * @generated
	 */
	void unsetCenterZ();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterZ <em>Center Z</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Center Z</em>' attribute is set.
	 * @see #unsetCenterZ()
	 * @see #getCenterZ()
	 * @see #setCenterZ(double)
	 * @generated
	 */
	boolean isSetCenterZ();

	/**
	 * Returns the value of the '<em><b>Tilt X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tilt X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tilt X</em>' attribute.
	 * @see #isSetTiltX()
	 * @see #unsetTiltX()
	 * @see #setTiltX(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSamplePosition_TiltX()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getTiltX();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltX <em>Tilt X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tilt X</em>' attribute.
	 * @see #isSetTiltX()
	 * @see #unsetTiltX()
	 * @see #getTiltX()
	 * @generated
	 */
	void setTiltX(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltX <em>Tilt X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetTiltX()
	 * @see #getTiltX()
	 * @see #setTiltX(double)
	 * @generated
	 */
	void unsetTiltX();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltX <em>Tilt X</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Tilt X</em>' attribute is set.
	 * @see #unsetTiltX()
	 * @see #getTiltX()
	 * @see #setTiltX(double)
	 * @generated
	 */
	boolean isSetTiltX();

	/**
	 * Returns the value of the '<em><b>Tilt Z</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tilt Z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tilt Z</em>' attribute.
	 * @see #isSetTiltZ()
	 * @see #unsetTiltZ()
	 * @see #setTiltZ(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSamplePosition_TiltZ()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getTiltZ();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltZ <em>Tilt Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tilt Z</em>' attribute.
	 * @see #isSetTiltZ()
	 * @see #unsetTiltZ()
	 * @see #getTiltZ()
	 * @generated
	 */
	void setTiltZ(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltZ <em>Tilt Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetTiltZ()
	 * @see #getTiltZ()
	 * @see #setTiltZ(double)
	 * @generated
	 */
	void unsetTiltZ();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltZ <em>Tilt Z</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Tilt Z</em>' attribute is set.
	 * @see #unsetTiltZ()
	 * @see #getTiltZ()
	 * @see #setTiltZ(double)
	 * @generated
	 */
	boolean isSetTiltZ();

} // SamplePosition
