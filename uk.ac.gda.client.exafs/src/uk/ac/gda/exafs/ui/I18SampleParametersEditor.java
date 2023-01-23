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

package uk.ac.gda.exafs.ui;

import java.net.URL;

import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanMultiPageEditor;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.xml.XMLBeanEditor;

public final class I18SampleParametersEditor extends ExperimentBeanMultiPageEditor {

	@Override
	public Class<?> getBeanClass() {
		return I18SampleParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return I18SampleParameters.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new I18SampleParametersUIEditor(path, getMappingUrl(), this, (I18SampleParameters) editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return I18SampleParameters.schemaURL; // Please make sure this field is present and the schema
	}

	@Override
	protected XMLBeanEditor createPage1() {
		// We do not want an XML editor
		// so we return null
		return null;
	}

}
