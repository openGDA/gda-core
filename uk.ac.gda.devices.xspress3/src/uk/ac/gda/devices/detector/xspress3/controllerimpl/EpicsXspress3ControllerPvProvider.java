package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;

public class EpicsXspress3ControllerPvProvider {

	public static final int NUMBER_ROIs = 10; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation
	public static final int MCA_SIZE = 4096; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation

	final private int numberOfDetectorChannels;

	// EPICS strings in camelcase are from the Quantum Detectors API, the ones in capitals are EPICS values

	// Control and Status
	private static String ACQUIRE_SUFFIX = ":Acquire";
	private static String ERASE_SUFFIX = ":ERASE";
	private static String RESET_SUFFIX = ":RESET";
	private static String NUM_IMAGES_SUFFIX = ":NumImages";
	private static String NUM_IMAGES_RBV_SUFFIX = ":NumImages_RBV";
	private static String ROI_CALC_SUFFIX = ":CTRL_MCA_ROI";
	private static String ROI_CALC_RBV_SUFFIX = ":CTRL_MCA_ROI_RBV";
	private static String TRIG_MODE_SUFFIX = ":TriggerMode";
	private static String TRIG_MODE_RBV_SUFFIX = ":TriggerMode_RBV";
	private static String STATUS_MSG_SUFFIX = ":StatusMessage_RBV";
	private static String STATUS_RBV_SUFFIX = ":DetectorState_RBV";
	private static String FRAMES_PER_READ_SUFFIX = ":ArrayRate_RBV";
	private static String FRAMES_AVAILABLE_SUFFIX = ":ArrayCounter_RBV";
	private static String BUSY_SUFFIX = ":Acquire_RBV";
	private static String UPDATEARRAYS_SUFFIX = ":UPDATE";

	// System Configuration
	private static String MAX_FRAMES_SUFFIX = ":NUM_FRAMES_CONFIG_RBV";
	private static String MCA_SIZE_SUFFIX = ":MAX_SPECTRA_RBV";
	private static String CONNECTION_STATUS = ":CONNECTED";

	// File creation PVs
	private static String STARTSTOP_FILE_WRITING = ":HDF5:Capture";
	private static String FILE_WRITING_RBV = ":HDF5:Capture_RBV";

	private static String FILE_PATH = ":HDF5:FilePath";
	private static String FILE_PATH_RBV = ":HDF5:FilePath_RBV";

	private static String FILE_PREFIX = ":HDF5:FileName";
	private static String FILE_PREFIX_RBV = ":HDF5:FileName_RBV";

	private static String NEXT_FILENUMBER = ":HDF5:FileNumber";
	private static String FILE_AUTOINCREMENT = ":HDF5:AutoIncrement";
	private static String FILE_NUMCAPTURE = ":HDF5:NumCapture";
	private static String FULLFILENAME = ":HDF5:FullFileName_RBV";

	private static String EXTRA_DIMS = ":HDF5:NumExtraDims";
	private static String EXTRA_DIM_N = ":HDF5:ExtraDimSizeN";
	private static String EXTRA_DIM_X= ":HDF5:ExtraDimSizeX";
	private static String EXTRA_DIM_Y = ":HDF5:ExtraDimSizeY";

	// All Element Sum File creation PVs
//	private static String ALL_EL_STARTSTOP_FILE_WRITING = ":ALLEL:HDF5:Capture";
//	private static String ALL_EL_FILE_WRITING_RBV = ":ALLEL:HDF5:Capture_RBV";
//
//	private static String ALL_EL_FILE_PATH = ":ALLEL:HDF5:FilePath";
//	private static String ALL_EL_FILE_PATH_RBV = ":ALLEL:HDF5:FilePath_RBV";
//
//	private static String ALL_EL_FILE_PREFIX = ":ALLEL:HDF5:FileName";
//	private static String ALL_EL_FILE_PREFIX_RBV = ":ALLEL:HDF5:FileName_RBV";
//
//	private static String ALL_EL_NEXT_FILENUMBER = ":ALLEL:HDF5:FileNumber";
//	private static String ALL_EL_FILE_AUTOINCREMENT = ":ALLEL:HDF5:AutoIncrement";
//	private static String ALL_EL_FILE_NUMCAPTURE = ":ALLEL:HDF5:NumCapture";
//
//	private static String ALL_EL_EXTRA_DIMS = ":ALLEL:HDF5:NumExtraDims";
//	private static String ALL_EL_EXTRA_DIM_N = ":ALLEL:HDF5:ExtraDimSizeN";
//	private static String ALL_EL_EXTRA_DIM_X= ":ALLEL:HDF5:ExtraDimSizeX";
//	private static String ALL_EL_EXTRA_DIM_Y = ":ALLEL:HDF5:ExtraDimSizeY";




