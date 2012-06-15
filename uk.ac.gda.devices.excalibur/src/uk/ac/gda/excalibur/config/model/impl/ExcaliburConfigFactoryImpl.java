/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import uk.ac.gda.excalibur.config.model.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ExcaliburConfigFactoryImpl extends EFactoryImpl implements ExcaliburConfigFactory {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ExcaliburConfigFactory init() {
		try {
			ExcaliburConfigFactory theExcaliburConfigFactory = (ExcaliburConfigFactory)EPackage.Registry.INSTANCE.getEFactory("http:///uk/ac/gda/excalibur/config/model.ecore"); 
			if (theExcaliburConfigFactory != null) {
				return theExcaliburConfigFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ExcaliburConfigFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExcaliburConfigFactoryImpl() {
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
			case ExcaliburConfigPackage.ANPER_MODEL: return createAnperModel();
			case ExcaliburConfigPackage.ARRAY_COUNTS_MODEL: return createArrayCountsModel();
			case ExcaliburConfigPackage.BASE_NODE: return createBaseNode();
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG: return createExcaliburConfig();
			case ExcaliburConfigPackage.GAP_MODEL: return createGapModel();
			case ExcaliburConfigPackage.MASTER_CONFIG_ADBASE_MODEL: return createMasterConfigAdbaseModel();
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE: return createMasterConfigNode();
			case ExcaliburConfigPackage.MASTER_MODEL: return createMasterModel();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL: return createMpxiiiChipRegModel();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL: return createMpxiiiGlobalRegModel();
			case ExcaliburConfigPackage.PIXEL_MODEL: return createPixelModel();
			case ExcaliburConfigPackage.READOUT_NODE: return createReadoutNode();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL: return createReadoutNodeFemModel();
			case ExcaliburConfigPackage.SUMMARY_ADBASE_MODEL: return createSummaryAdbaseModel();
			case ExcaliburConfigPackage.SUMMARY_NODE: return createSummaryNode();
			case ExcaliburConfigPackage.FIX_MODEL: return createFixModel();
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
			case ExcaliburConfigPackage.EXCEPTION:
				return createExceptionFromString(eDataType, initialValue);
			case ExcaliburConfigPackage.STRING_ARRAY:
				return createStringArrayFromString(eDataType, initialValue);
			case ExcaliburConfigPackage.SHORT_ARRAY:
				return createShortArrayFromString(eDataType, initialValue);
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
			case ExcaliburConfigPackage.EXCEPTION:
				return convertExceptionToString(eDataType, instanceValue);
			case ExcaliburConfigPackage.STRING_ARRAY:
				return convertStringArrayToString(eDataType, instanceValue);
			case ExcaliburConfigPackage.SHORT_ARRAY:
				return convertShortArrayToString(eDataType, instanceValue);
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
	public AnperModel createAnperModel() {
		AnperModelImpl anperModel = new AnperModelImpl();
		return anperModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ArrayCountsModel createArrayCountsModel() {
		ArrayCountsModelImpl arrayCountsModel = new ArrayCountsModelImpl();
		return arrayCountsModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BaseNode createBaseNode() {
		BaseNodeImpl baseNode = new BaseNodeImpl();
		return baseNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExcaliburConfig createExcaliburConfig() {
		ExcaliburConfigImpl excaliburConfig = new ExcaliburConfigImpl();
		return excaliburConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GapModel createGapModel() {
		GapModelImpl gapModel = new GapModelImpl();
		return gapModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MasterConfigAdbaseModel createMasterConfigAdbaseModel() {
		MasterConfigAdbaseModelImpl masterConfigAdbaseModel = new MasterConfigAdbaseModelImpl();
		return masterConfigAdbaseModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MasterConfigNode createMasterConfigNode() {
		MasterConfigNodeImpl masterConfigNode = new MasterConfigNodeImpl();
		return masterConfigNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MasterModel createMasterModel() {
		MasterModelImpl masterModel = new MasterModelImpl();
		return masterModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel createMpxiiiChipRegModel() {
		MpxiiiChipRegModelImpl mpxiiiChipRegModel = new MpxiiiChipRegModelImpl();
		return mpxiiiChipRegModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiGlobalRegModel createMpxiiiGlobalRegModel() {
		MpxiiiGlobalRegModelImpl mpxiiiGlobalRegModel = new MpxiiiGlobalRegModelImpl();
		return mpxiiiGlobalRegModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PixelModel createPixelModel() {
		PixelModelImpl pixelModel = new PixelModelImpl();
		return pixelModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReadoutNode createReadoutNode() {
		ReadoutNodeImpl readoutNode = new ReadoutNodeImpl();
		return readoutNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReadoutNodeFemModel createReadoutNodeFemModel() {
		ReadoutNodeFemModelImpl readoutNodeFemModel = new ReadoutNodeFemModelImpl();
		return readoutNodeFemModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SummaryAdbaseModel createSummaryAdbaseModel() {
		SummaryAdbaseModelImpl summaryAdbaseModel = new SummaryAdbaseModelImpl();
		return summaryAdbaseModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SummaryNode createSummaryNode() {
		SummaryNodeImpl summaryNode = new SummaryNodeImpl();
		return summaryNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FixModel createFixModel() {
		FixModelImpl fixModel = new FixModelImpl();
		return fixModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Exception createExceptionFromString(EDataType eDataType, String initialValue) {
		return (Exception)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertExceptionToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public String[] createStringArrayFromString(EDataType eDataType, String initialValue) {
		return (String[])super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public String convertStringArrayToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public short[] createShortArrayFromString(EDataType eDataType, String initialValue) {
		return (short[])super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public String convertShortArrayToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExcaliburConfigPackage getExcaliburConfigPackage() {
		return (ExcaliburConfigPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ExcaliburConfigPackage getPackage() {
		return ExcaliburConfigPackage.eINSTANCE;
	}

} //ExcaliburConfigFactoryImpl
