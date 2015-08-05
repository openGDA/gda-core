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

package uk.ac.gda.richbeans.examples;

import java.net.URL;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.richbeans.CompositeFactory;
import uk.ac.gda.richbeans.editors.DelegatingRichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 *
 */
public class ExampleEditor extends RichBeanMultiPageEditorPart implements CompositeFactory {

	@Override
	public Class<?> getBeanClass() {
		return ExampleParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return ExampleParameters.mappingURL;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {

		DelegatingRichBeanEditorPart editor = new DelegatingRichBeanEditorPart(path, getMappingUrl(), this,
				editingBean, this);
		editor.setRichEditorTabText("Example Custom UI");
		return editor;
	}

	@Override
	public URL getSchemaUrl() {
		return ExampleParameters.schemaURL;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new ExampleParametersComposite(parent, style);
	}
}