/**
 */
package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sample List</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.SampleList#getFilename <em>Filename</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.SampleList#getSamples <em>Samples</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSampleList()
 * @model
 * @generated
 */
public interface SampleList extends EObject {
	/**
	 * Returns the value of the '<em><b>Filename</b></em>' attribute.
	 * The default value is <code>"samples"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filename</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Filename</em>' attribute.
	 * @see #setFilename(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSampleList_Filename()
	 * @model default="samples"
	 * @generated
	 */
	String getFilename();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.SampleList#getFilename <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filename</em>' attribute.
	 * @see #getFilename()
	 * @generated
	 */
	void setFilename(String value);

	/**
	 * Returns the value of the '<em><b>Samples</b></em>' containment reference list.
	 * The list contents are of type {@link org.opengda.lde.model.ldeexperiment.Sample}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Samples</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Samples</em>' containment reference list.
	 * @see #isSetSamples()
	 * @see #unsetSamples()
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSampleList_Samples()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	EList<Sample> getSamples();

	/**
	 * Unsets the value of the '{@link org.opengda.lde.model.ldeexperiment.SampleList#getSamples <em>Samples</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSamples()
	 * @see #getSamples()
	 * @generated
	 */
	void unsetSamples();

	/**
	 * Returns whether the value of the '{@link org.opengda.lde.model.ldeexperiment.SampleList#getSamples <em>Samples</em>}' containment reference list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Samples</em>' containment reference list is set.
	 * @see #unsetSamples()
	 * @see #getSamples()
	 * @generated
	 */
	boolean isSetSamples();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Sample getSampleById(String regionID);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Sample getSampleByName(String sampleName);

} // SampleList
