/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;
import gda.factory.Finder;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

public class SpreadsheetViewComposite {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetViewComposite.class);

	private Composite parent;

	// ParametersForScan = parameters for each xml file (i.e. for one scan). This is the 'model' being viewed by the table
	private volatile List<ParametersForScan> parameterValuesForScanFiles = new ArrayList<ParametersForScan>();

	private String xmlDirectoryName = "";
	private List<String> xmlFiles = new ArrayList<>();
	private Text xmlDirectoryNameText;
	private Text outputDirectoryNameText;
	private String currentTableFilename = "";

	private SpreadsheetViewTable spreadsheetTable;
	private SpreadsheetViewConfig viewConfig;
	private SpreadsheetViewConfig viewConfigForSampleParameterMotors;

	public SpreadsheetViewComposite(Composite parent) {
		this.parent = parent;
		setConfigFromSpring();
	}

	private void setConfigFromSpring() {
		List<Findable> findables = Finder.getInstance().listAllLocalObjects(Findable.class.getSimpleName());
		for(Findable findable : findables) {
			if (findable instanceof SpreadsheetViewConfig) {
				this.viewConfig = (SpreadsheetViewConfig) findable;
				break;
			}
		}
		viewConfigForSampleParameterMotors = new SpreadsheetViewConfig();
	}

	public void setXmlDirectoryName(String xmlDirectoryName) {
		this.xmlDirectoryName = xmlDirectoryName;
		xmlFiles = SpreadsheetViewHelperClasses.getListOfFilesMatchingExtension(xmlDirectoryName, ".xml");
		if (spreadsheetTable != null) {
			spreadsheetTable.setXmlDirectoryName(xmlDirectoryName);
		}
		// set the textboxes for base xml directory and output directory.
		if (xmlDirectoryNameText!=null && outputDirectoryNameText!=null) {
			xmlDirectoryNameText.setText(xmlDirectoryName);
			outputDirectoryNameText.setText(xmlDirectoryName);
		}

		// Update the XML file paths in the parameters for each scan, so they all point to the new XML base directory.
		for(ParametersForScan paramsForScan : parameterValuesForScanFiles) {
			for(ParameterValuesForBean paramForBean : paramsForScan.getParameterValuesForScanBeans()) {
				String fullFileName = paramForBean.getBeanFileName();
				String filename = FilenameUtils.getName(fullFileName);
				String newFullFilename = FilenameUtils.normalize(xmlDirectoryName+"/"+filename);
				paramForBean.setBeanFileName(newFullFilename);
			}
		}
	}

	public String getXmlDirectoryName() {
		return this.xmlDirectoryName;
	}

	private ParametersForScan getInitialParametersForTableColumns() {
		Class<?>[] classes = new Class<?>[] { QEXAFSParameters.class, DetectorParameters.class, B18SampleParameters.class, OutputParameters.class };
		ParametersForScan tempParams = new ParametersForScan();
		for(Class<?> clazz : classes) {
			String className = clazz.getName();
			tempParams.addValuesForScanBean("", className);
		}
		return tempParams.getParametersForTableColumns();
	}

	public List<ParametersForScan> getParametersForScans() {
		return parameterValuesForScanFiles;
	}

	public void showParametersForScans() {
		System.out.println("Current override parameter settings");
		for(int i=0; i<parameterValuesForScanFiles.size(); i++) {
			for (ParameterValuesForBean override : parameterValuesForScanFiles.get(i).getParameterValuesForScanBeans()) {
				System.out.println(
						String.format("%s, %s : ", override.getBeanFileName(), override.getBeanType()));
				for (ParameterValue vals : override.getParameterValues()) {
					System.out.println("\t" + vals.getFullPathToGetter() + " : " + vals.getNewValue());
				}
			}
		}
	}

	public void createTableAndControls() {

		parent.setLayout(new FillLayout());

		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		addControlButtons(comp);
		int style = SWT.BORDER | SWT.HIDE_SELECTION | SWT.FULL_SELECTION;

		spreadsheetTable = new SpreadsheetViewTable(comp, style);
		spreadsheetTable.addColumnsToTable(getInitialParametersForTableColumns().getParameterValuesForScanBeans());
		spreadsheetTable.setXmlDirectoryName(xmlDirectoryName);
		spreadsheetTable.setViewConfig(viewConfig);

		addLoadSaveControls(comp);
		addGenerateScanControls(comp);

		parameterValuesForScanFiles.clear();

		spreadsheetTable.getTableViewer().setInput(parameterValuesForScanFiles);

		spreadsheetTable.getTableViewer().getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Retrieve the model from the TableItem
				TableItem item = spreadsheetTable.getTableViewer().getTable().getSelection()[0];
				ParametersForScan model = (ParametersForScan) item.getData();
				// Invoke editing of the element
				spreadsheetTable.getTableViewer().editElement(model, 0);
			}
		});

		addNewScan();

		// Expand the column widths to fit content
		spreadsheetTable.adjustColumnWidths();
	}

	private void addControlButtons(final Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

		addXmlDirectoryControls(comp);

		Composite compForButtons = new Composite(comp, SWT.NONE);
		compForButtons.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(compForButtons);

		addScanAddRemoveContols(compForButtons);
		addParameterModifierControls(compForButtons);
	}
	/**
	 * Add gui controls to select input xml directory
	 * @param parent
	 */
	private void addXmlDirectoryControls(final Composite parent) {
		// Xml directory selection controls ...

		Composite compForRow = new Composite(parent, SWT.NONE);
		compForRow.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(compForRow);

		Composite compForXmlDirLabelAndTextBox = new Composite(compForRow, SWT.NONE);
		compForXmlDirLabelAndTextBox.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(compForXmlDirLabelAndTextBox);

		Label xmDirectoryNameLabel = new Label(compForXmlDirLabelAndTextBox, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(xmDirectoryNameLabel);
		xmDirectoryNameLabel.setText("Directory with base XML files : ");

		xmlDirectoryNameText = new Text(compForXmlDirLabelAndTextBox, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).minSize(1, SWT.DEFAULT).applyTo(xmlDirectoryNameText);
		xmlDirectoryNameText.setText(xmlDirectoryName);
		xmlDirectoryNameText.setEditable(false);
		xmlDirectoryNameText.setToolTipText("Use the 'Browse' button to select the directory containing the XML files");

		Button setXmlDirectoryButton = new Button(compForRow, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(setXmlDirectoryButton);
		setXmlDirectoryButton.setText("Browse...");

		// Display widget to select input xml directory
		setXmlDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
				dirDialog.setMessage("Select directory containing xml files to use");
				dirDialog.setFilterPath(xmlDirectoryNameText.getText());
				String result = dirDialog.open();
				if (result != null) {
					setXmlDirectoryName(result);
				}
			}
		});
	}

	/**
	 * Add gui controls for selecting parameters to be modified
	 * @param parent
	 */
	private void addParameterModifierControls(final Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// Sample param modifier button
		Button setMeasurementConditionsButton = new Button(composite, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(setMeasurementConditionsButton);
		setMeasurementConditionsButton.setText("Set measurement conditions");

		// Listeners for the buttons ...
		setMeasurementConditionsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayMeasurementConditionsDialog(parent, 2);
			}
		});
	}

	/**
	 * Add gui controls to add/remove scans and clear the table
	 * @param parent
	 */
	private void addScanAddRemoveContols(final Composite parent) {
		// Xml directory selection controls ...
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, true));

		// Add, delete scan buttons
		Button addScanButton = new Button(comp, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addScanButton);
		addScanButton.setText("Add scan");
		addScanButton.setToolTipText("Add new scan - by copying values from the last one in the table");

		Button deleteScanButton = new Button(comp, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(deleteScanButton);
		deleteScanButton.setText("Remove scan");
		deleteScanButton.setToolTipText("Remove scan in currently selected row");

		addScanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addNewScan();
			}
		});

		deleteScanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteScan();
			}
		});
	}

	/**
	 *
	 */
	private void addClearTableControls(final Composite parent) {
		Button clearTableButton = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(clearTableButton);
		clearTableButton.setText("Clear table...");
		clearTableButton.setToolTipText("Clear table, reset everyhing");

		clearTableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean clearTable = MessageDialog.openQuestion(parent.getShell(), "Clear the table", "Are you sure you want to clear the table?");
				if (clearTable) {
					clearAllScans();
				}
			}
		});
	}

	/** Update the viewConfig to add parameters for generic sample parameter motors */
	private void updateViewConfig() {

		// Fist remove the previously added generic motor params from view config
		List<ParameterConfig> configForSampleParamMotors = viewConfigForSampleParameterMotors.getParameters();
		if (configForSampleParamMotors != null) {
			viewConfig.getParameters().removeAll(configForSampleParamMotors);
		}

		// Get generic motor params from the first sample parameters xml file
		List<ParameterConfig> motorParamFromScanFile = SpreadsheetViewHelperClasses.getSampleParameterMotorConfig(parameterValuesForScanFiles.get(0));
		viewConfigForSampleParameterMotors.setParameters(motorParamFromScanFile);
		if (motorParamFromScanFile != null) {
			viewConfig.getParameters().addAll(motorParamFromScanFile);
		}

	}

	private void addLoadSaveControls(final Composite parent) {
		Group comp = new Group(parent, SWT.NONE);
		comp.setLayout(new GridLayout(3, true));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

		// Load save buttons
		Button loadFileButton = new Button(comp, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(loadFileButton);
		loadFileButton.setText("Load table from file...");
		loadFileButton.setToolTipText("Load previously saved table");
		Button saveFileButton = new Button(comp, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(saveFileButton);
		saveFileButton.setText("Save table to file...");
		saveFileButton.setToolTipText("Save the current settings in the table to a file");
		addClearTableControls(comp);

		// Load save button actions...
		loadFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = parent.getShell();
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterNames(new String[] { "xml files", "All Files (*.*)" });
				dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
				String filename = dialog.open();
				if (filename != null) {
					logger.info("Loading table from file {}", filename);
					try {
						List<ParametersForScan> newParams = ParameterCollection.loadFromFile(filename);
						currentTableFilename = filename;
						parameterValuesForScanFiles.clear();
						parameterValuesForScanFiles.addAll(newParams);

						updateXmlDirectoryFromModel();
						if (xmlFiles.size()==0) {
							String message = "No xml files were found in directory "+xmlDirectoryName+".\n"+
									"You will be able to add/remove scans, change the parameter modifiers\n"+
									"and save the table, but not change the xml files selected for each scan\n"+
									"or generate the new scan xml files";
							MessageDialog.openWarning(parent.getShell(), "Warning", message);
						}
						spreadsheetTable.removeAllColumnsFromTable();

						updateViewConfig();

						spreadsheetTable.addColumnsToTable(parameterValuesForScanFiles.get(0).getParameterValuesForScanBeans());
						spreadsheetTable.refresh();
						spreadsheetTable.adjustColumnWidths();
					} catch (IOException e1) {
						logger.error("Problem encountered loading table from XML file", e1);
					}

				}
			}
		});

		saveFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = parent.getShell();
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterNames(new String[] { "xml files", "All Files (*.*)" });
				dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
				dialog.setText("Save to file");
				// Set filename and directory based on last full path to last loaded table, if available
				if (!currentTableFilename.isEmpty()) {
					dialog.setFileName(FilenameUtils.getName(currentTableFilename));
					dialog.setFilterPath(FilenameUtils.getFullPath(currentTableFilename));
				}
				String filename = dialog.open();
				if (filename != null) {
					boolean writeFile = true;
					File file = new File(filename);
					// confirm to write file if it already exists
					if (file.exists()) {
						writeFile = MessageDialog.openQuestion(shell, "File already exists!",
								"File " + filename + " already exists.\nDo you want to overwrite it?");
					}
					if (writeFile) {
						logger.info("Write to file {}", filename);
						System.out.println(ParameterCollection.toXML(parameterValuesForScanFiles));
						// try and save file
						try {
							ParameterCollection.saveToFile(parameterValuesForScanFiles, filename);
						} catch (IOException e1) {
							logger.error("Problem encountered saving table to XML file", e1);
						}
					}
				}
			}
		});
	}

	private void addGenerateScanControls(final Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText("Generate new XML files");
		group.setLayout(new GridLayout(2, true));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

		// Add Output directory name label, textbox and browse buttons
		Composite compForDirLabelAndTextBox = new Composite(group, SWT.NONE);
		compForDirLabelAndTextBox.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(compForDirLabelAndTextBox);

		Label xmDirectoryNameLabel = new Label(compForDirLabelAndTextBox, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(xmDirectoryNameLabel);
		xmDirectoryNameLabel.setText("Output directory name : ");

		outputDirectoryNameText = new Text(compForDirLabelAndTextBox, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(outputDirectoryNameText);
		outputDirectoryNameText.setText(xmlDirectoryName);


		Composite compForButtons = new Composite(group, SWT.NONE);
		compForButtons.setLayout(new GridLayout(2, true));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(compForButtons);

		Button browseOutputDirectoryButton = new Button(compForButtons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(browseOutputDirectoryButton);
		browseOutputDirectoryButton.setText("Browse...");

		// Add export scans button
		Button generateScansButton = new Button(compForButtons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(generateScansButton);
		generateScansButton.setText("Generate scan xml files...");

		generateScansButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String outputDirectoryName = outputDirectoryNameText.getText();
				String message = "";
				boolean outputDirExists = false;
				if (parameterValuesForScanFiles.size()==0) {
					message = "No scans to generate - nothing has been set up in the table!";
				}else if (StringUtils.isEmpty(outputDirectoryName)) {
					message = "Output directory name has not been specified!";
				} else {
					File dir = new File(outputDirectoryName);
					if (!dir.isDirectory()) {
						boolean createDir = MessageDialog.openQuestion(parent.getShell(), "Output directory for files does not exist", "Output directory "+outputDirectoryName+" does not exist.\nDo you want to create it?");
						if (createDir) {
							File file = new File(outputDirectoryName);
							file.mkdir();
							outputDirExists = true;
						}
					} else {
						outputDirExists = true;
					}
				}
				if (message.length()>0) {
					MessageDialog.openError(parent.getShell(), "Error", message);
				} else if (outputDirExists){
					boolean proceed = MessageDialog.openQuestion(parent.getShell(), "Generate scan files", "Scan files will be written to directory "+outputDirectoryName+".\nDo you want to continue?");
					if (proceed) {
						message = SpreadsheetViewHelperClasses.checkRequiredXmlsExist(parameterValuesForScanFiles);
						if (message.length()==0) {
							SpreadsheetViewHelperClasses.generateNewScans(parent, parameterValuesForScanFiles, outputDirectoryNameText.getText());
						} else {
							MessageDialog.openError(parent.getShell(), "Missing input XML file(s)", "Cannot generate new XML files - some files needed to be read cannot be found :\n"+message);
						}
					}
				}
			}
		});

		// Display widget to select input xml directory
		browseOutputDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
				dirDialog.setMessage("Select directory to export xml files to");
				dirDialog.setFilterPath(outputDirectoryNameText.getText());
				String result = dirDialog.open();
				if (result != null) {
					outputDirectoryNameText.setText(result);
				}
			}
		});

	}

	/**
	 * Display dialog showing available 'measurement conditions' (i.e. scan xml parameters). Update template and scan parameters based on selection after dialog is closed.
	 * @param parent
	 * @param typeIndex
	 */
	private void displayMeasurementConditionsDialog(Composite parent, int typeIndex) {
		// Store current width of each column in table (so can restore later)...
		HashMap<String, Integer> columnWidths = new HashMap<String,Integer>();
		for(TableColumn t : spreadsheetTable.getTableViewer().getTable().getColumns() ) {
			columnWidths.put(t.getText(), t.getWidth()); // store current width of column
		}

		List<ParameterValuesForBean> paramValuesForBeans = new ArrayList<>();
		int returnCode;

		if (viewConfig != null) {
			// Display parameter selection dialog
			updateViewConfig();

			ParameterSelectionDialog paramSelectDialog = new ParameterSelectionDialog(parent.getShell());
			paramSelectDialog.setParameterConfig(viewConfig.getParameters());
			paramSelectDialog.create();
			paramSelectDialog.setFromParameters(parameterValuesForScanFiles.get(0).getParameterValuesForScanBeans());
			paramSelectDialog.setBlockOnOpen(true);
			returnCode =  paramSelectDialog.open();
			paramValuesForBeans = paramSelectDialog.getOverrides();
			SpreadsheetViewHelperClasses.addSampleParameterMotorMoveFlag(paramValuesForBeans);

		} else {
			// Display 'method tree view' dialog
			ParameterValuesForBean currentSelectedOverrides = parameterValuesForScanFiles.get(0).getParameterValuesForScanBeans().get(typeIndex);

			Class<?> classForTree = currentSelectedOverrides.getBeanClass();
			if(classForTree==null) {
				logger.error("Problem creating class with name {}", currentSelectedOverrides.getBeanType());
				return;
			}
			MethodTreeViewDialog methodTreeDialog = new MethodTreeViewDialog(parent.getShell());
			methodTreeDialog.setClassTypeForTree(classForTree); // object used to reflect on to get get/set methods.
			methodTreeDialog.setBlockOnOpen(true);
			methodTreeDialog.create();
			methodTreeDialog.setFromOverrides(currentSelectedOverrides);
			returnCode = methodTreeDialog.open();
			paramValuesForBeans.add(methodTreeDialog.getOverrideBean());
		}

		// Update selected overrides in template with new values selected from gui
		if (returnCode == Window.OK) {

			// update the model to match newly selected parameters (add new parameters, remove ones from model that haven't been selected)
			SpreadsheetViewHelperClasses.addRemoveParameters(parameterValuesForScanFiles, paramValuesForBeans);

			// Sort into alphabetical order based on parameter name
			// SpreadsheetViewHelperClasses.sortModelModifiers(parameterValuesForScanFiles);

			// remove all columns
			spreadsheetTable.removeAllColumnsFromTable();

			// Add them back in...
			spreadsheetTable.addColumnsToTable(parameterValuesForScanFiles.get(0).getParameterValuesForScanBeans());

			// Set columns with same widths as before
			for(TableColumn t : spreadsheetTable.getTableViewer().getTable().getColumns() ) {
				Integer width = columnWidths.get(t.getText()); // store current width of column
				if (width!=null && width>0) {
					t.setWidth(width);
				}
			}

			// update table columns to show selected parameters
			spreadsheetTable.refresh();
			spreadsheetTable.getTableViewer().getTable().redraw();
		}
	}

	private void clearAllScans() {
		parameterValuesForScanFiles.clear();
		spreadsheetTable.removeAllColumnsFromTable();
		spreadsheetTable.addColumnsToTable(getInitialParametersForTableColumns().getParameterValuesForScanBeans());
		spreadsheetTable.refresh();
	}

	private void addNewScan() {
		ParametersForScan parametersToCopyFrom;
		if (parameterValuesForScanFiles.size() > 0) {
			// Use last override if there is one
			parametersToCopyFrom = parameterValuesForScanFiles.get(parameterValuesForScanFiles.size() - 1);
		} else {
			// use template
			parametersToCopyFrom = getInitialParametersForTableColumns();
		}

		// loop over params for each file and copy them
		ParametersForScan parametersForNewScan = new ParametersForScan();
		parametersForNewScan.setNumberOfRepetitions(parametersToCopyFrom.getNumberOfRepetitions());
		for (ParameterValuesForBean paramToCopyFrom : parametersToCopyFrom.getParameterValuesForScanBeans()) {
			ParameterValuesForBean param = new ParameterValuesForBean();
			param.copyFrom(paramToCopyFrom);

			// For the first scan, also set path to the xml file
			if (parameterValuesForScanFiles.size() == 0) {
				List<String> filesForBean = SpreadsheetViewHelperClasses.getListOfFilesMatchingType(xmlFiles, param.getBeanType());
				if (filesForBean.size() > 0) {
					param.setBeanFileName(filesForBean.get(0));
				}
			}
			parametersForNewScan.addValuesForScanBean(param);
		}

		parameterValuesForScanFiles.add(parametersForNewScan);

		spreadsheetTable.refresh();
	}

	private void deleteScan() {
		TableItem[] selectedItems = spreadsheetTable.getTableViewer().getTable().getSelection();
		if (selectedItems == null || selectedItems.length == 0) {
			return; // nothing selected
		}
		TableItem item = selectedItems[0];
		ParametersForScan selectedScan = (ParametersForScan) item.getData();

		// Remove selected scan from model list
		Iterator<ParametersForScan> iter = parameterValuesForScanFiles.iterator();
		while (iter.hasNext()) {
			ParametersForScan overrides = iter.next();
			if (overrides == selectedScan) {
				iter.remove();
			}
		}
		// Update viewer
		spreadsheetTable.refresh();
	}

	/**
	 * Update list of available xml files from current model; update the directory name in gui
	 */
	public void updateXmlDirectoryFromModel() {
		boolean allDirectoriesMatch = true;
		String lastDirName = "";

		// Extract name of base directory where the xml files are located; check that they all match
		int scanCount = 1;
		for(ParametersForScan parametersForScan : parameterValuesForScanFiles) {
			for(ParameterValuesForBean overrideForFile : parametersForScan.getParameterValuesForScanBeans() ) {
				String fileName = overrideForFile.getBeanFileName();
				String dirName = FilenameUtils.getFullPath(fileName);

				if (lastDirName.length() > 0 && !dirName.equals(lastDirName)) {
					logger.warn("Inconsistent directory names for scan {} : Found {}, expected {}", scanCount, dirName, lastDirName);
					allDirectoriesMatch = false;
				} else {
					lastDirName = dirName;
				}
			}
			if (!allDirectoriesMatch) {
				break;
			}
			scanCount++;
		}


		// Update the list of available xml files and textboxes
		setXmlDirectoryName(lastDirName);
		if (xmlFiles.size()==0) {
			logger.warn("No xml files found in directory {}.\n", lastDirName);
		}
	}

}
