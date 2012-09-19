/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Imagekeyencoding Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getDarkfield <em>Darkfield</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getFlatfield <em>Flatfield</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getProjection <em>Projection</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getImagekeyencodingType()
 * @model extendedMetaData="name='imagekeyencoding_._type' kind='elementOnly'"
 * @generated
 */
public interface ImagekeyencodingType extends EObject {
	/**
	 * Returns the value of the '<em><b>Darkfield</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Darkfield</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Darkfield</em>' attribute.
	 * @see #isSetDarkfield()
	 * @see #unsetDarkfield()
	 * @see #setDarkfield(int)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getImagekeyencodingType_Darkfield()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='darkfield' namespace='##targetNamespace'"
	 * @generated
	 */
	int getDarkfield();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getDarkfield <em>Darkfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Darkfield</em>' attribute.
	 * @see #isSetDarkfield()
	 * @see #unsetDarkfield()
	 * @see #getDarkfield()
	 * @generated
	 */
	void setDarkfield(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getDarkfield <em>Darkfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDarkfield()
	 * @see #getDarkfield()
	 * @see #setDarkfield(int)
	 * @generated
	 */
	void unsetDarkfield();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getDarkfield <em>Darkfield</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Darkfield</em>' attribute is set.
	 * @see #unsetDarkfield()
	 * @see #getDarkfield()
	 * @see #setDarkfield(int)
	 * @generated
	 */
	boolean isSetDarkfield();

	/**
	 * Returns the value of the '<em><b>Flatfield</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Flatfield</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Flatfield</em>' attribute.
	 * @see #isSetFlatfield()
	 * @see #unsetFlatfield()
	 * @see #setFlatfield(int)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getImagekeyencodingType_Flatfield()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='flatfield' namespace='##targetNamespace'"
	 * @generated
	 */
	int getFlatfield();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getFlatfield <em>Flatfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Flatfield</em>' attribute.
	 * @see #isSetFlatfield()
	 * @see #unsetFlatfield()
	 * @see #getFlatfield()
	 * @generated
	 */
	void setFlatfield(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getFlatfield <em>Flatfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFlatfield()
	 * @see #getFlatfield()
	 * @see #setFlatfield(int)
	 * @generated
	 */
	void unsetFlatfield();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getFlatfield <em>Flatfield</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Flatfield</em>' attribute is set.
	 * @see #unsetFlatfield()
	 * @see #getFlatfield()
	 * @see #setFlatfield(int)
	 * @generated
	 */
	boolean isSetFlatfield();

	/**
	 * Returns the value of the '<em><b>Projection</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Projection</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Projection</em>' attribute.
	 * @see #isSetProjection()
	 * @see #unsetProjection()
	 * @see #setProjection(int)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getImagekeyencodingType_Projection()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='projection' namespace='##targetNamespace'"
	 * @generated
	 */
	int getProjection();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getProjection <em>Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Projection</em>' attribute.
	 * @see #isSetProjection()
	 * @see #unsetProjection()
	 * @see #getProjection()
	 * @generated
	 */
	void setProjection(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getProjection <em>Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetProjection()
	 * @see #getProjection()
	 * @see #setProjection(int)
	 * @generated
	 */
	void unsetProjection();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType#getProjection <em>Projection</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Projection</em>' attribute is set.
	 * @see #unsetProjection()
	 * @see #getProjection()
	 * @see #setProjection(int)
	 * @generated
	 */
	boolean isSetProjection();

} // ImagekeyencodingType
