/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.livecontrol.LiveControl;

/**
 * An extension that allows drawing a Cross or a Cross and Grid on top of video stream.
 */
public class EpicsCameraViewerGridControls implements LiveStreamViewCameraControlsExtension {
	private static final Logger logger = LoggerFactory.getLogger(EpicsCameraViewerGridControls.class);

	private LiveControl centreXControl;
	private LiveControl centreYControl;
	private LiveControl spacingControl;
	private LiveControl toggleControl;

	private Scannable spacingScannable;
	private Scannable centreXScannable;
	private Scannable centreYScannable;
	private Scannable toggleScannable;

	private String secondaryId;

	private IPlottingSystem<Composite> plottingSystem;
	private LiveStreamView liveStreamView;

	private int imageSizeX;
	private int imageSizeY;

	private boolean plainCross;
	private boolean hideCentreXControl;
	private boolean hideCentreYControl;
	private boolean hideToggleControl;
	private boolean hideSpacingControl;

	private Color lineColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	private static final String START_OF_NAME = "lattice";

	private int spacing;

	private int centreX;

	private int centreY;

	private String toggleState;

	private static final String TOGGLE_ON = "On";
	private static final String TOGGLE_OFF = "Off";


	public EpicsCameraViewerGridControls(LiveControl centreXControl, LiveControl centreYControl,
			LiveControl spacingControl, LiveControl toggleControl) {
		this.centreXControl = centreXControl;
		this.centreYControl = centreYControl;
		this.spacingControl = spacingControl;
		this.toggleControl = toggleControl;
	}

	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {
		try {
			imageSizeX = cameraControl.getImageSizeX();
			imageSizeY = cameraControl.getImageSizeY();
		} catch (DeviceException e) {
			logger.error("Could not get image size dimensions x and y from camera control", e);
		}

		// Adding observers so that grid will be synchronised with epics grid
		centreXScannable.addIObserver(this::updateRegions);
		centreYScannable.addIObserver(this::updateRegions);
		toggleScannable.addIObserver(this::toggleGrid);
		spacingScannable.addIObserver(this::updateRegions);

		if (!hideCentreXControl) {
			centreXControl.createControl(composite);
		}

		if (!hideCentreYControl) {
			centreYControl.createControl(composite);
		}

		if (!hideToggleControl) {
			toggleControl.createControl(composite);
		}

		if (!plainCross && !hideSpacingControl) {
			spacingControl.createControl(composite);
		}

		// Get live stream view reference and get plotting system
		IViewReference viewReference= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findViewReference(LiveStreamView.ID, secondaryId);

		if (viewReference != null) {
			liveStreamView = (LiveStreamView)viewReference.getView(false);
			plottingSystem = liveStreamView.getPlottingSystem();
		} else {
			MessageBox dialog = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Could not set plotting system");
			String message = "Could not find LiveStreamView with id: ".concat(secondaryId);
			dialog.setMessage(message);
			dialog.open();
			return;
		}

		// Initialise lattice
		try {
			toggleState = (String) toggleScannable.getPosition();
			centreX = (int)((double)centreXScannable.getPosition());
			centreY = (int)((double)centreYScannable.getPosition());
			spacing = (int)((double)spacingScannable.getPosition());
			drawRegions(spacing, centreX, centreY);
			if (toggleState.equals(TOGGLE_OFF)) {
				plottingSystem.getRegions().stream()
					.filter(r -> r.getName().startsWith(START_OF_NAME))
					.forEach(r -> r.setVisible(false));
			}
		} catch (DeviceException e) {
			logger.error("Could not get position of scannable", e);
		} catch (ExecutionException e) {
			logger.error("It was not possible to create one or more regions", e);
		}
	}

	private void drawRegions(int spacing, int centreX, int centreY) throws ExecutionException {
		if(plainCross) {
			drawCross(centreX, centreY);
		} else {
			drawGrid(spacing, centreX, centreY);
		}
	}

