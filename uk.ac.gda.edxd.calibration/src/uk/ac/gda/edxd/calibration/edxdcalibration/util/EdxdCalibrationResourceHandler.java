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

package uk.ac.gda.edxd.calibration.edxdcalibration.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationFactory;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage;

public class EdxdCalibrationResourceHandler implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(EdxdCalibrationResourceHandler.class);

	private String fileLocation;

	private EditingDomain editingDomain;

	/**
	 * @return {@link ResourceSet}
	 */
	protected ResourceSet getResourceSet() {
		return getEditingDomain().getResourceSet();
	}

	public EditingDomain getEditingDomain() {
		if (editingDomain == null) {
			ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
					ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

			BasicCommandStack commandStack = new BasicCommandStack();
			editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack,
					new HashMap<Resource, Boolean>());

			ResourceSet resourceSet = editingDomain.getResourceSet();

			resourceSet.getPackageRegistry().put(EdxdcalibrationPackage.eINSTANCE.getNsPrefix(),
					EdxdcalibrationPackage.eINSTANCE);
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("ec", new EdxdcalibrationResourceFactoryImpl());
		}
		return editingDomain;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public Resource getResource(boolean shouldCreate) {
		final URI calibFileUri = URI.createFileURI(fileLocation);
		Resource res = null;

		boolean fileExists = new File(fileLocation).exists();
		if (!fileExists && !shouldCreate) {
			return null;
		}

		if (!fileExists) {
			final Resource resource = getResourceSet().createResource(calibFileUri);
			DocumentRoot documentRoot = EdxdcalibrationFactory.eINSTANCE.createDocumentRoot();
			EdxdCalibration calibration = EdxdcalibrationFactory.eINSTANCE.createEdxdCalibration();
			documentRoot.getEdxdCalibration().add(calibration);
			resource.getContents().add(documentRoot);
			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(XMLResource.OPTION_ENCODING, "UTF-8");
			try {
				resource.save(options);
			} catch (IOException e) {
				logger.error("Exception saving the configuration model", e);
			}
			res = resource;
		} else {
			res = getResourceSet().getResource(calibFileUri, true);
		}
		return res;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (fileLocation == null) {
			throw new IllegalStateException("fileLocation should be provided");
		}
	}

}
