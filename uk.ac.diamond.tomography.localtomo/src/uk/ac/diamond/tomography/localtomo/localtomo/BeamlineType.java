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
 * A representation of the model object '<em><b>Beamline Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType#getIxx <em>Ixx</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getBeamlineType()
 * @model extendedMetaData="name='beamline_._type' kind='elementOnly'"
 * @generated
 */
public interface BeamlineType extends EObject {
	/**
	 * Returns the value of the '<em><b>Ixx</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ixx</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ixx</em>' containment reference.
	 * @see #setIxx(IxxType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getBeamlineType_Ixx()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ixx' namespace='##targetNamespace'"
	 * @generated
	 */
	IxxType getIxx();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.BeamlineType#getIxx <em>Ixx</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ixx</em>' containment reference.
	 * @see #getIxx()
	 * @generated
	 */
	void setIxx(IxxType value);

} // BeamlineType
