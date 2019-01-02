/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.server.ncd.timing.data.SimpleTimerConfiguration;

@ServiceInterface(TimerController.class)
public class DefaultTimerController implements TimerController {
	private static final Logger log = LoggerFactory.getLogger(DefaultTimerController.class);

	private ObservableComponent observableComponent = new ObservableComponent();

	private Gson gson = new Gson ();

	private String name;
	private SimpleTimerConfiguration lastUsedConfiguration;
	private HardwareTimer hardwareTimer;

	public DefaultTimerController(HardwareTimer hardwareTimer, double exposure, int numberOfFrames, boolean delay, double delayTime) {
		this.hardwareTimer = hardwareTimer;

		lastUsedConfiguration = new SimpleTimerConfiguration();
		lastUsedConfiguration.setExposure (exposure);
		lastUsedConfiguration.setNumberOfFrames (numberOfFrames);
		lastUsedConfiguration.setDelay (delay);
		lastUsedConfiguration.setDelayTime (delayTime);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SimpleTimerConfiguration getLastUsedConfiguration() {
		return lastUsedConfiguration;
	}

	@Override
	public boolean configureTimer(SimpleTimerConfiguration simpleTimerConfiguration) throws HardwareTimerException {
		this.lastUsedConfiguration = simpleTimerConfiguration;

		try {
			hardwareTimer.configureTimer(
					simpleTimerConfiguration.getNumberOfFrames(),
					simpleTimerConfiguration.getExposure(),
					simpleTimerConfiguration.isDelay() ? simpleTimerConfiguration.getDelayTime() : hardwareTimer.getMinimumDelay());
			observableComponent.notifyIObservers(this, simpleTimerConfiguration);
		} catch (DeviceException e) {
			log.error("Unable to configure timer", e);
			return false;
		}
		return true;
	}

	@Override
	public SimpleTimerConfiguration loadTimer(File configurationFile) throws IOException {
		try (Reader reader = new FileReader(configurationFile)) {
			return gson.fromJson(reader, SimpleTimerConfiguration.class);
		} catch (IOException e) {
			throw new IOException ("Unable to load file " + configurationFile.getName() + ", Reason: " + e.getMessage());
		}
	}

	@Override
	public void saveTimer(File configurationFile, SimpleTimerConfiguration simpleTimerConfiguration) throws IOException {
		try (Writer writer = new FileWriter(configurationFile)) {
			Gson gson = new Gson ();
			gson.toJson(simpleTimerConfiguration, writer);
		} catch (IOException e) {
			throw new IOException ("Unable to save file " + configurationFile.getName() + ", Reason: " + e.getMessage());
		}
	}

	@Override
	public String getConfigurationFileExtension() {
		return hardwareTimer.getConfigurationFileExtension();
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}
