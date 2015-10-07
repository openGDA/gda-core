/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Region</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getRegionId <em>Region Id</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getLensMode <em>Lens Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getPassEnergy <em>Pass Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getExcitationEnergy <em>Excitation Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getAcquisitionMode <em>Acquisition Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getEnergyMode <em>Energy Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getFixEnergy <em>Fix Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getLowEnergy <em>Low Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getHighEnergy <em>High Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getEnergyStep <em>Energy Step</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getStepTime <em>Step Time</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getFirstXChannel <em>First XChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getLastXChannel <em>Last XChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getFirstYChannel <em>First YChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getLastYChannel <em>Last YChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getSlices <em>Slices</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getDetectorMode <em>Detector Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getADCMask <em>ADC Mask</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getDiscriminatorLevel <em>Discriminator Level</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getTotalSteps <em>Total Steps</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl#getTotalTime <em>Total Time</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RegionImpl extends EObjectImpl implements Region {
	/**
	 * The default value of the '{@link #getRegionId() <em>Region Id</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRegionId()
	 * @generated
	 * @ordered
	 */
	protected static final String REGION_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRegionId() <em>Region Id</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRegionId()
	 * @generated
	 * @ordered
	 */
	protected String regionId = REGION_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final STATUS STATUS_EDEFAULT = STATUS.READY;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected STATUS status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean enabled = ENABLED_EDEFAULT;

	/**
	 * This is true if the Enabled attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean enabledESet;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = "New Region";

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * This is true if the Name attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean nameESet;

	/**
	 * The default value of the '{@link #getLensMode() <em>Lens Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLensMode()
	 * @generated
	 * @ordered
	 */
	protected static final String LENS_MODE_EDEFAULT = "Transmission";

	/**
	 * The cached value of the '{@link #getLensMode() <em>Lens Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLensMode()
	 * @generated
	 * @ordered
	 */
	protected String lensMode = LENS_MODE_EDEFAULT;

	/**
	 * This is true if the Lens Mode attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean lensModeESet;

	/**
	 * The default value of the '{@link #getPassEnergy() <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getPassEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final int PASS_ENERGY_EDEFAULT = 10;

	/**
	 * The cached value of the '{@link #getPassEnergy() <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getPassEnergy()
	 * @generated
	 * @ordered
	 */
	protected int passEnergy = PASS_ENERGY_EDEFAULT;

	/**
	 * This is true if the Pass Energy attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean passEnergyESet;

	/**
	 * The cached value of the '{@link #getRunMode() <em>Run Mode</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRunMode()
	 * @generated
	 * @ordered
	 */
	protected RunMode runMode;

	/**
	 * This is true if the Run Mode containment reference has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean runModeESet;

	/**
	 * The default value of the '{@link #getExcitationEnergy() <em>Excitation Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getExcitationEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final double EXCITATION_ENERGY_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getExcitationEnergy() <em>Excitation Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getExcitationEnergy()
	 * @generated
	 * @ordered
	 */
	protected double excitationEnergy = EXCITATION_ENERGY_EDEFAULT;

	/**
	 * This is true if the Excitation Energy attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean excitationEnergyESet;

	/**
	 * The default value of the '{@link #getAcquisitionMode() <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getAcquisitionMode()
	 * @generated
	 * @ordered
	 */
	protected static final ACQUISITION_MODE ACQUISITION_MODE_EDEFAULT = ACQUISITION_MODE.SWEPT;

	/**
	 * The cached value of the '{@link #getAcquisitionMode() <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getAcquisitionMode()
	 * @generated
	 * @ordered
	 */
	protected ACQUISITION_MODE acquisitionMode = ACQUISITION_MODE_EDEFAULT;

	/**
	 * This is true if the Acquisition Mode attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean acquisitionModeESet;

	/**
	 * The default value of the '{@link #getEnergyMode() <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getEnergyMode()
	 * @generated
	 * @ordered
	 */
	protected static final ENERGY_MODE ENERGY_MODE_EDEFAULT = ENERGY_MODE.KINETIC;

	/**
	 * The cached value of the '{@link #getEnergyMode() <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getEnergyMode()
	 * @generated
	 * @ordered
	 */
	protected ENERGY_MODE energyMode = ENERGY_MODE_EDEFAULT;

	/**
	 * This is true if the Energy Mode attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean energyModeESet;

	/**
	 * The default value of the '{@link #getFixEnergy() <em>Fix Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFixEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final double FIX_ENERGY_EDEFAULT = 9.0;

	/**
	 * The cached value of the '{@link #getFixEnergy() <em>Fix Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFixEnergy()
	 * @generated
	 * @ordered
	 */
	protected double fixEnergy = FIX_ENERGY_EDEFAULT;

	/**
	 * This is true if the Fix Energy attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean fixEnergyESet;

	/**
	 * The default value of the '{@link #getLowEnergy() <em>Low Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLowEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final double LOW_ENERGY_EDEFAULT = 8.0;

	/**
	 * The cached value of the '{@link #getLowEnergy() <em>Low Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLowEnergy()
	 * @generated
	 * @ordered
	 */
	protected double lowEnergy = LOW_ENERGY_EDEFAULT;

	/**
	 * This is true if the Low Energy attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean lowEnergyESet;

	/**
	 * The default value of the '{@link #getHighEnergy() <em>High Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getHighEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final double HIGH_ENERGY_EDEFAULT = 10.0;

	/**
	 * The cached value of the '{@link #getHighEnergy() <em>High Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getHighEnergy()
	 * @generated
	 * @ordered
	 */
	protected double highEnergy = HIGH_ENERGY_EDEFAULT;

	/**
	 * This is true if the High Energy attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean highEnergyESet;

	/**
	 * The default value of the '{@link #getEnergyStep() <em>Energy Step</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getEnergyStep()
	 * @generated
	 * @ordered
	 */
	protected static final double ENERGY_STEP_EDEFAULT = 200.0;

	/**
	 * The cached value of the '{@link #getEnergyStep() <em>Energy Step</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getEnergyStep()
	 * @generated
	 * @ordered
	 */
	protected double energyStep = ENERGY_STEP_EDEFAULT;

	/**
	 * This is true if the Energy Step attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean energyStepESet;

	/**
	 * The default value of the '{@link #getStepTime() <em>Step Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getStepTime()
	 * @generated
	 * @ordered
	 */
	protected static final double STEP_TIME_EDEFAULT = 1.0;

	/**
	 * The cached value of the '{@link #getStepTime() <em>Step Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getStepTime()
	 * @generated
	 * @ordered
	 */
	protected double stepTime = STEP_TIME_EDEFAULT;

	/**
	 * This is true if the Step Time attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean stepTimeESet;

	/**
	 * The default value of the '{@link #getFirstXChannel() <em>First XChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getFirstXChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int FIRST_XCHANNEL_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getFirstXChannel() <em>First XChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getFirstXChannel()
	 * @generated
	 * @ordered
	 */
	protected int firstXChannel = FIRST_XCHANNEL_EDEFAULT;

	/**
	 * This is true if the First XChannel attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean firstXChannelESet;

	/**
	 * The default value of the '{@link #getLastXChannel() <em>Last XChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getLastXChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int LAST_XCHANNEL_EDEFAULT = 1000;

	/**
	 * The cached value of the '{@link #getLastXChannel() <em>Last XChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getLastXChannel()
	 * @generated
	 * @ordered
	 */
	protected int lastXChannel = LAST_XCHANNEL_EDEFAULT;

	/**
	 * This is true if the Last XChannel attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean lastXChannelESet;

	/**
	 * The default value of the '{@link #getFirstYChannel() <em>First YChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getFirstYChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int FIRST_YCHANNEL_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getFirstYChannel() <em>First YChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getFirstYChannel()
	 * @generated
	 * @ordered
	 */
	protected int firstYChannel = FIRST_YCHANNEL_EDEFAULT;

	/**
	 * This is true if the First YChannel attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean firstYChannelESet;

	/**
	 * The default value of the '{@link #getLastYChannel() <em>Last YChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getLastYChannel()
	 * @generated
	 * @ordered
	 */
	protected static final int LAST_YCHANNEL_EDEFAULT = 900;

	/**
	 * The cached value of the '{@link #getLastYChannel() <em>Last YChannel</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getLastYChannel()
	 * @generated
	 * @ordered
	 */
	protected int lastYChannel = LAST_YCHANNEL_EDEFAULT;

	/**
	 * This is true if the Last YChannel attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean lastYChannelESet;

	/**
	 * The default value of the '{@link #getSlices() <em>Slices</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getSlices()
	 * @generated
	 * @ordered
	 */
	protected static final int SLICES_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getSlices() <em>Slices</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getSlices()
	 * @generated
	 * @ordered
	 */
	protected int slices = SLICES_EDEFAULT;

	/**
	 * This is true if the Slices attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean slicesESet;

	/**
	 * The default value of the '{@link #getDetectorMode() <em>Detector Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDetectorMode()
	 * @generated
	 * @ordered
	 */
	protected static final DETECTOR_MODE DETECTOR_MODE_EDEFAULT = DETECTOR_MODE.ADC;

	/**
	 * The cached value of the '{@link #getDetectorMode() <em>Detector Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDetectorMode()
	 * @generated
	 * @ordered
	 */
	protected DETECTOR_MODE detectorMode = DETECTOR_MODE_EDEFAULT;

	/**
	 * This is true if the Detector Mode attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean detectorModeESet;

	/**
	 * The default value of the '{@link #getADCMask() <em>ADC Mask</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getADCMask()
	 * @generated
	 * @ordered
	 */
	protected static final int ADC_MASK_EDEFAULT = 255;

	/**
	 * The cached value of the '{@link #getADCMask() <em>ADC Mask</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getADCMask()
	 * @generated
	 * @ordered
	 */
	protected int adcMask = ADC_MASK_EDEFAULT;

	/**
	 * This is true if the ADC Mask attribute has been set.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean adcMaskESet;

	/**
	 * The default value of the '{@link #getDiscriminatorLevel() <em>Discriminator Level</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDiscriminatorLevel()
	 * @generated
	 * @ordered
	 */
	protected static final int DISCRIMINATOR_LEVEL_EDEFAULT = 10;

	/**
	 * The cached value of the '{@link #getDiscriminatorLevel() <em>Discriminator Level</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDiscriminatorLevel()
	 * @generated
	 * @ordered
	 */
	protected int discriminatorLevel = DISCRIMINATOR_LEVEL_EDEFAULT;

	/**
	 * This is true if the Discriminator Level attribute has been set. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	protected boolean discriminatorLevelESet;

	/**
	 * The default value of the '{@link #getTotalSteps() <em>Total Steps</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getTotalSteps()
	 * @generated
	 * @ordered
	 */
	protected static final int TOTAL_STEPS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTotalSteps() <em>Total Steps</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getTotalSteps()
	 * @generated
	 * @ordered
	 */
	protected int totalSteps = TOTAL_STEPS_EDEFAULT;

	/**
	 * This is true if the Total Steps attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean totalStepsESet;

	/**
	 * The default value of the '{@link #getTotalTime() <em>Total Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getTotalTime()
	 * @generated
	 * @ordered
	 */
	protected static final double TOTAL_TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getTotalTime() <em>Total Time</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getTotalTime()
	 * @generated
	 * @ordered
	 */
	protected double totalTime = TOTAL_TIME_EDEFAULT;

	/**
	 * This is true if the Total Time attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean totalTimeESet;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated NOT
	 */
	protected RegionImpl() {
		super();
		setRegionId(EcoreUtil.generateUUID());
		setRunMode(RegiondefinitionFactory.eINSTANCE.createRunMode());
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.REGION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRegionId() {
		return regionId;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRegionId(String newRegionId) {
		String oldRegionId = regionId;
		regionId = newRegionId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__REGION_ID, oldRegionId, regionId));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getName() {
		String newname=name.trim().replaceAll(" ", "_");
		return newname;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		boolean oldNameESet = nameESet;
		nameESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__NAME, oldName, name, !oldNameESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetName() {
		String oldName = name;
		boolean oldNameESet = nameESet;
		name = NAME_EDEFAULT;
		nameESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__NAME, oldName, NAME_EDEFAULT, oldNameESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetName() {
		return nameESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLensMode() {
		return lensMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLensMode(String newLensMode) {
		String oldLensMode = lensMode;
		lensMode = newLensMode;
		boolean oldLensModeESet = lensModeESet;
		lensModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__LENS_MODE, oldLensMode, lensMode, !oldLensModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetLensMode() {
		String oldLensMode = lensMode;
		boolean oldLensModeESet = lensModeESet;
		lensMode = LENS_MODE_EDEFAULT;
		lensModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__LENS_MODE, oldLensMode, LENS_MODE_EDEFAULT, oldLensModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetLensMode() {
		return lensModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPassEnergy() {
		return passEnergy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPassEnergy(int newPassEnergy) {
		int oldPassEnergy = passEnergy;
		passEnergy = newPassEnergy;
		boolean oldPassEnergyESet = passEnergyESet;
		passEnergyESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__PASS_ENERGY, oldPassEnergy, passEnergy, !oldPassEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetPassEnergy() {
		int oldPassEnergy = passEnergy;
		boolean oldPassEnergyESet = passEnergyESet;
		passEnergy = PASS_ENERGY_EDEFAULT;
		passEnergyESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__PASS_ENERGY, oldPassEnergy, PASS_ENERGY_EDEFAULT, oldPassEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetPassEnergy() {
		return passEnergyESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RunMode getRunMode() {
		return runMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRunMode(RunMode newRunMode,
			NotificationChain msgs) {
		RunMode oldRunMode = runMode;
		runMode = newRunMode;
		boolean oldRunModeESet = runModeESet;
		runModeESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__RUN_MODE, oldRunMode, newRunMode, !oldRunModeESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRunMode(RunMode newRunMode) {
		if (newRunMode != runMode) {
			NotificationChain msgs = null;
			if (runMode != null)
				msgs = ((InternalEObject)runMode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__RUN_MODE, null, msgs);
			if (newRunMode != null)
				msgs = ((InternalEObject)newRunMode).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__RUN_MODE, null, msgs);
			msgs = basicSetRunMode(newRunMode, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldRunModeESet = runModeESet;
			runModeESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__RUN_MODE, newRunMode, newRunMode, !oldRunModeESet));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetRunMode(NotificationChain msgs) {
		RunMode oldRunMode = runMode;
		runMode = null;
		boolean oldRunModeESet = runModeESet;
		runModeESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__RUN_MODE, oldRunMode, null, oldRunModeESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetRunMode() {
		if (runMode != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)runMode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegiondefinitionPackage.REGION__RUN_MODE, null, msgs);
			msgs = basicUnsetRunMode(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldRunModeESet = runModeESet;
			runModeESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__RUN_MODE, null, null, oldRunModeESet));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetRunMode() {
		return runModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getExcitationEnergy() {
		return excitationEnergy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExcitationEnergy(double newExcitationEnergy) {
		double oldExcitationEnergy = excitationEnergy;
		excitationEnergy = newExcitationEnergy;
		boolean oldExcitationEnergyESet = excitationEnergyESet;
		excitationEnergyESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__EXCITATION_ENERGY, oldExcitationEnergy, excitationEnergy, !oldExcitationEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetExcitationEnergy() {
		double oldExcitationEnergy = excitationEnergy;
		boolean oldExcitationEnergyESet = excitationEnergyESet;
		excitationEnergy = EXCITATION_ENERGY_EDEFAULT;
		excitationEnergyESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__EXCITATION_ENERGY, oldExcitationEnergy, EXCITATION_ENERGY_EDEFAULT, oldExcitationEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetExcitationEnergy() {
		return excitationEnergyESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ACQUISITION_MODE getAcquisitionMode() {
		return acquisitionMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAcquisitionMode(ACQUISITION_MODE newAcquisitionMode) {
		ACQUISITION_MODE oldAcquisitionMode = acquisitionMode;
		acquisitionMode = newAcquisitionMode == null ? ACQUISITION_MODE_EDEFAULT : newAcquisitionMode;
		boolean oldAcquisitionModeESet = acquisitionModeESet;
		acquisitionModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ACQUISITION_MODE, oldAcquisitionMode, acquisitionMode, !oldAcquisitionModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetAcquisitionMode() {
		ACQUISITION_MODE oldAcquisitionMode = acquisitionMode;
		boolean oldAcquisitionModeESet = acquisitionModeESet;
		acquisitionMode = ACQUISITION_MODE_EDEFAULT;
		acquisitionModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__ACQUISITION_MODE, oldAcquisitionMode, ACQUISITION_MODE_EDEFAULT, oldAcquisitionModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetAcquisitionMode() {
		return acquisitionModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ENERGY_MODE getEnergyMode() {
		return energyMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnergyMode(ENERGY_MODE newEnergyMode) {
		ENERGY_MODE oldEnergyMode = energyMode;
		energyMode = newEnergyMode == null ? ENERGY_MODE_EDEFAULT : newEnergyMode;
		boolean oldEnergyModeESet = energyModeESet;
		energyModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENERGY_MODE, oldEnergyMode, energyMode, !oldEnergyModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetEnergyMode() {
		ENERGY_MODE oldEnergyMode = energyMode;
		boolean oldEnergyModeESet = energyModeESet;
		energyMode = ENERGY_MODE_EDEFAULT;
		energyModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__ENERGY_MODE, oldEnergyMode, ENERGY_MODE_EDEFAULT, oldEnergyModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetEnergyMode() {
		return energyModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getFixEnergy() {
		return fixEnergy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFixEnergy(double newFixEnergy) {
		double oldFixEnergy = fixEnergy;
		fixEnergy = newFixEnergy;
		boolean oldFixEnergyESet = fixEnergyESet;
		fixEnergyESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__FIX_ENERGY, oldFixEnergy, fixEnergy, !oldFixEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetFixEnergy() {
		double oldFixEnergy = fixEnergy;
		boolean oldFixEnergyESet = fixEnergyESet;
		fixEnergy = FIX_ENERGY_EDEFAULT;
		fixEnergyESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__FIX_ENERGY, oldFixEnergy, FIX_ENERGY_EDEFAULT, oldFixEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetFixEnergy() {
		return fixEnergyESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getLowEnergy() {
		return lowEnergy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLowEnergy(double newLowEnergy) {
		double oldLowEnergy = lowEnergy;
		lowEnergy = newLowEnergy;
		boolean oldLowEnergyESet = lowEnergyESet;
		lowEnergyESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__LOW_ENERGY, oldLowEnergy, lowEnergy, !oldLowEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetLowEnergy() {
		double oldLowEnergy = lowEnergy;
		boolean oldLowEnergyESet = lowEnergyESet;
		lowEnergy = LOW_ENERGY_EDEFAULT;
		lowEnergyESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__LOW_ENERGY, oldLowEnergy, LOW_ENERGY_EDEFAULT, oldLowEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetLowEnergy() {
		return lowEnergyESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getHighEnergy() {
		return highEnergy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHighEnergy(double newHighEnergy) {
		double oldHighEnergy = highEnergy;
		highEnergy = newHighEnergy;
		boolean oldHighEnergyESet = highEnergyESet;
		highEnergyESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__HIGH_ENERGY, oldHighEnergy, highEnergy, !oldHighEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetHighEnergy() {
		double oldHighEnergy = highEnergy;
		boolean oldHighEnergyESet = highEnergyESet;
		highEnergy = HIGH_ENERGY_EDEFAULT;
		highEnergyESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__HIGH_ENERGY, oldHighEnergy, HIGH_ENERGY_EDEFAULT, oldHighEnergyESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetHighEnergy() {
		return highEnergyESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getEnergyStep() {
		return energyStep;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnergyStep(double newEnergyStep) {
		double oldEnergyStep = energyStep;
		energyStep = newEnergyStep;
		boolean oldEnergyStepESet = energyStepESet;
		energyStepESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENERGY_STEP, oldEnergyStep, energyStep, !oldEnergyStepESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetEnergyStep() {
		double oldEnergyStep = energyStep;
		boolean oldEnergyStepESet = energyStepESet;
		energyStep = ENERGY_STEP_EDEFAULT;
		energyStepESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__ENERGY_STEP, oldEnergyStep, ENERGY_STEP_EDEFAULT, oldEnergyStepESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetEnergyStep() {
		return energyStepESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getStepTime() {
		return stepTime;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStepTime(double newStepTime) {
		double oldStepTime = stepTime;
		stepTime = newStepTime;
		boolean oldStepTimeESet = stepTimeESet;
		stepTimeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__STEP_TIME, oldStepTime, stepTime, !oldStepTimeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetStepTime() {
		double oldStepTime = stepTime;
		boolean oldStepTimeESet = stepTimeESet;
		stepTime = STEP_TIME_EDEFAULT;
		stepTimeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__STEP_TIME, oldStepTime, STEP_TIME_EDEFAULT, oldStepTimeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetStepTime() {
		return stepTimeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getFirstXChannel() {
		return firstXChannel;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFirstXChannel(int newFirstXChannel) {
		int oldFirstXChannel = firstXChannel;
		firstXChannel = newFirstXChannel;
		boolean oldFirstXChannelESet = firstXChannelESet;
		firstXChannelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__FIRST_XCHANNEL, oldFirstXChannel, firstXChannel, !oldFirstXChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetFirstXChannel() {
		int oldFirstXChannel = firstXChannel;
		boolean oldFirstXChannelESet = firstXChannelESet;
		firstXChannel = FIRST_XCHANNEL_EDEFAULT;
		firstXChannelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__FIRST_XCHANNEL, oldFirstXChannel, FIRST_XCHANNEL_EDEFAULT, oldFirstXChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetFirstXChannel() {
		return firstXChannelESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getLastXChannel() {
		return lastXChannel;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastXChannel(int newLastXChannel) {
		int oldLastXChannel = lastXChannel;
		lastXChannel = newLastXChannel;
		boolean oldLastXChannelESet = lastXChannelESet;
		lastXChannelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__LAST_XCHANNEL, oldLastXChannel, lastXChannel, !oldLastXChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetLastXChannel() {
		int oldLastXChannel = lastXChannel;
		boolean oldLastXChannelESet = lastXChannelESet;
		lastXChannel = LAST_XCHANNEL_EDEFAULT;
		lastXChannelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__LAST_XCHANNEL, oldLastXChannel, LAST_XCHANNEL_EDEFAULT, oldLastXChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetLastXChannel() {
		return lastXChannelESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getFirstYChannel() {
		return firstYChannel;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFirstYChannel(int newFirstYChannel) {
		int oldFirstYChannel = firstYChannel;
		firstYChannel = newFirstYChannel;
		boolean oldFirstYChannelESet = firstYChannelESet;
		firstYChannelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__FIRST_YCHANNEL, oldFirstYChannel, firstYChannel, !oldFirstYChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetFirstYChannel() {
		int oldFirstYChannel = firstYChannel;
		boolean oldFirstYChannelESet = firstYChannelESet;
		firstYChannel = FIRST_YCHANNEL_EDEFAULT;
		firstYChannelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__FIRST_YCHANNEL, oldFirstYChannel, FIRST_YCHANNEL_EDEFAULT, oldFirstYChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetFirstYChannel() {
		return firstYChannelESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getLastYChannel() {
		return lastYChannel;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastYChannel(int newLastYChannel) {
		int oldLastYChannel = lastYChannel;
		lastYChannel = newLastYChannel;
		boolean oldLastYChannelESet = lastYChannelESet;
		lastYChannelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__LAST_YCHANNEL, oldLastYChannel, lastYChannel, !oldLastYChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetLastYChannel() {
		int oldLastYChannel = lastYChannel;
		boolean oldLastYChannelESet = lastYChannelESet;
		lastYChannel = LAST_YCHANNEL_EDEFAULT;
		lastYChannelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__LAST_YCHANNEL, oldLastYChannel, LAST_YCHANNEL_EDEFAULT, oldLastYChannelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetLastYChannel() {
		return lastYChannelESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getSlices() {
		return slices;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSlices(int newSlices) {
		int oldSlices = slices;
		slices = newSlices;
		boolean oldSlicesESet = slicesESet;
		slicesESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__SLICES, oldSlices, slices, !oldSlicesESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetSlices() {
		int oldSlices = slices;
		boolean oldSlicesESet = slicesESet;
		slices = SLICES_EDEFAULT;
		slicesESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__SLICES, oldSlices, SLICES_EDEFAULT, oldSlicesESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetSlices() {
		return slicesESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DETECTOR_MODE getDetectorMode() {
		return detectorMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDetectorMode(DETECTOR_MODE newDetectorMode) {
		DETECTOR_MODE oldDetectorMode = detectorMode;
		detectorMode = newDetectorMode == null ? DETECTOR_MODE_EDEFAULT : newDetectorMode;
		boolean oldDetectorModeESet = detectorModeESet;
		detectorModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__DETECTOR_MODE, oldDetectorMode, detectorMode, !oldDetectorModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetDetectorMode() {
		DETECTOR_MODE oldDetectorMode = detectorMode;
		boolean oldDetectorModeESet = detectorModeESet;
		detectorMode = DETECTOR_MODE_EDEFAULT;
		detectorModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__DETECTOR_MODE, oldDetectorMode, DETECTOR_MODE_EDEFAULT, oldDetectorModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDetectorMode() {
		return detectorModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getADCMask() {
		return adcMask;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setADCMask(int newADCMask) {
		int oldADCMask = adcMask;
		adcMask = newADCMask;
		boolean oldADCMaskESet = adcMaskESet;
		adcMaskESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ADC_MASK, oldADCMask, adcMask, !oldADCMaskESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetADCMask() {
		int oldADCMask = adcMask;
		boolean oldADCMaskESet = adcMaskESet;
		adcMask = ADC_MASK_EDEFAULT;
		adcMaskESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__ADC_MASK, oldADCMask, ADC_MASK_EDEFAULT, oldADCMaskESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetADCMask() {
		return adcMaskESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDiscriminatorLevel() {
		return discriminatorLevel;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDiscriminatorLevel(int newDiscriminatorLevel) {
		int oldDiscriminatorLevel = discriminatorLevel;
		discriminatorLevel = newDiscriminatorLevel;
		boolean oldDiscriminatorLevelESet = discriminatorLevelESet;
		discriminatorLevelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL, oldDiscriminatorLevel, discriminatorLevel, !oldDiscriminatorLevelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetDiscriminatorLevel() {
		int oldDiscriminatorLevel = discriminatorLevel;
		boolean oldDiscriminatorLevelESet = discriminatorLevelESet;
		discriminatorLevel = DISCRIMINATOR_LEVEL_EDEFAULT;
		discriminatorLevelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL, oldDiscriminatorLevel, DISCRIMINATOR_LEVEL_EDEFAULT, oldDiscriminatorLevelESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDiscriminatorLevel() {
		return discriminatorLevelESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getTotalSteps() {
		return totalSteps;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTotalSteps(int newTotalSteps) {
		int oldTotalSteps = totalSteps;
		totalSteps = newTotalSteps;
		boolean oldTotalStepsESet = totalStepsESet;
		totalStepsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__TOTAL_STEPS, oldTotalSteps, totalSteps, !oldTotalStepsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetTotalSteps() {
		int oldTotalSteps = totalSteps;
		boolean oldTotalStepsESet = totalStepsESet;
		totalSteps = TOTAL_STEPS_EDEFAULT;
		totalStepsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__TOTAL_STEPS, oldTotalSteps, TOTAL_STEPS_EDEFAULT, oldTotalStepsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetTotalSteps() {
		return totalStepsESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getTotalTime() {
		return totalTime;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTotalTime(double newTotalTime) {
		double oldTotalTime = totalTime;
		totalTime = newTotalTime;
		boolean oldTotalTimeESet = totalTimeESet;
		totalTimeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__TOTAL_TIME, oldTotalTime, totalTime, !oldTotalTimeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetTotalTime() {
		double oldTotalTime = totalTime;
		boolean oldTotalTimeESet = totalTimeESet;
		totalTime = TOTAL_TIME_EDEFAULT;
		totalTimeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__TOTAL_TIME, oldTotalTime, TOTAL_TIME_EDEFAULT, oldTotalTimeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetTotalTime() {
		return totalTimeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public STATUS getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(STATUS newStatus) {
		STATUS oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnabled(boolean newEnabled) {
		boolean oldEnabled = enabled;
		enabled = newEnabled;
		boolean oldEnabledESet = enabledESet;
		enabledESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.REGION__ENABLED, oldEnabled, enabled, !oldEnabledESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetEnabled() {
		boolean oldEnabled = enabled;
		boolean oldEnabledESet = enabledESet;
		enabled = ENABLED_EDEFAULT;
		enabledESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.REGION__ENABLED, oldEnabled, ENABLED_EDEFAULT, oldEnabledESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetEnabled() {
		return enabledESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return basicUnsetRunMode(msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__REGION_ID:
				return getRegionId();
			case RegiondefinitionPackage.REGION__STATUS:
				return getStatus();
			case RegiondefinitionPackage.REGION__ENABLED:
				return isEnabled();
			case RegiondefinitionPackage.REGION__NAME:
				return getName();
			case RegiondefinitionPackage.REGION__LENS_MODE:
				return getLensMode();
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				return getPassEnergy();
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return getRunMode();
			case RegiondefinitionPackage.REGION__EXCITATION_ENERGY:
				return getExcitationEnergy();
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				return getAcquisitionMode();
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				return getEnergyMode();
			case RegiondefinitionPackage.REGION__FIX_ENERGY:
				return getFixEnergy();
			case RegiondefinitionPackage.REGION__LOW_ENERGY:
				return getLowEnergy();
			case RegiondefinitionPackage.REGION__HIGH_ENERGY:
				return getHighEnergy();
			case RegiondefinitionPackage.REGION__ENERGY_STEP:
				return getEnergyStep();
			case RegiondefinitionPackage.REGION__STEP_TIME:
				return getStepTime();
			case RegiondefinitionPackage.REGION__FIRST_XCHANNEL:
				return getFirstXChannel();
			case RegiondefinitionPackage.REGION__LAST_XCHANNEL:
				return getLastXChannel();
			case RegiondefinitionPackage.REGION__FIRST_YCHANNEL:
				return getFirstYChannel();
			case RegiondefinitionPackage.REGION__LAST_YCHANNEL:
				return getLastYChannel();
			case RegiondefinitionPackage.REGION__SLICES:
				return getSlices();
			case RegiondefinitionPackage.REGION__DETECTOR_MODE:
				return getDetectorMode();
			case RegiondefinitionPackage.REGION__ADC_MASK:
				return getADCMask();
			case RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL:
				return getDiscriminatorLevel();
			case RegiondefinitionPackage.REGION__TOTAL_STEPS:
				return getTotalSteps();
			case RegiondefinitionPackage.REGION__TOTAL_TIME:
				return getTotalTime();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__REGION_ID:
				setRegionId((String)newValue);
				return;
			case RegiondefinitionPackage.REGION__STATUS:
				setStatus((STATUS)newValue);
				return;
			case RegiondefinitionPackage.REGION__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case RegiondefinitionPackage.REGION__NAME:
				setName((String)newValue);
				return;
			case RegiondefinitionPackage.REGION__LENS_MODE:
				setLensMode((String)newValue);
				return;
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				setPassEnergy((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__RUN_MODE:
				setRunMode((RunMode)newValue);
				return;
			case RegiondefinitionPackage.REGION__EXCITATION_ENERGY:
				setExcitationEnergy((Double)newValue);
				return;
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				setAcquisitionMode((ACQUISITION_MODE)newValue);
				return;
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				setEnergyMode((ENERGY_MODE)newValue);
				return;
			case RegiondefinitionPackage.REGION__FIX_ENERGY:
				setFixEnergy((Double)newValue);
				return;
			case RegiondefinitionPackage.REGION__LOW_ENERGY:
				setLowEnergy((Double)newValue);
				return;
			case RegiondefinitionPackage.REGION__HIGH_ENERGY:
				setHighEnergy((Double)newValue);
				return;
			case RegiondefinitionPackage.REGION__ENERGY_STEP:
				setEnergyStep((Double)newValue);
				return;
			case RegiondefinitionPackage.REGION__STEP_TIME:
				setStepTime((Double)newValue);
				return;
			case RegiondefinitionPackage.REGION__FIRST_XCHANNEL:
				setFirstXChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__LAST_XCHANNEL:
				setLastXChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__FIRST_YCHANNEL:
				setFirstYChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__LAST_YCHANNEL:
				setLastYChannel((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__SLICES:
				setSlices((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__DETECTOR_MODE:
				setDetectorMode((DETECTOR_MODE)newValue);
				return;
			case RegiondefinitionPackage.REGION__ADC_MASK:
				setADCMask((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL:
				setDiscriminatorLevel((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__TOTAL_STEPS:
				setTotalSteps((Integer)newValue);
				return;
			case RegiondefinitionPackage.REGION__TOTAL_TIME:
				setTotalTime((Double)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__REGION_ID:
				setRegionId(REGION_ID_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case RegiondefinitionPackage.REGION__ENABLED:
				unsetEnabled();
				return;
			case RegiondefinitionPackage.REGION__NAME:
				unsetName();
				return;
			case RegiondefinitionPackage.REGION__LENS_MODE:
				unsetLensMode();
				return;
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				unsetPassEnergy();
				return;
			case RegiondefinitionPackage.REGION__RUN_MODE:
				unsetRunMode();
				return;
			case RegiondefinitionPackage.REGION__EXCITATION_ENERGY:
				unsetExcitationEnergy();
				return;
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				unsetAcquisitionMode();
				return;
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				unsetEnergyMode();
				return;
			case RegiondefinitionPackage.REGION__FIX_ENERGY:
				unsetFixEnergy();
				return;
			case RegiondefinitionPackage.REGION__LOW_ENERGY:
				unsetLowEnergy();
				return;
			case RegiondefinitionPackage.REGION__HIGH_ENERGY:
				unsetHighEnergy();
				return;
			case RegiondefinitionPackage.REGION__ENERGY_STEP:
				unsetEnergyStep();
				return;
			case RegiondefinitionPackage.REGION__STEP_TIME:
				unsetStepTime();
				return;
			case RegiondefinitionPackage.REGION__FIRST_XCHANNEL:
				unsetFirstXChannel();
				return;
			case RegiondefinitionPackage.REGION__LAST_XCHANNEL:
				unsetLastXChannel();
				return;
			case RegiondefinitionPackage.REGION__FIRST_YCHANNEL:
				unsetFirstYChannel();
				return;
			case RegiondefinitionPackage.REGION__LAST_YCHANNEL:
				unsetLastYChannel();
				return;
			case RegiondefinitionPackage.REGION__SLICES:
				unsetSlices();
				return;
			case RegiondefinitionPackage.REGION__DETECTOR_MODE:
				unsetDetectorMode();
				return;
			case RegiondefinitionPackage.REGION__ADC_MASK:
				unsetADCMask();
				return;
			case RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL:
				unsetDiscriminatorLevel();
				return;
			case RegiondefinitionPackage.REGION__TOTAL_STEPS:
				unsetTotalSteps();
				return;
			case RegiondefinitionPackage.REGION__TOTAL_TIME:
				unsetTotalTime();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case RegiondefinitionPackage.REGION__REGION_ID:
				return REGION_ID_EDEFAULT == null ? regionId != null : !REGION_ID_EDEFAULT.equals(regionId);
			case RegiondefinitionPackage.REGION__STATUS:
				return status != STATUS_EDEFAULT;
			case RegiondefinitionPackage.REGION__ENABLED:
				return isSetEnabled();
			case RegiondefinitionPackage.REGION__NAME:
				return isSetName();
			case RegiondefinitionPackage.REGION__LENS_MODE:
				return isSetLensMode();
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
				return isSetPassEnergy();
			case RegiondefinitionPackage.REGION__RUN_MODE:
				return isSetRunMode();
			case RegiondefinitionPackage.REGION__EXCITATION_ENERGY:
				return isSetExcitationEnergy();
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
				return isSetAcquisitionMode();
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
				return isSetEnergyMode();
			case RegiondefinitionPackage.REGION__FIX_ENERGY:
				return isSetFixEnergy();
			case RegiondefinitionPackage.REGION__LOW_ENERGY:
				return isSetLowEnergy();
			case RegiondefinitionPackage.REGION__HIGH_ENERGY:
				return isSetHighEnergy();
			case RegiondefinitionPackage.REGION__ENERGY_STEP:
				return isSetEnergyStep();
			case RegiondefinitionPackage.REGION__STEP_TIME:
				return isSetStepTime();
			case RegiondefinitionPackage.REGION__FIRST_XCHANNEL:
				return isSetFirstXChannel();
			case RegiondefinitionPackage.REGION__LAST_XCHANNEL:
				return isSetLastXChannel();
			case RegiondefinitionPackage.REGION__FIRST_YCHANNEL:
				return isSetFirstYChannel();
			case RegiondefinitionPackage.REGION__LAST_YCHANNEL:
				return isSetLastYChannel();
			case RegiondefinitionPackage.REGION__SLICES:
				return isSetSlices();
			case RegiondefinitionPackage.REGION__DETECTOR_MODE:
				return isSetDetectorMode();
			case RegiondefinitionPackage.REGION__ADC_MASK:
				return isSetADCMask();
			case RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL:
				return isSetDiscriminatorLevel();
			case RegiondefinitionPackage.REGION__TOTAL_STEPS:
				return isSetTotalSteps();
			case RegiondefinitionPackage.REGION__TOTAL_TIME:
				return isSetTotalTime();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (regionId: "); //$NON-NLS-1$
		result.append(regionId);
		result.append(", Status: "); //$NON-NLS-1$
		result.append(status);
		result.append(", Enabled: "); //$NON-NLS-1$
		if (enabledESet) result.append(enabled); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", name: "); //$NON-NLS-1$
		if (nameESet) result.append(name); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", lensMode: "); //$NON-NLS-1$
		if (lensModeESet) result.append(lensMode); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", passEnergy: "); //$NON-NLS-1$
		if (passEnergyESet) result.append(passEnergy); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", excitationEnergy: "); //$NON-NLS-1$
		if (excitationEnergyESet) result.append(excitationEnergy); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", acquisitionMode: "); //$NON-NLS-1$
		if (acquisitionModeESet) result.append(acquisitionMode); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", energyMode: "); //$NON-NLS-1$
		if (energyModeESet) result.append(energyMode); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", fixEnergy: "); //$NON-NLS-1$
		if (fixEnergyESet) result.append(fixEnergy); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", lowEnergy: "); //$NON-NLS-1$
		if (lowEnergyESet) result.append(lowEnergy); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", highEnergy: "); //$NON-NLS-1$
		if (highEnergyESet) result.append(highEnergy); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", energyStep: "); //$NON-NLS-1$
		if (energyStepESet) result.append(energyStep); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", stepTime: "); //$NON-NLS-1$
		if (stepTimeESet) result.append(stepTime); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", firstXChannel: "); //$NON-NLS-1$
		if (firstXChannelESet) result.append(firstXChannel); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", lastXChannel: "); //$NON-NLS-1$
		if (lastXChannelESet) result.append(lastXChannel); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", firstYChannel: "); //$NON-NLS-1$
		if (firstYChannelESet) result.append(firstYChannel); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", lastYChannel: "); //$NON-NLS-1$
		if (lastYChannelESet) result.append(lastYChannel); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", slices: "); //$NON-NLS-1$
		if (slicesESet) result.append(slices); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", detectorMode: "); //$NON-NLS-1$
		if (detectorModeESet) result.append(detectorMode); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", ADCMask: "); //$NON-NLS-1$
		if (adcMaskESet) result.append(adcMask); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", discriminatorLevel: "); //$NON-NLS-1$
		if (discriminatorLevelESet) result.append(discriminatorLevel); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", totalSteps: "); //$NON-NLS-1$
		if (totalStepsESet) result.append(totalSteps); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", totalTime: "); //$NON-NLS-1$
		if (totalTimeESet) result.append(totalTime); else result.append("<unset>"); //$NON-NLS-1$
		result.append(')');
		return result.toString();
	}

} // RegionImpl
