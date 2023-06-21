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

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.PolarisationParameters;
import uk.ac.diamond.daq.mapping.impl.ScriptFiles;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.diamond.daq.mapping.ui.experiment.ScriptFilesSection;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;
import uk.ac.diamond.daq.mapping.ui.xanes.PolarisationSection;

/**
 * Section to set a script that will run before submitting a scan
 * The script will contain variables set in the {@link PolarisationSection}
 */
public class PolarisationSubmitScanSection extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(PolarisationSubmitScanSection.class);

	private String scriptFilePath;

	public PolarisationSubmitScanSection(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	@Override
	protected void submitScan() {
		final IMarshallerService marshallerService = getService(IMarshallerService.class);
		final PolarisationParameters parameters = getView().getSection(PolarisationSection.class).getScanParameters();

		try {
			var environment = Map.of(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(parameters));
			var scriptFiles = new ScriptFiles();
			scriptFiles.setBeforeScanScript(scriptFilePath);
			scriptFiles.setEnvironment(environment);
			getBean().setScriptFiles(scriptFiles);
			super.submitScan();
		} catch (Exception e) {
			logger.error("Could not submit scan", e);
		}
	}

	/**
	 * Display {@link PolarisationSection} and hide {@link ScriptFilesSection}
	 */
	@Override
	protected void onShow() {
		updateView(true);
	}

	/**
	 * Display {@link ScriptFilesSection} and hide {@link PolarisationSection}
	 */
	@Override
	protected void onHide() {
		updateView(false);
	}

	private void updateView(boolean onShow) {
		setSectionVisibility(PolarisationSection.class, onShow);
		updateScriptFiles();
		setSectionVisibility(ScriptFilesSection.class, !onShow);
		relayoutView();
	}

	/**
	 * Create a new empty {@link ScriptFiles} and sets it on the Mapping bean, so that
	 * other sections do not contain the script that was added when submitting a scan
	 */
	private void updateScriptFiles() {
		var scriptFiles = new ScriptFiles();
		getBean().setScriptFiles(scriptFiles);
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
}
