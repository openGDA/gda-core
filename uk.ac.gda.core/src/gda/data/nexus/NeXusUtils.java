/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.nexus;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.nexus.extractor.NexusExtractor;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.util.Version;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with NeXus files.
 */
public class NeXusUtils {

	private static final Logger logger = LoggerFactory.getLogger(NexusUtils.class);

	private final static String DEFAULT_NUMBER_VALUE = "-1.0";

	/**
	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
	 * (CCD) would have a rank=3.
	 *
	 * @param detector
	 * @return int
	 * @throws DeviceException
	 */
	public static int getRank(Detector detector) throws DeviceException {
		return getRank(detector.getDataDimensions());
	}

	/**
	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
	 * (CCD) would have a rank=3.
	 * @param detectorDataDimensions
	 * @return int
	 */
	public static int getRank(int[] detectorDataDimensions) {
		int rank = 0;

		// The rank is just the dimensionality of the data and the scan dimension - hence the +1
		rank = detectorDataDimensions.length+1;
		return rank;
	}

	/**
	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
	 * (CCD) would have a rank=3.
	 *
	 * @param width
	 * @param height
	 * @return int
	 */
	public static int getRank(int width, int height)  {
		int rank = 0;

		if (width > 1) {
			if (height > 1) {
				rank = 3;
			} else {
				rank = 2;
			}
		} else {
			rank = 1;
		}
		return rank;
	}

	/**
	 * Returns the dimensions array for a given detector that can be used by the NeXus API to create a data section.
	 *
	 * @param detector
	 * @return int[]
	 * @throws DeviceException
	 */
	public static int[] getDim(Detector detector) throws DeviceException {
		int[] detectorDataDimensions;
		int[] iDim;

		detectorDataDimensions = detector.getDataDimensions();

		iDim = getDim(detectorDataDimensions);

		return iDim;
	}

	/**
	 * Returns the dimensions array for a given width/height of data that can be used by the NeXus API to create a data
	 * section.
	 *
	 * @param dimension
	 * @return int[]
	 */
	public static int[] getDim(int[] dimension) {
		int[] iDim = new int[dimension.length + 1];
		iDim[0] = ILazyWriteableDataset.UNLIMITED;

		for (int i = 0; i < dimension.length; i++) {
			iDim[i + 1] = dimension[i];
		}

		return iDim;
	}

	/**
	 * Routine for writing the elements required for the XESraw definition to a given NX_ENTRY
	 *
	 * @param file
	 * @param entryName
	 * @throws NexusException
	 * @throws IOException
	 */
	public static void writeXESraw(NexusFile file, String entryName) throws NexusException, IOException {

		String beamline = LocalProperties.get("gda.instrument", "base");
		NumTracker runNumber = new NumTracker(beamline);
		Metadata metadata = GDAMetadataProvider.getInstance();

		// First lets check to see if the entry exists
		GroupNode group = file.getGroup(entryName, false);
		logger.debug("NeXus: Applying XESraw to NXentry " + entryName);

		try {
			NexusUtils.writeString(file, group, "title", metadata.getMetadataValue(GDAMetadataProvider.TITLE));
			NexusUtils.writeString(file, group, "investigation", metadata.getMetadataValue(GDAMetadataProvider.INVESTIGATION));
			NexusUtils.writeString(file, group, "proposal_identifier", metadata.getMetadataValue(GDAMetadataProvider.PROPOSAL));
			NexusUtils.writeString(file, group, "experiment_identifier", metadata.getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER));
			NexusUtils.writeString(file, group, "experiment_description",metadata.getMetadataValue(GDAMetadataProvider.EXPERIMENT_DESCRIPTION));
			NexusUtils.writeString(file, group, "collection_identifier", metadata.getMetadataValue(GDAMetadataProvider.COLLECTION_IDENTIFIER));
			NexusUtils.writeString(file, group, "collection_description", metadata.getMetadataValue(GDAMetadataProvider.COLLECTION_DESCRIPTION));
			NexusUtils.writeString(file, group, "run_cycle", metadata.getMetadataValue(GDAMetadataProvider.FACILITY_RUN_CYCLE));
			/* to allow unit tests to check for differences in files we need a way of forcing a certain version number */
			if (LocalProperties.check("gda.data.scan.datawriter.setTime0")){
				NexusUtils.writeString(file, group, "program_name", "GDA 7.11.0");
			} else {
				NexusUtils.writeString(file, group, "program_name", "GDA " + Version.getRelease());
			}

//			writeGeneralMetaData(file, metadata);


		} catch (DeviceException e) {
			logger.warn("XESraw: Problem reading one or more items of metadata.");
		}

