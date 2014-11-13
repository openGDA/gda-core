/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;

public class MicroFocusMappableDataProviderFactory {

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusMappableDataProviderFactory.class);

	public static MicroFocusMappableDataProvider getInstance(String fileName) throws Exception {

		Object bean = BeansFactory.getBean(new File(fileName));
		return getInstance(bean);

	}

	public static MicroFocusMappableDataProvider getInstance(Object bean) {

		if (bean instanceof XspressParameters) {
			logger.info("xspress bean ");
			return new XspressMFMappableDataProvider();
		} else if (bean instanceof Xspress3Parameters) {
			logger.info("xspress3 bean ");
			return new Xspress3MFMappableDataProvider();
		} else if (bean instanceof VortexParameters) {
			logger.info("vortex bean ");
			return new VortexMFMappableDataProvider();
		} else if (bean instanceof DetectorParameters) {
			return new ScalerMFMappableDataProvider();
		}

		throw new IllegalArgumentException("Data provider matching " + bean.getClass() + " could not be found!");
	}

}
