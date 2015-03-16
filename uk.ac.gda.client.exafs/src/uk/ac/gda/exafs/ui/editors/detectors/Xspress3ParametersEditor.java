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
import uk.ac.gda.exafs.ui.composites.detectors.Xspress3ParametersComposite;
import uk.ac.gda.richbeans.editors.DelegatingRichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public final class Xspress3ParametersEditor extends RichBeanMultiPageEditorPart {

	@Override
	public Class<?> getBeanClass() {
		return Xspress3Parameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return Xspress3Parameters.mappingURL;
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		DelegatingRichBeanEditorPart editor = new DelegatingRichBeanEditorPart(path, getMappingUrl(), this, editingBean);
		editor.setEditorClass(Xspress3ParametersComposite.class);
		editor.setScrollable(true);
		editor.setRichEditorTabText("Xspress3");
		return editor;

		// NB: there is a very subtle bug, which is related to how the DelegatingRichBeanEditorPart works - as it does
		// not share the data model (bean) with the Xspress3ParametersComposite that it instantiates, but does share the
		// bean with the XML editor.
		
		// So if the user presses 'apply to all' and then switches to the XML editor before saving then the XML is not
		// updated, but if the user saves first then everything is OK.
		
		// Not enough time to fix this bug: but the correct fix is related to the fact that the
		// Xspress3ParametersComposite bean is not the same object as the one held by the DelegatingRichBeanEditorPart
		// and the XML editor.
	}

	@Override
	public URL getSchemaUrl() {
		return Xspress3Parameters.schemaURL;
	}

}
