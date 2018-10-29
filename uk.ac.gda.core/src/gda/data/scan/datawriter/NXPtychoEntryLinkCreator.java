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

package gda.data.scan.datawriter;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.springframework.beans.factory.InitializingBean;

import gda.data.nexus.extractor.NexusExtractor;

/**
 *
 */
public class NXPtychoEntryLinkCreator extends NXLinkCreator implements InitializingBean {

	private static final String entry_definition = "NXptycho";
	private static final String entry_name = "ptycho_entry";

	// the data group
	private String data_data_target = "";
	private String data_lab_x_target = "";
	private String data_lab_y_target = "";
	private String data_theta_target = "";

	// the instrument group
	private String instrument_detector_data_target = "";
	private Double instrument_detector_x_pixel_size = Double.NaN;
	private Double instrument_detector_y_pixel_size = Double.NaN;
	private String instrument_detector_x_pixel_size_units = "undefined";
	private String instrument_detector_y_pixel_size_units = "undefined";
	private Double instrument_detector_distance = Double.NaN;
	private String instrument_detector_distance_units = "undefined";

	private String instrument_source_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource";
	private String instrument_source_current_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/current:SDS";
	private String instrument_source_energy_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/energy:SDS";
	private String instrument_source_name_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/name:SDS";
	private String instrument_source_probe_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/probe:SDS";
	private String instrument_source_type_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/type:SDS";

	private String instrument_monochromator_energy_target = "entry1:NXentry/scan_identifier:SDS";
	private String instrument_monochromator_wavelength_target = "entry1:NXentry/scan_identifier:SDS";

	// the sample group
	private String sample_name_target = "entry1:NXentry/scan_identifier:SDS";
	private String sample_transformations_lab_xy_lab_x_target = "";
	private String sample_transformations_lab_xy_lab_y_target = "";
	private String sample_transformations_theta_target = "";

	// other
	private String title_target = "entry1:NXentry/scan_identifier:SDS";

	// the data group

	// the instrument group
	public void setInstrument_detector_data_target(String instrument_detector_data_target) {
		this.instrument_detector_data_target = instrument_detector_data_target;
		this.data_data_target = this.instrument_detector_data_target;
	}

	public String getInstrument_detector_data_target() {
		return instrument_detector_data_target;
	}

	public void setInstrument_detector_x_pixel_size(Double instrument_detector_x_pixel_size) {
		this.instrument_detector_x_pixel_size = instrument_detector_x_pixel_size;
	}

	public Double getInstrument_detector_x_pixel_size() {
		return instrument_detector_x_pixel_size;
	}

	public void setInstrument_detector_x_pixel_size_units(String instrument_detector_x_pixel_size_units) {
		this.instrument_detector_x_pixel_size_units = instrument_detector_x_pixel_size_units;
	}

	public String getInstrument_detector_x_pixel_size_units() {
		return instrument_detector_x_pixel_size_units;
	}

	public void setInstrument_detector_y_pixel_size(Double instrument_detector_y_pixel_size) {
		this.instrument_detector_y_pixel_size = instrument_detector_y_pixel_size;
	}

	public Double getInstrument_detector_y_pixel_size() {
		return instrument_detector_y_pixel_size;
	}

	public void setInstrument_detector_y_pixel_size_units(String instrument_detector_y_pixel_size_units) {
		this.instrument_detector_y_pixel_size_units = instrument_detector_y_pixel_size_units;
	}

	public String getInstrument_detector_y_pixel_size_units() {
		return instrument_detector_y_pixel_size_units;
	}

	public void setInstrument_detector_distance(Double instrument_detector_distance) {
		this.instrument_detector_distance = instrument_detector_distance;
	}
	public Double getInstrument_detector_distance() {
		return instrument_detector_distance;
	}

	public void setInstrument_monochromator_energy_target(String instrument_monochromator_energy_target) {
		this.instrument_monochromator_energy_target = instrument_monochromator_energy_target;
	}
	public String getInstrument_monochromator_energy_target() {
		return instrument_monochromator_energy_target;
	}

	public void setInstrument_monochromator_wavelength_target(String instrument_monochromator_wavelength_target) {
		this.instrument_monochromator_wavelength_target = instrument_monochromator_wavelength_target;
	}
	public String getInstrument_monochromator_wavelength_target() {
		return instrument_monochromator_wavelength_target;
	}

	public void setInstrument_source_target(String instrument_source_target) {
		this.instrument_source_target = instrument_source_target;

		this.instrument_source_current_target = this.instrument_source_target + "/current:SDS";
		this.instrument_source_energy_target = this.instrument_source_target + "/energy:SDS";
		this.instrument_source_name_target = this.instrument_source_target + "/name:SDS";
		this.instrument_source_probe_target = this.instrument_source_target + "/probe:SDS";
		this.instrument_source_type_target = this.instrument_source_target + "/type:SDS";
	}

	public String getInstrument_source_target() {
		return instrument_source_target;
	}

