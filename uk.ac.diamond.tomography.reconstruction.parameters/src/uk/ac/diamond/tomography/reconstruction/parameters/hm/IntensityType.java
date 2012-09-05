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
 * A representation of the model object '<em><b>Intensity Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnLeft <em>Column Left</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnRight <em>Column Right</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroLeft <em>Zero Left</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroRight <em>Zero Right</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getIntensityType()
 * @model extendedMetaData="name='Intensity_._type' kind='elementOnly'"
 * @generated
 */
public interface IntensityType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType6)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getIntensityType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType6 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType6 value);

	/**
	 * Returns the value of the '<em><b>Column Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Column Left</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Column Left</em>' attribute.
	 * @see #setColumnLeft(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getIntensityType_ColumnLeft()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='ColumnLeft' namespace='##targetNamespace'"
	 * @generated
	 */
	String getColumnLeft();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnLeft <em>Column Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Column Left</em>' attribute.
	 * @see #getColumnLeft()
	 * @generated
	 */
	void setColumnLeft(String value);

	/**
	 * Returns the value of the '<em><b>Column Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Column Right</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Column Right</em>' attribute.
	 * @see #setColumnRight(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getIntensityType_ColumnRight()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='ColumnRight' namespace='##targetNamespace'"
	 * @generated
	 */
	String getColumnRight();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getColumnRight <em>Column Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Column Right</em>' attribute.
	 * @see #getColumnRight()
	 * @generated
	 */
	void setColumnRight(String value);

	/**
	 * Returns the value of the '<em><b>Zero Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Zero Left</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Zero Left</em>' attribute.
	 * @see #isSetZeroLeft()
	 * @see #unsetZeroLeft()
	 * @see #setZeroLeft(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getIntensityType_ZeroLeft()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ZeroLeft' namespace='##targetNamespace'"
	 * @generated
	 */
	int getZeroLeft();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroLeft <em>Zero Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Zero Left</em>' attribute.
	 * @see #isSetZeroLeft()
	 * @see #unsetZeroLeft()
	 * @see #getZeroLeft()
	 * @generated
	 */
	void setZeroLeft(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroLeft <em>Zero Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetZeroLeft()
	 * @see #getZeroLeft()
	 * @see #setZeroLeft(int)
	 * @generated
	 */
	void unsetZeroLeft();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroLeft <em>Zero Left</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Zero Left</em>' attribute is set.
	 * @see #unsetZeroLeft()
	 * @see #getZeroLeft()
	 * @see #setZeroLeft(int)
	 * @generated
	 */
	boolean isSetZeroLeft();

	/**
	 * Returns the value of the '<em><b>Zero Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Zero Right</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Zero Right</em>' attribute.
	 * @see #isSetZeroRight()
	 * @see #unsetZeroRight()
	 * @see #setZeroRight(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getIntensityType_ZeroRight()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ZeroRight' namespace='##targetNamespace'"
	 * @generated
	 */
	int getZeroRight();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroRight <em>Zero Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Zero Right</em>' attribute.
	 * @see #isSetZeroRight()
	 * @see #unsetZeroRight()
	 * @see #getZeroRight()
	 * @generated
	 */
	void setZeroRight(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroRight <em>Zero Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetZeroRight()
	 * @see #getZeroRight()
	 * @see #setZeroRight(int)
	 * @generated
	 */
	void unsetZeroRight();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType#getZeroRight <em>Zero Right</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Zero Right</em>' attribute is set.
	 * @see #unsetZeroRight()
	 * @see #getZeroRight()
	 * @see #setZeroRight(int)
	 * @generated
	 */
	boolean isSetZeroRight();

} // IntensityType
