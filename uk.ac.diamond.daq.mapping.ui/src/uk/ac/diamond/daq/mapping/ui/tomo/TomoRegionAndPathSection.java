/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathMapper.mapRegionOntoModel;

import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor.PathOption;

class TomoRegionAndPathSection extends AbstractRegionAndPathSection {

	protected TomoRegionAndPathSection(TensorTomoScanSetupView tomoView) {
		super(tomoView, RegionAndPathType.TOMO);
	}

	@Override
	protected RegionAndPathConfig createRegionAndPathConfig(RegionAndPathType type) {
		final TensorTomoScanBean tomoBean = getTomoBean();

		final RegionAndPathConfig config = new RegionAndPathConfig();
		config.type = type;
		config.axis1Name = tomoBean.getAngle1ScannableName();
		config.axis2Name = tomoBean.getAngle2ScannableName();
		config.regionModel = tomoBean.getAngleRegionModel();
		config.pathModel = tomoBean.getAnglePathModel();
		config.units = "deg";
		return config;
	}

	@Override
	public void createRegionAndPathEditors(final Composite parent, RegionAndPathConfig config) {
		super.createRegionAndPathEditors(parent, config);
		createRestoreDefaultsButton(parent);
	}

	private void createRestoreDefaultsButton(final Composite parent) {
		final Button tomoDefaultsButton = new Button(parent, SWT.PUSH);
		tomoDefaultsButton.setText("Restore Defaults");
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.TRAIL, SWT.CENTER).applyTo(tomoDefaultsButton);
		tomoDefaultsButton.addSelectionListener(widgetSelectedAdapter(event -> setTomographyDefaults()));
	}

	private void setTomographyDefaults() {
		// TODO what should the defaults be?
		final TensorTomoScanBean tomoBean = getTomoBean();
		final RectangularMappingRegion tomoRegion = (RectangularMappingRegion) tomoBean.getAngleRegionModel();
		tomoRegion.setxStart(0);
		tomoRegion.setxStop(180);
		tomoRegion.setyStart(0);
		tomoRegion.setyStop(90);

		// TODO calculate steps using formula
		tomoBean.getAnglePathModel().setxAxisStep(1.0);
		tomoBean.getAnglePathModel().setyAxisStep(1.0);
		mapRegionOntoModel(tomoBean.getAngleRegionModel(), tomoBean.getAnglePathModel());
	}

	@Override
	protected String getSectionLabel() {
		return "Tomography Setup";
	}

	@Override
	protected Set<PathOption> getPathOptions(final RegionAndPathConfig config) {
		return Set.of();
	}

}