	public void setInstrument_detector_distance_units(String instrument_detector_distance_units) {
		this.instrument_detector_distance_units = instrument_detector_distance_units;
	}
	public String getInstrument_detector_distance_units() {
		return instrument_detector_distance_units;
	}

	// the sample group
	public void setSample_name_target(String sample_name_target) {
		this.sample_name_target = sample_name_target;
	}

	public String getSample_name_target() {
		return sample_name_target;
	}

	public void setSample_transformations_lab_xy_lab_x_target(String sample_transformations_lab_xy_lab_x_target) {
		this.sample_transformations_lab_xy_lab_x_target = sample_transformations_lab_xy_lab_x_target;
		this.data_lab_x_target = this.sample_transformations_lab_xy_lab_x_target;
	}

	public String getSample_transformations_lab_xy_lab_x_target() {
		return sample_transformations_lab_xy_lab_x_target;
	}

	public void setSample_transformations_lab_xy_lab_y_target(String sample_transformations_lab_xy_lab_y_target) {
		this.sample_transformations_lab_xy_lab_y_target = sample_transformations_lab_xy_lab_y_target;
		this.data_lab_y_target = this.sample_transformations_lab_xy_lab_y_target;
	}

	public String getSample_transformations_lab_xy_lab_y_target() {
		return sample_transformations_lab_xy_lab_y_target;
	}

	public void setSample_transformations_theta_target(String sample_transformations_theta_target) {
		this.sample_transformations_theta_target = sample_transformations_theta_target;
		this.data_theta_target = this.sample_transformations_theta_target;
	}

	public String getSample_transformations_theta_target() {
		return sample_transformations_theta_target;
	}

	// other
	public void setTitle_target(String title_target) {
		this.title_target = title_target;
	}

	public String getTitle_target() {
		return title_target;
	}




	@Override
	public void afterPropertiesSet() throws Exception {
		// instrument/detector/data
		if (this.getInstrument_detector_data_target() == null)
			throw new IllegalStateException("instrument_detector_data_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/detector:NXdetector/data", getInstrument_detector_data_target());

		// instrument/source
		if (this.getInstrument_source_target() == null)
			throw new IllegalStateException("instrument_source_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/source:NXsource/current", this.instrument_source_current_target);
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/source:NXsource/energy", this.instrument_source_energy_target);
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/source:NXsource/name", this.instrument_source_name_target);
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/source:NXsource/probe", this.instrument_source_probe_target);
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/source:NXsource/type", this.instrument_source_type_target);

		// instrument/monochromator/energy
		if (this.getInstrument_monochromator_energy_target() == null)
			throw new IllegalStateException("instrument_monochromator_energy_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/monochromator:NXmonochromator/energy", this.instrument_monochromator_energy_target);

		// instrument/monochromator/wavelength
		if (this.getInstrument_monochromator_wavelength_target() == null)
			throw new IllegalStateException("instrument_monochromator_wavelength_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/instrument:NXinstrument/monochromator:NXmonochromator/wavelength", this.instrument_monochromator_wavelength_target);

		// sample/name
		if (this.getSample_name_target() == null)
			throw new IllegalStateException("sample_name_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/sample:NXsample/name", getSample_name_target());

		// data/data
		if (this.data_data_target == null)
			throw new IllegalStateException("data_data_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/data:NXdata/data", this.data_data_target);

		// data/theta
		if (this.data_theta_target == null)
			throw new IllegalStateException("data_theta_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/data:NXdata/theta", this.data_theta_target);

		// title
		if (this.getTitle_target() == null)
			throw new IllegalStateException("title_target is not set");
		addLink("/entry1:NXentry/"+NXPtychoEntryLinkCreator.entry_name+":NXsubentry/title", getTitle_target());
	}

	public void writeStringData(String filename, String dataName, String dataValue) throws Exception {
		NexusFile file = NexusFileHDF5.openNexusFile(filename);
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), "entry1", NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, NXPtychoEntryLinkCreator.entry_name, "NXsubentry");
//		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);
		NexusUtils.writeString(file, group, dataName, dataValue);
		file.close();
	}

	public void writeDoubleData(String filename, String dataName, double dataValue, String dataUnits) throws Exception {
		NexusFile file = NexusFileHDF5.openNexusFile(filename);
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), "entry1", NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, NXPtychoEntryLinkCreator.entry_name, "NXsubentry");
//		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);
		NexusUtils.writeDouble(file, group, dataName, dataValue, dataUnits);
		file.close();
	}

	@Override
	public void makelinks(String filename) throws Exception {

		super.makelinks(filename);

		// workaround: make non-link(s) as well
		writeDoubleData(filename, "instrument/detector/x_pixel_size", this.instrument_detector_x_pixel_size, this.instrument_detector_x_pixel_size_units);
		writeDoubleData(filename, "instrument/detector/y_pixel_size", this.instrument_detector_y_pixel_size, this.instrument_detector_y_pixel_size_units);
		writeDoubleData(filename, "instrument/detector/distance", this.instrument_detector_distance, this.instrument_detector_distance_units);

		writeStringData(filename, "definition", NXPtychoEntryLinkCreator.entry_definition);
	}
}
