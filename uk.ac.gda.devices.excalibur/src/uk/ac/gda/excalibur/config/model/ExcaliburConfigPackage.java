/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfigFactory
 * @model kind="package"
 * @generated
 */
public interface ExcaliburConfigPackage extends EPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///uk/ac/gda/excalibur/config/model.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "uk.ac.gda.excalibur.config.model";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ExcaliburConfigPackage eINSTANCE = uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl <em>Anper Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.AnperModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getAnperModel()
	 * @generated
	 */
	int ANPER_MODEL = 0;

	/**
	 * The feature id for the '<em><b>Preamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__PREAMP = 0;

	/**
	 * The feature id for the '<em><b>Ikrum</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__IKRUM = 1;

	/**
	 * The feature id for the '<em><b>Shaper</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__SHAPER = 2;

	/**
	 * The feature id for the '<em><b>Disc</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__DISC = 3;

	/**
	 * The feature id for the '<em><b>Discls</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__DISCLS = 4;

	/**
	 * The feature id for the '<em><b>Thresholdn</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLDN = 5;

	/**
	 * The feature id for the '<em><b>Dac Pixel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__DAC_PIXEL = 6;

	/**
	 * The feature id for the '<em><b>Delay</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__DELAY = 7;

	/**
	 * The feature id for the '<em><b>Tp Buffer In</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__TP_BUFFER_IN = 8;

	/**
	 * The feature id for the '<em><b>Tp Buffer Out</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__TP_BUFFER_OUT = 9;

	/**
	 * The feature id for the '<em><b>Rpz</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__RPZ = 10;

	/**
	 * The feature id for the '<em><b>Gnd</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__GND = 11;

	/**
	 * The feature id for the '<em><b>Tpref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__TPREF = 12;

	/**
	 * The feature id for the '<em><b>Fbk</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__FBK = 13;

	/**
	 * The feature id for the '<em><b>Cas</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__CAS = 14;

	/**
	 * The feature id for the '<em><b>Tpref A</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__TPREF_A = 15;

	/**
	 * The feature id for the '<em><b>Tpref B</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__TPREF_B = 16;

	/**
	 * The feature id for the '<em><b>Threshold0</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD0 = 17;

	/**
	 * The feature id for the '<em><b>Threshold1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD1 = 18;

	/**
	 * The feature id for the '<em><b>Threshold2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD2 = 19;

	/**
	 * The feature id for the '<em><b>Threshold3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD3 = 20;

	/**
	 * The feature id for the '<em><b>Threshold4</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD4 = 21;

	/**
	 * The feature id for the '<em><b>Threshold5</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD5 = 22;

	/**
	 * The feature id for the '<em><b>Threshold6</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD6 = 23;

	/**
	 * The feature id for the '<em><b>Threshold7</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL__THRESHOLD7 = 24;

	/**
	 * The number of structural features of the '<em>Anper Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANPER_MODEL_FEATURE_COUNT = 25;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl <em>Array Counts Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getArrayCountsModel()
	 * @generated
	 */
	int ARRAY_COUNTS_MODEL = 1;

	/**
	 * The feature id for the '<em><b>Array Count Fem1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1 = 0;

	/**
	 * The feature id for the '<em><b>Array Count Fem2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2 = 1;

	/**
	 * The feature id for the '<em><b>Array Count Fem3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3 = 2;

	/**
	 * The feature id for the '<em><b>Array Count Fem4</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4 = 3;

	/**
	 * The feature id for the '<em><b>Array Count Fem5</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5 = 4;

	/**
	 * The feature id for the '<em><b>Array Count Fem6</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6 = 5;

	/**
	 * The number of structural features of the '<em>Array Counts Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARRAY_COUNTS_MODEL_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl <em>Base Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getBaseNode()
	 * @generated
	 */
	int BASE_NODE = 2;

	/**
	 * The feature id for the '<em><b>Gap</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BASE_NODE__GAP = 0;

	/**
	 * The feature id for the '<em><b>Mst</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BASE_NODE__MST = 1;

	/**
	 * The feature id for the '<em><b>Fix</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BASE_NODE__FIX = 2;

	/**
	 * The number of structural features of the '<em>Base Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BASE_NODE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl <em>Excalibur Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getExcaliburConfig()
	 * @generated
	 */
	int EXCALIBUR_CONFIG = 3;

	/**
	 * The feature id for the '<em><b>Readout Nodes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXCALIBUR_CONFIG__READOUT_NODES = 0;

	/**
	 * The feature id for the '<em><b>Config Node</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXCALIBUR_CONFIG__CONFIG_NODE = 1;

	/**
	 * The feature id for the '<em><b>Summary Node</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXCALIBUR_CONFIG__SUMMARY_NODE = 2;

	/**
	 * The number of structural features of the '<em>Excalibur Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXCALIBUR_CONFIG_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.GapModelImpl <em>Gap Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.GapModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getGapModel()
	 * @generated
	 */
	int GAP_MODEL = 4;

	/**
	 * The feature id for the '<em><b>Gap Fill Constant</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_MODEL__GAP_FILL_CONSTANT = 0;

	/**
	 * The feature id for the '<em><b>Gap Filling Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_MODEL__GAP_FILLING_ENABLED = 1;

	/**
	 * The feature id for the '<em><b>Gap Fill Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_MODEL__GAP_FILL_MODE = 2;

	/**
	 * The number of structural features of the '<em>Gap Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GAP_MODEL_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.MasterConfigAdbaseModelImpl <em>Master Config Adbase Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.MasterConfigAdbaseModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMasterConfigAdbaseModel()
	 * @generated
	 */
	int MASTER_CONFIG_ADBASE_MODEL = 5;

	/**
	 * The feature id for the '<em><b>Counter Depth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH = 0;

	/**
	 * The number of structural features of the '<em>Master Config Adbase Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_ADBASE_MODEL_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.MasterConfigNodeImpl <em>Master Config Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.MasterConfigNodeImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMasterConfigNode()
	 * @generated
	 */
	int MASTER_CONFIG_NODE = 6;

	/**
	 * The feature id for the '<em><b>Gap</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_NODE__GAP = BASE_NODE__GAP;

	/**
	 * The feature id for the '<em><b>Mst</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_NODE__MST = BASE_NODE__MST;

	/**
	 * The feature id for the '<em><b>Fix</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_NODE__FIX = BASE_NODE__FIX;

	/**
	 * The feature id for the '<em><b>Config Fem</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_NODE__CONFIG_FEM = BASE_NODE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Master Config Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_CONFIG_NODE_FEATURE_COUNT = BASE_NODE_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.MasterModelImpl <em>Master Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.MasterModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMasterModel()
	 * @generated
	 */
	int MASTER_MODEL = 7;

	/**
	 * The feature id for the '<em><b>Frame Divisor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_MODEL__FRAME_DIVISOR = 0;

	/**
	 * The number of structural features of the '<em>Master Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MASTER_MODEL_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl <em>Mpxiii Chip Reg Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMpxiiiChipRegModel()
	 * @generated
	 */
	int MPXIII_CHIP_REG_MODEL = 8;

	/**
	 * The feature id for the '<em><b>Dac Sense</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__DAC_SENSE = 0;

	/**
	 * The feature id for the '<em><b>Dac Sense Decode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE = 1;

	/**
	 * The feature id for the '<em><b>Dac Sense Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME = 2;

	/**
	 * The feature id for the '<em><b>Dac External</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL = 3;

	/**
	 * The feature id for the '<em><b>Dac External Decode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE = 4;

	/**
	 * The feature id for the '<em><b>Dac External Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME = 5;

	/**
	 * The feature id for the '<em><b>Anper</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__ANPER = 6;

	/**
	 * The feature id for the '<em><b>Pixel</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL__PIXEL = 7;

	/**
	 * The number of structural features of the '<em>Mpxiii Chip Reg Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_CHIP_REG_MODEL_FEATURE_COUNT = 8;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl <em>Mpxiii Global Reg Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMpxiiiGlobalRegModel()
	 * @generated
	 */
	int MPXIII_GLOBAL_REG_MODEL = 9;

	/**
	 * The feature id for the '<em><b>Colour Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE = 0;

	/**
	 * The feature id for the '<em><b>Colour Mode As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING = 1;

	/**
	 * The feature id for the '<em><b>Colour Mode Labels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS = 2;

	/**
	 * The feature id for the '<em><b>Dac Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER = 3;

	/**
	 * The feature id for the '<em><b>Dac Name Calc1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1 = 4;

	/**
	 * The feature id for the '<em><b>Dac Name Calc2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2 = 5;

	/**
	 * The feature id for the '<em><b>Dac Name Calc3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3 = 6;

	/**
	 * The feature id for the '<em><b>Dac Name Sel1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1 = 7;

	/**
	 * The feature id for the '<em><b>Dac Name Sel2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2 = 8;

	/**
	 * The feature id for the '<em><b>Dac Name Sel3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3 = 9;

	/**
	 * The feature id for the '<em><b>Dac Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__DAC_NAME = 10;

	/**
	 * The feature id for the '<em><b>Counter Depth Labels</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS = 11;

	/**
	 * The feature id for the '<em><b>Counter Depth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH = 12;

	/**
	 * The feature id for the '<em><b>Counter Depth As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING = 13;

	/**
	 * The number of structural features of the '<em>Mpxiii Global Reg Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MPXIII_GLOBAL_REG_MODEL_FEATURE_COUNT = 14;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl <em>Pixel Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.PixelModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getPixelModel()
	 * @generated
	 */
	int PIXEL_MODEL = 10;

	/**
	 * The feature id for the '<em><b>Mask</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIXEL_MODEL__MASK = 0;

	/**
	 * The feature id for the '<em><b>Test</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIXEL_MODEL__TEST = 1;

	/**
	 * The feature id for the '<em><b>Gain Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIXEL_MODEL__GAIN_MODE = 2;

	/**
	 * The feature id for the '<em><b>Threshold A</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIXEL_MODEL__THRESHOLD_A = 3;

	/**
	 * The feature id for the '<em><b>Threshold B</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIXEL_MODEL__THRESHOLD_B = 4;

	/**
	 * The number of structural features of the '<em>Pixel Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIXEL_MODEL_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeImpl <em>Readout Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.ReadoutNodeImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getReadoutNode()
	 * @generated
	 */
	int READOUT_NODE = 11;

	/**
	 * The feature id for the '<em><b>Gap</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE__GAP = BASE_NODE__GAP;

	/**
	 * The feature id for the '<em><b>Mst</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE__MST = BASE_NODE__MST;

	/**
	 * The feature id for the '<em><b>Fix</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE__FIX = BASE_NODE__FIX;

	/**
	 * The feature id for the '<em><b>Readout Node Fem</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE__READOUT_NODE_FEM = BASE_NODE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE__ID = BASE_NODE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Readout Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEATURE_COUNT = BASE_NODE_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl <em>Readout Node Fem Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getReadoutNodeFemModel()
	 * @generated
	 */
	int READOUT_NODE_FEM_MODEL = 12;

	/**
	 * The feature id for the '<em><b>Counter Depth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__COUNTER_DEPTH = 0;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg1</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1 = 1;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg2</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2 = 2;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg3</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3 = 3;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg4</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4 = 4;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg5</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5 = 5;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg6</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6 = 6;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg7</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7 = 7;

	/**
	 * The feature id for the '<em><b>Mpxiii Chip Reg8</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8 = 8;

	/**
	 * The number of structural features of the '<em>Readout Node Fem Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READOUT_NODE_FEM_MODEL_FEATURE_COUNT = 9;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl <em>Summary Adbase Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getSummaryAdbaseModel()
	 * @generated
	 */
	int SUMMARY_ADBASE_MODEL = 13;

	/**
	 * The feature id for the '<em><b>Frame Divisor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_ADBASE_MODEL__FRAME_DIVISOR = 0;

	/**
	 * The feature id for the '<em><b>Counter Depth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_ADBASE_MODEL__COUNTER_DEPTH = 1;

	/**
	 * The feature id for the '<em><b>Gap Fill Constant</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT = 2;

	/**
	 * The number of structural features of the '<em>Summary Adbase Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_ADBASE_MODEL_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.SummaryNodeImpl <em>Summary Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.SummaryNodeImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getSummaryNode()
	 * @generated
	 */
	int SUMMARY_NODE = 14;

	/**
	 * The feature id for the '<em><b>Summary Fem</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_NODE__SUMMARY_FEM = 0;

	/**
	 * The number of structural features of the '<em>Summary Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_NODE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.gda.excalibur.config.model.impl.FixModelImpl <em>Fix Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.FixModelImpl
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getFixModel()
	 * @generated
	 */
	int FIX_MODEL = 15;

	/**
	 * The feature id for the '<em><b>Statistics Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIX_MODEL__STATISTICS_ENABLED = 0;

	/**
	 * The feature id for the '<em><b>Scale Edge Pixels Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED = 1;

	/**
	 * The number of structural features of the '<em>Fix Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIX_MODEL_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '<em>Exception</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.lang.Exception
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getException()
	 * @generated
	 */
	int EXCEPTION = 16;

	/**
	 * The meta object id for the '<em>String Array</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getStringArray()
	 * @generated
	 */
	int STRING_ARRAY = 17;

	/**
	 * The meta object id for the '<em>Short Array</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getShortArray()
	 * @generated
	 */
	int SHORT_ARRAY = 18;


	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.AnperModel <em>Anper Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Anper Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel
	 * @generated
	 */
	EClass getAnperModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getPreamp <em>Preamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Preamp</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getPreamp()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Preamp();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getIkrum <em>Ikrum</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ikrum</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getIkrum()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Ikrum();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getShaper <em>Shaper</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shaper</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getShaper()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Shaper();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getDisc <em>Disc</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Disc</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getDisc()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Disc();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getDiscls <em>Discls</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Discls</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getDiscls()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Discls();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThresholdn <em>Thresholdn</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Thresholdn</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThresholdn()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Thresholdn();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getDacPixel <em>Dac Pixel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Pixel</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getDacPixel()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_DacPixel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getDelay <em>Delay</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Delay</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getDelay()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Delay();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getTpBufferIn <em>Tp Buffer In</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tp Buffer In</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getTpBufferIn()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_TpBufferIn();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getTpBufferOut <em>Tp Buffer Out</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tp Buffer Out</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getTpBufferOut()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_TpBufferOut();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getRpz <em>Rpz</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Rpz</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getRpz()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Rpz();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getGnd <em>Gnd</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gnd</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getGnd()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Gnd();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getTpref <em>Tpref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tpref</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getTpref()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Tpref();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getFbk <em>Fbk</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fbk</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getFbk()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Fbk();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getCas <em>Cas</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Cas</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getCas()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Cas();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getTprefA <em>Tpref A</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tpref A</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getTprefA()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_TprefA();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getTprefB <em>Tpref B</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tpref B</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getTprefB()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_TprefB();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold0 <em>Threshold0</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold0</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold0()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold0();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold1 <em>Threshold1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold1</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold1()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold2 <em>Threshold2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold2</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold2()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold2();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold3 <em>Threshold3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold3</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold3()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold3();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold4 <em>Threshold4</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold4</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold4()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold4();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold5 <em>Threshold5</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold5</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold5()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold5();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold6 <em>Threshold6</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold6</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold6()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold6();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.AnperModel#getThreshold7 <em>Threshold7</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold7</em>'.
	 * @see uk.ac.gda.excalibur.config.model.AnperModel#getThreshold7()
	 * @see #getAnperModel()
	 * @generated
	 */
	EAttribute getAnperModel_Threshold7();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel <em>Array Counts Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Array Counts Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel
	 * @generated
	 */
	EClass getArrayCountsModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem1 <em>Array Count Fem1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Array Count Fem1</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem1()
	 * @see #getArrayCountsModel()
	 * @generated
	 */
	EAttribute getArrayCountsModel_ArrayCountFem1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem2 <em>Array Count Fem2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Array Count Fem2</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem2()
	 * @see #getArrayCountsModel()
	 * @generated
	 */
	EAttribute getArrayCountsModel_ArrayCountFem2();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem3 <em>Array Count Fem3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Array Count Fem3</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem3()
	 * @see #getArrayCountsModel()
	 * @generated
	 */
	EAttribute getArrayCountsModel_ArrayCountFem3();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem4 <em>Array Count Fem4</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Array Count Fem4</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem4()
	 * @see #getArrayCountsModel()
	 * @generated
	 */
	EAttribute getArrayCountsModel_ArrayCountFem4();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem5 <em>Array Count Fem5</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Array Count Fem5</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem5()
	 * @see #getArrayCountsModel()
	 * @generated
	 */
	EAttribute getArrayCountsModel_ArrayCountFem5();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem6 <em>Array Count Fem6</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Array Count Fem6</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ArrayCountsModel#getArrayCountFem6()
	 * @see #getArrayCountsModel()
	 * @generated
	 */
	EAttribute getArrayCountsModel_ArrayCountFem6();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.BaseNode <em>Base Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Base Node</em>'.
	 * @see uk.ac.gda.excalibur.config.model.BaseNode
	 * @generated
	 */
	EClass getBaseNode();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.BaseNode#getGap <em>Gap</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Gap</em>'.
	 * @see uk.ac.gda.excalibur.config.model.BaseNode#getGap()
	 * @see #getBaseNode()
	 * @generated
	 */
	EReference getBaseNode_Gap();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.BaseNode#getMst <em>Mst</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mst</em>'.
	 * @see uk.ac.gda.excalibur.config.model.BaseNode#getMst()
	 * @see #getBaseNode()
	 * @generated
	 */
	EReference getBaseNode_Mst();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.BaseNode#getFix <em>Fix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Fix</em>'.
	 * @see uk.ac.gda.excalibur.config.model.BaseNode#getFix()
	 * @see #getBaseNode()
	 * @generated
	 */
	EReference getBaseNode_Fix();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.ExcaliburConfig <em>Excalibur Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Excalibur Config</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfig
	 * @generated
	 */
	EClass getExcaliburConfig();

	/**
	 * Returns the meta object for the containment reference list '{@link uk.ac.gda.excalibur.config.model.ExcaliburConfig#getReadoutNodes <em>Readout Nodes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Readout Nodes</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfig#getReadoutNodes()
	 * @see #getExcaliburConfig()
	 * @generated
	 */
	EReference getExcaliburConfig_ReadoutNodes();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ExcaliburConfig#getConfigNode <em>Config Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Config Node</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfig#getConfigNode()
	 * @see #getExcaliburConfig()
	 * @generated
	 */
	EReference getExcaliburConfig_ConfigNode();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ExcaliburConfig#getSummaryNode <em>Summary Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Summary Node</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ExcaliburConfig#getSummaryNode()
	 * @see #getExcaliburConfig()
	 * @generated
	 */
	EReference getExcaliburConfig_SummaryNode();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.GapModel <em>Gap Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Gap Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.GapModel
	 * @generated
	 */
	EClass getGapModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.GapModel#getGapFillConstant <em>Gap Fill Constant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gap Fill Constant</em>'.
	 * @see uk.ac.gda.excalibur.config.model.GapModel#getGapFillConstant()
	 * @see #getGapModel()
	 * @generated
	 */
	EAttribute getGapModel_GapFillConstant();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.GapModel#isGapFillingEnabled <em>Gap Filling Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gap Filling Enabled</em>'.
	 * @see uk.ac.gda.excalibur.config.model.GapModel#isGapFillingEnabled()
	 * @see #getGapModel()
	 * @generated
	 */
	EAttribute getGapModel_GapFillingEnabled();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.GapModel#getGapFillMode <em>Gap Fill Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gap Fill Mode</em>'.
	 * @see uk.ac.gda.excalibur.config.model.GapModel#getGapFillMode()
	 * @see #getGapModel()
	 * @generated
	 */
	EAttribute getGapModel_GapFillMode();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel <em>Master Config Adbase Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Master Config Adbase Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel
	 * @generated
	 */
	EClass getMasterConfigAdbaseModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel#getCounterDepth <em>Counter Depth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter Depth</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel#getCounterDepth()
	 * @see #getMasterConfigAdbaseModel()
	 * @generated
	 */
	EAttribute getMasterConfigAdbaseModel_CounterDepth();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.MasterConfigNode <em>Master Config Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Master Config Node</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MasterConfigNode
	 * @generated
	 */
	EClass getMasterConfigNode();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.MasterConfigNode#getConfigFem <em>Config Fem</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Config Fem</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MasterConfigNode#getConfigFem()
	 * @see #getMasterConfigNode()
	 * @generated
	 */
	EReference getMasterConfigNode_ConfigFem();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.MasterModel <em>Master Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Master Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MasterModel
	 * @generated
	 */
	EClass getMasterModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MasterModel#getFrameDivisor <em>Frame Divisor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Frame Divisor</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MasterModel#getFrameDivisor()
	 * @see #getMasterModel()
	 * @generated
	 */
	EAttribute getMasterModel_FrameDivisor();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel <em>Mpxiii Chip Reg Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Mpxiii Chip Reg Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel
	 * @generated
	 */
	EClass getMpxiiiChipRegModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacSense <em>Dac Sense</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Sense</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacSense()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiChipRegModel_DacSense();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacSenseDecode <em>Dac Sense Decode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Sense Decode</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacSenseDecode()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiChipRegModel_DacSenseDecode();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacSenseName <em>Dac Sense Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Sense Name</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacSenseName()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiChipRegModel_DacSenseName();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacExternal <em>Dac External</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac External</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacExternal()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiChipRegModel_DacExternal();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacExternalDecode <em>Dac External Decode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac External Decode</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacExternalDecode()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiChipRegModel_DacExternalDecode();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacExternalName <em>Dac External Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac External Name</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getDacExternalName()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiChipRegModel_DacExternalName();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getAnper <em>Anper</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Anper</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getAnper()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EReference getMpxiiiChipRegModel_Anper();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getPixel <em>Pixel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Pixel</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel#getPixel()
	 * @see #getMpxiiiChipRegModel()
	 * @generated
	 */
	EReference getMpxiiiChipRegModel_Pixel();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel <em>Mpxiii Global Reg Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Mpxiii Global Reg Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel
	 * @generated
	 */
	EClass getMpxiiiGlobalRegModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourMode <em>Colour Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Colour Mode</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourMode()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_ColourMode();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourModeAsString <em>Colour Mode As String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Colour Mode As String</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourModeAsString()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_ColourModeAsString();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourModeLabels <em>Colour Mode Labels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Colour Mode Labels</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getColourModeLabels()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_ColourModeLabels();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNumber <em>Dac Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Number</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNumber()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNumber();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameCalc1 <em>Dac Name Calc1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name Calc1</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameCalc1()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNameCalc1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameCalc2 <em>Dac Name Calc2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name Calc2</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameCalc2()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNameCalc2();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameCalc3 <em>Dac Name Calc3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name Calc3</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameCalc3()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNameCalc3();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameSel1 <em>Dac Name Sel1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name Sel1</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameSel1()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNameSel1();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameSel2 <em>Dac Name Sel2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name Sel2</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameSel2()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNameSel2();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameSel3 <em>Dac Name Sel3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name Sel3</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacNameSel3()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacNameSel3();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacName <em>Dac Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dac Name</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getDacName()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_DacName();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepthLabels <em>Counter Depth Labels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter Depth Labels</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepthLabels()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_CounterDepthLabels();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepth <em>Counter Depth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter Depth</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepth()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_CounterDepth();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepthAsString <em>Counter Depth As String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter Depth As String</em>'.
	 * @see uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel#getCounterDepthAsString()
	 * @see #getMpxiiiGlobalRegModel()
	 * @generated
	 */
	EAttribute getMpxiiiGlobalRegModel_CounterDepthAsString();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.PixelModel <em>Pixel Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Pixel Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel
	 * @generated
	 */
	EClass getPixelModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.PixelModel#getMask <em>Mask</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mask</em>'.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel#getMask()
	 * @see #getPixelModel()
	 * @generated
	 */
	EAttribute getPixelModel_Mask();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.PixelModel#getTest <em>Test</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Test</em>'.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel#getTest()
	 * @see #getPixelModel()
	 * @generated
	 */
	EAttribute getPixelModel_Test();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.PixelModel#getGainMode <em>Gain Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gain Mode</em>'.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel#getGainMode()
	 * @see #getPixelModel()
	 * @generated
	 */
	EAttribute getPixelModel_GainMode();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.PixelModel#getThresholdA <em>Threshold A</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold A</em>'.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel#getThresholdA()
	 * @see #getPixelModel()
	 * @generated
	 */
	EAttribute getPixelModel_ThresholdA();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.PixelModel#getThresholdB <em>Threshold B</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threshold B</em>'.
	 * @see uk.ac.gda.excalibur.config.model.PixelModel#getThresholdB()
	 * @see #getPixelModel()
	 * @generated
	 */
	EAttribute getPixelModel_ThresholdB();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.ReadoutNode <em>Readout Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Readout Node</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNode
	 * @generated
	 */
	EClass getReadoutNode();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNode#getReadoutNodeFem <em>Readout Node Fem</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Readout Node Fem</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNode#getReadoutNodeFem()
	 * @see #getReadoutNode()
	 * @generated
	 */
	EReference getReadoutNode_ReadoutNodeFem();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ReadoutNode#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNode#getId()
	 * @see #getReadoutNode()
	 * @generated
	 */
	EAttribute getReadoutNode_Id();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel <em>Readout Node Fem Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Readout Node Fem Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel
	 * @generated
	 */
	EClass getReadoutNodeFemModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getCounterDepth <em>Counter Depth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter Depth</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getCounterDepth()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EAttribute getReadoutNodeFemModel_CounterDepth();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg1 <em>Mpxiii Chip Reg1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg1</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg1()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg1();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg2 <em>Mpxiii Chip Reg2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg2</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg2()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg2();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg3 <em>Mpxiii Chip Reg3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg3</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg3()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg3();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg4 <em>Mpxiii Chip Reg4</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg4</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg4()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg4();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg5 <em>Mpxiii Chip Reg5</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg5</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg5()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg5();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg6 <em>Mpxiii Chip Reg6</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg6</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg6()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg6();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg7 <em>Mpxiii Chip Reg7</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg7</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg7()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg7();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg8 <em>Mpxiii Chip Reg8</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Mpxiii Chip Reg8</em>'.
	 * @see uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel#getMpxiiiChipReg8()
	 * @see #getReadoutNodeFemModel()
	 * @generated
	 */
	EReference getReadoutNodeFemModel_MpxiiiChipReg8();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.SummaryAdbaseModel <em>Summary Adbase Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Summary Adbase Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.SummaryAdbaseModel
	 * @generated
	 */
	EClass getSummaryAdbaseModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.SummaryAdbaseModel#getFrameDivisor <em>Frame Divisor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Frame Divisor</em>'.
	 * @see uk.ac.gda.excalibur.config.model.SummaryAdbaseModel#getFrameDivisor()
	 * @see #getSummaryAdbaseModel()
	 * @generated
	 */
	EAttribute getSummaryAdbaseModel_FrameDivisor();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.SummaryAdbaseModel#getCounterDepth <em>Counter Depth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter Depth</em>'.
	 * @see uk.ac.gda.excalibur.config.model.SummaryAdbaseModel#getCounterDepth()
	 * @see #getSummaryAdbaseModel()
	 * @generated
	 */
	EAttribute getSummaryAdbaseModel_CounterDepth();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.SummaryAdbaseModel#getGapFillConstant <em>Gap Fill Constant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gap Fill Constant</em>'.
	 * @see uk.ac.gda.excalibur.config.model.SummaryAdbaseModel#getGapFillConstant()
	 * @see #getSummaryAdbaseModel()
	 * @generated
	 */
	EAttribute getSummaryAdbaseModel_GapFillConstant();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.SummaryNode <em>Summary Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Summary Node</em>'.
	 * @see uk.ac.gda.excalibur.config.model.SummaryNode
	 * @generated
	 */
	EClass getSummaryNode();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.excalibur.config.model.SummaryNode#getSummaryFem <em>Summary Fem</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Summary Fem</em>'.
	 * @see uk.ac.gda.excalibur.config.model.SummaryNode#getSummaryFem()
	 * @see #getSummaryNode()
	 * @generated
	 */
	EReference getSummaryNode_SummaryFem();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.excalibur.config.model.FixModel <em>Fix Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Fix Model</em>'.
	 * @see uk.ac.gda.excalibur.config.model.FixModel
	 * @generated
	 */
	EClass getFixModel();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.FixModel#isStatisticsEnabled <em>Statistics Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Statistics Enabled</em>'.
	 * @see uk.ac.gda.excalibur.config.model.FixModel#isStatisticsEnabled()
	 * @see #getFixModel()
	 * @generated
	 */
	EAttribute getFixModel_StatisticsEnabled();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.excalibur.config.model.FixModel#isScaleEdgePixelsEnabled <em>Scale Edge Pixels Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scale Edge Pixels Enabled</em>'.
	 * @see uk.ac.gda.excalibur.config.model.FixModel#isScaleEdgePixelsEnabled()
	 * @see #getFixModel()
	 * @generated
	 */
	EAttribute getFixModel_ScaleEdgePixelsEnabled();

	/**
	 * Returns the meta object for data type '{@link java.lang.Exception <em>Exception</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Exception</em>'.
	 * @see java.lang.Exception
	 * @model instanceClass="java.lang.Exception"
	 * @generated
	 */
	EDataType getException();

	/**
	 * Returns the meta object for data type '<em>String Array</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>String Array</em>'.
	 * @model instanceClass="java.lang.String[]"
	 * @generated
	 */
	EDataType getStringArray();

	/**
	 * Returns the meta object for data type '<em>Short Array</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Short Array</em>'.
	 * @model instanceClass="short[]"
	 * @generated
	 */
	EDataType getShortArray();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ExcaliburConfigFactory getExcaliburConfigFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("hiding")
	interface Literals {
		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl <em>Anper Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.AnperModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getAnperModel()
		 * @generated
		 */
		EClass ANPER_MODEL = eINSTANCE.getAnperModel();

		/**
		 * The meta object literal for the '<em><b>Preamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__PREAMP = eINSTANCE.getAnperModel_Preamp();

		/**
		 * The meta object literal for the '<em><b>Ikrum</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__IKRUM = eINSTANCE.getAnperModel_Ikrum();

		/**
		 * The meta object literal for the '<em><b>Shaper</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__SHAPER = eINSTANCE.getAnperModel_Shaper();

		/**
		 * The meta object literal for the '<em><b>Disc</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__DISC = eINSTANCE.getAnperModel_Disc();

		/**
		 * The meta object literal for the '<em><b>Discls</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__DISCLS = eINSTANCE.getAnperModel_Discls();

		/**
		 * The meta object literal for the '<em><b>Thresholdn</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLDN = eINSTANCE.getAnperModel_Thresholdn();

		/**
		 * The meta object literal for the '<em><b>Dac Pixel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__DAC_PIXEL = eINSTANCE.getAnperModel_DacPixel();

		/**
		 * The meta object literal for the '<em><b>Delay</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__DELAY = eINSTANCE.getAnperModel_Delay();

		/**
		 * The meta object literal for the '<em><b>Tp Buffer In</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__TP_BUFFER_IN = eINSTANCE.getAnperModel_TpBufferIn();

		/**
		 * The meta object literal for the '<em><b>Tp Buffer Out</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__TP_BUFFER_OUT = eINSTANCE.getAnperModel_TpBufferOut();

		/**
		 * The meta object literal for the '<em><b>Rpz</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__RPZ = eINSTANCE.getAnperModel_Rpz();

		/**
		 * The meta object literal for the '<em><b>Gnd</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__GND = eINSTANCE.getAnperModel_Gnd();

		/**
		 * The meta object literal for the '<em><b>Tpref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__TPREF = eINSTANCE.getAnperModel_Tpref();

		/**
		 * The meta object literal for the '<em><b>Fbk</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__FBK = eINSTANCE.getAnperModel_Fbk();

		/**
		 * The meta object literal for the '<em><b>Cas</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__CAS = eINSTANCE.getAnperModel_Cas();

		/**
		 * The meta object literal for the '<em><b>Tpref A</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__TPREF_A = eINSTANCE.getAnperModel_TprefA();

		/**
		 * The meta object literal for the '<em><b>Tpref B</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__TPREF_B = eINSTANCE.getAnperModel_TprefB();

		/**
		 * The meta object literal for the '<em><b>Threshold0</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD0 = eINSTANCE.getAnperModel_Threshold0();

		/**
		 * The meta object literal for the '<em><b>Threshold1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD1 = eINSTANCE.getAnperModel_Threshold1();

		/**
		 * The meta object literal for the '<em><b>Threshold2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD2 = eINSTANCE.getAnperModel_Threshold2();

		/**
		 * The meta object literal for the '<em><b>Threshold3</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD3 = eINSTANCE.getAnperModel_Threshold3();

		/**
		 * The meta object literal for the '<em><b>Threshold4</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD4 = eINSTANCE.getAnperModel_Threshold4();

		/**
		 * The meta object literal for the '<em><b>Threshold5</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD5 = eINSTANCE.getAnperModel_Threshold5();

		/**
		 * The meta object literal for the '<em><b>Threshold6</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD6 = eINSTANCE.getAnperModel_Threshold6();

		/**
		 * The meta object literal for the '<em><b>Threshold7</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANPER_MODEL__THRESHOLD7 = eINSTANCE.getAnperModel_Threshold7();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl <em>Array Counts Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.ArrayCountsModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getArrayCountsModel()
		 * @generated
		 */
		EClass ARRAY_COUNTS_MODEL = eINSTANCE.getArrayCountsModel();

		/**
		 * The meta object literal for the '<em><b>Array Count Fem1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM1 = eINSTANCE.getArrayCountsModel_ArrayCountFem1();

		/**
		 * The meta object literal for the '<em><b>Array Count Fem2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM2 = eINSTANCE.getArrayCountsModel_ArrayCountFem2();

		/**
		 * The meta object literal for the '<em><b>Array Count Fem3</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM3 = eINSTANCE.getArrayCountsModel_ArrayCountFem3();

		/**
		 * The meta object literal for the '<em><b>Array Count Fem4</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM4 = eINSTANCE.getArrayCountsModel_ArrayCountFem4();

		/**
		 * The meta object literal for the '<em><b>Array Count Fem5</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM5 = eINSTANCE.getArrayCountsModel_ArrayCountFem5();

		/**
		 * The meta object literal for the '<em><b>Array Count Fem6</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARRAY_COUNTS_MODEL__ARRAY_COUNT_FEM6 = eINSTANCE.getArrayCountsModel_ArrayCountFem6();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl <em>Base Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getBaseNode()
		 * @generated
		 */
		EClass BASE_NODE = eINSTANCE.getBaseNode();

		/**
		 * The meta object literal for the '<em><b>Gap</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BASE_NODE__GAP = eINSTANCE.getBaseNode_Gap();

		/**
		 * The meta object literal for the '<em><b>Mst</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BASE_NODE__MST = eINSTANCE.getBaseNode_Mst();

		/**
		 * The meta object literal for the '<em><b>Fix</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BASE_NODE__FIX = eINSTANCE.getBaseNode_Fix();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl <em>Excalibur Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getExcaliburConfig()
		 * @generated
		 */
		EClass EXCALIBUR_CONFIG = eINSTANCE.getExcaliburConfig();

		/**
		 * The meta object literal for the '<em><b>Readout Nodes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXCALIBUR_CONFIG__READOUT_NODES = eINSTANCE.getExcaliburConfig_ReadoutNodes();

		/**
		 * The meta object literal for the '<em><b>Config Node</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXCALIBUR_CONFIG__CONFIG_NODE = eINSTANCE.getExcaliburConfig_ConfigNode();

		/**
		 * The meta object literal for the '<em><b>Summary Node</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXCALIBUR_CONFIG__SUMMARY_NODE = eINSTANCE.getExcaliburConfig_SummaryNode();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.GapModelImpl <em>Gap Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.GapModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getGapModel()
		 * @generated
		 */
		EClass GAP_MODEL = eINSTANCE.getGapModel();

		/**
		 * The meta object literal for the '<em><b>Gap Fill Constant</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAP_MODEL__GAP_FILL_CONSTANT = eINSTANCE.getGapModel_GapFillConstant();

		/**
		 * The meta object literal for the '<em><b>Gap Filling Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAP_MODEL__GAP_FILLING_ENABLED = eINSTANCE.getGapModel_GapFillingEnabled();

		/**
		 * The meta object literal for the '<em><b>Gap Fill Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GAP_MODEL__GAP_FILL_MODE = eINSTANCE.getGapModel_GapFillMode();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.MasterConfigAdbaseModelImpl <em>Master Config Adbase Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.MasterConfigAdbaseModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMasterConfigAdbaseModel()
		 * @generated
		 */
		EClass MASTER_CONFIG_ADBASE_MODEL = eINSTANCE.getMasterConfigAdbaseModel();

		/**
		 * The meta object literal for the '<em><b>Counter Depth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MASTER_CONFIG_ADBASE_MODEL__COUNTER_DEPTH = eINSTANCE.getMasterConfigAdbaseModel_CounterDepth();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.MasterConfigNodeImpl <em>Master Config Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.MasterConfigNodeImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMasterConfigNode()
		 * @generated
		 */
		EClass MASTER_CONFIG_NODE = eINSTANCE.getMasterConfigNode();

		/**
		 * The meta object literal for the '<em><b>Config Fem</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MASTER_CONFIG_NODE__CONFIG_FEM = eINSTANCE.getMasterConfigNode_ConfigFem();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.MasterModelImpl <em>Master Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.MasterModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMasterModel()
		 * @generated
		 */
		EClass MASTER_MODEL = eINSTANCE.getMasterModel();

		/**
		 * The meta object literal for the '<em><b>Frame Divisor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MASTER_MODEL__FRAME_DIVISOR = eINSTANCE.getMasterModel_FrameDivisor();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl <em>Mpxiii Chip Reg Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMpxiiiChipRegModel()
		 * @generated
		 */
		EClass MPXIII_CHIP_REG_MODEL = eINSTANCE.getMpxiiiChipRegModel();

		/**
		 * The meta object literal for the '<em><b>Dac Sense</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_CHIP_REG_MODEL__DAC_SENSE = eINSTANCE.getMpxiiiChipRegModel_DacSense();

		/**
		 * The meta object literal for the '<em><b>Dac Sense Decode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE = eINSTANCE.getMpxiiiChipRegModel_DacSenseDecode();

		/**
		 * The meta object literal for the '<em><b>Dac Sense Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME = eINSTANCE.getMpxiiiChipRegModel_DacSenseName();

		/**
		 * The meta object literal for the '<em><b>Dac External</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL = eINSTANCE.getMpxiiiChipRegModel_DacExternal();

		/**
		 * The meta object literal for the '<em><b>Dac External Decode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE = eINSTANCE.getMpxiiiChipRegModel_DacExternalDecode();

		/**
		 * The meta object literal for the '<em><b>Dac External Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME = eINSTANCE.getMpxiiiChipRegModel_DacExternalName();

		/**
		 * The meta object literal for the '<em><b>Anper</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPXIII_CHIP_REG_MODEL__ANPER = eINSTANCE.getMpxiiiChipRegModel_Anper();

		/**
		 * The meta object literal for the '<em><b>Pixel</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MPXIII_CHIP_REG_MODEL__PIXEL = eINSTANCE.getMpxiiiChipRegModel_Pixel();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl <em>Mpxiii Global Reg Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getMpxiiiGlobalRegModel()
		 * @generated
		 */
		EClass MPXIII_GLOBAL_REG_MODEL = eINSTANCE.getMpxiiiGlobalRegModel();

		/**
		 * The meta object literal for the '<em><b>Colour Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE = eINSTANCE.getMpxiiiGlobalRegModel_ColourMode();

		/**
		 * The meta object literal for the '<em><b>Colour Mode As String</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING = eINSTANCE.getMpxiiiGlobalRegModel_ColourModeAsString();

		/**
		 * The meta object literal for the '<em><b>Colour Mode Labels</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS = eINSTANCE.getMpxiiiGlobalRegModel_ColourModeLabels();

		/**
		 * The meta object literal for the '<em><b>Dac Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER = eINSTANCE.getMpxiiiGlobalRegModel_DacNumber();

		/**
		 * The meta object literal for the '<em><b>Dac Name Calc1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1 = eINSTANCE.getMpxiiiGlobalRegModel_DacNameCalc1();

		/**
		 * The meta object literal for the '<em><b>Dac Name Calc2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2 = eINSTANCE.getMpxiiiGlobalRegModel_DacNameCalc2();

		/**
		 * The meta object literal for the '<em><b>Dac Name Calc3</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3 = eINSTANCE.getMpxiiiGlobalRegModel_DacNameCalc3();

		/**
		 * The meta object literal for the '<em><b>Dac Name Sel1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1 = eINSTANCE.getMpxiiiGlobalRegModel_DacNameSel1();

		/**
		 * The meta object literal for the '<em><b>Dac Name Sel2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2 = eINSTANCE.getMpxiiiGlobalRegModel_DacNameSel2();

		/**
		 * The meta object literal for the '<em><b>Dac Name Sel3</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3 = eINSTANCE.getMpxiiiGlobalRegModel_DacNameSel3();

		/**
		 * The meta object literal for the '<em><b>Dac Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__DAC_NAME = eINSTANCE.getMpxiiiGlobalRegModel_DacName();

		/**
		 * The meta object literal for the '<em><b>Counter Depth Labels</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS = eINSTANCE.getMpxiiiGlobalRegModel_CounterDepthLabels();

		/**
		 * The meta object literal for the '<em><b>Counter Depth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH = eINSTANCE.getMpxiiiGlobalRegModel_CounterDepth();

		/**
		 * The meta object literal for the '<em><b>Counter Depth As String</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING = eINSTANCE.getMpxiiiGlobalRegModel_CounterDepthAsString();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.PixelModelImpl <em>Pixel Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.PixelModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getPixelModel()
		 * @generated
		 */
		EClass PIXEL_MODEL = eINSTANCE.getPixelModel();

		/**
		 * The meta object literal for the '<em><b>Mask</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIXEL_MODEL__MASK = eINSTANCE.getPixelModel_Mask();

		/**
		 * The meta object literal for the '<em><b>Test</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIXEL_MODEL__TEST = eINSTANCE.getPixelModel_Test();

		/**
		 * The meta object literal for the '<em><b>Gain Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIXEL_MODEL__GAIN_MODE = eINSTANCE.getPixelModel_GainMode();

		/**
		 * The meta object literal for the '<em><b>Threshold A</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIXEL_MODEL__THRESHOLD_A = eINSTANCE.getPixelModel_ThresholdA();

		/**
		 * The meta object literal for the '<em><b>Threshold B</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIXEL_MODEL__THRESHOLD_B = eINSTANCE.getPixelModel_ThresholdB();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeImpl <em>Readout Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.ReadoutNodeImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getReadoutNode()
		 * @generated
		 */
		EClass READOUT_NODE = eINSTANCE.getReadoutNode();

		/**
		 * The meta object literal for the '<em><b>Readout Node Fem</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE__READOUT_NODE_FEM = eINSTANCE.getReadoutNode_ReadoutNodeFem();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute READOUT_NODE__ID = eINSTANCE.getReadoutNode_Id();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl <em>Readout Node Fem Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getReadoutNodeFemModel()
		 * @generated
		 */
		EClass READOUT_NODE_FEM_MODEL = eINSTANCE.getReadoutNodeFemModel();

		/**
		 * The meta object literal for the '<em><b>Counter Depth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute READOUT_NODE_FEM_MODEL__COUNTER_DEPTH = eINSTANCE.getReadoutNodeFemModel_CounterDepth();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg1</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg1();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg2</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg2();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg3</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg3();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg4</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg4();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg5</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg5();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg6</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg6();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg7</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg7();

		/**
		 * The meta object literal for the '<em><b>Mpxiii Chip Reg8</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8 = eINSTANCE.getReadoutNodeFemModel_MpxiiiChipReg8();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl <em>Summary Adbase Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.SummaryAdbaseModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getSummaryAdbaseModel()
		 * @generated
		 */
		EClass SUMMARY_ADBASE_MODEL = eINSTANCE.getSummaryAdbaseModel();

		/**
		 * The meta object literal for the '<em><b>Frame Divisor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SUMMARY_ADBASE_MODEL__FRAME_DIVISOR = eINSTANCE.getSummaryAdbaseModel_FrameDivisor();

		/**
		 * The meta object literal for the '<em><b>Counter Depth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SUMMARY_ADBASE_MODEL__COUNTER_DEPTH = eINSTANCE.getSummaryAdbaseModel_CounterDepth();

		/**
		 * The meta object literal for the '<em><b>Gap Fill Constant</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SUMMARY_ADBASE_MODEL__GAP_FILL_CONSTANT = eINSTANCE.getSummaryAdbaseModel_GapFillConstant();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.SummaryNodeImpl <em>Summary Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.SummaryNodeImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getSummaryNode()
		 * @generated
		 */
		EClass SUMMARY_NODE = eINSTANCE.getSummaryNode();

		/**
		 * The meta object literal for the '<em><b>Summary Fem</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SUMMARY_NODE__SUMMARY_FEM = eINSTANCE.getSummaryNode_SummaryFem();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.excalibur.config.model.impl.FixModelImpl <em>Fix Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.FixModelImpl
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getFixModel()
		 * @generated
		 */
		EClass FIX_MODEL = eINSTANCE.getFixModel();

		/**
		 * The meta object literal for the '<em><b>Statistics Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIX_MODEL__STATISTICS_ENABLED = eINSTANCE.getFixModel_StatisticsEnabled();

		/**
		 * The meta object literal for the '<em><b>Scale Edge Pixels Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED = eINSTANCE.getFixModel_ScaleEdgePixelsEnabled();

		/**
		 * The meta object literal for the '<em>Exception</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.lang.Exception
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getException()
		 * @generated
		 */
		EDataType EXCEPTION = eINSTANCE.getException();

		/**
		 * The meta object literal for the '<em>String Array</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getStringArray()
		 * @generated
		 */
		EDataType STRING_ARRAY = eINSTANCE.getStringArray();

		/**
		 * The meta object literal for the '<em>Short Array</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigPackageImpl#getShortArray()
		 * @generated
		 */
		EDataType SHORT_ARRAY = eINSTANCE.getShortArray();

	}

} //ExcaliburConfigPackage
