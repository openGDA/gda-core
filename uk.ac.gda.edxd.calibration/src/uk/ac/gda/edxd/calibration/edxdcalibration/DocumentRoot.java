/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Document Root</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot#getEdxdCalibration <em>Edxd Calibration</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getDocumentRoot()
 * @model
 * @generated
 */
public interface DocumentRoot extends EObject {
	/**
	 * Returns the value of the '<em><b>Edxd Calibration</b></em>' containment reference list.
	 * The list contents are of type {@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Edxd Calibration</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Edxd Calibration</em>' containment reference list.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getDocumentRoot_EdxdCalibration()
	 * @model containment="true"
	 * @generated
	 */
	EList<EdxdCalibration> getEdxdCalibration();

} // DocumentRoot
