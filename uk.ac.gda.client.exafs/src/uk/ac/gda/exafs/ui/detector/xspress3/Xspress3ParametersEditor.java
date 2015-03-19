/* Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress3;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanMultiPageEditor;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

@Deprecated
public class Xspress3ParametersEditor extends ExperimentBeanMultiPageEditor {
	
	@Override
	public Class<?> getBeanClass() {
		return Xspress3Parameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return Xspress3Parameters.mappingURL;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(String path,
			                                           Object editingBean) {
		return new Xspress3ParametersUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return Xspress3Parameters.schemaURL;
	}
	
	@Override
	public List<String> getPrivateXMLFields() {
		return Arrays.asList(new String[]{"data"});
	}

}
