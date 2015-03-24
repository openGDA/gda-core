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
import gda.device.Detector;
import gda.device.DeviceException;
import gda.util.Version;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with NeXus files.
 */
public class NeXusUtils {

	private static final Logger logger = LoggerFactory.getLogger(NexusUtils.class);

	private final static String DEFAULT_NUMBER_VALUE = "-1.0";
	@SuppressWarnings("unused")
	private final static String DEFAULT_STRING_VALUE = "Not Defined";

	private static enum DataType {
		/** Double */
		DOUBLE,
		/** Double Array */
		DOUBLE_ARRAY,
		/** Integer */
		INTEGER,
		/** Integer Array */
		INTEGER_ARRAY,
		/** Long */
		LONG,
		/** Long Array */
		LONG_ARRAY,
		/** String */
		STRING,
		/** Boolean */
		BOOLEAN,
		/** NXdata Class */
		NXDATA
	}

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
		iDim[0] = NexusGlobals.NX_UNLIMITED;

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
	public static void writeXESraw(NexusFileInterface file, String entryName) throws NexusException, IOException {

		String beamline = LocalProperties.get("gda.instrument", "base");
		NumTracker runNumber = new NumTracker(beamline);
		Metadata metadata = GDAMetadataProvider.getInstance();

		// First lets check to see if the entry exists
		if (file.groupdir().get(entryName) == null) {
			throw new NexusException("Creating XESraw: specified entry does not exist!");
		}

		logger.debug("NeXus: Applying XESraw to NXentry " + entryName);

		// Now lets open the entry
		file.opengroup(entryName, "NXentry");

		try {
			NexusUtils.writeNexusString(file, "title", metadata.getMetadataValue(GDAMetadataProvider.TITLE));
			NexusUtils.writeNexusString(file, "investigation", metadata.getMetadataValue(GDAMetadataProvider.INVESTIGATION));
			NexusUtils.writeNexusString(file, "proposal_identifier", metadata.getMetadataValue(GDAMetadataProvider.PROPOSAL));
			NexusUtils.writeNexusString(file, "experiment_identifier", metadata.getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER));
			NexusUtils.writeNexusString(file, "experiment_description",metadata.getMetadataValue(GDAMetadataProvider.EXPERIMENT_DESCRIPTION));
			NexusUtils.writeNexusString(file, "collection_identifier", metadata.getMetadataValue(GDAMetadataProvider.COLLECTION_IDENTIFIER));
			NexusUtils.writeNexusString(file, "collection_description", metadata.getMetadataValue(GDAMetadataProvider.COLLECTION_DESCRIPTION));
			NexusUtils.writeNexusString(file, "run_cycle", metadata.getMetadataValue(GDAMetadataProvider.FACILITY_RUN_CYCLE));
			/* to allow unit tests to check for differences in files we need a way of forcing a certain version number */
			if (LocalProperties.check("gda.data.scan.datawriter.setTime0")){
				NexusUtils.writeNexusString(file, "program_name", "GDA 7.11.0");
			} else {
				NexusUtils.writeNexusString(file, "program_name", "GDA " + Version.getReleaseVersion());
			}

//			writeGeneralMetaData(file, metadata);
						
			
		} catch (DeviceException e) {
			logger.warn("XESraw: Problem reading one or more items of metadata.");
		}

		// Run Number
		// writeNexusInteger(file, "entry_identifier", (int) runNumber.getCurrentFileNumber());
		NexusUtils.writeNexusString(file, "entry_identifier", String.valueOf(runNumber.getCurrentFileNumber()));

		// Write the NXuser
		write_NXuser(file);

		// Make the NXinstrument
		write_NXinstrument(file);

		// TODO Check to see if there is a bending magnet or insertion device

		// TODO write NXsample

