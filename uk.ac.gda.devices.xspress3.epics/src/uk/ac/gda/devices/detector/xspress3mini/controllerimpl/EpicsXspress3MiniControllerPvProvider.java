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

package uk.ac.gda.devices.detector.xspress3mini.controllerimpl;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import uk.ac.gda.devices.detector.xspress3.XSPRESS3_MINI_TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.ACQUIRE_STATE;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.EpicsXspress3ControllerPvProvider;

public class EpicsXspress3MiniControllerPvProvider extends EpicsXspress3ControllerPvProvider {

	public static final int NUMBER_ROIS_MINI = 6; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation

	// Control and Status
	private static final String ROI_CALC_SUFFIX = ":MCA1:Enable";
	private static final String ROI_CALC_RBV_SUFFIX = ":MCA1:Enable_RBV";
	private static final String UPDATEARRAYS_SUFFIX = ":UPDATE_ARRAYS";

	// MCA and ROI
	private static final String ROI_LOW_BIN_TEMPLATE = ":C%1d_SCA5_LLM";  // ROI (1-6)
	private static final String ROI_BIN_SIZE_TEMPLATE = ":C%1d_SCA5_HLM";// ROI (1-6)
	private static final String ROI_COUNT_TEMPLATE = ":C%1d_SCA5:Value_RBV";// channel (1-8),ROI (1-4)
	private static final String ROIS_TEMPLATE = ":C%1d_SCAS:6:TSArrayValue"; // channel (1-8),ROI (1-4) this points towards a waveform
	private static final String CHANNEL_ENABLE_TEMPLATE = ":MCA%1d:Enable";

	private static final String SCA_ARRAY_TEMPLATE = ":C%d_SCAS:%d:TSArrayValue"; // channel (1-2), scaler index (1-9) this points towards a waveform
	private static final String SCA_UPDATE_ARRAY_TEMPLATE = ":C%d_SCAS:TS:TSAcquire"; // channel (1-2),
	private static final String SCA_ATTR_NAME_TEMPLATE = ":C%d_SCAS:%d:AttrName_RBV"; // channel (1-2),

	private static final String SCA_TEMPLATE = ":C%d_SCA%d:Value_RBV";
	private static final String ACQUIRE_TIME_TEMPLATE = ":AcquireTime";

	// the shared PVs with the Controller which uses this object
	protected ReadOnlyPV<Double> pvGetRoiCalcMini;
	protected PV<ACQUIRE_STATE>[] pvsSCA5UpdateArraysMini;
	protected ReadOnlyPV<Integer[]>[] pvsTotalTime;
	protected PV<Integer> pvEraseMini;
	protected PV<Integer>[][] pvsROISize;// [roi][channel]

	protected ReadOnlyPV<String>[][] pvsSCAttrName;

	protected PV<XSPRESS3_MINI_TRIGGER_MODE> pvSetTrigModeMini;
	protected ReadOnlyPV<XSPRESS3_MINI_TRIGGER_MODE> pvGetTrigModeMini;

	protected PV<Double> pvAcquireTime;

	/**
	 * Scaler index to use for different data types (new Xspress3Mini Epics interface)
	 */
	public enum ScalerIndex {
		TIME,
		RESET_TICKS,
		RESET_COUNTS,
		ALL_EVENT,
		ALL_GOOD,
		WINDOW_1,
		WINDOW_2,
		PILEUP,
		TOTAL_TIME;

		public int getIndex() {
			return ordinal();
		}
	}

	@Override
	protected int getScalerIndexTimeIndex() {
		return ScalerIndex.TIME.getIndex();
	}

	@Override
	protected int getScalerIndexResetTicksIndex() {
		return ScalerIndex.RESET_TICKS.getIndex();
	}

	@Override
	protected int getScalerIndexResetCountsIndex() {
		return ScalerIndex.RESET_COUNTS.getIndex();
	}

	@Override
	protected int getScalerIndexAllEventIndex() {
		return ScalerIndex.ALL_EVENT.getIndex();
	}

	@Override
	protected int getScalerIndexAllGoodIndex() {
		return ScalerIndex.ALL_GOOD.getIndex();
	}

	@Override
	protected int getScalerIndexWindow1Index() {
		return ScalerIndex.WINDOW_1.getIndex();
	}

	@Override
	protected int getScalerIndexWindow2Index() {
		return ScalerIndex.WINDOW_2.getIndex();
	}

	@Override
	protected int getScalerIndexPileupIndex() {
		return ScalerIndex.PILEUP.getIndex();
	}

	protected int getScalerIndexTotalTimeIndex() {
		return ScalerIndex.TOTAL_TIME.getIndex();
	}

