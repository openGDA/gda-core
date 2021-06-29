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

package uk.ac.gda.server.ncd.subdetector;

import static gda.configuration.properties.LocalProperties.GDA_BEAMLINE_NAME;
import static uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem.OTHER_DETECTOR;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import uk.ac.gda.devices.pressurecell.PressureCell;

public class NcdPressureCell extends NcdSubDetector {
	private static final Logger logger = LoggerFactory.getLogger(NcdPressureCell.class);
	private static final String BEAMLINE = LocalProperties.get(GDA_BEAMLINE_NAME);
	private PressureCell cell;

	private double jumpFromPressure = 1;
	private double jumpToPressure = 1;
	private String innerPath = "#entry/instrument/detector/data";
	private int samplesBefore;
	private int samplesAfter;

	public void setJumpPressures(double from, double to) {
		jumpFromPressure = from;
		jumpToPressure = to;
	}

	@Override
	public void atScanStart(ScanInformation info) throws DeviceException {
		cell.setJumpPressures(jumpFromPressure, jumpToPressure);
		cell.armJumpValve();
		setFilePath(info);
		cell.setTriggers(samplesBefore, samplesAfter);
		cell.setAcquire(true);
	}

	private void setFilePath(ScanInformation info) throws DeviceException {
		var directory = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		var filename = String.format("%s-%d-%s", BEAMLINE, info.getScanNumber(), getName());
		cell.setFilePath(directory, filename);
	}

	@Override
	public void configure() throws FactoryException {

	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public void reconfigure() throws FactoryException {
	}

	@Override
	public void clear() throws DeviceException {
	}

	@Override
	public void start() throws DeviceException {
	}

	@Override
	public void stop() throws DeviceException {
		cell.stop();
	}

	@Override
	public String getDetectorType() {
		return OTHER_DETECTOR;
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		var filename = cell.getLastFileName();
		if (filename == null || filename.isBlank()) {
			throw new DeviceException(getName() + " - No filename available at end of scan");
		} else if (Files.notExists(Paths.get(filename))) {
			logger.warn("{} - File '{}' does not exist at the end of scan", getName(), filename);
		}
		dataTree.addExternalFileLink(getName(), getName(), "nxfile://" + filename + innerPath, frames);
	}

	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		return null;
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return null;
	}

	public PressureCell getCell() {
		return cell;
	}

	public void setCell(PressureCell cell) {
		this.cell = cell;
	}

	public int getSamplesAfter() {
		return samplesAfter;
	}

	public void setSamplesAfter(int samplesAfter) {
		this.samplesAfter = samplesAfter;
	}

	public int getSamplesBefore() {
		return samplesBefore;
	}

	public void setSamplesBefore(int samplesBefore) {
		this.samplesBefore = samplesBefore;
	}

	public void setJumpFromPressure(double jumpFromPressure) {
		this.jumpFromPressure = jumpFromPressure;
	}

	public double getJumpFromPressure() {
		return jumpFromPressure;
	}

	public void setJumpToPressure(double jumpToPressure) {
		this.jumpToPressure = jumpToPressure;
	}

	public double getJumpToPressure() {
		return jumpToPressure;
	}

	public String getInnerPath() {
		return innerPath;
	}

	public void setInnerPath(String innerPath) {
		this.innerPath = innerPath;
	}
}
