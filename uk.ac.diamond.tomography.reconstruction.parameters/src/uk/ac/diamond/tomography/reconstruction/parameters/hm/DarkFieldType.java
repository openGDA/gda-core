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
 * A representation of the model object '<em><b>Dark Field Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueBefore <em>Value Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueAfter <em>Value After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileBefore <em>File Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileAfter <em>File After</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getProfileType <em>Profile Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileProfile <em>File Profile</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType()
 * @model extendedMetaData="name='DarkField_._type' kind='elementOnly'"
 * @generated
 */
public interface DarkFieldType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType13)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType13 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType13 value);

	/**
	 * Returns the value of the '<em><b>Value Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Before</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Before</em>' attribute.
	 * @see #setValueBefore(Double)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_ValueBefore()
	 * @model extendedMetaData="kind='element' name='ValueBefore' namespace='##targetNamespace'"
	 * @generated
	 */
	Double getValueBefore();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueBefore <em>Value Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Before</em>' attribute.
	 * @see #getValueBefore()
	 * @generated
	 */
	void setValueBefore(Double value);

	/**
	 * Returns the value of the '<em><b>Value After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value After</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value After</em>' attribute.
	 * @see #setValueAfter(Double)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_ValueAfter()
	 * @model extendedMetaData="kind='element' name='ValueAfter' namespace='##targetNamespace'"
	 * @generated
	 */
	Double getValueAfter();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getValueAfter <em>Value After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value After</em>' attribute.
	 * @see #getValueAfter()
	 * @generated
	 */
	void setValueAfter(Double value);

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
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_FileBefore()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='FileBefore' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFileBefore();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileBefore <em>File Before</em>}' attribute.
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
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_FileAfter()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='FileAfter' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFileAfter();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileAfter <em>File After</em>}' attribute.
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
	 * @see #setProfileType(ProfileTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_ProfileType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ProfileType' namespace='##targetNamespace'"
	 * @generated
	 */
	ProfileTypeType getProfileType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getProfileType <em>Profile Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Profile Type</em>' containment reference.
	 * @see #getProfileType()
	 * @generated
	 */
	void setProfileType(ProfileTypeType value);

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
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getDarkFieldType_FileProfile()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='FileProfile' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFileProfile();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType#getFileProfile <em>File Profile</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Profile</em>' attribute.
	 * @see #getFileProfile()
	 * @generated
	 */
	void setFileProfile(String value);

} // DarkFieldType
