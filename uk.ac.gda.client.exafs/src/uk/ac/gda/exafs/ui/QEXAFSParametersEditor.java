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

package uk.ac.gda.exafs.ui;

import java.net.URL;

import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class QEXAFSParametersEditor extends ExafsBeanFileSelectionEditor implements DirtyContainer {

	@Override
	public Class<?> getBeanClass() {
		return QEXAFSParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return QEXAFSParameters.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new QEXAFSParametersUIEditor(path, this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return QEXAFSParameters.schemaURL; // Please make sure this field is present and the schema
	}

}