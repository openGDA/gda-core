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
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModule <em>Module</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi <em>Detector Roi</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin <em>Detector Bin</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties()
 * @model
 * @generated
 */
public interface DetectorProperties extends EObject {
	/**
	 * Returns the value of the '<em><b>Module</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Module</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Module</em>' attribute.
	 * @see #isSetModule()
	 * @see #unsetModule()
	 * @see #setModule(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_Module()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getModule();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModule <em>Module</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Module</em>' attribute.
	 * @see #isSetModule()
	 * @see #unsetModule()
	 * @see #getModule()
	 * @generated
	 */
	void setModule(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModule <em>Module</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetModule()
	 * @see #getModule()
	 * @see #setModule(Integer)
	 * @generated
	 */
	void unsetModule();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModule <em>Module</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Module</em>' attribute is set.
	 * @see #unsetModule()
	 * @see #getModule()
	 * @see #setModule(Integer)
	 * @generated
	 */
	boolean isSetModule();

	/**
	 * Returns the value of the '<em><b>Desired3 DResolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Desired3 DResolution</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Desired3 DResolution</em>' attribute.
	 * @see #isSetDesired3DResolution()
	 * @see #unsetDesired3DResolution()
	 * @see #setDesired3DResolution(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_Desired3DResolution()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getDesired3DResolution();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Desired3 DResolution</em>' attribute.
	 * @see #isSetDesired3DResolution()
	 * @see #unsetDesired3DResolution()
	 * @see #getDesired3DResolution()
	 * @generated
	 */
	void setDesired3DResolution(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDesired3DResolution()
	 * @see #getDesired3DResolution()
	 * @see #setDesired3DResolution(Integer)
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
	 * @see #setDesired3DResolution(Integer)
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
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Acquisition Time Divider</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Acquisition Time Divider</em>' attribute.
	 * @see #isSetAcquisitionTimeDivider()
	 * @see #unsetAcquisitionTimeDivider()
	 * @see #setAcquisitionTimeDivider(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorProperties_AcquisitionTimeDivider()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getAcquisitionTimeDivider();

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
	void setAcquisitionTimeDivider(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetAcquisitionTimeDivider()
	 * @see #getAcquisitionTimeDivider()
	 * @see #setAcquisitionTimeDivider(Integer)
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
	 * @see #setAcquisitionTimeDivider(Integer)
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

} // DetectorProperties
