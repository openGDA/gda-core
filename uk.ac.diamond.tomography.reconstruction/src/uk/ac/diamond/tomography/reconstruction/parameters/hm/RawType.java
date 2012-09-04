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
 * A representation of the model object '<em><b>Raw Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getBits <em>Bits</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getOffset <em>Offset</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getByteOrder <em>Byte Order</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getXlen <em>Xlen</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getYlen <em>Ylen</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getZlen <em>Zlen</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getGap <em>Gap</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType()
 * @model extendedMetaData="name='Raw_._type' kind='elementOnly'"
 * @generated
 */
public interface RawType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType16)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType16 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType16 value);

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
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Bits()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Bits' namespace='##targetNamespace'"
	 * @generated
	 */
	int getBits();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getBits <em>Bits</em>}' attribute.
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
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getBits <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetBits()
	 * @see #getBits()
	 * @see #setBits(int)
	 * @generated
	 */
	void unsetBits();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getBits <em>Bits</em>}' attribute is set.
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
	 * Returns the value of the '<em><b>Offset</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Offset</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Offset</em>' containment reference.
	 * @see #setOffset(OffsetType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Offset()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Offset' namespace='##targetNamespace'"
	 * @generated
	 */
	OffsetType getOffset();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getOffset <em>Offset</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Offset</em>' containment reference.
	 * @see #getOffset()
	 * @generated
	 */
	void setOffset(OffsetType value);

	/**
	 * Returns the value of the '<em><b>Byte Order</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Byte Order</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Byte Order</em>' containment reference.
	 * @see #setByteOrder(ByteOrderType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_ByteOrder()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ByteOrder' namespace='##targetNamespace'"
	 * @generated
	 */
	ByteOrderType getByteOrder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getByteOrder <em>Byte Order</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Byte Order</em>' containment reference.
	 * @see #getByteOrder()
	 * @generated
	 */
	void setByteOrder(ByteOrderType value);

	/**
	 * Returns the value of the '<em><b>Xlen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xlen</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xlen</em>' attribute.
	 * @see #isSetXlen()
	 * @see #unsetXlen()
	 * @see #setXlen(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Xlen()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Xlen' namespace='##targetNamespace'"
	 * @generated
	 */
	int getXlen();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getXlen <em>Xlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Xlen</em>' attribute.
	 * @see #isSetXlen()
	 * @see #unsetXlen()
	 * @see #getXlen()
	 * @generated
	 */
	void setXlen(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getXlen <em>Xlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetXlen()
	 * @see #getXlen()
	 * @see #setXlen(int)
	 * @generated
	 */
	void unsetXlen();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getXlen <em>Xlen</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Xlen</em>' attribute is set.
	 * @see #unsetXlen()
	 * @see #getXlen()
	 * @see #setXlen(int)
	 * @generated
	 */
	boolean isSetXlen();

	/**
	 * Returns the value of the '<em><b>Ylen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ylen</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ylen</em>' attribute.
	 * @see #isSetYlen()
	 * @see #unsetYlen()
	 * @see #setYlen(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Ylen()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Ylen' namespace='##targetNamespace'"
	 * @generated
	 */
	int getYlen();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getYlen <em>Ylen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ylen</em>' attribute.
	 * @see #isSetYlen()
	 * @see #unsetYlen()
	 * @see #getYlen()
	 * @generated
	 */
	void setYlen(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getYlen <em>Ylen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetYlen()
	 * @see #getYlen()
	 * @see #setYlen(int)
	 * @generated
	 */
	void unsetYlen();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getYlen <em>Ylen</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Ylen</em>' attribute is set.
	 * @see #unsetYlen()
	 * @see #getYlen()
	 * @see #setYlen(int)
	 * @generated
	 */
	boolean isSetYlen();

	/**
	 * Returns the value of the '<em><b>Zlen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Zlen</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Zlen</em>' attribute.
	 * @see #isSetZlen()
	 * @see #unsetZlen()
	 * @see #setZlen(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Zlen()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='Zlen' namespace='##targetNamespace'"
	 * @generated
	 */
	int getZlen();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getZlen <em>Zlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Zlen</em>' attribute.
	 * @see #isSetZlen()
	 * @see #unsetZlen()
	 * @see #getZlen()
	 * @generated
	 */
	void setZlen(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getZlen <em>Zlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetZlen()
	 * @see #getZlen()
	 * @see #setZlen(int)
	 * @generated
	 */
	void unsetZlen();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getZlen <em>Zlen</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Zlen</em>' attribute is set.
	 * @see #unsetZlen()
	 * @see #getZlen()
	 * @see #setZlen(int)
	 * @generated
	 */
	boolean isSetZlen();

	/**
	 * Returns the value of the '<em><b>Gap</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Gap</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Gap</em>' containment reference.
	 * @see #setGap(GapType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Gap()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Gap' namespace='##targetNamespace'"
	 * @generated
	 */
	GapType getGap();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getGap <em>Gap</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Gap</em>' containment reference.
	 * @see #getGap()
	 * @generated
	 */
	void setGap(GapType value);

	/**
	 * Returns the value of the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Done</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Done</em>' attribute.
	 * @see #setDone(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getRawType_Done()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString" required="true"
	 *        extendedMetaData="kind='attribute' name='done' namespace='##targetNamespace'"
	 * @generated
	 */
	String getDone();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType#getDone <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Done</em>' attribute.
	 * @see #getDone()
	 * @generated
	 */
	void setDone(String value);

} // RawType
