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

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorCompositeController;
import uk.ac.gda.richbeans.CompositeFactory;
import uk.ac.gda.richbeans.editors.DelegatingRichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * Configures the parameters of a fluorescence detector. Subclasses should override getBeanClass(), getMappingUrl(),
 * getSchemaUrl(), getRichEditorTabText() and getNewController() with specific implementations.
 */
public abstract class FluorescenceDetectorParametersEditor extends RichBeanMultiPageEditorPart implements
		CompositeFactory {

	FluorescenceDetectorCompositeController controller;

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		DelegatingRichBeanEditorPart editor = new DelegatingRichBeanEditorPart(path, getMappingUrl(), this,
				editingBean, this);
		editor.setRichEditorTabText(getRichEditorTabText());
		controller = getNewController();
		controller.setEditingBean(editingBean);
		return editor;
	}

	protected abstract String getRichEditorTabText();

	protected abstract FluorescenceDetectorCompositeController getNewController();

	@Override
	public Composite createComposite(Composite parent, int style) {
		FluorescenceDetectorComposite composite = new FluorescenceDetectorComposite(parent, style);
		if (controller == null) {
			controller = getNewController();
		}
		controller.setEditorUI(composite);
		controller.initialise();
		return composite;
	}

	@Override
	protected void linkUI() {
		super.linkUI();
		if (controller != null) {
			controller.setEditingBean(editingBean);
		}
	}
}
