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
 * A representation of the model object '<em><b>Detector Properties</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi <em>Detector Roi</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin <em>Detector Bin</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModuleParameters <em>Module Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties()
 * @model
 * @generated
 */
public interface DetectorProperties extends EObject {
	/**
	 * Returns the value of the '<em><b>Desired3 DResolution</b></em>' attribute.
	 * The default value is <code>"Full"</code>.
	 * The literals are from the enumeration {@link uk.ac.gda.tomography.parameters.Resolution}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Desired3 DResolution</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Desired3 DResolution</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.Resolution
	 * @see #isSetDesired3DResolution()
	 * @see #unsetDesired3DResolution()
	 * @see #setDesired3DResolution(Resolution)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_Desired3DResolution()
	 * @model default="Full" unsettable="true" required="true"
	 * @generated
	 */
	Resolution getDesired3DResolution();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Desired3 DResolution</em>' attribute.
	 * @see uk.ac.gda.tomography.parameters.Resolution
	 * @see #isSetDesired3DResolution()
	 * @see #unsetDesired3DResolution()
	 * @see #getDesired3DResolution()
	 * @generated
	 */
	void setDesired3DResolution(Resolution value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDesired3DResolution()
	 * @see #getDesired3DResolution()
	 * @see #setDesired3DResolution(Resolution)
	 * @generated
	 */
	void unsetDesired3DResolution();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Desired3 DResolution</em>' attribute is set.
	 * @see #unsetDesired3DResolution()
	 * @see #getDesired3DResolution()
	 * @see #setDesired3DResolution(Resolution)
	 * @generated
	 */
	boolean isSetDesired3DResolution();

	/**
	 * Returns the value of the '<em><b>Number Of Framer Per Projection</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number Of Framer Per Projection</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number Of Framer Per Projection</em>' attribute.
	 * @see #isSetNumberOfFramerPerProjection()
	 * @see #unsetNumberOfFramerPerProjection()
	 * @see #setNumberOfFramerPerProjection(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_NumberOfFramerPerProjection()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getNumberOfFramerPerProjection();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number Of Framer Per Projection</em>' attribute.
	 * @see #isSetNumberOfFramerPerProjection()
	 * @see #unsetNumberOfFramerPerProjection()
	 * @see #getNumberOfFramerPerProjection()
	 * @generated
	 */
	void setNumberOfFramerPerProjection(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNumberOfFramerPerProjection()
	 * @see #getNumberOfFramerPerProjection()
	 * @see #setNumberOfFramerPerProjection(Integer)
	 * @generated
	 */
	void unsetNumberOfFramerPerProjection();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Number Of Framer Per Projection</em>' attribute is set.
	 * @see #unsetNumberOfFramerPerProjection()
	 * @see #getNumberOfFramerPerProjection()
	 * @see #setNumberOfFramerPerProjection(Integer)
	 * @generated
	 */
	boolean isSetNumberOfFramerPerProjection();

	/**
	 * Returns the value of the '<em><b>Acquisition Time Divider</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Acquisition Time Divider</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Acquisition Time Divider</em>' attribute.
	 * @see #isSetAcquisitionTimeDivider()
	 * @see #unsetAcquisitionTimeDivider()
	 * @see #setAcquisitionTimeDivider(double)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_AcquisitionTimeDivider()
	 * @model default="1" unsettable="true" required="true"
	 * @generated
	 */
	double getAcquisitionTimeDivider();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Acquisition Time Divider</em>' attribute.
	 * @see #isSetAcquisitionTimeDivider()
	 * @see #unsetAcquisitionTimeDivider()
	 * @see #getAcquisitionTimeDivider()
	 * @generated
	 */
	void setAcquisitionTimeDivider(double value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetAcquisitionTimeDivider()
	 * @see #getAcquisitionTimeDivider()
	 * @see #setAcquisitionTimeDivider(double)
	 * @generated
	 */
	void unsetAcquisitionTimeDivider();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Acquisition Time Divider</em>' attribute is set.
	 * @see #unsetAcquisitionTimeDivider()
	 * @see #getAcquisitionTimeDivider()
	 * @see #setAcquisitionTimeDivider(double)
	 * @generated
	 */
	boolean isSetAcquisitionTimeDivider();

	/**
	 * Returns the value of the '<em><b>Detector Roi</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector Roi</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector Roi</em>' containment reference.
	 * @see #isSetDetectorRoi()
	 * @see #unsetDetectorRoi()
	 * @see #setDetectorRoi(DetectorRoi)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_DetectorRoi()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	DetectorRoi getDetectorRoi();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi <em>Detector Roi</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector Roi</em>' containment reference.
	 * @see #isSetDetectorRoi()
	 * @see #unsetDetectorRoi()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	void setDetectorRoi(DetectorRoi value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi <em>Detector Roi</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDetectorRoi()
	 * @see #getDetectorRoi()
	 * @see #setDetectorRoi(DetectorRoi)
	 * @generated
	 */
	void unsetDetectorRoi();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi <em>Detector Roi</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Detector Roi</em>' containment reference is set.
	 * @see #unsetDetectorRoi()
	 * @see #getDetectorRoi()
	 * @see #setDetectorRoi(DetectorRoi)
	 * @generated
	 */
	boolean isSetDetectorRoi();

	/**
	 * Returns the value of the '<em><b>Detector Bin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector Bin</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector Bin</em>' containment reference.
	 * @see #isSetDetectorBin()
	 * @see #unsetDetectorBin()
	 * @see #setDetectorBin(DetectorBin)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_DetectorBin()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	DetectorBin getDetectorBin();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin <em>Detector Bin</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector Bin</em>' containment reference.
	 * @see #isSetDetectorBin()
	 * @see #unsetDetectorBin()
	 * @see #getDetectorBin()
	 * @generated
	 */
	void setDetectorBin(DetectorBin value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin <em>Detector Bin</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDetectorBin()
	 * @see #getDetectorBin()
	 * @see #setDetectorBin(DetectorBin)
	 * @generated
	 */
	void unsetDetectorBin();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin <em>Detector Bin</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Detector Bin</em>' containment reference is set.
	 * @see #unsetDetectorBin()
	 * @see #getDetectorBin()
	 * @see #setDetectorBin(DetectorBin)
	 * @generated
	 */
	boolean isSetDetectorBin();

	/**
	 * Returns the value of the '<em><b>Module Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Module Parameters</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Module Parameters</em>' containment reference.
	 * @see #isSetModuleParameters()
	 * @see #unsetModuleParameters()
	 * @see #setModuleParameters(Module)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_ModuleParameters()
	 * @model containment="true" unsettable="true" required="true"
	 * @generated
	 */
	Module getModuleParameters();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModuleParameters <em>Module Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Module Parameters</em>' containment reference.
	 * @see #isSetModuleParameters()
	 * @see #unsetModuleParameters()
	 * @see #getModuleParameters()
	 * @generated
	 */
	void setModuleParameters(Module value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModuleParameters <em>Module Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetModuleParameters()
	 * @see #getModuleParameters()
	 * @see #setModuleParameters(Module)
	 * @generated
	 */
	void unsetModuleParameters();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModuleParameters <em>Module Parameters</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Module Parameters</em>' containment reference is set.
	 * @see #unsetModuleParameters()
	 * @see #getModuleParameters()
	 * @see #setModuleParameters(Module)
	 * @generated
	 */
	boolean isSetModuleParameters();

} // DetectorProperties
