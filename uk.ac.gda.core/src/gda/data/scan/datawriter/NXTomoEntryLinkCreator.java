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
public class NXTomoEntryLinkCreator extends NXLinkCreator implements InitializingBean {

	private static final String entry_definition = "NXtomo";

	private String control_data_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/current:SDS";

	private String data_data_target = "entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/data:SDS";
	private String data_rotation_angle_target = "entry1:NXentry/tomo_entry:NXsubentry/sample:NXsample/rotation_angle:SDS";

	private String instrument_detector_data_target = "entry1:NXentry/instrument:NXinstrument/pco1_hw_hdf:NXdetector/data:SDS";
	private String instrument_detector_image_key_target = "entry1:NXentry/instrument:NXinstrument/tomoScanDevice:NXpositioner/image_key:SDS";
	private Double instrument_detector_x_pixel_size = Double.NaN;
	private Double instrument_detector_y_pixel_size = Double.NaN;
	private String instrument_detector_pixel_size_units = "undefined";
	private Double instrument_detector_distance = Double.NaN;
	private String instrument_detector_distance_units = "undefined";
	private Double instrument_detector_x_rotation_axis_pixel_position = Double.NaN;
	private Double instrument_detector_y_rotation_axis_pixel_position = Double.NaN;

	private String instrument_source_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource";
	private String instrument_source_current_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/current:SDS";
	private String instrument_source_energy_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/energy:SDS";
	private String instrument_source_name_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/name:SDS";
	private String instrument_source_probe_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/probe:SDS";
	private String instrument_source_type_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/type:SDS";

	private String sample_rotation_angle_target = "entry1:NXentry/instrument:NXinstrument/tomoScanDevice:NXpositioner/ss1_rot:SDS";
	private String sample_x_translation_target = "entry1:NXentry/instrument:NXinstrument/sample_stage:NXpositioner/ss1_samplex:SDS";
	private String sample_y_translation_target = "entry1:NXentry/instrument:NXinstrument/sample_stage:NXpositioner/ss1_sampley:SDS";
	private String sample_z_translation_target = "entry1:NXentry/instrument:NXinstrument/sample_stage:NXpositioner/ss1_samplez:SDS";

	private String title_target = "entry1:NXentry/scan_identifier:SDS";

	public String getControl_data_target() {
		return control_data_target;
	}

	public void setControl_data_target(String control_data_target) {
		this.control_data_target = control_data_target;
	}

	public String getInstrument_detector_data_target() {
		return instrument_detector_data_target;
	}

	public void setInstrument_detector_data_target(String instrument_detector_data_target) {
		this.instrument_detector_data_target = instrument_detector_data_target;
		this.data_data_target = this.instrument_detector_data_target;
	}

	public String getInstrument_detector_image_key_target() {
		return instrument_detector_image_key_target;
	}

	public void setInstrument_detector_image_key_target(String instrument_detector_image_key_target) {
		this.instrument_detector_image_key_target = instrument_detector_image_key_target;
	}

	public String getInstrument_detector_pixel_size_units() {
		return instrument_detector_pixel_size_units;
	}

	public void setInstrument_detector_pixel_size_units(String instrument_detector_pixel_size_units) {
		this.instrument_detector_pixel_size_units = instrument_detector_pixel_size_units;
	}

	public String getSample_rotation_angle_target() {
		return sample_rotation_angle_target;
	}

	public void setSample_rotation_angle_target(String sample_rotation_angle_target) {
		this.sample_rotation_angle_target = sample_rotation_angle_target;
	}

	public String getInstrument_source_target() {
		return instrument_source_target;
	}

	public void setInstrument_source_target(String instrument_source_target) {
		this.instrument_source_target = instrument_source_target;

		this.instrument_source_current_target = this.instrument_source_target + "/current:SDS";
		this.instrument_source_energy_target = this.instrument_source_target + "/energy:SDS";
		this.instrument_source_name_target = this.instrument_source_target + "/name:SDS";
		this.instrument_source_probe_target = this.instrument_source_target + "/probe:SDS";
		this.instrument_source_type_target = this.instrument_source_target + "/type:SDS";
	}

	public String getSample_x_translation_target() {
		return sample_x_translation_target;
	}

	public void setSample_x_translation_target(String sample_x_translation_target) {
		this.sample_x_translation_target = sample_x_translation_target;
	}

	public String getSample_y_translation_target() {
		return sample_y_translation_target;
	}

	public void setSample_y_translation_target(String sample_y_translation_target) {
		this.sample_y_translation_target = sample_y_translation_target;
	}

	public String getSample_z_translation_target() {
		return sample_z_translation_target;
	}

	public void setSample_z_translation_target(String sample_z_translation_target) {
		this.sample_z_translation_target = sample_z_translation_target;
	}

	public String getTitle_target() {
		return title_target;
	}

	public void setTitle_target(String title_target) {
		this.title_target = title_target;
	}

	public Double getInstrument_detector_x_pixel_size() {
		return instrument_detector_x_pixel_size;
	}

	public void setInstrument_detector_x_pixel_size(Double instrument_detector_x_pixel_size) {
		this.instrument_detector_x_pixel_size = instrument_detector_x_pixel_size;
	}

	public Double getInstrument_detector_y_pixel_size() {
		return instrument_detector_y_pixel_size;
	}

