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

	private String energyName;
	private List<IScanModelWrapper<IAxialModel>> outerScannables;

	public PolarisationSubmitScanSection(String energyName) {
		this.energyName = energyName;
	}

	@Override
	protected void submitScan() {
		try {
			// create before script and set it in the bean
			var scriptFiles = createBeforeScript();
			getBean().setScriptFiles(scriptFiles);
			// get energy model
			var energyModel = getEnergyModel();
			if (energyModel.isPresent()) {
				submitScan(energyModel.get());
			} else {
				super.submitScan();
			}
		} catch (Exception e) {
			logger.error("Could not submit scan", e);
		}
	}

	private void submitScan(IAxialModel energyModel) throws ScanningException {
		if (energyModel instanceof AxialArrayModel model) {
			var energyValues = DoubleStream.of(model.getPositions()).boxed().toList();
			submitEnergyScan(energyValues);
		} else if (energyModel instanceof AxialStepModel model) {
			var energyValues = getEnergyValuesFromStepModel(model);
			submitEnergyScan(energyValues);
		} else if (energyModel instanceof AxialMultiStepModel model) {
			model.getModels().stream()
				.map(this::getEnergyValuesFromStepModel)
				.forEach(this::submitEnergyScan);
		} else {
			throw new ScanningException("Could not submit scan with this energy model");
		}
	}

	private void submitEnergyScan(List<Double> energyValues) {
		// get current OuterScannables selection and set the bean's OuterScannables as empty list
		outerScannables = getBean().getScanDefinition().getOuterScannables();
		getBean().getScanDefinition().setOuterScannables(Collections.emptyList());

		// submit first scan with the first energy value and the before script set
		submitScan(energyValues.get(0));
		// set empty before script
		getBean().setScriptFiles(new ScriptFiles());

		// for each energy value submit a scan
		energyValues.subList(1, energyValues.size())
			.forEach(this::submitScan);

		// restore bean OuterScannables selection
		restoreMappingView();
	}


	private void submitScan(double energyValue) {
		updateBeamlineConfiguration(energyValue);
		super.submitScan();
	}

	private void updateBeamlineConfiguration(double energyValue) {
		getBean().setBeamlineConfiguration(Map.of(energyName, energyValue));
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

	private Optional<IAxialModel> getEnergyModel(){
		return getBean().getScanDefinition().getOuterScannables().stream()
				.filter(model -> model.getName().equals(energyName) && model.isIncludeInScan() && model.getModel()!= null)
				.map(IScanModelWrapper::getModel)
				.findFirst();
	}

	private void restoreMappingView() {
		getBean().getScanDefinition().setOuterScannables(outerScannables);
		getBean().setBeamlineConfiguration(null);
	}

	private void updateScriptFiles() {
		var scriptFiles = new ScriptFiles();
		getBean().setScriptFiles(scriptFiles);
	}

	protected ScriptFiles createBeforeScript() throws Exception {
		final IMarshallerService marshallerService = getService(IMarshallerService.class);
		final PolarisationSection section = getView().getSection(PolarisationSection.class);

		var scriptFiles = new ScriptFiles();
		scriptFiles.setBeforeScanScript(section.getScriptFilePath());

		var environment = Map.of(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(section.getScanParameters()));
		scriptFiles.setEnvironment(environment);

		return scriptFiles;
	}

	private void selectOuterScannable(String scannableName, boolean select) {
		final MappingExperimentView mappingView = getView();
		final OuterScannablesSection outerScannablesSection = getView().getSection(OuterScannablesSection.class);
		if (outerScannablesSection == null) {
			logger.error("OuterScannablesSection not found");
			return;
		}
		outerScannablesSection.showScannable(scannableName, select);
		mappingView.updateControls();
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

	protected void updateView(boolean onShow) {
		setSectionVisibility(PolarisationSection.class, onShow);

		updateScriptFiles();
		setSectionVisibility(ScriptFilesSection.class, !onShow);

		selectOuterScannable(energyName, false);

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



}
