/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Preprocessing Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksBefore <em>High Peaks Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getRingArtefacts <em>Ring Artefacts</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getIntensity <em>Intensity</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterRows <em>High Peaks After Rows</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterColumns <em>High Peaks After Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getPreprocessingType()
 * @model extendedMetaData="name='Preprocessing_._type' kind='elementOnly'"
 * @generated
 */
public interface PreprocessingType extends EObject {
	/**
	 * Returns the value of the '<em><b>High Peaks Before</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>High Peaks Before</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>High Peaks Before</em>' containment reference.
	 * @see #setHighPeaksBefore(HighPeaksBeforeType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getPreprocessingType_HighPeaksBefore()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='HighPeaksBefore' namespace='##targetNamespace'"
	 * @generated
	 */
	HighPeaksBeforeType getHighPeaksBefore();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksBefore <em>High Peaks Before</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>High Peaks Before</em>' containment reference.
	 * @see #getHighPeaksBefore()
	 * @generated
	 */
	void setHighPeaksBefore(HighPeaksBeforeType value);

	/**
	 * Returns the value of the '<em><b>Ring Artefacts</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ring Artefacts</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ring Artefacts</em>' containment reference.
	 * @see #setRingArtefacts(RingArtefactsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getPreprocessingType_RingArtefacts()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='RingArtefacts' namespace='##targetNamespace'"
	 * @generated
	 */
	RingArtefactsType getRingArtefacts();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getRingArtefacts <em>Ring Artefacts</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ring Artefacts</em>' containment reference.
	 * @see #getRingArtefacts()
	 * @generated
	 */
	void setRingArtefacts(RingArtefactsType value);

	/**
	 * Returns the value of the '<em><b>Intensity</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Intensity</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intensity</em>' containment reference.
	 * @see #setIntensity(IntensityType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getPreprocessingType_Intensity()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Intensity' namespace='##targetNamespace'"
	 * @generated
	 */
	IntensityType getIntensity();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getIntensity <em>Intensity</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Intensity</em>' containment reference.
	 * @see #getIntensity()
	 * @generated
	 */
	void setIntensity(IntensityType value);

	/**
	 * Returns the value of the '<em><b>High Peaks After Rows</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>High Peaks After Rows</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>High Peaks After Rows</em>' containment reference.
	 * @see #setHighPeaksAfterRows(HighPeaksAfterRowsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getPreprocessingType_HighPeaksAfterRows()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='HighPeaksAfterRows' namespace='##targetNamespace'"
	 * @generated
	 */
	HighPeaksAfterRowsType getHighPeaksAfterRows();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterRows <em>High Peaks After Rows</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>High Peaks After Rows</em>' containment reference.
	 * @see #getHighPeaksAfterRows()
	 * @generated
	 */
	void setHighPeaksAfterRows(HighPeaksAfterRowsType value);

	/**
	 * Returns the value of the '<em><b>High Peaks After Columns</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>High Peaks After Columns</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>High Peaks After Columns</em>' containment reference.
	 * @see #setHighPeaksAfterColumns(HighPeaksAfterColumnsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getPreprocessingType_HighPeaksAfterColumns()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='HighPeaksAfterColumns' namespace='##targetNamespace'"
	 * @generated
	 */
	HighPeaksAfterColumnsType getHighPeaksAfterColumns();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType#getHighPeaksAfterColumns <em>High Peaks After Columns</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>High Peaks After Columns</em>' containment reference.
	 * @see #getHighPeaksAfterColumns()
	 * @generated
	 */
	void setHighPeaksAfterColumns(HighPeaksAfterColumnsType value);

} // PreprocessingType
