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

package uk.ac.diamond.daq.tomography.datacollection.ui.views;

import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.tomography.datacollection.ui.adviewer.ADViewerConstants;
import uk.ac.diamond.daq.tomography.datacollection.ui.adviewer.DataCollectionADControllerImpl;
import uk.ac.diamond.daq.tomography.datacollection.ui.adviewer.DataCollectionMJPEGViewComposite;
import uk.ac.gda.epics.adviewer.composites.MJPeg;
import uk.ac.gda.epics.adviewer.views.ADActionUtils;
import uk.ac.gda.epics.adviewer.views.MJPegView;

public class DataCollectionMJPegView extends MJPegView {
	private static final Logger logger = LoggerFactory.getLogger(DataCollectionMJPegView.class);
	public static final String Id = "uk.ac.diamond.daq.tomography.datacollection.ui.views.DataCollectionMJPegView";
	private DataCollectionADControllerImpl adControllerImpl;

	public DataCollectionMJPegView() {
		super(ADViewerConstants.AD_CONTROLLER_SERVICE_NAME);
	}

	@Override
	protected MJPeg createPartControlEx(Composite parent) {
		try {
			adControllerImpl = (DataCollectionADControllerImpl) getAdController();
			DataCollectionMJPEGViewComposite mJPEGViewComposite = new DataCollectionMJPEGViewComposite(parent, adControllerImpl.getStagesCompositeFactory());
			mJPEGViewComposite.setADController(adControllerImpl, this);
			return mJPEGViewComposite.getMJPeg();
		} catch (Exception e) {
			logger.error("Cannot create Data Collection MJPEG View Composite", e);
		}
		return null;
	}

	@Override
	protected void createShowViewAction() {
		List<IAction> actions = new Vector<IAction>();
		{
			actions.add(ADActionUtils.addShowViewAction("Show Stats", DataCollectionPCOHistogramView.Id, null, "Show stats view for selected camera",
					uk.ac.gda.epics.adviewer.Activator.getHistogramViewImage()));
			actions.add(ADActionUtils.addShowViewAction("Show Array", DataCollectionPCOArrayView.Id, null, "Show array view for selected camera",
					uk.ac.gda.epics.adviewer.Activator.getTwoDArrayViewImage()));
		}
		for (IAction iAction : actions) {
			getViewSite().getActionBars().getToolBarManager().add(iAction);
		}
	}
}
