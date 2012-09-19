/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Cluster Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.ClusterType#getQsub <em>Qsub</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getClusterType()
 * @model extendedMetaData="name='cluster_._type' kind='elementOnly'"
 * @generated
 */
public interface ClusterType extends EObject {
	/**
	 * Returns the value of the '<em><b>Qsub</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Qsub</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Qsub</em>' containment reference.
	 * @see #setQsub(QsubType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getClusterType_Qsub()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='qsub' namespace='##targetNamespace'"
	 * @generated
	 */
	QsubType getQsub();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.ClusterType#getQsub <em>Qsub</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Qsub</em>' containment reference.
	 * @see #getQsub()
	 * @generated
	 */
	void setQsub(QsubType value);

} // ClusterType
