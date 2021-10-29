/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
 */

package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.detector.xspress3.CAPTURE_MODE;
import uk.ac.gda.devices.detector.xspress3.ReadyForNextRow;
import uk.ac.gda.devices.detector.xspress3.UPDATE_CTRL;
import uk.ac.gda.devices.detector.xspress3.XSPRESS3_TRIGGER_MODE;

public class EpicsXspress3ControllerPvProvider {

	private static final Logger logger = LoggerFactory.getLogger(EpicsXspress3ControllerPvProvider.class);

	public static final int NUMBER_ROIS_DEFAULT = 10; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation
	public static final int MCA_SIZE_DEFAULT = 4096; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation

	// EPICS strings in camelcase are from the Quantum Detectors API, the ones in capitals are EPICS values

	// Control and Status
	private static final String ACQUIRE_SUFFIX = ":Acquire";
	private static final String ERASE_SUFFIX = ":ERASE";
	private static final String RESET_SUFFIX = ":RESET";
	private static final String ARRAY_COUNTER_SUFFIX = ":ArrayCounter";
	private static final String POINTS_PER_ROW_SUFFIX = ":PointsPerRow";
	private static final String READY_FOR_NEXT_ROW_SUFFIX = ":ReadyForNextRow_RBV";
	private static final String NUM_IMAGES_SUFFIX = ":NumImages";
	private static final String NUM_IMAGES_RBV_SUFFIX = ":NumImages_RBV";
	private static final String ROI_CALC_SUFFIX = ":CTRL_MCA_ROI";
	private static final String ROI_CALC_RBV_SUFFIX = ":CTRL_MCA_ROI_RBV";
	private static final String TRIG_MODE_SUFFIX = ":TriggerMode";
	private static final String TRIG_MODE_RBV_SUFFIX = ":TriggerMode_RBV";
	private static final String STATUS_MSG_SUFFIX = ":StatusMessage_RBV";
	private static final String STATUS_RBV_SUFFIX = ":DetectorState_RBV";
	private static final String FRAMES_PER_READ_SUFFIX = ":ArrayRate_RBV";
	private static final String FRAMES_AVAILABLE_SUFFIX = ":ArrayCounter_RBV";
	private static final String BUSY_SUFFIX = ":Acquire_RBV";
	private static final String[] UPDATE_ARRAYS_SUFFIXES = {":UPDATE_ARRAYS", ":UPDATE"};

	// Added a PV to make sure that the arrays have ben updated before to readout, this PV should be used only for step scans because the update of this PV is
	// only done every
	// 10 ms (check Adam)
	private static final String UPDATEARRAYS_FRAME_NUMBER_SUFFIX = ":AVAILABLE_FRAME";

	// System Configuration
	private static final String MAX_FRAMES_SUFFIX = ":NUM_FRAMES_CONFIG_RBV";
	private static final String MCA_SIZE_SUFFIX = ":MAX_SPECTRA_RBV";
	private static final String CONNECTION_STATUS = ":CONNECTED";
	private static final String MAX_NUM_CHANNELS = ":MAX_NUM_CHANNELS_RBV";

	// File creation PVs
	private static final String FILE_ENABLE_CALLBACKS = ":HDF5:EnableCallbacks";
	private static final String FILE_ARRAY_COUNTER = ":HDF5:ArrayCounter";
	private static final String FILE_CAPTURE_MODE = ":HDF5:FileWriteMode";
	private static final String STARTSTOP_FILE_WRITING = ":HDF5:Capture";
	private static final String FILE_WRITING_RBV = ":HDF5:Capture_RBV";

	private static final String FILE_TEMPLATE = ":HDF5:FileTemplate";
	private static final String FILE_PATH = ":HDF5:FilePath";
	private static final String FILE_PATH_RBV = ":HDF5:FilePath_RBV";

	private static final String FILE_PREFIX = ":HDF5:FileName";
	private static final String FILE_PREFIX_RBV = ":HDF5:FileName_RBV";

	private static final String NEXT_FILENUMBER = ":HDF5:FileNumber";
	private static final String FILE_AUTOINCREMENT = ":HDF5:AutoIncrement";
	private static final String FILE_NUMCAPTURE = ":HDF5:NumCapture";
	private static final String FILE_NUMCAPTURED_RBV = ":HDF5:NumCaptured_RBV";
	private static final String FULLFILENAME = ":HDF5:FullFileName_RBV";

	private static final String EXTRA_DIMS = ":HDF5:NumExtraDims";
	private static final String EXTRA_DIM_N = ":HDF5:ExtraDimSizeN";
	private static final String EXTRA_DIM_X = ":HDF5:ExtraDimSizeX";
	private static final String EXTRA_DIM_Y = ":HDF5:ExtraDimSizeY";
	private static final String DIM_ATT_DATASETS = ":HDF5:DimAttDatasets";

	// File performance PVs
	private static final String FILE_NDARRAYPORT = ":HDF5:NDArrayPort";
	private static final String FILE_ATTR = ":HDF5:StoreAttr";
	private static final String FILE_PERFORM = ":HDF5:StorePerform";
	private static final String FILE_NUMFRAMESCHUNKS = ":HDF5:NumFramesChunks";
	private static final String FILE_NDATTRIBUTECHUNK = ":HDF5:NDAttributeChunk";
	private static final String FILE_LAZYOPEN = ":HDF5:LazyOpen";
	private static final String FILE_HDFXML = ":HDF5:XMLFileName";
	private static final String FILE_POSITIONMODE = ":HDF5:PositionMode";
	private static final String DTC_NDARRAYPORT = ":DTC:NDArrayPort";

