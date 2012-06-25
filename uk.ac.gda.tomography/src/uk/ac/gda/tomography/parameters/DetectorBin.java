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
 * A representation of the model object '<em><b>Detector Bin</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinX <em>Bin X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinY <em>Bin Y</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorBin()
 * @model
 * @generated
 */
public interface DetectorBin extends EObject {
	/**
	 * Returns the value of the '<em><b>Bin X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bin X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bin X</em>' attribute.
	 * @see #isSetBinX()
	 * @see #unsetBinX()
	 * @see #setBinX(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorBin_BinX()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getBinX();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinX <em>Bin X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bin X</em>' attribute.
	 * @see #isSetBinX()
	 * @see #unsetBinX()
	 * @see #getBinX()
	 * @generated
	 */
	void setBinX(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinX <em>Bin X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetBinX()
	 * @see #getBinX()
	 * @see #setBinX(Integer)
	 * @generated
	 */
	void unsetBinX();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinX <em>Bin X</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Bin X</em>' attribute is set.
	 * @see #unsetBinX()
	 * @see #getBinX()
	 * @see #setBinX(Integer)
	 * @generated
	 */
	boolean isSetBinX();

	/**
	 * Returns the value of the '<em><b>Bin Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bin Y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bin Y</em>' attribute.
	 * @see #isSetBinY()
	 * @see #unsetBinY()
	 * @see #setBinY(Integer)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getDetectorBin_BinY()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	Integer getBinY();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinY <em>Bin Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bin Y</em>' attribute.
	 * @see #isSetBinY()
	 * @see #unsetBinY()
	 * @see #getBinY()
	 * @generated
	 */
	void setBinY(Integer value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinY <em>Bin Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetBinY()
	 * @see #getBinY()
	 * @see #setBinY(Integer)
	 * @generated
	 */
	void unsetBinY();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinY <em>Bin Y</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Bin Y</em>' attribute is set.
	 * @see #unsetBinY()
	 * @see #getBinY()
	 * @see #setBinY(Integer)
	 * @generated
	 */
	boolean isSetBinY();

} // DetectorBin
