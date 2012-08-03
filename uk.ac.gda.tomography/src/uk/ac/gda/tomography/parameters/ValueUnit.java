/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Value Unit</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.ValueUnit#getUnits <em>Units</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.ValueUnit#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getValueUnit()
 * @model
 * @generated
 */
public interface ValueUnit extends EObject {
	/**
	 * Returns the value of the '<em><b>Units</b></em>' attribute.
	 * The literals are from the enumeration {@link uk.ac.gda.tomography.parameters.Unit}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Units</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Units</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.Unit
	 * @see #setUnits(Unit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getValueUnit_Units()
	 * @model
	 * @generated
	 */
	Unit getUnits();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.ValueUnit#getUnits <em>Units</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Units</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.Unit
	 * @see #getUnits()
	 * @generated
	 */
	void setUnits(Unit value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getValueUnit_Value()
	 * @model
	 * @generated
	 */
	double getValue();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.ValueUnit#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(double value);

} // ValueUnit
