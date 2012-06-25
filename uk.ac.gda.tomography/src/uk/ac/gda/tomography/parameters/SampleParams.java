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
 * A representation of the model object '<em><b>Sample Params</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleParams#getPosition <em>Position</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleParams#getWeight <em>Weight</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleParams()
 * @model
 * @generated
 */
public interface SampleParams extends EObject {
	/**
	 * Returns the value of the '<em><b>Position</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Position</em>' containment reference.
	 * @see #isSetPosition()
	 * @see #unsetPosition()
	 * @see #setPosition(SamplePosition)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleParams_Position()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	SamplePosition getPosition();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleParams#getPosition <em>Position</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position</em>' containment reference.
	 * @see #isSetPosition()
	 * @see #unsetPosition()
	 * @see #getPosition()
	 * @generated
	 */
	void setPosition(SamplePosition value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleParams#getPosition <em>Position</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPosition()
	 * @see #getPosition()
	 * @see #setPosition(SamplePosition)
	 * @generated
	 */
	void unsetPosition();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleParams#getPosition <em>Position</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Position</em>' containment reference is set.
	 * @see #unsetPosition()
	 * @see #getPosition()
	 * @see #setPosition(SamplePosition)
	 * @generated
	 */
	boolean isSetPosition();

	/**
	 * Returns the value of the '<em><b>Weight</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Weight</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Weight</em>' attribute.
	 * @see #isSetWeight()
	 * @see #unsetWeight()
	 * @see #setWeight(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleParams_Weight()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getWeight();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleParams#getWeight <em>Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Weight</em>' attribute.
	 * @see #isSetWeight()
	 * @see #unsetWeight()
	 * @see #getWeight()
	 * @generated
	 */
	void setWeight(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleParams#getWeight <em>Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetWeight()
	 * @see #getWeight()
	 * @see #setWeight(double)
	 * @generated
	 */
	void unsetWeight();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleParams#getWeight <em>Weight</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Weight</em>' attribute is set.
	 * @see #unsetWeight()
	 * @see #getWeight()
	 * @see #setWeight(double)
	 * @generated
	 */
	boolean isSetWeight();

} // SampleParams