		// Run Number
		// writeNexusInteger(file, "entry_identifier", (int) runNumber.getCurrentFileNumber());
		NexusUtils.writeString(file, group, "entry_identifier", String.valueOf(runNumber.getCurrentFileNumber()));

		// Write the NXuser
		write_NXuser(file, group);

		// Make the NXinstrument
		write_NXinstrument(file, group);

		// TODO Check to see if there is a bending magnet or insertion device

		// TODO write NXsample

	}

	/**
	 * Creates an NXinstrument entry at the current position in the NeXus file.
	 *
	 * @param file
	 *            The NeXus file handle
	 * @param group
	 * @throws NexusException
	 */
	public static void write_NXinstrument(NexusFile file, GroupNode group) throws NexusException {

		String beamline = LocalProperties.get("gda.instrument", "base");

		// Make instrument if it's not there.
		group = file.getGroup(group, "instrument", NexusExtractor.NXInstrumentClassName, true);

		NexusUtils.writeString(file, group, "name", beamline);

		// These are other components that we may or may not have values for...
		write_NXmonochromator(file, group);

		write_NXinsertion_device(file, group);

		// Make the source
		write_NXsource(file, group);

	}

	/**
	 * Creates an NXuser(s) entry at the current position in the NeXus file.
	 *
	 * @param file
	 *            The NeXus file handle
	 * @param group
	 * @throws NexusException
	 */
	public static void write_NXuser(NexusFile file, GroupNode group) throws NexusException {

		Metadata metadata = GDAMetadataProvider.getInstance();

		// TODO At the moment we will use the old implementation. Need to get a list of users from the ICAT.
		group = file.getGroup(group, "user01", "NXuser", true);

		try {
			NexusUtils.writeString(file, group, "username", metadata.getMetadataValue("federalid"));
		} catch (DeviceException e) {
			logger.warn("NXuser: Problem reading one or more items of metadata.");
		}
	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXsource(NexusFile file, GroupNode group) throws NexusException {
		Metadata metadata = GDAMetadataProvider.getInstance();

		// Make the source if it's not there.
		group = file.getGroup(group, "source", "NXsource", true);

		try {
			NexusUtils.writeString(file, group, "name", metadata.getMetadataValue("facility.name", "gda.facility", "DLS"));
			NexusUtils.writeString(file, group, "type", metadata.getMetadataValue("facility.type", "gda.facility.type", "Synchrotron X-ray Source"));
			NexusUtils.writeString(file, group, "probe", metadata.getMetadataValue("facility.probe", "gda.facility.probe", "x-ray"));
			if (!metadata.getMetadataValue("instrument.source.energy").isEmpty())
				NexusUtils.writeDouble(file, group, "energy", Double.parseDouble(metadata.getMetadataValue("instrument.source.energy",	null, DEFAULT_NUMBER_VALUE)), "GeV");
			if (!metadata.getMetadataValue("instrument.source.current").isEmpty())
				NexusUtils.writeDouble(file, group, "current", Double.parseDouble(metadata.getMetadataValue("instrument.source.current",	null, DEFAULT_NUMBER_VALUE)), "mA");
		} catch (DeviceException e) {
			logger.warn("NXsource: Problem reading one or more items of metadata.");
		}
	}

	/**
	 * @param list
	 * @return true if any of the items in the list are defined in the metadata list.
	 * @throws DeviceException
	 */
	public static boolean findMetaItems(ArrayList<String> list) throws DeviceException {

		Metadata metadata = GDAMetadataProvider.getInstance();

		for (String string : list) {
			if (!metadata.getMetadataValue(string).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXmonochromator(NexusFile file, GroupNode group) throws NexusException {
		Metadata metadata = GDAMetadataProvider.getInstance();
		boolean found = false;

		ArrayList<String> items = new ArrayList<String>();
		items.add("instrument.monochromator.name");
		items.add("instrument.monochromator.wavelength");
		items.add("instrument.monochromator.energy");

		// Have a look to see if any of the metadata items have been defined.
		try {
			found = findMetaItems(items);
		} catch (DeviceException e) {
			logger.warn("NXmonochromator: Problem reading one or more items of metadata.");
		}

		if (found) {
			// If we can't find the metadata object, then just create a blank one so that
			// we fail gracefully on reading any metadata items.

			// Make the source if it's not there.
			group = file.getGroup(group, "monochromator", "NXmonochromator", true);

			try {
				if (metadata.getMetadataValue("instrument.monochromator.name") != null)
					NexusUtils.writeString(file, group, "name", metadata.getMetadataValue("instrument.monochromator.name"));
				if (metadata.getMetadataValue("instrument.monochromator.energy") != null) {
					NexusUtils.writeDouble(file, group, "energy", Double.parseDouble(metadata.getMetadataValue("instrument.monochromator.energy")), "keV");
				}
				if (metadata.getMetadataValue("instrument.monochromator.wavelength") != null) {
					NexusUtils.writeDouble(file, group, "wavelength", Double.parseDouble(metadata.getMetadataValue("instrument.monochromator.wavelength")), "Angstrom");
				}

			} catch (DeviceException e) {
				logger.warn("NXmonochromator: Problem reading one or more items of metadata.");
			}
		}
	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXinsertion_device(NexusFile file, GroupNode group) throws NexusException {
		boolean found = false;
		Metadata metadata = GDAMetadataProvider.getInstance();

		ArrayList<String> items = new ArrayList<String>();
		items.add("instrument.insertion_device.name");
		items.add("instrument.insertion_device.type");
		items.add("instrument.insertion_device.gap");
		items.add("instrument.insertion_device.taper");
		items.add("instrument.insertion_device.phase");
		items.add("instrument.insertion_device.poles");
		items.add("instrument.insertion_device.length");
		items.add("instrument.insertion_device.power");
		items.add("instrument.insertion_device.energy");
		items.add("instrument.insertion_device.bandwidth");
		items.add("instrument.insertion_device.harmonic");
		items.add("instrument.insertion_device.spectrum");

		// Have a look to see if any of the metadata items have been defined.
		try {
			found = findMetaItems(items);
		} catch (DeviceException e) {
			logger.warn("NXinsertion_device: Problem reading one or more items of metadata.");
		}

		if (found) {
			// If we can't find the metadata object, then just create a blank one so that
			// we fail gracefully on reading any metadata items.

			// Make the group if it's not there.
			group = file.getGroup(group, "insertion_device", "NXinsertion_device", true);

			try {
				String gap = metadata.getMetadataValue("instrument.insertion_device.gap");
				if (gap != null) {
					NexusUtils.writeDouble(file, group, "gap", Double.parseDouble(gap), "mm");
				}
			} catch (DeviceException e) {
				logger.warn("NXinsertion_device: Problem reading one or more items of metadata.");
			}
			// name
			// type
			// taper
			// phase
			// poles
			// length
			// power
			// energy
			// bandwidth
			// harmonic
			// spectrum

		}
	}

	/**
	 * @param file
	 * @param group
	 * @throws NexusException
	 */
	public static void write_NXbending_magnet(NexusFile file, GroupNode group) throws NexusException {
		Metadata metadata = GDAMetadataProvider.getInstance();
		boolean found = false;

		// Elements of this component
		String name = "";
		double critical_energy = Double.parseDouble(DEFAULT_NUMBER_VALUE);
		double bending_radius = Double.parseDouble(DEFAULT_NUMBER_VALUE);
		double[] spectrum;

		ArrayList<String> items = new ArrayList<String>();
		items.add("instrument.bending_magnet.name");
		items.add("instrument.bending_magnet.bending_radius");
		items.add("instrument.bending_magnet.critical_energy");
		items.add("instrument.bending_magnet.spectrum");

		// Have a look to see if any of the metadata items have been defined.
		try {
			found = findMetaItems(items);
		} catch (DeviceException e) {
			logger.warn("NXbending_magnet: Problem reading one or more items of metadata.");
		}
		if (found) {

			// TODO Check to see if there are any Bending Magnet fields. Or maybe just one to indicate a BM ?

			// Make the source if it's not there.
			group = file.getGroup(group, "bending_magnet", "NXbending_magnet", true);

			// Lets get the metadata values.
			try {
				name = metadata.getMetadataValue("instrument.bending_magnet.name");
				bending_radius = Double.parseDouble(metadata.getMetadataValue(
						"instrument.bending_magnet.bending_radius", null, DEFAULT_NUMBER_VALUE));
				critical_energy = Double.parseDouble(metadata.getMetadataValue(
						"instrument.bending_magnet.critical_energy", null, DEFAULT_NUMBER_VALUE));
				String spectrumStr = metadata.getMetadataValue("instrument.bending_magnet.spectrum", null,
						DEFAULT_NUMBER_VALUE);
				String[] spectrumArray = spectrumStr.split(" ");
				spectrum = new double[spectrumArray.length];
				for (int i = 0; i < spectrumArray.length; i++) {
					spectrum[i] = Double.parseDouble(spectrumArray[i]);
				}

				// Now lets write these to the file
				NexusUtils.writeString(file, group, "name", name);
				NexusUtils.writeDouble(file, group, "bending_radius", bending_radius);
				NexusUtils.writeDouble(file, group, "critical_energy", critical_energy);

				group = file.getGroup(group, "spectrum", NexusExtractor.NXDataClassName, true);
				NexusUtils.writeDoubleArray(file, group, "data", spectrum);
			} catch (DeviceException e) {
				logger.warn("NXbending_magnet: Problem reading one or more items of metadata.");
			}
		}
	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXsample(NexusFile file, GroupNode group) throws NexusException {
		// TODO Check to see if there are any Sample fields.

		// Make the source if it's not there.
		group = file.getGroup(group, "sample", "NXsample", true);

		// name
		// chemical_formula
		// temperature
		// electric_field
		// magnetic_field
		// stress_field
		// pressure
		// changer_position
		// unit_cell
		// unit_cell_volume
		// sample_orientation
		// orientation_matrix
		// mass
		// density
		// relative_molecular_mass
		// type
		// description
		// preparation_date
		// ...

	}

	/**
	 * Routine for writing the elements required for Small Angle Scattering definition to a given NX_ENTRY.
	 *
	 * @param file
	 * @param entryName
	 */
	public static void writeSAS(@SuppressWarnings("unused") NexusFile file, @SuppressWarnings("unused") String entryName) {
		// Not implemented yet - give me chance!
		logger.warn("SAS Definition: Trying to use an unimplemented feature.");
	}

	/**
	 * Routine for writing the elements required for XAS/EXAFS definition to a given NX_ENTRY.
	 *
	 * @param file
	 * @param entryName
	 */
	public static void writeXAS(@SuppressWarnings("unused") NexusFile file, @SuppressWarnings("unused") String entryName) {
		// Not implemented yet - give me chance!
		logger.warn("XAS Definition: Trying to use an unimplemented feature.");
	}
}
