/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.ILockTranslatable;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.view.LiveStreamView;

public class LineProfileRegionsButton implements LiveStreamViewCameraControlsExtension {

	private static final Logger logger = LoggerFactory.getLogger(LineProfileRegionsButton.class);
	private IPlottingSystem<Composite> sourcePlottingSystem;
	private String streamID;
	private String streamType;
	private IRegion xregion;
	private IRegion yregion;
	private boolean fixedRegionWidth;


	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {
		Label label = new Label(composite, SWT.NONE);
		label.setText("LP regions");
		Button button = new Button(composite, SWT.TOGGLE);
		button.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		button.setToolTipText("On/Off");

		IViewReference viewReference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findViewReference(LiveStreamView.ID, String.join("#", streamID, streamType));

		if (viewReference != null) {
			LiveStreamView liveStreamView = (LiveStreamView)viewReference.getView(false);
			sourcePlottingSystem = liveStreamView.getPlottingSystem();
		} else {
			throw new IllegalStateException("Could not find live stream view and therefore could not get plotting system");
		}

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					drawRegionX();
					drawRegionY();
					button.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
				} else {
					sourcePlottingSystem.removeRegion(xregion);
					sourcePlottingSystem.removeRegion(yregion);
					button.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
				}
			}
		});
	}

	private void drawRegionX() {
		RectangularROI xroi = new XAxisBoxROI(100, 0);
		try {
			xregion = sourcePlottingSystem.createRegion("x_profile_region", RegionType.XAXIS);
		} catch (Exception e) {
			logger.debug("Could not create line profile region x");
		}
		xregion.setROI(xroi);
		xregion.setUserRegion(false);
		sourcePlottingSystem.addRegion(xregion);

		if(fixedRegionWidth) {
			((ILockTranslatable)xregion).translateOnly(true);
		}

	}

	private void drawRegionY() {
		RectangularROI yroi = new YAxisBoxROI(100, 0);
		try {
			yregion = sourcePlottingSystem.createRegion("y_profile_region", RegionType.YAXIS);
		} catch (Exception e) {
			logger.debug("Could not create line profile region y");
		}
		yregion.setROI(yroi);
		yregion.setUserRegion(false);
		sourcePlottingSystem.addRegion(yregion);

		if(fixedRegionWidth) {
			((ILockTranslatable)yregion).translateOnly(true);
		}
	}

	public void setFixedRegionWidth(boolean fixedRegionWidth) {
		this.fixedRegionWidth = fixedRegionWidth;
	}

	public void setStreamID(String streamID) {
		this.streamID = streamID;
	}

	public void setStreamType(String streamType) {
		this.streamType = streamType;
	}

}
