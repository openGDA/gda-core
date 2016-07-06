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

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefineRoiDialog extends BaseRoiDialog {
	private static final String BTN_FULL_IMG = "Full Image";

	private static final String REGION_NAME = "SelectROI";

	private static final Logger logger = LoggerFactory.getLogger(DefineRoiDialog.class);

	public DefineRoiDialog(Shell parentShell, Dataset image) {
		super(parentShell, image, 800, 700);
	}

	private int[] roi;

	@Override
	public void doCreateControl(Composite plotComposite) {
		plottingSystem.setTitle("Select region for ROI");
		IRegion boxRegion = null;
		try {
			boxRegion = plottingSystem.createRegion(REGION_NAME, RegionType.BOX);
		} catch (Exception e) {
			logger.error("Problem creating plotting system.", e);
		}

		if (boxRegion != null) {
			boxRegion.addROIListener(new IROIListener.Stub() {
				@Override
				public void roiChanged(ROIEvent evt) {
					ITrace next = plottingSystem.getTraces().iterator().next();
					if (next instanceof IImageTrace) {
						if (evt.getROI() instanceof RectangularROI) {
							RectangularROI rectRoi = (RectangularROI) evt.getROI();

							int[] startPos = rectRoi.getIntPoint();
							double[] endPointDbl = rectRoi.getEndPoint();
							int[] endPos = new int[] { (int) endPointDbl[0], (int) endPointDbl[1] };

							int[] newRoi = new int[] { startPos[0], startPos[1], endPos[0], endPos[1] };
							setRoiValues(newRoi);
						}
					}
				}
			});
		}

	}

	protected void setRoiValues(int[] newRoi) {
		roi = newRoi;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button btnSelectAll = createButton(parent, IDialogConstants.SELECT_ALL_ID, BTN_FULL_IMG, false);
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				roi = new int[] { 0, 0, image.getShape()[0], image.getShape()[1] };
				setReturnCode(OK);
				DefineRoiDialog.this.close();
			}
		});
	}

	public int[] getRoi() {
		return roi;
	}
}
