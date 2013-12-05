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

package uk.ac.gda.exafs.ui.detector.vortex;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.exafs.ui.ExafsBeanFileSelectionEditor;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class VortexParametersEditor extends ExafsBeanFileSelectionEditor {

	@Override
	public Class<?> getBeanClass() {
		return VortexParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return VortexParameters.mappingURL;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new VortexParametersUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return VortexParameters.schemaURL;
	}
	
	@Override
	public List<String> getPrivateXMLFields() {
		return Arrays.asList(new String[]{"data"});
	}
	
}