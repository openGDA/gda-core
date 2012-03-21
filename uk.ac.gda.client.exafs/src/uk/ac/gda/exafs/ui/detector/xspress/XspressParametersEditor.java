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

package uk.ac.gda.exafs.ui.detector.xspress;


import java.net.URL;
import java.util.Arrays;
import java.util.List;

import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.ExafsBeanFileSelectionEditor;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

/**
 *
 */
public class XspressParametersEditor extends ExafsBeanFileSelectionEditor {

	
	/**
	 * The id for the editor
	 */
	public static final String ID = "org.diamond.exafs.ui.XspressParametersEditor";

	@Override
	public Class<?> getBeanClass() {
		return XspressParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return XspressParameters.mappingURL;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(String path,
			                                           Object editingBean) {
		return new XspressParametersUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return XspressParameters.schemaURL;
	}
	
	@Override
	public List<String> getPrivateXMLFields() {
		return Arrays.asList(new String[]{"data"});
	}

}
