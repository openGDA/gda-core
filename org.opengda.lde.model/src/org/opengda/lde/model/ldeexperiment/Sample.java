/**
 */
package org.opengda.lde.model.ldeexperiment;

import java.util.Date;
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
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getMailCount <em>Mail Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFileCount <em>Data File Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant <em>Calibrant</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_x <em>Calibrant x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_y <em>Calibrant y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_exposure <em>Calibrant exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getX_start <em>Xstart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getX_stop <em>Xstop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getX_step <em>Xstep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getY_stop <em>Ystop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getY_step <em>Ystep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDriverID <em>Driver ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_x <em>Pixium x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_y <em>Pixium y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_z <em>Pixium z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getY_start <em>Ystart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getEndDate <em>End Date</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample()
 * @model
 * @generated
 */
public interface Sample extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * The default value is <code>"new_sample"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Name()
	 * @model default="new_sample"
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
	 * @see #isSetSampleID()
	 * @see #unsetSampleID()
	 * @see #setSampleID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_SampleID()
	 * @model default="" unsettable="true"
	 * @generated
	 */
	String getSampleID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ID</em>' attribute.
	 * @see #isSetSampleID()
	 * @see #unsetSampleID()
	 * @see #getSampleID()
	 * @generated
	 */
	void setSampleID(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleID()
	 * @see #getSampleID()
	 * @see #setSampleID(String)
	 * @generated
	 */
	void unsetSampleID();

	/**
	 * Returns whether the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample ID</em>' attribute is set.
	 * @see #unsetSampleID()
	 * @see #getSampleID()
	 * @see #setSampleID(String)
	 * @generated
	 */
	boolean isSetSampleID();

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
	 * @see #isSetStatus()
	 * @see #unsetStatus()
	 * @see #setStatus(STATUS)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Status()
	 * @model default="READY" unsettable="true" transient="true"
	 * @generated
	 */
	STATUS getStatus();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see #isSetStatus()
	 * @see #unsetStatus()
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(STATUS value);

	/**
	 * Unsets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetStatus()
	 * @see #getStatus()
	 * @see #setStatus(STATUS)
	 * @generated
	 */
	void unsetStatus();

	/**
	 * Returns whether the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Status</em>' attribute is set.
	 * @see #unsetStatus()
	 * @see #getStatus()
	 * @see #setStatus(STATUS)
	 * @generated
	 */
	boolean isSetStatus();

	/**
	 * Returns the value of the '<em><b>Active</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Active</em>' attribute.
	 * @see #setActive(boolean)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Active()
	 * @model default="true"
	 * @generated
	 */
	boolean isActive();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active</em>' attribute.
	 * @see #isActive()
	 * @generated
	 */
	void setActive(boolean value);

	/**
	 * Returns the value of the '<em><b>Cell ID</b></em>' attribute.
	 * The default value is <code>"ms1-1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cell ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cell ID</em>' attribute.
	 * @see #setCellID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_CellID()
	 * @model default="ms1-1"
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
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_VisitID()
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
	 * The default value is <code>"chiu.tang@diamond.ac.uk"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Email</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email</em>' attribute.
	 * @see #setEmail(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Email()
	 * @model default="chiu.tang@diamond.ac.uk" unique="false"
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
	 * The default value is <code>"scan x 1 10 1 pixium 1.5"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command</em>' attribute.
	 * @see #setCommand(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Command()
	 * @model default="scan x 1 10 1 pixium 1.5"
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
	 * The default value is <code>"Please add your comment here"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Comment()
	 * @model default="Please add your comment here"
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
	 * Returns the value of the '<em><b>Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Start Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Date</em>' attribute.
	 * @see #setStartDate(Date)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_StartDate()
	 * @model
	 * @generated
	 */
	Date getStartDate();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getStartDate <em>Start Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Date</em>' attribute.
	 * @see #getStartDate()
	 * @generated
	 */
	void setStartDate(Date value);

	/**
	 * Returns the value of the '<em><b>End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End Date</em>' attribute.
	 * @see #setEndDate(Date)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_EndDate()
	 * @model
	 * @generated
	 */
	Date getEndDate();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getEndDate <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End Date</em>' attribute.
	 * @see #getEndDate()
	 * @generated
	 */
	void setEndDate(Date value);

	/**
	 * Returns the value of the '<em><b>Mail Count</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mail Count</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mail Count</em>' attribute.
	 * @see #setMailCount(int)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_MailCount()
	 * @model default="0"
	 * @generated
	 */
	int getMailCount();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getMailCount <em>Mail Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mail Count</em>' attribute.
	 * @see #getMailCount()
	 * @generated
	 */
	void setMailCount(int value);

	/**
	 * Returns the value of the '<em><b>Data File Count</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data File Count</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data File Count</em>' attribute.
	 * @see #setDataFileCount(int)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_DataFileCount()
	 * @model default="0"
	 * @generated
	 */
	int getDataFileCount();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFileCount <em>Data File Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data File Count</em>' attribute.
	 * @see #getDataFileCount()
	 * @generated
	 */
	void setDataFileCount(int value);

	/**
	 * Returns the value of the '<em><b>Data File Path</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data File Path</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data File Path</em>' attribute.
	 * @see #setDataFilePath(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_DataFilePath()
	 * @model default=""
	 * @generated
	 */
	String getDataFilePath();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data File Path</em>' attribute.
	 * @see #getDataFilePath()
	 * @generated
	 */
	void setDataFilePath(String value);

	/**
	 * Returns the value of the '<em><b>Calibrant</b></em>' attribute.
	 * The default value is <code>"Si"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant</em>' attribute.
	 * @see #setCalibrant(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Calibrant()
	 * @model default="Si"
	 * @generated
	 */
	String getCalibrant();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant <em>Calibrant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant</em>' attribute.
	 * @see #getCalibrant()
	 * @generated
	 */
	void setCalibrant(String value);

	/**
	 * Returns the value of the '<em><b>Calibrant x</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant x</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant x</em>' attribute.
	 * @see #setCalibrant_x(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Calibrant_x()
	 * @model default="0"
	 * @generated
	 */
	double getCalibrant_x();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_x <em>Calibrant x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant x</em>' attribute.
	 * @see #getCalibrant_x()
	 * @generated
	 */
	void setCalibrant_x(double value);

	/**
	 * Returns the value of the '<em><b>Calibrant y</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant y</em>' attribute.
	 * @see #setCalibrant_y(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Calibrant_y()
	 * @model default="0"
	 * @generated
	 */
	double getCalibrant_y();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_y <em>Calibrant y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant y</em>' attribute.
	 * @see #getCalibrant_y()
	 * @generated
	 */
	void setCalibrant_y(double value);

	/**
	 * Returns the value of the '<em><b>Calibrant exposure</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant exposure</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant exposure</em>' attribute.
	 * @see #setCalibrant_exposure(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Calibrant_exposure()
	 * @model default="1.0"
	 * @generated
	 */
	double getCalibrant_exposure();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_exposure <em>Calibrant exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Calibrant exposure</em>' attribute.
	 * @see #getCalibrant_exposure()
	 * @generated
	 */
	void setCalibrant_exposure(double value);

	/**
	 * Returns the value of the '<em><b>Xstart</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xstart</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xstart</em>' attribute.
	 * @see #setX_start(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_X_start()
	 * @model default="0"
	 * @generated
	 */
	double getX_start();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getX_start <em>Xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Xstart</em>' attribute.
	 * @see #getX_start()
	 * @generated
	 */
	void setX_start(double value);

	/**
	 * Returns the value of the '<em><b>Xstop</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xstop</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xstop</em>' attribute.
	 * @see #setX_stop(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_X_stop()
	 * @model default="1.0"
	 * @generated
	 */
	double getX_stop();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getX_stop <em>Xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Xstop</em>' attribute.
	 * @see #getX_stop()
	 * @generated
	 */
	void setX_stop(double value);

	/**
	 * Returns the value of the '<em><b>Xstep</b></em>' attribute.
	 * The default value is <code>"0.1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xstep</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xstep</em>' attribute.
	 * @see #setX_step(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_X_step()
	 * @model default="0.1"
	 * @generated
	 */
	double getX_step();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getX_step <em>Xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Xstep</em>' attribute.
	 * @see #getX_step()
	 * @generated
	 */
	void setX_step(double value);

	/**
	 * Returns the value of the '<em><b>Ystop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ystop</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ystop</em>' attribute.
	 * @see #setY_stop(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Y_stop()
	 * @model
	 * @generated
	 */
	Double getY_stop();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getY_stop <em>Ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ystop</em>' attribute.
	 * @see #getY_stop()
	 * @generated
	 */
	void setY_stop(Double value);

	/**
	 * Returns the value of the '<em><b>Ystep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ystep</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ystep</em>' attribute.
	 * @see #setY_step(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Y_step()
	 * @model
	 * @generated
	 */
	Double getY_step();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getY_step <em>Ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ystep</em>' attribute.
	 * @see #getY_step()
	 * @generated
	 */
	void setY_step(Double value);

	/**
	 * Returns the value of the '<em><b>Sample exposure</b></em>' attribute.
	 * The default value is <code>"5.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample exposure</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample exposure</em>' attribute.
	 * @see #setSample_exposure(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Sample_exposure()
	 * @model default="5.0"
	 * @generated
	 */
	double getSample_exposure();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample exposure</em>' attribute.
	 * @see #getSample_exposure()
	 * @generated
	 */
	void setSample_exposure(double value);

	/**
	 * Returns the value of the '<em><b>Driver ID</b></em>' attribute.
	 * The default value is <code>"i11-1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Driver ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Driver ID</em>' attribute.
	 * @see #setDriverID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_DriverID()
	 * @model default="i11-1"
	 * @generated
	 */
	String getDriverID();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDriverID <em>Driver ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Driver ID</em>' attribute.
	 * @see #getDriverID()
	 * @generated
	 */
	void setDriverID(String value);

	/**
	 * Returns the value of the '<em><b>Pixium x</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pixium x</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pixium x</em>' attribute.
	 * @see #setPixium_x(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Pixium_x()
	 * @model default="0.0"
	 * @generated
	 */
	double getPixium_x();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_x <em>Pixium x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pixium x</em>' attribute.
	 * @see #getPixium_x()
	 * @generated
	 */
	void setPixium_x(double value);

	/**
	 * Returns the value of the '<em><b>Pixium y</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pixium y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pixium y</em>' attribute.
	 * @see #setPixium_y(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Pixium_y()
	 * @model default="0.0"
	 * @generated
	 */
	double getPixium_y();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_y <em>Pixium y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pixium y</em>' attribute.
	 * @see #getPixium_y()
	 * @generated
	 */
	void setPixium_y(double value);

	/**
	 * Returns the value of the '<em><b>Pixium z</b></em>' attribute.
	 * The default value is <code>"100.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pixium z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pixium z</em>' attribute.
	 * @see #setPixium_z(double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Pixium_z()
	 * @model default="100.0"
	 * @generated
	 */
	double getPixium_z();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_z <em>Pixium z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pixium z</em>' attribute.
	 * @see #getPixium_z()
	 * @generated
	 */
	void setPixium_z(double value);

	/**
	 * Returns the value of the '<em><b>Ystart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ystart</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ystart</em>' attribute.
	 * @see #setY_start(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentPackage#getSample_Y_start()
	 * @model
	 * @generated
	 */
	Double getY_start();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getY_start <em>Ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ystart</em>' attribute.
	 * @see #getY_start()
	 * @generated
	 */
	void setY_start(Double value);

} // Sample
