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

import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
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

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.composites.TwoDArray;
import uk.ac.gda.epics.adviewer.composites.tomove.PlottingSystemIRegionPlotServerConnector;

public class TwoDArrayView extends ViewPart implements InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(TwoDArrayView.class);

	private TwoDArray twoDArray;
	ADController config;

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

		parent.setLayout(new FillLayout());
		twoDArray = new TwoDArray(this, parent, SWT.NONE);
		try {
			twoDArray.setADController(config);
		} catch (Exception e) {
			logger.error("Error configuring twoDArray composite", e);
		}
		twoDArray.showLeft(true);
		if( image != null) {
			setTitleImage(image);
		}
		setPartName(name ); 

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
	}

	protected void createActions() {
/*		List<IAction> actions = new Vector<IAction>();			
		{
			IAction action = new Action("", IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					twoDArray.showLeft(!twoDArray.getShowLeft());
					this.setChecked(twoDArray.getShowLeft());
				}
			};
			action.setChecked(twoDArray.getShowLeft());
			action.setToolTipText("Show/Hide Left Panel");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/show_left.png"));
			actions.add(action);
		}	
		for (IAction iAction : actions) {
			getViewSite().getActionBars().getToolBarManager().add(iAction);
		}*/
		
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
