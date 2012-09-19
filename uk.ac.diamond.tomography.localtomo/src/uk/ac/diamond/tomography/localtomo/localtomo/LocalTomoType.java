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
 * A representation of the model object '<em><b>Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getBeamline <em>Beamline</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getTomodo <em>Tomodo</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getLocalTomoType()
 * @model extendedMetaData="name='LocalTomo_._type' kind='elementOnly'"
 * @generated
 */
public interface LocalTomoType extends EObject {
	/**
	 * Returns the value of the '<em><b>Beamline</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Beamline</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Beamline</em>' containment reference.
	 * @see #setBeamline(BeamlineType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getLocalTomoType_Beamline()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='beamline' namespace='##targetNamespace'"
	 * @generated
	 */
	BeamlineType getBeamline();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getBeamline <em>Beamline</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Beamline</em>' containment reference.
	 * @see #getBeamline()
	 * @generated
	 */
	void setBeamline(BeamlineType value);

	/**
	 * Returns the value of the '<em><b>Tomodo</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tomodo</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tomodo</em>' containment reference.
	 * @see #setTomodo(TomodoType)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getLocalTomoType_Tomodo()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='tomodo' namespace='##targetNamespace'"
	 * @generated
	 */
	TomodoType getTomodo();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoType#getTomodo <em>Tomodo</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tomodo</em>' containment reference.
	 * @see #getTomodo()
	 * @generated
	 */
	void setTomodo(TomodoType value);

} // LocalTomoType
