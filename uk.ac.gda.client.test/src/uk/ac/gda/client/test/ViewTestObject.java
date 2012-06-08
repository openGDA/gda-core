/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.test;

import gda.TestHelpers;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerStatus;
import gda.observable.ObservableComponent;
import gda.rcp.util.UIScanDataPointEventService;
import gda.rcp.views.scan.AbstractScanPlotView;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotBean;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.io.FileUtils;

public class ViewTestObject implements IScanDataPointProvider {

	private long pointPause = 50;
	private List<String> fileData;
	private int lineIndex;

	public ViewTestObject(final AscciLineParser handler, final URL data) throws Exception {
		TestHelpers.setUpTest(handler.getClass(), "setUp", true);

		InterfaceProvider.setScanDataPointProviderForTesting(this);
		UIScanDataPointEventService.getInstance().reconnect();

		this.fileData = FileUtils.readFileAsList(new File(EclipseUtils.getAbsoluteUrl(data).getFile()));
		this.lineIndex = 0;

	}

	public IViewPart openView(final String id) throws Exception {

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IViewPart view = window.getActivePage().showView(id);
		window.getActivePage().activate(view);

		ActionFactory.IWorkbenchAction maximizeAction = ActionFactory.MAXIMIZE.create(window);
		maximizeAction.run(); // Will maximize the active part

		return view;
	}

	public void createAndMonitorPoints() throws Exception {
		monitorPoints();
	}

	private void monitorPoints() throws Exception {
		// Delay until all points plotted.
		while (!isDataComplete()) {
			EclipseUtils.delay(2000); // NOTE too short is bad.
		}

		if (UIScanDataPointEventService.getInstance().getCurrentDataPoints().size() != (lineIndex - 1))
			throw new Exception("Current points are size "
					+ UIScanDataPointEventService.getInstance().getCurrentDataPoints().size() + " and line index is "
					+ lineIndex);

		comp.notifyIObservers(this, new JythonServerStatus(0, 0)); // Stop does not reset, test this

		if (UIScanDataPointEventService.getInstance().getCurrentDataPoints().size() != (lineIndex - 1))
			throw new Exception("Current points are size "
					+ UIScanDataPointEventService.getInstance().getCurrentDataPoints().size() + " and line index is "
					+ lineIndex);
	}

	public void checkData(AbstractScanPlotView part) throws Exception {
		checkData(part, lineIndex);
	}

	public void checkData(AbstractScanPlotView part, final int checkSize) throws Exception {
		final PlotBean bean = part.getPlotBean();
		final Map<String, ? extends IDataset> data = bean.getDataSets();
		if (data.isEmpty())
			throw new Exception("Data should not be empty!");
		for (IDataset d : data.values()) {
			DoubleDataset oldD = (DoubleDataset) DatasetUtils.convertToAbstractDataset(d).cast(AbstractDataset.FLOAT64);
			if (oldD.getSize() != checkSize)
				throw new Exception("Data (size = " + oldD.getSize() + ") should be same size as plotted data (size = "
						+ checkSize + ").");
			if (!PlotData.validateData(oldD.getData()))
				throw new Exception("Some points were invalid but the data set sent was valid.");
		}
	}

	private ObservableComponent comp = new ObservableComponent();
	private IScanDataPoint lastScanDataPoint;

	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
		comp.addIObserver(anObserver);
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
		comp.deleteIObserver(anObserver);
	}

	@Override
	public void update(Object dataSource, Object data) {
		if( data instanceof IScanDataPoint)
			lastScanDataPoint = (IScanDataPoint)data;
		comp.notifyIObservers(dataSource, data);
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return lastScanDataPoint;
	}

	public int getLineIndex() {
		return lineIndex;
	}

	public boolean isDataComplete() {
		return lineIndex >= fileData.size();
	}

	public interface AscciLineParser {
		public void parseLine(IScanDataPoint point, String line);
	}

	/**
	 * @return Returns the pointPause.
	 */
	public long getPointPause() {
		return pointPause;
	}

	/**
	 * @param pointPause
	 *            The pointPause to set.
	 */
	public void setPointPause(long pointPause) {
		this.pointPause = pointPause;
	}

}
