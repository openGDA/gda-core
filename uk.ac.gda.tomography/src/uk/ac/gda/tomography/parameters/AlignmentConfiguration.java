/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import java.util.Date;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Alignment Configuration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId <em>Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy <em>Energy</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDescription <em>Description</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties <em>Detector Properties</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleStageParameters <em>Sample Stage Parameters</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode <em>Scan Mode</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime <em>Sample Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedUserId <em>Created User Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedDateTime <em>Created Date Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleWeight <em>Sample Weight</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorStageParameters <em>Detector Stage Parameters</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getProposalId <em>Proposal Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getStitchParameters <em>Stitch Parameters</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSelectedToRun <em>Selected To Run</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration()
 * @model
 * @generated
 */
public interface AlignmentConfiguration extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #isSetId()
	 * @see #unsetId()
	 * @see #setId(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_Id()
	 * @model unsettable="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #isSetId()
	 * @see #unsetId()
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetId()
	 * @see #getId()
	 * @see #setId(String)
	 * @generated
	 */
	void unsetId();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId <em>Id</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Id</em>' attribute is set.
	 * @see #unsetId()
	 * @see #getId()
	 * @see #setId(String)
	 * @generated
	 */
	boolean isSetId();

	/**
	 * Returns the value of the '<em><b>Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy</em>' attribute.
	 * @see #setEnergy(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_Energy()
	 * @model required="true"
	 * @generated
	 */
	double getEnergy();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy <em>Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy</em>' attribute.
	 * @see #getEnergy()
	 * @generated
	 */
	void setEnergy(double value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_Description()
	 * @model required="true"
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Detector Properties</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector Properties</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector Properties</em>' containment reference.
	 * @see #isSetDetectorProperties()
	 * @see #unsetDetectorProperties()
	 * @see #setDetectorProperties(DetectorProperties)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_DetectorProperties()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	DetectorProperties getDetectorProperties();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties <em>Detector Properties</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector Properties</em>' containment reference.
	 * @see #isSetDetectorProperties()
	 * @see #unsetDetectorProperties()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	void setDetectorProperties(DetectorProperties value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties <em>Detector Properties</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDetectorProperties()
	 * @see #getDetectorProperties()
	 * @see #setDetectorProperties(DetectorProperties)
	 * @generated
	 */
	void unsetDetectorProperties();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties <em>Detector Properties</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Detector Properties</em>' containment reference is set.
	 * @see #unsetDetectorProperties()
	 * @see #getDetectorProperties()
	 * @see #setDetectorProperties(DetectorProperties)
	 * @generated
	 */
	boolean isSetDetectorProperties();

	/**
	 * Returns the value of the '<em><b>Sample Stage Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample Stage Parameters</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample Stage Parameters</em>' containment reference.
	 * @see #isSetSampleStageParameters()
	 * @see #unsetSampleStageParameters()
	 * @see #setSampleStageParameters(SampleStage)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_SampleStageParameters()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	SampleStage getSampleStageParameters();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleStageParameters <em>Sample Stage Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample Stage Parameters</em>' containment reference.
	 * @see #isSetSampleStageParameters()
	 * @see #unsetSampleStageParameters()
	 * @see #getSampleStageParameters()
	 * @generated
	 */
	void setSampleStageParameters(SampleStage value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleStageParameters <em>Sample Stage Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleStageParameters()
	 * @see #getSampleStageParameters()
	 * @see #setSampleStageParameters(SampleStage)
	 * @generated
	 */
	void unsetSampleStageParameters();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleStageParameters <em>Sample Stage Parameters</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample Stage Parameters</em>' containment reference is set.
	 * @see #unsetSampleStageParameters()
	 * @see #getSampleStageParameters()
	 * @see #setSampleStageParameters(SampleStage)
	 * @generated
	 */
	boolean isSetSampleStageParameters();

	/**
	 * Returns the value of the '<em><b>Scan Mode</b></em>' attribute.
	 * The default value is <code>"Step"</code>.
	 * The literals are from the enumeration {@link uk.ac.gda.tomography.parameters.ScanMode}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scan Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scan Mode</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.ScanMode
	 * @see #isSetScanMode()
	 * @see #unsetScanMode()
	 * @see #setScanMode(ScanMode)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_ScanMode()
	 * @model default="Step" unsettable="true" required="true"
	 * @generated
	 */
	ScanMode getScanMode();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode <em>Scan Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scan Mode</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.ScanMode
	 * @see #isSetScanMode()
	 * @see #unsetScanMode()
	 * @see #getScanMode()
	 * @generated
	 */
	void setScanMode(ScanMode value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode <em>Scan Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetScanMode()
	 * @see #getScanMode()
	 * @see #setScanMode(ScanMode)
	 * @generated
	 */
	void unsetScanMode();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode <em>Scan Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Scan Mode</em>' attribute is set.
	 * @see #unsetScanMode()
	 * @see #getScanMode()
	 * @see #setScanMode(ScanMode)
	 * @generated
	 */
	boolean isSetScanMode();

	/**
	 * Returns the value of the '<em><b>Sample Exposure Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample Exposure Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample Exposure Time</em>' attribute.
	 * @see #isSetSampleExposureTime()
	 * @see #unsetSampleExposureTime()
	 * @see #setSampleExposureTime(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_SampleExposureTime()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getSampleExposureTime();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime <em>Sample Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample Exposure Time</em>' attribute.
	 * @see #isSetSampleExposureTime()
	 * @see #unsetSampleExposureTime()
	 * @see #getSampleExposureTime()
	 * @generated
	 */
	void setSampleExposureTime(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime <em>Sample Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleExposureTime()
	 * @see #getSampleExposureTime()
	 * @see #setSampleExposureTime(double)
	 * @generated
	 */
	void unsetSampleExposureTime();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime <em>Sample Exposure Time</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample Exposure Time</em>' attribute is set.
	 * @see #unsetSampleExposureTime()
	 * @see #getSampleExposureTime()
	 * @see #setSampleExposureTime(double)
	 * @generated
	 */
	boolean isSetSampleExposureTime();

	/**
	 * Returns the value of the '<em><b>Flat Exposure Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Flat Exposure Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Flat Exposure Time</em>' attribute.
	 * @see #isSetFlatExposureTime()
	 * @see #unsetFlatExposureTime()
	 * @see #setFlatExposureTime(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_FlatExposureTime()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getFlatExposureTime();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Flat Exposure Time</em>' attribute.
	 * @see #isSetFlatExposureTime()
	 * @see #unsetFlatExposureTime()
	 * @see #getFlatExposureTime()
	 * @generated
	 */
	void setFlatExposureTime(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFlatExposureTime()
	 * @see #getFlatExposureTime()
	 * @see #setFlatExposureTime(double)
	 * @generated
	 */
	void unsetFlatExposureTime();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Flat Exposure Time</em>' attribute is set.
	 * @see #unsetFlatExposureTime()
	 * @see #getFlatExposureTime()
	 * @see #setFlatExposureTime(double)
	 * @generated
	 */
	boolean isSetFlatExposureTime();

	/**
	 * Returns the value of the '<em><b>Created User Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Created User Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created User Id</em>' attribute.
	 * @see #setCreatedUserId(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_CreatedUserId()
	 * @model unique="false"
	 * @generated
	 */
	String getCreatedUserId();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedUserId <em>Created User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created User Id</em>' attribute.
	 * @see #getCreatedUserId()
	 * @generated
	 */
	void setCreatedUserId(String value);

	/**
	 * Returns the value of the '<em><b>Created Date Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Created Date Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created Date Time</em>' attribute.
	 * @see #setCreatedDateTime(Date)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_CreatedDateTime()
	 * @model unique="false"
	 * @generated
	 */
	Date getCreatedDateTime();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedDateTime <em>Created Date Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created Date Time</em>' attribute.
	 * @see #getCreatedDateTime()
	 * @generated
	 */
	void setCreatedDateTime(Date value);

	/**
	 * Returns the value of the '<em><b>Sample Weight</b></em>' attribute.
	 * The literals are from the enumeration {@link uk.ac.gda.tomography.parameters.SampleWeight}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample Weight</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample Weight</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.SampleWeight
	 * @see #setSampleWeight(SampleWeight)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_SampleWeight()
	 * @model
	 * @generated
	 */
	SampleWeight getSampleWeight();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleWeight <em>Sample Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample Weight</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.SampleWeight
	 * @see #getSampleWeight()
	 * @generated
	 */
	void setSampleWeight(SampleWeight value);

	/**
	 * Returns the value of the '<em><b>Detector Stage Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector Stage Parameters</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector Stage Parameters</em>' containment reference.
	 * @see #isSetDetectorStageParameters()
	 * @see #unsetDetectorStageParameters()
	 * @see #setDetectorStageParameters(DetectorStage)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_DetectorStageParameters()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	DetectorStage getDetectorStageParameters();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorStageParameters <em>Detector Stage Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector Stage Parameters</em>' containment reference.
	 * @see #isSetDetectorStageParameters()
	 * @see #unsetDetectorStageParameters()
	 * @see #getDetectorStageParameters()
	 * @generated
	 */
	void setDetectorStageParameters(DetectorStage value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorStageParameters <em>Detector Stage Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDetectorStageParameters()
	 * @see #getDetectorStageParameters()
	 * @see #setDetectorStageParameters(DetectorStage)
	 * @generated
	 */
	void unsetDetectorStageParameters();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorStageParameters <em>Detector Stage Parameters</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Detector Stage Parameters</em>' containment reference is set.
	 * @see #unsetDetectorStageParameters()
	 * @see #getDetectorStageParameters()
	 * @see #setDetectorStageParameters(DetectorStage)
	 * @generated
	 */
	boolean isSetDetectorStageParameters();

	/**
	 * Returns the value of the '<em><b>Proposal Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Proposal Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Proposal Id</em>' attribute.
	 * @see #setProposalId(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_ProposalId()
	 * @model unique="false"
	 * @generated
	 */
	String getProposalId();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getProposalId <em>Proposal Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Proposal Id</em>' attribute.
	 * @see #getProposalId()
	 * @generated
	 */
	void setProposalId(String value);

	/**
	 * Returns the value of the '<em><b>Stitch Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stitch Parameters</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stitch Parameters</em>' containment reference.
	 * @see #isSetStitchParameters()
	 * @see #unsetStitchParameters()
	 * @see #setStitchParameters(StitchParameters)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_StitchParameters()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	StitchParameters getStitchParameters();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getStitchParameters <em>Stitch Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stitch Parameters</em>' containment reference.
	 * @see #isSetStitchParameters()
	 * @see #unsetStitchParameters()
	 * @see #getStitchParameters()
	 * @generated
	 */
	void setStitchParameters(StitchParameters value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getStitchParameters <em>Stitch Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetStitchParameters()
	 * @see #getStitchParameters()
	 * @see #setStitchParameters(StitchParameters)
	 * @generated
	 */
	void unsetStitchParameters();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getStitchParameters <em>Stitch Parameters</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Stitch Parameters</em>' containment reference is set.
	 * @see #unsetStitchParameters()
	 * @see #getStitchParameters()
	 * @see #setStitchParameters(StitchParameters)
	 * @generated
	 */
	boolean isSetStitchParameters();

	/**
	 * Returns the value of the '<em><b>Selected To Run</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Selected To Run</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Selected To Run</em>' attribute.
	 * @see #setSelectedToRun(Boolean)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_SelectedToRun()
	 * @model default="false"
	 * @generated
	 */
	Boolean getSelectedToRun();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSelectedToRun <em>Selected To Run</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Selected To Run</em>' attribute.
	 * @see #getSelectedToRun()
	 * @generated
	 */
	void setSelectedToRun(Boolean value);

} // AlignmentConfiguration
