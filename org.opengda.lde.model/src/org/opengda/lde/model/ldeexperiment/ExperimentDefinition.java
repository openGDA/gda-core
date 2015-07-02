package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Experiment Definition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getExperiments <em>Experiments</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperimentDefinition()
 * @model
 * @generated
 */
public interface ExperimentDefinition extends EObject {
	/**
	 * Returns the value of the '<em><b>Experiments</b></em>' containment reference list.
	 * The list contents are of type {@link org.opengda.lde.model.ldeexperiment.Experiment}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Experiments</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Experiments</em>' containment reference list.
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperimentDefinition_Experiments()
	 * @model containment="true"
	 * @generated
	 */
	EList<Experiment> getExperiments();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Experiment getExperiment(String name);

} // ExperimentDefinition
