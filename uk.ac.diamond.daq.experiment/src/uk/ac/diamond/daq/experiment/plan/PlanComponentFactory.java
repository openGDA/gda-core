/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import gda.device.Scannable;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

/**
 * Holds common functionality for the description of general plan components.
 * @param <T> The plan component built by {@link #build(IPlanRegistrar)}
 */
public abstract class PlanComponentFactory<T> {

	private final String name;

	private SignalSource source;
	private ISampleEnvironmentVariable sev;

	PlanComponentFactory(String name) {
		this.name = name;
	}

	/**
	 * Called internally to build the component
	 * once it has been fully described.
	 */
	abstract T build(IPlanRegistrar registrar);

	String getName() {
		return name;
	}

	SignalSource getSource() {
		return source;
	}

	ISampleEnvironmentVariable getSampleEnvironmentVariable() {
		return sev;
	}

	void setScannableSev(Scannable scannable) {
		sev = new SampleEnvironmentVariable(scannable);
		source = SignalSource.POSITION;
	}

	void setCustomSev(DoubleSupplier signalSource) {
		sev = new SampleEnvironmentVariable(signalSource);
		source = SignalSource.POSITION;
	}

	void setCustomSev(DoubleSupplier signalSource, String name) {
		sev = new SampleEnvironmentVariable(new ExternalSourceWrapper(signalSource, name));
		source = SignalSource.POSITION;
	}

	void setTimerSev() {
		sev = PlanFactory.getSystemTimer();
		source = SignalSource.TIME;
	}

}
