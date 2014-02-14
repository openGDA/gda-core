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

import java.util.List;
import java.util.Vector;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
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
import uk.ac.gda.epics.adviewer.ADControllerFactory;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.Ids;
import uk.ac.gda.epics.adviewer.composites.MJPeg;

public class MJPegView extends ViewPart {
	public static final String Id = "uk.ac.gda.epics.adviewer.mpegview";
	private static final Logger logger = LoggerFactory.getLogger(MJPegView.class);
	private MJPeg mJPeg;
	private ADController adController;
	private String name = "";
	private Image image = null;
	private String serviceName;

	public MJPegView() {
		super();
	}

	public MJPegView(String serviceName) {
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
				try {
					adController = ADControllerFactory.getInstance().getADController(serviceName);
				} catch (Exception e) {
					logger.error("Error getting ADController", e);
					throw new RuntimeException("Error getting ADController see log for details");
				}
				name = adController.getDetectorName() + " MPeg";
			}

			parent.setLayout(new FillLayout());

			mJPeg = createPartControlEx(parent);

			createActions();
			createMenu();
			createToolbar();
			createContextMenu();
			hookGlobalActions();

			if (image != null) {
				setTitleImage(image);
			}
			setPartName(name);

		} catch (Exception e) {
			logger.error("Error creating MJPEGView", e);
		}

	}

	protected MJPeg createPartControlEx(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mJPeg = new MJPeg(composite_2, SWT.NONE);
		mJPeg.setADController(adController);
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
		List<IAction> actions = new Vector<IAction>();
		{
			actions.add(ADActionUtils.addAction("Fit Image to window", Ids.COMMANDS_FIT_IMAGE_TO_WINDOW,
					Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));
			actions.add(ADActionUtils.addAction("Set Exposure", Ids.COMMANDS_SET_EXPOSURE,
					Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));
			actions.add(ADActionUtils.addAction("Rescale Live Image", Ids.COMMANDS_SET_LIVEVIEW_SCALE,
					Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME, adController.getServiceName()));
		}
		for (IAction iAction : actions) {
			getViewSite().getActionBars().getToolBarManager().add(iAction);
		}
		createShowViewAction();
	}

	protected void createShowViewAction() {
		List<IAction> actions = new Vector<IAction>();
		{
			actions.add(ADActionUtils.addShowViewAction("Show Stats", HistogramView.Id, adController.getServiceName(),
					"Show stats view for selected camera", Activator.getHistogramViewImage()));
			actions.add(ADActionUtils.addShowViewAction("Show Array", TwoDArrayView.Id, adController.getServiceName(),
					"Show array view for selected camera", Activator.getTwoDArrayViewImage()));
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
		if (image != null) {
			image.dispose();
			image = null;
		}
		super.dispose();
	}

}