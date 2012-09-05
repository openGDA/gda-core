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
 * A representation of the model object '<em><b>HMxml Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType#getFBP <em>FBP</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getHMxmlType()
 * @model extendedMetaData="name='HMxml_._type' kind='elementOnly'"
 * @generated
 */
public interface HMxmlType extends EObject {
	/**
	 * Returns the value of the '<em><b>FBP</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>FBP</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>FBP</em>' containment reference.
	 * @see #setFBP(FBPType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getHMxmlType_FBP()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='FBP' namespace='##targetNamespace'"
	 * @generated
	 */
	FBPType getFBP();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType#getFBP <em>FBP</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>FBP</em>' containment reference.
	 * @see #getFBP()
	 * @generated
	 */
	void setFBP(FBPType value);

} // HMxmlType
