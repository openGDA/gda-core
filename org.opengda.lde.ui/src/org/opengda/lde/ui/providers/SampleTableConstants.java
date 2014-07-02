package org.opengda.lde.ui.providers;

public interface SampleTableConstants {

	final static int COL_STATUS = 0;
	static final int COL_ACTIVE = COL_STATUS + 1;
	static final int COL_SAMPLE_NAME = COL_ACTIVE + 1;
	static final int COL_CELL_ID = COL_SAMPLE_NAME + 1;
	static final int COL_VISIT_ID = COL_CELL_ID + 1;
	static final int COL_EMAIL = COL_VISIT_ID + 1;
	static final int COL_COMMAND = COL_EMAIL + 1;
	static final int COL_COMMENT = COL_COMMAND + 1;
	static final int COL_START_DATE = COL_COMMENT + 1;
	static final int COL_END_DATE = COL_START_DATE + 1;
	static final int COL_MAIL_COUNT = COL_END_DATE + 1;
	static final int COL_DATA_FILE_COUNT = COL_MAIL_COUNT + 1;

	static final String STATUS = "Status";
	static final String ACTIVE = "Active";
	static final String SAMPLE_NAME = "Sample Name";
	static final String CELL_ID = "Cell ID";
	static final String VISIT_ID = "Visit ID";
	static final String EMAIL = "Email";
	static final String COMMAND = "Command";
	static final String COMMENT = "Comment";
	static final String START_DATE = "Start Date";
	static final String END_DATE = "End Date";
	static final String MAIL_COUNT = "Mail Count";
	static final String DATA_FILE_COUNT = "Data File Count";
}
