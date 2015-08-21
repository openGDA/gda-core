/*******************************************************************************
 * Copyright © 2009, 2014 Diamond Light Source Ltd
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
 * 	Diamond Light Source Ltd
 *******************************************************************************/
/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.opengda.lde.model.ldeexperiment.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class LDEExperimentsFactoryImpl extends EFactoryImpl implements LDEExperimentsFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static LDEExperimentsFactory init() {
		try {
			LDEExperimentsFactory theLDEExperimentsFactory = (LDEExperimentsFactory)EPackage.Registry.INSTANCE.getEFactory(LDEExperimentsPackage.eNS_URI);
			if (theLDEExperimentsFactory != null) {
				return theLDEExperimentsFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new LDEExperimentsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LDEExperimentsFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION: return createExperimentDefinition();
			case LDEExperimentsPackage.EXPERIMENT: return createExperiment();
			case LDEExperimentsPackage.STAGE: return createStage();
			case LDEExperimentsPackage.CELL: return createCell();
			case LDEExperimentsPackage.SAMPLE: return createSample();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case LDEExperimentsPackage.STATUS:
				return createSTATUSFromString(eDataType, initialValue);
			case LDEExperimentsPackage.STAGE_ID_STRING:
				return createStageIDStringFromString(eDataType, initialValue);
			case LDEExperimentsPackage.DATE:
				return createDateFromString(eDataType, initialValue);
			case LDEExperimentsPackage.CALIBRANT_NAME_STRING:
				return createCalibrantNameStringFromString(eDataType, initialValue);
			case LDEExperimentsPackage.CELL_ID_STRING:
				return createCellIDStringFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case LDEExperimentsPackage.STATUS:
				return convertSTATUSToString(eDataType, instanceValue);
			case LDEExperimentsPackage.STAGE_ID_STRING:
				return convertStageIDStringToString(eDataType, instanceValue);
			case LDEExperimentsPackage.DATE:
				return convertDateToString(eDataType, instanceValue);
			case LDEExperimentsPackage.CALIBRANT_NAME_STRING:
				return convertCalibrantNameStringToString(eDataType, instanceValue);
			case LDEExperimentsPackage.CELL_ID_STRING:
				return convertCellIDStringToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExperimentDefinition createExperimentDefinition() {
		ExperimentDefinitionImpl experimentDefinition = new ExperimentDefinitionImpl();
		return experimentDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Experiment createExperiment() {
		ExperimentImpl experiment = new ExperimentImpl();
		return experiment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Stage createStage() {
		StageImpl stage = new StageImpl();
		return stage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell createCell() {
		CellImpl cell = new CellImpl();
		return cell;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Sample createSample() {
		SampleImpl sample = new SampleImpl();
		return sample;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public STATUS createSTATUSFromString(EDataType eDataType, String initialValue) {
		STATUS result = STATUS.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSTATUSToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String createStageIDStringFromString(EDataType eDataType, String initialValue) {
		return (String)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertStageIDStringToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Date createDateFromString(EDataType eDataType, String initialValue) {
		if (initialValue==null) return null;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		ParsePosition position= new ParsePosition(0);
		Date result=format.parse(initialValue, position);
		if (position.getIndex()==0) {
			throw new IllegalArgumentException("Date must be of format dd/MM/yyyy");
		}
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertDateToString(EDataType eDataType, Object instanceValue) {
		if (instanceValue==null) return null;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		return format.format((Date)instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String createCalibrantNameStringFromString(EDataType eDataType, String initialValue) {
		return (String)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertCalibrantNameStringToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue).replace('-', ' ');
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String createCellIDStringFromString(EDataType eDataType, String initialValue) {
		return ((String)super.createFromString(eDataType, initialValue)).replace(' ', '-');
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertCellIDStringToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LDEExperimentsPackage getLDEExperimentsPackage() {
		return (LDEExperimentsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static LDEExperimentsPackage getPackage() {
		return LDEExperimentsPackage.eINSTANCE;
	}

} //LDEExperimentsFactoryImpl
