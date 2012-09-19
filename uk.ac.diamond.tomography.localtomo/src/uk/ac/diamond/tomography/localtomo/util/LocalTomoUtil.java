/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.localtomo.util;

import gda.configuration.properties.LocalProperties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import uk.ac.diamond.tomography.localtomo.DocumentRoot;
import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.LocalTomoType;

public class LocalTomoUtil {
	private static ResourceSet resourceSet;
	private static final String DIAMOND_TOMO_RECON_LOCALPARAM = "diamond.tomo.recon.localparam";

	public static LocalTomoType getLocalTomoObject() {
		String localTomoParamsFileLocation = LocalProperties.get(DIAMOND_TOMO_RECON_LOCALPARAM);
		//To cope with DAWN by passing a localproperty
		if (localTomoParamsFileLocation == null) {
			localTomoParamsFileLocation = System.getProperty(DIAMOND_TOMO_RECON_LOCALPARAM);
		}

		if (localTomoParamsFileLocation != null) {
			final URI tomoConfigUri = URI.createFileURI(localTomoParamsFileLocation);
			Resource res = getResourceSet().getResource(tomoConfigUri, true);
			EObject eObject = res.getContents().get(0);
			if (eObject instanceof DocumentRoot) {
				DocumentRoot dr = (DocumentRoot) eObject;
				return dr.getLocalTomo();
			}
		}
		return null;
	}

	/**
	 * @return {@link ResourceSet}
	 */
	private static ResourceSet getResourceSet() {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			// To initialize the resourceset resource factory registry with the excalibur config package
			EPackage.Registry.INSTANCE.put(LocalTomoPackage.eNS_URI, LocalTomoPackage.eINSTANCE);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
					.put("xml", new LocalTomoResourceFactoryImpl());
		}
		return resourceSet;
	}

	public static String getLocalTomoUtilFileLocation() {
		return LocalProperties.get(DIAMOND_TOMO_RECON_LOCALPARAM);
	}
}
