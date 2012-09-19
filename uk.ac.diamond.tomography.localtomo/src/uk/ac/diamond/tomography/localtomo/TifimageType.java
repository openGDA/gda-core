/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tifimage Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TifimageType#getFilenameFmt <em>Filename Fmt</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTifimageType()
 * @model extendedMetaData="name='tifimage_._type' kind='elementOnly'"
 * @generated
 */
public interface TifimageType extends EObject {
	/**
	 * Returns the value of the '<em><b>Filename Fmt</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filename Fmt</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Filename Fmt</em>' containment reference.
	 * @see #setFilenameFmt(FilenameFmtType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTifimageType_FilenameFmt()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='filenameFmt' namespace='##targetNamespace'"
	 * @generated
	 */
	FilenameFmtType getFilenameFmt();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TifimageType#getFilenameFmt <em>Filename Fmt</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filename Fmt</em>' containment reference.
	 * @see #getFilenameFmt()
	 * @generated
	 */
	void setFilenameFmt(FilenameFmtType value);

} // TifimageType
