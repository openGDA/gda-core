/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentBeanDescription;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.components.cell.IXMLFileListProvider;

public class SwitchScanWizardPageTwo extends WizardPage {

	Combo scanFiles;
	Combo sampleFiles;
	Combo detectorFiles;
	Combo outputFiles;

	Label lblChooseParameters;
	Label lblChooseScan;
	Label lblChooseSample;
	Label lblChooseDetector;
	Label lblChooseOutput;

	String scanType;

	IFile newScanFile;
	IFile newSampleFile;
	IFile newDetectorFile;
	IFile newOutputFile;

	ScanObject selected;
	IExperimentEditorManager controller;

	private Map<String, IExperimentBeanDescription> ACTIONS;

	protected SwitchScanWizardPageTwo() {
		super("Choose scan files");
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			scanType = ((SwitchScanWizardPageOne) getWizard().getStartingPage()).getScanType();
			String[] scans = getFileList(scanType.toLowerCase());
			scanFiles.setItems(scans);
			scanFiles.select(scans.length - 1);
			updateScan();
			updateDetector();
			updateSample();
			updateOutput();
			scanFiles.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					updateScan();
				}
			});
		}
	}

	@Override
	public void createControl(Composite parent) {

		this.setTitle("Please select the parameter files for your scan.");

		Composite selectFilesArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(selectFilesArea);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(selectFilesArea);

		String[] samples = getFileList("sample");
		String[] detectors = getFileList("detector");
		String[] outputs = getFileList("output");
		selectFilesArea.setLayout(new GridLayout(2, false));

		lblChooseScan = new Label(selectFilesArea, 0);
		lblChooseScan.setText("Scan file");
		scanFiles = new Combo(selectFilesArea, 0);
		GridData gd_scanFiles = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_scanFiles.widthHint = 297;
		scanFiles.setLayoutData(gd_scanFiles);

		lblChooseSample = new Label(selectFilesArea, 0);
		lblChooseSample.setText("Sample file");
		sampleFiles = new Combo(selectFilesArea, 0);
		GridData gd_sampleFiles = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sampleFiles.widthHint = 297;
		sampleFiles.setLayoutData(gd_sampleFiles);
		sampleFiles.setItems(samples);

		String current = selected.getSampleFileName();
		for (int i = 0; i < samples.length; i++)
			if (samples[i].equals(current))
				sampleFiles.select(i);

		lblChooseDetector = new Label(selectFilesArea, 0);
		lblChooseDetector.setText("Detector file");
		detectorFiles = new Combo(selectFilesArea, 0);
		GridData gd_detectorFiles = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_detectorFiles.widthHint = 297;
		detectorFiles.setLayoutData(gd_detectorFiles);
		detectorFiles.setItems(detectors);

		current = selected.getDetectorFileName();
		for (int i = 0; i < detectors.length; i++)
			if (detectors[i].equals(current))
				detectorFiles.select(i);

		lblChooseOutput = new Label(selectFilesArea, 0);
		lblChooseOutput.setText("Output file");
		outputFiles = new Combo(selectFilesArea, 0);
		GridData gd_outputFiles = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_outputFiles.widthHint = 297;
		outputFiles.setLayoutData(gd_outputFiles);
		outputFiles.setItems(outputs);

		current = selected.getOutputFileName();
		for (int i = 0; i < outputs.length; i++)
			if (outputs[i].equals(current))
				outputFiles.select(i);

		sampleFiles.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSample();
			}
		});

		detectorFiles.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDetector();
			}
		});

		outputFiles.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateOutput();
			}
		});

		setPageComplete(true);
		setErrorMessage(null);
		setMessage(null);
		setControl(selectFilesArea);
	}

	private void updateScan() {
			Object[] detectorFileList = getEditorFiles(scanType.toLowerCase());
			Object file = detectorFileList[scanFiles.getSelectionIndex()];
			if (file instanceof String) {
				final String fileType = (String) file;
				file = ACTIONS.get(fileType).getXmlCommandHander()
						.doCopy(selected.getFolder());
			}
			newScanFile = (IFile) file;
	}

	private void updateSample() {
		Object[] sampleFileList = getEditorFiles("sample");
		Object file = sampleFileList[sampleFiles.getSelectionIndex()];
		if (file instanceof String) {
			final String fileType = (String) file;
			file = ACTIONS.get(fileType).getXmlCommandHander()
					.doCopy(selected.getFolder());
		}
		newSampleFile = (IFile) file;
	}

	private void updateDetector() {
		Object[] detectorFileList = getEditorFiles("detector");
		Object file = detectorFileList[detectorFiles.getSelectionIndex()];
		if (file instanceof String) {
			final String fileType = (String) file;
			file = ACTIONS.get(fileType).getXmlCommandHander()
					.doCopy(selected.getFolder());
		}
		newDetectorFile = (IFile) file;
	}

	private void updateOutput() {
		Object[] outputFileList = getEditorFiles("output");
		Object file = outputFileList[outputFiles.getSelectionIndex()];
		if (file instanceof String) {
			final String fileType = (String) file;
			file = ACTIONS.get(fileType).getXmlCommandHander()
					.doCopy(selected.getFolder());
		}
		newOutputFile = (IFile) file;
	}

	private String[] getFileList(String name) {
		Object[] fileList = getEditorFiles(name);
		String[] files = new String[fileList.length];
		for (int i = 0; i < files.length; i++) {
			files[i] = fileList[i].toString()
					.substring(fileList[i].toString().lastIndexOf("/") + 1);
		}
		return files;
	}

	private Object[] getEditorFiles(String type) {

		controller = ExperimentFactory.getExperimentEditorManager();
		IFolder expFolder = controller.getSelectedMultiScan().getContainingFolder();
		@SuppressWarnings("unused")
		IResource[] folderMembers = null;
		try {
			folderMembers = expFolder.members();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		selected = (ScanObject) controller.getSelectedScan();

		IFolder currentDirectory = selected.getFolder();

		List<Object> objects = new ArrayList<Object>();
		if (getScanBeanTypes(type) != null) {
			for (IXMLFileListProvider fileListProvider : getScanBeanTypes(type)) {
				objects.addAll(fileListProvider.getSortedFileList(currentDirectory));
			}
		}

		ACTIONS = new LinkedHashMap<String, IExperimentBeanDescription>();
		if (getScanBeanTypes(type) != null) {
			for (IExperimentBeanDescription desc : getScanBeanTypes(type)) {
				ACTIONS.put("<New " + desc.getName() + ">", desc);
			}
		}

		objects.addAll(ACTIONS.keySet());
		return objects.toArray();
	}

	private Collection<IExperimentBeanDescription> getScanBeanTypes(String editorType) {

		List<IExperimentBeanDescription> beanTypes = new ArrayList<IExperimentBeanDescription>();
		List<IExperimentBeanDescription> allBeanTypes = ExperimentBeanManager.INSTANCE.getBeanDescriptions();
		for (IExperimentBeanDescription type : allBeanTypes) {

			if (editorType.equals("xas"))
				if (type.getName().equals("XAS Scan"))
					beanTypes.add(type);

			if (editorType.equals("xes"))
				if (type.getName().equals("XES Scan"))
					beanTypes.add(type);

			if (editorType.equals("qexafs"))
				if (type.getName().equals("QEXAFS Scan"))
					beanTypes.add(type);

			if (editorType.equals("xanes"))
				if (type.getName().equals("XANES Scan"))
					beanTypes.add(type);

			if (editorType.equals("microfocus"))
				if (type.getName().equals("Micro Focus"))
					beanTypes.add(type);

			if (editorType.equals("detector"))
				if (type.getName().equals("Detector"))
					beanTypes.add(type);

			if (editorType.equals("sample"))
				if (type.getName().equals("B18 Sample") || type.getName().equals("I18 Sample") || type.getName().equals("I20 Sample") )
					beanTypes.add(type);

			if (editorType.equals("output"))
				if (type.getName().equals("Output"))
					beanTypes.add(type);

		}
		return beanTypes;
	}

	public IFile getNewScanFile() {
		return newScanFile;
	}

	public IFile getNewSampleFile() {
		return newSampleFile;
	}

	public IFile getNewDetectorFile() {
		return newDetectorFile;
	}

	public IFile getNewOutputFile() {
		return newOutputFile;
	}

	public ScanObject getSelected() {
		return selected;
	}

	public IExperimentEditorManager getController() {
		return controller;
	}
}