	// MCA and ROI
	private static final String ROI_LOW_BIN_TEMPLATE = ":C%1d_MCA_ROI%1d_LLM";  // channel (1-8),ROI (1-4)
	private static final String ROI_HIGH_BIN_TEMPLATE = ":C%1d_MCA_ROI%1d_HLM";// channel (1-8),ROI (1-4)
	private static final String ROI_COUNT_TEMPLATE = ";C%1d_SCA5:Value_RBV";// channel (1-8),ROI (1-4)
	private static final String ROIS_TEMPLATE = ":C%1d_SCA5:ArrayData_RBV.VAL"; // channel (1-8),ROI (1-4) this points towards a waveform
	private static final String MCA_TEMPLATE = ":ARR%1d:ArrayData";// channel (1-8) this points towards a waveform
	private static final String MCA_SUM_TEMPLATE = ":ARRSUM%1d:ArrayData";// channel (1-8) this points towards a waveform
	private static final String CHANNEL_ENABLE_TEMPLATE = ":C%1d_PluginControlVal";
	private static final String CHANNEL_ENABLE_V3_TEMPLATE = ":MCA%1d:Enable";

	// SCA
	private static final String SCA_WIN1_LOW_BIN_TEMPLATE = ":C%1d_SCA5_LLM";// channel (1-8)
	private static final String SCA_WIN1_LOW_BIN_RBV_TEMPLATE = ":C%1d_SCA5_LLM_RBV";// channel (1-8)
	private static final String SCA_WIN1_HIGH_BIN_TEMPLATE = ":C%1d_SCA5_HLM";// channel (1-8)
	private static final String SCA_WIN1_HIGH_BIN_RBV_TEMPLATE = ":C%1d_SCA5_HLM_RBV";// channel (1-8)
	private static final String SCA_WIN2_LOW_BIN_TEMPLATE = ":C%1d_SCA6_LLM";// channel (1-8)
	private static final String SCA_WIN2_LOW_BIN_RBV_TEMPLATE = ":C%1d_SCA6_LLM_RBV";// channel (1-8)
	private static final String SCA_WIN2_HIGH_BIN_TEMPLATE = ":C%1d_SCA6_HLM";// channel (1-8)
	private static final String SCA_WIN2_HIGH_BIN_RBV_TEMPLATE = ":C%1d_SCA6_HLM_RBV";// channel (1-8)

	private static final String SCA_WIN1_SCAS_TEMPLATE = ":C%1d_SCA5:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static final String SCA5_UPDATE_ARRAYS_SCAS_TEMPLATE = ":C%1d_SCA5:Update";// channel (1-8)  this points towards a waveform
	private static final String SCA_WIN2_SCAS_TEMPLATE = ":C%1d_SCA6:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform

	private static final String SCA_TIME_SCAS_TEMPLATE = ":C%1d_SCA0:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static final String SCA_RESET_TICKS_SCAS_TEMPLATE = ":C%1d_SCA1:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static final String SCA_RESET_COUNT_TEMPLATE = ":C%1d_SCA2:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static final String SCA_ALL_EVENT_TEMPLATE = ":C%1d_SCA3:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static final String SCA_ALL_GOOD_TEMPLATE = ":C%1d_SCA4:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform
	private static final String SCA_PILEUP_TEMPLATE = ":C%1d_SCA7:ArrayData_RBV.VAL";// channel (1-8)  this points towards a waveform

	// Deadtime correction parameters
	private static final String ALL_GOOD_EVT_GRAD_TEMPLATE = ":C%1d_DTC_AEG_RBV";// channel (1-8)
	private static final String ALL_GOOD_EVT_OFFSET_TEMPLATE = ":C%1d_DTC_AEO_RBV";// channel (1-8)
	private static final String IN_WIN_EVT_GRAD_TEMPLATE = ":C%1d_DTC_IWG_RBV";// channel (1-8)
	private static final String INWIN_EVT_OFFSET_TEMPLATE = ":C%1d_DTC_IWO_RBV";// channel (1-8)
	private static final String SQUASH_AUX_DIM_TEMPLATE = ":DTC:SquashAuxDim"; // since IOC version 3-0

	private static final String SCA_ARRAY_TEMPLATE = ":C%d_SCAS:%d:TSArrayValue"; // channel (1-8), scaler index (1-8) this points towards a waveform
	private static final String SCA_UPDATE_ARRAY_TEMPLATE = ":C%d_SCAS:TSControl"; // channel (1-8),
	private static final String SCA_ATTR_NAME_TEMPLATE = ":C%d_SCAS:%d:AttrName_RBV"; // channel (1-8),
	private static final String SCA_UPDATE_TIME_SERIES_TEMPLATE = ":C%d_SCAS:TS:TSAcquire"; // channel (1-8),

	private int numberRois = NUMBER_ROIS_DEFAULT;
	private int mcaSize = MCA_SIZE_DEFAULT;



	private TimeSeriesPv timeSeriesControls = null;

	// THis contains the values that can be set on current time series array PV
	private List<String> timeSeriesControlValues = Collections.emptyList();

	// Possible values the 'time series control' PV names and start/stop values can take
	// (VERSION_3 is newest IOC version, VERSION_1 the oldest)
	private enum TimeSeriesPv {
		VERSION_3(SCA_UPDATE_TIME_SERIES_TEMPLATE, "Acquire", "Done", ""),
		VERSION_2(SCA_UPDATE_ARRAY_TEMPLATE, "Erase/Start", "Stop", "Read"),
		VERSION_1(SCA5_UPDATE_ARRAYS_SCAS_TEMPLATE, "", "", "Enable"),
		NONE("", "", "", ""); // Oldest Xspress4 IOC doesn't have any time series PVs.

