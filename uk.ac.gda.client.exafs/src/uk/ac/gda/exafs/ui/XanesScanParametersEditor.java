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

import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class XanesScanParametersEditor extends XasXanesParametersEditor {

	@Override
	public Class<?> getBeanClass() {
		return XanesScanParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return XanesScanParameters.mappingURL;
	}

	@Override
	public URL getSchemaUrl() {
		return XanesScanParameters.schemaUrl;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(final String path, final Object editingBean) {
		return new XanesScanParametersUIEditor(path, this, editingBean);
	}

}
