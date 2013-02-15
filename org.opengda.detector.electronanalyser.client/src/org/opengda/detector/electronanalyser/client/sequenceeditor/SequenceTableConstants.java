package org.opengda.detector.electronanalyser.client.sequenceeditor;

public interface SequenceTableConstants {

	final static int COL_STATUS = 0;
	static final int COL_ENABLED = COL_STATUS + 1;
	static final int COL_REGION_NAME = COL_ENABLED + 1;
	static final int COL_LENS_MODE = COL_REGION_NAME + 1;
	static final int COL_PASS_ENERGY = COL_LENS_MODE + 1;
	static final int COL_EXCITATION_ENERGY = COL_PASS_ENERGY + 1;
	static final int COL_ENERGY_MODE = COL_EXCITATION_ENERGY + 1;
	static final int COL_LOW_ENERGY = COL_ENERGY_MODE + 1;
	static final int COL_HIGH_ENERGY = COL_LOW_ENERGY + 1;
	static final int COL_ENERGY_STEP = COL_HIGH_ENERGY + 1;
	static final int COL_STEP_TIME = COL_ENERGY_STEP + 1;
	static final int COL_STEPS = COL_STEP_TIME + 1;
	static final int COL_TOTAL_TIME = COL_STEPS + 1;
	static final int COL_X_CHANNEL_FROM = COL_TOTAL_TIME + 1;
	static final int COL_X_CHANNEL_TO = COL_X_CHANNEL_FROM + 1;
	static final int COL_Y_CHANNEL_FROM = COL_X_CHANNEL_TO + 1;
	static final int COL_Y_CHANNEL_TO = COL_Y_CHANNEL_FROM + 1;
	static final int COL_SLICES = COL_Y_CHANNEL_TO + 1;
	static final int COL_MODE = COL_SLICES + 1;

	static final String STATUS = "Status";
	static final String MODE = "Mode";
	static final String SLICES = "Slices";
	static final String Y_CHANNEL_TO = "Y-Channel To";
	static final String Y_CHANNEL_FROM = "Y-Channel from";
	static final String X_CHANNEL_TO = "X-Channel To";
	static final String X_CHANNEL_FROM = "X-Channel From";
	static final String TOTAL_TIME = "Total Time";
	static final String STEPS = "Steps";
	static final String STEP_TIME = "Step Time";
	static final String ENERGY_STEP = "Energy Step";
	static final String HIGH_ENERGY = "High Energy";
	static final String LOW_ENERGY = "Low Energy";
	static final String ENERGY_MODE = "Energy Mode";
	static final String EXCITATION_ENERGY = "Excitation Energy";
	static final String PASS_ENERGY = "Pass Energy";
	static final String LENS_MODE = "Lens Mode";
	static final String REGION_NAME = "Region Name";
	static final String ENABLED = "Enabled";

}
