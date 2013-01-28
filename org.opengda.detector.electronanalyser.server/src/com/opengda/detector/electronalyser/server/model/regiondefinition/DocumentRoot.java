/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Document Root</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot#getSequence <em>Sequence</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getDocumentRoot()
 * @model
 * @generated
 */
public interface DocumentRoot extends EObject {
	/**
	 * Returns the value of the '<em><b>Sequence</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sequence</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sequence</em>' containment reference.
	 * @see #setSequence(Sequence)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getDocumentRoot_Sequence()
	 * @model containment="true"
	 * @generated
	 */
	Sequence getSequence();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot#getSequence <em>Sequence</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sequence</em>' containment reference.
	 * @see #getSequence()
	 * @generated
	 */
	void setSequence(Sequence value);

} // DocumentRoot
