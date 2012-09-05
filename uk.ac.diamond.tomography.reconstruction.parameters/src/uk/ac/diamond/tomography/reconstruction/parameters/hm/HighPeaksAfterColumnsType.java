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
 * A representation of the model object '<em><b>High Peaks After Columns Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getNumberPixels <em>Number Pixels</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getJump <em>Jump</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getHighPeaksAfterColumnsType()
 * @model extendedMetaData="name='HighPeaksAfterColumns_._type' kind='elementOnly'"
 * @generated
 */
public interface HighPeaksAfterColumnsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType10)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getHighPeaksAfterColumnsType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType10 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType10 value);

	/**
	 * Returns the value of the '<em><b>Number Pixels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number Pixels</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number Pixels</em>' attribute.
	 * @see #isSetNumberPixels()
	 * @see #unsetNumberPixels()
	 * @see #setNumberPixels(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getHighPeaksAfterColumnsType_NumberPixels()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='NumberPixels' namespace='##targetNamespace'"
	 * @generated
	 */
	int getNumberPixels();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getNumberPixels <em>Number Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number Pixels</em>' attribute.
	 * @see #isSetNumberPixels()
	 * @see #unsetNumberPixels()
	 * @see #getNumberPixels()
	 * @generated
	 */
	void setNumberPixels(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getNumberPixels <em>Number Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNumberPixels()
	 * @see #getNumberPixels()
	 * @see #setNumberPixels(int)
	 * @generated
	 */
	void unsetNumberPixels();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getNumberPixels <em>Number Pixels</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Number Pixels</em>' attribute is set.
	 * @see #unsetNumberPixels()
	 * @see #getNumberPixels()
	 * @see #setNumberPixels(int)
	 * @generated
	 */
	boolean isSetNumberPixels();

	/**
	 * Returns the value of the '<em><b>Jump</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Jump</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Jump</em>' attribute.
	 * @see #setJump(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getHighPeaksAfterColumnsType_Jump()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='Jump' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getJump();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType#getJump <em>Jump</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Jump</em>' attribute.
	 * @see #getJump()
	 * @generated
	 */
	void setJump(BigDecimal value);

} // HighPeaksAfterColumnsType
