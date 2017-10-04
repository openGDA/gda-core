/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.editors.detectors;

import java.net.URL;

import uk.ac.gda.beans.vortex.VortexParameters;

/**
 * Configures the parameters of an Vortex detector
 */
public final class VortexParametersEditor extends FluorescenceDetectorParametersEditor {

	public static final String ID = "uk.ac.gda.exafs.ui.VortexParametersEditor";

	@Override
	public Class<?> getBeanClass() {
		return VortexParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return VortexParameters.mappingURL;
	}

	@Override
	public URL getSchemaUrl() {
		return VortexParameters.schemaURL;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Vortex";
	}
}
