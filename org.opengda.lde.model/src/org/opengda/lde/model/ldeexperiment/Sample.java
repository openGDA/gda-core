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
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant <em>Calibrant</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_x <em>Calibrant x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_y <em>Calibrant y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_exposure <em>Calibrant exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_start <em>Sample xstart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_stop <em>Sample xstop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_step <em>Sample xstep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_start <em>Sample ystart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_stop <em>Sample ystop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_step <em>Sample ystep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDetector_x <em>Detector x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDetector_y <em>Detector y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDetector_z <em>Detector z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getEndDate <em>End Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getMailCount <em>Mail Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFileCount <em>Data File Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample()
 * @model
 * @generated
 */
public interface Sample extends EObject {
	/**
	 * Returns the value of the '<em><b>Sample ID</b></em>' attribute.
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_SampleID()
	 * @model unsettable="true"
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Status()
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
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Active</em>' attribute.
	 * @see #setActive(boolean)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Active()
	 * @model default="false"
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Name()
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
	 * Returns the value of the '<em><b>Cell ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cell ID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cell ID</em>' attribute.
	 * @see #setCellID(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_CellID()
	 * @model
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
	 * Returns the value of the '<em><b>Calibrant</b></em>' attribute.
	 * The default value is <code>"Si(NIST SRM 640c)"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Calibrant</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Calibrant</em>' attribute.
	 * @see #setCalibrant(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Calibrant()
	 * @model default="Si(NIST SRM 640c)"
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Calibrant_x()
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Calibrant_y()
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Calibrant_exposure()
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
	 * Returns the value of the '<em><b>Sample xstart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample xstart</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample xstart</em>' attribute.
	 * @see #setSample_x_start(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_x_start()
	 * @model
	 * @generated
	 */
	Double getSample_x_start();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_start <em>Sample xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample xstart</em>' attribute.
	 * @see #getSample_x_start()
	 * @generated
	 */
	void setSample_x_start(Double value);

	/**
	 * Returns the value of the '<em><b>Sample xstop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample xstop</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample xstop</em>' attribute.
	 * @see #setSample_x_stop(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_x_stop()
	 * @model
	 * @generated
	 */
	Double getSample_x_stop();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_stop <em>Sample xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample xstop</em>' attribute.
	 * @see #getSample_x_stop()
	 * @generated
	 */
	void setSample_x_stop(Double value);

	/**
	 * Returns the value of the '<em><b>Sample xstep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample xstep</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample xstep</em>' attribute.
	 * @see #setSample_x_step(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_x_step()
	 * @model
	 * @generated
	 */
	Double getSample_x_step();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_x_step <em>Sample xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample xstep</em>' attribute.
	 * @see #getSample_x_step()
	 * @generated
	 */
	void setSample_x_step(Double value);

	/**
	 * Returns the value of the '<em><b>Sample ystart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ystart</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ystart</em>' attribute.
	 * @see #setSample_y_start(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_y_start()
	 * @model
	 * @generated
	 */
	Double getSample_y_start();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_start <em>Sample ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ystart</em>' attribute.
	 * @see #getSample_y_start()
	 * @generated
	 */
	void setSample_y_start(Double value);

	/**
	 * Returns the value of the '<em><b>Sample ystop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ystop</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ystop</em>' attribute.
	 * @see #setSample_y_stop(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_y_stop()
	 * @model
	 * @generated
	 */
	Double getSample_y_stop();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_stop <em>Sample ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ystop</em>' attribute.
	 * @see #getSample_y_stop()
	 * @generated
	 */
	void setSample_y_stop(Double value);

	/**
	 * Returns the value of the '<em><b>Sample ystep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample ystep</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample ystep</em>' attribute.
	 * @see #setSample_y_step(Double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_y_step()
	 * @model
	 * @generated
	 */
	Double getSample_y_step();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_y_step <em>Sample ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample ystep</em>' attribute.
	 * @see #getSample_y_step()
	 * @generated
	 */
	void setSample_y_step(Double value);

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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Sample_exposure()
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
	 * Returns the value of the '<em><b>Detector x</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector x</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector x</em>' attribute.
	 * @see #setDetector_x(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Detector_x()
	 * @model default="0.0"
	 * @generated
	 */
	double getDetector_x();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDetector_x <em>Detector x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector x</em>' attribute.
	 * @see #getDetector_x()
	 * @generated
	 */
	void setDetector_x(double value);

	/**
	 * Returns the value of the '<em><b>Detector y</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector y</em>' attribute.
	 * @see #setDetector_y(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Detector_y()
	 * @model default="0.0"
	 * @generated
	 */
	double getDetector_y();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDetector_y <em>Detector y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector y</em>' attribute.
	 * @see #getDetector_y()
	 * @generated
	 */
	void setDetector_y(double value);

	/**
	 * Returns the value of the '<em><b>Detector z</b></em>' attribute.
	 * The default value is <code>"100.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector z</em>' attribute.
	 * @see #setDetector_z(double)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Detector_z()
	 * @model default="100.0"
	 * @generated
	 */
	double getDetector_z();

	/**
	 * Sets the value of the '{@link org.opengda.lde.model.ldeexperiment.Sample#getDetector_z <em>Detector z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector z</em>' attribute.
	 * @see #getDetector_z()
	 * @generated
	 */
	void setDetector_z(double value);

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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Email()
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
	 * Returns the value of the '<em><b>Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Start Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Date</em>' attribute.
	 * @see #setStartDate(Date)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_StartDate()
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_EndDate()
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_MailCount()
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
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_DataFileCount()
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
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * The default value is <code>"comment here"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_Comment()
	 * @model default="comment here"
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
	 * Returns the value of the '<em><b>Data File Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data File Path</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data File Path</em>' attribute.
	 * @see #setDataFilePath(String)
	 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage#getSample_DataFilePath()
	 * @model
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

} // Sample
