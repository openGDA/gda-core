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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gson.annotations.Expose;

import gda.rcp.GDAClientActivator;
import gda.scan.IScanDataPoint;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.liveplot.IPlotLineColorService;
import uk.ac.gda.client.plotting.model.LineTraceProviderNode.TraceStyleDetails;

public class ScanEntryNode extends ScanNode {

	private static final String SCAN_DATA_STORE_PREFIX = "scan:";
	private final IObservableList children = new WritableList(new ArrayList<ScanDataItemNode>(), ScanDataItemNode.class);
	private final List<Double> cachedData = Collections.synchronizedList(new ArrayList<Double>());

	@Expose
	private final List<String> detectorScanItemNames;
	@Expose
	private final List<String> positionScanItemNames;
	private final String xAxisName;

	public ScanEntryNode(String identifier, String fileName, List<String> positionScanItemNames, List<String> detectorScanItemNames, String[] selectedScanItemNamesFromPreviousScan, Node parent) {
		super(identifier, fileName, parent);
		this.detectorScanItemNames = detectorScanItemNames;
		this.positionScanItemNames = positionScanItemNames;
		xAxisName = this.positionScanItemNames.get(0);
		createScanDataItems(selectedScanItemNamesFromPreviousScan);
	}

	public List<String> getDetectorScanItemNames() {
		return detectorScanItemNames;
	}

	public List<String> getPositionScanItemNames() {
		return positionScanItemNames;
	}

	public DoubleDataset getData() {
		synchronized (cachedData) {
			if (cachedData.isEmpty()) {
				loadCachedDataFromFile();
			}
			DoubleDataset dataset = (DoubleDataset) DatasetFactory.createFromList(cachedData);
			dataset.setName(xAxisName);
			return dataset;
		}
	}

	@Override
	public IObservableList getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

	public void clearCache() {
		synchronized (cachedData) {
			cachedData.clear();
		}
		for (Object obj : children) {
			((ScanDataItemNode) obj).clearCache();
		}
	}

	public void update(List<IScanDataPoint> scanDataPoint) {
		synchronized (cachedData) {
			for (IScanDataPoint p : scanDataPoint) {
				cachedData.add(p.getPositionsAsDoubles()[0]);
			}
		}

		// TODO Currently detectorScanItemNames are added then positionScanItemNames, needs reviewing on how they are shown

		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i < scanDataPoint.get(0).getPositionsAsDoubles().length - 1; i++) {
			for (IScanDataPoint p : scanDataPoint) {
				values.add( p.getPositionsAsDoubles()[i + 1]);
			}
			((ScanDataItemNode) children.get(i)).update(values);
			values.clear();
		}

		if (detectorScanItemNames != null) {
			int offset = scanDataPoint.get(0).getPositionsAsDoubles().length - 1;
			values.clear();
			for (int i = 0; i < scanDataPoint.get(0).getDetectorDataAsDoubles().length; i++) {
				for (IScanDataPoint p : scanDataPoint) {
					values.add(p.getDetectorDataAsDoubles()[i]);
				}
				((ScanDataItemNode) children.get(i + offset)).update(values);
				values.clear();
			}
		}

		// Write stored data to file at the end of scan rather than for every scan data point.
		int numScanPoints = scanDataPoint.get(0).getNumberOfPoints();
		if (cachedData.size() == numScanPoints) {
			Display.getDefault().asyncExec(saveData);
			for (Object child : children) {
				((ScanDataItemNode) child).saveCachedData();
			}
		}
	}

	@Override
	public void removeChild(Node dataNode) {
		// Not supported
	}

	@Override
	public void disposeResources() {
		PlottingDataStore.INSTANCE.getPreferenceDataStore().removeConfiguration(getStoredIdentifier());
	}

	private void createScanDataItems(String[] selectedScanItemNamesFromPreviousScan) {
		List<String> selectedScanItemNamesFromPreviousScanList = Arrays.asList(selectedScanItemNamesFromPreviousScan);
		// TODO Currently detectorScanItemNames are added then positionScanItemNames, needs reviewing on how they are shown
		if (positionScanItemNames != null) {
			for (int i = 1; i < positionScanItemNames.size(); i++) { // 0 is reserved for x-axis
				boolean plotByDefault = true;
				if (!selectedScanItemNamesFromPreviousScanList.isEmpty()) {
					plotByDefault = selectedScanItemNamesFromPreviousScanList.contains(positionScanItemNames.get(i));
				}
				createScanDataItem(positionScanItemNames.get(i), plotByDefault);
			}
		}
		if (detectorScanItemNames != null) {
			for (String scanItemName : detectorScanItemNames) {
				boolean plotByDefault = true;
				if (!selectedScanItemNamesFromPreviousScanList.isEmpty()) {
					plotByDefault = selectedScanItemNamesFromPreviousScanList.contains(scanItemName);
				}
				createScanDataItem(scanItemName, plotByDefault);
			}
		}
		children.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {

					@Override
					public void handleRemove(int index, Object element) {
						((ScanDataItemNode) element).disposeResources();
					}

					@Override
					public void handleAdd(int index, Object element) {}
				});
			}
		});
	}

	private void createScanDataItem(String scanItemName, boolean plotByDefault) {
		String dataItemIdentifier = createScanItemIdentifier(scanItemName);
		TraceStyleDetails traceStyle = createDefaultTraceStyle(scanItemName);
		ScanDataItemNode scanDataItemNode = new ScanDataItemNode(dataItemIdentifier, scanItemName, traceStyle, this, plotByDefault, this);
		children.add(scanDataItemNode);
	}

	private TraceStyleDetails createDefaultTraceStyle(String scanDataItem) {
		TraceStyleDetails traceStyle = null;
		RootDataNode experimentDataNode = (RootDataNode) this.getParent();
		if ((experimentDataNode.getChildren().size() - experimentDataNode.getChildren().indexOf(this)) % 2 == 0) {
			traceStyle = TraceStyleDetails.createDefaultSolidTrace();
		} else {
			traceStyle = TraceStyleDetails.createDefaultDashTrace();
		}
		traceStyle.setColorHexValue(getColorInHex(scanDataItem));
		return  traceStyle;
	}

	private String getColorInHex(String scanDataItem) {
		BundleContext context = GDAClientActivator.getBundleContext();
		ServiceReference<IPlotLineColorService> serviceRef = context.getServiceReference(IPlotLineColorService.class);
		if (serviceRef != null) {
			String colorValue = (String) serviceRef.getProperty(scanDataItem);
			if (colorValue != null) {
				return colorValue;
			}
		}

		return UIHelper.getRandomColor();
	}

	private String createScanItemIdentifier(String scanDataItem) {
		return "Scan@" + getIdentifier() + "@" + scanDataItem;
	}

	private void loadCachedDataFromFile() {
		List<Double> storedList = PlottingDataStore.INSTANCE.getPreferenceDataStore().loadArrayConfiguration(getStoredIdentifier(), Double.class);
		cachedData.addAll(storedList);
	}

	private String getStoredIdentifier() {
		return SCAN_DATA_STORE_PREFIX + this.getIdentifier();
	}

	private final Runnable saveData = new Runnable() {
		@Override
		public void run() {
			synchronized (cachedData) {
				PlottingDataStore.INSTANCE.getPreferenceDataStore().saveConfiguration(getStoredIdentifier(), cachedData);
			}
		}
	};
}
