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
 * A representation of the model object '<em><b>Sample Stage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleStage#getVertical <em>Vertical</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterX <em>Center X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterZ <em>Center Z</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltX <em>Tilt X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltZ <em>Tilt Z</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.SampleStage#getBaseX <em>Base X</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage()
 * @model
 * @generated
 */
public interface SampleStage extends EObject {
	/**
	 * Returns the value of the '<em><b>Vertical</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Vertical</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Vertical</em>' containment reference.
	 * @see #isSetVertical()
	 * @see #unsetVertical()
	 * @see #setVertical(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage_Vertical()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getVertical();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getVertical <em>Vertical</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Vertical</em>' containment reference.
	 * @see #isSetVertical()
	 * @see #unsetVertical()
	 * @see #getVertical()
	 * @generated
	 */
	void setVertical(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getVertical <em>Vertical</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetVertical()
	 * @see #getVertical()
	 * @see #setVertical(ValueUnit)
	 * @generated
	 */
	void unsetVertical();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getVertical <em>Vertical</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Vertical</em>' containment reference is set.
	 * @see #unsetVertical()
	 * @see #getVertical()
	 * @see #setVertical(ValueUnit)
	 * @generated
	 */
	boolean isSetVertical();

	/**
	 * Returns the value of the '<em><b>Center X</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Center X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Center X</em>' containment reference.
	 * @see #isSetCenterX()
	 * @see #unsetCenterX()
	 * @see #setCenterX(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage_CenterX()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getCenterX();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterX <em>Center X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Center X</em>' containment reference.
	 * @see #isSetCenterX()
	 * @see #unsetCenterX()
	 * @see #getCenterX()
	 * @generated
	 */
	void setCenterX(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterX <em>Center X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCenterX()
	 * @see #getCenterX()
	 * @see #setCenterX(ValueUnit)
	 * @generated
	 */
	void unsetCenterX();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterX <em>Center X</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Center X</em>' containment reference is set.
	 * @see #unsetCenterX()
	 * @see #getCenterX()
	 * @see #setCenterX(ValueUnit)
	 * @generated
	 */
	boolean isSetCenterX();

	/**
	 * Returns the value of the '<em><b>Center Z</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Center Z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Center Z</em>' containment reference.
	 * @see #isSetCenterZ()
	 * @see #unsetCenterZ()
	 * @see #setCenterZ(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage_CenterZ()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getCenterZ();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterZ <em>Center Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Center Z</em>' containment reference.
	 * @see #isSetCenterZ()
	 * @see #unsetCenterZ()
	 * @see #getCenterZ()
	 * @generated
	 */
	void setCenterZ(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterZ <em>Center Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCenterZ()
	 * @see #getCenterZ()
	 * @see #setCenterZ(ValueUnit)
	 * @generated
	 */
	void unsetCenterZ();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getCenterZ <em>Center Z</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Center Z</em>' containment reference is set.
	 * @see #unsetCenterZ()
	 * @see #getCenterZ()
	 * @see #setCenterZ(ValueUnit)
	 * @generated
	 */
	boolean isSetCenterZ();

	/**
	 * Returns the value of the '<em><b>Tilt X</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tilt X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tilt X</em>' containment reference.
	 * @see #isSetTiltX()
	 * @see #unsetTiltX()
	 * @see #setTiltX(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage_TiltX()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getTiltX();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltX <em>Tilt X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tilt X</em>' containment reference.
	 * @see #isSetTiltX()
	 * @see #unsetTiltX()
	 * @see #getTiltX()
	 * @generated
	 */
	void setTiltX(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltX <em>Tilt X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetTiltX()
	 * @see #getTiltX()
	 * @see #setTiltX(ValueUnit)
	 * @generated
	 */
	void unsetTiltX();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltX <em>Tilt X</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Tilt X</em>' containment reference is set.
	 * @see #unsetTiltX()
	 * @see #getTiltX()
	 * @see #setTiltX(ValueUnit)
	 * @generated
	 */
	boolean isSetTiltX();

	/**
	 * Returns the value of the '<em><b>Tilt Z</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tilt Z</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tilt Z</em>' containment reference.
	 * @see #isSetTiltZ()
	 * @see #unsetTiltZ()
	 * @see #setTiltZ(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage_TiltZ()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getTiltZ();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltZ <em>Tilt Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tilt Z</em>' containment reference.
	 * @see #isSetTiltZ()
	 * @see #unsetTiltZ()
	 * @see #getTiltZ()
	 * @generated
	 */
	void setTiltZ(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltZ <em>Tilt Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetTiltZ()
	 * @see #getTiltZ()
	 * @see #setTiltZ(ValueUnit)
	 * @generated
	 */
	void unsetTiltZ();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getTiltZ <em>Tilt Z</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Tilt Z</em>' containment reference is set.
	 * @see #unsetTiltZ()
	 * @see #getTiltZ()
	 * @see #setTiltZ(ValueUnit)
	 * @generated
	 */
	boolean isSetTiltZ();

	/**
	 * Returns the value of the '<em><b>Base X</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Base X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Base X</em>' containment reference.
	 * @see #isSetBaseX()
	 * @see #unsetBaseX()
	 * @see #setBaseX(ValueUnit)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleStage_BaseX()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	ValueUnit getBaseX();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getBaseX <em>Base X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base X</em>' containment reference.
	 * @see #isSetBaseX()
	 * @see #unsetBaseX()
	 * @see #getBaseX()
	 * @generated
	 */
	void setBaseX(ValueUnit value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getBaseX <em>Base X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetBaseX()
	 * @see #getBaseX()
	 * @see #setBaseX(ValueUnit)
	 * @generated
	 */
	void unsetBaseX();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.SampleStage#getBaseX <em>Base X</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Base X</em>' containment reference is set.
	 * @see #unsetBaseX()
	 * @see #getBaseX()
	 * @see #setBaseX(ValueUnit)
	 * @generated
	 */
	boolean isSetBaseX();

} // SampleStage
