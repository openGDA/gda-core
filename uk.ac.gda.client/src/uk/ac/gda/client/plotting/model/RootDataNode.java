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

package uk.ac.gda.client.plotting.model;

import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootDataNode extends DataNode implements IScanDataPointObserver {

	private static final Logger logger = LoggerFactory.getLogger(RootDataNode.class);

	public static final long DELAY_TO_PLOT_SCAN_DATA_POINTS_IN_MILLI = 250L;
	public static final int MAX_SCANS_HISTORY = 500;
	public static final int MAX_SCANS_WITH_CACHED_DATA = 10;

	public static final String DATA_STORE_NAME = "plotting_data";
	private static final int MAX_THREAD_POOL_FOR_PLOTTING = 5;

	private final List<IScanDataPoint> cachedPoints = Collections.synchronizedList(new ArrayList<IScanDataPoint>());
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(MAX_THREAD_POOL_FOR_PLOTTING);

	private final List<ScanDataNode> innerChildren = new LinkedList<ScanDataNode>();
	private final RollingWritableList children = new RollingWritableList(innerChildren, ScanDataNode.class);

	public RootDataNode() {
		super(null);
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				plotAndClearCached();
			}
		}, 0L, DELAY_TO_PLOT_SCAN_DATA_POINTS_IN_MILLI, TimeUnit.MILLISECONDS);
		loadData(); // Loading data before adding listener
		children.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {
					@Override
					public void handleRemove(int index, Object element) {
						((ScanDataNode) element).disposeResources();
						saveScanHistory();
					}

					@Override
					public void handleAdd(int index, Object element) {
						saveScanHistory();
					}
				});
			}
		});
	}

	private void loadData() {
		List<ScanDataNode> scansToLoad = PlottingDataStore.INSTANCE.getPreferenceDataStore().loadArrayConfiguration(DATA_STORE_NAME, ScanDataNode.class);
		if (scansToLoad != null) {
			for (ScanDataNode loadedScan : scansToLoad) {
				ScanDataNode scanDataNode = new ScanDataNode(loadedScan.getIdentifier(), loadedScan.getFileName(), loadedScan.getPositionScanItemNames(), loadedScan.getDetectorScanItemNames(), this);
				children.add(scanDataNode);
			}
		}
	}

	@Override
	public IObservableList getChildren() {
		return children;
	}

	@Override
	public String getIdentifier() {
		return "";
	}

	@Override
	public void update(final Object source, final Object arg) {
		if (arg instanceof IScanDataPoint) {
			addScanDataPoint((IScanDataPoint) arg);
		}
	}

	private void addScanDataPoint(IScanDataPoint scanDataPoint) {
		synchronized (cachedPoints) {
			cachedPoints.add(scanDataPoint);
		}
	}

	private void plotAndClearCached() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (cachedPoints) {
					if (!cachedPoints.isEmpty()) {
						try {
							updateDataSetInUI(cachedPoints);
						} catch (Exception e) {
							logger.error("Unable to update data", e);
						}
						cachedPoints.clear();
					}
				}
			}
		});
	}

	int scanNo = 0;
	protected void updateDataSetInUI(List<IScanDataPoint> scanDataPoints) {
		// FIXME! More work needed to be able to configure which scan entries are shown
//		if ((scanDataPoint.getScanPlotSettings() != null && scanDataPoint.getScanPlotSettings().getYAxesShown().length < 1)) {
//			return;
//		}

		for(int i = 0; i < scanDataPoints.size(); i++) {
			IScanDataPoint currentPoint = scanDataPoints.get(i);
			int numberOfPoints = currentPoint.getNumberOfPoints();
			int currentPointNumber = currentPoint.getCurrentPointNumber();
			int currentScan = currentPoint.getScanIdentifier();
			
			if (children.isEmpty() || currentScan > Integer.parseInt(getLastDataNode().getIdentifier())) {
				List<String> detectorScanItemNames = null;
				if (!currentPoint.getDetectorHeader().isEmpty() && currentPoint.getDetectorDataAsDoubles().length > 0) {
					detectorScanItemNames = currentPoint.getDetectorHeader();
				}
				ScanDataNode newScanDataNode = new ScanDataNode(
						Integer.toString(currentPoint.getScanIdentifier()),
						currentPoint.getCurrentFilename(),
						currentPoint.getPositionHeader(),
						detectorScanItemNames,
						this);
				children.addAndUpdate(newScanDataNode);
			}
			List<IScanDataPoint> points = new ArrayList<IScanDataPoint>();
			for (int j = 0; j < numberOfPoints - currentPointNumber && i < scanDataPoints.size(); j++) {
				points.add(scanDataPoints.get(i++));
			}
			updatePoints(points, getLastDataNode());
		}
		
	}
	
	private ScanDataNode getLastDataNode() {
		return (ScanDataNode) children.get(0);
	}

	private void updatePoints(List<IScanDataPoint> points, ScanDataNode currentScanDataNode) {
		if (currentScanDataNode != null&& !points.isEmpty()) {
			currentScanDataNode.update(points);
			for (Object object : currentScanDataNode.getChildren()) {
				this.firePropertyChange(DATA_ADDED_PROP_NAME, null, object);
			}
		}
	}

	@Override
	public void disposeResources() {
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
		executorService.shutdown();
	}

	private class RollingWritableList extends WritableList {

		public RollingWritableList(List<?> toWrap, Object elementType) {
			super(toWrap, elementType);
		}

		public void addAndUpdate(ScanDataNode element) {
			if (size() >= MAX_SCANS_WITH_CACHED_DATA) {
				final ScanDataNode node = (ScanDataNode) RollingWritableList.super.get(MAX_SCANS_WITH_CACHED_DATA - 1);
				node.clearCache();
			}
			super.add(0, element);
			while (size() > MAX_SCANS_HISTORY) {
				super.remove(size() - 1);
			}
		}
	}

	private void saveScanHistory() {
		PlottingDataStore.INSTANCE.getPreferenceDataStore().saveConfiguration(DATA_STORE_NAME, innerChildren);
	}

	@Override
	public void removeChild(DataNode dataNode) {
		children.remove(dataNode);
	}
}