	public EpicsXspress3MiniControllerPvProvider(String epicsTemplate, int numberOfDetectorChannels){
		super(epicsTemplate, numberOfDetectorChannels);
		setNumberRois(NUMBER_ROIS_MINI);
	}

	@Override
	protected void createControlPVs() {
		super.createControlPVs();
		pvEraseMini = LazyPVFactory.newIntegerFromEnumPV(generatePVName(getEraseSuffix()));
		pvGetRoiCalcMini = LazyPVFactory.newReadOnlyDoublePV(generatePVName(getRoiCalcRbvSuffix()));
		pvSetTrigModeMini = LazyPVFactory.newEnumPV(generatePVName(getTrigModeSuffix()), XSPRESS3_MINI_TRIGGER_MODE.class);
		pvGetTrigModeMini = LazyPVFactory.newReadOnlyEnumPV(generatePVName(getTrigModeRbvSuffix()), XSPRESS3_MINI_TRIGGER_MODE.class);
		pvAcquireTime = LazyPVFactory.newDoublePV(generatePVName(getAcquireTimeTemplate()));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void createReadoutPVs() {
		super.createReadoutPVs();

		pvsSCA5UpdateArraysMini = new PV[numberOfDetectorChannels];
		pvsTotalTime = new ReadOnlyPV[numberOfDetectorChannels];

		for (int channel = 1; channel <= numberOfDetectorChannels; channel++){
			pvsSCA5UpdateArraysMini[channel-1] = LazyPVFactory.newEnumPV(generatePVName(getScaUpdateArrayTemplate(), channel), ACQUIRE_STATE.class);

			pvsScalerWindow1[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV (generatePVName(getScaTemplate(), channel, getScalerIndexWindow1Index()));
			pvsScalerWindow2[channel-1] = LazyPVFactory.newReadOnlyDoubleArrayPV (generatePVName(getScaTemplate(), channel, getScalerIndexWindow2Index()));

			pvsTime[channel-1]          = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexTimeIndex()));
			pvsResetTicks[channel-1]    = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexResetTicksIndex()));
			pvsResetCount[channel-1]    = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexResetCountsIndex()));
			pvsAllEvent[channel-1]      = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexAllEventIndex()));
			pvsAllGood[channel-1]       = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexAllGoodIndex()));
			pvsPileup[channel-1]        = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexPileupIndex()));
			pvsTotalTime[channel-1]     = LazyPVFactory.newReadOnlyIntegerArrayPV(generatePVName(getScaTemplate(), channel, getScalerIndexTotalTimeIndex()));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void createMCAPVs() {
		super.createMCAPVs();

		pvsROISize = new PV[getNumberRois()][numberOfDetectorChannels];

		for (int roi = 1; roi <= getNumberRois(); roi++){
			for (int channel = 1; channel <= numberOfDetectorChannels; channel++){
				pvsROISize[roi-1][channel-1] = LazyPVFactory.newIntegerPV(generatePVName(getRoiBinSizeTemplate(),channel,roi));        //:C%1d_MCA_ROI%1d_HLM - not sure this is right
			}
		}
	}

	@Override
	protected String getRoiCalcSuffix() {
		return ROI_CALC_SUFFIX;
	}

	@Override
	protected String getRoiCalcRbvSuffix() {
		return ROI_CALC_RBV_SUFFIX;
	}

	@Override
	protected String getRoiLowBinTemplate() {
		return ROI_LOW_BIN_TEMPLATE;
	}

	protected String getRoiBinSizeTemplate() {
		return ROI_BIN_SIZE_TEMPLATE;
	}

	@Override
	protected String getRoiCountTemplate() {
		return ROI_COUNT_TEMPLATE;
	}

	@Override
	protected String getRoisTemplate() {
		return ROIS_TEMPLATE;
	}

	@Override
	protected String getChannelEnableTemplate() {
		return CHANNEL_ENABLE_TEMPLATE;
	}

	@Override
	protected String getScaArrayTemplate() {
		return SCA_ARRAY_TEMPLATE;
	}

	@Override
	protected String getScaUpdateArrayTemplate() {
		return SCA_UPDATE_ARRAY_TEMPLATE;
	}

	@Override
	protected String getScaAttrNameTemplate() {
		return SCA_ATTR_NAME_TEMPLATE;
	}

	@Override
	protected int getScalerIndexLength() {
		return ScalerIndex.values().length;
	}


	public String getScaTemplate() {
		return SCA_TEMPLATE;
	}

	protected String getAcquireTimeTemplate() {
		return ACQUIRE_TIME_TEMPLATE;
	}
}
