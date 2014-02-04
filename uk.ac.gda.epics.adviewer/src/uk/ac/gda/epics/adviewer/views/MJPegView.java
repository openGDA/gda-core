/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.MJPeg;

public class MJPegView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(MJPegView.class);
	private MJPeg mJPeg;
	private ADController config;
	private String name="";
	private Image image=null;

	public MJPegView(ADController config, IConfigurationElement configurationElement) {
		this.config = config;
		try{
			name = configurationElement.getAttribute("name");
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

	public MJPegView() {
		super();
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
			mJPeg=createPartControlEx(parent);
		} catch (Exception e) {
			logger.error("Error creating MJPEGView", e);
		}
		
		if( image != null) {
			setTitleImage(image);
		}
		setPartName(name);

		try {
			createActions();
		} catch (Exception e) {
			logger.error("Error creating actions", e);
		}
		createMenu();
		createToolbar();
		createContextMenu();
		hookGlobalActions();
	}
	
	protected MJPeg createPartControlEx(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mJPeg = new MJPeg(composite_2, SWT.NONE);		
		mJPeg.setADController(config);
		mJPeg.showLeft(true);
		return mJPeg;
	}

	protected void hookGlobalActions() {
	}

	protected void createContextMenu() {
	}

	protected void createToolbar() {
	}

	protected void createMenu() {
	}
	
	protected void createActions() throws NotDefinedException {
		ADActionUtils actionUtils = new ADActionUtils();
		List<IAction> actions = new Vector<IAction>();			
		{
			actions.add(actionUtils.addAction("Fit Image to window", Ids.COMMANDS_FIT_IMAGE_TO_WINDOW, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));
			actions.add(actionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));
			actions.add(actionUtils.addAction("Rescale Live Image", Ids.COMMANDS_SET_LIVEVIEW_SCALE, Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, config.getServiceName()));
			actions.add( actionUtils.addShowViewAction("Show Histogram", Ids.COMMANDS_SHOW_HISTOGRAM_VIEW, config.getServiceName(), "Show Histogram view for selected camera"));
			actions.add( actionUtils.addShowViewAction("Show Raw Image", Ids.COMMANDS_SHOW_RAW_IMAGE_VIEW, config.getServiceName(), "Show Raw Image view for selected camera"));
		}	
		for (IAction iAction : actions) {
			getViewSite().getActionBars().getToolBarManager().add(iAction);
		}
	}
	
	@Override
	public void setFocus() {
		mJPeg.setFocus();
	}

	public static void reportErrorToUserAndLog(String s, Throwable th) {
		logger.error(s, th);
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
				s + ":" + th.getMessage());
	}

	public static void reportErrorToUserAndLog(String s) {
		logger.error(s);
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", s);
	}

	public void zoomToFit() {
		mJPeg.zoomFit();
	}

	@Override
	public void dispose() {
		if( image != null){
			image.dispose();
			image=null;
		}
		super.dispose();
	}

}