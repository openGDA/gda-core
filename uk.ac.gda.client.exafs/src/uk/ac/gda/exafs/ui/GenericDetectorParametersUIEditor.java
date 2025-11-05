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

package uk.ac.gda.exafs.ui;

import static java.lang.Boolean.TRUE;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorGroupTemplateConfiguration;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public class GenericDetectorParametersUIEditor extends FauxRichBeansEditor<DetectorParameters> {

	private static final Logger logger = LoggerFactory.getLogger(GenericDetectorParametersUIEditor.class);

	private ScrolledComposite scrolledComposite;
	private List<DetectorConfig> detectorConfigs;
	private DetectorGroupTemplateConfiguration templateConfiguration;
	private static final GridDataFactory DESCRIPTION_GRID_DATA = GridDataFactory.swtDefaults().hint(180, SWT.DEFAULT);
	private static final GridDataFactory SCRIPTNAME_GRID_DATA = GridDataFactory.swtDefaults().hint(450, SWT.DEFAULT);

	private String currentDirectory = ""; // directory of the scan, detector, output, sample xmls currently being edited

	public GenericDetectorParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, DetectorParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		detectorConfigs = editingBean.getDetectorConfigurations();
	}

	private Composite parent;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite detectorParametersComposite = new Composite(scrolledComposite, SWT.FILL);
		GridLayoutFactory.fillDefaults().applyTo(detectorParametersComposite);

		createDetectorDetectorSection(detectorParametersComposite);

		scrolledComposite.setContent(detectorParametersComposite);
		scrolledComposite.setMinSize(detectorParametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Force full layout (required when called by linkUI)
		GridUtils.layoutFull(parent);

		currentDirectory = ExperimentFactory.getExperimentEditorManager().getSelectedFolder().getLocation().toString();

		if (templateConfiguration == null) {
			templateConfiguration = Finder.findSingleton(DetectorGroupTemplateConfiguration.class);
		}
	}

	private void createDetectorDetectorSection(Composite parent) {
		Composite comp = new Composite(parent, SWT.FILL);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(comp);
		for (DetectorConfig detectorConfig : detectorConfigs) {
			Composite widgetComp = new Composite(parent, SWT.FILL);
			GridLayoutFactory.fillDefaults().numColumns(getNumWidgets(detectorConfig)).applyTo(widgetComp);
			addDetectorControls(widgetComp, detectorConfig);
		}
	}

	@Override
	public void linkUI(boolean tf) {
		// update GUI from bean when it has been changed.
		scrolledComposite.dispose();
		detectorConfigs = getBean().getDetectorConfigurations();
		createPartControl(parent);
	}

	/**
	 * Number of GUI widgets required for a particular detector config
	 * @param detectorConfig
	 * @return
	 */
	private int getNumWidgets(DetectorConfig detectorConfig) {
		int numColumnsRequired = 2;
		if (TRUE.equals(detectorConfig.isUseConfigFile())) {
			numColumnsRequired += 3;
		}
		if (TRUE.equals(detectorConfig.isUseScriptCommand())) {
			numColumnsRequired += 2;
		}
		return numColumnsRequired;
	}

	private void addDetectorControls(Composite parent, DetectorConfig detectorConfig) {

		Label descriptionLabel = new Label(parent, SWT.FILL | SWT.RIGHT);
		descriptionLabel.setText(StringUtils.defaultIfEmpty(detectorConfig.getDescription(), detectorConfig.getDetectorName()));
		descriptionLabel.setToolTipText(detectorConfig.getDetectorName());
		DESCRIPTION_GRID_DATA.applyTo(descriptionLabel);

		Button useInScanCheckbox = new Button(parent, SWT.CHECK);
		if (TRUE.equals(detectorConfig.getAlwaysUseDetectorInScan())) {
			useInScanCheckbox.setToolTipText("This detector is always included in the scan");
			// Make sure checkbox is always ticked
			useInScanCheckbox.setSelection(true);
			useInScanCheckbox.addListener(SWT.Selection, event -> useInScanCheckbox.setSelection(true));
		} else {
			useInScanCheckbox.setToolTipText("Select to include the detector in the scan");
			useInScanCheckbox.setSelection(detectorConfig.isUseDetectorInScan());
			useInScanCheckbox.addListener(SWT.Selection, event -> {
				detectorConfig.setUseDetectorInScan(useInScanCheckbox.getSelection());
				beanChanged();
			});
		}

		if (TRUE.equals(detectorConfig.isUseConfigFile())) {
			// add textbox with name of xml file, 'browse for file' button and 'configure' button
			addConfigfileControls(parent, detectorConfig);
		}

		if (TRUE.equals(detectorConfig.isUseScriptCommand())) {
			// add textbox for entering name of script/command, browse button
			addScriptCommandControls(parent, detectorConfig);
		}
	}

	/**
	 * Add controls for setting the detector config file : textbox with name of xml file, 'browse for file' button and 'configure' button
	 * @param parent
	 * @param detectorConfig
	 */
	private void addConfigfileControls(Composite parent, DetectorConfig detectorConfig) {

		DetectorConfigFileControls detFileControls = new DetectorConfigFileControls();
		detFileControls.addControls(parent, detectorConfig);
		detFileControls.setCurrentDirectory(currentDirectory);
		detFileControls.setTemplateConfiguration(templateConfiguration);

		// Add listeners to update the detector config name in the model if texbox content changes
		addTextboxListeners(detectorConfig::setConfigFileName, detectorConfig::getConfigFileName, detFileControls.getFilenameTextbox());
	}

	/**
	 * Add textbox for setting the Jython script file name/Jython command and a 'browse for script' button
	 * @param parent
	 * @param detectorConfig
	 */
	private void addScriptCommandControls(Composite parent, DetectorConfig detectorConfig) {
		Text filenameCommandTextbox = new Text(parent, SWT.FILL);
		filenameCommandTextbox.setText(StringUtils.defaultString(detectorConfig.getScriptCommand()));
		filenameCommandTextbox.setToolTipText("Jython command/name of Jython script to run before configuring the detector");
		SCRIPTNAME_GRID_DATA.applyTo(filenameCommandTextbox);

		addTextboxListeners(detectorConfig::setScriptCommand, detectorConfig::getScriptCommand, filenameCommandTextbox);

		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setText("Select script file...");
		browseButton.addListener(SWT.Selection, event -> {
			FileDialog dialog = OutputParametersUIEditor.getJythonScriptFileBrowser();
			String filename = dialog.open();
			if (filename != null) {
				// update the textbox; listeners take care of updating the model
				filenameCommandTextbox.setText(filename);
				alignTextboxContents(filenameCommandTextbox);
			}
		});
	}

	/**
	 * Add listeners to a textbox to update a model when the Textbox content changes (i.e. checks current content on modify, focus lost, enter key pressed events to see if model needs updating).
	 * @param setter - a consumer that will be used to set a value in the model from latest value from widget.
	 * @param getter - supplier to retrieve value from the model
	 * @param textbox user input widget
	 */
	private void addTextboxListeners(Consumer<String> setter, Supplier<String> getter, Text textbox) {

		Consumer<FocusEvent> updater = event -> {
			logger.debug("Textbox update : new value = {}", textbox.getText());
			if (StringUtils.defaultString(getter.get()).equals(textbox.getText())) {
				logger.debug("Text not modified");
			} else {
				logger.debug("Text content modified");
				setter.accept(textbox.getText());
				beanChanged();
			}
		};

		textbox.addListener(SWT.Modify, e -> updater.accept(null));

		textbox.addFocusListener(FocusListener.focusLostAdapter(updater));

		textbox.addListener(SWT.Traverse, event -> {
			if (event.detail == SWT.TRAVERSE_RETURN) {
				updater.accept(null);
			}
		});
	}

	public void setDetectorTemplateConfiguration(DetectorGroupTemplateConfiguration templateConfiguration) {
		this.templateConfiguration = templateConfiguration;
	}

	private void alignTextboxContents(Text textbox) {
		textbox.setSelection(textbox.getText().length());
	}

	@Override
	protected String getRichEditorTabText() {
		return "Detector Parameters";
	}

	@Override
	public void setFocus() {
		scrolledComposite.setFocus();
	}

	@Override
	public void dispose() {
		scrolledComposite.dispose();
	}
}