		private final String nameTemplate;
		private final String startValue;
		private final String stopValue;
		private final String updateValue;

		private TimeSeriesPv(String nameTemplate, String startValue, String stopValue, String updateValue) {
			this.nameTemplate = nameTemplate;
			this.startValue = startValue;
			this.stopValue = stopValue;
			this.updateValue = updateValue;
		}
		String getNameTemplate() {
			return nameTemplate;
		}
		String getStartValue() {
			return startValue;
		}
		String getStopValue() {
			return stopValue;
		}
		String getUpdateValue() {
			return updateValue;
		}
	}

	protected int numberOfDetectorChannels;

	// the shared PVs with the Controller which uses this object
	protected String epicsTemplate;
	public PV<ACQUIRE_STATE> pvAcquire;
	protected PV<ERASE_STATE> pvErase;
	protected PV<Integer> pvReset;
	protected PV<Integer> pvSetArrayCounter;
	protected PV<Integer> pvUpdate;
	protected PV<Integer> pvUpdateArraysAvailableFrame;
	protected PV<Integer> pvPointsPerRow;
	protected ReadOnlyPV<ReadyForNextRow> pvReadyForNextRow;
	protected PV<Integer> pvSetNumImages;
	protected ReadOnlyPV<Integer> pvGetNumImages;
	protected PV<UPDATE_CTRL> pvSetRoiCalc;
	protected ReadOnlyPV<UPDATE_RBV> pvGetRoiCalc;
	protected PV<XSPRESS3_TRIGGER_MODE> pvSetTrigMode;
	protected ReadOnlyPV<XSPRESS3_TRIGGER_MODE> pvGetTrigMode;
	protected ReadOnlyPV<String> pvGetStatusMsg;
	protected ReadOnlyPV<XSPRESS3_EPICS_STATUS> pvGetState;
	protected ReadOnlyPV<Integer> pvGetNumFramesPerReadout;
	protected ReadOnlyPV<Boolean> pvIsBusy;
	protected ReadOnlyPV<Integer> pvGetNumFramesAvailableToReadout;
	protected ReadOnlyPV<CONNECTION_STATE> pvIsConnected;
	protected ReadOnlyPV<Integer> pvGetMaxFrames;
	protected ReadOnlyPV<Integer> pvGetMCASize;
	public ReadOnlyPV<Integer> pvGetMaxNumChannels;
	protected PV<UPDATE_RBV>[] pvsChannelEnable;
	public ReadOnlyPV<Double[]>[] pvsScalerWindow1;
	private PV<Integer>[] pvsSCA5UpdateArrays;
	protected ReadOnlyPV<Double[]>[] pvsScalerWindow2;
	public ReadOnlyPV<Integer[]>[] pvsTime;
	public ReadOnlyPV<Integer[]>[] pvsResetTicks;
	public ReadOnlyPV<Integer[]>[] pvsResetCount;
	public ReadOnlyPV<Integer[]>[] pvsAllEvent;
	public ReadOnlyPV<Integer[]>[] pvsAllGood;
	public ReadOnlyPV<Integer[]>[] pvsPileup;
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
	public ReadOnlyPV<Double[]>[] pvsLatestMCA; //[channel]
	protected ReadOnlyPV<Double[]>[] pvsLatestMCASummed; //[channel]
	public PV<Integer>[][] pvsROILLM;// [roi][channel]
	protected PV<Integer>[][] pvsROIHLM;// [roi][channel]
	protected ReadOnlyPV<Double>[][] pvsLatestROI;  // [roi][channel]
	protected ReadOnlyPV<Double[]>[][] pvsROIs;   //[roi][channel]

	protected PV<Integer> pvSetFileArrayCounter;
	protected PV<UPDATE_CTRL> pvSetFileEnableCallbacks;
	protected PV<CAPTURE_MODE> pvSetFileCaptureMode;
	protected PV<CAPTURE_CTRL_RBV> pvStartStopFileWriting;
	protected ReadOnlyPV<CAPTURE_CTRL_RBV> pvIsFileWriting;
	protected ReadOnlyPV<Integer> pvGetFileWritingStatus;
	protected PV<String> pvSetFilePath;
	protected ReadOnlyPV<String> pvGetFilePath;
	protected PV<String> pvSetFileTemplate;
	protected PV<String> pvSetFilePrefix;
	protected ReadOnlyPV<String> pvGetFilePrefix;
	protected PV<Integer> pvNextFileNumber;
	protected PV<Integer> pvExtraDimensions;
	protected PV<Integer> pvExtraDimN;
	protected PV<Integer> pvExtraDimX;
	protected PV<Integer> pvExtraDimY;
	protected PV<Integer> pvDimAttDatasets;
	protected PV<Boolean> pvHDFAutoIncrement;
	protected PV<Integer> pvHDFNumCapture;
	protected PV<Integer> pvHDFNumCaptureRBV;
	protected ReadOnlyPV<String> pvHDFFullFileName;

	protected PV<String> pvHDFNDArrayPort;
	protected PV<Boolean> pvHDFAttributes;
	protected PV<Boolean> pvHDFPerformance;
	protected PV<Integer> pvHDFNumFramesChunks;
	protected PV<Integer> pvHDFNDAttributeChunk;
	protected PV<Boolean> pvHDFLazyOpen;
	protected PV<String> pvHDFXML;
	protected PV<Boolean> pvHDFPositionMode;

	protected PV<String> pvDtcInputArrayPort;

