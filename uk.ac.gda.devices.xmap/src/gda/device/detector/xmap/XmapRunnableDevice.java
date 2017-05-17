/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.eclipse.scanning.example.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.xmap.api.XmapRunnableDeviceModel;
import gda.factory.Finder;

/**
 * <p>
 * This is an implementation of a new style GDA detector that can connect to an XMAP detector.
 * </p>
 *
 * @author Anthony Hull
 */
public class XmapRunnableDevice extends AbstractRunnableDevice<XmapRunnableDeviceModel>
		implements IWritableDetector<XmapRunnableDeviceModel>, INexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(XmapRunnableDevice.class);
	private static final String DATASET_NAME_FULL_SPECTRUM = "fullSpectrum";
	private NexusXmap xmapMca;
	private String detectorName;

	// Map from dataset name to the lazy dataset used to write to the Nexus file
	private Map<String, ILazyWriteableDataset> datasetMap = null;

	public XmapRunnableDevice() {
		super(Services.getRunnableDeviceService());
	}

	@Override
	public void configure(XmapRunnableDeviceModel model) throws ScanningException {
		super.configure(model);
		setDeviceState(DeviceState.CONFIGURING);

		// Get the detector named in the model
		detectorName = model.getName();
		xmapMca = Finder.getInstance().find(detectorName);
		if (xmapMca == null) {
			final String message = "Could not find XMAP MCA: " + detectorName;
			logger.error(message);
			throw new ScanningException(message);
		}

		// Configure detector
		try {
			xmapMca.setAcquisitionTime(model.getExposureTime());
			xmapMca.configure();
			xmapMca.atScanStart();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			final String message = "Configuring controller failed";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}

		setDeviceState(DeviceState.READY);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo scanInfo) throws NexusException {
		datasetMap = new HashMap<>();

		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();
		nxDetector.setCount_timeScalar(model.getExposureTime());

		// Create lazy dataset for each data type returned by the detector
		try {
			final List<INexusTree> detectorDataNodes = getDetectorDataNodes();
			for (INexusTree dataNode : detectorDataNodes) {
				final String datasetName = dataNode.getName();
				logger.debug("Creating dataset for " + datasetName);

				final ILazyWriteableDataset lazyDataset = createLazyDatasetForNexusTreeNode(dataNode, scanInfo.getRank());
				nxDetector.createDataNode(datasetName, lazyDataset);
				datasetMap.put(datasetName, lazyDataset);
			}
		} catch (Exception e) {
			final String message = "Could not get list of datasets from xmap detector";
			logger.error(message, e);
			throw new NexusException(message, e);
		}

		// Set fullSpectrum as the primary data from the detector
		final NexusObjectWrapper<NXdetector> wrapper = new NexusObjectWrapper<>(getName(), nxDetector);
		for (final String datasetName : datasetMap.keySet()) {
			if (datasetName.equals(DATASET_NAME_FULL_SPECTRUM)) {
				wrapper.setPrimaryDataFieldName(datasetName);
			} else {
				wrapper.addAdditionalPrimaryDataFieldName(datasetName);
			}
		}
		return wrapper;
	}

	private static ILazyWriteableDataset createLazyDatasetForNexusTreeNode(INexusTree node, int scanRank) {
		// Extract frame of data from INexusTree :
		final String datasetName = node.getName();
		final Dataset dset = node.getData().toDataset();
		final int dataType = dset.getDType();
		final int[] dataShape = dset.getShape();

		// Set max shape of lazy dataset : i.e. scan shape + detector frame data shape
		final int[] maxShape = new int[scanRank + dataShape.length];
		Arrays.fill(maxShape, ILazyWriteableDataset.UNLIMITED);

		// Set dimensions corresponding to shape of detector frame
		// (scan dimensions are unlimited, i.e. set to -1)
		for (int i = 0; i < dataShape.length; i++) {
			maxShape[scanRank + i] = dataShape[i];
		}

		// Initial shape of lazy dataset (0 for scan dimensions, frame shape for the rest)
		final int[] shapeForLazy = maxShape.clone();
		Arrays.fill(shapeForLazy, 0, scanRank, 0);

		final ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(datasetName, dataType, shapeForLazy, maxShape, null);
		return lazy;
	}

	private List<INexusTree> getDetectorDataNodes() throws DeviceException {
		// Get list of data fields that detector will produce (assume detector has already been configured)
		final NexusTreeProvider nexusTreeProvider = xmapMca.readout();

		final List<INexusTree> dataFields = new ArrayList<>();
		final INexusTree detectorNode = nexusTreeProvider.getNexusTree().getChildNode(0);
		for (int i = 0; i < detectorNode.getNumberOfChildNodes(); i++) {
			dataFields.add(detectorNode.getChildNode(i));
		}

		return dataFields;
	}

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		try {
			xmapMca.clearAndStart();
			xmapMca.waitWhileBusy();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Acquiring from detector failed", e);
		}
	}

	@Override
	public boolean write(IPosition position) throws ScanningException {
		try {
			// Get data from detector
			final NexusTreeProvider nexusTreeProvider = xmapMca.readout();
			final INexusTree detectorNode = nexusTreeProvider.getNexusTree().getChildNode(0);

			if (!detectorNode.getName().equals(detectorName)) {
				final String message = "Incorrect detector name in data: expected "
						+ detectorName + ", actual: " + detectorNode.getName();
				logger.error(message);
				throw new ScanningException(message);
			}

			// For each data node from the detector, update the corresponding dataset
			for (int i = 0; i < detectorNode.getNumberOfChildNodes(); i++) {
				final INexusTree dataNode = detectorNode.getChildNode(i);
				final NexusGroupData data = dataNode.getData();
				final ILazyWriteableDataset dataset = datasetMap.get(dataNode.getName());

				final IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(position, data.getDimensions());
				final SliceND sliceND = new SliceND(dataset.getShape(), dataset.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
				dataset.setSlice(null, data.toDataset(), sliceND);
			}
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			final String message = "Getting the data from the detector failed";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}

		setDeviceState(DeviceState.READY);
		return true;
	}

}
