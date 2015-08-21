/**
 * Copyright ©2015 Diamond Light Source Ltd
 * 
 * This file is part of GDA.
 *  
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * 	Fajin Yuan
 */
package org.opengda.lde.model.ldeexperiment.util;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import org.opengda.lde.model.ldeexperiment.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage
 * @generated
 */
public class LDEExperimentsValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final LDEExperimentsValidator INSTANCE = new LDEExperimentsValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "org.opengda.lde.model.ldeexperiment";

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LDEExperimentsValidator() {
		super();
	}

	/**
	 * Returns the package of this validator switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EPackage getEPackage() {
	  return LDEExperimentsPackage.eINSTANCE;
	}

	/**
	 * Calls <code>validateXXX</code> for the corresponding classifier of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
		switch (classifierID) {
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION:
				return validateExperimentDefinition((ExperimentDefinition)value, diagnostics, context);
			case LDEExperimentsPackage.EXPERIMENT:
				return validateExperiment((Experiment)value, diagnostics, context);
			case LDEExperimentsPackage.STAGE:
				return validateStage((Stage)value, diagnostics, context);
			case LDEExperimentsPackage.CELL:
				return validateCell((Cell)value, diagnostics, context);
			case LDEExperimentsPackage.SAMPLE:
				return validateSample((Sample)value, diagnostics, context);
			case LDEExperimentsPackage.STATUS:
				return validateSTATUS((STATUS)value, diagnostics, context);
			case LDEExperimentsPackage.STAGE_ID_STRING:
				return validateStageIDString((String)value, diagnostics, context);
			case LDEExperimentsPackage.DATE:
				return validateDate((Date)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateExperimentDefinition(ExperimentDefinition experimentDefinition, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(experimentDefinition, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateExperiment(Experiment experiment, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(experiment, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateStage(Stage stage, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(stage, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validateStage_ValidStageID(stage, diagnostics, context);
		return result;
	}

	/**
	 * Validates the ValidStageID constraint of '<em>Stage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateStage_ValidStageID(Stage stage, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// TODO implement the constraint
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (false) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 0,
						 "_UI_GenericConstraint_diagnostic",
						 new Object[] { "ValidStageID", getObjectLabel(stage, context) },
						 new Object[] { stage },
						 context));
			}
			return false;
		}
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCell(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(cell, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateSample(Sample sample, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(sample, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateSTATUS(STATUS status, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateStageIDString(String stageIDString, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validateStageIDString_Enumeration(stageIDString, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @see #validateStageIDString_Enumeration
	 */
	public static final Collection<Object> STAGE_ID_STRING__ENUMERATION__VALUES =
		wrapEnumerationValues
			(new Object[] {
				 "LS1",
				 "LS2",
				 "MS1",
				 "MS2",
				 "MS3",
				 "MS4",
				 "SS1",
				 "SS2",
				 "SS3",
				 "SS4",
				 "SS5",
				 "SS6",
				 "ROBOT"
			 });

	/**
	 * Validates the Enumeration constraint of '<em>Stage ID String</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateStageIDString_Enumeration(String stageIDString, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = STAGE_ID_STRING__ENUMERATION__VALUES.contains(stageIDString);
		if (!result && diagnostics != null)
			reportEnumerationViolation(LDEExperimentsPackage.Literals.STAGE_ID_STRING, stageIDString, STAGE_ID_STRING__ENUMERATION__VALUES, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDate(Date date, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return true;
	}

	/**
	 * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		// TODO
		// Specialize this to return a resource locator for messages specific to this validator.
		// Ensure that you remove @generated or mark it @generated NOT
		return super.getResourceLocator();
	}

} //LDEExperimentsValidator