	// Display Updates
//	private static String SCALER_UPDATE_SUFFIX = ":CTRL_DATA_UPDATE";
//	private static String SCALER_UPDATE_RBV_SUFFIX = ":CTRL_DATA_UPDATE_RBV";
//	private static String SCALER_UPDATE_PERIOD_SUFFIX = ":CTRL_DATA_UPDATE_PERIOD";
//	private static String MCA_UPDATE_SUFFIX = ":CTRL_MCA_UPDATE";
//	private static String MCA_UPDATE_RBV_SUFFIX = ":CTRL_MCA_UPDATE_RBV";
//	private static String MCA_UPDATE_PERIOD_SUFFIX = ":CTRL_MCA_UPDATE_PERIOD";
//	private static String SCA_UPDATE_SUFFIX = ":CTRL_SCA_UPDATE";
//	private static String SCA_UPDATE_RBV_SUFFIX = ":CTRL_SCA_UPDATE_RBV";
//	private static String SCA_UPDATE_PERIOD_SUFFIX = ":CTRL_SCA_UPDATE_PERIOD";

	// MCA and ROI
	private static String ROI_LOW_BIN_TEMPLATE = ":C%1d_MCA_ROI%1d_LLM";  // channel (1-8),ROI (1-4)
	private static String ROI_HIGH_BIN_TEMPLATE = ":C%1d_MCA_ROI%1d_HLM";// channel (1-8),ROI (1-4)
	private static String ROI_COUNT_TEMPLATE = ";C%1d_ROI%1d:Value_RBV";// channel (1-8),ROI (1-4)
	private static String ROIS_TEMPLATE = ":C%1d_ROI%1d:ArrayData_RBV.VAL"; // channel (1-8),ROI (1-4) this points towards a waveform
	private static String MCA_TEMPLATE = ":ARR%1d:ArrayData";// channel (1-8) this points towards a waveform
	private static String MCA_SUM_TEMPLATE = ":ARRSUM%1d:ArrayData";// channel (1-8) this points towards a waveform
	private static String CHANNEL_ENABLE_TEMPLATE = ":C%1d_PluginControlVal";

	// SCA
	private static String SCA_WIN1_LOW_BIN_TEMPLATE = ":C%1d_SCA5_LLM";// channel (1-8)
	private static String SCA_WIN1_LOW_BIN_RBV_TEMPLATE = ":C%1d_SCA5_LLM_RBV";// channel (1-8)
	private static String SCA_WIN1_HIGH_BIN_TEMPLATE = ":C%1d_SCA5_HLM";// channel (1-8)
	private static String SCA_WIN1_HIGH_BIN_RBV_TEMPLATE = ":C%1d_SCA5_HLM_RBV";// channel (1-8)
	private static String SCA_WIN2_LOW_BIN_TEMPLATE = ":C%1d_SCA6_LLM";// channel (1-8)
	private static String SCA_WIN2_LOW_BIN_RBV_TEMPLATE = ":C%1d_SCA6_LLM_RBV";// channel (1-8)
	private static String SCA_WIN2_HIGH_BIN_TEMPLATE = ":C%1d_SCA6_HLM";// channel (1-8)
	private static String SCA_WIN2_HIGH_BIN_RBV_TEMPLATE = ":C%1d_SCA6_HLM_RBV";// channel (1-8)

	private static String SCA_WIN1_SCAS_TEMPLATE = ":C%1d_SCA5:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static String SCA_WIN2_SCAS_TEMPLATE = ":C%1d_SCA6:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform

	private static String SCA_TIME_SCAS_TEMPLATE = ":C%1d_SCA0:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static String SCA_RESET_TICKS_SCAS_TEMPLATE = ":C%1d_SCA1:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static String SCA_RESET_COUNT_TEMPLATE = ":C%1d_SCA2:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static String SCA_ALL_EVENT_TEMPLATE = ":C%1d_SCA3:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static String SCA_ALL_GOOD_TEMPLATE = ":C%1d_SCA4:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static String SCA_PILEUP_TEMPLATE = ":C%1d_SCA7:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform

