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

import java.util.Optional;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorCompositeController;
import uk.ac.gda.exafs.ui.views.detectors.FluorescenceDetectorViewFactory;
import uk.ac.gda.richbeans.CompositeFactory;
import uk.ac.gda.richbeans.editors.DelegatingRichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * Configures the parameters of a fluorescence detector. Subclasses should override getBeanClass(), getMappingUrl(),
 * getSchemaUrl() and getRichEditorTabText() with specific implementations.
 */
public abstract class FluorescenceDetectorParametersEditor extends RichBeanMultiPageEditorPart implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorParametersEditor.class);

	private FluorescenceDetectorParameters cachedBean;
	protected FluorescenceDetectorCompositeController controller;

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		DelegatingRichBeanEditorPart editor = new DelegatingRichBeanEditorPart(path, getMappingUrl(), this, editingBean, this);
		editor.setRichEditorTabText(getRichEditorTabText());
		if (editingBean instanceof FluorescenceDetectorParameters) {
			cachedBean = (FluorescenceDetectorParameters) editingBean;
		}
		return editor;
	}

	protected abstract String getRichEditorTabText();

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.info("Creating Fluorescence detector view for detector : {}", cachedBean.getDetectorName());
		FluorescenceDetectorComposite composite = new FluorescenceDetectorComposite(parent, style);
		controller = new FluorescenceDetectorCompositeController(composite);
		controller.setDetectorParameters(cachedBean);
		controller.setContainer(this);

		// Try to set the max counts and MCA scannables using the view factory configuration settings.
		getViewConfig(cachedBean.getDetectorName()).ifPresent(viewConfig -> {
			logger.info("Using max detector element counts and MCA scannable names from view config settings in {}", viewConfig.getName());
			composite.setMaxDetectorElementCounts(viewConfig.getMaxDetectorElementCounts());
			controller.setScannablesForMcaFiles(viewConfig.getMcaScannablesToUse());
		});
		controller.initialise();
		return composite;
	}

	/**
	 * Find the {@link FluorescenceDetectorViewFactory} object for the named detector
	 * in the client spring configuration
	 *
	 * @param detectorName
	 * @return Optional view factory object; empty optional if config is not present for the detecto
	 */
	private Optional<FluorescenceDetectorViewFactory> getViewConfig(String detectorName) {
		return Finder.listLocalFindablesOfType(FluorescenceDetectorViewFactory.class)
				.stream()
				.filter(config-> config.getDetectorName().equals(detectorName))
				.findFirst();
	}

	@Override
	protected void linkUI() {
		super.linkUI();
		if (controller != null) {
			// This method is called after the editing bean has been changed; pass that on to the controller
			if (editingBean instanceof FluorescenceDetectorParameters) {
				controller.setDetectorParameters((FluorescenceDetectorParameters) editingBean);
			} else {
				throw new IllegalStateException("Editing bean must be a FluorescenceDetectorParameters object");
			}
		}
	}
}
