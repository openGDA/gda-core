/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.widget.ActiveMode;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.beans.exafs.b18.FurnaceParameters;
import uk.ac.gda.beans.exafs.b18.LakeshoreParameters;
import uk.ac.gda.beans.exafs.b18.PulseTubeCryostatParameters;
import uk.ac.gda.beans.exafs.b18.SampleWheelParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.SampleMotorViewConfig.ConfigDetails;
import uk.ac.gda.exafs.ui.composites.B18FurnaceComposite;
import uk.ac.gda.exafs.ui.composites.B18PulseTubeCryostatComposite;
import uk.ac.gda.exafs.ui.composites.LakeshoreComposite;
import uk.ac.gda.exafs.ui.composites.SampleWheelParametersComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public final class B18SampleParametersUIEditor extends FauxRichBeansEditor<B18SampleParameters> {

	private static final Logger logger = LoggerFactory.getLogger(B18SampleParametersUIEditor.class);

	private FieldComposite name;
	private FieldComposite description1;
	private FieldComposite description2;
	private ComboWrapper sampleEnvironment;

	private B18PulseTubeCryostatComposite pulseTubeCryostatParameters;
	private B18FurnaceComposite furnaceParameters;
	private LakeshoreComposite lakeshoreComposite;

	private StackLayout environmentLayout;
	private Composite blankEnvironmentComposite;
	private ScrolledComposite scrolledComposite;

	private SampleWheelParametersComposite sampleWheelParametersComposite;
	private List<SampleParameterMotorPositionsComposite> motorPositionComposites = Collections.emptyList();

	private Composite parent;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public B18SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, B18SampleParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	@Override
	public String getRichEditorTabText() {
		return "B18SampleParametersEditor";
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;

		parent.setLayout(new FillLayout());

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite scrolledContents = new Composite(scrolledComposite, SWT.NONE);
		scrolledContents.setLayout(new GridLayout(1, false));
		scrolledComposite.setContent(scrolledContents);

		Composite topComposite = new Composite(scrolledContents, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(topComposite);

		Label nameLabel = new Label(topComposite, SWT.NONE);
		nameLabel.setText("Sample name");

		name = new TextWrapper(topComposite, SWT.BORDER | SWT.SINGLE);
		((TextWrapper) name).setTextType(TEXT_TYPE.FILENAME);
		GridDataFactory.fillDefaults().hint(294, SWT.DEFAULT).applyTo(name);

		Label descriptionLabel = new Label(topComposite, SWT.NONE);
		descriptionLabel.setText("Sample description");
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(descriptionLabel);

		description1 = new TextWrapper(topComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 53).applyTo(description1);

		Label commentsLabel = new Label(topComposite, SWT.NONE);
		commentsLabel.setText("Additional comments");
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(commentsLabel);

		description2 = new TextWrapper(topComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 53).applyTo(description2);

		Composite bottomComposite = new Composite(scrolledContents, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(bottomComposite);

		// Sample stage motors
		setupSampleStageColumns(bottomComposite);

		Composite temperatureSampleWheelContainer = new Composite(bottomComposite, SWT.NONE);
		temperatureSampleWheelContainer.setLayout(new GridLayout(1, false));
		temperatureSampleWheelContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

		// Temperature
		ExpandableComposite temperatureExpandableComposite = new ExpandableComposite(temperatureSampleWheelContainer, SWT.NONE);
		temperatureExpandableComposite.setText("Temperature Controller");
		temperatureExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		createTemp(temperatureExpandableComposite);
		temperatureExpandableComposite.addExpansionListener(IExpansionListener.expansionStateChangedAdapter(e -> {
			if (!sampleEnvironment.getValue().toString().equals("none")) {
				temperatureExpandableComposite.setExpanded(true);
			}
			updateExpandable(temperatureExpandableComposite);
		}));

		// Sample wheel
		ExpandableComposite wheelExpandableComposite = new ExpandableComposite(temperatureSampleWheelContainer, SWT.NONE);
		wheelExpandableComposite.setText("Sample Wheel");
		wheelExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		createWheel(wheelExpandableComposite);
		wheelExpandableComposite.addExpansionListener(IExpansionListener.expansionStateChangedAdapter(e -> {
			if (getBean().getSampleWheelParameters().isWheelEnabled()) {
				wheelExpandableComposite.setExpanded(true);
			}
			updateExpandable(wheelExpandableComposite);
		}));

		if (!getBean().getTemperatureControl().equals("none")) {
			temperatureExpandableComposite.setExpanded(true);
			updateEnvironmentType();
		}

		if (getBean().getSampleWheelParameters().isWheelEnabled()) {
			wheelExpandableComposite.setExpanded(true);
		}

		updateUiFromBean();

		refreshScrolledContentsSize();

		// Switch on the richbeans widgets so update events are fired
		Stream.of(name, description1, description2).forEach(widget -> {
			widget.addValueListener(this::updateBeanFromUi);
			widget.on();
		});
	}

	private void setupSampleStageColumns(Composite parentComposite) {
		// Try to locate the SampleMotorViewConfig object
		SampleMotorViewConfig motorViewConfig = Finder.findLocalSingleton(SampleMotorViewConfig.class);

		// Setup motor position controls for all the motors in sample parameters
		if (motorViewConfig == null) {
			logger.debug("Creating 'Sample parameter' motor controls for all motors in the paremeters");
			ExpandableComposite sampleStageExpandableComposite = new ExpandableComposite(parentComposite, SWT.NONE);
			sampleStageExpandableComposite.setText("Sample stages");
			sampleStageExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			motorPositionComposites = new ArrayList<>();
			motorPositionComposites.add(createSampleStage(sampleStageExpandableComposite, Collections.emptyList()));
			return;
		}

		// Setup motor position controls groups according to SampleMotorViewConfig settings ...

		// Collect the configuration settings for each column
		Map<Integer, List<ConfigDetails>> groupedConfigs = new LinkedHashMap<>();
		for(var config : motorViewConfig.getConfigurations()) {
			groupedConfigs.computeIfAbsent(config.getColumnNumber(), k -> new ArrayList<ConfigDetails>()).add(config);
		}

		logger.debug("Creating 'Sample parameter' motor controls using config from {}", motorViewConfig.getName());
		motorPositionComposites = new ArrayList<>();
		for(var configList : groupedConfigs.entrySet()) {
			logger.debug("Creating Sample parameter GUI column {}", configList.getKey());

			// Composite to contain all the controls to go in the column
			Composite container = new Composite(parentComposite, SWT.NONE);
			container.setLayout(new GridLayout(1, false));
			container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

			// Add the motor controls - each set of controls goes in its own expandable composite.
			for(var config : configList.getValue()) {
				logger.debug("Adding control group '{}' to column : {}", config.getGroupName(), config.getScannableNames());
				ExpandableComposite sampleStageExpandableComposite = new ExpandableComposite(container, SWT.NONE);
				sampleStageExpandableComposite.setText(config.getGroupName());
				sampleStageExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

				motorPositionComposites.add(createSampleStage(sampleStageExpandableComposite, config.getScannableNames()));
			}
		}
	}

	/**
	 *  This is called when one of the widgets in motorPositionComposite is changed
	 */
	@Override
	public void valueChangePerformed(ValueEvent event) {
		super.valueChangePerformed(event);
		updateBeanFromUi(event);
	}

	private void updateBeanFromUi(ValueEvent e) {
		if (guiUpdateInProgress) {
			return;
		}
		B18SampleParameters params = getBean();
		params.setName(name.getValue().toString());
		params.setDescription1(description1.getValue().toString());
		params.setDescription2(description2.getValue().toString());

		// update sample parameter motor settings from the GUI
		for(var comp : motorPositionComposites) {
			comp.getValue();
		}

		// Temperature control
		params.setTemperatureControl(sampleEnvironment.getValue().toString());

		// Furnace
		params.setFurnaceParameters(furnaceParameters.getParameterBean());

		// Pulse tube
		params.setPulseTubeCryostatParameters(pulseTubeCryostatParameters.getParameterBean());

		// Lakeshore
		params.setLakeshoreParameters(lakeshoreComposite.getParameterBean());

		// Sample wheel
		params.setSampleWheelParameters(sampleWheelParametersComposite.getParameterBean());

		beanChanged();
	}

	private volatile boolean guiUpdateInProgress = false;

	private void updateUiFromBean() {
		if (guiUpdateInProgress) {
			return;
		}
		try {
			guiUpdateInProgress = true;

			B18SampleParameters params = getBean();
			name.setValue(params.getName());
			description1.setValue(params.getDescription1());
			description2.setValue(params.getDescription2());

			sampleEnvironment.setValue(params.getTemperatureControl());

			lakeshoreComposite.setupUiFromBean(params.getLakeshoreParameters());
			pulseTubeCryostatParameters.setupUiFromBean(params.getPulseTubeCryostatParameters());
			furnaceParameters.setupUiFromBean(params.getFurnaceParameters());
			sampleWheelParametersComposite.setupUiFromBean(params.getSampleWheelParameters());

		} finally {
			guiUpdateInProgress = false;
		}
	}

	/**
	 * Update the expansion state of the sample stage positions composite
	 * and redraw it.
	 * It is automatically set to be expanded if any of the motors are selected to be moved.
	 */
	private void updateSampleStageExpansion(ExpandableComposite sampleStageExpandable, List<String> filterList) {
		// Make sure list of composite is expanded if any motors are set to be moved
		getBean().getSampleParameterMotorPositions()
			.stream()
			.filter(SampleParameterMotorPosition::getDoMove)
			.filter(mot -> filterList.isEmpty() || filterList.contains(mot.getScannableName()))
			.findFirst()
			.ifPresent(p -> sampleStageExpandable.setExpanded(true));

		updateExpandable(sampleStageExpandable);
	}

	private void updateExpandable(ExpandableComposite expComposite) {
		GridUtils.layoutFull(expComposite);
		refreshScrolledContentsSize();

		// Try to set focus on first element in the expandable composite (the twistie toggle at top of composite)
		Control[] childWidgets = expComposite.getChildren();
		if (childWidgets != null && childWidgets.length > 0) {
			logger.debug("Trying to set focus to {}. Has focus = {}", expComposite.getText(), childWidgets[0].setFocus());
		}
	}

	private void refreshScrolledContentsSize() {
		Control content = scrolledComposite.getContent();
		scrolledComposite.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}



	/**
	 * New sample stage selection widget to allow multiple stages to be selected and used in a scan.
	 * @since 27/4/2016
	 */
	private SampleParameterMotorPositionsComposite createSampleStage(ExpandableComposite parentExpandable, List<String> filterList) {
		Composite stageComp = new Composite(parentExpandable, SWT.NONE);
		stageComp.setLayout(new GridLayout());

		Group grpStage = new Group(stageComp, SWT.NONE);
		grpStage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));;
		grpStage.setLayout(new GridLayout());

		List<SampleParameterMotorPosition> motorPositions = getBean().getSampleParameterMotorPositions();

		SampleParameterMotorPositionsComposite	motorPositionComposite = new SampleParameterMotorPositionsComposite(grpStage, motorPositions);
		motorPositionComposite.setParentEditor(this);
		motorPositionComposite.setFilterList(filterList);
		motorPositionComposite.makeComposite();

		parentExpandable.addExpansionListener( IExpansionListener.expansionStateChangedAdapter(e ->
			updateSampleStageExpansion(parentExpandable, filterList) ));

		updateSampleStageExpansion(parentExpandable, filterList);

		parentExpandable.setClient(stageComp);

		return motorPositionComposite;
	}

	@SuppressWarnings("unused")
	public void createTemp(ExpandableComposite parentExpandable) {
		Composite tempComp = new Composite(parentExpandable, SWT.NONE);
		tempComp.setLayout(new GridLayout(3, false));

		Group grpBeanComposite = new Group(tempComp, SWT.NONE);
		grpBeanComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		grpBeanComposite.setLayout(new GridLayout(2, false));

		Label label = new Label(grpBeanComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("temperatureControl");
		sampleEnvironment = new ComboWrapper(grpBeanComposite, SWT.READ_ONLY);
		sampleEnvironment.setItems(new String[] { "none", "pulsetubecryostat", "furnace", "lakeshore" });
		sampleEnvironment.select(0);
		sampleEnvironment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		sampleEnvironment.addValueListener(e -> updateEnvironmentType());

		Group grpEnvironmentParameters = new Group(grpBeanComposite, SWT.NONE);
		environmentLayout = new StackLayout();
		grpEnvironmentParameters.setLayout(environmentLayout);
		grpEnvironmentParameters.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));

		blankEnvironmentComposite = new Composite(grpEnvironmentParameters, SWT.NONE);
		pulseTubeCryostatParameters = new B18PulseTubeCryostatComposite(grpEnvironmentParameters, SWT.NONE);
		pulseTubeCryostatParameters.setVisible(false);
		pulseTubeCryostatParameters.setEditorClass(PulseTubeCryostatParameters.class);
		pulseTubeCryostatParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);

		new Label(pulseTubeCryostatParameters, SWT.NONE);
		new Label(pulseTubeCryostatParameters.getSetPoint(), SWT.NONE);
		new Label(pulseTubeCryostatParameters.getTolerance(), SWT.NONE);
		new Label(pulseTubeCryostatParameters.getTime(), SWT.NONE);

		furnaceParameters = new B18FurnaceComposite(grpEnvironmentParameters, SWT.NONE);
		furnaceParameters.setVisible(false);
		furnaceParameters.setEditorClass(FurnaceParameters.class);
		furnaceParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);

		new Label(furnaceParameters, SWT.NONE);
		new Label(furnaceParameters.getTolerance(), SWT.NONE);
		new Label(furnaceParameters.getTime(), SWT.NONE);

		lakeshoreComposite = new LakeshoreComposite(grpEnvironmentParameters, SWT.NONE);
		lakeshoreComposite.setVisible(false);
		lakeshoreComposite.setEditorClass(LakeshoreParameters.class);
		lakeshoreComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);

		new Label(lakeshoreComposite, SWT.NONE);
		new Label(lakeshoreComposite.getTolerance(), SWT.NONE);
		new Label(lakeshoreComposite.getTime(), SWT.NONE);

		parentExpandable.setClient(tempComp);

		//Add value listener an activate the widgets
		Stream.of(sampleEnvironment, pulseTubeCryostatParameters, furnaceParameters, lakeshoreComposite)
			.forEach(widget -> {
				widget.addValueListener(this::updateBeanFromUi);
				widget.on();
			});
	}

	public void createWheel(ExpandableComposite parentExpandable) {
		Composite wheelComp = new Composite(parentExpandable, SWT.NONE);
		wheelComp.setLayout(new GridLayout(4, false));

		Group grpSampleWheel = new Group(wheelComp, SWT.NONE);
		grpSampleWheel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		grpSampleWheel.setLayout(new GridLayout(2, false));

		sampleWheelParametersComposite = new SampleWheelParametersComposite(grpSampleWheel, SWT.NONE, getBean());
		sampleWheelParametersComposite.setBounds(10, 29, 400, 230);
		sampleWheelParametersComposite.setEditorClass(SampleWheelParameters.class);
		sampleWheelParametersComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);
		sampleWheelParametersComposite.layout();

		sampleWheelParametersComposite.addValueListener(this::updateBeanFromUi);
		sampleWheelParametersComposite.on();

		parentExpandable.setClient(wheelComp);
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		scrolledComposite.dispose();
		createPartControl(parent);
	}

	private void updateEnvironmentType() {
		switch (sampleEnvironment.getSelectionIndex()) {
		case 0:
			environmentLayout.topControl = blankEnvironmentComposite;
			break;
		case 1:
			environmentLayout.topControl = pulseTubeCryostatParameters;
			break;
		case 2:
			environmentLayout.topControl = furnaceParameters;
			break;
		case 3:
			environmentLayout.topControl = lakeshoreComposite;
			break;
		}
		GridUtils.layoutFull(environmentLayout.topControl.getParent());
	}

	@Override
	public void setFocus() {
	}
}
