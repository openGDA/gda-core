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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.widgets.file.FileBox;
import org.eclipse.richbeans.widgets.file.FileBox.ChoiceType;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
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

import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.util.beans.BeansFactory;

/**
 * @author Matthew Gerring
 *
 */
public class SoftXRaysComposite extends WorkingEnergyWithDrainCurrentsComposite {
	private final static Logger logger = LoggerFactory.getLogger(SoftXRaysComposite.class);
	private RadioWrapper detectorType;
	private Link configure;
	private SelectionAdapter configureAction;
	private FileBox configFileName;
	private File editorFolder;
	private Label configurationFileNameLabel;
	private BooleanWrapper collectDiffractionImages;
	private ScaleBox mythenEnergy;
	private ScaleBox mythenTime;
	private Composite diffractionComp;
	private Label collectDiffImagesLabel;

	/**
	 * @param parent
	 * @param style
	 */
	public SoftXRaysComposite(Composite parent, int style, IBeanController control) {
		super(parent, style, null);
		setLayout(new GridLayout());

		Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);

		Label detectorTypeLabel = new Label(top, SWT.NONE);
		detectorTypeLabel.setLayoutData(new GridData());
		detectorTypeLabel.setText("Detector Type");
		String[] items = new String[] { "Silicon Soft X-Rays", "Gas Microstrip" };
		detectorType = new RadioWrapper(top, SWT.NONE, items);
        detectorType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.SHOW_MYTHEN)) {

        	collectDiffImagesLabel = new Label(top, SWT.NONE);
        	collectDiffImagesLabel.setText("Diffraction Images");

			diffractionComp = new Composite(top, SWT.NONE);
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

        detectorType.addValueListener(new ValueAdapter("Detector Type Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateFileName(e.getValue());
				updateFileSelectionVisibility(e.getValue());
			}
		});

		Composite confComp = new Composite(detectorType, SWT.NONE);
		confComp.setLayout(new GridLayout());

		configure = new Link(confComp, SWT.NONE);
		configure.setText("<a>Configure</a>");
		configureAction = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object value = detectorType.getValue();
				if (value.equals("Silicon Soft X-Rays")) {
					try {
						// NOTE Currently editing local file.
						final IFolder dir      =ExperimentFactory.getExperimentEditorManager().getSelectedFolder();
						final IFile vortexFile = dir.getFile(configFileName.getText());
						if (!vortexFile.exists()) {
							String newName = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(VortexParameters.class).doCopy(dir).getName();
							configFileName.setText(newName);
						}
						ExperimentFactory.getExperimentEditorManager().openEditor(vortexFile);
					} catch (Exception e1) {
						logger.error("Cannot open vortex parameters.", e1);
					}
				}
			}
		};
		configure.addSelectionListener(configureAction);

		configurationFileNameLabel = new Label(top, SWT.NONE);
		configurationFileNameLabel.setText("Configuration File Name");

		configFileName = new FileBox(top, SWT.NONE);
		configFileName.setChoiceType(ChoiceType.NAME_ONLY);

		GridData gd_configFileName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		configFileName.setLayoutData(gd_configFileName);
		configFileName.addValueListener(new ValueAdapter("Test detector file name") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				Object value = detectorType.getValue();
				if (value.equals("Silicon Soft X-Rays")) {
					String name = (String) e.getValue();
					File file = new File(configFileName.getFolder(), name);
					if (!file.exists())
						return;
					try {
						configFileName.setError(false, null);
						if (BeansFactory.isBean(file, VortexParameters.class))
							detectorType.setValue("Silicon Soft X-Rays");
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
			}
		});
		createEdgeEnergy(top, control);
		createDrainCurrentSection(control);
	}

	@Override
	public void dispose() {
		if (configure!=null && !configure.isDisposed())
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
	 * @param editorFolder
	 */
	public void setCurrentFolder(final File editorFolder) {
		this.editorFolder = editorFolder;
        configFileName.setFolder(editorFolder);
	}

	public void updateFileName() {
		final Object fileNameValue = configFileName.getText();
		if (fileNameValue==null||"".equals(fileNameValue)) {
			updateFileName(getDetectorType().getValue());
		}
		updateFileSelectionVisibility(getDetectorType().getValue());
	}

	private void updateFileSelectionVisibility(Object value) {
		boolean visible = "Silicon Soft X-Rays".equals(value);
		GridUtils.startMultiLayout(this);
		try {
			GridUtils.setVisibleAndLayout(configFileName, visible);
			GridUtils.setVisibleAndLayout(configurationFileNameLabel, visible);
			GridUtils.setVisibleAndLayout(configure, visible);
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	private void updateFileName(Object value) {
		if(value==null) {
			configFileName.setText("");
			return;
		}
		if (value.equals("Silicon Soft X-Rays"))
			configFileName.setText("Vortex_Parameters.xml");
		else
			configFileName.setText("");
	}

	public ScaleBox getMythenEnergy() {
		return mythenEnergy;
	}

	public ScaleBox getMythenTime() {
		return mythenTime;
	}

}