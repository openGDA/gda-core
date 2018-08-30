/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import uk.ac.gda.excalibur.config.model.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage
 * @generated
 */
public class ExcaliburConfigSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ExcaliburConfigPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExcaliburConfigSwitch() {
		if (modelPackage == null) {
			modelPackage = ExcaliburConfigPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @parameter ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ExcaliburConfigPackage.ANPER_MODEL: {
				AnperModel anperModel = (AnperModel)theEObject;
				T result = caseAnperModel(anperModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL: {
				ArrayCountsModel arrayCountsModel = (ArrayCountsModel)theEObject;
				T result = caseArrayCountsModel(arrayCountsModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.BASE_NODE: {
				BaseNode baseNode = (BaseNode)theEObject;
				T result = caseBaseNode(baseNode);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG: {
				ExcaliburConfig excaliburConfig = (ExcaliburConfig)theEObject;
				T result = caseExcaliburConfig(excaliburConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.GAP_MODEL: {
				GapModel gapModel = (GapModel)theEObject;
				T result = caseGapModel(gapModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL: {
				MasterConfigAdbaseModel masterConfigAdbaseModel = (MasterConfigAdbaseModel)theEObject;
				T result = caseMasterConfigAdbaseModel(masterConfigAdbaseModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE: {
				MasterConfigNode masterConfigNode = (MasterConfigNode)theEObject;
				T result = caseMasterConfigNode(masterConfigNode);
				if (result == null) result = caseBaseNode(masterConfigNode);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.MASTER_MODEL: {
				MasterModel masterModel = (MasterModel)theEObject;
				T result = caseMasterModel(masterModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL: {
				MpxiiiChipRegModel mpxiiiChipRegModel = (MpxiiiChipRegModel)theEObject;
				T result = caseMpxiiiChipRegModel(mpxiiiChipRegModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL: {
				MpxiiiGlobalRegModel mpxiiiGlobalRegModel = (MpxiiiGlobalRegModel)theEObject;
				T result = caseMpxiiiGlobalRegModel(mpxiiiGlobalRegModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.PIXEL_MODEL: {
				PixelModel pixelModel = (PixelModel)theEObject;
				T result = casePixelModel(pixelModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.READOUT_NODE: {
				ReadoutNode readoutNode = (ReadoutNode)theEObject;
				T result = caseReadoutNode(readoutNode);
				if (result == null) result = caseBaseNode(readoutNode);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL: {
				ReadoutNodeFemModel readoutNodeFemModel = (ReadoutNodeFemModel)theEObject;
				T result = caseReadoutNodeFemModel(readoutNodeFemModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL: {
				SummaryAdbaseModel summaryAdbaseModel = (SummaryAdbaseModel)theEObject;
				T result = caseSummaryAdbaseModel(summaryAdbaseModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.SUMMARY_NODE: {
				SummaryNode summaryNode = (SummaryNode)theEObject;
				T result = caseSummaryNode(summaryNode);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ExcaliburConfigPackage.FIX_MODEL: {
				FixModel fixModel = (FixModel)theEObject;
				T result = caseFixModel(fixModel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Anper Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Anper Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAnperModel(AnperModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Array Counts Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Array Counts Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseArrayCountsModel(ArrayCountsModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Base Node</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Base Node</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBaseNode(BaseNode object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Excalibur Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Excalibur Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExcaliburConfig(ExcaliburConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Gap Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Gap Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGapModel(GapModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Master Config Adbase Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Master Config Adbase Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMasterConfigAdbaseModel(MasterConfigAdbaseModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Master Config Node</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Master Config Node</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMasterConfigNode(MasterConfigNode object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Master Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Master Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMasterModel(MasterModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Mpxiii Chip Reg Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Mpxiii Chip Reg Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMpxiiiChipRegModel(MpxiiiChipRegModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Mpxiii Global Reg Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Mpxiii Global Reg Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMpxiiiGlobalRegModel(MpxiiiGlobalRegModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Pixel Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Pixel Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePixelModel(PixelModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Readout Node</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Readout Node</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReadoutNode(ReadoutNode object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Readout Node Fem Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Readout Node Fem Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReadoutNodeFemModel(ReadoutNodeFemModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Summary Adbase Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Summary Adbase Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSummaryAdbaseModel(SummaryAdbaseModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Summary Node</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Summary Node</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSummaryNode(SummaryNode object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Fix Model</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Fix Model</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFixModel(FixModel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //ExcaliburConfigSwitch
