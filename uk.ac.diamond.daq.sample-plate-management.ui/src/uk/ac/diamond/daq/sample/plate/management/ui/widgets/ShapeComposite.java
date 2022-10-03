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

package uk.ac.diamond.daq.sample.plate.management.ui.widgets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.sample.plate.management.ui.PathscanConfigConstants;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegionConfig;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegisteredSample;

public class ShapeComposite extends Composite {
	public static final int MAX_POINTS = 100;

	private IEventBroker eventBroker;

	private String shapeName;

	private AnalyserComposite analyserComposite;

	private Combo shapeCombo;

	private Combo samplesCombo;

	private Map<String, RegionConfig> regionConfigs;

	private Map<String, RegisteredSample> displayedSamples = new HashMap<>();

	private RegisteredSample currentSample;

	private Text xStartLineText;

	private Text yStartLineText;

	private Text xEndLineText;

	private Text yEndLineText;

	private Spinner pointsSpinner;

	private Text xPointText;

	private Text yPointText;

	public ShapeComposite(Composite parent, int style, String shapeName, List<RegisteredSample> samples, IEventBroker eventBroker) {
		super(parent, style);
		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(4).create());
		this.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.fillDefaults().grab(true, false).span(4,1).applyTo(this);

		this.shapeName = shapeName;
		this.eventBroker = eventBroker;

		addLabel(this, "Sample:", span(1).align(SWT.FILL, SWT.FILL));
		samplesCombo = new Combo(this, SWT.READ_ONLY);
		span(1).applyTo(samplesCombo);

		addLabel(this, "Thickness:", span(1));
		Label thicknessLabel = addLabel(this, "0 mm", span(1).grab(true, false).align(SWT.BEGINNING, SWT.CENTER));

		for (RegisteredSample sample: samples) {
			String sampleName = sample.getId() + ": " + sample.getLabel();
			samplesCombo.add(sampleName);
			displayedSamples.put(sampleName, sample);
		}
		samplesCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			String sampleName = ((Combo) event.getSource()).getText();
			currentSample = displayedSamples.get(sampleName);
			thicknessLabel.setText(currentSample.getThickness().toString() + " mm");
		}));

		addLabel(this, "Shape:", span(1).align(SWT.FILL, SWT.FILL));
		shapeCombo = addShapeCombo(this);

		analyserComposite = new AnalyserComposite(this, SWT.NONE, eventBroker);
	}

	public String getShapeName() {
		return shapeName;
	}

	public Combo getShapeCombo() {
		return shapeCombo;
	}

	public Combo getSampleCombo() {
		return samplesCombo;
	}

	public double getThickness() {
		return currentSample.getThickness();
	}

	public Map<String, RegionConfig> getRegionConfigs() {
		return regionConfigs;
	}

	public Spinner getPointsSpinner() {
		return pointsSpinner;
	}

	public Text getXStartLineText() {
		return xStartLineText;
	}

	public Text getYStartLineText() {
		return yStartLineText;
	}

	public Text getXEndLineText() {
		return xEndLineText;
	}

	public Text getYEndLineText() {
		return yEndLineText;
	}

	public Text getXPointText() {
		return xPointText;
	}

	public Text getYPointText() {
		return yPointText;
	}

	public AnalyserComposite getAnalyserComposite() {
		return analyserComposite;
	}

	private Combo addShapeCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);

		regionConfigs = new HashMap<>();
		regionConfigs.put("Line", new RegionConfig(getLineShapeGroup(this), new LineMappingRegion(), new TwoAxisLinePointsModel()));
		regionConfigs.put("Point", new RegionConfig(getPointShapeGroup(this), new PointMappingRegion(), new TwoAxisPointSingleModel()));

		combo.setItems(regionConfigs.keySet().toArray(String[]::new));
		GridDataFactory.swtDefaults().span(3, 1).applyTo(combo);

		combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			String comboText = ((Combo) event.getSource()).getText();
			// TODO: lock tab switching
			for (String shape: regionConfigs.keySet()) {
				if (shape.equals(comboText)) {
					Group group = regionConfigs.get(shape).getGroup();
					setShowComposite(group, true);
				} else {
					Group group = regionConfigs.get(shape).getGroup();
					setShowComposite(group, false);
				}
			}
		}));

		return combo;
	}

	private void setShowComposite(Composite composite, boolean show) {
		GridData data = (GridData) composite.getLayoutData();
		data.exclude = !show;
		composite.setVisible(show);
		composite.layout(true, true);										// group
		composite.getParent().getParent().getParent().layout(true, true);	// child (pathscanconfigview)
		eventBroker.post(PathscanConfigConstants.TOPIC_RESIZE_SCROLL, null);
	}

	private Group getLineShapeGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		GridData dataLineShapeGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
		dataLineShapeGroup.horizontalSpan = 4;
		dataLineShapeGroup.exclude = true;
		group.setVisible(false);
		group.setLayoutData(dataLineShapeGroup);
		group.setLayout(GridLayoutFactory.swtDefaults().numColumns(6).create());
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		addLabel(group, "X Start:", span(1).align(SWT.FILL, SWT.FILL));
		xStartLineText = addText(group, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		addLabel(group, "mm", span(1).align(SWT.FILL, SWT.FILL));

		addLabel(group, "Y Start:", span(1).align(SWT.FILL, SWT.FILL));
		yStartLineText = addText(group, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		addLabel(group, "mm", span(1).align(SWT.FILL, SWT.FILL));

		addLabel(group, "X End:", span(1).align(SWT.FILL, SWT.FILL));
		xEndLineText = addText(group, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		addLabel(group, "mm", span(1).align(SWT.FILL, SWT.FILL));

		addLabel(group, "Y End:", span(1).align(SWT.FILL, SWT.FILL));
		yEndLineText = addText(group, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		addLabel(group, "mm", span(1).align(SWT.FILL, SWT.FILL));

		addLabel(group, "Points:", span(1).align(SWT.FILL, SWT.FILL));
		pointsSpinner = new Spinner(group, SWT.BORDER);
		pointsSpinner.setMinimum(2);
		pointsSpinner.setMaximum(MAX_POINTS);
		pointsSpinner.setSelection(2);
		pointsSpinner.setIncrement(1);
		pointsSpinner.setPageIncrement(10);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(pointsSpinner);

		return group;
	}

	private Group getPointShapeGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		GridData dataPointShapeGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
		dataPointShapeGroup.horizontalSpan = 4;
		dataPointShapeGroup.exclude = true;
		group.setVisible(false);
		group.setLayoutData(dataPointShapeGroup);
		group.setLayout(GridLayoutFactory.swtDefaults().numColumns(6).create());
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		addLabel(group, "X:", span(1).align(SWT.FILL, SWT.FILL));
		xPointText = addText(group, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		addLabel(group, "mm ", span(1).align(SWT.FILL, SWT.FILL));

		addLabel(group, "Y:", span(1).align(SWT.FILL, SWT.FILL));
		yPointText = addText(group, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		addLabel(group, "mm ", span(1).align(SWT.FILL, SWT.FILL));

		return group;
	}

	private Text addText(Composite parent, GridDataFactory layout, boolean textEnabled) {
		Text text = new Text(parent, SWT.BORDER);
		text.setEnabled(textEnabled);
		layout.applyTo(text);
		return text;
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		layout.applyTo(label);
		return label;
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.swtDefaults().span(span, 1);
	}
}