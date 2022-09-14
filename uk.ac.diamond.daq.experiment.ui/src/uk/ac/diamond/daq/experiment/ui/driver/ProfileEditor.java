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

package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.Services;
import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public class ProfileEditor {

	private static final Logger logger = LoggerFactory.getLogger(ProfileEditor.class);

	private ListWithCustomEditor listEditor = new ListWithCustomEditor();
	private DriverProfileSectionEditor elementEditor = new DriverProfileSectionEditor();

	private PropertyChangeListener modelChanged = event -> updatePlot();

	private IPlottingSystem<Composite> plottingSystem;
	private String quantityName;
	private String quantityUnits;

	private SingleAxisLinearSeries model;

	public void createControl(Composite parent) {
		Composite base = new Composite(parent, SWT.BORDER);
		base.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		base.setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(base);
		STRETCH.copy().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(base);

		Composite listEditorComposite = new Composite(base, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(false, true).applyTo(listEditorComposite);
		GridLayoutFactory.swtDefaults().applyTo(listEditorComposite);

		listEditorComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		listEditorComposite.setBackgroundMode(SWT.INHERIT_FORCE);

		new Label(listEditorComposite, SWT.NONE).setText("Sections");

		listEditor.setListHeight(150);
		listEditor.setTemplate(new DriverProfileSection());

		listEditor.setElementEditor(elementEditor);
		listEditor.addListListener(event -> listChanged());
		listEditor.create(listEditorComposite);

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Could not create plot", e);
			return;
		}
		plottingSystem.createPlotPart(base, "profile", null, PlotType.XY, null);
		plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		listEditor.addListListener(modelChanged);
		updatePlot();
	}

	public void setModel(SingleAxisLinearSeries model) {
		this.model = model;
		listEditor.getList().forEach(section -> section.removePropertyChangeListener(modelChanged));
		listEditor.setList(model.getProfile().stream()
				.map(EditableWithListWidget.class::cast).collect(Collectors.toList()));

		model.getProfile().forEach(section -> section.addPropertyChangeListener(modelChanged));
		updatePlot();
	}

	public void clear() {
		this.model = null;
		listEditor.setList(Collections.emptyList());
		plottingSystem.clear();
	}

	private void updatePlot() {
		plottingSystem.clear();

		// create dataset from model
		List<DriverProfileSection> segments = getProfile();
		if (segments.isEmpty()) return;

		double[] x = new double[segments.size()+1];
		double[] y = new double[segments.size()+1];

		x[0] = 0;
		y[0] = segments.get(0).getStart();

		for (int i = 0; i < segments.size(); i++) {
			x[i+1] = segments.get(i).getDuration() + x[i];
			y[i+1] = segments.get(i).getStop();
		}

		final Dataset xDataset = DatasetFactory.createFromObject(x);
		final Dataset yDataset = DatasetFactory.createFromObject(y);

		xDataset.setName("Time (min)");
		yDataset.setName(getQuantityName() + " (" + getQuantityUnits() + ")");

		plottingSystem.createPlot1D(xDataset, Arrays.asList(yDataset), null);
		plottingSystem.clearAnnotations();
		plottingSystem.setTitle("");
		plottingSystem.setShowLegend(false);
	}

	private String getQuantityName() {
		return quantityName != null ? quantityName : "Unknown";
	}
	private String getQuantityUnits() {
		return quantityUnits != null ? quantityUnits : "a.u.";
	}

	private void listChanged() {
		listEditor.getList().forEach(item -> {
			item.removePropertyChangeListener(modelChanged);
			item.addPropertyChangeListener(modelChanged);
		});
	}

	public void saveProfile(String driver, String experimentId) {
		model.setProfile(getProfile());
		Services.getExperimentService().saveDriverProfile(model, driver, experimentId);
	}

	private List<DriverProfileSection> getProfile() {
		return listEditor.getList().stream().map(DriverProfileSection.class::cast).collect(Collectors.toList());
	}
}
