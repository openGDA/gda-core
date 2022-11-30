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

package uk.ac.gda.epics.adviewer.views;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import gda.device.detector.nxdetector.roi.PlotServerROISelectionProvider;
import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.ADControllerFactory;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.TwoDArray;
import uk.ac.gda.epics.adviewer.composites.tomove.PlottingSystemIRegionPlotServerConnector;
import uk.ac.gda.ui.event.PartAdapter;

public class TwoDArrayView extends ViewPart implements InitializingBean {
	public static final String ID = "uk.ac.gda.epics.adviewer.twodArrayView";

	private static final Logger logger = LoggerFactory.getLogger(TwoDArrayView.class);
	private TwoDArray twoDArray;

	private ADController adController;
	private IPartListener2 partListener;
	private PlottingSystemIRegionPlotServerConnector plotServerConnector;
	private String name;
	private Image image;

	private String serviceName;

	public TwoDArrayView() {
		super();
	}

	public TwoDArrayView(String serviceName) {
		super();
		this.serviceName = serviceName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (adController == null)
			throw new Exception("Config is null");
	}

	protected ADController getAdController() {
		return adController;
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			if (adController == null) {
				if (StringUtils.isEmpty(serviceName))
					serviceName = getViewSite().getSecondaryId();
				if (StringUtils.isEmpty(serviceName))
					throw new RuntimeException("No secondary id given");
				logger.info("TwoDArrayView.createPartControl() serviceName=" + serviceName);
				try {
					adController = ADControllerFactory.getInstance().getADController(serviceName);
				} catch (Exception e) {
					logger.error("Error getting ADController", e);
					throw new RuntimeException("Error getting ADController see log for details");
				}
				name = adController.getDetectorName() + " Array";
			} else {
				logger.info("TwoDArrayView.createPartControl() adController.getServiceName=" + adController.getServiceName());
			}
			parent.setLayout(new FillLayout());

			twoDArray = new TwoDArray(this, parent, SWT.NONE, adController);
			twoDArray.showLeft(true);
			partListener = new PartAdapter() {

				@Override
				public void partVisible(IWorkbenchPartReference partRef) {
					if (partRef.getPart(false) == TwoDArrayView.this)
						twoDArray.setViewIsVisible(true);
				}

				@Override
				public void partHidden(IWorkbenchPartReference partRef) {
					if (partRef.getPart(false) == TwoDArrayView.this)
						twoDArray.setViewIsVisible(false);
				}

			};
			getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);

			if (adController.isConnectToPlotServer()) {
				plotServerConnector = new PlottingSystemIRegionPlotServerConnector(this.twoDArray.getPlottingSystem(),
						PlotServerROISelectionProvider.getGuiName(adController.getDetectorName()));
			}
			twoDArray.restore(name);
			createActions();
			if (image != null) {
				setTitleImage(image);
			}
			setPartName(name);

		} catch (Exception e) {
			logger.error("Error configuring twoDArray composite", e);
		}
	}

	protected void createActions() throws NotDefinedException {
		final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(ADActionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE,
					Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));
		toolBarManager.add(ADActionUtils.addAction("Set LiveView Scale", Ids.COMMANDS_SET_LIVEVIEW_SCALE,
					Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));

		createShowViewAction();
	}

	protected void createShowViewAction() {
		final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(ADActionUtils.addShowViewAction("Show Stats", HistogramView.ID, adController.getServiceName(),
				"Show stats view for selected camera", Activator.getHistogramViewImage()));
		toolBarManager.add(ADActionUtils.addShowViewAction("Show MJPeg", MJPegView.ID, adController.getServiceName(),
				"Show MJPeg view for selected camera", Activator.getMJPegViewImage()));
	}

	@Override
	public void setFocus() {
		twoDArray.setFocus();
	}

	@Override
	public void dispose() {
		if (twoDArray != null)
			twoDArray.save(name);
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (partListener != null) {
			getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
			partListener = null;
		}
		if (plotServerConnector != null) {
			plotServerConnector.disconnect();
			plotServerConnector = null;
		}
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return this.twoDArray.getPlottingSystem();
		}
		return super.getAdapter(clazz);
	}

	public TwoDArray getTwoDArray() {
		return twoDArray;
	}

}