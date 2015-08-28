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
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.model.ldeexperiment.impl.SampledefinitionModelPlugin;

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
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Has ID' of 'Stage'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int STAGE__HAS_ID = 1;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Has Cell ID' of 'Cell'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int CELL__HAS_CELL_ID = 2;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Has Visit ID' of 'Cell'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int CELL__HAS_VISIT_ID = 3;

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 3;

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
			case LDEExperimentsPackage.CALIBRANT_NAME_STRING:
				return validateCalibrantNameString((String)value, diagnostics, context);
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
		if (!validate_NoCircularContainment(experiment, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(experiment, diagnostics, context);
		if (result || diagnostics != null) result &= validateExperiment_NonNegativeQuantity(experiment, diagnostics, context);
		return result;
	}

	/**
	 * Validates the NonNegativeQuantity constraint of '<em>Experiment</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean validateExperiment_NonNegativeQuantity(Experiment experiment, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (experiment.getNumberOfStages()<0) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.EXPERIMENT__NUMBER_OF_STAGES,
						 "_UI_NumberOfStagesConstraint_diagnostic",
						 new Object[] { "NonNegativeQuantity", getObjectLabel(experiment, context) },
						 new Object[] { experiment },
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
		if (result || diagnostics != null) result &= validateStage_NonNegativeQuantity(stage, diagnostics, context);
		if (result || diagnostics != null) result &= validateStage_hasID(stage, diagnostics, context);
		return result;
	}

	/**
	 * Validates the NonNegativeQuantity constraint of '<em>Stage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean validateStage_NonNegativeQuantity(Stage stage, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (stage.getNumberOfCells()<0) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.STAGE__NUMBER_OF_CELLS,
						 "_UI_NumberOfCellsConstraint_diagnostic",
						 new Object[] { "NonNegativeQuantity", getObjectLabel(stage, context) },
						 new Object[] { stage },
						 context));
			}
			return false;
		}
		return true;
	}

	/**
	 * Validates the hasID constraint of '<em>Stage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateStage_hasID(Stage stage, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return stage.hasID(diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCell(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(cell, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validateCell_ValidStartDate(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validateCell_ValidEndDate(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validateCell_NonNegativeQuantity(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validateCell_hasCellID(cell, diagnostics, context);
		if (result || diagnostics != null) result &= validateCell_hasVisitID(cell, diagnostics, context);
		return result;
	}

	/**
	 * Validates the ValidStartDate constraint of '<em>Cell</em>'.
	 * <!-- begin-user-doc -->
	 * check if start date is null or after end date.
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean validateCell_ValidStartDate(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (cell.getStartDate()== null) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.CELL__START_DATE,
						 "_UI_StartDateNullConstraint_diagnostic",
						 new Object[] { "ValidStartDate", getObjectLabel(cell, context) },
						 new Object[] { cell },
						 context));
			}
			return false;
		} else if (cell.getEndDate()!=null && cell.getStartDate().after(cell.getEndDate())) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.CELL__START_DATE,
						 "_UI_StartDateAfterEndDateConstraint_diagnostic",
						 new Object[] { "ValidStartDate", getObjectLabel(cell, context) },
						 new Object[] { cell },
						 context));
			}
			return false;			
		}
		return true;
	}

	/**
	 * Validates the ValidEndDate constraint of '<em>Cell</em>'.
	 * <!-- begin-user-doc -->
	 * check if end date is null or before start date.
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean validateCell_ValidEndDate(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (cell.getEndDate()==null) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.CELL__END_DATE,
						 "_UI_EndDateNullConstraint_diagnostic",
						 new Object[] { "ValidEndDate", getObjectLabel(cell, context) },
						 new Object[] { cell },
						 context));
			}
			return false;
		} else if (cell.getStartDate()!=null && cell.getEndDate().before(cell.getStartDate())) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.CELL__END_DATE,
						 "_UI_EndDateBeforeStartDateConstraint_diagnostic",
						 new Object[] { "ValidEndDate", getObjectLabel(cell, context) },
						 new Object[] { cell },
						 context));
			}
			return false;
		}
		return true;
	}

	/**
	 * Validates the NonNegativeQuantity constraint of '<em>Cell</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean validateCell_NonNegativeQuantity(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (cell.getNumberOfSamples()<0) {
			if (diagnostics != null) {
				diagnostics.add
					(createDiagnostic
						(Diagnostic.ERROR,
						 DIAGNOSTIC_SOURCE,
						 LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES,
						 "_UI_NumberOfSamplesConstraint_diagnostic",
						 new Object[] { "NonNegativeQuantity", getObjectLabel(cell, context) },
						 new Object[] { cell },
						 context));
			}
			return false;
		}
		return true;
	}

	/**
	 * Validates the hasCellID constraint of '<em>Cell</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCell_hasCellID(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return cell.hasCellID(diagnostics, context);
	}

	/**
	 * Validates the hasVisitID constraint of '<em>Cell</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCell_hasVisitID(Cell cell, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return cell.hasVisitID(diagnostics, context);
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCalibrantNameString(String calibrantNameString, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validateCalibrantNameString_Enumeration(calibrantNameString, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @see #validateCalibrantNameString_Enumeration
	 */
	public static final Collection<Object> CALIBRANT_NAME_STRING__ENUMERATION__VALUES =
		wrapEnumerationValues
			(new Object[] {
				 "Si(NIST-SRM-640c)",
				 "CeO2(NIST-SRM-674b)"
			 });

	/**
	 * Validates the Enumeration constraint of '<em>Calibrant Name String</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean validateCalibrantNameString_Enumeration(String calibrantNameString, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = CALIBRANT_NAME_STRING__ENUMERATION__VALUES.contains(calibrantNameString);
		if (!result && diagnostics != null)
			reportEnumerationViolation(LDEExperimentsPackage.Literals.CALIBRANT_NAME_STRING, calibrantNameString, CALIBRANT_NAME_STRING__ENUMERATION__VALUES, diagnostics, context);
		return result;
	}

	/**
	 * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return SampledefinitionModelPlugin.INSTANCE;
	}

} //LDEExperimentsValidator
