/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.device.detector.xspress.ResGrades;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;

public class ResolutionGrade{
	private static final Map<String, Object> RES_ALL;
	private static final Map<String, Object> RES_NO_16;
	private ComboAndNumberWrapper resGrade;
	static {
		RES_ALL = new HashMap<String, Object>(3);
		RES_ALL.put("Sum all grades", ResGrades.NONE);
		RES_ALL.put("Individual grades", ResGrades.ALLGRADES);
		RES_ALL.put("Threshold", ResGrades.THRESHOLD);
		RES_NO_16 = new HashMap<String, Object>(3);
		RES_NO_16.put("Sum all grades", ResGrades.NONE);
		RES_NO_16.put("Threshold", ResGrades.THRESHOLD);
	}
	
	public ResolutionGrade(Composite parent) {
		createResGrade(parent);
	}

	private void createResGrade(Composite composite){
		Label resGradeLabel = new Label(composite, SWT.NONE);
		resGradeLabel.setText("Resolution Grade");
		resGradeLabel.setToolTipText("The resolution setting during calibration and XAS scans");
		resGradeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		resGrade = new ComboAndNumberWrapper(composite, SWT.READ_ONLY, Arrays.asList(new String[] { ResGrades.THRESHOLD }));
		resGrade.setItems(RES_ALL);
		resGrade.getValueField().setMaximum(15.99);
		resGrade.getValueField().setMinimum(0.0);
		resGrade.getValueField().setDecimalPlaces(1);
		resGrade.getValueField().setNumericValue(1d);
		resGrade.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}
	
	protected void updateResModeItems(boolean readoutRois) {
		Object val = resGrade.getValue();
		if (readoutRois)
			resGrade.setItems(RES_ALL);
		else
			resGrade.setItems(RES_NO_16);
		resGrade.setValue(val);
	}

	public ComboAndNumberWrapper getResGrade() {
		return resGrade;
	}

}