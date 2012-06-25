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
 * A representation of the model object '<em><b>Alignment Configuration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId <em>Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy <em>Energy</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getNumberOfProjections <em>Number Of Projections</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDescription <em>Description</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties <em>Detector Properties</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleDetectorDistance <em>Sample Detector Distance</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleParams <em>Sample Params</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode <em>Scan Mode</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime <em>Sample Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}</li>
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
	 * Returns the value of the '<em><b>Number Of Projections</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number Of Projections</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number Of Projections</em>' attribute.
	 * @see #isSetNumberOfProjections()
	 * @see #unsetNumberOfProjections()
	 * @see #setNumberOfProjections(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_NumberOfProjections()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getNumberOfProjections();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getNumberOfProjections <em>Number Of Projections</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number Of Projections</em>' attribute.
	 * @see #isSetNumberOfProjections()
	 * @see #unsetNumberOfProjections()
	 * @see #getNumberOfProjections()
	 * @generated
	 */
	void setNumberOfProjections(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getNumberOfProjections <em>Number Of Projections</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNumberOfProjections()
	 * @see #getNumberOfProjections()
	 * @see #setNumberOfProjections(Integer)
	 * @generated
	 */
	void unsetNumberOfProjections();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getNumberOfProjections <em>Number Of Projections</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Number Of Projections</em>' attribute is set.
	 * @see #unsetNumberOfProjections()
	 * @see #getNumberOfProjections()
	 * @see #setNumberOfProjections(Integer)
	 * @generated
	 */
	boolean isSetNumberOfProjections();

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
	 * Returns the value of the '<em><b>Sample Detector Distance</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample Detector Distance</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample Detector Distance</em>' attribute.
	 * @see #isSetSampleDetectorDistance()
	 * @see #unsetSampleDetectorDistance()
	 * @see #setSampleDetectorDistance(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_SampleDetectorDistance()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	double getSampleDetectorDistance();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleDetectorDistance <em>Sample Detector Distance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample Detector Distance</em>' attribute.
	 * @see #isSetSampleDetectorDistance()
	 * @see #unsetSampleDetectorDistance()
	 * @see #getSampleDetectorDistance()
	 * @generated
	 */
	void setSampleDetectorDistance(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleDetectorDistance <em>Sample Detector Distance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleDetectorDistance()
	 * @see #getSampleDetectorDistance()
	 * @see #setSampleDetectorDistance(double)
	 * @generated
	 */
	void unsetSampleDetectorDistance();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleDetectorDistance <em>Sample Detector Distance</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample Detector Distance</em>' attribute is set.
	 * @see #unsetSampleDetectorDistance()
	 * @see #getSampleDetectorDistance()
	 * @see #setSampleDetectorDistance(double)
	 * @generated
	 */
	boolean isSetSampleDetectorDistance();

	/**
	 * Returns the value of the '<em><b>Sample Params</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample Params</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample Params</em>' containment reference.
	 * @see #isSetSampleParams()
	 * @see #unsetSampleParams()
	 * @see #setSampleParams(SampleParams)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getAlignmentConfiguration_SampleParams()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	SampleParams getSampleParams();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleParams <em>Sample Params</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample Params</em>' containment reference.
	 * @see #isSetSampleParams()
	 * @see #unsetSampleParams()
	 * @see #getSampleParams()
	 * @generated
	 */
	void setSampleParams(SampleParams value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleParams <em>Sample Params</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleParams()
	 * @see #getSampleParams()
	 * @see #setSampleParams(SampleParams)
	 * @generated
	 */
	void unsetSampleParams();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleParams <em>Sample Params</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample Params</em>' containment reference is set.
	 * @see #unsetSampleParams()
	 * @see #getSampleParams()
	 * @see #setSampleParams(SampleParams)
	 * @generated
	 */
	boolean isSetSampleParams();

	/**
	 * Returns the value of the '<em><b>Scan Mode</b></em>' attribute.
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
	 * @model unsettable="true" required="true"
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

} // AlignmentConfiguration
