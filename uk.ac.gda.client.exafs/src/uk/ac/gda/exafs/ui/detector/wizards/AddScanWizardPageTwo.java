package uk.ac.gda.exafs.ui.detector.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
import java.util.List;

import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentBeanDescription;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.components.cell.IXMLFileListProvider;

public class AddScanWizardPageTwo extends WizardPage {

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

	protected AddScanWizardPageTwo() {
		super("Choose scan files");
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			scanType = ((AddScanWizardPageOne) getWizard().getStartingPage()).getScanType();
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
		sampleFiles.select(samples.length - 1);

		lblChooseDetector = new Label(selectFilesArea, 0);
		lblChooseDetector.setText("Detector file");
		detectorFiles = new Combo(selectFilesArea, 0);
		GridData gd_detectorFiles = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_detectorFiles.widthHint = 297;
		detectorFiles.setLayoutData(gd_detectorFiles);
		detectorFiles.setItems(detectors);
		detectorFiles.select(detectors.length - 1);

		lblChooseOutput = new Label(selectFilesArea, 0);
		lblChooseOutput.setText("Output file");
		outputFiles = new Combo(selectFilesArea, 0);
		GridData gd_outputFiles = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_outputFiles.widthHint = 297;
		outputFiles.setLayoutData(gd_outputFiles);
		outputFiles.setItems(outputs);
		outputFiles.select(outputs.length - 1);
		
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
		Object[] detectorFileList = getEditorFiles(name);
		String[] detectors = new String[detectorFileList.length];
		for (int i = 0; i < detectors.length; i++) {
			detectors[i] = detectorFileList[i].toString()
					.substring(detectorFileList[i].toString().lastIndexOf("/") + 1);
		}
		return detectors;
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

			if (editorType.equals("qexafs"))
				if (type.getName().equals("QEXAFS Scan"))
					beanTypes.add(type);

			if (editorType.equals("xanes"))
				if (type.getName().equals("XANES Scan"))
					beanTypes.add(type);

			if (editorType.equals("detector"))
				if (type.getName().equals("Detector"))
					beanTypes.add(type);

			if (editorType.equals("sample"))
				if (type.getName().equals("B18 Sample"))
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
