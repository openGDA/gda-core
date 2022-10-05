/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Document Root</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot#getSequence <em>Sequence</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getDocumentRoot()
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
	 * @see #isSetSequence()
	 * @see #unsetSequence()
	 * @see #setSequence(Sequence)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getDocumentRoot_Sequence()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	Sequence getSequence();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot#getSequence <em>Sequence</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sequence</em>' containment reference.
	 * @see #isSetSequence()
	 * @see #unsetSequence()
	 * @see #getSequence()
	 * @generated
	 */
	void setSequence(Sequence value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot#getSequence <em>Sequence</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSequence()
	 * @see #getSequence()
	 * @see #setSequence(Sequence)
	 * @generated
	 */
	void unsetSequence();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot#getSequence <em>Sequence</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sequence</em>' containment reference is set.
	 * @see #unsetSequence()
	 * @see #getSequence()
	 * @see #setSequence(Sequence)
	 * @generated
	 */
	boolean isSetSequence();

} // DocumentRoot
