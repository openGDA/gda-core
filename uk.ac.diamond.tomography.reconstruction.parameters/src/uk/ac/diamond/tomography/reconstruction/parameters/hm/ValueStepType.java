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
 * A representation of the model object '<em><b>Value Step Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPercent <em>Percent</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPixel <em>Pixel</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getValueStepType()
 * @model extendedMetaData="name='ValueStep_._type' kind='elementOnly'"
 * @generated
 */
public interface ValueStepType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType4)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getValueStepType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType4 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType4 value);

	/**
	 * Returns the value of the '<em><b>Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Percent</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Percent</em>' attribute.
	 * @see #isSetPercent()
	 * @see #unsetPercent()
	 * @see #setPercent(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getValueStepType_Percent()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Percent' namespace='##targetNamespace'"
	 * @generated
	 */
	int getPercent();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPercent <em>Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Percent</em>' attribute.
	 * @see #isSetPercent()
	 * @see #unsetPercent()
	 * @see #getPercent()
	 * @generated
	 */
	void setPercent(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPercent <em>Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPercent()
	 * @see #getPercent()
	 * @see #setPercent(int)
	 * @generated
	 */
	void unsetPercent();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPercent <em>Percent</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Percent</em>' attribute is set.
	 * @see #unsetPercent()
	 * @see #getPercent()
	 * @see #setPercent(int)
	 * @generated
	 */
	boolean isSetPercent();

	/**
	 * Returns the value of the '<em><b>Pixel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pixel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pixel</em>' attribute.
	 * @see #isSetPixel()
	 * @see #unsetPixel()
	 * @see #setPixel(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getValueStepType_Pixel()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Pixel' namespace='##targetNamespace'"
	 * @generated
	 */
	int getPixel();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPixel <em>Pixel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pixel</em>' attribute.
	 * @see #isSetPixel()
	 * @see #unsetPixel()
	 * @see #getPixel()
	 * @generated
	 */
	void setPixel(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPixel <em>Pixel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPixel()
	 * @see #getPixel()
	 * @see #setPixel(int)
	 * @generated
	 */
	void unsetPixel();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueStepType#getPixel <em>Pixel</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Pixel</em>' attribute is set.
	 * @see #unsetPixel()
	 * @see #getPixel()
	 * @see #setPixel(int)
	 * @generated
	 */
	boolean isSetPixel();

} // ValueStepType
