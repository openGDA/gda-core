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

package uk.ac.gda.exafs.ui.composites;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.file.FileBox;
import org.eclipse.richbeans.widgets.file.FileBox.ChoiceType;
import org.eclipse.richbeans.widgets.wrappers.RadioWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorGroupTemplateConfiguration;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.i20.MedipixParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.io.FileUtils;

public class FluorescenceComposite extends WorkingEnergyWithIonChambersComposite {
	private final static Logger logger = LoggerFactory.getLogger(FluorescenceComposite.class);
	private RadioWrapper detectorType;
	private Link configure;
	private SelectionAdapter configureAction;
	private FileBox configFileName;
	private File editorFolder;
	private boolean checkTemplate = LocalProperties.check("gda.microfocus.checkInTemplate");
	private boolean fileNameChangeRequired = false;
	private boolean autoChangeFluorescenceFile = LocalProperties.check("gda.microfocus.exafs.autoChangeFluorescenceFile");
	private IBeanController control;
	private Map<String,String> detectorNameToParamTypeMap;
	private Map<String,Class> detectorNameToClassMap;
	private DetectorParameters detectorParameters;
	private DetectorGroupTemplateConfiguration detectorConfigInformation = null;

	public String[] getDetectorTypesFromConfig() {
		if (detectorConfigInformation != null && detectorConfigInformation.getDetectorGroupsMap().size() > 0) {
			String expType = detectorParameters.getExperimentType();
			List<String> detList = detectorConfigInformation.getDetectorGroupsMap().get(expType);
			if (detList != null) {
				logger.info("Using {} detector list from spring config : {}", expType, ArrayUtils.toString(detList));
				return detList.toArray(new String[0]);
			}
		}
		return null;
	}

	public String[] getDetectorTypes(boolean includeVortex, boolean includeGermanium, boolean includeXspress3, boolean includeMedipix) {
		String[] items = new String[]{};
		if (includeVortex){
			items = (String[]) ArrayUtils.add(items, FluorescenceParameters.SILICON_DET_TYPE);
		}
		if (includeGermanium){
			items = (String[]) ArrayUtils.add(items, FluorescenceParameters.GERMANIUM_DET_TYPE);
		}
		if (includeXspress3){
			items = (String[]) ArrayUtils.add(items, FluorescenceParameters.XSPRESS3_DET_TYPE);
		}
		if (includeMedipix) {
			items = (String[]) ArrayUtils.add(items, FluorescenceParameters.MEDIPIX_DET_TYPE);
		}
		return items;
	}

	public FluorescenceComposite(Composite parent, int style, boolean includeVortex, boolean includeGermanium, boolean includeXspress3, boolean includeMedipix,
			DetectorParameters abean, final IBeanController control) {
		super(parent, style, abean);
		this.control = control;
		detectorParameters = abean;
		setLayout(new GridLayout());
		Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);
		Label detectorTypeLabel = new Label(top, SWT.NONE);
		detectorTypeLabel.setLayoutData(new GridData());
		detectorTypeLabel.setText("Detector Type");
		Composite confComp = new Composite(top, SWT.NONE);
		confComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(confComp);

		detectorConfigInformation = Finder.getInstance().findNoWarn(DetectorGroupTemplateConfiguration.NAME);

		String[] items = getDetectorTypesFromConfig();
		if (items==null) {
			items = getDetectorTypes(includeVortex, includeGermanium, includeXspress3, includeMedipix);
		}

