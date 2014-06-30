/**
 */
package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sample</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample()
 * @model
 * @generated
 */
public interface Sample extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Sample ID</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ID</em>' attribute.
	 * @see #setSampleID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_SampleID()
	 * @model default=""
	 * @generated
	 */
	String getSampleID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ID</em>' attribute.
	 * @see #getSampleID()
	 * @generated
	 */
	void setSampleID(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The default value is <code>"READY"</code>.
	 * The literals are from the enumeration {@link org.opengda.lde.model.ldeexperiment.STATUS}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Status</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see #setStatus(STATUS)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Status()
	 * @model default="READY" transient="true"
	 * @generated
	 */
	STATUS getStatus();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(STATUS value);

	/**
	 * Returns the value of the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Enabled</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Enabled</em>' attribute.
	 * @see #setEnabled(boolean)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Enabled()
	 * @model
	 * @generated
	 */
	boolean isEnabled();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#isEnabled <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enabled</em>' attribute.
	 * @see #isEnabled()
	 * @generated
	 */
	void setEnabled(boolean value);

	/**
	 * Returns the value of the '<em><b>Cell ID</b></em>' attribute.
	 * The default value is <code>"i11-0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cell ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cell ID</em>' attribute.
	 * @see #setCellID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_CellID()
	 * @model default="i11-0"
	 * @generated
	 */
	String getCellID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCellID <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cell ID</em>' attribute.
	 * @see #getCellID()
	 * @generated
	 */
	void setCellID(String value);

	/**
	 * Returns the value of the '<em><b>Visit ID</b></em>' attribute.
	 * The default value is <code>"0-0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visit ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visit ID</em>' attribute.
	 * @see #setVisitID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_VisitID()
	 * @model default="0-0"
	 * @generated
	 */
	String getVisitID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getVisitID <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visit ID</em>' attribute.
	 * @see #getVisitID()
	 * @generated
	 */
	void setVisitID(String value);

	/**
	 * Returns the value of the '<em><b>Email</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Email</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email</em>' attribute.
	 * @see #setEmail(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Email()
	 * @model default="" unique="false"
	 * @generated
	 */
	String getEmail();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getEmail <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Email</em>' attribute.
	 * @see #getEmail()
	 * @generated
	 */
	void setEmail(String value);

	/**
	 * Returns the value of the '<em><b>Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command</em>' attribute.
	 * @see #setCommand(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Command()
	 * @model
	 * @generated
	 */
	String getCommand();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' attribute.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(String value);

	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	String buildDataFilePath(String filePath);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	String buildFilename(String name);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	STATUS executeCommand(String command);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean validateFilePath(String filrPath);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean validateCommand(String command);

} // Sample
