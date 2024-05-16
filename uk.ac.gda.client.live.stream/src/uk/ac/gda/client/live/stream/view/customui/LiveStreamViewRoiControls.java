/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.api.camera.CameraControl;

/**
 * Controls for live stream view that allows the ROI in area detector plugin to be set and retrieved.
 */
public class LiveStreamViewRoiControls extends AbstractLiveStreamViewCustomUi {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewRoiControls.class);
	private static final String ICON_PLUGIN = "uk.ac.gda.client.live.stream";

	private CameraControl cameraControl;

	public LiveStreamViewRoiControls(CameraControl cameraControl) {
		Objects.requireNonNull(cameraControl, "Camera control must not be null");
		this.cameraControl = cameraControl;
	}

	@Override
	public void createUi(Composite composite) {
		Composite mainComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(mainComposite);

		// Set detector ROI from ROI in plot window
		Text setDetectorRoilabel = new Text(mainComposite, SWT.NONE);
		setDetectorRoilabel.setText("Set detector ROI");

		Button setRoiDetector = new Button(mainComposite, SWT.PUSH);
		setRoiDetector.setImage(createImage("reset_view.png"));
		setRoiDetector.setToolTipText("Set detector ROI from plot ROI");
		setRoiDetector.addSelectionListener(widgetSelectedAdapter(e -> setDetectorRoi()));

		// Set ROI in plot window from ROI on detector
		Text setPlotRoilabel = new Text(mainComposite, SWT.NONE);
		setPlotRoilabel.setText("Get detector ROI");

		Button setRoiPlot = new Button(mainComposite, SWT.PUSH);
		setRoiPlot.setImage(createImage("reset_view.png"));
		setRoiPlot.setToolTipText("Get detector ROI and show it on the plot");
		setRoiPlot.addSelectionListener(widgetSelectedAdapter(e -> setPlotRoi()));

		setPlotRoi();

		composite.addDisposeListener(l -> {
			mainComposite.dispose();
			setRoiDetector.getImage().dispose();
			setRoiPlot.getImage().dispose();
		});

	}

	private Image createImage(String fileName) {
		final ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(ICON_PLUGIN, "icons/" + fileName);
		return descriptor.createImage();
	}
	public void setCameraControl(CameraControl cameraControl) {
		this.cameraControl = cameraControl;
	}

	private void setDetectorRoi() {
		IROI roiFromPlot = getRoiFromPlot();
		if (roiFromPlot == null) {
			logger.info("Cannot set ROI on detector - no ROI has been set in the plot!");
			return;
		}
		try {
			setRoiOnDetector(roiFromPlot);
		} catch (DeviceException e) {
			logger.error("Problem setting detector ROI from plot ROI", e);
		}
	}

	private void setPlotRoi() {
		try {
			IROI roi = getRoiFromDetector();
			setRoiOnPlot(roi);
		} catch (Exception e) {
			logger.error("Problem setting ROI on plot using ROI from detector", e);
		}
	}
	private IROI getRoiFromPlot() {
		Collection<IRegion> regions = getPlottingSystem().getRegions(RegionType.BOX);
		if (regions == null) {
			return null;
		}
		return regions.stream().map(IRegion::getROI).findFirst().orElse(null);
	}

	private void setRoiOnPlot(IROI roi) throws Exception {
		IPlottingSystem<Composite> plotter = getPlottingSystem();
		plotter.clearRegions();
		IRegion plotRegion = plotter.createRegion("ROI", RegionType.BOX);
		plotRegion.setROI(roi);
		plotter.addRegion(plotRegion);
	}

	private IROI getRoiFromDetector() throws DeviceException {
		int[] roiVals = cameraControl.getRoi();
		return new RectangularROI(roiVals[0], roiVals[1], roiVals[2], roiVals[3], 0);
	}

	private void setRoiOnDetector(IROI roi) throws DeviceException {
		IRectangularROI bounds = roi.getBounds();
		cameraControl.setRoi((int) roi.getPointX(), (int) roi.getPointY(), bounds.getIntLength(0),
				bounds.getIntLength(1));
	}
}
