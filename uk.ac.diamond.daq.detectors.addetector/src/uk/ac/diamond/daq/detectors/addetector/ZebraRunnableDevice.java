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

package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.dawnsci.analysis.api.AbstractClientProvider;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.detectors.addetector.api.ZebraModel;

/**
 * Runnable device wrapper for Zebra box to allow it to be used in Mapping scans
 *
 * This implementation was originally written for I14 and assumes that the box has 4 channels (DIV blocks) all of which
 * return a double value.
 */
public class ZebraRunnableDevice extends AbstractRunnableDevice<ZebraModel> implements IWritableDetector<ZebraModel>, INexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractClientProvider.class);
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePv;

	private Channel resetChannel;
	private Channel armChannel;
	private Channel armOutChannel;
	private Channel gateWidthChannel;
	private Channel pulseDelayChannel;
	private Channel div1LastChannel;
	private Channel div2LastChannel;
	private Channel div3LastChannel;
	private Channel div4LastChannel;

	private ILazyWriteableDataset dataset1;
	private ILazyWriteableDataset dataset2;
	private ILazyWriteableDataset dataset3;
	private ILazyWriteableDataset dataset4;

	public ZebraRunnableDevice() {
		super(ServiceHolder.getRunnableDeviceService());
	}

	@Override
	public void configure(ZebraModel model) throws ScanningException {
		if (StringUtils.isEmpty(basePv)) {
			final String message = "Base PV not set";
			logger.error(message);
			throw new ScanningException(message);
		}

		// Create Epics channels
		try {
			resetChannel = EPICS_CONTROLLER.createChannel(basePv + "SYS_RESET.PROC");
			armChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_ARM");
			armOutChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_ARM_OUT");
			gateWidthChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_GATE_WID");
			pulseDelayChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_PULSE_DLY");
			div1LastChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_DIV1_LAST");
			div2LastChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_DIV2_LAST");
			div3LastChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_DIV3_LAST");
			div4LastChannel = EPICS_CONTROLLER.createChannel(basePv + "PC_DIV4_LAST");
		} catch (CAException | TimeoutException e) {
			final String message = "Error creating EPICS channel";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}

		// Setup exposure time
		try {
			EPICS_CONTROLLER.caputWait(gateWidthChannel, model.getExposureTime());
			EPICS_CONTROLLER.caputWait(pulseDelayChannel, model.getExposureTime());
		} catch (TimeoutException | CAException | InterruptedException e) {
			final String message = "Error setting exposure time";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}

		super.configure(model);
	}

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		try {
			// Reset
			EPICS_CONTROLLER.caputWait(resetChannel, 1);
			// Arm
			EPICS_CONTROLLER.caput(armChannel, 1);

			while (EPICS_CONTROLLER.cagetInt(armOutChannel) != 0) {
				Thread.sleep(50);
			}

		} catch (CAException | TimeoutException e) {
			final String message = "Error collecting data";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo scanInfo) throws NexusException {

		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();
		nxDetector.setCount_timeScalar(model.getExposureTime());

		dataset1 = nxDetector.initializeLazyDataset("data1", scanInfo.getRank(), Double.class);
		dataset2 = nxDetector.initializeLazyDataset("data2", scanInfo.getRank(), Double.class);
		dataset3 = nxDetector.initializeLazyDataset("data3", scanInfo.getRank(), Double.class);
		dataset4 = nxDetector.initializeLazyDataset("data4", scanInfo.getRank(), Double.class);

		// Set the chunking
//		data.setChunking(scanInfo.createChunk(dataDimensions));

		final NexusObjectWrapper<NXdetector> nexusProvider = new NexusObjectWrapper<NXdetector>(getName(), nxDetector);

		// "data" is the name of the primary data field (i.e. the 'signal' field of the default NXdata)
		nexusProvider.setPrimaryDataFieldName("data1");
		nexusProvider.addAdditionalPrimaryDataFieldName("data2");
		nexusProvider.addAdditionalPrimaryDataFieldName("data3");
		nexusProvider.addAdditionalPrimaryDataFieldName("data4");

		return nexusProvider;
	}

	@Override
	public boolean write(IPosition position) throws ScanningException {

		try {
			final double data1 = EPICS_CONTROLLER.cagetDouble(div1LastChannel);
			final double data2 = EPICS_CONTROLLER.cagetDouble(div2LastChannel);
			final double data3 = EPICS_CONTROLLER.cagetDouble(div3LastChannel);
			final double data4 = EPICS_CONTROLLER.cagetDouble(div4LastChannel);

			final IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(position);

			// We assume that all datasets have the same shape
			final SliceND slice = new SliceND(dataset1.getShape(), dataset1.getMaxShape(), scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());

			final Dataset ds1 = DatasetFactory.createFromObject(data1);
			final Dataset ds2 = DatasetFactory.createFromObject(data2);
			final Dataset ds3 = DatasetFactory.createFromObject(data3);
			final Dataset ds4 = DatasetFactory.createFromObject(data4);

			dataset1.setSlice(null, ds1, slice);
			dataset2.setSlice(null, ds2, slice);
			dataset3.setSlice(null, ds3, slice);
			dataset4.setSlice(null, ds4, slice);

		} catch (TimeoutException | CAException | InterruptedException | DatasetException e) {
			final String message = "Error writing data";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}

		setDeviceState(DeviceState.ARMED);
		return true;
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}
}