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

package org.diamond.tomo.highrestomo.ui.editors;

import java.net.URL;

import org.diamond.tomo.highrestomo.beans.HighResolutionTomographyParameters;

import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * @author fcp94556
 *
 */
public class HighResTomoEditor extends RichBeanMultiPageEditorPart {

	@Override
	public Class<?> getBeanClass() {
		return HighResolutionTomographyParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return HighResolutionTomographyParameters.mappingURL;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new HighResTomoBeanEditor(path,getMappingUrl(),this,editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return HighResolutionTomographyParameters.schemaURL;
	}

}

	