		// Close the NXentry
		file.closegroup();
	}

	/**
	 * Creates an NXinstrument entry at the current position in the NeXus file.
	 * 
	 * @param file
	 *            The NeXus file handle
	 * @throws NexusException
	 */
	public static void write_NXinstrument(NexusFileInterface file) throws NexusException {

		String beamline = LocalProperties.get("gda.instrument", "base");

		// Make instrument if it's not there.
		if (file.groupdir().get("instrument") == null) {
			logger.debug("NeXus: Creating NXinstrument");
			file.makegroup("instrument", "NXinstrument");
		}
		// Open the NXinstrument
		file.opengroup("instrument", "NXinstrument");

		NexusUtils.writeNexusString(file, "name", beamline);

		// These are other components that we may or may not have values for...
		write_NXmonochromator(file);

		write_NXinsertion_device(file);
		
		// Make the source
		write_NXsource(file);

		// Close the NXinstrument group before returning, so that we are at the same point in the file.
		file.closegroup();

	}

	/**
	 * Creates an NXuser(s) entry at the current position in the NeXus file.
	 * 
	 * @param file
	 *            The NeXus file handle
	 * @throws NexusException
	 */
	public static void write_NXuser(NexusFileInterface file) throws NexusException {

		Metadata metadata = GDAMetadataProvider.getInstance();

		// TODO At the moment we will use the old implementation. Need to get a list of users from the ICAT.
		file.makegroup("user01", "NXuser");
		file.opengroup("user01", "NXuser");

		try {
			NexusUtils.writeNexusString(file, "username", metadata.getMetadataValue("federalid"));
		} catch (DeviceException e) {
			logger.warn("NXuser: Problem reading one or more items of metadata.");
		}

		file.closegroup();
	}
		
	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXsource(NexusFileInterface file) throws NexusException {
		Metadata metadata = GDAMetadataProvider.getInstance();

		// Make the source if it's not there.
		if (file.groupdir().get("source") == null) {
			logger.debug("NeXus: Creating NXsource");
			file.makegroup("source", "NXsource");
		}
		// Open the NXsource
		file.opengroup("source", "NXsource");

		try {
			NexusUtils.writeNexusString(file, "name", metadata.getMetadataValue("facility.name", "gda.facility", "DLS"));
			NexusUtils.writeNexusString(file, "type", metadata.getMetadataValue("facility.type", "gda.facility.type", "Synchrotron X-ray Source"));
			NexusUtils.writeNexusString(file, "probe", metadata.getMetadataValue("facility.probe", "gda.facility.probe", "x-ray"));
			if (!metadata.getMetadataValue("instrument.source.energy").isEmpty())
				NexusUtils.writeNexusDouble(file, "energy", Double.parseDouble(metadata.getMetadataValue("instrument.source.energy",	null, DEFAULT_NUMBER_VALUE)), "GeV");
			if (!metadata.getMetadataValue("instrument.source.current").isEmpty())
				NexusUtils.writeNexusDouble(file, "current", Double.parseDouble(metadata.getMetadataValue("instrument.source.current",	null, DEFAULT_NUMBER_VALUE)), "mA");
		} catch (DeviceException e) {
			logger.warn("NXsource: Problem reading one or more items of metadata.");
		}

		// Close the NXsource group before returning, so that we are at the same point in the file.
		file.closegroup();
	}

	/**
	 * @param list
	 * @return true if any of the items in the list are defined in the metadata list.
	 * @throws DeviceException
	 */
	public static boolean findMetaItems(ArrayList<String> list) throws DeviceException {

		Metadata metadata = GDAMetadataProvider.getInstance();

		for (String string : list) {
			if ((metadata.getMetadataValue(string)).isEmpty() == false) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXmonochromator(NexusFileInterface file) throws NexusException {
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
			if (file.groupdir().get("monochromator") == null) {
				logger.debug("NeXus: Creating NXmonochromator");
				file.makegroup("monochromator", "NXmonochromator");
			}
			// Open the NXmonochromator
			file.opengroup("monochromator", "NXmonochromator");

			try {
				if (metadata.getMetadataValue("instrument.monochromator.name") != null)
					NexusUtils.writeNexusString(file, "name", metadata.getMetadataValue("instrument.monochromator.name"));
				if (metadata.getMetadataValue("instrument.monochromator.energy") != null) {
					NexusUtils.writeNexusDouble(file, "energy", Double.parseDouble(metadata.getMetadataValue("instrument.monochromator.energy")), "keV");
				} 
				if (metadata.getMetadataValue("instrument.monochromator.wavelength") != null) {
					NexusUtils.writeNexusDouble(file, "wavelength", Double.parseDouble(metadata.getMetadataValue("instrument.monochromator.wavelength")), "Angstrom");
				}

			} catch (DeviceException e) {
				logger.warn("NXmonochromator: Problem reading one or more items of metadata.");
			}

			// Close the NXmonochromator
			file.closegroup();
		}

	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXinsertion_device(NexusFileInterface file) throws NexusException {
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
			if (file.groupdir().get("insertion_device") == null) {
				logger.debug("NeXus: Creating NXinsertion_device");
				file.makegroup("insertion_device", "NXinsertion_device");
			}
			// Open the NXinsertion_device
			file.opengroup("insertion_device", "NXinsertion_device");

			try {
				String gap = metadata.getMetadataValue("instrument.insertion_device.gap");
				if (gap != null) {
					NexusUtils.writeNexusDouble(file, "gap", Double.parseDouble(gap), "mm");
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

			// Close the NXinsertion_device
			file.closegroup();
		}
	}

	/**
	 * @param file
	 * @param names
	 * @param datatypes
	 * @throws NexusException
	 */
	@SuppressWarnings("unused")
	public static void writeMetadataList(NexusFileInterface file, ArrayList<String> names, ArrayList<DataType> datatypes)
			throws NexusException {
		Metadata metadata = GDAMetadataProvider.getInstance();
		// for (String item : names) {
		// try {
		// String value = metadata.getMetadataValue(item);
		// if
		// } catch (DeviceException e) {
		// logger.warn("Problem reading one or more items of metadata.");
		// }
		// }

	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXbending_magnet(NexusFileInterface file) throws NexusException {
		Metadata metadata = GDAMetadataProvider.getInstance();
		boolean found = false;

		// Elements of this component
		String name = "";
		Double critical_energy = Double.parseDouble(DEFAULT_NUMBER_VALUE);
		Double bending_radius = Double.parseDouble(DEFAULT_NUMBER_VALUE);
		Double[] spectrum;

		ArrayList<String> items = new ArrayList<String>();
		items.add("instrument.bending_magnet.name");
		items.add("instrument.bending_magnet.bending_radius");
		items.add("instrument.bending_magnet.critical_energy");
		items.add("instrument.bending_magnet.spectrum");

		ArrayList<DataType> datatype = new ArrayList<DataType>();
		datatype.add(DataType.STRING);
		datatype.add(DataType.DOUBLE);
		datatype.add(DataType.DOUBLE);
		datatype.add(DataType.NXDATA);

		// Have a look to see if any of the metadata items have been defined.
		try {
			found = findMetaItems(items);
		} catch (DeviceException e) {
			logger.warn("NXbending_magnet: Problem reading one or more items of metadata.");
		}
		if (found) {

			// TODO Check to see if there are any Bending Magnet fields. Or maybe just one to indicate a BM ?

			// Make the source if it's not there.
			if (file.groupdir().get("bending_magnet") == null) {
				logger.debug("NeXus: Creating NXbending_magnet");
				file.makegroup("bending_magnet", "NXbending_magnet");
			}
			// Open the NXbending_magnet
			file.opengroup("bending_magnet", "NXbending_magnet");

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
				spectrum = new Double[spectrumArray.length];
				for (int i = 0; i < spectrumArray.length; i++) {
					spectrum[i] = Double.parseDouble(spectrumArray[i]);
				}

				// Now lets write these to the file
				NexusUtils.writeNexusString(file, "name", name);
				NexusUtils.writeNexusDouble(file, "bending_radius", bending_radius);
				NexusUtils.writeNexusDouble(file, "critical_energy", critical_energy);

				file.makegroup("spectrum", "NXdata");
				file.opengroup("spectrum", "NXdata");
				NexusUtils.writeNexusDoubleArray(file, "data", spectrum);
				file.closegroup();

			} catch (DeviceException e) {
				logger.warn("NXbending_magnet: Problem reading one or more items of metadata.");
			}

			// Close the NXbending_magnet
			file.closegroup();
		}
	}

	/**
	 * @param file
	 * @throws NexusException
	 */
	public static void write_NXsample(NexusFileInterface file) throws NexusException {
		// TODO Check to see if there are any Sample fields.

		// Make the source if it's not there.
		if (file.groupdir().get("sample") == null) {
			logger.debug("NeXus: Creating NXsample");
			file.makegroup("sample", "NXsample");
		}
		// Open the NXsample
		file.opengroup("sample", "NXsample");

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

		// Close the NXsample
		file.closegroup();
	}

	/**
	 * Routine for writing the elements required for Small Angle Scattering definition to a given NX_ENTRY.
	 * 
	 * @param file
	 * @param entryName
	 */
	public static void writeSAS(@SuppressWarnings("unused") NexusFileInterface file, @SuppressWarnings("unused") String entryName) {
		// Not implemented yet - give me chance!
		logger.warn("SAS Definition: Trying to use an unimplemented feature.");
	}

	/**
	 * Routine for writing the elements required for XAS/EXAFS definition to a given NX_ENTRY.
	 * 
	 * @param file
	 * @param entryName
	 */
	public static void writeXAS(@SuppressWarnings("unused") NexusFileInterface file, @SuppressWarnings("unused") String entryName) {
		// Not implemented yet - give me chance!
		logger.warn("XAS Definition: Trying to use an unimplemented feature.");
	}
}