	// Deadtime correction parameters
	private static String ALL_GOOD_EVT_GRAD_TEMPLATE = ":C%1d_DTC_AEG_RBV";// channel (1-8)
	private static String ALL_GOOD_EVT_OFFSET_TEMPLATE = ":C%1d_DTC_AEO_RBV";// channel (1-8)
	private static String IN_WIN_EVT_GRAD_TEMPLATE = ":C%1d_DTC_IWG_RBV";// channel (1-8)
	private static String INWIN_EVT_OFFSET_TEMPLATE = ":C%1d_DTC_IWO_RBV";// channel (1-8)

	// the shared PVs with the Controller which uses this object
	protected String epicsTemplate;
	protected PV<ACQUIRE_STATE> pvAcquire;
	protected PV<ERASE_STATE> pvErase;
	protected PV<Integer> pvReset;
	protected PV<Integer> pvUpdate;
	protected PV<Integer> pvSetNumImages;
	protected ReadOnlyPV<Integer> pvGetNumImages;
	protected PV<UPDATE_CTRL> pvSetRoiCalc;
	protected ReadOnlyPV<UPDATE_RBV> pvGetRoiCalc;
	protected PV<TRIGGER_MODE> pvSetTrigMode;
	protected ReadOnlyPV<TRIGGER_MODE> pvGetTrigMode;
	protected ReadOnlyPV<String> pvGetStatusMsg;
	protected ReadOnlyPV<XSPRESS3_EPICS_STATUS> pvGetState;
	protected ReadOnlyPV<Integer> pvGetNumFramesPerReadout;
	protected ReadOnlyPV<Boolean> pvIsBusy;
	protected ReadOnlyPV<Integer> pvGetNumFramesAvailableToReadout;
	protected ReadOnlyPV<CONNECTION_STATE> pvIsConnected;
	protected ReadOnlyPV<Integer> pvGetMaxFrames;
	protected ReadOnlyPV<Integer> pvGetMCASize;
//	protected PV<UPDATE_CTRL> pvSetScalerUpdate;
//	protected ReadOnlyPV<UPDATE_RBV> pvGetScalerUpdate;
//	protected PV<Integer> pvScalerUpdatePeriod;
//	protected PV<UPDATE_CTRL> pvSetMCAUpdate;
//	protected ReadOnlyPV<UPDATE_RBV> pvGetMCAUpdate;
//	protected PV<Integer> pvMCAUpdatePeriod;
//	protected PV<UPDATE_CTRL> pvSetSCAROIUpdate;
//	protected ReadOnlyPV<UPDATE_RBV> pvGetSCAROIUpdate;
//	protected PV<Integer> pvSCAROIUpdatePeriod;
	protected PV<UPDATE_RBV>[] pvsChannelEnable;
	protected ReadOnlyPV<Double[]>[] pvsScalerWindow1;
	protected ReadOnlyPV<Double[]>[] pvsScalerWindow2;
	protected ReadOnlyPV<Integer[]>[] pvsTime;
	protected ReadOnlyPV<Integer[]>[] pvsResetTicks;
	protected ReadOnlyPV<Integer[]>[] pvsResetCount;
	protected ReadOnlyPV<Integer[]>[] pvsAllEvent;
	protected ReadOnlyPV<Integer[]>[] pvsAllGood;
	protected ReadOnlyPV<Integer[]>[] pvsPileup;
	protected ReadOnlyPV<Integer>[] pvsGoodEventGradient;
	protected ReadOnlyPV<Integer>[] pvsGoodEventOffset;
	protected ReadOnlyPV<Integer>[] pvsInWinEventGradient;
	protected ReadOnlyPV<Integer>[] pvsInWinEventOffset;
	protected PV<Integer>[] pvsScaWin1Low;
	protected ReadOnlyPV<Integer>[] pvsScaWin1LowRBV;
	protected PV<Integer>[] pvsScaWin1High;
	protected ReadOnlyPV<Integer>[] pvsScaWin1HighRBV;
	protected PV<Integer>[] pvsScaWin2Low;
	protected ReadOnlyPV<Integer>[] pvsScaWin2LowRBV;
	protected PV<Integer>[] pvsScaWin2High;
	protected ReadOnlyPV<Integer>[] pvsScaWin2HighRBV;
	protected ReadOnlyPV<Double[]>[] pvsLatestMCA; //[channel]
	protected ReadOnlyPV<Double[]>[] pvsLatestMCASummed; //[channel]
	protected PV<Integer>[][] pvsROILLM;// [roi][channel]
	protected PV<Integer>[][] pvsROIHLM;// [roi][channel]
	protected ReadOnlyPV<Double>[][] pvsLatestROI;  // [roi][channel]
	protected ReadOnlyPV<Double[]>[][] pvsROIs;   //[roi][channel]

