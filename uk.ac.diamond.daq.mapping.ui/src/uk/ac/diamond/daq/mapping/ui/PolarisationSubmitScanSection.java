/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_CUSTOM_PARAMS;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.PolarisationParameters.Polarisation;
import uk.ac.diamond.daq.mapping.impl.ScriptFiles;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.diamond.daq.mapping.ui.experiment.BeamlineConfigurationSection;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;
import uk.ac.diamond.daq.mapping.ui.experiment.OuterScannablesSection;
import uk.ac.diamond.daq.mapping.ui.experiment.ScriptFilesSection;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;
import uk.ac.diamond.daq.mapping.ui.xanes.PolarisationSection;

public class PolarisationSubmitScanSection extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(PolarisationSubmitScanSection.class);

	private String scriptFilePath;
	private String energyName;
	private List<IScanModelWrapper<IAxialModel>> outerScannables;

	/**
	 * Constructs a new {@link PolarisationSubmitScanSection} with the specified script file path and energy name.
	 *
	 * This constructor initialises the section with the given script file path and energy name.
	 * The energy name represents the scannable that will be moved before the scan begins,
	 * and the script file path points to the location of the script that should be executed before the scan starts.
	 *
	 * @param scriptFilePath the file path to the script that should be executed before the scan
	 * @param energyName the name of the energy parameter for the scan
	 */
	public PolarisationSubmitScanSection(String scriptFilePath, String energyName) {
		this.scriptFilePath = scriptFilePath;
		this.energyName = energyName;
	}

	/**
	 * Submits the scan by first preparing the necessary script and energy model.
	 *
	 * This method is called when the user clicks the "Submit Scan" button after defining the scan in the mapping view.
	 *
	 * This method creates a "before scan" script and sets it in the bean. Then, it retrieves the energy model associated
	 * with the scan and prepares the energy values format before submitting the scan. If there isn't an energy model,
	 * it submits the scan as it is.
	 */
	@Override
	protected void submitScan() {
		try {
			// create before script and sets it in the bean
			var scriptFiles = createBeforeScanScript();
			getBean().setScriptFiles(scriptFiles);

			// get energy model
			var energyModel = getEnergyModel();

			if (energyModel.isPresent()) {
				processEnergyModelForScan(energyModel.get());
			} else {
				super.submitScan();
			}
		} catch (Exception e) {
			logger.error("Could not submit scan", e);
		}
	}

	/**
	 * Submits the scan for the specified energy model.
	 * Depending on the type of energy model, this method retrieves the positions that the energy object will move to.
	 *
	 * 	{@link AxialArrayModel}: Represents a single array of energy positions. The scan is submitted with these positions directly.
	 *  {@link AxialStepModel}: Defines a range of energy values using a start, stop, and step.
	 *  	The method called generates all the intermediate positions between start and stop based on the step size,
	 *  {@link AxialMultiStepModel}: It contains multiple {@link AxialStepModel} models.
	 *   	For each contained model, the scan is prepared independently by repeating the process used for {@link AxialStepModel}.

	 * If the provided energy model is not one of the expected types, a {@link ScanningException} is thrown.
	 *
	 * @param energyModel the energy model to be used for the scan
	 * @throws ScanningException if the energy model is unsupported or an error occurs during scan submission.
	 */
	private void processEnergyModelForScan(IAxialModel energyModel) throws ScanningException {
		switch (energyModel) {
			case AxialArrayModel model -> {
				var energyValues = DoubleStream.of(model.getPositions()).boxed().toList();
				prepareEnergyScan(energyValues);
			}
		    case AxialStepModel model -> {
		    	var energyValues = getEnergyValuesFromStepModel(model);
		    	prepareEnergyScan(energyValues);
		  	}
		    case AxialMultiStepModel model ->
		    	model.getModels().stream()
		    		.map(this::getEnergyValuesFromStepModel)
		    		.forEach(this::prepareEnergyScan);
		    default -> throw new ScanningException("Could not submit scan with this energy model");
	    }}

	/**
	 * Prepares and submits a series of energy scans for the provided list of energy values.
	 *
	 * Firstly, it saves the current selection of outer scannables and clears the list.
	 * This prevents that the energy scannable is included as an outer scannable in the scan itself.
	 * As it will be later added to the beamline configuration to move before the scan starts.
	 *
	 * The "before scan" script file, which was set earlier, is only required for the first scan. After submitting the
	 * first scan with the first energy value, the script files are reset to an empty {@link ScriptFiles} object to
	 * prevent the "before scan" script from running in subsequent scans.

	 * After the first scan, the method proceeds to submit a scan for each subsequent energy value in the list.

	 * Finally, the method restores the original outer scannables selection after the scans have been submitted.
	 *
	 * @param energyValues a list of energy values
	 */
	private void prepareEnergyScan(List<Double> energyValues) {
		// get current OuterScannables selection and set the bean's OuterScannables as empty list
		outerScannables = getBean().getScanDefinition().getOuterScannables();
		getBean().getScanDefinition().setOuterScannables(Collections.emptyList());

		// submit first scan with the first energy value and the before script set
		submitEnergyScan(energyValues.get(0));
		// set empty before script
		getBean().setScriptFiles(new ScriptFiles());

		// submit a scan for each energy value
		energyValues.subList(1, energyValues.size())
			.forEach(this::submitEnergyScan);

		// restore bean OuterScannables selection
		restoreMappingView();
	}


	/**
	 * Sets the beamline configuration for the scan and submits the scan with the specified energy value.
	 *
	 * This method updates the beamline configuration with the energy name (defined for this scan) and its corresponding value.
	 * Before the scan begins, the energy scannable will move to the set value.
	 *
	 * Then, method calls the superclass to submit the scan with the updated beamline configuration.
	 *
	 * @param energyValue the energy value to be used in the beamline configuration for this scan
	 */
	private void submitEnergyScan(double energyValue) {
		getBean().setBeamlineConfiguration(Map.of(energyName, energyValue));
		super.submitScan();
	}

	private List<Double> getEnergyValuesFromStepModel(AxialStepModel stepModel) {
		var start = BigDecimal.valueOf(stepModel.getStart());
		var stop = BigDecimal.valueOf(stepModel.getStop());
		var step = BigDecimal.valueOf(stepModel.getStep());

		return Stream.iterate(start,
				d -> d.compareTo(stop) <= 0,
				d -> d.add(step))
		      .mapToDouble(BigDecimal::doubleValue).boxed().toList();
		}

	/**
	 * Retrieves the energy model associated with the current scan configuration.
	 *
	 * This method searches through the list of outer scannables in the scan definition to find a model that matches
	 * the specified energy name, is marked to be included in the scan, and has a non-null associated model.
	 */
	private Optional<IAxialModel> getEnergyModel(){
		return getBean().getScanDefinition().getOuterScannables().stream()
				.filter(model -> model.getName().equals(energyName) && model.isIncludeInScan() && model.getModel()!= null)
				.map(IScanModelWrapper::getModel)
				.findFirst();
	}

	/**
	 * Reset the Mapping view the previous selected {@link OuterScannable} and a null {@link BeamlineConfiguration}
	 */
	private void restoreMappingView() {
		getBean().getScanDefinition().setOuterScannables(outerScannables);
		getBean().setBeamlineConfiguration(null);
	}

	/**
	 * Displays the specified scannable in the {@link OuterScannablesSection} but does not select it.
	 */
	private void displayOuterScannable(String scannableName) {
		final MappingExperimentView mappingView = getView();
		final OuterScannablesSection outerScannablesSection = getView().getSection(OuterScannablesSection.class);
		if (outerScannablesSection == null) {
			logger.error("OuterScannablesSection not found");
			return;
		}
		outerScannablesSection.showScannable(scannableName, false);
		mappingView.updateControls();
	}

	/**
	 * Creates and returns a {@link ScriptFiles} object that contains the "before scan" script
	 * and the associated environment parameters for the polarisation scan.
	 *
	 * The method sets the "before scan" script file based on the configured script file path. It also
	 * passes the user defined scan parameters in the {@link PolarisationSection} into the script environment.
	 *
	 * @return a {@link ScriptFiles} object containing the "before scan" script and environment parameters
	 * @throws Exception if there is an error while marshalling the scan parameters
	 */
	private ScriptFiles createBeforeScanScript() throws Exception {
		final IMarshallerService marshallerService = getService(IMarshallerService.class);
		final PolarisationSection section = getView().getSection(PolarisationSection.class);

		var scriptFiles = new ScriptFiles();
		scriptFiles.setBeforeScanScript(getScriptFilePath());

		Map<String, String> environment = new HashMap<>();
		environment.put(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(section.getScanParameters()));

		if (section.getScanParameters().getPolarisation().equals(Polarisation.LINEARDEGREES) && isDegreeValid(section)) {
			environment.put("selectedDegreeExperiment", String.valueOf(section.getSelectedDegree()));
		}

		scriptFiles.setEnvironment(environment);

		return scriptFiles;
	}

	private boolean isDegreeValid(PolarisationSection section) {
		return section.getDegreesList().contains(section.getSelectedDegree());
	}

	private void setSectionVisibility(Class<? extends AbstractHideableMappingSection> hideableSection, boolean visible) {
		final AbstractHideableMappingSection section = getView().getSection(hideableSection);
		if (section == null) {
			var errorMessage = String.format("%s not found", hideableSection.getClass().getName());
			logger.error(errorMessage);
		} else {
			section.setVisible(visible);
		}
	}

	/**
	 * Updates the visibility of various sections in the view based on the onShow flag.
	 *
	 * If onShow is true, the {@link PolarisationSection} is displayed,
	 * but {@link ScriptFilesSection} and {@link BeamlineConfigurationSection} are hidden.
	 * This is is to prevent the possibility of adding multiple scripts or a different beamline configuration to the one set
	 * in this section.
	 *
	 * Also, the script files are reset to an empty state in both cases by setting a new empty {@link ScriptFiles} object in the bean.
	 */
	protected void updateView(boolean onShow) {
		setSectionVisibility(PolarisationSection.class, onShow);

		var scriptFiles = new ScriptFiles();
		getBean().setScriptFiles(scriptFiles);
		setSectionVisibility(ScriptFilesSection.class, !onShow);

		displayOuterScannable(energyName);

		setSectionVisibility(BeamlineConfigurationSection.class, !onShow);

		relayoutView();
	}

	@Override
	protected void onShow() {
		updateView(true);
	}

	@Override
	protected void onHide() {
		updateView(false);
	}

	protected String getScriptFilePath() {
		return scriptFilePath;
	}
}
