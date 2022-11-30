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
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.ADControllerFactory;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.Histogram;

public class HistogramView extends ViewPart {
	public static final String ID = "uk.ac.gda.epics.adviewer.histogramview";
	public static final String UK_AC_GDA_EPICS_ADVIEWER_COMMANDS_SET_EXPOSURE = "uk.ac.gda.epics.adviewer.commands.setExposure";
	private static final Logger logger = LoggerFactory.getLogger(HistogramView.class);
	private Histogram histogram;
	private ADController adController;
	private String name;
	private Image image;
	private String serviceName;

	public HistogramView() {
	}

	public HistogramView(String serviceName) {
		super();
		this.serviceName = serviceName;
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
				logger.info("HistogramView.createPartControl() serviceName={}", serviceName);
				try {
					adController = ADControllerFactory.getInstance().getADController(serviceName);
				} catch (Exception e) {
					logger.error("Error getting ADController", e);
					throw new RuntimeException("Error getting ADController see log for details");
				}
				name = adController.getDetectorName() + " Stats";
			} else {
				logger.info("HistogramView.createPartControl() adController.getServiceName=" + adController.getServiceName());
			}
			parent.setLayout(new FillLayout());

			histogram = new Histogram(this, parent, SWT.NONE);
			histogram.setADController(adController);
			histogram.start();
			histogram.startStats();
			histogram.showLeft(true);

			createActions();

			if (image != null) {
				setTitleImage(image);
			}
			setPartName(name);

		} catch (Exception e) {
			logger.error("Error starting  histogram view", e);
		}
	}

	protected void createActions() throws NotDefinedException {
		final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(ADActionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE,
				Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));

		createShowViewAction();
	}

	protected void createShowViewAction() {
		final IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(ADActionUtils.addShowViewAction("Show MJPeg", MJPegView.ID, adController.getServiceName(),
				"Show MJPeg view for selected camera", Activator.getMJPegViewImage()));
		toolBarManager.add(ADActionUtils.addShowViewAction("Show Array", TwoDArrayView.ID, adController.getServiceName(),
				"Show array view for selected camera",Activator.getTwoDArrayViewImage()));
	}

	@Override
	public void setFocus() {
		histogram.setFocus();
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return this.histogram.getPlottingSystem();
		}
		return super.getAdapter(clazz);
	}

}