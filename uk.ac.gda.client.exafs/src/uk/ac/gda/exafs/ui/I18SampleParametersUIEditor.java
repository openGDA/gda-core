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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.gda.beans.exafs.ScannableConfiguration;
import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.client.widgets.BeamlineConfigurationControls;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public class I18SampleParametersUIEditor extends FauxRichBeansEditor<I18SampleParameters> {

	private static final Logger logger = LoggerFactory.getLogger(I18SampleParametersUIEditor.class);

	public I18SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, I18SampleParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	private ScrolledComposite scrolledComposite;

	@Override
	public void createPartControl(Composite parent) {

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		var sampleParametersComposite = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(sampleParametersComposite);

		scrolledComposite.setContent(sampleParametersComposite);

		var detailsGroup = standardGroup(sampleParametersComposite, "Sample details");
		createSampleDetailsSection(detailsGroup);

		var configurationGroup = standardGroup(sampleParametersComposite, "Beamline configuration", 400);
		createBeamlineConfigurationSection(configurationGroup);

		scrolledComposite.setMinSize(sampleParametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private Group standardGroup(Composite parent, String groupName) {
		return standardGroup(parent, groupName, SWT.DEFAULT);
	}

	private Group standardGroup(Composite parent, String groupName, int height) {
		var group = new Group(parent, SWT.NONE);
		group.setText(groupName);
		GridLayoutFactory.swtDefaults().applyTo(group);
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, height).applyTo(group);
		return group;
	}

	private void createSampleDetailsSection(Composite parent) {

		var details = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).spacing(20, 5).applyTo(details);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(details);

		// grid data for controls
		GridDataFactory rightAlign = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory stretch = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// controls
		var nameLabel = new Label(details, SWT.NONE);
		nameLabel.setText("Name");
		rightAlign.applyTo(nameLabel);

		var name = new Text(details, SWT.BORDER);
		stretch.applyTo(name);

		var descriptionLabel = new Label(details, SWT.NONE);
		descriptionLabel.setText("Description");
		rightAlign.copy().align(SWT.RIGHT, SWT.TOP).applyTo(descriptionLabel);

		var description = new Text(details, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		stretch.copy().hint(SWT.DEFAULT, 50).applyTo(description);

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

	private void createBeamlineConfigurationSection(Composite parent) {
		var configuration = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(configuration);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(configuration);

		Map<String, Object> initialConfiguration = getBeamlineConfiguration(getBean().getScannableConfigurations());

		var configurationControls = new BeamlineConfigurationControls(ScannableDeviceConnectorService.getInstance(), initialConfiguration);
		configurationControls.addDataChangedCallback(new BeamlineConfigurationUpdater(configurationControls::getBeamlineConfiguration, getBean()::setScannableConfigurations));
		configurationControls.draw(configuration);
	}

	private Map<String, Object> getBeamlineConfiguration(List<ScannableConfiguration> scannableConfigurations) {
		return scannableConfigurations.stream()
				.collect(Collectors.toMap(
						ScannableConfiguration::getScannableName,
						ScannableConfiguration::getPosition,
						(e1, e2) -> e1, LinkedHashMap::new));
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

	private class BeamlineConfigurationUpdater implements Runnable {

		private Supplier<Map<String, Object>> beamlineConfigurationSupplier;
		private Consumer<List<ScannableConfiguration>> configurationConsumer;

		public BeamlineConfigurationUpdater(Supplier<Map<String, Object>> beamlineConfigurationSupplier, Consumer<List<ScannableConfiguration>> configurationConsumer) {
			this.beamlineConfigurationSupplier = beamlineConfigurationSupplier;
			this.configurationConsumer = configurationConsumer;
		}

		@Override
		public void run() {
			configurationConsumer.accept(convert(beamlineConfigurationSupplier.get()));
			beanChanged();
		}

		private List<ScannableConfiguration> convert(Map<String, Object> beamlineConfiguration) {
			return beamlineConfiguration.entrySet().stream()
				.map(e -> new ScannableConfiguration(e.getKey(), e.getValue().toString()))
				.collect(Collectors.toList());
		}
	}
}
