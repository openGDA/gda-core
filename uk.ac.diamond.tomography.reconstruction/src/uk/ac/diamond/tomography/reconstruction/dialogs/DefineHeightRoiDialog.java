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

package uk.ac.diamond.tomography.reconstruction.dialogs;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefineHeightRoiDialog extends BaseRoiDialog {
	private static final String PLOT_TITLE = "Select region for ROI";
	private static final String REGION_NAME_START = "Start";
	private static final Logger logger = LoggerFactory.getLogger(DefineHeightRoiDialog.class);
	private IRegion selectedRegion;
	private int[] startEnd;

	public DefineHeightRoiDialog(Shell parentShell, Dataset image) {
		super(parentShell, image, 1000, 800);
	}

	@Override
	public void doCreateControl(Composite parent) {
		setShellStyle(SWT.RESIZE);
		plottingSystem.setTitle(PLOT_TITLE);

		try {
			selectedRegion = plottingSystem.createRegion(REGION_NAME_START, RegionType.YAXIS);
			int height = image.getShape()[1];
			selectedRegion.setROI(new RectangularROI(0, height / 4, image.getShape()[0], height / 4, 0));
			selectedRegion.setShowPosition(true);
			selectedRegion.setLabel(REGION_NAME_START);
			selectedRegion.setLineWidth(4);
			plottingSystem.addRegion(selectedRegion);
		} catch (Exception e) {
			logger.error("Problem creating Region 'start'", e);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button btnselectFull = createButton(parent, IDialogConstants.SELECT_ALL_ID, "Select Full", false);
		btnselectFull.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int height = image.getShape()[0];
				selectedRegion.setROI(new RectangularROI(0, 0, image.getShape()[1], height, 0));
			}
		});

		Button btnRunRecon = createButton(parent, IDialogConstants.CLIENT_ID + 3, "Run Reconstruction", false);
		btnRunRecon.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startEnd = new int[2];
				if (plottingSystem.getRegions().contains(selectedRegion)) {
					IROI roi = selectedRegion.getROI();
					if (roi instanceof RectangularROI) {
						RectangularROI rec = (RectangularROI) roi;
						int[] intPoint = rec.getIntPoint();
						double[] endPointDbl = rec.getEndPoint();
						startEnd[0] = intPoint[1];
						startEnd[1] = (int) endPointDbl[1];
					}
				} else {
					startEnd[0] = 0;
					startEnd[1] = image.getShape()[1];
				}

				close();
			}
		});

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public int[] getStartEnd() {
		if (startEnd != null) {
			int height = image.getShape()[0];
			if (startEnd[0] < 0) {
				startEnd[0] = 0;
			} else if (startEnd[0] > height) {
				startEnd[0] = height;
			}
			if (startEnd[1] < 0) {
				startEnd[1] = 0;
			} else if (startEnd[1] > height) {
				startEnd[1] = height;
			}

		}
		return startEnd;
	}
}
