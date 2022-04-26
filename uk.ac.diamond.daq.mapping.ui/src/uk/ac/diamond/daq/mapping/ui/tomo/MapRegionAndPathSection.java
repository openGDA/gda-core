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

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor.CommonPathOption;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor.PathOption;

class MapRegionAndPathSection extends AbstractRegionAndPathSection {

	protected MapRegionAndPathSection(TensorTomoScanSetupView tomoView) {
		super(tomoView, RegionAndPathType.MAP);
	}

	@Override
	protected RegionAndPathConfig createRegionAndPathConfig(RegionAndPathType type) {
		final TensorTomoScanBean tomoBean = getTomoBean();
		final MappingStageInfo mappingStageInfo = getService(MappingStageInfo.class);

		final RegionAndPathConfig config = new RegionAndPathConfig();
		config.type = type;
		config.axis1Name = mappingStageInfo.getPlotXAxisName();
		config.axis2Name = mappingStageInfo.getPlotYAxisName();
		config.regionModel = tomoBean.getGridRegionModel();
		config.pathModel = tomoBean.getGridPathModel();
		config.units = "mm";
		return config;
	}

	@Override
	protected void createUpperControls(final Composite composite) {
		super.createUpperControls(composite);
		createDrawMapControl(composite);
	}

	private void createDrawMapControl(Composite parent) {
		final Composite composite = createComposite(parent, 2, true);

		// TODO do we need the draw default region button, used by the mapping view (MultiFunctionButton)?
		final Label redrawLabel = new Label(composite, SWT.NONE);
		redrawLabel.setText("Click button to draw/redraw mapping region:");

		final Button redrawRegionButton = new Button(composite, SWT.NONE);
		redrawRegionButton.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/map--pencil.png")));
		redrawRegionButton.setToolTipText("Draw/Redraw region");
		redrawRegionButton.addSelectionListener(widgetSelectedAdapter(e -> tomoView.drawMappingRegion()));
	}

	@Override
	protected String getSectionLabel() {
		return "Map Grid Setup";
	}

	@Override
	protected Set<PathOption> getPathOptions(final RegionAndPathConfig config) {
		return Set.of(CommonPathOption.ALTERNATING);
	}

}
