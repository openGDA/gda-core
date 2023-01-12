/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage
 * @generated
 */
public interface ExcaliburConfigFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ExcaliburConfigFactory eINSTANCE = uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Anper Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Anper Model</em>'.
	 * @generated
	 */
	AnperModel createAnperModel();

	/**
	 * Returns a new object of class '<em>Array Counts Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Array Counts Model</em>'.
	 * @generated
	 */
	ArrayCountsModel createArrayCountsModel();

	/**
	 * Returns a new object of class '<em>Base Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Base Node</em>'.
	 * @generated
	 */
	BaseNode createBaseNode();

	/**
	 * Returns a new object of class '<em>Excalibur Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Excalibur Config</em>'.
	 * @generated
	 */
	ExcaliburConfig createExcaliburConfig();

	/**
	 * Returns a new object of class '<em>Gap Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Gap Model</em>'.
	 * @generated
	 */
	GapModel createGapModel();

	/**
	 * Returns a new object of class '<em>Master Config Adbase Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Master Config Adbase Model</em>'.
	 * @generated
	 */
	MasterConfigAdbaseModel createMasterConfigAdbaseModel();

	/**
	 * Returns a new object of class '<em>Master Config Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Master Config Node</em>'.
	 * @generated
	 */
	MasterConfigNode createMasterConfigNode();

	/**
	 * Returns a new object of class '<em>Master Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Master Model</em>'.
	 * @generated
	 */
	MasterModel createMasterModel();

	/**
	 * Returns a new object of class '<em>Mpxiii Chip Reg Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Mpxiii Chip Reg Model</em>'.
	 * @generated
	 */
	MpxiiiChipRegModel createMpxiiiChipRegModel();

	/**
	 * Returns a new object of class '<em>Mpxiii Global Reg Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Mpxiii Global Reg Model</em>'.
	 * @generated
	 */
	MpxiiiGlobalRegModel createMpxiiiGlobalRegModel();

	/**
	 * Returns a new object of class '<em>Pixel Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Pixel Model</em>'.
	 * @generated
	 */
	PixelModel createPixelModel();

	/**
	 * Returns a new object of class '<em>Readout Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Readout Node</em>'.
	 * @generated
	 */
	ReadoutNode createReadoutNode();

	/**
	 * Returns a new object of class '<em>Readout Node Fem Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Readout Node Fem Model</em>'.
	 * @generated
	 */
	ReadoutNodeFemModel createReadoutNodeFemModel();

	/**
	 * Returns a new object of class '<em>Summary Adbase Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Summary Adbase Model</em>'.
	 * @generated
	 */
	SummaryAdbaseModel createSummaryAdbaseModel();

	/**
	 * Returns a new object of class '<em>Summary Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Summary Node</em>'.
	 * @generated
	 */
	SummaryNode createSummaryNode();

	/**
	 * Returns a new object of class '<em>Fix Model</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Fix Model</em>'.
	 * @generated
	 */
	FixModel createFixModel();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ExcaliburConfigPackage getExcaliburConfigPackage();

} //ExcaliburConfigFactory
