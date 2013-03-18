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

import org.nexusformat.NexusFile;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 */
public class NXTomoEntryLinkCreator extends NXLinkCreator implements InitializingBean {
	
	private String control_data_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/current:NXdata";
	
	private String data_data_target = "entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/data:SDS";
	private String data_rotation_angle_target = "entry1:NXentry/tomo_entry:NXsubentry/sample:NXsample/rotation_angle:NXdata";
	
	private String instrument_detector_data_target = "entry1:NXentry/instrument:NXinstrument/pco1_hw_hdf:NXdetector/data:SDS";
	private String instrument_detector_distance_target = "entry1:NXentry/scan_identifier:NXdata";
	private String instrument_detector_image_key_target = "entry1:NXentry/instrument:NXinstrument/tomoScanDevice:NXpositioner/image_key:NXdata";
	private String instrument_detector_x_pixel_size_target = "entry1:NXentry/scan_identifier:NXdata";
	private String instrument_detector_y_pixel_size_target = "entry1:NXentry/scan_identifier:NXdata";
	
	private String instrument_source_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource";
	private String instrument_source_current_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/current:NXdata";
	private String instrument_source_energy_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/energy:NXdata";
	private String instrument_source_name_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/name:NXdata";
	private String instrument_source_probe_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/probe:NXdata";
	private String instrument_source_type_target = "entry1:NXentry/instrument:NXinstrument/source:NXsource/type:NXdata";
	
	private String sample_rotation_angle_target = "entry1:NXentry/instrument:NXinstrument/tomoScanDevice:NXpositioner/ss1_rot:NXdata";
	private String sample_x_translation_target = "entry1:NXentry/instrument:NXinstrument/sample_stage:NXpositioner/ss1_samplex:NXdata";
	private String sample_y_translation_target = "entry1:NXentry/instrument:NXinstrument/sample_stage:NXpositioner/ss1_sampley:NXdata";
	private String sample_z_translation_target = "entry1:NXentry/instrument:NXinstrument/sample_stage:NXpositioner/ss1_samplez:NXdata";
	
	private String title_target = "entry1:NXentry/scan_identifier:NXdata";
	
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

	public String getInstrument_detector_distance_target() {
		return instrument_detector_distance_target;
	}

	public void setInstrument_detector_distance_target(String instrument_detector_distance_target) {
		this.instrument_detector_distance_target = instrument_detector_distance_target;
	}

	public String getInstrument_detector_image_key_target() {
		return instrument_detector_image_key_target;
	}

	public void setInstrument_detector_image_key_target(String instrument_detector_image_key_target) {
		this.instrument_detector_image_key_target = instrument_detector_image_key_target;
	}

	public String getInstrument_detector_x_pixel_size_target() {
		return instrument_detector_x_pixel_size_target;
	}

	public void setInstrument_detector_x_pixel_size_target(String instrument_detector_x_pixel_size_target) {
		this.instrument_detector_x_pixel_size_target = instrument_detector_x_pixel_size_target;
	}

	public String getInstrument_detector_y_pixel_size_target() {
		return instrument_detector_y_pixel_size_target;
	}

	public void setInstrument_detector_y_pixel_size_target(String instrument_detector_y_pixel_size_target) {
		this.instrument_detector_y_pixel_size_target = instrument_detector_y_pixel_size_target;
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
		
		this.instrument_source_current_target = this.instrument_source_target + "/current:NXdata";
		this.instrument_source_energy_target = this.instrument_source_target + "/energy:NXdata";
		this.instrument_source_name_target = this.instrument_source_target + "/name:NXdata";
		this.instrument_source_probe_target = this.instrument_source_target + "/probe:NXdata";
		this.instrument_source_type_target = this.instrument_source_target + "/type:NXdata";
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
				
		// instrument/detector/distance
		if (this.getInstrument_detector_distance_target() == null)
			throw new IllegalStateException("instrument_detector_distance_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/distance", getInstrument_detector_distance_target());
		
		// instrument/detector/image_key
		if (this.getInstrument_detector_image_key_target() == null)
			throw new IllegalStateException("instrument_detector_image_key_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/image_key", getInstrument_detector_image_key_target());
		
		// instrument/detector/x_pixel_size
		if (this.getInstrument_detector_x_pixel_size_target() == null)
			throw new IllegalStateException("instrument_detector_x_pixel_size_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/x_pixel_size", getInstrument_detector_x_pixel_size_target());
		
		// instrument/detector/y_pixel_size
		if (this.getInstrument_detector_x_pixel_size_target() == null)
			throw new IllegalStateException("instrument_detector_y_pixel_size_target is not set");
		addLink("/entry1:NXentry/tomo_entry:NXsubentry/instrument:NXinstrument/detector:NXdetector/y_pixel_size", getInstrument_detector_y_pixel_size_target());
		
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
	
	public void writeStringData(String filename, String dataName, String dataValue ) throws Exception {
		NexusFile file = new NexusFile(filename,NexusFile.NXACC_RDWR);
		try {
			// navigate to desired place
			file.opengroup("entry1", "NXentry");
			file.opengroup("tomo_entry", "NXsubentry");
			
			int[] arr = { dataValue.length() };
			file.makedata(dataName, NexusFile.NX_CHAR, 1, arr);
			file.opendata(dataName);
			file.putdata(dataValue.getBytes());
			file.closedata();
			
			// navigate back from desired place
			file.closegroup();
			file.closegroup();
		} finally {
			file.flush();
			try {
				file.finalize();
			} catch (Throwable e) {
				throw new Exception("Error finalising " + filename,e);
			} finally {
				file.close();
				
			}
		}
	}
	
	@Override
	public void makelinks(String filename) throws Exception {
		
		super.makelinks(filename);
		
		// workaround: make non-link(s) as well
		writeStringData(filename, "definition", "NXtomo");	
	}
}
