/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.composites;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.client.tomo.StatInfo;

/**
 *
 */
public class StatInfoComposite extends Composite {
	private static final String SIGMA = "Sigma:";
	private static final String MEAN = "Mean:";
	private static final String MAX = "Max:";
	private static final String MIN = "Min:";
	private static final String BOLD_TEXT_11 = "bold-text_11";
	private FontRegistry fontRegistry;
	private Label lblMinVal;
	private Label lblMaxVal;
	private Label lblMeanVal;
	private Label lblSigmaVal;
	private Label lblExposureTimeVal;

	public StatInfoComposite(Composite parent, int style) {
		super(parent, style);
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
		}
		this.setBackground(ColorConstants.white);
		GridLayout layout = new GridLayout(10, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		this.setLayout(layout);

		Label lblMin = new Label(this, SWT.RIGHT);
		lblMin.setBackground(ColorConstants.white);
		lblMin.setText(MIN);
		lblMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblMinVal = new Label(this, SWT.LEFT);
		lblMinVal.setBackground(ColorConstants.white);
		lblMinVal.setText("0.001");
		lblMinVal.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblMinVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblMax = new Label(this, SWT.RIGHT);
		lblMax.setText(MAX);
		lblMax.setBackground(ColorConstants.white);
		lblMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblMaxVal = new Label(this, SWT.LEFT);
		lblMaxVal.setText("65504");
		lblMaxVal.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblMaxVal.setBackground(ColorConstants.white);
		lblMaxVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblMean = new Label(this, SWT.RIGHT);
		lblMean.setBackground(ColorConstants.white);
		lblMean.setText(MEAN);
		lblMean.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblMeanVal = new Label(this, SWT.LEFT);
		lblMeanVal.setText("32753.2");
		lblMeanVal.setBackground(ColorConstants.white);
		lblMeanVal.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblMeanVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblSigma = new Label(this, SWT.RIGHT);
		lblSigma.setText(SIGMA);
		lblSigma.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblSigma.setBackground(ColorConstants.white);
		lblSigmaVal = new Label(this, SWT.LEFT);
		lblSigmaVal.setText("18918.6");
		lblSigmaVal.setBackground(ColorConstants.white);
		lblSigmaVal.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblSigmaVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblExposureTime = new Label(this, SWT.RIGHT);
		lblExposureTime.setText("Exposure: ");
		lblExposureTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblExposureTime.setBackground(ColorConstants.white);
		lblExposureTimeVal = new Label(this, SWT.LEFT);
		lblExposureTimeVal.setText("0.001");
		lblExposureTimeVal.setBackground(ColorConstants.white);
		lblExposureTimeVal.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblExposureTimeVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void updateLabelInUI(final Label lbl, final String val) {
		if (lbl != null && !lbl.isDisposed()) {
			lbl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					lbl.setText(val);
				}
			});
		}
	}

	public void updateValue(StatInfo statInfoEnum, String val) {
		switch (statInfoEnum) {
		case MIN:
			updateLabelInUI(lblMinVal, val);
			break;
		case MAX:
			updateLabelInUI(lblMaxVal, val);
			break;
		case MEAN:
			updateLabelInUI(lblMeanVal, val);
			break;
		case SIGMA:
			updateLabelInUI(lblSigmaVal, val);
			break;
		}
	}

	public void updateExposureTime(final double acqExposure) {
		if (lblExposureTimeVal != null && !lblExposureTimeVal.isDisposed()) {
			lblExposureTimeVal.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					lblExposureTimeVal.setText(String.format("%.3g (s)",acqExposure));
				}
			});
		}
	}
}
