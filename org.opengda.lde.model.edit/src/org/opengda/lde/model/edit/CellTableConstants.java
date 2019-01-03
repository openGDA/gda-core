package org.opengda.lde.model.edit;

public interface CellTableConstants {


	static final int COL_CELL_NAME = 0;
	static final int COL_CELL_ID = COL_CELL_NAME + 1;
	static final int COL_VISIT_ID = COL_CELL_ID + 1;
	static final int COL_CALIBRANT_NAME= COL_VISIT_ID + 1;
	static final int COL_CALIBRANT_X= COL_CALIBRANT_NAME + 1;
	static final int COL_CALIBRANT_Y= COL_CALIBRANT_X + 1;
	static final int COL_CALIBRANT_EXPOSURE= COL_CALIBRANT_Y + 1;
	static final int COL_NUMBER_OF_SAMPLES= COL_CALIBRANT_EXPOSURE + 1;
	static final int COL_ENV_SCANNABLE_NAMES= COL_NUMBER_OF_SAMPLES + 1;
	static final int COL_START_DATE= COL_ENV_SCANNABLE_NAMES + 1;
	static final int COL_END_DATE= COL_START_DATE + 1;
	static final int COL_EMAIL= COL_END_DATE + 1;
	static final int COL_AUTO_EMAIL= COL_EMAIL + 1;

	static final String CELL_NAME = "Name";
	static final String CELL_ID = "ID";
	static final String VISIT_ID = "Visit";
	static final String CALIBRANT_NAME = "Calibrant";
	static final String CALIBRANT_X = "X";
	static final String CALIBRANT_Y = "Y";
	static final String CALIBRANT_EXPOSURE = "Exposure";
	static final String NUMBER_OF_SAMPLES ="No.Samples";
	static final String ENV_SCANNABLE_NAMES= "Scannables";
	static final String START_DATE = "Start Date";
	static final String END_DATE = "End Date";
	static final String EMAIL = "Email";
	static final String AUTO_EMAILING = "Auto Email";
}