	protected ReadOnlyPV<String>[][] pvsSCAttrName;

	// PVs introduced in IOC v 3-0
	protected PV<UPDATE_RBV>[] pvsChannelEnableIocV3;
	protected PV<UPDATE_RBV> pvSquashAuxDim;

	/**
	 * Scaler index to use for different data types (new Xspress3 Epics interface)
	 */
	public enum ScalerIndex {
		TIME,
		RESET_TICKS,
		RESET_COUNTS,
		ALL_EVENT,
		ALL_GOOD,
		WINDOW_1,
		WINDOW_2,
		PILEUP;

		public int getIndex() {
			return ordinal() + 1;
		}
	}

	protected int getScalerIndexLength() {
		return ScalerIndex.values().length;
	}

	protected int getScalerIndexTimeIndex() {
		return ScalerIndex.TIME.getIndex();
	}

	protected int getScalerIndexResetTicksIndex() {
		return ScalerIndex.RESET_TICKS.getIndex();
	}

	protected int getScalerIndexResetCountsIndex() {
		return ScalerIndex.RESET_COUNTS.getIndex();
	}

	protected int getScalerIndexAllEventIndex() {
		return ScalerIndex.ALL_EVENT.getIndex();
	}

	protected int getScalerIndexAllGoodIndex() {
		return ScalerIndex.ALL_GOOD.getIndex();
	}

	protected int getScalerIndexWindow1Index() {
		return ScalerIndex.WINDOW_1.getIndex();
	}

	protected int getScalerIndexWindow2Index() {
		return ScalerIndex.WINDOW_2.getIndex();
	}

	protected int getScalerIndexPileupIndex() {
		return ScalerIndex.PILEUP.getIndex();
	}

	public EpicsXspress3ControllerPvProvider(String epicsTemplate, int numberOfDetectorChannels) {
		this.numberOfDetectorChannels = numberOfDetectorChannels;
		this.epicsTemplate = epicsTemplate;
	}

	public void createPVs() throws DeviceException {
		createControlPVs();
		createUpdatePV();
		createFileWritingPVs();
		createDisplayPVs();
		createReadoutPVs();
		createMCAPVs();
	}

