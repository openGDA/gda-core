/**
 */
package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Experiment Definition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getSamplelist <em>Samplelist</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperimentDefinition()
 * @model
 * @generated
 */
public interface ExperimentDefinition extends EObject {
	/**
	 * Returns the value of the '<em><b>Samplelist</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Samplelist</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Samplelist</em>' containment reference.
	 * @see #setSamplelist(SampleList)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getExperimentDefinition_Samplelist()
	 * @model containment="true"
	 * @generated
	 */
	SampleList getSamplelist();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getSamplelist <em>Samplelist</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Samplelist</em>' containment reference.
	 * @see #getSamplelist()
	 * @generated
	 */
	void setSamplelist(SampleList value);

} // ExperimentDefinition
