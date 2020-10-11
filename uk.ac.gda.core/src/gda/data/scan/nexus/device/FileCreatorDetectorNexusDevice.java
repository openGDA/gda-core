/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;

/**
 * An instance of this class wraps a {@link Detector} to implement {@link INexusDevice} for detector
 * where {@link Detector#createsOwnFiles()} returns <code>true</code>. This code is derived from
 * {@link NexusDataWriter}.makeFileCreatorDetector()
 */
public class FileCreatorDetectorNexusDevice extends AbstractDetectorNexusDeviceAdapter {

	private ILazyWriteableDataset fileNameDataset;

	public FileCreatorDetectorNexusDevice(Detector detector) {
		super(detector);
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return null; // no NXdata group is created for file creator detectors
	}

	@Override
	protected void writeMetaDataFields(NXdetector detGroup, Detector detector) throws DeviceException {
		// TODO DAQ-3203: make the metadata writing consistent with other kinds of detectors?
		detGroup.setDescriptionScalar("Generic GDA Detector - External Files");
		detGroup.setTypeScalar("Detector");
	}

	@Override
	protected void writeDataFields(NexusScanInfo info, NXdetector detGroup) throws NexusException {
		final NXnote dataFileNote = NexusNodeFactory.createNXnote();
		detGroup.setData_file(dataFileNote);

		fileNameDataset = dataFileNote.initializeLazyDataset(NXnote.NX_FILE_NAME, info.getRank(), String.class);
		fileNameDataset.setChunking(info.createChunk(false, 8));
		fileNameDataset.setWritingAsync(true);
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		try {
			IWritableNexusDevice.writeDataset(fileNameDataset, data, scanSlice);
		} catch (DatasetException e) {
			throw new NexusException("Could not write data for detector " + getName());
		}
	}

	@Override
	public void scanEnd() throws NexusException {
		fileNameDataset = null;
	}

}
