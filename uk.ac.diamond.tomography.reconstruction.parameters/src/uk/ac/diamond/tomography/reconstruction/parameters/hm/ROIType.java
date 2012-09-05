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
 * A representation of the model object '<em><b>ROI Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmin <em>Xmin</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmax <em>Xmax</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmin <em>Ymin</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmax <em>Ymax</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidthType <em>Output Width Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidth <em>Output Width</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getAngle <em>Angle</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType()
 * @model extendedMetaData="name='ROI_._type' kind='elementOnly'"
 * @generated
 */
public interface ROIType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType3)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType3 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType3 value);

	/**
	 * Returns the value of the '<em><b>Xmin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xmin</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xmin</em>' attribute.
	 * @see #isSetXmin()
	 * @see #unsetXmin()
	 * @see #setXmin(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_Xmin()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Xmin' namespace='##targetNamespace'"
	 * @generated
	 */
	int getXmin();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmin <em>Xmin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Xmin</em>' attribute.
	 * @see #isSetXmin()
	 * @see #unsetXmin()
	 * @see #getXmin()
	 * @generated
	 */
	void setXmin(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmin <em>Xmin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetXmin()
	 * @see #getXmin()
	 * @see #setXmin(int)
	 * @generated
	 */
	void unsetXmin();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmin <em>Xmin</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Xmin</em>' attribute is set.
	 * @see #unsetXmin()
	 * @see #getXmin()
	 * @see #setXmin(int)
	 * @generated
	 */
	boolean isSetXmin();

	/**
	 * Returns the value of the '<em><b>Xmax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xmax</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xmax</em>' attribute.
	 * @see #isSetXmax()
	 * @see #unsetXmax()
	 * @see #setXmax(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_Xmax()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Xmax' namespace='##targetNamespace'"
	 * @generated
	 */
	int getXmax();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmax <em>Xmax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Xmax</em>' attribute.
	 * @see #isSetXmax()
	 * @see #unsetXmax()
	 * @see #getXmax()
	 * @generated
	 */
	void setXmax(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmax <em>Xmax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetXmax()
	 * @see #getXmax()
	 * @see #setXmax(int)
	 * @generated
	 */
	void unsetXmax();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getXmax <em>Xmax</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Xmax</em>' attribute is set.
	 * @see #unsetXmax()
	 * @see #getXmax()
	 * @see #setXmax(int)
	 * @generated
	 */
	boolean isSetXmax();

	/**
	 * Returns the value of the '<em><b>Ymin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ymin</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ymin</em>' attribute.
	 * @see #isSetYmin()
	 * @see #unsetYmin()
	 * @see #setYmin(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_Ymin()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Ymin' namespace='##targetNamespace'"
	 * @generated
	 */
	int getYmin();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmin <em>Ymin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ymin</em>' attribute.
	 * @see #isSetYmin()
	 * @see #unsetYmin()
	 * @see #getYmin()
	 * @generated
	 */
	void setYmin(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmin <em>Ymin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetYmin()
	 * @see #getYmin()
	 * @see #setYmin(int)
	 * @generated
	 */
	void unsetYmin();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmin <em>Ymin</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Ymin</em>' attribute is set.
	 * @see #unsetYmin()
	 * @see #getYmin()
	 * @see #setYmin(int)
	 * @generated
	 */
	boolean isSetYmin();

	/**
	 * Returns the value of the '<em><b>Ymax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ymax</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ymax</em>' attribute.
	 * @see #isSetYmax()
	 * @see #unsetYmax()
	 * @see #setYmax(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_Ymax()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Ymax' namespace='##targetNamespace'"
	 * @generated
	 */
	int getYmax();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmax <em>Ymax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ymax</em>' attribute.
	 * @see #isSetYmax()
	 * @see #unsetYmax()
	 * @see #getYmax()
	 * @generated
	 */
	void setYmax(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmax <em>Ymax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetYmax()
	 * @see #getYmax()
	 * @see #setYmax(int)
	 * @generated
	 */
	void unsetYmax();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getYmax <em>Ymax</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Ymax</em>' attribute is set.
	 * @see #unsetYmax()
	 * @see #getYmax()
	 * @see #setYmax(int)
	 * @generated
	 */
	boolean isSetYmax();

	/**
	 * Returns the value of the '<em><b>Output Width Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Width Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Width Type</em>' containment reference.
	 * @see #setOutputWidthType(OutputWidthTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_OutputWidthType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='OutputWidthType' namespace='##targetNamespace'"
	 * @generated
	 */
	OutputWidthTypeType getOutputWidthType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidthType <em>Output Width Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Width Type</em>' containment reference.
	 * @see #getOutputWidthType()
	 * @generated
	 */
	void setOutputWidthType(OutputWidthTypeType value);

	/**
	 * Returns the value of the '<em><b>Output Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Width</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Width</em>' attribute.
	 * @see #isSetOutputWidth()
	 * @see #unsetOutputWidth()
	 * @see #setOutputWidth(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_OutputWidth()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='OutputWidth' namespace='##targetNamespace'"
	 * @generated
	 */
	int getOutputWidth();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidth <em>Output Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Width</em>' attribute.
	 * @see #isSetOutputWidth()
	 * @see #unsetOutputWidth()
	 * @see #getOutputWidth()
	 * @generated
	 */
	void setOutputWidth(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidth <em>Output Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetOutputWidth()
	 * @see #getOutputWidth()
	 * @see #setOutputWidth(int)
	 * @generated
	 */
	void unsetOutputWidth();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getOutputWidth <em>Output Width</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Output Width</em>' attribute is set.
	 * @see #unsetOutputWidth()
	 * @see #getOutputWidth()
	 * @see #setOutputWidth(int)
	 * @generated
	 */
	boolean isSetOutputWidth();

	/**
	 * Returns the value of the '<em><b>Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Angle</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Angle</em>' attribute.
	 * @see #setAngle(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getROIType_Angle()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='Angle' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getAngle();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType#getAngle <em>Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Angle</em>' attribute.
	 * @see #getAngle()
	 * @generated
	 */
	void setAngle(BigDecimal value);

} // ROIType
