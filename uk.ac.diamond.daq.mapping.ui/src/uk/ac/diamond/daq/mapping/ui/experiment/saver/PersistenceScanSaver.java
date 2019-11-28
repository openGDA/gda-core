/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.saver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.application.persistence.data.SearchResult;
import uk.ac.diamond.daq.application.persistence.data.SearchResultRow;
import uk.ac.diamond.daq.application.persistence.service.PersistenceException;
import uk.ac.diamond.daq.client.gui.persistence.AbstractSearchResultLabelProvider;
import uk.ac.diamond.daq.client.gui.persistence.SearchResultViewDialog;
import uk.ac.diamond.daq.client.gui.persistence.SearchResultViewDialogMode;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.PersistableMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.ComplexScanNameLabelProvider;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;
import uk.ac.diamond.daq.persistence.manager.PersistenceServiceWrapper;

public class PersistenceScanSaver extends ScanSaver {
	private static final Logger log = LoggerFactory.getLogger(PersistenceScanSaver.class);

	private ScanManagementController smController;
	private PersistenceServiceWrapper persistenceServiceWrapper = null;
	private IMappingExperimentBeanProvider beanProvider = null;

	public PersistenceScanSaver (Consumer<Optional<IMappingExperimentBean>> postLoad,
			ScanManagementController smController) {
		super(postLoad);

		this.smController = smController;
	}

	public IMappingExperimentBean getMappingBean() {
		if (beanProvider == null) {
			beanProvider = PlatformUI.getWorkbench().getService(IMappingExperimentBeanProvider.class);
		}
		return beanProvider.getMappingExperimentBean();
	}

	public PersistenceServiceWrapper getPersistenceServiceWrapper() {
		if (persistenceServiceWrapper == null) {
			persistenceServiceWrapper = PlatformUI.getWorkbench().getService(PersistenceServiceWrapper.class);
		}
		return persistenceServiceWrapper;
	}

	@Override
	public SavedScanMetaData[] listScans () {
		try {
			SearchResult searchResult = getPersistenceServiceWrapper().get(PersistableMappingExperimentBean.class);
			if (!searchResult.getRows().isEmpty()) {
				SavedScanMetaData[] result = new SavedScanMetaData[searchResult.getRows().size()];
				int i = 0;
				for (SearchResultRow row : searchResult.getRows()) {
					result[i++] = new SavedScanMetaData(row.getPersistenceId(),
							row.getValue(PersistableMappingExperimentBean.SCAN_NAME_TITLE));
				}
				return result;
			}
		} catch (PersistenceException e) {
			log.error("Could not find existing scans", e);
		}
		return new SavedScanMetaData[0];
	}

	private List<AbstractSearchResultLabelProvider> getLabelProviders () {
		return Arrays.asList(
						new ComplexScanNameLabelProvider("Name",
								PersistableMappingExperimentBean.SCAN_NAME_TITLE,
								true, 0),
						new ComplexScanNameLabelProvider("Dimensions",
								PersistableMappingExperimentBean.SCAN_NAME_TITLE,
								false, 1));
	}

	@Override
	public Optional<IMappingExperimentBean> loadBean (SavedScanMetaData savedScanMetaData) {
		return smController.loadScanMappingBean(savedScanMetaData.getId());
	}

	@Override
	public void save () {
		PersistenceServiceWrapper persistenceService = PlatformUI.getWorkbench().getService(PersistenceServiceWrapper.class);
		SearchResult searchResult;
		try {
			searchResult = persistenceService.get(PersistableMappingExperimentBean.class);
		} catch (PersistenceException e1) {
			log.error("Unable to find existing Mapping Beans", e1);
			return;
		}

		SearchResultViewDialog searchDialog = new SearchResultViewDialog(Display.getCurrent().getActiveShell(),
				searchResult, "Save Scan Definition", true, true,
				PersistableMappingExperimentBean.SCAN_NAME_TITLE,
				SearchResultViewDialogMode.save, getLabelProviders());
        if (searchDialog.open() == Window.OK) {
			IMappingExperimentBean mappingBean = getMappingBean();
			if (searchDialog.getItemId() != SearchResultViewDialog.INVALID_ID) {
				mappingBean.setId(searchDialog.getItemId());
			} else if (searchDialog.getNewName() != null) {
				mappingBean.setId(SearchResultViewDialog.INVALID_ID);
				String name = smController.buildDescriptiveFilename(searchDialog.getNewName());
				mappingBean.setDisplayName(name);
			}
			smController.saveScanAs(mappingBean.getId());
        }
	}

	@Override
	public void delete (SavedScanMetaData savedScanMetaData) {
		if (savedScanMetaData != null && savedScanMetaData.getId() != SearchResultViewDialog.INVALID_ID) {
			smController.deleteScan (savedScanMetaData.getId());
		}
	}
}
