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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
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

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.exafs.ui.dialogs.OverridesForParametersFile.ParameterOverride;

public class ScanOverrideComposite {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ScanOverrideComposite.class);

	private Composite parent;

	// override parameters for each sample parameters xml file (this is the 'model' being viewed by the table)
	private volatile List<OverridesForScan> overridesForScanFiles = new ArrayList<OverridesForScan>();

	private OverridesForScan scanOverridesTemplate;

	private TableViewer viewer;

	private String xmlDirectoryName = "";
	private List<String> xmlFiles;
	private Text xmDirectoryNameText;
	private String currentTableFilename = "";

	public ScanOverrideComposite(Composite parent) {
		this.parent = parent;
		setScanOverrideTemplate();
	}

	public void setXmlDirectoryName(String xmlDirectoryName) {
		this.xmlDirectoryName = xmlDirectoryName;
	}

	public String getXmlDirectoryName() {
		return this.xmlDirectoryName;
	}

	private void setScanOverrideTemplate() {
		scanOverridesTemplate = new OverridesForScan();
		scanOverridesTemplate.addOverride("Scan", QEXAFSParameters.class.getName());
		scanOverridesTemplate.addOverride("Detector", DetectorParameters.class.getName());
		scanOverridesTemplate.addOverride("Sample", B18SampleParameters.class.getName());
		scanOverridesTemplate.addOverride("Output", OutputParameters.class.getName());
	}

	public List<OverridesForScan> getOverrideForScans() {
		return overridesForScanFiles;
	}

	public void showOverrideList() {
		System.out.println("Current override parameter settings");
		for(int i=0; i<overridesForScanFiles.size(); i++) {
			for (OverridesForParametersFile override : overridesForScanFiles.get(i).getOverrides()) {
				System.out.println(
						String.format("%s, %s : ", override.getXmlFileName(), override.getContainingClassType()));
				for (ParameterOverride vals : override.getOverrides()) {
					System.out.println("\t" + vals.getFullPathToGetter() + " : " + vals.getNewValue());
				}
			}
		}
	}

	private class XmlNameLabelProvider extends ColumnLabelProvider {
		private final int typeIndex;
		public XmlNameLabelProvider(int typeIndex) {
			this.typeIndex = typeIndex;
		}
		@Override
		public String getText(Object element) {
			OverridesForScan param = (OverridesForScan) element;
			String fileFullPath = param.getOverrides().get(typeIndex).getXmlFileName();
			return FilenameUtils.getName(fileFullPath);
		}
	}

	// EditingSupport for xml filename editing via combo box selection
	private class XmlNameEditingSupport extends EditingSupport {

		final int typeIndex;
		public XmlNameEditingSupport(ColumnViewer viewer, int typeIndex) {
			super(viewer);
			this.typeIndex = typeIndex;
		}

		private String[] xmlFileNamesForCombo;

		@Override
		protected CellEditor getCellEditor(final Object element) {
			OverridesForScan opf = (OverridesForScan) element;
			List<String> suitableFiles = OverrideTableClasses.getListOfFilesMatchingType(xmlFiles, opf.getOverrides().get(typeIndex).getContainingClassType());
			int i=0;
			xmlFileNamesForCombo = new String[suitableFiles.size()];
			for(String fullFilePath : suitableFiles) {
				xmlFileNamesForCombo[i++]=FilenameUtils.getName(fullFilePath);
			}

			ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite) getViewer().getControl(), xmlFileNamesForCombo);
			return ce;
		}

		@Override
		protected boolean canEdit(Object ob) {
			if (xmlFiles == null || xmlFiles.size()==0) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		protected Object getValue(Object element) {
			// get value from model and convert to int to update combobox
			OverridesForScan param = (OverridesForScan) element;
			String filename = param.getOverrides().get(typeIndex).getXmlFileName();
			int index = Arrays.binarySearch(xmlFileNamesForCombo, FilenameUtils.getName(filename) );
					// .getName.getFilenameFromFullPath(filename));
			return Math.max(index, 0);
		}

		@Override
		protected void setValue(Object element, Object value) {
			// update model from table
			OverridesForScan param = (OverridesForScan) element;
			int index = (Integer) value;
			param.getOverrides().get(typeIndex).setXmlFileName(xmlDirectoryName+"/"+xmlFileNamesForCombo[index]);
			getViewer().update(param, null);
		}

	}

	/**
	 * Set new parameter override value using supplied string from column in table.
	 *
	 * @param param
	 * @param value
	 * @param columnNumber
	 */
	private void setOverrideFromColumnData(OverridesForScan paramForScan, Object value, int typeIndex, int paramIndex) {
		String strValue = (String) value;
		OverridesForParametersFile params = paramForScan.getOverrides().get(typeIndex);
		params.getOverrides().get(paramIndex).setNewValue(strValue);
	}

	/**
	 * Get parameter override value to go in column of table
	 *
	 * @param param
	 * @param paramIndex
	 * @return
	 */
	private Object getDataForColumn(OverridesForScan param, int typeIndex, int index) {
		return param.getOverrides().get(typeIndex).getOverrides().get(index).getNewValue();
	}

	private class ParameterLabelProvider extends ColumnLabelProvider  {
		final int paramIndex;
		final int typeIndex;
		public ParameterLabelProvider(int typeIndex, int paramIndex) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
		}
		@Override
		public String getText(Object element) {
			OverridesForScan param = (OverridesForScan) element;
			return (String) getDataForColumn(param, typeIndex, paramIndex);
		}
	};

	// EditingSupport for parameter values of modifier
	private class ParameterValueEditingSupport extends EditingSupport {

		final int typeIndex;
		final int paramIndex;

		public ParameterValueEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
		}

		// Called to update value in model from value in edited cell of table
		@Override
		public void setValue(Object element, Object value) {
			OverridesForScan param = (OverridesForScan) element;
			setOverrideFromColumnData(param, value, typeIndex, paramIndex);
			getViewer().update(param, null);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			OverridesForScan param = (OverridesForScan) element;
			return getDataForColumn(param, typeIndex, paramIndex);
		}
	}

	/**
	 * Display tree view widget for selecting which parameters are to be overridden
	 * @param parent
	 * @param typeIndex
	 */
	private void displayTreeViewSelector(Composite parent, int typeIndex) {
		// Store current width of each column in table (so can restore later)...
		HashMap<String, Integer> columnWidths = new HashMap<String,Integer>();
		for(TableColumn t : viewer.getTable().getColumns() ) {
			columnWidths.put(t.getText(), t.getWidth()); // store current width of column
		}

		// Get selected overrides from template, create new instance of bean object to do reflection on
		OverridesForParametersFile currentSelectedOverrides = scanOverridesTemplate.getOverrides().get(typeIndex);
		Class<?> classForTree = OverrideTableClasses.getClassWithName(currentSelectedOverrides.getContainingClassType());
		if(classForTree==null) {
			logger.error("Problem creating class with name {}", currentSelectedOverrides.getContainingClassType());
			return;
		}

		// Create method tree view
		MethodTreeViewDialog methodTreeDialog = new MethodTreeViewDialog(parent.getShell());
		methodTreeDialog.setClassTypeForTree(classForTree); // object used to reflect on to get get/set methods.
		methodTreeDialog.setBlockOnOpen(true);
		methodTreeDialog.create();
		methodTreeDialog.setFromOverrides(currentSelectedOverrides); // update checked status for selected overrides
																// (widget needs to have been created first)
		int returnCode = methodTreeDialog.open();

		// Update selected overrides in template with new values selected from gui
		if (returnCode == Dialog.OK) {
			// Get new user selected overrides
			OverridesForParametersFile newSelectedOverrides = methodTreeDialog.getOverrideBean();

			// update the model to match new overrides
			OverrideTableClasses.updateModelModifiers(overridesForScanFiles, typeIndex, newSelectedOverrides);

			// Sort into alphabetical order based on parameter override name
			OverrideTableClasses.sortModelModifiers(overridesForScanFiles);

			// update template to match the model
			scanOverridesTemplate = OverrideTableClasses.getSelectedOverridesFromModel(overridesForScanFiles);

			// remove all columns
			removeAllColumnsFromTable(viewer);

			// Add them back in...
			addColumnsToTable(viewer, scanOverridesTemplate.getOverrides());

			// Set columns with same widths as before
			for(TableColumn t : viewer.getTable().getColumns() ) {
				Integer width = columnWidths.get(t.getText()); // store current width of column
				if (width!=null && width>0) {
					t.setWidth(width);
				}
			}

			// update table columns to show selected parameters
			viewer.refresh();
			viewer.getTable().redraw();
		}
	}
	/**
	 *
	 */
	private void addClearControls(final Composite parent) {
		Button clearTableButton = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(clearTableButton);
		clearTableButton.setText("Clear table...");
		clearTableButton.setToolTipText("Clear table, reset everyhing");

		clearTableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean clearTable = MessageDialog.openQuestion(parent.getShell(), "Clear the table", "Are you sure you want to clear the table?");
				if (clearTable) {
					setScanOverrideTemplate();
					overridesForScanFiles.clear();
					removeAllColumnsFromTable(viewer);
					addColumnsToTable(viewer, scanOverridesTemplate.getOverrides());
					viewer.refresh();
				}
			}
		});
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
		addClearControls(comp);

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
						List<OverridesForScan> newParams = OverrideCollection.loadFromFile(filename);
						currentTableFilename = filename;
						overridesForScanFiles.clear();
						overridesForScanFiles.addAll(newParams);
						scanOverridesTemplate = OverrideTableClasses.getSelectedOverridesFromModel(overridesForScanFiles);

						updateXmlDirectoryFromModel();
						if (xmlFiles.size()==0) {
							String message = "No xml files were found in directory "+xmlDirectoryName+".\n"+
									"You will be able to add/remove scans, change the parameter modifiers\n"+
									"and save the table, but not change the xml files selected for each scan\n"+
									"or generate the new scan xml files";
							MessageDialog.openWarning(parent.getShell(), "Warning", message);
						}
						removeAllColumnsFromTable(viewer);
						addColumnsToTable(viewer, scanOverridesTemplate.getOverrides());
						adjustColumnWidths(viewer);
						viewer.refresh();
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
						System.out.println(OverrideCollection.toXML(overridesForScanFiles));
						// try and save file
						try {
							OverrideCollection.saveToFile(overridesForScanFiles, filename);
						} catch (IOException e1) {
							logger.error("Problem encountered saving table to XML file", e1);
						}
					}
				}
			}
		});
	}

	private void addGenerateScanControls(final Composite parent) {

		Group group = new Group(parent, SWT.DEFAULT);
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

		final Text outputDirectoryNameText = new Text(compForDirLabelAndTextBox, SWT.NONE);
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
				if (overridesForScanFiles.size()==0) {
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
						}
					}
				}
				if (message.length()>0) {
					MessageDialog.openError(parent.getShell(), "Error", message);
				} else {
					boolean proceed = MessageDialog.openQuestion(parent.getShell(), "Generate scan files", "Scan files will be writted to directory "+outputDirectoryName+".\nDo you want to continue?");
					if (proceed) {
						OverrideTableClasses.generateNewScans(parent, overridesForScanFiles, outputDirectoryNameText.getText());
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
				System.out.println("XML directory : " + result);
				if (result != null) {
					outputDirectoryNameText.setText(result);
				}
			}
		});
	}


	/**
	 * Add gui controls to add/remove scans and clear the table
	 * @param parent
	 */
	private void addScanAddRemoveContols(final Composite parent) {
		// Xml directory selection controls ...
		Group comp = new Group(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		comp.setText("Add and remove scans");

		// GridDataFactory.fillDefaults().span(2,1).grab(true, false).applyTo(comp);

		// Add, delete scan buttons
		Button addScanButton = new Button(comp, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addScanButton);
		addScanButton.setText("Add scan");
		addScanButton.setToolTipText("Add new scan - by copying settings from last one (if available)");

		Button deleteScanButton = new Button(comp, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(deleteScanButton);
		deleteScanButton.setText("Remove scan");
		deleteScanButton.setToolTipText("Remove scan in row that has current focus");

		addScanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OverridesForScan overrideToCopyFrom;
				if (overridesForScanFiles.size() > 0) {
					// Use last override if there is one
					overrideToCopyFrom = overridesForScanFiles.get(overridesForScanFiles.size() - 1);
				} else {
					// use template
					overrideToCopyFrom = scanOverridesTemplate;
				}

				// loop over params for each file and copy them
				OverridesForScan overrideForNewScan = new OverridesForScan();
				for (OverridesForParametersFile opf : overrideToCopyFrom.getOverrides()) {
					OverridesForParametersFile p = new OverridesForParametersFile();
					p.copyFrom(opf);
					overrideForNewScan.addOverride(p);
				}
				overridesForScanFiles.add(overrideForNewScan);

				viewer.refresh();
			}
		});

		deleteScanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selectedItems = viewer.getTable().getSelection();
				if (selectedItems == null || selectedItems.length == 0) {
					return; // nothing selected
				}
				TableItem item = selectedItems[0];
				OverridesForScan selectedScan = (OverridesForScan) item.getData();

				// Remove selected scan from model list
				Iterator<OverridesForScan> iter = overridesForScanFiles.iterator();
				while (iter.hasNext()) {
					OverridesForScan overrides = iter.next();
					if (overrides == selectedScan) {
						iter.remove();
					}
				}
				// Update viewer
				viewer.refresh();
			}
		});
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

		xmDirectoryNameText = new Text(compForXmlDirLabelAndTextBox, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).minSize(1, SWT.DEFAULT).applyTo(xmDirectoryNameText);
		xmDirectoryNameText.setText(xmlDirectoryName);

		Button setXmlDirectoryButton = new Button(compForRow, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(setXmlDirectoryButton);
		setXmlDirectoryButton.setText("Browse...");

		// Display widget to select input xml directory
		setXmlDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
				dirDialog.setMessage("Select directory containing xml files to use");
				dirDialog.setFilterPath(xmDirectoryNameText.getText());
				String result = dirDialog.open();
				if (result != null) {
					xmDirectoryNameText.setText(result);
					setXmlDirectory(result);
				}
			}
		});
	}

	/**
	 * Add sample parameter gui controls
	 * @param parent
	 */
	private void addParameterModifierControls(final Composite parent) {

		Group groupForButtons = new Group(parent, SWT.DEFAULT);
		groupForButtons.setText("Select parameters to be modified");
		groupForButtons.setLayout(new GridLayout(1, false));
		//GridDataFactory.fillDefaults().grab(false, false).applyTo(groupForButtons);

		// Sample param modifier button
		Button modifySampleParamsButton = new Button(groupForButtons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(modifySampleParamsButton);
		modifySampleParamsButton.setText("Sample...");

		// Output param modifier button
		Button modifyOutputParamsButton = new Button(groupForButtons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(modifyOutputParamsButton);
		modifyOutputParamsButton.setText("Output...");

		// Listeners for the buttons ...
		modifySampleParamsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayTreeViewSelector(parent, 2);
			}
		});

		modifyOutputParamsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayTreeViewSelector(parent, 3);
			}
		});
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
	 * Set path to xml directory and create list of all xml files it contains.
	 * @param xmlDirectory
	 */
	public void setXmlDirectory(String xmlDirectory) {
		xmlDirectoryName = xmlDirectory;
		xmlFiles = OverrideTableClasses.getListOfFilesMatchingExtension(xmlDirectoryName, ".xml");
	}

	/**
	 * Update list of available xml files from current model; update the directory name in gui
	 */
	public void updateXmlDirectoryFromModel() {
		boolean allDirectoriesMatch = true;
		String lastDirName = "";

		// Extract name of base directory where the xml files are located; check that they all match
		int scanCount = 1;
		for(OverridesForScan overrideForScan : overridesForScanFiles) {
			for(OverridesForParametersFile overrideForFile : overrideForScan.getOverrides() ) {
				String fileName = overrideForFile.getXmlFileName();
				String dirName = FilenameUtils.getFullPath(fileName);

				if (lastDirName.length() > 0 && !dirName.equals(lastDirName)) {
					logger.warn("Inconsistent directory names for scan {} : Found {}, expected {}", scanCount, dirName, lastDirName);
					allDirectoriesMatch = false;
				} else {
					lastDirName = dirName;
				}
			}
			if (allDirectoriesMatch==false) {
				break;
			}
			scanCount++;
		}

		// Update gui textbox
		xmDirectoryNameText.setText(lastDirName);

		// Update the list of available xml files
		setXmlDirectory(lastDirName);
		if (xmlFiles.size()==0) {
			logger.warn("No xml files found in directory {}.\n", lastDirName);
		}
	}

	private void adjustColumnWidths(TableViewer viewer) {
		GC gc = new GC(viewer.getControl());
		for(TableColumn t : viewer.getTable().getColumns() ) {
			// t.pack();
			Point size = gc.stringExtent(t.getText());
			t.setWidth(size.x+5);
		}
		gc.dispose();
	}

	private void removeAllColumnsFromTable(TableViewer viewer) {
		// Remove all columns from table
		for(TableColumn t : viewer.getTable().getColumns() ) {
			 t.dispose();
		}
	}

	/**
	 * Add columns to table : for each scan file add column with combo box for file selection, followed
	 * by zero or more columns to contain values of selected parameters modifiers.
	 * @param viewer
	 */
	private void addColumnsToTable(TableViewer viewer, List<OverridesForParametersFile> overridesParamFiles) {
		int typeIndex=0;
		int minWidth = 75;
		for(OverridesForParametersFile scanFile : overridesParamFiles) {

			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
			column.setText(scanFile.getXmlFileName());
			column.setWidth(minWidth);
			TableViewerColumn columnViewer = new TableViewerColumn(viewer, column);
			columnViewer.setLabelProvider(new XmlNameLabelProvider(typeIndex));
			columnViewer.setEditingSupport(new XmlNameEditingSupport(viewer, typeIndex));

			// Add column for each selected parameter modifier
			int paramIndex=0;
			for(ParameterOverride override : scanFile.getOverrides() ) {
				column = new TableColumn(viewer.getTable(), SWT.NONE);
				String columnLabel = override.getFullPathToGetter().replace("get", "").replace(".", "\n");
				column.setText(columnLabel);
				column.setWidth(minWidth);
				columnViewer = new TableViewerColumn(viewer, column);
				columnViewer.setLabelProvider(new ParameterLabelProvider(typeIndex, paramIndex));
				columnViewer.setEditingSupport(new ParameterValueEditingSupport(viewer, typeIndex, paramIndex));
				paramIndex++;
			}
			typeIndex++;
		}
	}

	public void createTableAndControls() {

		parent.getShell().setText("Experiment scan XML builder");
		parent.setLayout(new FillLayout());

		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		addControlButtons(comp);
		int style = SWT.BORDER | SWT.HIDE_SELECTION | SWT.FULL_SELECTION;
		viewer = new TableViewer(comp, style);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		// set layout on the Table so it fills rest of composite
		viewer.getTable().setLayout(new FillLayout());
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		OverrideTableClasses.setupForCursorNavigation(viewer);
		addColumnsToTable(viewer, scanOverridesTemplate.getOverrides());

		addLoadSaveControls(comp);
		addGenerateScanControls(comp);

		overridesForScanFiles.clear();

		viewer.setInput(overridesForScanFiles);

		viewer.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Retrieve the model from the TableItem
				TableItem item = viewer.getTable().getSelection()[0];
				OverridesForScan model = (OverridesForScan) item.getData();
				// Invoke editing of the element
				viewer.editElement(model, 0);
			}
		});

		// Expand the column widths to fit content
		adjustColumnWidths(viewer);
	}
}
