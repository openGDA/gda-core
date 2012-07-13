/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
package uk.ac.gda.client.tomo.configuration.view.xviewer;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.swt.SWT;

/**
 *
 */
public class TomoConfigXViewerFactory extends XViewerFactory {

	private static final String COLUMN_PREFIX = "tomo.alignment";

	public static final String USER_ID_COL_ID = COLUMN_PREFIX + ".userId";

	public static final String FLAT_EXPOSURE_TIME_COL_ID = COLUMN_PREFIX + ".flatExposureTime";

	public static final String SAMPLE_EXPOSURE_TIME_COL_ID = COLUMN_PREFIX + ".sampleExposureTime";

	public static final String SAMPLE_DETECTOR_DIST_COL_ID = COLUMN_PREFIX + ".sampleDetectorDist";

	public static final String SAMPLE_DESC_COL_ID = COLUMN_PREFIX + ".sampleDesc";

	private static XViewerColumn User_Id_Col = new XViewerColumn(USER_ID_COL_ID, "User Id", 100, SWT.LEFT, true,
			SortDataType.String, false, "User Id");

	private static XViewerColumn Sample_Desc_Col = new XViewerColumn(SAMPLE_DESC_COL_ID, "Sample Description", 100,
			SWT.LEFT, true, SortDataType.String, false, "Sample Description");

	private static XViewerColumn Sample_Detector_Distance_Col = new XViewerColumn(SAMPLE_DETECTOR_DIST_COL_ID,
			"Sample Detector Distance", 100, SWT.LEFT, true, SortDataType.Float, false, "Sample Detector Distance");

	private static XViewerColumn Sample_Exposure_Time_Col = new XViewerColumn(SAMPLE_EXPOSURE_TIME_COL_ID,
			"Sample Exposure Time", 100, SWT.LEFT, true, SortDataType.Float, false, "Sample Exposure Time");

	private static XViewerColumn Flat_Exposure_Time_Col = new XViewerColumn(FLAT_EXPOSURE_TIME_COL_ID,
			"Flat Exposure Time", 100, SWT.LEFT, true, SortDataType.Float, false, "Flat Exposure Time");

	@Override
	public boolean isAdmin() {
		return false;
	}

	public TomoConfigXViewerFactory(String namespace) {
		super(namespace);
		registerColumn(User_Id_Col, Sample_Desc_Col, Sample_Detector_Distance_Col, Sample_Exposure_Time_Col,
				Flat_Exposure_Time_Col);
	}

}