	protected void createControlPVs() {
		pvAcquire = LazyPVFactory.newEnumPV(generatePVName(getAcquireSuffix()), ACQUIRE_STATE.class);
		pvErase = LazyPVFactory.newEnumPV(generatePVName(getEraseSuffix()), ERASE_STATE.class);
		pvReset = LazyPVFactory.newIntegerPV(generatePVName(getResetSuffix()));
		pvSetArrayCounter = LazyPVFactory.newIntegerPV(generatePVName(getArrayCounterSuffix()));
		pvUpdateArraysAvailableFrame = LazyPVFactory.newIntegerPV(generatePVName(getUpdateArraysFrameNumberSuffix()));
		pvPointsPerRow = LazyPVFactory.newIntegerPV(generatePVName(getPointsPerRowSuffix()));
		pvReadyForNextRow = LazyPVFactory.newEnumPV(generatePVName(getReadyForNextRowSuffix()), ReadyForNextRow.class);
		pvSetNumImages = LazyPVFactory.newIntegerPV(generatePVName(getNumImagesSuffix()));
		pvGetNumImages = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getNumImagesRbvSuffix()));
		pvSetRoiCalc = LazyPVFactory.newEnumPV(generatePVName(getRoiCalcSuffix()), UPDATE_CTRL.class);
		pvGetRoiCalc = LazyPVFactory.newReadOnlyEnumPV(generatePVName(getRoiCalcRbvSuffix()), UPDATE_RBV.class);
		pvSetTrigMode = LazyPVFactory.newEnumPV(generatePVName(getTrigModeSuffix()), XSPRESS3_TRIGGER_MODE.class);
		pvGetTrigMode = LazyPVFactory.newReadOnlyEnumPV(generatePVName(getTrigModeRbvSuffix()), XSPRESS3_TRIGGER_MODE.class);
		pvGetStatusMsg = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(getStatusMsgSuffix()));
		pvGetState = LazyPVFactory.newReadOnlyEnumPV(generatePVName(getStatusRbvSuffix()), XSPRESS3_EPICS_STATUS.class);
		pvGetNumFramesPerReadout = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getFramesPerReadSuffix()));
		pvGetNumFramesAvailableToReadout = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getFramesAvailableSuffix()));
		pvIsBusy = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(generatePVName(getBusySuffix()));
		pvIsConnected = LazyPVFactory.newReadOnlyEnumPV(generatePVName(getConnectionStatus()), CONNECTION_STATE.class);
	}

	protected void createUpdatePV() throws DeviceException {
		String updatePvName = Arrays.stream(UPDATE_ARRAYS_SUFFIXES)
			.map(this::generatePVName)
			.filter(this::pvExists)
			.findFirst()
			.orElseThrow(() -> new DeviceException("Could not create PV for updating arrays. PV with suffix matching "+Arrays.toString(UPDATE_ARRAYS_SUFFIXES)+" was not found"));

		logger.info("Using {} for 'update' PV", updatePvName);
		pvUpdate = LazyPVFactory.newIntegerPV(generatePVName(updatePvName));
	}

	protected void createFileWritingPVs() {
		pvSetFileEnableCallbacks = LazyPVFactory.newEnumPV(generatePVName(getFileEnabledCallbacks()), UPDATE_CTRL.class);
		pvSetFileArrayCounter = LazyPVFactory.newIntegerPV(generatePVName(getFileArrayCounter()));
		pvSetFileCaptureMode = LazyPVFactory.newEnumPV(generatePVName(getFileCaptureMode()), CAPTURE_MODE.class);
		pvStartStopFileWriting =  LazyPVFactory.newEnumPV(generatePVName(getStartStopFileWriting()),CAPTURE_CTRL_RBV.class);
		pvIsFileWriting = LazyPVFactory.newReadOnlyEnumPV(generatePVName(getFileWritingRbv()),CAPTURE_CTRL_RBV.class);
		pvSetFileTemplate = LazyPVFactory.newStringFromWaveformPV(generatePVName(getFileTemplate()));
		pvSetFilePath = LazyPVFactory.newStringFromWaveformPV(generatePVName(getFilePath()));
		pvGetFilePath = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(getFilePathRbv()));
		pvSetFilePrefix = LazyPVFactory.newStringFromWaveformPV(generatePVName(getFilePrefix()));
		pvGetFilePrefix = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(getFilePrefixRbv()));
		pvNextFileNumber = LazyPVFactory.newIntegerPV(generatePVName(getNextFileNumber()));
		pvHDFAutoIncrement = LazyPVFactory.newBooleanFromEnumPV(generatePVName(getFileAutoIncrement()));
		pvHDFNumCapture = LazyPVFactory.newIntegerPV(generatePVName(getFileNumCapture()));
		pvHDFNumCaptureRBV = LazyPVFactory.newIntegerPV(generatePVName(getFileNumCapturedRbv()));
		pvHDFNDArrayPort = LazyPVFactory.newStringPV(generatePVName(getFileNDArrayPort()));
		pvHDFAttributes = LazyPVFactory.newBooleanFromEnumPV(generatePVName(getFileAttr()));
		pvHDFPerformance = LazyPVFactory.newBooleanFromEnumPV(generatePVName(getFilePerform()));
		pvHDFNumFramesChunks = LazyPVFactory.newIntegerPV(generatePVName(getFileNumFramesChunks()));
		pvHDFNDAttributeChunk = LazyPVFactory.newIntegerPV(generatePVName(getFileNDAttributeChunk()));
		pvHDFLazyOpen = LazyPVFactory.newBooleanFromEnumPV(generatePVName(getFileLazyOpen()));
		pvHDFXML = LazyPVFactory.newStringFromWaveformPV(generatePVName(getFileHDFXml()));
		pvHDFPositionMode = LazyPVFactory.newBooleanFromEnumPV(generatePVName(getFilePositionMode()));

		pvHDFFullFileName = LazyPVFactory.newReadOnlyStringFromWaveformPV(generatePVName(getFullFilename()));
		pvExtraDimensions = LazyPVFactory.newIntegerPV(generatePVName(getExtraDims()));
		pvExtraDimN = LazyPVFactory.newIntegerPV(generatePVName(getExtraDimN()));
		pvExtraDimX = LazyPVFactory.newIntegerPV(generatePVName(getExtraDimX()));
		pvExtraDimY = LazyPVFactory.newIntegerPV(generatePVName(getExtraDimY()));
		pvDimAttDatasets = LazyPVFactory.newIntegerPV(generatePVName(getDimAttDatasets()));
	}

	@SuppressWarnings("unchecked")
	protected void createDisplayPVs() {
		pvGetMaxFrames = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getMaxFramesSuffix()));
		pvGetMCASize = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getMcaSizeSuffix()));
		pvGetMaxNumChannels = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getMaxNumChannels()));

		pvsChannelEnable = new PV[numberOfDetectorChannels];
		pvsChannelEnableIocV3 = new PV[numberOfDetectorChannels];

		for (int channel = 1; channel <= numberOfDetectorChannels; channel++) {
			pvsChannelEnable[channel - 1] = LazyPVFactory.newEnumPV(generatePVName(getChannelEnableTemplate(), channel),
					UPDATE_RBV.class);

			pvsChannelEnableIocV3[channel - 1] = LazyPVFactory.newEnumPV(generatePVName(getChannelEnableV3Template(), channel),
					UPDATE_RBV.class);
		}

		pvSquashAuxDim = LazyPVFactory.newEnumPV(generatePVName(getSquashAuxDimTemplate()), UPDATE_RBV.class);
		pvDtcInputArrayPort = LazyPVFactory.newStringPV(generatePVName(getDtcInputArrayPort()));
	}

	@SuppressWarnings("unchecked")
	protected void createReadoutPVs() {
		pvsScalerWindow1 = new ReadOnlyPV[numberOfDetectorChannels];
		pvsScalerWindow2 = new ReadOnlyPV[numberOfDetectorChannels];
		pvsSCA5UpdateArrays = new PV[numberOfDetectorChannels];
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

		int numScalers = getScalerIndexLength();
		pvsSCAttrName = new ReadOnlyPV[numberOfDetectorChannels][numScalers];

		boolean useTSArrayValueNames = hasTSArrayValueNames();
		boolean makeScalerAttrNames = hasScalerAttrNames();
		timeSeriesControls = getTimeSeriesPvObject();
		if (!timeSeriesControls.getNameTemplate().isEmpty()) {
			timeSeriesControlValues = getTimeSeriesControlValues(generatePVName(timeSeriesControls.getNameTemplate(), 1));
		}
		logger.info("Creating Xspress PVs : {} channels,  {} scalers", numberOfDetectorChannels, numScalers);
		logger.info("Using '{}' PV names for time series control; use 'TSArrayValue' PV names for scaler array data = {}; make AttrName PVs = {}",
				timeSeriesControls.getNameTemplate(), useTSArrayValueNames, makeScalerAttrNames);

		for (int channel = 1; channel <= numberOfDetectorChannels; channel++){

			if (useTSArrayValueNames) {
				pvsScalerWindow1[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexWindow1Index()));
				pvsScalerWindow2[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexWindow2Index()));
			} else {
				pvsScalerWindow1[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getScaWin1ScasTemplate(),channel));
				pvsScalerWindow2[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getScaWin2ScasTemplate(),channel));
			}

			if (makeScalerAttrNames) {
				for(int scalerNumber=1; scalerNumber<=numScalers; scalerNumber++) {
					pvsSCAttrName[channel-1][scalerNumber-1] = LazyPVFactory.newReadOnlyStringPV(generatePVName(getScaAttrNameTemplate(), channel, scalerNumber));
				}
			}

			pvsSCA5UpdateArrays[channel - 1] = LazyPVFactory.newIntegerPV(generatePVName(timeSeriesControls.getNameTemplate(), channel));

			pvsScaWin1Low[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getScaWin1LowBinTemplate(),channel));
			pvsScaWin1LowRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getScaWin1LowBinRbvTemplate(),channel));
			pvsScaWin1High[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getScaWin1HighBinTemplate(),channel));
			pvsScaWin1HighRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getScaWin1HighBinRbvTemplate(),channel));
			pvsScaWin2Low[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getScaWin2LowBinTemplate(),channel));
			pvsScaWin2LowRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getScaWin2LowBinRbvTemplate(),channel));
			pvsScaWin2High[channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getScaWin2HighBinTemplate(),channel));
			pvsScaWin2HighRBV[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getScaWin2HighBinRbvTemplate(),channel));

			if (useTSArrayValueNames) {
				pvsTime[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexTimeIndex())); // time
				pvsResetTicks[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexResetTicksIndex())); //reset ticks
				pvsResetCount[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexResetCountsIndex())); // reset count
				pvsAllEvent[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexAllEventIndex())); //all event
				pvsAllGood[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexAllGoodIndex())); // all good
				pvsPileup[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaArrayTemplate(), channel, getScalerIndexPileupIndex())); // pileup

			} else {
				pvsTime[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTimeScasTemplate(),channel));
				pvsResetTicks[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaResetTicksScasTemplate(),channel));
				pvsResetCount[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaResetCountTemplate(),channel));
				pvsAllEvent[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaAllEventTemplate(),channel));
				pvsAllGood[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaAllGoodTemplate(),channel));
				pvsPileup[channel-1] = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaPileupTemplate(),channel));
			}
			pvsGoodEventGradient[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getAllGoodEvtGradTemplate(),channel));
			pvsGoodEventOffset[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getAllGoodEvtOffsetTemplate(),channel));
			pvsInWinEventGradient[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getInWinEvtGradTemplate(),channel));
			pvsInWinEventOffset[channel-1] = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(getInWinEvtOffsetTemplate(),channel));

			pvsLatestMCA[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getMcaTemplate(),channel));
			pvsLatestMCASummed[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getMcaSumTemplate(),channel));
		}
	}


	public void startTimeSeries() throws IOException {
		controlTimeSeries(timeSeriesControls.getStartValue());
	}

	public void stopTimeSeries() throws IOException {
		controlTimeSeries(timeSeriesControls.getStopValue());
	}

	public void updateTimeSeries() throws IOException {
		controlTimeSeries(timeSeriesControls.getUpdateValue());
	}
	public TimeSeriesPv getTimeSeriesType() {
		return timeSeriesControls;
	}

	private void controlTimeSeries(String value) throws IOException {
		if (value.isEmpty()) {
			return;
		}
		logger.debug("Trying to set time series array control {} to : {}", generatePVName(timeSeriesControls.getNameTemplate()), value);
		int ind = timeSeriesControlValues.indexOf(value);
		if (ind > -1) {
			for(int i=0; i<numberOfDetectorChannels; i++) {
				pvsSCA5UpdateArrays[i].putNoWait(ind);
			}
		} else {
			logger.warn("Value {} not valid for {} - expected one of {}", value, generatePVName(timeSeriesControls.getNameTemplate()), timeSeriesControlValues);
		}
	}

	public List<String> getTimeSeriesControlValues() {
		return timeSeriesControlValues;
	}

	/**
	 * Return a list of all possible enum values from named PV.
	 * @param pvName
	 * @return list of allow values
	 */
	private List<String> getTimeSeriesControlValues(String pvName) {
		if (pvName.isEmpty()) {
			return Collections.emptyList();
		}
		try {
			EpicsController controller = EpicsController.getInstance();
			Channel ch = controller.createChannel(pvName);
			return Arrays.asList(controller.cagetLabels(ch));
		} catch (CAException | TimeoutException | InterruptedException e) {
			logger.error("Problem getting Enum values from {}", pvName, e);
		}
		return Collections.emptyList();
	}

	private boolean hasScalerAttrNames() {
		return pvExists(generatePVName(SCA_ATTR_NAME_TEMPLATE, 1, 1));
	}

	private boolean hasTSArrayValueNames() {
		return pvExists(generatePVName(SCA_ARRAY_TEMPLATE, 1, 1));
	}

	/**
	 * Look through the various versions of TimeSeriesPv and return object that matches
	 * the PVs on currently running IOC.
	 * @return TimeSeriesPv for currently running IOC
	 */
	private TimeSeriesPv getTimeSeriesPvObject() {
		for(var timeSeriesPv : TimeSeriesPv.values()) {
			String fullName = generatePVName(timeSeriesPv.getNameTemplate(), 1);
			if (pvExists(fullName)) {
				return timeSeriesPv;
			}
		}
		return TimeSeriesPv.NONE;
	}

	/**
	 * Try to open a channel to the named PV.
	 * @param pvName
	 * @return True if PV exists, false otherwise
	 */
	private boolean pvExists(String pvName) {
		try {
			EpicsChannelManager manager = new EpicsChannelManager();
			Channel channel = manager.createChannel(pvName, false);
			manager.creationPhaseCompleted();
			manager.tryInitialize(100);
			return channel.getConnectionState() == Channel.CONNECTED;
		} catch(IllegalStateException | CAException ex) {
			logger.error("Problem checking if PV {} exists", pvName, ex);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected void createMCAPVs() {
		pvsROILLM = new PV[getNumberRois()][numberOfDetectorChannels];
		pvsROIHLM = new PV[getNumberRois()][numberOfDetectorChannels];
		pvsLatestROI = new ReadOnlyPV[getNumberRois()][numberOfDetectorChannels];
		pvsROIs = new ReadOnlyPV[getNumberRois()][numberOfDetectorChannels];

		for (int roi = 1; roi <= getNumberRois(); roi++){
			for (int channel = 1; channel <= numberOfDetectorChannels; channel++){
				pvsROILLM[roi-1][channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getRoiLowBinTemplate(),channel,roi));
				pvsROIHLM[roi-1][channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getRoiHighBinTemplate(),channel,roi));
				pvsLatestROI[roi-1][channel-1] = LazyPVFactory.newReadOnlyDoublePV(generatePVName(getRoiCountTemplate(),channel,roi));
				pvsROIs[roi-1][channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV(generatePVName(getRoisTemplate(),channel,roi));
			}
		}
	}

	protected String generatePVName(String suffix) {
		return epicsTemplate + suffix;
	}

	protected String generatePVName(String template, int param1) {
		return epicsTemplate + String.format(template, param1);
	}

	protected String generatePVName(String template, int param1,int param2) {
		return epicsTemplate + String.format(template, param1,param2);
	}

	protected String getAcquireSuffix() {
		return ACQUIRE_SUFFIX;
	}

	protected String getEraseSuffix() {
		return ERASE_SUFFIX;
	}

	protected String getResetSuffix() {
		return RESET_SUFFIX;
	}

	protected String getArrayCounterSuffix() {
		return ARRAY_COUNTER_SUFFIX;
	}

	protected String getPointsPerRowSuffix() {
		return POINTS_PER_ROW_SUFFIX;
	}

	protected String getReadyForNextRowSuffix() {
		return READY_FOR_NEXT_ROW_SUFFIX;
	}

	protected String getNumImagesSuffix() {
		return NUM_IMAGES_SUFFIX;
	}

	protected String getNumImagesRbvSuffix() {
		return NUM_IMAGES_RBV_SUFFIX;
	}

	protected String getRoiCalcSuffix() {
		return ROI_CALC_SUFFIX;
	}

	protected String getRoiCalcRbvSuffix() {
		return ROI_CALC_RBV_SUFFIX;
	}

	protected String getTrigModeSuffix() {
		return TRIG_MODE_SUFFIX;
	}

	protected String getTrigModeRbvSuffix() {
		return TRIG_MODE_RBV_SUFFIX;
	}

	protected String getStatusMsgSuffix() {
		return STATUS_MSG_SUFFIX;
	}

	protected String getStatusRbvSuffix() {
		return STATUS_RBV_SUFFIX;
	}

	protected String getFramesPerReadSuffix() {
		return FRAMES_PER_READ_SUFFIX;
	}

	protected String getFramesAvailableSuffix() {
		return FRAMES_AVAILABLE_SUFFIX;
	}

	protected String getBusySuffix() {
		return BUSY_SUFFIX;
	}

	protected String getUpdateArraysFrameNumberSuffix() {
		return UPDATEARRAYS_FRAME_NUMBER_SUFFIX;
	}

	protected String getMaxFramesSuffix() {
		return MAX_FRAMES_SUFFIX;
	}

	protected String getMcaSizeSuffix() {
		return MCA_SIZE_SUFFIX;
	}

	protected String getConnectionStatus() {
		return CONNECTION_STATUS;
	}

	protected String getMaxNumChannels() {
		return MAX_NUM_CHANNELS;
	}

	protected String getFileEnabledCallbacks() {
		return FILE_ENABLE_CALLBACKS;
	}

	protected String getFileArrayCounter() {
		return FILE_ARRAY_COUNTER;
	}

	protected String getFileCaptureMode() {
		return FILE_CAPTURE_MODE;
	}

	protected String getStartStopFileWriting() {
		return STARTSTOP_FILE_WRITING;
	}

	protected String getFileWritingRbv() {
		return FILE_WRITING_RBV;
	}

	protected String getFileTemplate() {
		return FILE_TEMPLATE;
	}

	protected String getFilePath() {
		return FILE_PATH;
	}

	protected String getFilePathRbv() {
		return FILE_PATH_RBV;
	}

	protected String getFilePrefix() {
		return FILE_PREFIX;
	}

	protected String getFilePrefixRbv() {
		return FILE_PREFIX_RBV;
	}

	protected String getNextFileNumber() {
		return NEXT_FILENUMBER;
	}

	protected String getFileAutoIncrement() {
		return FILE_AUTOINCREMENT;
	}

	protected String getFileNumCapture() {
		return FILE_NUMCAPTURE;
	}

	protected String getFileNumCapturedRbv() {
		return FILE_NUMCAPTURED_RBV;
	}

	protected String getFullFilename() {
		return FULLFILENAME;
	}

	protected String getExtraDims() {
		return EXTRA_DIMS;
	}

	protected String getExtraDimN() {
		return EXTRA_DIM_N;
	}

	protected String getExtraDimX() {
		return EXTRA_DIM_X;
	}

	protected String getExtraDimY() {
		return EXTRA_DIM_Y;
	}

	protected String getDimAttDatasets() {
		return DIM_ATT_DATASETS;
	}

	protected String getFileNDArrayPort() {
		return FILE_NDARRAYPORT;
	}

	protected String getDtcInputArrayPort() {
		return DTC_NDARRAYPORT;
	}

	protected String getFileAttr() {
		return FILE_ATTR;
	}

	protected String getFilePerform() {
		return FILE_PERFORM;
	}

	protected String getFileNumFramesChunks() {
		return FILE_NUMFRAMESCHUNKS;
	}

	protected String getFileNDAttributeChunk() {
		return FILE_NDATTRIBUTECHUNK;
	}

	protected String getFileLazyOpen() {
		return FILE_LAZYOPEN;
	}

	protected String getFileHDFXml() {
		return FILE_HDFXML;
	}

	protected String getFilePositionMode() {
		return FILE_POSITIONMODE;
	}

	protected String getRoiLowBinTemplate() {
		return ROI_LOW_BIN_TEMPLATE;
	}

	protected String getRoiHighBinTemplate() {
		return ROI_HIGH_BIN_TEMPLATE;
	}

	protected String getRoiCountTemplate() {
		return ROI_COUNT_TEMPLATE;
	}

	protected String getRoisTemplate() {
		return ROIS_TEMPLATE;
	}

	protected String getMcaTemplate() {
		return MCA_TEMPLATE;
	}

	protected String getMcaSumTemplate() {
		return MCA_SUM_TEMPLATE;
	}

	protected String getChannelEnableTemplate() {
		return CHANNEL_ENABLE_TEMPLATE;
	}

	protected String getChannelEnableV3Template() {
		return CHANNEL_ENABLE_V3_TEMPLATE;
	}

	protected String getScaWin1LowBinTemplate() {
		return SCA_WIN1_LOW_BIN_TEMPLATE;
	}

	protected String getScaWin1LowBinRbvTemplate() {
		return SCA_WIN1_LOW_BIN_RBV_TEMPLATE;
	}

	protected String getScaWin1HighBinTemplate() {
		return SCA_WIN1_HIGH_BIN_TEMPLATE;
	}

	protected String getScaWin1HighBinRbvTemplate() {
		return SCA_WIN1_HIGH_BIN_RBV_TEMPLATE;
	}

	protected String getScaWin2LowBinTemplate() {
		return SCA_WIN2_LOW_BIN_TEMPLATE;
	}

	protected String getScaWin2LowBinRbvTemplate() {
		return SCA_WIN2_LOW_BIN_RBV_TEMPLATE;
	}

	protected String getScaWin2HighBinTemplate() {
		return SCA_WIN2_HIGH_BIN_TEMPLATE;
	}

	protected String getScaWin2HighBinRbvTemplate() {
		return SCA_WIN2_HIGH_BIN_RBV_TEMPLATE;
	}

	protected String getScaWin1ScasTemplate() {
		return SCA_WIN1_SCAS_TEMPLATE;
	}

	protected String getSca5UpdateArraysScasTemplate() {
		return SCA5_UPDATE_ARRAYS_SCAS_TEMPLATE;
	}

	protected String getScaWin2ScasTemplate() {
		return SCA_WIN2_SCAS_TEMPLATE;
	}

	protected String getScaTimeScasTemplate() {
		return SCA_TIME_SCAS_TEMPLATE;
	}

	protected String getScaResetTicksScasTemplate() {
		return SCA_RESET_TICKS_SCAS_TEMPLATE;
	}

	protected String getScaResetCountTemplate() {
		return SCA_RESET_COUNT_TEMPLATE;
	}

	protected String getScaAllEventTemplate() {
		return SCA_ALL_EVENT_TEMPLATE;
	}

	protected String getScaAllGoodTemplate() {
		return SCA_ALL_GOOD_TEMPLATE;
	}

	protected String getScaPileupTemplate() {
		return SCA_PILEUP_TEMPLATE;
	}

	protected String getAllGoodEvtGradTemplate() {
		return ALL_GOOD_EVT_GRAD_TEMPLATE;
	}

	protected String getAllGoodEvtOffsetTemplate() {
		return ALL_GOOD_EVT_OFFSET_TEMPLATE;
	}

	protected String getInWinEvtGradTemplate() {
		return IN_WIN_EVT_GRAD_TEMPLATE;
	}

	protected String getInWinEvtOffsetTemplate() {
		return INWIN_EVT_OFFSET_TEMPLATE;
	}

	protected String getSquashAuxDimTemplate() {
		return SQUASH_AUX_DIM_TEMPLATE;
	}

	protected String getScaArrayTemplate() {
		return SCA_ARRAY_TEMPLATE;
	}

	protected String getScaUpdateArrayTemplate() {
		return SCA_UPDATE_ARRAY_TEMPLATE;
	}

	protected String getScaAttrNameTemplate() {
		return SCA_ATTR_NAME_TEMPLATE;
	}

	public int getNumberRois() {
		return numberRois;
	}

	public void setNumberRois(int numberRois) {
		this.numberRois = numberRois;
	}

	public int getMcaSize() {
		return mcaSize;
	}

	public void setMcaSize(int mcaSize) {
		this.mcaSize = mcaSize;
	}
}
