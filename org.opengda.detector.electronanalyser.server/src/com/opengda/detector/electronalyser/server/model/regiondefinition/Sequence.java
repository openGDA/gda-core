/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sequence</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Sequence#getRegion <em>Region</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getSequence()
 * @model
 * @generated
 */
public interface Sequence extends EObject {
	/**
	 * Returns the value of the '<em><b>Region</b></em>' containment reference list.
	 * The list contents are of type {@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Region</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Region</em>' containment reference list.
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getSequence_Region()
	 * @model containment="true"
	 * @generated
	 */
	EList<Region> getRegion();

} // Sequence
