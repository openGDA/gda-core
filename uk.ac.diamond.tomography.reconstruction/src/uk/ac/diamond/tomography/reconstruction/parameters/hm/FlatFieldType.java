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
 * A representation of the model object '<em><b>Flat Field Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueBefore <em>Value Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueAfter <em>Value After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileBefore <em>File Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileAfter <em>File After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getProfileType <em>Profile Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileProfile <em>File Profile</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType()
 * @model extendedMetaData="name='FlatField_._type' kind='elementOnly'"
 * @generated
 */
public interface FlatFieldType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType15)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType15 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType15 value);

	/**
	 * Returns the value of the '<em><b>Value Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Before</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Before</em>' attribute.
	 * @see #isSetValueBefore()
	 * @see #unsetValueBefore()
	 * @see #setValueBefore(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_ValueBefore()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ValueBefore' namespace='##targetNamespace'"
	 * @generated
	 */
	int getValueBefore();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueBefore <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Before</em>' attribute.
	 * @see #isSetValueBefore()
	 * @see #unsetValueBefore()
	 * @see #getValueBefore()
	 * @generated
	 */
	void setValueBefore(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueBefore <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetValueBefore()
	 * @see #getValueBefore()
	 * @see #setValueBefore(int)
	 * @generated
	 */
	void unsetValueBefore();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueBefore <em>Value Before</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Value Before</em>' attribute is set.
	 * @see #unsetValueBefore()
	 * @see #getValueBefore()
	 * @see #setValueBefore(int)
	 * @generated
	 */
	boolean isSetValueBefore();

	/**
	 * Returns the value of the '<em><b>Value After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value After</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value After</em>' attribute.
	 * @see #isSetValueAfter()
	 * @see #unsetValueAfter()
	 * @see #setValueAfter(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_ValueAfter()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ValueAfter' namespace='##targetNamespace'"
	 * @generated
	 */
	int getValueAfter();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueAfter <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value After</em>' attribute.
	 * @see #isSetValueAfter()
	 * @see #unsetValueAfter()
	 * @see #getValueAfter()
	 * @generated
	 */
	void setValueAfter(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueAfter <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetValueAfter()
	 * @see #getValueAfter()
	 * @see #setValueAfter(int)
	 * @generated
	 */
	void unsetValueAfter();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getValueAfter <em>Value After</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Value After</em>' attribute is set.
	 * @see #unsetValueAfter()
	 * @see #getValueAfter()
	 * @see #setValueAfter(int)
	 * @generated
	 */
	boolean isSetValueAfter();

	/**
	 * Returns the value of the '<em><b>File Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Before</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Before</em>' attribute.
	 * @see #setFileBefore(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_FileBefore()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='FileBefore' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFileBefore();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileBefore <em>File Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Before</em>' attribute.
	 * @see #getFileBefore()
	 * @generated
	 */
	void setFileBefore(String value);

	/**
	 * Returns the value of the '<em><b>File After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File After</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File After</em>' attribute.
	 * @see #setFileAfter(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_FileAfter()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='FileAfter' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFileAfter();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileAfter <em>File After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File After</em>' attribute.
	 * @see #getFileAfter()
	 * @generated
	 */
	void setFileAfter(String value);

	/**
	 * Returns the value of the '<em><b>Profile Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Profile Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Profile Type</em>' containment reference.
	 * @see #setProfileType(ProfileTypeType1)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_ProfileType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ProfileType' namespace='##targetNamespace'"
	 * @generated
	 */
	ProfileTypeType1 getProfileType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getProfileType <em>Profile Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Profile Type</em>' containment reference.
	 * @see #getProfileType()
	 * @generated
	 */
	void setProfileType(ProfileTypeType1 value);

	/**
	 * Returns the value of the '<em><b>File Profile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Profile</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Profile</em>' attribute.
	 * @see #setFileProfile(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFlatFieldType_FileProfile()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='FileProfile' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFileProfile();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType#getFileProfile <em>File Profile</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Profile</em>' attribute.
	 * @see #getFileProfile()
	 * @generated
	 */
	void setFileProfile(String value);

} // FlatFieldType
