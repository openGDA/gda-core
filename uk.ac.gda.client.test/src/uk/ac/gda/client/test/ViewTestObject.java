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
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.rcp.util.UIScanDataPointEventService;
import gda.rcp.views.scan.AbstractScanPlotView;
import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPoint;
import gda.scan.ScanEvent;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotBean;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.io.FileUtils;

public class ViewTestObject implements IScanDataPointProvider {

	private long pointPause = 50;
	private List<String> fileData;
	private int lineIndex;
	private AscciLineParser handler;

	public ViewTestObject(final AscciLineParser handler, final URL data) throws Exception {
		this.handler = handler;
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
		createPoints();
		monitorPoints();
	}

	private ScanDataPointGenerator createPoints() throws Exception {

		final ScanDataPointGenerator queue = new ScanDataPointGenerator() {
			@Override
			public Scannable getScannable() {
				return new DummyScannable("x axis") {

					@Override
					public Object rawGetPosition() {
						if (lineIndex >= fileData.size())
							return 0d;
						final String line = fileData.get(lineIndex);
						final String[] d = line.split(" ");
						currentPosition = Double.parseDouble(d[0]);
						return currentPosition;
					}
				};
			}
		};

		sdpObserverComponent.notifyIObservers(this, new JythonServerStatus(1, 0));

		if (UIScanDataPointEventService.getInstance().getCurrentDataPoints().size() != 0)
			throw new Exception("There are points in the service and there should be none.");

		queue.start();

		return queue;
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

		sdpObserverComponent.notifyIObservers(this, new JythonServerStatus(0, 0)); // Stop does not reset, test this

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
			DoubleDataset oldD = (DoubleDataset) DatasetUtils.cast(d, Dataset.FLOAT64);
			if (oldD.getSize() != checkSize)
				throw new Exception("Data (size = " + oldD.getSize() + ") should be same size as plotted data (size = "
						+ checkSize + ").");
			if (!PlotData.validateData(oldD.getData()))
				throw new Exception("Some points were invalid but the data set sent was valid.");
		}
	}

	private ObservableComponent sdpObserverComponent = new ObservableComponent();
	private ObservableComponent scanEventObserverComponent = new ObservableComponent();
	private IScanDataPoint lastScanDataPoint;

	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
		sdpObserverComponent.addIObserver(anObserver);
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
		sdpObserverComponent.deleteIObserver(anObserver);
	}

	public void update(Object dataSource, Object data) {
		if (data instanceof IScanDataPoint) {
			lastScanDataPoint = (IScanDataPoint) data;
			sdpObserverComponent.notifyIObservers(dataSource, data);
		} else if (data instanceof ScanEvent){
			scanEventObserverComponent.notifyIObservers(dataSource, data);
		}
	}


	@Override
	public void addScanEventObserver(IObserver anObserver) {
		scanEventObserverComponent.addIObserver(anObserver);
	}

	@Override
	public void deleteScanEventObserver(IObserver anObserver){
		scanEventObserverComponent.addIObserver(anObserver);
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

	private abstract class ScanDataPointGenerator {

		public abstract Scannable getScannable();

		public void start() {

			Thread thread = new Thread() {
				@Override
				public void run() {
					for (String line : fileData) {

						try {
							if (Thread.interrupted())
								return;
							if (isDataComplete())
								return;

							++lineIndex;
							if (line.startsWith("#"))
								continue;

							final ScanDataPoint scanDataPoint = new ScanDataPoint();
							scanDataPoint.setUniqueName("scanName");
							scanDataPoint.setScanIdentifier(1);

							final Scannable scannable = getScannable();

							scanDataPoint.addScannable(scannable);
							scanDataPoint.addScannablePosition(scannable.getPosition(), scannable.getOutputFormat());

							scanDataPoint.setCurrentPointNumber(lineIndex);
							scanDataPoint.setNumberOfPoints(1);
							scanDataPoint.setInstrument("instrument");
							scanDataPoint.setCommand("blah blah");
							scanDataPoint.setCurrentFilename("fred.nxs");
							scanDataPoint.setNumberOfChildScans(0);

							handler.parseLine(scanDataPoint, line);

							update(this, scanDataPoint);
							Thread.sleep(pointPause);

						} catch (Exception ne) {
							ne.printStackTrace();
						}
					}
				}
			};
			thread.start();
		}

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
