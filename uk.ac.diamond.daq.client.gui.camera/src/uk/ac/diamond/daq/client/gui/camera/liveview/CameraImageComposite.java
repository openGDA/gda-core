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

package uk.ac.diamond.daq.client.gui.camera.liveview;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.client.gui.camera.event.DrawableRegionRegisteredEvent;
import uk.ac.diamond.daq.client.gui.camera.event.RegisterDrawableRegionEvent;
import uk.ac.diamond.daq.client.gui.camera.roi.ROIListener;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.event.ListenToConnectionEvent;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Instantiates the elements for the CameraConfiguration top area
 *
 * @author Maurzio Nagni
 */
public class CameraImageComposite extends Composite implements CameraPlotter {

	public static final String CAMERA_IMAGE_PLOTTING_SYSTEM_NAME = "CameraImagePlottingSystem";

	private static final Logger logger = LoggerFactory.getLogger(CameraImageComposite.class);

	private final LivePlottingComposite plottingComposite;

	/**
	 * Integrates a {@link LiveStreamConnection} into composite element. Can be used
	 * to display a fixed camera live stream.
	 *
	 * @param parent               where this Composite will live
	 * @param style                the composite style
	 * @param liveStreamConnection the streaming associated with this composite
	 * @throws GDAClientException if problems occur in the composite creation or
	 *                            with the live stream
	 */
	public CameraImageComposite(Composite parent, int style, LiveStreamConnection liveStreamConnection)
			throws GDAClientException {
		super(parent, style);
		//createClientGridDataFactory().applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		plottingComposite = new LivePlottingComposite(this, SWT.NONE, CAMERA_IMAGE_PLOTTING_SYSTEM_NAME, liveStreamConnection);
		plottingComposite.setShowTitle(true);

		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plottingComposite);

		// Registers the region into the camera
		SpringApplicationContextProxy.addDisposableApplicationListener(this, registerDrawableRegionListener(this));

		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(this);
		logger.debug("CameraImageComposite created");
	}

	/**
	 * Integrates a {@link LiveStreamConnection} into composite element but is
	 * handled by the {@code parent} composite. It does not explicitly accept
	 * directly a {@link LiveStreamConnection} because the internal code is
	 * listening for {@link ListenToConnectionEvent} published through Spring by the
	 * {@code parent}
	 *
	 *
	 * @param parent               where this Composite will live
	 * @param style                the composite style
	 * @throws GDAClientException if problems occur in the composite creation or
	 *                            with the live stream
	 */
	public CameraImageComposite(Composite parent, int style) throws GDAClientException {
		this(parent, style, null);
	}

	@Override
	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingComposite.getPlottingSystem();
	}

	@Override
	public IImageTrace getImageTrace() {
		return plottingComposite.getITrace();
	}

	private ApplicationListener<RegisterDrawableRegionEvent> registerDrawableRegionListener(Composite parent) {
		return new ApplicationListener<RegisterDrawableRegionEvent>() {
			@Override
			public void onApplicationEvent(RegisterDrawableRegionEvent event) {
				if (!event.haveSameParent(parent)) {
					return;
				}
				DrawableRegion roiSelectionRegion = new DrawableRegion(getPlottingSystem(), event.getColor(),
						ClientMessagesUtility.getMessage(event.getName()),
						new ROIListener(parent, plottingComposite, event.getRegionID()), event.getRegionID());
				roiSelectionRegion.setActive(true);
				publishEvent(new DrawableRegionRegisteredEvent(parent, ClientSWTElements.findParentUUID(parent), roiSelectionRegion));
			}
		};
	}
}