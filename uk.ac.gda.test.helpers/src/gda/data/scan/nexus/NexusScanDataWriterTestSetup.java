/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.data.scan.nexus;

import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;

import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.scanning.api.device.IScannableDeviceService;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import uk.ac.diamond.daq.scanning.FilePathService;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class NexusScanDataWriterTestSetup {

	private static ServiceHolder gdaDataServiceHolder;
	private static org.eclipse.dawnsci.nexus.scan.ServiceHolder oednsServiceHolder;
	private static org.eclipse.dawnsci.nexus.ServiceHolder oednServiceHolder;

	private NexusScanDataWriterTestSetup() {
		// private constructor to prevent instantiation
	}

	public static void setUp() {
		// note: if TestHelpers.setUpTest is called, this property will be reset to NexusDataWriter, so you'll need to set it again
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, NexusScanDataWriter.class.getSimpleName());

		final NexusDeviceService nexusDeviceService = new NexusDeviceService();

		gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		gdaDataServiceHolder.setNexusDeviceService(nexusDeviceService);
		gdaDataServiceHolder.setFilePathService(new FilePathService());

		oednsServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		oednsServiceHolder.setNexusDeviceService(nexusDeviceService);
		oednsServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
		oednsServiceHolder.setTemplateService(new NexusTemplateServiceImpl());

		oednServiceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		oednServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		oednServiceHolder.setNexusDeviceAdapterFactory(new GDANexusDeviceAdapterFactory());

		ServiceProvider.setService(IScannableDeviceService.class, new ScannableDeviceConnectorService());
	}

	public static void tearDown() {
		LocalProperties.clearProperty(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);

		gdaDataServiceHolder.setNexusScanFileService(null);
		gdaDataServiceHolder.setNexusDeviceService(null);

		oednsServiceHolder.setNexusDeviceService(null);
		oednsServiceHolder.setNexusBuilderFactory(null);
		oednsServiceHolder.setTemplateService(null);

		oednServiceHolder.setNexusFileFactory(null);
		oednServiceHolder.setNexusDeviceAdapterFactory(null);

		ServiceProvider.reset();
	}

}
