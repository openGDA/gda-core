/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.example.detector;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.SimpleDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.eclipse.scanning.malcolm.core.Services;

public class RandomIntDetector extends AbstractRunnableDevice<SimpleDetectorModel> implements IWritableDetector<SimpleDetectorModel>, INexusDevice<NXdetector> {

	private static final int[] IMAGE_SIZE = { 8, 8 };

	private ILazyWriteableDataset imageData;

	public RandomIntDetector() throws ScanningException {
		super(Services.getRunnableDeviceService());
		setDeviceState(DeviceState.READY);
	}

	@Override
	public void configure(SimpleDetectorModel model) throws ScanningException {
		super.configure(model);
		setName(model.getName());
	}

	@Override
	public void run(IPosition position) throws ScanningException {
		// nothing to do
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXdetector detector = NexusNodeFactory.createNXdetector();
		detector.setCount_timeScalar(model.getExposureTime());

		imageData = detector.initializeLazyDataset(NXdetector.NX_DATA, info.getRank() + 2, Integer.class);
		imageData.setChunking(info.createChunk(IMAGE_SIZE));

		return new NexusObjectWrapper<>(getName(), detector, NXdetector.NX_DATA);
	}

	@Override
	public boolean write(IPosition position) throws ScanningException{
		final IDataset image = Random.randint(0, 100, IMAGE_SIZE);

		final IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(position, IMAGE_SIZE);
		final SliceND sliceNd = new SliceND(imageData.getShape(), imageData.getMaxShape(), scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
		try {
			imageData.setSlice(null, image, sliceNd);
		} catch (Exception e) {
			throw new ScanningException(e);
		}
		return true;
	}

}
