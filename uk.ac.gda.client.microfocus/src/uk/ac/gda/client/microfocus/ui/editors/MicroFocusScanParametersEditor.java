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

package uk.ac.gda.client.microfocus.ui.editors;

import java.net.URL;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.richbeans.CompositeFactory;
import uk.ac.gda.richbeans.editors.DelegatingRichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public final class MicroFocusScanParametersEditor extends RichBeanMultiPageEditorPart implements CompositeFactory {

	private MicroFocusScanParametersComposite microfocusComposite;

	@Override
	public Class<?> getBeanClass() {
		return MicroFocusScanParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return MicroFocusScanParameters.mappingURL;
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		DelegatingRichBeanEditorPart editor = new DelegatingRichBeanEditorPart(path, getMappingUrl(), this,
				editingBean, this);
		editor.setScrollable(true);
		editor.setRichEditorTabText("Map");
		return editor;
	}

	@Override
	protected RichBeanEditorPart createPage0() {
		RichBeanEditorPart result = super.createPage0();
		microfocusComposite.updateScanInfo();
		return result;
	}

	@Override
	public URL getSchemaUrl() {
		return MicroFocusScanParameters.schemaUrl;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		microfocusComposite = new MicroFocusScanParametersComposite(parent, style);
		return microfocusComposite;
	}

}
