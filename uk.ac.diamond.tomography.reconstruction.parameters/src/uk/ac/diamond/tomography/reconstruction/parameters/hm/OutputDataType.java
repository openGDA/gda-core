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
 * A representation of the model object '<em><b>Output Data Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getState <em>State</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFolder <em>Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getSuffix <em>Suffix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getExtension <em>Extension</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getNOD <em>NOD</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileFirst <em>File First</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileStep <em>File Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBitsType <em>Bits Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBits <em>Bits</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getRestrictions <em>Restrictions</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMin <em>Value Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMax <em>Value Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getShape <em>Shape</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType()
 * @model extendedMetaData="name='OutputData_._type' kind='elementOnly'"
 * @generated
 */
public interface OutputDataType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType2)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType2 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType2 value);

	/**
	 * Returns the value of the '<em><b>State</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>State</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State</em>' containment reference.
	 * @see #setState(StateType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_State()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='State' namespace='##targetNamespace'"
	 * @generated
	 */
	StateType getState();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getState <em>State</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State</em>' containment reference.
	 * @see #getState()
	 * @generated
	 */
	void setState(StateType value);

	/**
	 * Returns the value of the '<em><b>Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Folder</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Folder</em>' attribute.
	 * @see #setFolder(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Folder()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='Folder' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFolder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFolder <em>Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Folder</em>' attribute.
	 * @see #getFolder()
	 * @generated
	 */
	void setFolder(String value);

	/**
	 * Returns the value of the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Prefix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Prefix</em>' attribute.
	 * @see #setPrefix(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Prefix()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='Prefix' namespace='##targetNamespace'"
	 * @generated
	 */
	String getPrefix();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getPrefix <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prefix</em>' attribute.
	 * @see #getPrefix()
	 * @generated
	 */
	void setPrefix(String value);

	/**
	 * Returns the value of the '<em><b>Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Suffix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Suffix</em>' attribute.
	 * @see #setSuffix(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Suffix()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='Suffix' namespace='##targetNamespace'"
	 * @generated
	 */
	String getSuffix();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getSuffix <em>Suffix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Suffix</em>' attribute.
	 * @see #getSuffix()
	 * @generated
	 */
	void setSuffix(String value);

	/**
	 * Returns the value of the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extension</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extension</em>' attribute.
	 * @see #setExtension(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Extension()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='Extension' namespace='##targetNamespace'"
	 * @generated
	 */
	String getExtension();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getExtension <em>Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extension</em>' attribute.
	 * @see #getExtension()
	 * @generated
	 */
	void setExtension(String value);

	/**
	 * Returns the value of the '<em><b>NOD</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>NOD</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>NOD</em>' attribute.
	 * @see #isSetNOD()
	 * @see #unsetNOD()
	 * @see #setNOD(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_NOD()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='NOD' namespace='##targetNamespace'"
	 * @generated
	 */
	int getNOD();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getNOD <em>NOD</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>NOD</em>' attribute.
	 * @see #isSetNOD()
	 * @see #unsetNOD()
	 * @see #getNOD()
	 * @generated
	 */
	void setNOD(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getNOD <em>NOD</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNOD()
	 * @see #getNOD()
	 * @see #setNOD(int)
	 * @generated
	 */
	void unsetNOD();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getNOD <em>NOD</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>NOD</em>' attribute is set.
	 * @see #unsetNOD()
	 * @see #getNOD()
	 * @see #setNOD(int)
	 * @generated
	 */
	boolean isSetNOD();

	/**
	 * Returns the value of the '<em><b>File First</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File First</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File First</em>' attribute.
	 * @see #isSetFileFirst()
	 * @see #unsetFileFirst()
	 * @see #setFileFirst(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_FileFirst()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='FileFirst' namespace='##targetNamespace'"
	 * @generated
	 */
	int getFileFirst();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileFirst <em>File First</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File First</em>' attribute.
	 * @see #isSetFileFirst()
	 * @see #unsetFileFirst()
	 * @see #getFileFirst()
	 * @generated
	 */
	void setFileFirst(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileFirst <em>File First</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFileFirst()
	 * @see #getFileFirst()
	 * @see #setFileFirst(int)
	 * @generated
	 */
	void unsetFileFirst();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileFirst <em>File First</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>File First</em>' attribute is set.
	 * @see #unsetFileFirst()
	 * @see #getFileFirst()
	 * @see #setFileFirst(int)
	 * @generated
	 */
	boolean isSetFileFirst();

	/**
	 * Returns the value of the '<em><b>File Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Step</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Step</em>' attribute.
	 * @see #isSetFileStep()
	 * @see #unsetFileStep()
	 * @see #setFileStep(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_FileStep()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='FileStep' namespace='##targetNamespace'"
	 * @generated
	 */
	int getFileStep();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileStep <em>File Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Step</em>' attribute.
	 * @see #isSetFileStep()
	 * @see #unsetFileStep()
	 * @see #getFileStep()
	 * @generated
	 */
	void setFileStep(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileStep <em>File Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFileStep()
	 * @see #getFileStep()
	 * @see #setFileStep(int)
	 * @generated
	 */
	void unsetFileStep();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getFileStep <em>File Step</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>File Step</em>' attribute is set.
	 * @see #unsetFileStep()
	 * @see #getFileStep()
	 * @see #setFileStep(int)
	 * @generated
	 */
	boolean isSetFileStep();

	/**
	 * Returns the value of the '<em><b>Bits Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bits Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bits Type</em>' containment reference.
	 * @see #setBitsType(BitsTypeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_BitsType()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='BitsType' namespace='##targetNamespace'"
	 * @generated
	 */
	BitsTypeType getBitsType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBitsType <em>Bits Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bits Type</em>' containment reference.
	 * @see #getBitsType()
	 * @generated
	 */
	void setBitsType(BitsTypeType value);

	/**
	 * Returns the value of the '<em><b>Bits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bits</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bits</em>' attribute.
	 * @see #isSetBits()
	 * @see #unsetBits()
	 * @see #setBits(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Bits()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Bits' namespace='##targetNamespace'"
	 * @generated
	 */
	int getBits();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBits <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bits</em>' attribute.
	 * @see #isSetBits()
	 * @see #unsetBits()
	 * @see #getBits()
	 * @generated
	 */
	void setBits(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBits <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetBits()
	 * @see #getBits()
	 * @see #setBits(int)
	 * @generated
	 */
	void unsetBits();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getBits <em>Bits</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Bits</em>' attribute is set.
	 * @see #unsetBits()
	 * @see #getBits()
	 * @see #setBits(int)
	 * @generated
	 */
	boolean isSetBits();

	/**
	 * Returns the value of the '<em><b>Restrictions</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Restrictions</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Restrictions</em>' containment reference.
	 * @see #setRestrictions(RestrictionsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Restrictions()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Restrictions' namespace='##targetNamespace'"
	 * @generated
	 */
	RestrictionsType getRestrictions();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getRestrictions <em>Restrictions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Restrictions</em>' containment reference.
	 * @see #getRestrictions()
	 * @generated
	 */
	void setRestrictions(RestrictionsType value);

	/**
	 * Returns the value of the '<em><b>Value Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Min</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Min</em>' attribute.
	 * @see #setValueMin(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_ValueMin()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ValueMin' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getValueMin();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMin <em>Value Min</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Min</em>' attribute.
	 * @see #getValueMin()
	 * @generated
	 */
	void setValueMin(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Value Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Max</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Max</em>' attribute.
	 * @see #setValueMax(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_ValueMax()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ValueMax' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getValueMax();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getValueMax <em>Value Max</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Max</em>' attribute.
	 * @see #getValueMax()
	 * @generated
	 */
	void setValueMax(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shape</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shape</em>' containment reference.
	 * @see #setShape(ShapeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getOutputDataType_Shape()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Shape' namespace='##targetNamespace'"
	 * @generated
	 */
	ShapeType getShape();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType#getShape <em>Shape</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shape</em>' containment reference.
	 * @see #getShape()
	 * @generated
	 */
	void setShape(ShapeType value);

} // OutputDataType
