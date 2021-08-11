/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.opal.checkboxgroup.CheckBoxGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.gda.beans.exafs.i18.AttenuatorParameters;
import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public class I18SampleParametersUIEditor extends FauxRichBeansEditor<I18SampleParameters> {

	private static final Logger logger = LoggerFactory.getLogger(I18SampleParametersUIEditor.class);

	public I18SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, I18SampleParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	private static final int GROUP_WIDTH = 380;

	/**
	 * GridData for groups to be left aligned and have a width of {@link #GROUP_WIDTH}
	 */
	private static final GridDataFactory GROUP_FORMAT = GridDataFactory
														.fillDefaults()
														.align(SWT.LEFT, SWT.CENTER)
														.hint(GROUP_WIDTH, SWT.DEFAULT)
														.grab(true, false);

	/**
	 * 2-column layout for composites inside groups
	 */
	private static final GridLayoutFactory TWO_COLUMN_LAYOUT = GridLayoutFactory
																.fillDefaults()
																.numColumns(2)
																.equalWidth(true)
																.spacing(20, 5);

	/**
	 * For right-aligned labels
	 */
	private static final GridDataFactory RIGHT_ALIGN = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);

	/**
	 * For controls to take all available space horizontally
	 */
	private static final GridDataFactory STRETCH = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	private ScrolledComposite scrolledComposite;

	@Override
	public void createPartControl(Composite parent) {

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		var sampleParametersComposite = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(sampleParametersComposite);

		scrolledComposite.setContent(sampleParametersComposite);

		var details = formattedCompositeInGroup(sampleParametersComposite, "Sample Details");
		createSampleDetailsSection(details);

		var sampleStage = formattedCompositeInGroup(sampleParametersComposite, "Sample Stage");
		createSampleStageSection(sampleStage);

		var attenuators = formattedCompositeInGroup(sampleParametersComposite, "Attenuators");
		createAttenuatorsSection(attenuators);

		createMirrorSection(sampleParametersComposite);

		scrolledComposite.setMinSize(sampleParametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private Composite formattedCompositeInGroup(Composite parent, String groupName) {
		final var group = new Group(parent, SWT.NONE);
		group.setText(groupName);

		GridLayoutFactory.swtDefaults().applyTo(group);
		GROUP_FORMAT.applyTo(group);

		final var composite = new Composite(group, SWT.NONE);
		TWO_COLUMN_LAYOUT.applyTo(composite);

		STRETCH.applyTo(composite);

		return composite;
	}

	private void createSampleDetailsSection(Composite details) {

		// controls
		var nameLabel = new Label(details, SWT.NONE);
		nameLabel.setText("Name");
		RIGHT_ALIGN.applyTo(nameLabel);

		var name = new Text(details, SWT.BORDER);
		STRETCH.applyTo(name);

		var descriptionLabel = new Label(details, SWT.NONE);
		descriptionLabel.setText("Description");
		RIGHT_ALIGN.copy().align(SWT.RIGHT, SWT.TOP).applyTo(descriptionLabel);

		var description = new Text(details, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		STRETCH.copy().hint(SWT.DEFAULT, 50).applyTo(description);

		// bindings
		name.setText(getBean().getName() == null ? "" : getBean().getName());
		name.addListener(SWT.Modify, event -> {
			getBean().setName(name.getText());
			beanChanged();
		});

		description.setText(getBean().getDescription() == null ? "" : getBean().getDescription());
		description.addListener(SWT.Modify, event -> {
			getBean().setDescription(description.getText());
			beanChanged();
		});
	}

	private void createSampleStageSection(Composite section) {

		// controls
		var xLabel = new Label(section, SWT.NONE);
		RIGHT_ALIGN.applyTo(xLabel);
		xLabel.setText("X");

		var x = new Text(section, SWT.BORDER);
		STRETCH.applyTo(x);

		var yLabel = new Label(section, SWT.NONE);
		RIGHT_ALIGN.applyTo(yLabel);
		yLabel.setText("Y");

		var y = new Text(section, SWT.BORDER);
		STRETCH.applyTo(y);

		var zLabel = new Label(section, SWT.NONE);
		RIGHT_ALIGN.applyTo(zLabel);
		zLabel.setText("Z");

		var z = new Text(section, SWT.BORDER);
		STRETCH.applyTo(z);

		skipCell(section);

		var fetchPosition = new Button(section, SWT.PUSH);
		fetchPosition.setText("Fetch stage position");
		STRETCH.applyTo(fetchPosition);


		// bindings
		var bean = getBean().getSampleStageParameters();

		x.setText(String.valueOf(bean.getX()));
		x.addListener(SWT.Modify, event -> {
			bean.setX(Double.parseDouble(x.getText()));
			beanChanged();
		});

		y.setText(String.valueOf(bean.getY()));
		y.addListener(SWT.Modify, event -> {
			bean.setY(Double.parseDouble(y.getText()));
			beanChanged();
		});

		z.setText(String.valueOf(bean.getZ()));
		z.addListener(SWT.Modify, event -> {
			bean.setZ(Double.parseDouble(z.getText()));
			beanChanged();
		});

		fetchPosition.addListener(SWT.Selection, event -> {

			Scannable xAxis = Finder.find(bean.getXName());
			Scannable yAxis = Finder.find(bean.getYName());
			Scannable zAxis = Finder.find(bean.getZName());

			try {
				double xPos = (double) xAxis.getPosition();
				double yPos = (double) yAxis.getPosition();
				double zPos = (double) zAxis.getPosition();

				x.setText(String.valueOf(xPos));
				y.setText(String.valueOf(yPos));
				z.setText(String.valueOf(zPos));

				bean.setX(xPos);
				bean.setY(yPos);
				bean.setZ(zPos);

				beanChanged();
			} catch (DeviceException e) {
				logger.error("Error retrieving stage position");
			}

		});
	}

	private void createAttenuatorsSection(Composite section) {

		Map<String, Combo> attenuators = new HashMap<>();

		for (AttenuatorParameters attenuator : getBean().getAttenuators()) {
			var label = new Label(section, SWT.NONE);
			label.setText(attenuator.getName());
			RIGHT_ALIGN.applyTo(label);

			var combo = new Combo(section, SWT.READ_ONLY);
			STRETCH.applyTo(combo);

			List<String> attn1Positions = attenuator.getPosition();
			combo.setItems(attn1Positions.toArray(new String[0]));
			combo.select(attn1Positions.indexOf(attenuator.getSelectedPosition()));
			combo.addListener(SWT.Selection, event -> {
				attenuator.setSelectedPosition(combo.getText());
				beanChanged();
			});

			attenuators.put(attenuator.getName(), combo);
		}

		skipCell(section);

		var fetch = new Button(section, SWT.PUSH);
		fetch.setText("Fetch positions");
		STRETCH.applyTo(fetch);

		fetch.addListener(SWT.Selection, event -> {
			for (AttenuatorParameters attenuatorBean : getBean().getAttenuators()) {
				String attenuatorName = attenuatorBean.getName();
				Scannable attenuator = Finder.find(attenuatorName);
				try {
					String position = (String) attenuator.getPosition();
					attenuators.get(attenuatorName).select(attenuatorBean.getPosition().indexOf(position));
					attenuatorBean.setSelectedPosition(position);
					beanChanged();
				} catch (DeviceException e) {
					logger.error("Error getting position for attenuator {}", attenuatorName, e);
				}
			}
		});
	}

	private void createMirrorSection(Composite parent) {

		//controls
		final var group = new CheckBoxGroup(parent, SWT.NONE);
		group.setText("KB Mirror");

		GridLayoutFactory.swtDefaults().applyTo(group);
		GROUP_FORMAT.applyTo(group);

		final Composite section = group.getContent();
		TWO_COLUMN_LAYOUT.applyTo(section);
		STRETCH.applyTo(section);

		var xLabel = new Label(section, SWT.NONE);
		xLabel.setText("X");
		RIGHT_ALIGN.applyTo(xLabel);

		var x = new Text(section, SWT.BORDER);
		STRETCH.applyTo(x);

		skipCell(section);

		var fetch = new Button(section, SWT.PUSH);
		fetch.setText("Fetch position");
		STRETCH.applyTo(fetch);

		// binding
		group.setSelection(getBean().isVfmxActive());
		group.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getBean().setVfmxActive(group.isActivated());
				beanChanged();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do not bind unless selection is deliberate
			}
		});


		x.setText(String.valueOf(getBean().getVfmx()));
		x.addListener(SWT.Modify, event -> {
			getBean().setVfmx(Double.parseDouble(x.getText()));
			beanChanged();
		});

		fetch.addListener(SWT.Selection, event -> {
			Scannable xMotor = Finder.find("kb_vfm_x");
			try {
				double position = (double) xMotor.getPosition();
				x.setText(String.valueOf(position));
				getBean().setVfmx(position);
				beanChanged();
			} catch (DeviceException e) {
				logger.error("Error getting KB mirror position", e);
			}
		});
	}

	@SuppressWarnings("unused")
	private void skipCell(Composite composite) {
		new Label(composite, SWT.NONE);
	}

	@Override
	public void setFocus() {
		scrolledComposite.setFocus();
	}

	@Override
	protected String getRichEditorTabText() {
		return "Sample Parameters";
	}

	@Override
	public void dispose() {
		scrolledComposite.dispose();
	}
}
