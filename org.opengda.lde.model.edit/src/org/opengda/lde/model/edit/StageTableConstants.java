package org.opengda.lde.model.edit;

public interface StageTableConstants {

	final static int COL_STAGE_ID = 0;
	static final int COL_DETECTOR_X= COL_STAGE_ID + 1;
	static final int COL_DETECTOR_Y= COL_DETECTOR_X + 1;
	static final int COL_DETECTOR_Z= COL_DETECTOR_Y + 1;
	static final int COL_CAMERA_X = COL_DETECTOR_Z + 1;
	static final int COL_CAMERA_Y = COL_CAMERA_X + 1;
	static final int COL_CAMERA_Z = COL_CAMERA_Y + 1;
	static final int COL_NUMBER_OF_CELLS = COL_CAMERA_Z + 1;

	static final String STAGE_ID = "Stage ID";
	static final String DETECTOR_X= "Detector X";
	static final String DETECTOR_Y= "Detector Y";
	static final String DETECTOR_Z= "Detector Z relative";
	static final String CAMERA_X= "Camera X";
	static final String CAMERA_Y= "Camera Y";
	static final String CAMERA_Z= "Camera Z";
	static final String NUMBER_OF_CELLS="Number of cells";
}
