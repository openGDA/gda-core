/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import gda.exafs.scan.ScanObject;
import gda.util.JsonHelper;
import gda.util.XrayLibHelper;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.IonchamberOptimisationParams;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public class IonchamberOptimisationParamsEditor extends EditorPart {
	private static final Logger logger = LoggerFactory.getLogger(IonchamberOptimisationParamsEditor.class);
	public static final String ID = "uk.ac.gda.exafs.ui.IonchamberOptimisationParamsEditor";

	private static final GridDataFactory ENERGY_GRID_DATA = GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT);

	private boolean isDirty = false;
	private IonchamberOptimisationParams params;
	private String filePath;

	private Button btnAuto;
	private Text txtEdgeEnergy;
	private Text txtFinalEnergy;

	private boolean isAuto;
	private double edgeEnergy;
	private double finalEnergy;
	private int adjustEnergy = 50;

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (filePath == null) {
			MessageDialog.openError(getSite().getShell(), "Error", "File path is not available");
			return;
		}

		savetoJsonFile();

		isDirty = false;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
		final IFile currentiFile = EclipseUtils.getIFile(getEditorInput());
		final IFolder folder = (currentiFile != null) ? (IFolder) currentiFile.getParent() : null;

		final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setText("Save as json");
		dialog.setFilterExtensions(new String[] { "*.json" });
		final File currentFile = new File(filePath);
		dialog.setFilterPath(currentFile.getParentFile().getAbsolutePath());
		dialog.setFileName(currentFile.getName());

	    String newFile = dialog.open();

	    if (newFile != null) {

			if (!newFile.endsWith(".json")) newFile = newFile+".json";
			newFile = validateFileName(newFile);
			if (newFile==null) return;

			final File file = new File(newFile);
			if (file.exists()) { //check if file exist, overwrite or not.
				final boolean ovr = MessageDialog.openQuestion(getSite().getShell(), "Confirm File Overwrite",
						                                      "The file '"+file.getName()+"' exists in '"+file.getParentFile().getName()+"'.\n\n"+
						                                      "Would you like to overwrite it?");
				if (!ovr) return;
			}
			try { // create new file, update filePath.
				file.createNewFile();
				filePath = file.toString();

			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Cannot save file.",
						               "The file '"+file.getName()+"' cannot be created in '"+file.getParentFile().getName()+"'.\n\n"+
						               e.getMessage());
			    return;
			}

			// update editor input to new file.
	        IEditorInput input;
	        if (folder!=null&&folder.getLocation().toFile().equals(file.getParentFile())) {
	        	final IFile ifile = folder.getFile(file.getName());
	        	try {
					ifile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					logger.error("Cannot refresh "+ifile, e);
				}
	        	input = new FileEditorInput(ifile);
	        } else {
	        	input = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(file));
	        }

	        setInput(input);
	        setPartName(input.getName());

	        savetoJsonFile();

	        isDirty = false;
	        firePartPropertyChanged(RichBeanMultiPageEditorPart.FILE_NAME_CHANGE_PROPERTY, currentFile.getName(), newFile);
		}

	}

	private void savetoJsonFile() {
		params = new IonchamberOptimisationParams();
		params.setAutoControl(btnAuto.getSelection());
		setEdgeEnergy(Double.parseDouble(txtEdgeEnergy.getText()));
		setFinalEnergy(Double.parseDouble(txtFinalEnergy.getText()));
		double[] newEnergies = {getEdgeEnergy(), getFinalEnergy() };
		params.setEnergies(newEnergies);

		Gson gson = new Gson();
		try (FileWriter writer = new FileWriter(filePath)) {
			gson.toJson(params, writer);
		} catch (IOException e) {
			logger.error("Failed to save json file.", e);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			filePath = EclipseUtils.getFilePath(input);
			String jsonString = Files.readString(Paths.get(filePath));
			logger.info("Deserializing json string : {}", jsonString);

			var p = JsonHelper.createObject(jsonString, IonchamberOptimisationParams.class);

			if (p != null) {
				params = p;
			}
		} catch (IOException e) {
			logger.error("Problem loading settings from Json file", e);
		}
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite adjComp = new Composite(parent, SWT.FILL);
		GridLayoutFactory.fillDefaults().applyTo(adjComp);

		isAuto = params.isAutoControl();
		setEdgeEnergy(params.getEnergies()[0]);
		setFinalEnergy(params.getEnergies()[1]);

		createAdjustment(adjComp);
	}

	public void createAdjustment(Composite parent) {
		Composite adjComp = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout(1, false);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		layout.marginLeft = 8;
		layout.marginRight = 8;
		adjComp.setLayout(layout);
		adjComp.setLayoutData(gd);

		Group grpComp = new Group(adjComp, SWT.NONE);
		grpComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		grpComp.setLayout(new GridLayout(5, false));

		gd.horizontalSpan = 5;

		btnAuto = new Button(grpComp, SWT.CHECK);
		btnAuto.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnAuto.setLayoutData(gd);
		btnAuto.setText("auto control");
		btnAuto.setToolTipText("automatically use energy from scan: (edge-50eV, final)");
		btnAuto.setSelection(isAuto);

		btnAuto.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean selection = ((Button) event.getSource()).getSelection();
				setTextEnable(selection);
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});

		Label lbl = new Label(grpComp, SWT.NONE);
		lbl.setText("adjust_sensitivities_2E");

		txtEdgeEnergy = new Text(grpComp, SWT.BORDER);
		txtEdgeEnergy.setText(Double.toString(getEdgeEnergy()));
		ENERGY_GRID_DATA.applyTo(txtEdgeEnergy);

		txtFinalEnergy = new Text(grpComp, SWT.BORDER);
		txtFinalEnergy.setText(Double.toString(getFinalEnergy()));
		ENERGY_GRID_DATA.applyTo(txtFinalEnergy);

		setTextEnable(isAuto);

		Button btnGetValue = new Button(grpComp, SWT.PUSH);
		btnGetValue.setText("Get Energy from Qexafs");
		btnGetValue.setToolTipText("Get edge-50eV, final energy from scan");
		btnGetValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnergyFromScan();
			}
		});

		// When selected textbox changes : Update value
		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		};
		txtEdgeEnergy.addModifyListener(modifyListener);
		txtFinalEnergy.addModifyListener(modifyListener);
	}

	private void setTextEnable(boolean enable) {
		if (enable) {
			setEnergyFromScan();
			txtEdgeEnergy.setEnabled(false);
			txtFinalEnergy.setEnabled(false);
		} else {
			txtEdgeEnergy.setEnabled(true);
			txtFinalEnergy.setEnabled(true);
		}
	}

	/**
	 * Try to set the element and line from QExafs, Xas scan parameters of currently selected scan
	 */
	public void setEnergyFromScan() {
		// Return immediately if can't get required extension point (i.e. client not using Experiment explorer and xml files to setup experiments)
		try {
			ExperimentFactory.getExperimentObjectManagerClass();
		} catch (Exception e1) {
			logger.info(
					"Element and edge information not available from scan settings - unable to set element and line.");
			return;
		}

		try {
			final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
			final IScanParameters scanParams = ob.getScanParameters();

			if (scanParams instanceof QEXAFSParameters p) {
				double edgeEVal = XrayLibHelper.getEdgeEnergy(p.getElement(), p.getEdge()) - adjustEnergy;
				String finalEVal = Double.toString(p.getFinalEnergy());

				txtEdgeEnergy.setText(Double.toString(edgeEVal));
				txtFinalEnergy.setText(finalEVal);
			}
		} catch (Exception e) {
			logger.warn("Problem getting element and edge information from scan settings", e);
		}
	}

	public double getEdgeEnergy() {
		return edgeEnergy;
	}

	public void setEdgeEnergy(double edgeEnergy) {
		this.edgeEnergy = edgeEnergy;
	}

	public double getFinalEnergy() {
		return finalEnergy;
	}

	public void setFinalEnergy(double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	public int getAdjustEnergy() {
		return adjustEnergy;
	}

	public void setAdjustEnergy(int adjustEnergy) {
		this.adjustEnergy = adjustEnergy;
	}

	protected String validateFileName(final String newFile) {
		return newFile;
	}

	@Override
	public void setFocus() {
		btnAuto.setFocus();
	}
}
