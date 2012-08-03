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
 * A representation of the model object '<em><b>Module</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.Module#getModuleNumber <em>Module Number</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.Module#getHorizontalFieldOfView <em>Horizontal Field Of View</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getModule()
 * @model
 * @generated
 */
public interface Module extends EObject {
	/**
	 * Returns the value of the '<em><b>Module Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Module Number</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Module Number</em>' attribute.
	 * @see #isSetModuleNumber()
	 * @see #unsetModuleNumber()
	 * @see #setModuleNumber(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getModule_ModuleNumber()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getModuleNumber();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.Module#getModuleNumber <em>Module Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Module Number</em>' attribute.
	 * @see #isSetModuleNumber()
	 * @see #unsetModuleNumber()
	 * @see #getModuleNumber()
	 * @generated
	 */
	void setModuleNumber(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.Module#getModuleNumber <em>Module Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetModuleNumber()
	 * @see #getModuleNumber()
	 * @see #setModuleNumber(Integer)
	 * @generated
	 */
	void unsetModuleNumber();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.Module#getModuleNumber <em>Module Number</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Module Number</em>' attribute is set.
	 * @see #unsetModuleNumber()
	 * @see #getModuleNumber()
	 * @see #setModuleNumber(Integer)
	 * @generated
	 */
	boolean isSetModuleNumber();

	/**
	 * Returns the value of the '<em><b>Horizontal Field Of View</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Horizontal Field Of View</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Horizontal Field Of View</em>' containment reference.
	 * @see #isSetHorizontalFieldOfView()
	 * @see #unsetHorizontalFieldOfView()
	 * @see #setHorizontalFieldOfView(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getModule_HorizontalFieldOfView()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getHorizontalFieldOfView();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.Module#getHorizontalFieldOfView <em>Horizontal Field Of View</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Horizontal Field Of View</em>' containment reference.
	 * @see #isSetHorizontalFieldOfView()
	 * @see #unsetHorizontalFieldOfView()
	 * @see #getHorizontalFieldOfView()
	 * @generated
	 */
	void setHorizontalFieldOfView(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.Module#getHorizontalFieldOfView <em>Horizontal Field Of View</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetHorizontalFieldOfView()
	 * @see #getHorizontalFieldOfView()
	 * @see #setHorizontalFieldOfView(ValueUnit)
	 * @generated
	 */
	void unsetHorizontalFieldOfView();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.Module#getHorizontalFieldOfView <em>Horizontal Field Of View</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Horizontal Field Of View</em>' containment reference is set.
	 * @see #unsetHorizontalFieldOfView()
	 * @see #getHorizontalFieldOfView()
	 * @see #setHorizontalFieldOfView(ValueUnit)
	 * @generated
	 */
	boolean isSetHorizontalFieldOfView();

} // Module
