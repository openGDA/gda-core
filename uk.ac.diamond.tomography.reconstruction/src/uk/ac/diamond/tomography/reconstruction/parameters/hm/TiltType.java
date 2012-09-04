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
 * A representation of the model object '<em><b>Tilt Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getXTilt <em>XTilt</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getZTilt <em>ZTilt</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTiltType()
 * @model extendedMetaData="name='Tilt_._type' kind='elementOnly'"
 * @generated
 */
public interface TiltType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType8)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTiltType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType8 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType8 value);

	/**
	 * Returns the value of the '<em><b>XTilt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>XTilt</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>XTilt</em>' attribute.
	 * @see #setXTilt(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTiltType_XTilt()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='X-tilt' namespace='##targetNamespace'"
	 * @generated
	 */
	String getXTilt();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getXTilt <em>XTilt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>XTilt</em>' attribute.
	 * @see #getXTilt()
	 * @generated
	 */
	void setXTilt(String value);

	/**
	 * Returns the value of the '<em><b>ZTilt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>ZTilt</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>ZTilt</em>' attribute.
	 * @see #setZTilt(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTiltType_ZTilt()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='Z-tilt' namespace='##targetNamespace'"
	 * @generated
	 */
	String getZTilt();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getZTilt <em>ZTilt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>ZTilt</em>' attribute.
	 * @see #getZTilt()
	 * @generated
	 */
	void setZTilt(String value);

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
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getTiltType_Done()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString" required="true"
	 *        extendedMetaData="kind='attribute' name='done' namespace='##targetNamespace'"
	 * @generated
	 */
	String getDone();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType#getDone <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Done</em>' attribute.
	 * @see #getDone()
	 * @generated
	 */
	void setDone(String value);

} // TiltType
