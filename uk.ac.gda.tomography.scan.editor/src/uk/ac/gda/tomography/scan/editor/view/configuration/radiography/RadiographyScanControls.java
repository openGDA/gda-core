/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view.configuration.radiography;

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.ClientSWTElements.spinner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import gda.factory.Finder;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.tomography.scan.editor.view.configuration.TomographyConfiguration;
import uk.ac.gda.tomography.scan.editor.view.configuration.tomography.DarkFlatCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.configuration.tomography.ExposureCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.configuration.tomography.InAndOutOfBeamPositionControls;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;


public class RadiographyScanControls implements CompositeFactory, Reloadable {

	private Text name;
	private Spinner projections;

	private List<Reloadable> reloadables = new ArrayList<>();
	private DataBindingContext bindingContext = new DataBindingContext();

	private final TomographyConfiguration config;

	public RadiographyScanControls() {
		config = Finder.findLocalSingleton(TomographyConfiguration.class);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		var composite = composite(parent, 1);
		STRETCH.copy().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

		createNameControl(composite);
		createProjectionsControls(composite);
		createDetectorControls(composite);
		createDarksAndFlatsControls(composite);
		createPositionControls(composite);

		bindControls();

		return composite;
	}

	private void createNameControl(Composite parent) {
		var composite = composite(parent, 2);
		label(composite, "Acquisition name");
		name = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(name);
	}

	private void createProjectionsControls(Composite parent) {
		var projectionsComposite = composite(parent, 2);
		label(projectionsComposite, "Projections");
		projections = spinner(projectionsComposite);
	}

	private void createDetectorControls(Composite parent) {
		var exposure = composite(parent, 2);

		label(exposure, "Detector exposure (s)");
		var exposureControl = new ExposureCompositeFactory();
		exposureControl.createComposite(exposure, SWT.NONE);
		reloadables.add(exposureControl);
	}

	private void createDarksAndFlatsControls(Composite parent) {
		var darkAndFlats = new DarkFlatCompositeFactory();
		darkAndFlats.createComposite(parent, SWT.NONE);
		reloadables.add(darkAndFlats);
	}

	private void createPositionControls(Composite parent) {
		var position = new InAndOutOfBeamPositionControls(config);
		position.createControls(parent);
		reloadables.add(position);
	}

	@Override
	public void reload() {
		disposeBindings();
		bindControls();
		reloadables.forEach(Reloadable::reload);
	}

	private void disposeBindings() {
		new ArrayList<>(bindingContext.getBindings()).forEach(binding -> {
			bindingContext.removeBinding(binding);
			binding.dispose();
		});
	}

	private void bindControls() {
		bindName();
		bindProjections();
	}

	private void bindName() {
		var nameUi = WidgetProperties.text(SWT.Modify).observe(name);
		var nameModel = PojoProperties.value("name", String.class).observe(getScanningAcquisition());
		bindingContext.bindValue(nameUi, nameModel);
	}

	private void bindProjections() {
		var projectionsUi = WidgetProperties.spinnerSelection().observe(projections);
		var projectionsModel = PojoProperties.value("points", Integer.class).observe(getScanningAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getScanpathDocument().getScannableTrackDocuments().get(0));
		bindingContext.bindValue(projectionsUi, projectionsModel);
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}

	private ScanningAcquisition getScanningAcquisition() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningAcquisition().orElseThrow();
	}
}
