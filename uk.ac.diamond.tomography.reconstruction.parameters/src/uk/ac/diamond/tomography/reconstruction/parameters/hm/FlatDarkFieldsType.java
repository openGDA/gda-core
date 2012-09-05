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
 * A representation of the model object '<em><b>Flat Dark Fields Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getFlatField <em>Flat Field</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getDarkField <em>Dark Field</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatDarkFieldsType()
 * @model extendedMetaData="name='FlatDarkFields_._type' kind='elementOnly'"
 * @generated
 */
public interface FlatDarkFieldsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Flat Field</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Flat Field</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Flat Field</em>' containment reference.
	 * @see #setFlatField(FlatFieldType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatDarkFieldsType_FlatField()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='FlatField' namespace='##targetNamespace'"
	 * @generated
	 */
	FlatFieldType getFlatField();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getFlatField <em>Flat Field</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Flat Field</em>' containment reference.
	 * @see #getFlatField()
	 * @generated
	 */
	void setFlatField(FlatFieldType value);

	/**
	 * Returns the value of the '<em><b>Dark Field</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dark Field</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dark Field</em>' containment reference.
	 * @see #setDarkField(DarkFieldType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatDarkFieldsType_DarkField()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='DarkField' namespace='##targetNamespace'"
	 * @generated
	 */
	DarkFieldType getDarkField();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType#getDarkField <em>Dark Field</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dark Field</em>' containment reference.
	 * @see #getDarkField()
	 * @generated
	 */
	void setDarkField(DarkFieldType value);

} // FlatDarkFieldsType
