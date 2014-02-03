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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.Histogram;

public class HistogramView extends ViewPart{
	public static final String UK_AC_GDA_EPICS_ADVIEWER_COMMANDS_SET_EXPOSURE = "uk.ac.gda.epics.adviewer.commands.setExposure";

	private static final Logger logger = LoggerFactory.getLogger(HistogramView.class);

	private Histogram histogram;
	ADController config;

	private String name;

	private Image image;
	
	public HistogramView(ADController config, IConfigurationElement configurationElement) {
		this.config = config;
		name = configurationElement.getAttribute("name");
		try{
			String icon = configurationElement.getAttribute("icon");
			if( icon.isEmpty()){
				image = config.getTwoDarrayViewImageDescriptor().createImage();
			} else {
				URL iconURL = Platform.getBundle(configurationElement.getContributor().getName()).getResource(icon);
				ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(iconURL);
				image = imageDescriptor.createImage();
			}
		}catch (Exception e){
			logger.warn("Unable to get image for view",e);
		}
	}

	public HistogramView(){
	}


	@Override
	public void createPartControl(Composite parent) {

		try {
			if( config== null){
				String serviceName = getViewSite().getSecondaryId();
				if( StringUtils.isEmpty(serviceName))
					throw new RuntimeException("No secondary id given");
				config = (ADController)Activator.getNamedService(ADController.class, serviceName);
				name = serviceName + " Profile";
			}
			parent.setLayout(new FillLayout());

			histogram = new Histogram(this, parent, SWT.NONE);
			histogram.setADController(config);
			histogram.start();
			histogram.startStats();
			histogram.showLeft(true);
			List<IAction> actions = new Vector<IAction>();
			ADActionUtils actionUtils = new ADActionUtils();
			{
				actions.add(actionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));
				actions.add(actionUtils.addAction("Set LiveView Scale", Ids.COMMANDS_SET_LIVEVIEW_SCALE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));

				ICommandImageService service = (ICommandImageService) PlatformUI.getWorkbench().getService(ICommandImageService.class);
				ImageDescriptor imageDescriptor2 = service.getImageDescriptor(Ids.COMMANDS_SHOW_LIVEVIEW);
				
				actions.add( actionUtils.createShowViewAction("Show MJpeg", "uk.ac.gda.epics.adviewer.mjpegview", config.getServiceName(), "Show MJPEG view for selected camera", imageDescriptor2));
			}	
			for (IAction iAction : actions) {
				getViewSite().getActionBars().getToolBarManager().add(iAction);
			}
			
		} catch (Exception e) {
			logger.error("Error starting  areaDetectorProfileComposite", e);
		}
		if( image != null) {
			setTitleImage(image);
		}
		setPartName(name );
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
