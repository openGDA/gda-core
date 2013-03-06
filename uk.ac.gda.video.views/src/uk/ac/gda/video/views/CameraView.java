/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.video.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

public class CameraView extends ViewPart implements NewImageListener  {
	public static final String ID = "uk.ac.gda.video.views.cameraview";
	static final Logger logger = LoggerFactory.getLogger(CameraView.class);
	public CameraView() {

	}
	@Override
	public void createPartControl(Composite parent) {
		
		int selected;
		if (this.getViewSite().getSecondaryId() != null)
			selected = Integer.parseInt(this.getViewSite().getSecondaryId());
		else
			selected = 0;
		
		parent.setLayout(new FillLayout());
		cameraConfig = Activator.getCameraConfig();

		if (cameraConfig == null) {
			Label lblCamera = new Label(parent, SWT.NONE);
			lblCamera.setText("No ICameraConfig service found");
			return;
		}


		cameraComposite = new CameraComposite(parent, SWT.NONE, cameraConfig, parent.getDisplay(), this);
		cameraComposite.select(selected);
	}

	@Override
	public void setFocus() {
		if( cameraComposite != null)
			cameraComposite.setFocus();
	}


	public void handleAnalyse(@SuppressWarnings("unused") ExecutionEvent event)  {
		sendForAnalysis();
	}
	private void sendForAnalysis() {
		try {
			DataBean dbean = new DataBean(GuiPlotMode.TWOD);
			List<DataSetWithAxisInformation> data = new ArrayList<DataSetWithAxisInformation>();
			DataSetWithAxisInformation info = new DataSetWithAxisInformation();
			info.setData(cameraComposite.getDataset());
			data.add(info);
			dbean.setData(data);

			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IViewPart showView = window.getActivePage().showView(cameraConfig.getPlotViewID());
			if(showView == null || !(showView instanceof PlotView)){
				MessageBox messageBox = new MessageBox(cameraComposite.getShell(), SWT.ICON_WARNING );
				messageBox.setText("Plot " + cameraConfig.getPlotViewID() + " cannot be opened or is not of type PlotView");
			} else {
				((PlotView)showView).processPlotUpdate(dbean);
			}
			
		} catch (PartInitException e) {
			logger.error("Error handling analyse", e);
		}
	}

	private CameraComposite cameraComposite;
	private ICameraConfig cameraConfig;



	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		if (Activator.getCameraConfig() == null)
			return;
	}
	boolean send=false;
	public void handleAnalyseContinuous(@SuppressWarnings("unused") ExecutionEvent event, boolean send) {
		this.send=send;
	}
	@Override
	public void handlerNewImageNotification() {
		if( send){
			sendForAnalysis();
		}
		
	}
	@Override
	public void dispose() {
		super.dispose();
		if( cameraComposite != null)
			cameraComposite.dispose();
	}

}