	protected PV<CAPTURE_CTRL_RBV> pvStartStopFileWriting;
	protected ReadOnlyPV<CAPTURE_CTRL_RBV> pvIsFileWriting;
	protected ReadOnlyPV<Integer> pvGetFileWritingStatus;
	protected PV<String> pvSetFilePath;
	protected ReadOnlyPV<String> pvGetFilePath;
	protected PV<String> pvSetFilePrefix;
	protected ReadOnlyPV<String> pvGetFilePrefix;
	protected PV<Integer> pvNextFileNumber;
	protected PV<Integer> pvExtraDimensions;
	protected PV<Integer> pvExtraDimN;
	protected PV<Integer> pvExtraDimX;
	protected PV<Integer> pvExtraDimY;
	protected PV<Boolean> pvHDFAutoIncrement;
	protected PV<Integer> pvHDFNumCapture;
	protected ReadOnlyPV<String> pvHDFFullFileName;

//	protected PV<CAPTURE_CTRL_RBV> pvAllElementSumStartStopFileWriting;
//	protected ReadOnlyPV<CAPTURE_CTRL_RBV> pvAllElementSumIsFileWriting;
//	protected ReadOnlyPV<Integer> pvAllElementSumGetFileWritingStatus;
//	protected PV<String> pvAllElementSumSetFilePath;
//	protected ReadOnlyPV<String> pvAllElementSumGetFilePath;
//	protected PV<String> pvAllElementSumSetFilePrefix;
//	protected ReadOnlyPV<String> pvAllElementSumGetFilePrefix;
//	protected PV<Integer> pvAllElementSumNextFileNumber;
//	protected PV<Integer> pvAllElementSumExtraDimensions;
//	protected PV<Integer> pvAllElementSumExtraDimN;
//	protected PV<Integer> pvAllElementSumExtraDimX;
//	protected PV<Integer> pvAllElementSumExtraDimY;
//	protected PV<Boolean> pvAllElementSumHDFAutoIncrement;
//	protected PV<Integer> pvAllElementSumHDFNumCapture;

	public EpicsXspress3ControllerPvProvider(String epicsTemplate, int numberOfDetectorChannels) throws FactoryException {
		this.numberOfDetectorChannels = numberOfDetectorChannels;
		if (epicsTemplate == null || epicsTemplate.isEmpty()){
			throw new FactoryException("Epics template has not been set!");
		}
		this.epicsTemplate = epicsTemplate;
		createPVs();
	}

	private void createPVs() {
		createControlPVs();
		createFileWritingPVs();
		createDisplayPVs();
		createReadoutPVs();
		createMCAPVs();
	}

