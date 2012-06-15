/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import uk.ac.gda.excalibur.config.model.AnperModel;
import uk.ac.gda.excalibur.config.model.ArrayCountsModel;
import uk.ac.gda.excalibur.config.model.BaseNode;
import uk.ac.gda.excalibur.config.model.ExcaliburConfig;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigFactory;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.FixModel;
import uk.ac.gda.excalibur.config.model.GapModel;
import uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel;
import uk.ac.gda.excalibur.config.model.MasterConfigNode;
import uk.ac.gda.excalibur.config.model.MasterModel;
import uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel;
import uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel;
import uk.ac.gda.excalibur.config.model.PixelModel;
import uk.ac.gda.excalibur.config.model.ReadoutNode;
import uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel;
import uk.ac.gda.excalibur.config.model.SummaryAdbaseModel;
import uk.ac.gda.excalibur.config.model.SummaryNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ExcaliburConfigPackageImpl extends EPackageImpl implements ExcaliburConfigPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass anperModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass arrayCountsModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass baseNodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass excaliburConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass gapModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass masterConfigAdbaseModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass masterConfigNodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass masterModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mpxiiiChipRegModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mpxiiiGlobalRegModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass pixelModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass readoutNodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass readoutNodeFemModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass summaryAdbaseModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass summaryNodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fixModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType exceptionEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType stringArrayEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType shortArrayEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ExcaliburConfigPackageImpl() {
		super(eNS_URI, ExcaliburConfigFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link ExcaliburConfigPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ExcaliburConfigPackage init() {
		if (isInited) return (ExcaliburConfigPackage)EPackage.Registry.INSTANCE.getEPackage(ExcaliburConfigPackage.eNS_URI);

		// Obtain or create and register package
		ExcaliburConfigPackageImpl theExcaliburConfigPackage = (ExcaliburConfigPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ExcaliburConfigPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ExcaliburConfigPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theExcaliburConfigPackage.createPackageContents();

		// Initialize created meta-data
		theExcaliburConfigPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theExcaliburConfigPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ExcaliburConfigPackage.eNS_URI, theExcaliburConfigPackage);
		return theExcaliburConfigPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAnperModel() {
		return anperModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Preamp() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Ikrum() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Shaper() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Disc() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Discls() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Thresholdn() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_DacPixel() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Delay() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_TpBufferIn() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_TpBufferOut() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Rpz() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Gnd() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Tpref() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Fbk() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Cas() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_TprefA() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_TprefB() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold0() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold1() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold2() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold3() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold4() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold5() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold6() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(23);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnperModel_Threshold7() {
		return (EAttribute)anperModelEClass.getEStructuralFeatures().get(24);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getArrayCountsModel() {
		return arrayCountsModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArrayCountsModel_ArrayCountFem1() {
		return (EAttribute)arrayCountsModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArrayCountsModel_ArrayCountFem2() {
		return (EAttribute)arrayCountsModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArrayCountsModel_ArrayCountFem3() {
		return (EAttribute)arrayCountsModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArrayCountsModel_ArrayCountFem4() {
		return (EAttribute)arrayCountsModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArrayCountsModel_ArrayCountFem5() {
		return (EAttribute)arrayCountsModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArrayCountsModel_ArrayCountFem6() {
		return (EAttribute)arrayCountsModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBaseNode() {
		return baseNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBaseNode_Gap() {
		return (EReference)baseNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBaseNode_Mst() {
		return (EReference)baseNodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBaseNode_Fix() {
		return (EReference)baseNodeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExcaliburConfig() {
		return excaliburConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExcaliburConfig_ReadoutNodes() {
		return (EReference)excaliburConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExcaliburConfig_ConfigNode() {
		return (EReference)excaliburConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExcaliburConfig_SummaryNode() {
		return (EReference)excaliburConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGapModel() {
		return gapModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGapModel_GapFillConstant() {
		return (EAttribute)gapModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGapModel_GapFillingEnabled() {
		return (EAttribute)gapModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGapModel_GapFillMode() {
		return (EAttribute)gapModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMasterConfigAdbaseModel() {
		return masterConfigAdbaseModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMasterConfigAdbaseModel_CounterDepth() {
		return (EAttribute)masterConfigAdbaseModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMasterConfigNode() {
		return masterConfigNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMasterConfigNode_ConfigFem() {
		return (EReference)masterConfigNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMasterModel() {
		return masterModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMasterModel_FrameDivisor() {
		return (EAttribute)masterModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMpxiiiChipRegModel() {
		return mpxiiiChipRegModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiChipRegModel_DacSense() {
		return (EAttribute)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiChipRegModel_DacSenseDecode() {
		return (EAttribute)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiChipRegModel_DacSenseName() {
		return (EAttribute)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiChipRegModel_DacExternal() {
		return (EAttribute)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiChipRegModel_DacExternalDecode() {
		return (EAttribute)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiChipRegModel_DacExternalName() {
		return (EAttribute)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMpxiiiChipRegModel_Anper() {
		return (EReference)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMpxiiiChipRegModel_Pixel() {
		return (EReference)mpxiiiChipRegModelEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMpxiiiGlobalRegModel() {
		return mpxiiiGlobalRegModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_ColourMode() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_ColourModeAsString() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_ColourModeLabels() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNumber() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNameCalc1() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNameCalc2() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNameCalc3() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNameSel1() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNameSel2() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacNameSel3() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_DacName() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_CounterDepthLabels() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_CounterDepth() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMpxiiiGlobalRegModel_CounterDepthAsString() {
		return (EAttribute)mpxiiiGlobalRegModelEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPixelModel() {
		return pixelModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPixelModel_Mask() {
		return (EAttribute)pixelModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPixelModel_Test() {
		return (EAttribute)pixelModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPixelModel_GainMode() {
		return (EAttribute)pixelModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPixelModel_ThresholdA() {
		return (EAttribute)pixelModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPixelModel_ThresholdB() {
		return (EAttribute)pixelModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReadoutNode() {
		return readoutNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNode_ReadoutNodeFem() {
		return (EReference)readoutNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReadoutNode_Id() {
		return (EAttribute)readoutNodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReadoutNodeFemModel() {
		return readoutNodeFemModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReadoutNodeFemModel_CounterDepth() {
		return (EAttribute)readoutNodeFemModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg1() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg2() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg3() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg4() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg5() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg6() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg7() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReadoutNodeFemModel_MpxiiiChipReg8() {
		return (EReference)readoutNodeFemModelEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSummaryAdbaseModel() {
		return summaryAdbaseModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSummaryAdbaseModel_FrameDivisor() {
		return (EAttribute)summaryAdbaseModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSummaryAdbaseModel_CounterDepth() {
		return (EAttribute)summaryAdbaseModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSummaryAdbaseModel_GapFillConstant() {
		return (EAttribute)summaryAdbaseModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSummaryNode() {
		return summaryNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSummaryNode_SummaryFem() {
		return (EReference)summaryNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFixModel() {
		return fixModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFixModel_StatisticsEnabled() {
		return (EAttribute)fixModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFixModel_ScaleEdgePixelsEnabled() {
		return (EAttribute)fixModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getException() {
		return exceptionEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getStringArray() {
		return stringArrayEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getShortArray() {
		return shortArrayEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExcaliburConfigFactory getExcaliburConfigFactory() {
		return (ExcaliburConfigFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		anperModelEClass = createEClass(ANPER_MODEL);
		createEAttribute(anperModelEClass, ANPER_MODEL__PREAMP);
		createEAttribute(anperModelEClass, ANPER_MODEL__IKRUM);
		createEAttribute(anperModelEClass, ANPER_MODEL__SHAPER);
		createEAttribute(anperModelEClass, ANPER_MODEL__DISC);
		createEAttribute(anperModelEClass, ANPER_MODEL__DISCLS);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLDN);
		createEAttribute(anperModelEClass, ANPER_MODEL__DAC_PIXEL);
		createEAttribute(anperModelEClass, ANPER_MODEL__DELAY);
		createEAttribute(anperModelEClass, ANPER_MODEL__TP_BUFFER_IN);
		createEAttribute(anperModelEClass, ANPER_MODEL__TP_BUFFER_OUT);
		createEAttribute(anperModelEClass, ANPER_MODEL__RPZ);
		createEAttribute(anperModelEClass, ANPER_MODEL__GND);
		createEAttribute(anperModelEClass, ANPER_MODEL__TPREF);
		createEAttribute(anperModelEClass, ANPER_MODEL__FBK);
		createEAttribute(anperModelEClass, ANPER_MODEL__CAS);
		createEAttribute(anperModelEClass, ANPER_MODEL__TPREF_A);
		createEAttribute(anperModelEClass, ANPER_MODEL__TPREF_B);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD0);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD1);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD2);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD3);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD4);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD5);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD6);
		createEAttribute(anperModelEClass, ANPER_MODEL__THRESHOLD7);

		arrayCountsModelEClass = createEClass(ARRAY_COUNTS_MODEL);
		createEAttribute(arrayCountsModelEClass, ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1);
		createEAttribute(arrayCountsModelEClass, ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2);
		createEAttribute(arrayCountsModelEClass, ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3);
		createEAttribute(arrayCountsModelEClass, ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4);
		createEAttribute(arrayCountsModelEClass, ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5);
		createEAttribute(arrayCountsModelEClass, ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6);

		baseNodeEClass = createEClass(BASE_NODE);
		createEReference(baseNodeEClass, BASE_NODE__GAP);
		createEReference(baseNodeEClass, BASE_NODE__MST);
		createEReference(baseNodeEClass, BASE_NODE__FIX);

		excaliburConfigEClass = createEClass(EXCALIBUR_CONFIG);
		createEReference(excaliburConfigEClass, EXCALIBUR_CONFIG__READOUT_NODES);
		createEReference(excaliburConfigEClass, EXCALIBUR_CONFIG__CONFIG_NODE);
		createEReference(excaliburConfigEClass, EXCALIBUR_CONFIG__SUMMARY_NODE);

		gapModelEClass = createEClass(GAP_MODEL);
		createEAttribute(gapModelEClass, GAP_MODEL__GAP_FILL_CONSTANT);
		createEAttribute(gapModelEClass, GAP_MODEL__GAP_FILLING_ENABLED);
		createEAttribute(gapModelEClass, GAP_MODEL__GAP_FILL_MODE);

		masterConfigAdbaseModelEClass = createEClass(MASTER_CONFIG_ADBASE_MODEL);
		createEAttribute(masterConfigAdbaseModelEClass, MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH);

		masterConfigNodeEClass = createEClass(MASTER_CONFIG_NODE);
		createEReference(masterConfigNodeEClass, MASTER_CONFIG_NODE__CONFIG_FEM);

		masterModelEClass = createEClass(MASTER_MODEL);
		createEAttribute(masterModelEClass, MASTER_MODEL__FRAME_DIVISOR);

		mpxiiiChipRegModelEClass = createEClass(MPXIII_CHIP_REG_MODEL);
		createEAttribute(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__DAC_SENSE);
		createEAttribute(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE);
		createEAttribute(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME);
		createEAttribute(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL);
		createEAttribute(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE);
		createEAttribute(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME);
		createEReference(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__ANPER);
		createEReference(mpxiiiChipRegModelEClass, MPXIII_CHIP_REG_MODEL__PIXEL);

		mpxiiiGlobalRegModelEClass = createEClass(MPXIII_GLOBAL_REG_MODEL);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__DAC_NAME);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH);
		createEAttribute(mpxiiiGlobalRegModelEClass, MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING);

		pixelModelEClass = createEClass(PIXEL_MODEL);
		createEAttribute(pixelModelEClass, PIXEL_MODEL__MASK);
		createEAttribute(pixelModelEClass, PIXEL_MODEL__TEST);
		createEAttribute(pixelModelEClass, PIXEL_MODEL__GAIN_MODE);
		createEAttribute(pixelModelEClass, PIXEL_MODEL__THRESHOLD_A);
		createEAttribute(pixelModelEClass, PIXEL_MODEL__THRESHOLD_B);

		readoutNodeEClass = createEClass(READOUT_NODE);
		createEReference(readoutNodeEClass, READOUT_NODE__READOUT_NODE_FEM);
		createEAttribute(readoutNodeEClass, READOUT_NODE__ID);

		readoutNodeFemModelEClass = createEClass(READOUT_NODE_FEM_MODEL);
		createEAttribute(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__COUNTER_DEPTH);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7);
		createEReference(readoutNodeFemModelEClass, READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8);

		summaryAdbaseModelEClass = createEClass(SUMMARY_ADBASE_MODEL);
		createEAttribute(summaryAdbaseModelEClass, SUMMARY_ADBASE_MODEL__FRAME_DIVISOR);
		createEAttribute(summaryAdbaseModelEClass, SUMMARY_ADBASE_MODEL__COUNTER_DEPTH);
		createEAttribute(summaryAdbaseModelEClass, SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT);

		summaryNodeEClass = createEClass(SUMMARY_NODE);
		createEReference(summaryNodeEClass, SUMMARY_NODE__SUMMARY_FEM);

		fixModelEClass = createEClass(FIX_MODEL);
		createEAttribute(fixModelEClass, FIX_MODEL__STATISTICS_ENABLED);
		createEAttribute(fixModelEClass, FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED);

		// Create data types
		exceptionEDataType = createEDataType(EXCEPTION);
		stringArrayEDataType = createEDataType(STRING_ARRAY);
		shortArrayEDataType = createEDataType(SHORT_ARRAY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		masterConfigNodeEClass.getESuperTypes().add(this.getBaseNode());
		readoutNodeEClass.getESuperTypes().add(this.getBaseNode());

		// Initialize classes and features; add operations and parameters
		initEClass(anperModelEClass, AnperModel.class, "AnperModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAnperModel_Preamp(), ecorePackage.getEInt(), "preamp", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Ikrum(), ecorePackage.getEInt(), "ikrum", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Shaper(), ecorePackage.getEInt(), "shaper", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Disc(), ecorePackage.getEInt(), "disc", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Discls(), ecorePackage.getEInt(), "discls", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Thresholdn(), ecorePackage.getEInt(), "thresholdn", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_DacPixel(), ecorePackage.getEInt(), "dacPixel", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Delay(), ecorePackage.getEInt(), "delay", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_TpBufferIn(), ecorePackage.getEInt(), "tpBufferIn", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_TpBufferOut(), ecorePackage.getEInt(), "tpBufferOut", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Rpz(), ecorePackage.getEInt(), "rpz", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Gnd(), ecorePackage.getEInt(), "gnd", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Tpref(), ecorePackage.getEInt(), "tpref", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Fbk(), ecorePackage.getEInt(), "fbk", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Cas(), ecorePackage.getEInt(), "cas", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_TprefA(), ecorePackage.getEInt(), "tprefA", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_TprefB(), ecorePackage.getEInt(), "tprefB", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold0(), ecorePackage.getEInt(), "threshold0", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold1(), ecorePackage.getEInt(), "threshold1", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold2(), ecorePackage.getEInt(), "threshold2", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold3(), ecorePackage.getEInt(), "threshold3", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold4(), ecorePackage.getEInt(), "threshold4", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold5(), ecorePackage.getEInt(), "threshold5", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold6(), ecorePackage.getEInt(), "threshold6", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnperModel_Threshold7(), ecorePackage.getEInt(), "threshold7", null, 0, 1, AnperModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(arrayCountsModelEClass, ArrayCountsModel.class, "ArrayCountsModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getArrayCountsModel_ArrayCountFem1(), ecorePackage.getEInt(), "arrayCountFem1", null, 0, 1, ArrayCountsModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArrayCountsModel_ArrayCountFem2(), ecorePackage.getEInt(), "arrayCountFem2", null, 0, 1, ArrayCountsModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArrayCountsModel_ArrayCountFem3(), ecorePackage.getEInt(), "arrayCountFem3", null, 0, 1, ArrayCountsModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArrayCountsModel_ArrayCountFem4(), ecorePackage.getEInt(), "arrayCountFem4", null, 0, 1, ArrayCountsModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArrayCountsModel_ArrayCountFem5(), ecorePackage.getEInt(), "arrayCountFem5", null, 0, 1, ArrayCountsModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArrayCountsModel_ArrayCountFem6(), ecorePackage.getEInt(), "arrayCountFem6", null, 0, 1, ArrayCountsModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(baseNodeEClass, BaseNode.class, "BaseNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBaseNode_Gap(), this.getGapModel(), null, "gap", null, 0, 1, BaseNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBaseNode_Mst(), this.getMasterModel(), null, "mst", null, 0, 1, BaseNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBaseNode_Fix(), this.getFixModel(), null, "fix", null, 0, 1, BaseNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(excaliburConfigEClass, ExcaliburConfig.class, "ExcaliburConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getExcaliburConfig_ReadoutNodes(), this.getReadoutNode(), null, "readoutNodes", null, 0, 6, ExcaliburConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getExcaliburConfig_ConfigNode(), this.getMasterConfigNode(), null, "configNode", null, 0, 1, ExcaliburConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getExcaliburConfig_SummaryNode(), this.getSummaryNode(), null, "summaryNode", null, 0, 1, ExcaliburConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(gapModelEClass, GapModel.class, "GapModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGapModel_GapFillConstant(), ecorePackage.getEInt(), "gapFillConstant", null, 0, 1, GapModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGapModel_GapFillingEnabled(), ecorePackage.getEBoolean(), "gapFillingEnabled", null, 0, 1, GapModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGapModel_GapFillMode(), ecorePackage.getEInt(), "gapFillMode", null, 0, 1, GapModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(masterConfigAdbaseModelEClass, MasterConfigAdbaseModel.class, "MasterConfigAdbaseModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMasterConfigAdbaseModel_CounterDepth(), ecorePackage.getEInt(), "counterDepth", null, 0, 1, MasterConfigAdbaseModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(masterConfigNodeEClass, MasterConfigNode.class, "MasterConfigNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMasterConfigNode_ConfigFem(), this.getMasterConfigAdbaseModel(), null, "configFem", null, 0, 1, MasterConfigNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(masterModelEClass, MasterModel.class, "MasterModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMasterModel_FrameDivisor(), ecorePackage.getEInt(), "frameDivisor", null, 0, 1, MasterModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mpxiiiChipRegModelEClass, MpxiiiChipRegModel.class, "MpxiiiChipRegModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMpxiiiChipRegModel_DacSense(), ecorePackage.getEInt(), "dacSense", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiChipRegModel_DacSenseDecode(), ecorePackage.getEInt(), "dacSenseDecode", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiChipRegModel_DacSenseName(), ecorePackage.getEString(), "dacSenseName", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiChipRegModel_DacExternal(), ecorePackage.getEInt(), "dacExternal", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiChipRegModel_DacExternalDecode(), ecorePackage.getEInt(), "dacExternalDecode", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiChipRegModel_DacExternalName(), ecorePackage.getEString(), "dacExternalName", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getMpxiiiChipRegModel_Anper(), this.getAnperModel(), null, "anper", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getMpxiiiChipRegModel_Pixel(), this.getPixelModel(), null, "pixel", null, 0, 1, MpxiiiChipRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mpxiiiGlobalRegModelEClass, MpxiiiGlobalRegModel.class, "MpxiiiGlobalRegModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMpxiiiGlobalRegModel_ColourMode(), ecorePackage.getEInt(), "colourMode", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_ColourModeAsString(), ecorePackage.getEString(), "colourModeAsString", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_ColourModeLabels(), this.getStringArray(), "colourModeLabels", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNumber(), ecorePackage.getEDouble(), "dacNumber", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNameCalc1(), ecorePackage.getEDouble(), "dacNameCalc1", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNameCalc2(), ecorePackage.getEDouble(), "dacNameCalc2", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNameCalc3(), ecorePackage.getEDouble(), "dacNameCalc3", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNameSel1(), ecorePackage.getEInt(), "dacNameSel1", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNameSel2(), ecorePackage.getEInt(), "dacNameSel2", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacNameSel3(), ecorePackage.getEInt(), "dacNameSel3", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_DacName(), ecorePackage.getEString(), "dacName", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_CounterDepthLabels(), this.getStringArray(), "counterDepthLabels", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_CounterDepth(), ecorePackage.getEInt(), "counterDepth", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMpxiiiGlobalRegModel_CounterDepthAsString(), ecorePackage.getEString(), "counterDepthAsString", null, 0, 1, MpxiiiGlobalRegModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(pixelModelEClass, PixelModel.class, "PixelModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPixelModel_Mask(), this.getShortArray(), "mask", null, 0, 1, PixelModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPixelModel_Test(), this.getShortArray(), "test", null, 0, 1, PixelModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPixelModel_GainMode(), this.getShortArray(), "gainMode", null, 0, 1, PixelModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPixelModel_ThresholdA(), this.getShortArray(), "thresholdA", null, 0, 1, PixelModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPixelModel_ThresholdB(), this.getShortArray(), "thresholdB", null, 0, 1, PixelModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(readoutNodeEClass, ReadoutNode.class, "ReadoutNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getReadoutNode_ReadoutNodeFem(), this.getReadoutNodeFemModel(), null, "readoutNodeFem", null, 0, 1, ReadoutNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReadoutNode_Id(), ecorePackage.getEInt(), "id", null, 0, 1, ReadoutNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(readoutNodeFemModelEClass, ReadoutNodeFemModel.class, "ReadoutNodeFemModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReadoutNodeFemModel_CounterDepth(), ecorePackage.getEInt(), "counterDepth", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg1(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg1", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg2(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg2", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg3(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg3", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg4(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg4", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg5(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg5", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg6(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg6", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg7(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg7", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReadoutNodeFemModel_MpxiiiChipReg8(), this.getMpxiiiChipRegModel(), null, "mpxiiiChipReg8", null, 0, 1, ReadoutNodeFemModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(summaryAdbaseModelEClass, SummaryAdbaseModel.class, "SummaryAdbaseModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSummaryAdbaseModel_FrameDivisor(), ecorePackage.getEInt(), "frameDivisor", null, 0, 1, SummaryAdbaseModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSummaryAdbaseModel_CounterDepth(), ecorePackage.getEInt(), "counterDepth", null, 0, 1, SummaryAdbaseModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSummaryAdbaseModel_GapFillConstant(), ecorePackage.getEInt(), "gapFillConstant", null, 0, 1, SummaryAdbaseModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(summaryNodeEClass, SummaryNode.class, "SummaryNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSummaryNode_SummaryFem(), this.getSummaryAdbaseModel(), null, "summaryFem", null, 0, 1, SummaryNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fixModelEClass, FixModel.class, "FixModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFixModel_StatisticsEnabled(), ecorePackage.getEBoolean(), "statisticsEnabled", null, 0, 1, FixModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFixModel_ScaleEdgePixelsEnabled(), ecorePackage.getEBoolean(), "scaleEdgePixelsEnabled", null, 0, 1, FixModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize data types
		initEDataType(exceptionEDataType, Exception.class, "Exception", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(stringArrayEDataType, String[].class, "StringArray", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(shortArrayEDataType, short[].class, "ShortArray", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //ExcaliburConfigPackageImpl
