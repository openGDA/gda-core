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

import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
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
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.Histogram;

public class HistogramView extends ViewPart{
	public static final String Id = "uk.ac.gda.epics.adviewer.histogramview";
	public static final String UK_AC_GDA_EPICS_ADVIEWER_COMMANDS_SET_EXPOSURE = "uk.ac.gda.epics.adviewer.commands.setExposure";
	private static final Logger logger = LoggerFactory.getLogger(HistogramView.class);
	private Histogram histogram;
	private ADController adController;
	private String name;
	private Image image;
	private boolean createdViaExtendedConstructor=false;	
	
	public HistogramView(ADController adController, IConfigurationElement configurationElement) {
		this.adController = adController;
		name = configurationElement.getAttribute("name");
		try{
			String icon = configurationElement.getAttribute("icon");
			if( icon.isEmpty()){
				image = adController.getTwoDarrayViewImageDescriptor().createImage();
			} else {
				URL iconURL = Platform.getBundle(configurationElement.getContributor().getName()).getResource(icon);
				ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(iconURL);
				image = imageDescriptor.createImage();
			}
		}catch (Exception e){
			logger.warn("Unable to get image for view",e);
		}
		createdViaExtendedConstructor = true;
	}

	public HistogramView(){
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			if( adController== null){
				String serviceName = getViewSite().getSecondaryId();
				if( StringUtils.isEmpty(serviceName))
					throw new RuntimeException("No secondary id given");
				try {
					adController = ADControllerFactory.getInstance().getADController(serviceName);
				} catch (Exception e) {
					logger.error("Error getting ADController",e);
					throw new RuntimeException("Error getting ADController see log for details");
				}
				name = adController.getDetectorName() + " Stats";
				
			}
			parent.setLayout(new FillLayout());

			histogram = new Histogram(this, parent, SWT.NONE);
			histogram.setADController(adController);
			histogram.start();
			histogram.startStats();
			histogram.showLeft(true);
			
			createActions();

			if( image != null) {
				setTitleImage(image);
			}
			setPartName(name );
			
		} catch (Exception e) {
			logger.error("Error starting  histogram view", e);
		}
	}
	
	protected void createActions() throws NotDefinedException {
		if(!createdViaExtendedConstructor){
			List<IAction> actions = new Vector<IAction>();
			{
				actions.add(ADActionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));
				actions.add( ADActionUtils.addShowViewAction("Show MPeg", MJPegView.Id, adController.getServiceName(), "Show MPeg view for selected camera"));
				actions.add( ADActionUtils.addShowViewAction("Show Array", TwoDArrayView.Id, adController.getServiceName(), "Show array view for selected camera"));				
			}	
			for (IAction iAction : actions) {
				getViewSite().getActionBars().getToolBarManager().add(iAction);
			}
		}
	}
	
	@Override
	public void setFocus() {
		histogram.setFocus();
	}

	@Override
	public void dispose() {
		if( image != null){
			image.dispose();
			image=null;
		}
		super.dispose();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return this.histogram.getPlottingSystem();
		}
		return super.getAdapter(clazz);
	}

}