	public void setInstrument_detector_y_pixel_size(Double instrument_detector_y_pixel_size) {
		this.instrument_detector_y_pixel_size = instrument_detector_y_pixel_size;
	}

	public Double getInstrument_detector_distance() {
		return instrument_detector_distance;
	}

	public void setInstrument_detector_distance(Double instrument_detector_distance) {
		this.instrument_detector_distance = instrument_detector_distance;
	}

	public String getInstrument_detector_distance_units() {
		return instrument_detector_distance_units;
	}

	public void setInstrument_detector_distance_units(String instrument_detector_distance_units) {
		this.instrument_detector_distance_units = instrument_detector_distance_units;
	}

	public Double getInstrument_detector_x_rotation_axis_pixel_position() {
		return instrument_detector_x_rotation_axis_pixel_position;
	}

	public void setInstrument_detector_x_rotation_axis_pixel_position(
			Double instrument_detector_x_rotation_axis_pixel_position) {
		this.instrument_detector_x_rotation_axis_pixel_position = instrument_detector_x_rotation_axis_pixel_position;
	}

	public Double getInstrument_detector_y_rotation_axis_pixel_position() {
		return instrument_detector_y_rotation_axis_pixel_position;
	}

	public void setInstrument_detector_y_rotation_axis_pixel_position(
			Double instrument_detector_y_rotation_axis_pixel_position) {
		this.instrument_detector_y_rotation_axis_pixel_position = instrument_detector_y_rotation_axis_pixel_position;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// control/data
		if (this.getControl_data_target() == null)
			throw new IllegalStateException("control_data_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/control:NXMonitor/data", getControl_data_target());

		// instrument/detector/data
		if (this.getInstrument_detector_data_target() == null)
			throw new IllegalStateException("instrument_detector_data_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/data", getInstrument_detector_data_target());

		// instrument/detector/image_key
		if (this.getInstrument_detector_image_key_target() == null)
			throw new IllegalStateException("instrument_detector_image_key_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/image_key", getInstrument_detector_image_key_target());

		// instrument/source
		if (this.getInstrument_source_target() == null)
			throw new IllegalStateException("instrument_source_target is not set");
		//addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/source", getInstrument_source_target());
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/source:NXsource/current", this.instrument_source_current_target);
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/source:NXsource/energy", this.instrument_source_energy_target);
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/source:NXsource/name", this.instrument_source_name_target);
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/source:NXsource/probe", this.instrument_source_probe_target);
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/source:NXsource/type", this.instrument_source_type_target);

		// sample/rotation_angle
		if (this.getSample_rotation_angle_target() == null)
			throw new IllegalStateException("sample_rotation_angle_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/sample:NXsample/rotation_angle", getSample_rotation_angle_target());

		// sample/x_translation
		if (this.getSample_x_translation_target() == null)
			throw new IllegalStateException("sample_x_translation_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/sample:NXsample/x_translation", getSample_x_translation_target());

		// sample/y_translation
		if (this.getSample_y_translation_target() == null)
			throw new IllegalStateException("sample_y_translation_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/sample:NXsample/y_translation", getSample_y_translation_target());

		// sample/z_translation
		if (this.getSample_z_translation_target() == null)
			throw new IllegalStateException("sample_z_translation_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/sample:NXsample/z_translation", getSample_z_translation_target());

		// title
		if (this.getTitle_target() == null)
			throw new IllegalStateException("title_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/title", getTitle_target());


		// data/data
		if (this.data_data_target == null)
			throw new IllegalStateException("data_data_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/data:NXdata/data", this.data_data_target);

		// data/rotation_angle
		if (this.data_rotation_angle_target == null)
			throw new IllegalStateException("data_rotation_angle_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/data:NXdata/rotation_angle", this.data_rotation_angle_target);
	}

	public void writeStringData(String filename, String dataName, String dataValue) throws Exception {
		NexusFile file = NexusFileHDF5.openNexusFile(filename);
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), "entry1", NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "tomo_entry", "NXsubentry");
//		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);
		NexusUtils.writeString(file, group, dataName, dataValue);
		file.close();
	}

	public void writeDoubleData(String filename, String dataName, double dataValue, String dataUnits) throws Exception {
		NexusFile file = NexusFileHDF5.openNexusFile(filename);
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), "entry1", NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "tomo_entry", "NXsubentry");
//		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);
		NexusUtils.writeDouble(file, group, dataName, dataValue, dataUnits);
		file.close();
	}

	@Override
	public void makelinks(String filename) throws Exception {

		super.makelinks(filename);

		// workaround: make non-link(s) as well
		writeDoubleData(filename, "instrument/detector/x_pixel_size", this.instrument_detector_x_pixel_size, this.instrument_detector_pixel_size_units);
		writeDoubleData(filename, "instrument/detector/y_pixel_size", this.instrument_detector_y_pixel_size, this.instrument_detector_pixel_size_units);
		writeDoubleData(filename, "instrument/detector/distance", this.instrument_detector_distance, this.instrument_detector_distance_units);
		writeDoubleData(filename, "instrument/detector/x_rotation_axis_pixel_position", this.instrument_detector_x_rotation_axis_pixel_position, "pixel");
		writeDoubleData(filename, "instrument/detector/y_rotation_axis_pixel_position", this.instrument_detector_y_rotation_axis_pixel_position, "pixel");

		writeStringData(filename, "definition", NXTomoEntryLinkCreator.entry_definition);
	}
}
