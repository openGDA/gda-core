/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import gda.commandqueue.Processor;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.Scannable;
import gda.jython.scriptcontroller.logging.LoggingScriptController;

import java.util.ArrayList;

public abstract class SpectroscopyScanFactory {

	protected BeamlinePreparer beamlinePreparer;
	protected DetectorPreparer detectorPreparer;
	protected OutputPreparer outputPreparer;
	protected SampleEnvironmentPreparer samplePreparer;
	protected Processor commandQueueProcessor;
	protected LoggingScriptController XASLoggingScriptController;
	protected AsciiDataWriterConfiguration datawriterconfig;
	protected Scannable energyScannable;
	protected NXMetaDataProvider metashop;
	protected ArrayList<AsciiMetadataConfig> original_header;
	protected boolean includeSampleNameInNexusName;

	protected void checkSharedObjectsNonNull() {

		checkDefined(beamlinePreparer, "beamlinePreparer");
		checkDefined(samplePreparer, "samplePreparer");
		checkDefined(outputPreparer, "outputPreparer");
		checkDefined(commandQueueProcessor, "commandQueueProcessor");
		checkDefined(XASLoggingScriptController, "XASLoggingScriptController");
		checkDefined(datawriterconfig, "datawriterconfig");
		checkDefined(original_header, "original_header");
		checkDefined(metashop, "metashop");
		checkDefined(includeSampleNameInNexusName, "includeSampleNameInNexusName");
	}

	protected void checkDefined(Object objectToTest, String objectname) {
		if (objectToTest == null) {
			throw new IllegalArgumentException(objectname + " is not defined!");
		}
	}

	public BeamlinePreparer getBeamlinePreparer() {
		return beamlinePreparer;
	}

	public void setBeamlinePreparer(BeamlinePreparer beamlinePreparer) {
		this.beamlinePreparer = beamlinePreparer;
	}

	public DetectorPreparer getDetectorPreparer() {
		return detectorPreparer;
	}

	public void setDetectorPreparer(DetectorPreparer detectorPreparer) {
		this.detectorPreparer = detectorPreparer;
	}

	public OutputPreparer getOutputPreparer() {
		return outputPreparer;
	}

	public void setOutputPreparer(OutputPreparer outputPreparer) {
		this.outputPreparer = outputPreparer;
	}

	public SampleEnvironmentPreparer getSamplePreparer() {
		return samplePreparer;
	}

	public void setSamplePreparer(SampleEnvironmentPreparer samplePreparer) {
		this.samplePreparer = samplePreparer;
	}

	public Processor getCommandQueueProcessor() {
		return commandQueueProcessor;
	}

	public void setCommandQueueProcessor(Processor commandQueueProcessor) {
		this.commandQueueProcessor = commandQueueProcessor;
	}

	public LoggingScriptController getXASLoggingScriptController() {
		return XASLoggingScriptController;
	}

	public void setXASLoggingScriptController(LoggingScriptController xASLoggingScriptController) {
		XASLoggingScriptController = xASLoggingScriptController;
	}

	public AsciiDataWriterConfiguration getDatawriterconfig() {
		return datawriterconfig;
	}

	public void setDatawriterconfig(AsciiDataWriterConfiguration datawriterconfig) {
		this.datawriterconfig = datawriterconfig;
	}

	public Scannable getEnergyScannable() {
		return energyScannable;
	}

	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}

	public NXMetaDataProvider getMetashop() {
		return metashop;
	}

	public void setMetashop(NXMetaDataProvider metashop) {
		this.metashop = metashop;
	}

	public ArrayList<AsciiMetadataConfig> getOriginal_header() {
		return original_header;
	}

	public void setOriginal_header(ArrayList<AsciiMetadataConfig> original_header) {
		this.original_header = original_header;
	}

	public boolean isIncludeSampleNameInNexusName() {
		return includeSampleNameInNexusName;
	}

	public void setIncludeSampleNameInNexusName(boolean includeSampleNameInNexusName) {
		this.includeSampleNameInNexusName = includeSampleNameInNexusName;
	}
}
