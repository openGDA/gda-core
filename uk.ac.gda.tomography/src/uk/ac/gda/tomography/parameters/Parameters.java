/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Parameters</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.Parameters#getConfigurationSet <em>Configuration Set</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getParameters()
 * @model
 * @generated
 */
public interface Parameters extends EObject {
	/**
	 * Returns the value of the '<em><b>Configuration Set</b></em>' containment reference list.
	 * The list contents are of type {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Configuration Set</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Configuration Set</em>' containment reference list.
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getParameters_ConfigurationSet()
	 * @model containment="true"
	 * @generated
	 */
	EList<AlignmentConfiguration> getConfigurationSet();

} // Parameters
