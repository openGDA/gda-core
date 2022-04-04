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

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathModelEditor;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor.PathOption;
import uk.ac.diamond.daq.mapping.ui.path.PathEditorProvider;
import uk.ac.diamond.daq.mapping.ui.region.RegionEditorProvider;

abstract class AbstractRegionAndPathSection extends AbstractTomoViewSection {

	protected enum RegionAndPathType {
		MAP, TOMO;
	}

	protected static class RegionAndPathConfig {
		public RegionAndPathType type;
		public String axis1Name;
		public String axis2Name;
		public IMappingScanRegionShape regionModel;
		public AbstractTwoAxisGridModel pathModel;
		public String units;
	}

	private final RegionAndPathType type;

	protected AbstractRegionAndPathSection(TensorTomoScanSetupView tomoView, RegionAndPathType type) {
		super(tomoView);
		this.type = type;
	}

	@Override
	public void createControls(Composite parent) {
		createSeparator(parent);

		final Composite composite = createComposite(parent, 1, true);
		createUpperControls(composite);

		final RegionAndPathConfig config = createRegionAndPathConfig(type);
		createRegionAndPathEditors(composite, config);
	}

	protected void createUpperControls(final Composite composite) {
		final Label label = new Label(composite, SWT.NONE);
		label.setText(getSectionLabel());
	}

	protected abstract String getSectionLabel();

	protected void createRegionAndPathEditors(final Composite parent, final RegionAndPathConfig config) {
		final Composite editorsComposite = createComposite(parent, 2, false);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 5).applyTo(editorsComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(editorsComposite);

		createRegionEditor(editorsComposite, config);
		createPathEditor(editorsComposite, config);
	}

	private void createPathEditor(final Composite parent, final RegionAndPathConfig config) {
		// grid model is the step for each axis
		final AbstractPathEditor pathEditor = PathEditorProvider.createPathComposite(config.pathModel, getEclipseContext());
		pathEditor.setAxisNames(config.axis1Name, config.axis2Name);
		pathEditor.setOptionsToDisplay(getPathOptions(config));
		pathEditor.createEditorPart(parent);
	}

	protected abstract Set<PathOption> getPathOptions(final RegionAndPathConfig config);

	private void createRegionEditor(final Composite parent, final RegionAndPathConfig config) {
		// Region is the start/stop for each axis
		final Map<String, String> regionUnits = Map.of(config.axis1Name, config.units, config.axis2Name, config.units);
		final AbstractRegionPathModelEditor<IMappingScanRegionShape> regionEditor = RegionEditorProvider.createRegionEditor(
				config.regionModel, regionUnits, getEclipseContext());
		regionEditor.setAxisNames(config.axis1Name, config.axis2Name);
		regionEditor.setUnitsEditable(false);
		regionEditor.createEditorPart(parent);
	}

	protected abstract RegionAndPathConfig createRegionAndPathConfig(RegionAndPathType type);

	@Override
	public void configureScanBean(ScanBean scanBean) {
		// nothing to do - creating the CompoundModel is done by the view
	}

}
