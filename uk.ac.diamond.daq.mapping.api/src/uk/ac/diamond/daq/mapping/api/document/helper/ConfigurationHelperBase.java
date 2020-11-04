/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.helper;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;

/**
 * Collection of methods to update a {@link ScanningConfiguration} instance. Constructor and methods are protected
 * because this class is not supposed to be used alone.
 *
 * @author Maurizio Nagni
 */
public class ConfigurationHelperBase {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationHelperBase.class);

	/**
	 * The scanning acquisition acquisition configuration data
	 */
	private final Supplier<? extends AcquisitionConfigurationBase<?>> scanningConfigurationSupplier;

	protected ConfigurationHelperBase(Supplier<? extends AcquisitionConfigurationBase<?>> scanningConfigurationSupplier) {
		this.scanningConfigurationSupplier = scanningConfigurationSupplier;
	}

	protected AcquisitionConfigurationBase<?> getScanningParameters() {
		return scanningConfigurationSupplier.get();
	}

	protected static Logger getLogger() {
		return logger;
	}
}