		this.detectorType = new RadioWrapper(confComp, SWT.NONE, items);
		detectorType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		detectorType.addValueListener(new ValueAdapter("Detector Type Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				setFileNameChangeRequired(true);
				updateFileName(e.getValue());
			}
		});


		final Label configurationFileNameLabel = new Label(confComp, SWT.NONE);
		configurationFileNameLabel.setText("Configuration file:");

		configFileName = new FileBox(confComp, SWT.NONE);
		configFileName.setChoiceType(ChoiceType.NAME_ONLY);
		if(autoChangeFluorescenceFile)
			configFileName.setFolder(new File(LocalProperties.getConfigDir() + File.separator+ "templates"));
		configFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		configFileName.addValueListener(new ValueAdapter("Test detector file name") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				final String name = (String) e.getValue();
				if (name.isEmpty())
					return;
				final File file = new File(configFileName.getFolder(), name);
				if (!file.exists())
					return;
				try {
					String detTypeString = "";
					configFileName.setError(false, null);
					if (BeansFactory.isBean(file, XspressParameters.class))
						detTypeString = FluorescenceParameters.GERMANIUM_DET_TYPE;
					else if (BeansFactory.isBean(file, VortexParameters.class))
						detTypeString = FluorescenceParameters.SILICON_DET_TYPE;
					else if (BeansFactory.isBean(file, Xspress3Parameters.class))
						detTypeString =  FluorescenceParameters.XSPRESS3_DET_TYPE;
					else if (BeansFactory.isBean(file, MedipixParameters.class))
						detTypeString = FluorescenceParameters.MEDIPIX_DET_TYPE;

					if (StringUtils.isEmpty(detTypeString)) {
						configFileName.setError(true, "File chosen is not of a detector type.");
						detectorType.clear();
					}
					if (file.getParent().equals(editorFolder))
						configFileName.setError(true, "Please choose a detector file in the same folder.");
				} catch (Exception e1) {
					logger.error("Cannot get bean type of '" + file.getName() + "'.", e1);
				}
			}
		});

		configure = new Link(confComp, SWT.NONE);
		configure.setText("<a>Configure</a>");
		configure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		setupDetectorNameMaps();

		configureAction = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String detectorNameString =  (String) detectorType.getValue();
				String paramTypeString = detectorNameToParamTypeMap.get(detectorNameString);
				try {
					// NOTE Currently editing local file.
					checkConfigFile(detectorNameString);
				} catch (Exception e1) {
					logger.error("Cannot open "+paramTypeString+" parameters.", e1);

					MessageDialog.open(MessageDialog.INFORMATION, getShell(), "Detector configuration problem",
						"Problem creating default values to use for detector \'"+detectorNameString+"\'\n"+
						"Check log panel for more information.", SWT.NONE);

				}
			}
		};
		configure.addSelectionListener(configureAction);

		try {
			// TODO FIXME Should not be using controller in UI really!
			control.addBeanFieldValueListener("Element", new ValueAdapter("FluorElementListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					setFileNameChangeRequired(true);
					updateFileName();
				}
			});
			control.addBeanFieldValueListener("Edge", new ValueAdapter("FluorEdgeListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					setFileNameChangeRequired(true);
					updateFileName();
				}
			});
		} catch (Exception ne) {
			logger.error("Cannot add EdgeEnergy listeners.", ne);
		}

		createDiffractionSection(top);
		if (!ExafsActivator.getDefault().getPreferenceStore().getDefaultBoolean(ExafsPreferenceConstants.HIDE_WORKING_ENERGY)) {
			createEdgeEnergy(top, control);
		}
		createIonChamberSection(abean, control);
	}

	/**
	 * Setup maps between detector names and corresponding parameter type and java class.
	 *
	 * @since 18/5/2016
	 */
	private void setupDetectorNameMaps() {
		detectorNameToParamTypeMap = new HashMap<String, String>();
		detectorNameToParamTypeMap.put(FluorescenceParameters.GERMANIUM_DET_TYPE, "Xspress");
		detectorNameToParamTypeMap.put(FluorescenceParameters.SILICON_DET_TYPE, "Vortex");
		detectorNameToParamTypeMap.put(FluorescenceParameters.XSPRESS3_DET_TYPE, "Xspress3");
		detectorNameToParamTypeMap.put(FluorescenceParameters.MEDIPIX_DET_TYPE, "Medipix");

		detectorNameToClassMap = new HashMap<String, Class>();
		detectorNameToClassMap.put(FluorescenceParameters.GERMANIUM_DET_TYPE, XspressParameters.class);
		detectorNameToClassMap.put(FluorescenceParameters.SILICON_DET_TYPE, VortexParameters.class);
		detectorNameToClassMap.put(FluorescenceParameters.XSPRESS3_DET_TYPE, Xspress3Parameters.class);
		detectorNameToClassMap.put(FluorescenceParameters.MEDIPIX_DET_TYPE, MedipixParameters.class);
	}

	@Override
	public void dispose() {
		if (configure != null && !configure.isDisposed())
			configure.removeSelectionListener(this.configureAction);
		super.dispose();
	}

	public RadioWrapper getDetectorType() {
		return detectorType;
	}

	public FileBox getConfigFileName() {
		return configFileName;
	}

	/**
	 * Tells the file chooser widget to update folder.
	 *
	 * @param editorFolder
	 */
	public void setCurrentFolder(final File editorFolder) {
		this.editorFolder = editorFolder;
		if(autoChangeFluorescenceFile)
			configFileName.setFolder(new File(LocalProperties.getConfigDir() + File.separator+ "templates"));
		else
			configFileName.setFolder(editorFolder);
	}

	public void updateFileName() {
		if (!configFileName.isDisposed()) {
			final String fileNameValue = configFileName.getText();
			if (fileNameValue == null || fileNameValue.isEmpty() || isFileNameChangeRequired()) {
				updateFileName(getDetectorType().getValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateFileName(Object value) {
		if (!this.isVisible())
			return;
		if (value == null) {
			configFileName.setText("");
			return;
		}
		if (autoChangeFluorescenceFile) {
			try {
				final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
				if (ob != null) {
					final String element, edge;
					final Object params = ob.getScanParameters();
					if ((params instanceof XasScanParameters) || params instanceof XanesScanParameters) {

						if (params instanceof XasScanParameters) {
							element = control.getBeanField("Element", XasScanParameters.class).getValue().toString();
							edge = control.getBeanField("Edge", XasScanParameters.class).getValue().toString();
						} else {
							element = control.getBeanField("Element", XanesScanParameters.class).getValue().toString();
							edge = control.getBeanField("Edge", XanesScanParameters.class).getValue().toString();
						}

						if (value.equals(FluorescenceParameters.SILICON_DET_TYPE)) {
							configFileName.setText("Vortex_Parameters" + element + "_" + edge + ".xml");
						} else if (value.equals(FluorescenceParameters.GERMANIUM_DET_TYPE)) {
							configFileName.setText("Xspress_Parameters" + element + "_" + edge + ".xml");
						} else if (value.equals(FluorescenceParameters.XSPRESS3_DET_TYPE)) {
							configFileName.setText("Xspress3_Parameters" + element + "_" + edge + ".xml");
						}
					} else {
						configFileName.setText("");
					}
					setFileNameChangeRequired(false);
				}
			} catch (Exception ex) {
				logger.error("Cannot auto change the Fluorescence file name.", ex);
				setFileNameChangeRequired(true);
				configFileName.setText("");
			}
		} else {
			configFileName.setText("");
		}
	}

	public void setFileNameChangeRequired(boolean fileNameChangeRequired) {
		this.fileNameChangeRequired = fileNameChangeRequired;
	}

	public boolean isFileNameChangeRequired() {
		return fileNameChangeRequired;
	}

	/**
	 * Lookup the name of a detector configuration file to use for fluorescence detector in named detector group,
	 * by using current instance of {@link DetectorGroupTemplateConfiguration}, as configured using
	 * client-side spring.
	 * @param detectorGroup
	 * @return Name of detector configuration file (full path). Empty string if no filename could be set.
	 */
	public String getDetectorTemplateFileName(String detectorGroup) {
		if (detectorConfigInformation != null && detectorConfigInformation.getDetectorTemplateMap().size() > 0) {

			// Get list of detectors in currently selected detector group
			String[] detList = new String[0];
			for(DetectorGroup g : detectorParameters.getDetectorGroups()) {
				if (g.getName().equals(detectorGroup)) {
					detList = g.getDetector();
					break;
				}
			}
			// Lookup name of the detector template settings file to use for detector in selected group
			// (there should only be one match per group, only for fluorescence detector.)
			Map<String, String> templateMap = detectorConfigInformation.getDetectorTemplateMap();
			String configName = "";
			for( String detName : detList) { // detname typically: xmapMca, xspress3system, xspress3 ...
				if (templateMap.containsKey(detName)) {
					configName = templateMap.get(detName);
				}
			}
			if (StringUtils.isEmpty(configName)) {
				logger.warn("No Spring configuration found for detectors in group {}", detectorGroup);
			}
			return configName;
		} else {
			return "";
		}
	}

	/**
	 * Make a copy of the template configuration file.
	 * The copied file is given a unique name by using {@link FileUtils#getUnique(File, String, String)}.
	 * @param templateFilename
	 * @param outputDirname directory to put the new file in
	 * @return Name of the newly generated file
	 */
	public String copyConfigFromTemplate(String templateFilename, String outputDirname) {

			File templateFileFullpath= new File(templateFilename); // full path to file
			if (!templateFileFullpath.exists()){
				logger.warn("Could not find default settings file \'{}\' for detector.", templateFileFullpath);
				return "";
			}

			File outputFolder = new File(outputDirname);
			String filenameTemplate = templateFileFullpath.getName().replace(".xml", "");
			File outputFile = FileUtils.getUnique(outputFolder, filenameTemplate, "xml");

			try {
				logger.info("Copying detector parameters file from {} to {}", templateFilename, outputFile.getAbsolutePath());
				FileUtils.copy(templateFileFullpath, outputFile);
			} catch (IOException e) {
				logger.error("Problem creating detector configuration file {} from template file {}",  outputFile.getAbsolutePath(), templateFileFullpath.getAbsolutePath(), e);
			}
			return outputFile.getName();
	}

	public void checkConfigFile(String detectorType) throws ClassNotFoundException{
		final IFolder dir = ExperimentFactory.getExperimentEditorManager().getSelectedFolder();

		IFile configFile = dir.getFile(configFileName.getText());

		// Try to create new file using information stored in detector template configuration.
		if (!configFile.exists() && detectorConfigInformation!=null) {
			// If no template is created here, attempt to use old XmlCommandHandler method to create new config file
			String templateFilename = getDetectorTemplateFileName(detectorType);
			if (!templateFilename.isEmpty()) {
				String newConfigName = copyConfigFromTemplate(templateFilename, dir.getLocation().toString());
				// Update gui and resource tree to make eclipse aware of the newly created file
				if (!StringUtils.isEmpty(newConfigName)) {
					configFileName.setText(newConfigName);
					try {
						dir.refreshLocal(IResource.DEPTH_ONE, null);
						configFile = dir.getFile(newConfigName);
					} catch (CoreException e) {
						logger.warn("Problem updating resource tree", e);
					}
				}
			} else {
				logger.info("");
			}
		}

		configFile = dir.getFile(configFileName.getText());

		if (!configFile.exists()) {
			// Lookup xml handler for detector type (both class and handler will be null if detector type is not found)
			Class detectorClassType = detectorNameToClassMap.get(detectorType);
			final XMLCommandHandler  xmlCommandHandler = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(detectorClassType);
			IFile returnFromTemplate = null;

			if ( xmlCommandHandler == null )
				throw new ClassNotFoundException("XmlCommandHandler not found");
			if (checkTemplate && !configFileName.getText().isEmpty())
				returnFromTemplate = xmlCommandHandler.doTemplateCopy(dir, configFileName.getText());
			if (returnFromTemplate == null) {
				String newName = xmlCommandHandler.doCopy(dir).getName();
				configFileName.setText(newName);
			}
		}
		configFile = dir.getFile(configFileName.getText());
		if (configFile.exists())
			ExperimentFactory.getExperimentEditorManager().openEditor(configFile);
	}

}
