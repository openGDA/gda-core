/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Circles Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMin <em>Value Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMax <em>Value Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueStep <em>Value Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getComm <em>Comm</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getCirclesType()
 * @model extendedMetaData="name='Circles_._type' kind='elementOnly'"
 * @generated
 */
public interface CirclesType extends EObject {
	/**
	 * Returns the value of the '<em><b>Value Min</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Min</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Min</em>' containment reference.
	 * @see #setValueMin(ValueMinType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getCirclesType_ValueMin()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ValueMin' namespace='##targetNamespace'"
	 * @generated
	 */
	ValueMinType getValueMin();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMin <em>Value Min</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Min</em>' containment reference.
	 * @see #getValueMin()
	 * @generated
	 */
	void setValueMin(ValueMinType value);

	/**
	 * Returns the value of the '<em><b>Value Max</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Max</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Max</em>' containment reference.
	 * @see #setValueMax(ValueMaxType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getCirclesType_ValueMax()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ValueMax' namespace='##targetNamespace'"
	 * @generated
	 */
	ValueMaxType getValueMax();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueMax <em>Value Max</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Max</em>' containment reference.
	 * @see #getValueMax()
	 * @generated
	 */
	void setValueMax(ValueMaxType value);

	/**
	 * Returns the value of the '<em><b>Value Step</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Step</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Step</em>' containment reference.
	 * @see #setValueStep(ValueStepType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getCirclesType_ValueStep()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ValueStep' namespace='##targetNamespace'"
	 * @generated
	 */
	ValueStepType getValueStep();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getValueStep <em>Value Step</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Step</em>' containment reference.
	 * @see #getValueStep()
	 * @generated
	 */
	void setValueStep(ValueStepType value);

	/**
	 * Returns the value of the '<em><b>Comm</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comm</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comm</em>' attribute.
	 * @see #setComm(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getCirclesType_Comm()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
	 *        extendedMetaData="kind='attribute' name='comm' namespace='##targetNamespace'"
	 * @generated
	 */
	String getComm();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType#getComm <em>Comm</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comm</em>' attribute.
	 * @see #getComm()
	 * @generated
	 */
	void setComm(String value);

} // CirclesType
