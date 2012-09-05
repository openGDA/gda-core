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
 * A representation of the model object '<em><b>Missed Projections Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getValue <em>Value</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getInfo <em>Info</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getMissedProjectionsType()
 * @model extendedMetaData="name='MissedProjections_._type' kind='simple'"
 * @generated
 */
public interface MissedProjectionsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getMissedProjectionsType_Value()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="name=':0' kind='simple'"
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

	/**
	 * Returns the value of the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Info</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Info</em>' attribute.
	 * @see #setInfo(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getMissedProjectionsType_Info()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
	 *        extendedMetaData="kind='attribute' name='info' namespace='##targetNamespace'"
	 * @generated
	 */
	String getInfo();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType#getInfo <em>Info</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Info</em>' attribute.
	 * @see #getInfo()
	 * @generated
	 */
	void setInfo(String value);

} // MissedProjectionsType
