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

package uk.ac.gda.server.ncd.samplerack;

import static gda.jython.InterfaceProvider.getTerminalPrinter;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.motor.EpicsMotor;
import gda.device.scannable.ScannableMotor;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public abstract class SampleRackBase implements Configurable, SampleRack {
	private static final String SAMPLE_BACKGROUND = "sample_background";
	private static final String POITIONER_DEVICE_MOVE_WAITING_INTERRUPTED = "Poitioner Device move waiting interrupted ";
	private static final String SAMPLE_RACK_SCAN_ABORTED = " Sample Rack scan aborted. ";
	private static final Logger logger = LoggerFactory.getLogger(SampleRackBase.class);

	private String name;
	private String description;
	private UUID id = UUID.randomUUID();
	protected transient RackScanRunner runner;
	protected transient ITerminalPrinter terminalPrinter;
	protected transient RackConfiguration rackConfiguration;
	/** Other scannables and their positions that require moving to a known state prior to collection */
	private transient Map<Scannable, Object> environment = new HashMap<>();

	@Override
	public void runSamples(SampleConfiguration config) throws DeviceException {
		var background = config.getBackground();
		Metadata meta = GDAMetadataProvider.getInstance();
		var previousBg = meta.getMetadataValue(SAMPLE_BACKGROUND);
		meta.setMetadataValue(SAMPLE_BACKGROUND, background);
		var samples = config.getActiveSamples();
		setupEnvironment();
		terminalPrinter.print("Running " + samples.length + " samples");
		logger.info("Running {} samples", samples.length);
		for (var sample: stream(samples).sorted((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.getCell(), s2.getCell())).collect(toList())) {
			terminalPrinter.print("Running scan for sample " + sample.getName());
			try {
				scanSample(sample);
			} catch (SampleRackException se) {
				logger.debug("SampleConfiguration is {}", config.toString());
				logger.error(sample.getName() + SAMPLE_RACK_SCAN_ABORTED, se);
				getTerminalPrinter().print(" ");
				getTerminalPrinter().print(sample.getName() + SAMPLE_RACK_SCAN_ABORTED);
				getTerminalPrinter().print("");
				break;
			}
		}
		meta.setMetadataValue(SAMPLE_BACKGROUND, previousBg);
	}

	/** Set up environment, set metadata etc */
	private void setupEnvironment() throws DeviceException {
		for (var entry: environment.entrySet()) {
			entry.getKey().moveTo(entry.getValue());
		}
	}

	protected abstract void scanSample(Sample sample) throws DeviceException, SampleRackException;

	protected void retryingMove(Sample sample, Scannable motor, double position, int maxAttempts) throws SampleRackException, MotorException {

		SampleRackException lastException = null;
		for (int count = 0; count < maxAttempts; count++) {
			try {
				lastException=null;
				motor.moveTo(position);
				break;
			}
			catch (DeviceException e) {
				getTerminalPrinter().print(" ");
				getTerminalPrinter().print("Poitioner device move exception for the "+ timeFrequency(count) +" time for : " + sample.getName());
				logger.error("Poitioner device move exception for Sample {}, Position {} ", sample, motor, e);
				lastException = new SampleRackException("Poitioner device move exception for Sample " +sample+ "Position "+ motor, e);

				try {
					TimeUnit.SECONDS.sleep(3);
					if (motor instanceof ScannableMotor scnMotor && scnMotor.getMotor() instanceof EpicsMotor epicsMotor) {
						epicsMotor.forceCallback();
						logger.debug("Reset the motor position {}.", epicsMotor.getName());
						getTerminalPrinter().print(" ");
						getTerminalPrinter().print("Reset the motor position "+ epicsMotor.getName());
					}
				} catch (InterruptedException ie) {
					getTerminalPrinter().print(" ");
					getTerminalPrinter().print(POITIONER_DEVICE_MOVE_WAITING_INTERRUPTED + sample.getName());
					logger.error(POITIONER_DEVICE_MOVE_WAITING_INTERRUPTED, ie);
					Thread.currentThread().interrupt();
				}
			}
		}
		if (lastException != null) {
			getTerminalPrinter().print(" ");
			getTerminalPrinter().print("Poitioner device move exception occured 3 times. Sample Rack scan aborted.");
			getTerminalPrinter().print(" ");
			throw lastException;
		}
	}

	private String timeFrequency(int count) {

		if (count==0) {
			return "first";
		} else if (count==1) {
			return "second";
		} else {
			return "third";
		}
	}

	@Override
	public void configureRack(RackConfigurationInput rackConfigInput) {
		this.rackConfiguration = rackConfigInput.intoRackConfiguration();
	}

	@Override
	public RackConfigurationInput getRackConfigurationInput() {
		return rackConfiguration.intoRackConfigurationInput();
	}

	@Override
	public String toString() {
		return name + " (" + description + ")";
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public UUID getID() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setRunner(RackScanRunner runner) {
		this.runner = runner;
	}

	public RackScanRunner getRunner() {
		return runner;
	}

	@Override
	public void configure() throws FactoryException {
		terminalPrinter = InterfaceProvider.getTerminalPrinter();
	}

	@Override
	public boolean isConfigured() {
		return terminalPrinter != null;
	}

	@Override
	public void reconfigure() throws FactoryException {
		configure();
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	public Map<Scannable, Object> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<Scannable, Object> environment) {
		this.environment = environment;
	}

	public RackConfiguration getRackConfiguration() {
		return rackConfiguration;
	}

	public void setRackConfiguration(RackConfiguration rackConfiguration) {
		this.rackConfiguration = rackConfiguration;
	}
}