	private void createControlPVs() {
		 pvAcquire = LazyPVFactory.newEnumPV(generatePVName(ACQUIRE_SUFFIX),ACQUIRE_STATE.class);
		 pvErase = LazyPVFactory.newEnumPV(generatePVName(ERASE_SUFFIX), ERASE_STATE.class);
		 pvReset = LazyPVFactory.newIntegerPV(generatePVName(RESET_SUFFIX));
		 pvUpdate = LazyPVFactory.newIntegerPV(generatePVName(UPDATEARRAYS_SUFFIX));
		 pvSetNumImages = LazyPVFactory.newIntegerPV(generatePVName(NUM_IMAGES_SUFFIX));
		 pvGetNumImages = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(NUM_IMAGES_RBV_SUFFIX));
		 pvSetRoiCalc  = LazyPVFactory.newEnumPV(generatePVName(ROI_CALC_SUFFIX),UPDATE_CTRL.class);
		 pvGetRoiCalc = LazyPVFactory.newReadOnlyEnumPV(generatePVName(ROI_CALC_RBV_SUFFIX),UPDATE_RBV.class);
		 pvSetTrigMode = LazyPVFactory.newEnumPV(generatePVName(TRIG_MODE_SUFFIX), TRIGGER_MODE.class);
		 pvGetTrigMode = LazyPVFactory.newReadOnlyEnumPV(generatePVName(TRIG_MODE_RBV_SUFFIX), TRIGGER_MODE.class);
		 pvGetStatusMsg = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(STATUS_MSG_SUFFIX));
		 pvGetState = LazyPVFactory.newReadOnlyEnumPV(generatePVName(STATUS_RBV_SUFFIX),XSPRESS3_EPICS_STATUS.class);
		 pvGetNumFramesPerReadout = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(FRAMES_PER_READ_SUFFIX));
		 pvGetNumFramesAvailableToReadout = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(FRAMES_AVAILABLE_SUFFIX));
		 pvIsBusy = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(generatePVName(BUSY_SUFFIX));
		 pvIsConnected = LazyPVFactory.newReadOnlyEnumPV(generatePVName(CONNECTION_STATUS), CONNECTION_STATE.class);
	}

	private void createFileWritingPVs() {
		pvStartStopFileWriting =  LazyPVFactory.newEnumPV(generatePVName(STARTSTOP_FILE_WRITING),CAPTURE_CTRL_RBV.class);
		pvIsFileWriting = LazyPVFactory.newReadOnlyEnumPV(generatePVName(FILE_WRITING_RBV),CAPTURE_CTRL_RBV.class);
		pvSetFilePath = LazyPVFactory.newStringFromWaveformPV(generatePVName(FILE_PATH));
		pvGetFilePath = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(FILE_PATH_RBV));
		pvSetFilePrefix = LazyPVFactory.newStringFromWaveformPV(generatePVName(FILE_PREFIX));
		pvGetFilePrefix = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(FILE_PREFIX_RBV));
		pvNextFileNumber = LazyPVFactory.newIntegerPV(generatePVName(NEXT_FILENUMBER));
		pvHDFAutoIncrement = LazyPVFactory.newBooleanFromEnumPV(generatePVName(FILE_AUTOINCREMENT));
		pvHDFNumCapture = LazyPVFactory.newIntegerPV(generatePVName(FILE_NUMCAPTURE));

		pvHDFFullFileName = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(FULLFILENAME));
		pvExtraDimensions = LazyPVFactory.newIntegerPV(generatePVName(EXTRA_DIMS));
		pvExtraDimN = LazyPVFactory.newIntegerPV(generatePVName(EXTRA_DIM_N));
		pvExtraDimX = LazyPVFactory.newIntegerPV(generatePVName(EXTRA_DIM_X));
		pvExtraDimY = LazyPVFactory.newIntegerPV(generatePVName(EXTRA_DIM_Y));

