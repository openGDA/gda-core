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

import gda.configuration.properties.LocalProperties;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
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

	public FluorescenceComposite(Composite parent, int style, boolean includeVortex, boolean includeGermanium, boolean includeXspress3,
			DetectorParameters abean, final IBeanController control) {
		super(parent, style, abean);
		this.control = control;
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

		String[] items = new String[]{};
		if (includeVortex){
			items = (String[]) ArrayUtils.add(items, "Silicon");
		}
		if (includeGermanium){
			items = (String[]) ArrayUtils.add(items, "Germanium");
		}
		if (includeXspress3){
			items = (String[]) ArrayUtils.add(items, "Xspress3");
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
					configFileName.setError(false, null);
					if (BeansFactory.isBean(file, XspressParameters.class))
						detectorType.setValue("Germanium");
					else if (BeansFactory.isBean(file, VortexParameters.class))
						detectorType.setValue("Silicon");
					else if (BeansFactory.isBean(file, Xspress3Parameters.class))
						detectorType.setValue("Xspress3");
					else {
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
		configureAction = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object value = detectorType.getValue();
				if (value.equals("Germanium")) {
					try {
						// NOTE Currently editing local file.
						checkConfigFile("Germanium");
					} catch (Exception e1) {
						logger.error("Cannot open xspress parameters.", e1);
					}
				} else if (value.equals("Silicon")) {
					try {
						// NOTE Currently editing local file.
						checkConfigFile("Silicon");
					} catch (Exception e1) {
						logger.error("Cannot open vortex parameters.", e1);
					}
				} else if (value.equals("Xspress3")) {
					try {
						// NOTE Currently editing local file.
						checkConfigFile("Xspress3");
					} catch (Exception e1) {
						logger.error("Cannot open Xspress3 parameters.", e1);
					}
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

						if (value.equals("Silicon")) {
							configFileName.setText("Vortex_Parameters" + element + "_" + edge + ".xml");
						} else if (value.equals("Germanium")) {
							configFileName.setText("Xspress_Parameters" + element + "_" + edge + ".xml");
						} else if (value.equals("Xspress3")) {
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

	public void checkConfigFile(String detectorType) throws ClassNotFoundException{
		final IFolder dir = ExperimentFactory.getExperimentEditorManager().getSelectedFolder();
		IFile configFile = dir.getFile(configFileName.getText());
		final XMLCommandHandler xmlCommandHandler;

		if (detectorType.equals("Germanium"))
			xmlCommandHandler = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XspressParameters.class);
		else if (detectorType.equals("Silicon"))
			xmlCommandHandler = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(VortexParameters.class);
		else if (detectorType.equals("Xspress3"))
			xmlCommandHandler = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(Xspress3Parameters.class);
		else
			throw new ClassNotFoundException("XmlCommandHandler not found");
		IFile returnFromTemplate = null;
		if (!configFile.exists()) {
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
