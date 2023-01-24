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

import uk.ac.gda.beans.vortex.Xspress3Parameters;

/**
 * Configures the parameters of an Xspress3 detector
 */
public final class Xspress3ParametersEditor extends FluorescenceDetectorParametersEditor {

	public static final String ID = "uk.ac.gda.exafs.ui.Xspress3ParametersEditor";

	@Override
	public Class<?> getBeanClass() {
		return Xspress3Parameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return Xspress3Parameters.mappingURL;
	}

	@Override
	public URL getSchemaUrl() {
		return Xspress3Parameters.schemaURL;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Xspress3";
	}
}