//		pvAllElementSumStartStopFileWriting =  LazyPVFactory.newEnumPV(generatePVName(ALL_EL_STARTSTOP_FILE_WRITING),CAPTURE_CTRL_RBV.class);
//		pvAllElementSumIsFileWriting = LazyPVFactory.newReadOnlyEnumPV(generatePVName(ALL_EL_FILE_WRITING_RBV),CAPTURE_CTRL_RBV.class);
//		pvAllElementSumSetFilePath = LazyPVFactory.newStringFromWaveformPV(generatePVName(ALL_EL_FILE_PATH));
//		pvAllElementSumGetFilePath = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(ALL_EL_FILE_PATH_RBV));
//		pvAllElementSumSetFilePrefix = LazyPVFactory.newStringFromWaveformPV(generatePVName(ALL_EL_FILE_PREFIX));
//		pvAllElementSumGetFilePrefix = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(ALL_EL_FILE_PREFIX_RBV));
//		pvAllElementSumNextFileNumber = LazyPVFactory.newIntegerPV(generatePVName(ALL_EL_NEXT_FILENUMBER));
//		pvAllElementSumHDFAutoIncrement = LazyPVFactory.newBooleanFromEnumPV(generatePVName(ALL_EL_FILE_AUTOINCREMENT));
//		pvAllElementSumHDFNumCapture = LazyPVFactory.newIntegerPV(generatePVName(ALL_EL_FILE_NUMCAPTURE));
//		pvAllElementSumExtraDimensions = LazyPVFactory.newIntegerPV(generatePVName(ALL_EL_EXTRA_DIMS));
//		pvAllElementSumExtraDimN = LazyPVFactory.newIntegerPV(generatePVName(ALL_EL_EXTRA_DIM_N));
//		pvAllElementSumExtraDimX = LazyPVFactory.newIntegerPV(generatePVName(ALL_EL_EXTRA_DIM_X));
//		pvAllElementSumExtraDimY = LazyPVFactory.newIntegerPV(generatePVName(ALL_EL_EXTRA_DIM_Y));

	}

	@SuppressWarnings("unchecked")
	private void createDisplayPVs() {
		pvGetMaxFrames = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(MAX_FRAMES_SUFFIX));
		pvGetMCASize = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(MCA_SIZE_SUFFIX));

		pvsChannelEnable = new PV[numberOfDetectorChannels];
		for (int channel = 1; channel <= numberOfDetectorChannels; channel++) {
			pvsChannelEnable[channel - 1] = LazyPVFactory.newEnumPV(generatePVName(CHANNEL_ENABLE_TEMPLATE, channel),
					UPDATE_RBV.class);
		}

		// pvSetScalerUpdate =
		// LazyPVFactory.newEnumPV(generatePVName(SCALER_UPDATE_SUFFIX),UPDATE_CTRL.class);
		// pvGetScalerUpdate =
		// LazyPVFactory.newReadOnlyEnumPV(generatePVName(SCALER_UPDATE_RBV_SUFFIX),UPDATE_RBV.class);
		// pvScalerUpdatePeriod =
		// LazyPVFactory.newIntegerPV(generatePVName(SCALER_UPDATE_PERIOD_SUFFIX));
		// pvSetMCAUpdate =
		// LazyPVFactory.newEnumPV(generatePVName(MCA_UPDATE_SUFFIX),UPDATE_CTRL.class);
		// pvGetMCAUpdate =
		// LazyPVFactory.newReadOnlyEnumPV(generatePVName(MCA_UPDATE_RBV_SUFFIX),UPDATE_RBV.class);
		// pvMCAUpdatePeriod =
		// LazyPVFactory.newIntegerPV(generatePVName(MCA_UPDATE_PERIOD_SUFFIX));
		// pvSetSCAROIUpdate =
		// LazyPVFactory.newEnumPV(generatePVName(SCA_UPDATE_SUFFIX),UPDATE_CTRL.class);
		// pvGetSCAROIUpdate =
		// LazyPVFactory.newReadOnlyEnumPV(generatePVName(SCA_UPDATE_RBV_SUFFIX),UPDATE_RBV.class);
		// pvSCAROIUpdatePeriod =
		// LazyPVFactory.newIntegerPV(generatePVName(SCA_UPDATE_PERIOD_SUFFIX));
	}

	@SuppressWarnings("unchecked")
	private void createReadoutPVs() {
		pvsScalerWindow1 = new ReadOnlyPV[numberOfDetectorChannels];
		pvsScalerWindow2 = new ReadOnlyPV[numberOfDetectorChannels];

		pvsScaWin1Low = new PV[numberOfDetectorChannels];
		pvsScaWin1LowRBV = new ReadOnlyPV[numberOfDetectorChannels];
		pvsScaWin1High = new PV[numberOfDetectorChannels];
		pvsScaWin1HighRBV = new ReadOnlyPV[numberOfDetectorChannels];
		pvsScaWin2Low = new PV[numberOfDetectorChannels];
		pvsScaWin2LowRBV = new ReadOnlyPV[numberOfDetectorChannels];
		pvsScaWin2High = new PV[numberOfDetectorChannels];
		pvsScaWin2HighRBV = new ReadOnlyPV[numberOfDetectorChannels];

		pvsTime = new ReadOnlyPV[numberOfDetectorChannels];
		pvsResetTicks = new ReadOnlyPV[numberOfDetectorChannels];
		pvsResetCount = new ReadOnlyPV[numberOfDetectorChannels];
		pvsAllEvent = new ReadOnlyPV[numberOfDetectorChannels];
		pvsAllGood = new ReadOnlyPV[numberOfDetectorChannels];
		pvsPileup = new ReadOnlyPV[numberOfDetectorChannels];

		pvsGoodEventGradient = new ReadOnlyPV[numberOfDetectorChannels];
		pvsGoodEventOffset = new ReadOnlyPV[numberOfDetectorChannels];
		pvsInWinEventGradient = new ReadOnlyPV[numberOfDetectorChannels];
		pvsInWinEventOffset = new ReadOnlyPV[numberOfDetectorChannels];

		pvsLatestMCA = new ReadOnlyPV[numberOfDetectorChannels];
		pvsLatestMCASummed = new ReadOnlyPV[numberOfDetectorChannels];

		for (int channel = 1; channel <= numberOfDetectorChannels; channel++){
			pvsScalerWindow1[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(SCA_WIN1_SCAS_TEMPLATE,channel));
			pvsScalerWindow2[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(SCA_WIN2_SCAS_TEMPLATE,channel));

			pvsScaWin1Low[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(SCA_WIN1_LOW_BIN_TEMPLATE,channel));
			pvsScaWin1LowRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(SCA_WIN1_LOW_BIN_RBV_TEMPLATE,channel));
			pvsScaWin1High[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(SCA_WIN1_HIGH_BIN_TEMPLATE,channel));
			pvsScaWin1HighRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(SCA_WIN1_HIGH_BIN_RBV_TEMPLATE,channel));
			pvsScaWin2Low[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(SCA_WIN2_LOW_BIN_TEMPLATE,channel));
			pvsScaWin2LowRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(SCA_WIN2_LOW_BIN_RBV_TEMPLATE,channel));
			pvsScaWin2High[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(SCA_WIN2_HIGH_BIN_TEMPLATE,channel));
			pvsScaWin2HighRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(SCA_WIN2_HIGH_BIN_RBV_TEMPLATE,channel));

			pvsTime[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(SCA_TIME_SCAS_TEMPLATE,channel));
			pvsResetTicks[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(SCA_RESET_TICKS_SCAS_TEMPLATE,channel));
			pvsResetCount[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(SCA_RESET_COUNT_TEMPLATE,channel));
			pvsAllEvent[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(SCA_ALL_EVENT_TEMPLATE,channel));
			pvsAllGood[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(SCA_ALL_GOOD_TEMPLATE,channel));
			pvsPileup[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(SCA_PILEUP_TEMPLATE,channel));

			pvsGoodEventGradient[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(ALL_GOOD_EVT_GRAD_TEMPLATE,channel));
			pvsGoodEventOffset[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(ALL_GOOD_EVT_OFFSET_TEMPLATE,channel));
			pvsInWinEventGradient[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(IN_WIN_EVT_GRAD_TEMPLATE,channel));
			pvsInWinEventOffset[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(INWIN_EVT_OFFSET_TEMPLATE,channel));

			pvsLatestMCA[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(MCA_TEMPLATE,channel));
			pvsLatestMCASummed[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(MCA_SUM_TEMPLATE,channel));
		}
	}


	@SuppressWarnings("unchecked")
	private void createMCAPVs() {
		pvsROILLM = new PV[NUMBER_ROIs][numberOfDetectorChannels];
		pvsROIHLM = new PV[NUMBER_ROIs][numberOfDetectorChannels];
		pvsLatestROI = new ReadOnlyPV[NUMBER_ROIs][numberOfDetectorChannels];
		pvsROIs = new ReadOnlyPV[NUMBER_ROIs][numberOfDetectorChannels];

		for (int roi = 1; roi <= NUMBER_ROIs; roi++){
			for (int channel = 1; channel <= numberOfDetectorChannels; channel++){
				pvsROILLM[roi-1][channel-1] = LazyPVFactory.newIntegerPV(generatePVName(ROI_LOW_BIN_TEMPLATE,channel,roi));
				pvsROIHLM[roi-1][channel-1] = LazyPVFactory.newIntegerPV(generatePVName(ROI_HIGH_BIN_TEMPLATE,channel,roi));
				pvsLatestROI[roi-1][channel-1] = LazyPVFactory.newReadOnlyDoublePV(generatePVName(ROI_COUNT_TEMPLATE,channel,roi));
				pvsROIs[roi-1][channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(ROIS_TEMPLATE,channel,roi));
			}
		}
	}


	private String generatePVName(String suffix) {
		return epicsTemplate + suffix;
	}

	private String generatePVName(String template, int param1) {
		return epicsTemplate + String.format(template, param1);
	}

	private String generatePVName(String template, int param1,int param2) {
		return epicsTemplate + String.format(template, param1,param2);
	}

}
