/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Pass Energy</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PassEnergy#getEnergyValue <em>Energy Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getPassEnergy()
 * @model
 * @generated
 */
public interface PassEnergy extends EObject {
	/**
	 * Returns the value of the '<em><b>Energy Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy Value</em>' attribute.
	 * @see #isSetEnergyValue()
	 * @see #unsetEnergyValue()
	 * @see #setEnergyValue(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getPassEnergy_EnergyValue()
	 * @model unsettable="true"
	 * @generated
	 */
	int getEnergyValue();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PassEnergy#getEnergyValue <em>Energy Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy Value</em>' attribute.
	 * @see #isSetEnergyValue()
	 * @see #unsetEnergyValue()
	 * @see #getEnergyValue()
	 * @generated
	 */
	void setEnergyValue(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PassEnergy#getEnergyValue <em>Energy Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetEnergyValue()
	 * @see #getEnergyValue()
	 * @see #setEnergyValue(int)
	 * @generated
	 */
	void unsetEnergyValue();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PassEnergy#getEnergyValue <em>Energy Value</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Energy Value</em>' attribute is set.
	 * @see #unsetEnergyValue()
	 * @see #getEnergyValue()
	 * @see #setEnergyValue(int)
	 * @generated
	 */
	boolean isSetEnergyValue();

} // PassEnergy
