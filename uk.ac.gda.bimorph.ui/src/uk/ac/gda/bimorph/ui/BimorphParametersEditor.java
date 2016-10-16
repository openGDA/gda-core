/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.bimorph.ui;

import java.net.URL;

import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public final class BimorphParametersEditor extends RichBeanMultiPageEditorPart {

	@Override
	public Class<?> getBeanClass() {
		return BimorphParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return BimorphParameters.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new BimorphParametersUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return BimorphParameters.schemaURL; // Please make sure this field is present and the schema
	}
}