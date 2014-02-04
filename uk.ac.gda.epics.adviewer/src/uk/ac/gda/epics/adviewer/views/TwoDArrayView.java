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

import gda.device.detector.nxdetector.roi.PlotServerROISelectionProvider;

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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.TwoDArray;
import uk.ac.gda.epics.adviewer.composites.tomove.PlottingSystemIRegionPlotServerConnector;
import uk.ac.gda.epics.adviewer.Activator;

public class TwoDArrayView extends ViewPart implements InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(TwoDArrayView.class);
	private TwoDArray twoDArray;
	private ADController config;
	private IPartListener2 partListener;
	private PlottingSystemIRegionPlotServerConnector plotServerConnector;
	private String name;
	private Image image;
	
	public TwoDArrayView(ADController config, IConfigurationElement configurationElement) {
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

	@Override
	public void afterPropertiesSet() throws Exception {
		if( config == null)
			throw new Exception("Config is null");
	}

	@Override
	public void createPartControl(Composite parent) {

		if( config== null){
			String serviceName = getViewSite().getSecondaryId();
			if( StringUtils.isEmpty(serviceName))
				throw new RuntimeException("No secondary id given");
			config = (ADController)Activator.getNamedService(ADController.class, serviceName);
			name = serviceName + " MJPeg";
		}
		
		parent.setLayout(new FillLayout());
		try {
			twoDArray = new TwoDArray(this, parent, SWT.NONE);
			twoDArray.setADController(config);
			twoDArray.showLeft(true);
			partListener = new IPartListener2() {
				
				@Override
				public void partVisible(IWorkbenchPartReference partRef) {
					if( partRef.getPart(false) ==  TwoDArrayView.this)
						twoDArray.setViewIsVisible(true);
				}
				
				@Override
				public void partOpened(IWorkbenchPartReference partRef) {
				}
				
				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {
				}
				
				@Override
				public void partHidden(IWorkbenchPartReference partRef) {
					if( partRef.getPart(false) ==  TwoDArrayView.this)
						twoDArray.setViewIsVisible(false);
				}
				
				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {
				}
				
				@Override
				public void partClosed(IWorkbenchPartReference partRef) {
				}
				
				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {
				}
				
				@Override
				public void partActivated(IWorkbenchPartReference partRef) {
				}
			};
			getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
			
			if( config.isConnectToPlotServer()){
				plotServerConnector = new PlottingSystemIRegionPlotServerConnector(this.twoDArray.getPlottingSystem(), PlotServerROISelectionProvider.getGuiName(config.getDetectorName()));
			}
			twoDArray.restore(name);
			createActions();		
		} catch (Exception e) {
			logger.error("Error configuring twoDArray composite", e);
		}
		if( image != null) {
			setTitleImage(image);
		}
		setPartName(name ); 
	}
	
	protected void createActions() throws NotDefinedException {
		ADActionUtils actionUtils = new ADActionUtils();
		List<IAction> actions = new Vector<IAction>();
		{
			actions.add(actionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));
			actions.add(actionUtils.addAction("Set LiveView Scale", Ids.COMMANDS_SET_LIVEVIEW_SCALE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));
		}	
		for (IAction iAction : actions) {
			getViewSite().getActionBars().getToolBarManager().add(iAction);
		}
	}
	
	@Override
	public void setFocus() {
		twoDArray.setFocus();
	}

	@Override
	public void dispose() {
		twoDArray.save(name);
		if( image != null){
			image.dispose();
			image=null;
		}
		if( partListener != null){
			getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
			partListener = null;
		}
		if( plotServerConnector != null){
			plotServerConnector.disconnect();
			plotServerConnector = null;
		}
		super.dispose();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return this.twoDArray.getPlottingSystem();
		}
		return super.getAdapter(clazz);
	}
	
}