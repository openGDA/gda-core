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
 * A representation of the model object '<em><b>Shutter Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.ShutterType#getShutterOpenPhys <em>Shutter Open Phys</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.ShutterType#getShutterClosedPhys <em>Shutter Closed Phys</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getShutterType()
 * @model extendedMetaData="name='shutter_._type' kind='elementOnly'"
 * @generated
 */
public interface ShutterType extends EObject {
	/**
	 * Returns the value of the '<em><b>Shutter Open Phys</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shutter Open Phys</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shutter Open Phys</em>' containment reference.
	 * @see #setShutterOpenPhys(ShutterOpenPhysType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getShutterType_ShutterOpenPhys()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='shutterOpenPhys' namespace='##targetNamespace'"
	 * @generated
	 */
	ShutterOpenPhysType getShutterOpenPhys();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.ShutterType#getShutterOpenPhys <em>Shutter Open Phys</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shutter Open Phys</em>' containment reference.
	 * @see #getShutterOpenPhys()
	 * @generated
	 */
	void setShutterOpenPhys(ShutterOpenPhysType value);

	/**
	 * Returns the value of the '<em><b>Shutter Closed Phys</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shutter Closed Phys</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shutter Closed Phys</em>' containment reference.
	 * @see #setShutterClosedPhys(ShutterClosedPhysType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getShutterType_ShutterClosedPhys()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='shutterClosedPhys' namespace='##targetNamespace'"
	 * @generated
	 */
	ShutterClosedPhysType getShutterClosedPhys();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.ShutterType#getShutterClosedPhys <em>Shutter Closed Phys</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shutter Closed Phys</em>' containment reference.
	 * @see #getShutterClosedPhys()
	 * @generated
	 */
	void setShutterClosedPhys(ShutterClosedPhysType value);

} // ShutterType
