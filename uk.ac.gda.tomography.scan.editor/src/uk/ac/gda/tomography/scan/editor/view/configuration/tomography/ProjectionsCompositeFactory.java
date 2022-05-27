/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view.configuration.tomography;

import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.GUIComponents.integerPositiveContent;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * Composite to edit the points in a {@link ScannableTrackDocument} document.
 *
 * @author Maurizio Nagni
 */
public class ProjectionsCompositeFactory implements CompositeFactory, Reloadable {

	private DataBindingContext bindingContext = new DataBindingContext();
	private ISideEffect updatePublisher;
	private Text projections;

	@Override
	public Composite createComposite(Composite parent, int style) {
		var composite = createClientGroup(parent, SWT.NONE, 1, PROJECTIONS);
		createClientGridDataFactory().applyTo(composite);
		projections = integerPositiveContent(composite, SWT.NONE, SWT.BORDER,
				PROJECTIONS, PROJECTIONS_TOOLTIP);

		bindControls();

		return composite;
	}

	private void bindControls() {
		var projectionsUi = WidgetProperties.text(SWT.Modify).observe(projections);
		var projectionsModel = PojoProperties.value("points", Integer.class).observe(getScannableTrackDocument());
		bindingContext.bindValue(projectionsUi, projectionsModel);

		// when the model is updated we publish an event to notify other components
		updatePublisher = ISideEffect.create(projectionsModel::getValue, ignored ->
			SpringApplicationContextFacade.publishEvent(new ScanningAcquisitionChangeEvent(ProjectionsCompositeFactory.this)));
	}

	private void disposeBindings() {
		updatePublisher.dispose();
		new ArrayList<>(bindingContext.getBindings()).forEach(binding -> {
			bindingContext.removeBinding(binding);
			binding.dispose();
		});
	}

	private ScannableTrackDocument getScannableTrackDocument() {
		return getScanningAcquisitionTemporaryHelper().getScannableTrackDocuments()
				.iterator().next();
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}

	@Override
	public void reload() {
		if (projections == null || projections.isDisposed()) return;
		disposeBindings();
		bindControls();
	}
}
