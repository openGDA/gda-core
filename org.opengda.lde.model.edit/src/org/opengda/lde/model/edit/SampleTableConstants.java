package org.opengda.lde.model.edit;

public interface SampleTableConstants {

	final static int COL_STATUS = 0;
	static final int COL_PROGRESS = COL_STATUS + 1;
	static final int COL_ACTIVE = COL_PROGRESS + 1;
	static final int COL_SAMPLE_NAME = COL_ACTIVE + 1;
	static final int COL_SAMPLE_X_START= COL_SAMPLE_NAME + 1;
	static final int COL_SAMPLE_X_STOP= COL_SAMPLE_X_START + 1;
	static final int COL_SAMPLE_X_STEP= COL_SAMPLE_X_STOP + 1;
	static final int COL_SAMPLE_Y_START= COL_SAMPLE_X_STEP + 1;
	static final int COL_SAMPLE_Y_STOP= COL_SAMPLE_Y_START + 1;
	static final int COL_SAMPLE_Y_STEP= COL_SAMPLE_Y_STOP + 1;
	static final int COL_SAMPLE_EXPOSURE= COL_SAMPLE_Y_STEP + 1;
	static final int COL_COMMAND = COL_SAMPLE_EXPOSURE + 1;
	static final int COL_COMMENT = COL_COMMAND + 1;
	static final int COL_DATA_FILE_COUNT = COL_COMMENT + 1;
	static final int COL_CELL = COL_DATA_FILE_COUNT + 1;
	static final int COL_STAGE = COL_CELL + 1;

	static final String STATUS = "Run\nState";
	static final String PROGRESS = "Progress";
	static final String ACTIVE = "Use";
	static final String SAMPLE_NAME = "Sample\nName";
	static final String SAMPLE_X_START= "Sample\nX Start";
	static final String SAMPLE_X_STOP= "Sample\nX Stop";
	static final String SAMPLE_X_STEP= "Sample\nX Step";
	static final String SAMPLE_Y_START= "Sample\nY Start";
	static final String SAMPLE_Y_STOP= "Sample\nY Stop";
	static final String SAMPLE_Y_STEP= "Sample\nY Step";
	static final String SAMPLE_EXPOSURE= "Sample\nExposure";
	static final String COMMAND = "Command";
	static final String COMMENT = "Comment";
	static final String DATA_FILE_COUNT = "Data\nCount";
	static final String CELL_ID = "Cell\nID";
	static final String STAGE_ID = "Stage\nID";
}
