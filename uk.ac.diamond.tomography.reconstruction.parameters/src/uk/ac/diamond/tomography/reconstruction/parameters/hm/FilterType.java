/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

import java.math.BigDecimal;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Filter Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getName <em>Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getBandwidth <em>Bandwidth</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getWindowName <em>Window Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getNormalisation <em>Normalisation</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getPixelSize <em>Pixel Size</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType()
 * @model extendedMetaData="name='Filter_._type' kind='elementOnly'"
 * @generated
 */
public interface FilterType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType7)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType7 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType7 value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' containment reference.
	 * @see #setName(NameType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType_Name()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Name' namespace='##targetNamespace'"
	 * @generated
	 */
	NameType getName();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getName <em>Name</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' containment reference.
	 * @see #getName()
	 * @generated
	 */
	void setName(NameType value);

	/**
	 * Returns the value of the '<em><b>Bandwidth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bandwidth</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bandwidth</em>' attribute.
	 * @see #setBandwidth(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType_Bandwidth()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='Bandwidth' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getBandwidth();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getBandwidth <em>Bandwidth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bandwidth</em>' attribute.
	 * @see #getBandwidth()
	 * @generated
	 */
	void setBandwidth(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Window Name</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Window Name</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Window Name</em>' containment reference.
	 * @see #setWindowName(WindowNameType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType_WindowName()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='WindowName' namespace='##targetNamespace'"
	 * @generated
	 */
	WindowNameType getWindowName();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getWindowName <em>Window Name</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Window Name</em>' containment reference.
	 * @see #getWindowName()
	 * @generated
	 */
	void setWindowName(WindowNameType value);

	/**
	 * Returns the value of the '<em><b>Normalisation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Normalisation</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Normalisation</em>' containment reference.
	 * @see #setNormalisation(NormalisationType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType_Normalisation()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Normalisation' namespace='##targetNamespace'"
	 * @generated
	 */
	NormalisationType getNormalisation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getNormalisation <em>Normalisation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Normalisation</em>' containment reference.
	 * @see #getNormalisation()
	 * @generated
	 */
	void setNormalisation(NormalisationType value);

	/**
	 * Returns the value of the '<em><b>Pixel Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pixel Size</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pixel Size</em>' attribute.
	 * @see #setPixelSize(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFilterType_PixelSize()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='PixelSize' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getPixelSize();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType#getPixelSize <em>Pixel Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pixel Size</em>' attribute.
	 * @see #getPixelSize()
	 * @generated
	 */
	void setPixelSize(BigDecimal value);

} // FilterType
