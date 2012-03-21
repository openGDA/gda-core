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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.layout.GridLayoutFactory;
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

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.file.FileBox;
import uk.ac.gda.richbeans.components.file.FileBox.ChoiceType;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.RadioWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

public class FluorescenceComposite extends WorkingEnergyWithIonChambersComposite {

	private final static Logger logger = LoggerFactory.getLogger(FluorescenceComposite.class);

	private RadioWrapper detectorType;
	private BooleanWrapper collectDiffractionImages;
	private ScaleBox mythenEnergy;
	private ScaleBox mythenTime;
	private Link configure;

	private SelectionAdapter configureAction;

	private FileBox configFileName;
	private File editorFolder;
	private boolean checkTemplate = LocalProperties.check("gda.microfocus.checkInTemplate");
	private boolean fileNameChangeRequired = false;
	private boolean autoChangeFluorescenceFile = LocalProperties
			.check("gda.microfocus.exafs.autoChangeFluorescenceFile");

	public FluorescenceComposite(Composite parent, int style, boolean includeGermanium, DetectorParameters abean) {

		super(parent, style, abean);
		setLayout(new GridLayout());
		final Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);

		final Label detectorTypeLabel = new Label(top, SWT.NONE);
		detectorTypeLabel.setLayoutData(new GridData());
		detectorTypeLabel.setText("Detector Type");
		