	/**
	 * Draws a cross centred at a set of coordinates <p>
	 * This method is using XAXIS_LINE and YAXIS_LINE type regions </br>
	 * together with XAxisBocROI that allows to set the thickness </br>
	 * of the cross
	 *
	 * @param lineCoordinates
	 * @throws ExecutionException
	 */
	private void drawCross(int centreX, int centreY) throws ExecutionException {
		// Draw cross
		IRegion xRegion;
		try {
			xRegion = plottingSystem.createRegion("latticeCrossVertical", RegionType.XAXIS_LINE);
		} catch (Exception e) {
			throw new ExecutionException("Exception on creating crosshairX", e);
		}
		xRegion.setRegionColor(lineColor);
		IROI roix = new XAxisBoxROI();
		roix.setPoint(centreX, 0);
		xRegion.setROI(roix);
		xRegion.setMobile(false);
		xRegion.setLineWidth(xRegion.getLineWidth()*4);
		plottingSystem.addRegion(xRegion);
		IRegion yRegion;
		try {
			yRegion = plottingSystem.createRegion("latticeCrossHorizontal", RegionType.YAXIS_LINE);
		} catch (Exception e) {
			throw new ExecutionException("Exception on creating crosshairY", e);
		}
		yRegion.setRegionColor(lineColor);
		IROI roiy = new YAxisBoxROI();
		roiy.setPoint(0, centreY);
		yRegion.setROI(roiy);
		yRegion.setMobile(false);
		yRegion.setLineWidth(yRegion.getLineWidth()*4);
		plottingSystem.addRegion(yRegion);
	}

	private void drawGrid(int spacing, int centreX, int centreY) throws ExecutionException {

		IRegion gridRegion;
		try {
			gridRegion = plottingSystem.createRegion("latticeDawnGrid", RegionType.CENTERED_GRID);
		} catch (Exception e) {
			throw new ExecutionException("Exception on creating region", e);
		}
		gridRegion.setRegionColor(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
		gridRegion.setAlpha(0);
		GridROI gridroi = new GridROI(0, 0, imageSizeX*2, imageSizeY*2, 0, spacing, spacing, true, false);
		gridroi.setMidPoint(new double[] {centreX, centreY});
		gridRegion.setROI(gridroi);
		plottingSystem.addRegion(gridRegion);
	}

	private void updateRegions(Object source, Object arg) {
		Display.getDefault().asyncExec(() -> {
			if (!(arg instanceof ScannablePositionChangeEvent event)) return;
			if (source == spacingScannable) {
				spacing = Integer.parseInt(event.newPosition.toString());
			} else if (source == centreXScannable) {
				centreX = Integer.parseInt(event.newPosition.toString());
			} else if (source == centreYScannable) {
				centreY = Integer.parseInt(event.newPosition.toString());
			}
			if (toggleState.equals(TOGGLE_ON)) showGrid();
		});
	}

	private void showGrid() {
		Display.getDefault().asyncExec(() -> {
			try {
				plottingSystem.clearRegions();
				drawRegions(spacing, centreX, centreY);
			}
			catch (ExecutionException e) {
				logger.error("It was not possible to create one or more regions", e);
			}
		});
	}

	private void toggleGrid(@SuppressWarnings("unused") Object source, Object arg) {
		Display.getDefault().asyncExec(() -> {
			if (!(arg instanceof ScannablePositionChangeEvent event)) return;
			toggleState = event.newPosition.toString();
			if (toggleState.equals(TOGGLE_ON)) {
				showGrid();
			} else {
				plottingSystem.getRegions().stream().filter(r -> r.getName().startsWith(START_OF_NAME))
						.forEach(r -> r.setVisible(false));
				}
		});
	}


	public void setSecondaryId(String secondaryId) {
		this.secondaryId = secondaryId;
	}

	public void setSpacingScannable(Scannable spacingScannable) {
		this.spacingScannable = spacingScannable;
	}

	public void setCentreXScannable(Scannable centreXScannable) {
		this.centreXScannable = centreXScannable;
	}

	public void setCentreYScannable(Scannable centreYScannable) {
		this.centreYScannable = centreYScannable;
	}

	public void setToggleScannable(Scannable toggleScannable) {
		this.toggleScannable = toggleScannable;
	}

	public void setPlainCross(boolean plainCross) {
		this.plainCross = plainCross;
	}

	public void setHideCentreXControl(boolean hideCentreXControl) {
		this.hideCentreXControl = hideCentreXControl;
	}

	public void setHideCentreYControl(boolean hideCentreYControl) {
		this.hideCentreYControl = hideCentreYControl;
	}

	public void setHideToggleControl(boolean hideToggleControl) {
		this.hideToggleControl = hideToggleControl;
	}

	public void setHideSpacingControl(boolean hideSpacingControl) {
		this.hideSpacingControl = hideSpacingControl;
	}

}
