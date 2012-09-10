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
 * A representation of the model object '<em><b>Ring Artefacts Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterN <em>Parameter N</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterR <em>Parameter R</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getNumSeries <em>Num Series</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRingArtefactsType()
 * @model extendedMetaData="name='RingArtefacts_._type' kind='elementOnly'"
 * @generated
 */
public interface RingArtefactsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType5)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRingArtefactsType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType5 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType5 value);

	/**
	 * Returns the value of the '<em><b>Parameter N</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameter N</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameter N</em>' attribute.
	 * @see #setParameterN(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRingArtefactsType_ParameterN()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ParameterN' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getParameterN();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterN <em>Parameter N</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parameter N</em>' attribute.
	 * @see #getParameterN()
	 * @generated
	 */
	void setParameterN(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Parameter R</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameter R</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameter R</em>' attribute.
	 * @see #setParameterR(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRingArtefactsType_ParameterR()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ParameterR' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getParameterR();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getParameterR <em>Parameter R</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parameter R</em>' attribute.
	 * @see #getParameterR()
	 * @generated
	 */
	void setParameterR(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Num Series</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Num Series</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Num Series</em>' containment reference.
	 * @see #setNumSeries(NumSeriesType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRingArtefactsType_NumSeries()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='NumSeries' namespace='##targetNamespace'"
	 * @generated
	 */
	NumSeriesType getNumSeries();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType#getNumSeries <em>Num Series</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Num Series</em>' containment reference.
	 * @see #getNumSeries()
	 * @generated
	 */
	void setNumSeries(NumSeriesType value);

} // RingArtefactsType