		final Composite confComp = new Composite(top, SWT.NONE);
		confComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(confComp);

		
		String[] items;
		if (includeGermanium) {
			items = new String[] { "Silicon", "Germanium" };
		} else {
			items = new String[] { "Silicon" };
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
				final File file = new File(configFileName.getFolder(), name);
				if (!file.exists())
					return;
				try {
					configFileName.setError(false, null);
					if (BeansFactory.isBean(file, XspressParameters.class)) {
						detectorType.setValue("Germanium");
					} else if (BeansFactory.isBean(file, VortexParameters.class)) {
						detectorType.setValue("Silicon");
					} else {
						configFileName.setError(true, "File chosen is not of a detector type.");
						detectorType.clear();
					}
					if (file.getParent().equals(editorFolder)) {
						configFileName.setError(true, "Please choose a detector file in the same folder.");
					}
				} catch (Exception e1) {
					logger.error("Cannot get bean type of '" + file.getName() + "'.", e1);
				}
			}
		});

		this.configure = new Link(confComp, SWT.NONE);
		configure.setText("<a>Configure</a>");
		configure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		this.configureAction = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object value = detectorType.getValue();
				if (value.equals("Germanium")) {
					try {
						// NOTE Currently editing local file.
						final IFolder dir = ExperimentFactory.getExperimentEditorManager().getSelectedFolder();
						final IFile xspressFile = dir.getFile(configFileName.getText());
						IFile returnFromTemplate = null;
						if (!xspressFile.exists()) {
							if (checkTemplate) {
								returnFromTemplate = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(
										XspressParameters.class).doTemplateCopy(dir, configFileName.getText());
							}
							if (returnFromTemplate == null) {
								String newName = ExperimentBeanManager.INSTANCE
										.getXmlCommandHandler(XspressParameters.class).doCopy(dir).getName();
								configFileName.setText(newName);
							}
						}
						if (xspressFile.exists()) {
							ExperimentFactory.getExperimentEditorManager().openEditor(xspressFile);
						}
					} catch (Exception e1) {
						logger.error("Cannot open xspress parameters.", e1);
					}
				} else if (value.equals("Silicon")) {
					try {
						// NOTE Currently editing local file.
						final IFolder dir = ExperimentFactory.getExperimentEditorManager().getSelectedFolder();
						IFile vortexFile = dir.getFile(configFileName.getText());
						IFile returnFromTemplate = null;
						if (!vortexFile.exists()) {
							if (checkTemplate) {
								returnFromTemplate = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(
										VortexParameters.class).doTemplateCopy(dir, configFileName.getText());
							}
							if (returnFromTemplate == null) {
								String newName = ExperimentBeanManager.INSTANCE
										.getXmlCommandHandler(VortexParameters.class).doCopy(dir).getName();
								configFileName.setText(newName);
							}
						}
						vortexFile = dir.getFile(configFileName.getText());
						if (vortexFile.exists()) {
							ExperimentFactory.getExperimentEditorManager().openEditor(vortexFile);
						}
					} catch (Exception e1) {
						logger.error("Cannot open vortex parameters.", e1);
					}
				}

			}
		};
		configure.addSelectionListener(configureAction);	

		try {
			BeanUI.addBeanFieldValueListener(XasScanParameters.class, "Element", new ValueAdapter(
					"FluorElementListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					setFileNameChangeRequired(true);
					updateFileName();
				}
			});
			BeanUI.addBeanFieldValueListener(XasScanParameters.class, "Edge", new ValueAdapter("FluorEdgeListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					setFileNameChangeRequired(true);
					updateFileName();
				}
			});
			BeanUI.addBeanFieldValueListener(XanesScanParameters.class, "Element", new ValueAdapter(
					"FluorElementListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					setFileNameChangeRequired(true);
					updateFileName();
				}
			});
			BeanUI.addBeanFieldValueListener(XanesScanParameters.class, "Edge", new ValueAdapter("FluorEdgeListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					setFileNameChangeRequired(true);
					updateFileName();
				}
			});
		} catch (Exception ne) {
			logger.error("Cannot add EdgeEnergy listeners.", ne);
		}
		
		if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.SHOW_MYTHEN)) {

			final Label collectDiffImagesLabel = new Label(top, SWT.NONE);
			collectDiffImagesLabel.setText("Diffraction Images");

			final Composite diffractionComp = new Composite(top, SWT.NONE);
			diffractionComp.setLayout(new GridLayout(5, true));

			collectDiffractionImages = new BooleanWrapper(diffractionComp, SWT.NONE);
			collectDiffractionImages.setToolTipText("Collect diffraction data at the start and end of scans");
			collectDiffractionImages.setText("Collect");

			final Label mythenEnergyLabel = new Label(diffractionComp, SWT.NONE);
			mythenEnergyLabel.setText("     Energy");
			mythenEnergy = new ScaleBox(diffractionComp, SWT.NONE);
			mythenEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			mythenEnergy.setMaximum(20000.0);
			mythenEnergy.setUnit("eV");

			final Label mythenTimeLabel = new Label(diffractionComp, SWT.NONE);
			mythenTimeLabel.setText("     Time");
			mythenTime = new ScaleBox(diffractionComp, SWT.NONE);
			mythenTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			collectDiffractionImages.addValueListener(new ValueListener() {

				@Override
				public String getValueListenerName() {
					return null;
				}

				@Override
				public void valueChangePerformed(ValueEvent e) {
					mythenEnergy.setVisible(collectDiffractionImages.getValue());
					mythenTime.setVisible(collectDiffractionImages.getValue());
					mythenEnergyLabel.setVisible(collectDiffractionImages.getValue());
					mythenTimeLabel.setVisible(collectDiffractionImages.getValue());
				}
			});
		}

		if (!ExafsActivator.getDefault().getPreferenceStore()
				.getDefaultBoolean(ExafsPreferenceConstants.HIDE_WORKING_ENERGY)) {
			createEdgeEnergy(top);
			createIonChamberSection(abean);
		}
	}

	@Override
	public void dispose() {
		if (configure != null && !configure.isDisposed()) {
			configure.removeSelectionListener(this.configureAction);
		}
		super.dispose();
	}

	public RadioWrapper getDetectorType() {
		return detectorType;
	}

	public FileBox getConfigFileName() {
		return configFileName;
	}

	/**
	 * Return whether or not to collect diffraction images
	 * 
	 * @return true if we should collect them, else false
	 */
	public BooleanWrapper getCollectDiffractionImages() {
		return collectDiffractionImages;
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

	public ScaleBox getMythenEnergy() {
		return mythenEnergy;
	}

	public ScaleBox getMythenTime() {
		return mythenTime;
	}

	public void updateFileName() {
		if (!configFileName.isDisposed()) {
			final Object fileNameValue = configFileName.getText();
			if (fileNameValue == null || "".equals(fileNameValue) || isFileNameChangeRequired()) {
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
							element = BeanUI.getBeanField("Element", XasScanParameters.class).getValue().toString();
							edge = BeanUI.getBeanField("Edge", XasScanParameters.class).getValue().toString();
						} else {
							element = BeanUI.getBeanField("Element", XanesScanParameters.class).getValue().toString();
							edge = BeanUI.getBeanField("Edge", XanesScanParameters.class).getValue().toString();
						}
						if (value.equals("Silicon")) {
							configFileName.setText("Vortex_Parameters" + element + "_" + edge + ".xml");
						} else if (value.equals("Germanium")) {
							configFileName.setText("Xspress_Parameters" + element + "_" + edge + ".xml");
						}
					} else {
						if (value.equals("Silicon")) {
							configFileName.setText("Vortex_Parameters.xml");
						} else if (value.equals("Germanium")) {
							configFileName.setText("Xspress_Parameters.xml");
						}
					}
					setFileNameChangeRequired(false);
				}
			} catch (Exception ex) {
				logger.error("Cannot auto change the Fluorescence file name.", ex);
				setFileNameChangeRequired(true);
				if (value.equals("Silicon")) {
					configFileName.setText("Vortex_Parameters.xml");
				} else if (value.equals("Germanium")) {
					configFileName.setText("Xspress_Parameters.xml");
				}
			}
		} else {
			if (value.equals("Silicon")) {
				configFileName.setText("Vortex_Parameters.xml");
			} else if (value.equals("Germanium")) {
				configFileName.setText("Xspress_Parameters.xml");
			}
		}
	}

	public void setFileNameChangeRequired(boolean fileNameChangeRequired) {
		this.fileNameChangeRequired = fileNameChangeRequired;
	}

	public boolean isFileNameChangeRequired() {
		return fileNameChangeRequired;
	}

}
