/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.nexus;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.SDAUtils;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.cell.CComboCellEditor;
import uk.ac.gda.richbeans.components.cell.SpinnerCellEditorWithPlayButton;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 * Dialog to slice multi-dimensional data.
 * 
 * Copied from nexus tree viewer but in a simpler to use UI.
 * 
 * TODO Extend this dialog to slice MCA data into 1D plots from their stored values.
 * Currently only 2D supported.
 * 
 * TODO Perhaps move this dialog to top level GUI, maybe through 
 */
public class SliceComponent {
	
	private static final Logger logger = LoggerFactory.getLogger(SliceComponent.class);

	private static final List<String> COLUMN_PROPERTIES = Arrays.asList(new String[]{"Dimension","Axis","Slice"});
	
	private Object      plotType;
	private SliceObject sliceObject;
	private int[]       dataShape;
	private PlotWindow  plotWindow;
	private boolean     autoUpdate=true;

	private TableViewer                        viewer;
	private DimsDataList                       dimsData;
	private CLabel                             errorLabel, explain;
	private Button                             updateAutomatically;
	private Button                             rangeMode;
	private Composite                          area;
	
	protected final BlockingDeque<SliceObject> sliceQueue;
	private Job                                sliceJob;

	
	public SliceComponent() {
		this.sliceQueue = new LinkedBlockingDeque<SliceObject>(7);
	}
	
	public Control createPartControl(Composite parent) {
		
		this.area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(1, false));
		
		this.explain = new CLabel(area, SWT.WRAP);
		final GridData eData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		eData.heightHint=44;
		explain.setLayoutData(eData);

		final Composite top = new Composite(area, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		top.setLayout(new GridLayout(2, false));
		final CLabel label = new CLabel(top, SWT.NONE);
		label.setText("Type");
		
		final ComboWrapper plotChoice = new ComboWrapper(top, SWT.READ_ONLY);
		plotChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final Map<String,Integer> items = new LinkedHashMap<String,Integer>(3);
		items.put("2D",         0);
		items.put("2D Surface", 1);
		plotChoice.setItems(items);
		plotChoice.addValueListener(new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				plotType = e.getValue();
				slice(true);
			}
		});
		plotType = 0;
		plotChoice.setValue(plotType);
		plotChoice.on();
	
		this.viewer = new TableViewer(area, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		createColumns(viewer);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(COLUMN_PROPERTIES.toArray(new String[COLUMN_PROPERTIES.size()]));
		viewer.setCellEditors(createCellEditors(viewer));
		viewer.setCellModifier(createModifier(viewer));
			
		
		this.errorLabel = new CLabel(area, SWT.NONE);
		errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		errorLabel.setImage(AnalysisRCPActivator.getImage("icons/error.png"));
		GridUtils.setVisible(errorLabel,         false);
		
		this.updateAutomatically = new Button(area, SWT.CHECK);
		updateAutomatically.setText("Automatic update");
		updateAutomatically.setToolTipText("Update plot when slice changes");
		updateAutomatically.setSelection(true);
		updateAutomatically.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		updateAutomatically.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoUpdate = updateAutomatically.getSelection();
				slice(false);
			}
		});
		
		this.rangeMode = new Button(area, SWT.CHECK);
		rangeMode.setText("Slice as range");
		rangeMode.setToolTipText("Enter the slice as a range, which is summed.");
		rangeMode.setSelection(false);
		rangeMode.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		rangeMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRangeModeType();
			}
		});

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
				interrupt();
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				if (dimsData==null) return DimsDataList.getDefault();
				return dimsData.getElements();
			}
		});
		viewer.setInput(new Object());
    	
		return area;
	}
	
	protected void updateRangeModeType() {
		final SpinnerCellEditorWithPlayButton scewp = (SpinnerCellEditorWithPlayButton)viewer.getCellEditors()[2];
		scewp.setRangeMode(rangeMode.getSelection());
	}

	private void createDimsData() {
		
		final int dims = dataShape.length;
		
		final File dataFile     = new File(sliceObject.getPath());
		final File lastSettings = new File(SDAUtils.getSdaHome()+dataFile.getName()+"."+sliceObject.getName()+".xml");
		if (lastSettings.exists()) {
			XMLDecoder decoder = null;
			try {
				this.dimsData = new DimsDataList();
				decoder = new XMLDecoder(new FileInputStream(lastSettings));
				for (int i = 0; i < dims; i++) {
					dimsData.add((DimsData)decoder.readObject());
					if (dimsData.getDimsData(i).getSliceRange()!=null) {
						rangeMode.setSelection(true);
						updateRangeModeType();
					}
				}
				
			} catch (Exception ne) {
				logger.debug("Cannot load slice data from last settings!", ne);
				dimsData = null;
			} finally {
				if (decoder!=null) decoder.close();
			}
		}
		
		if (dimsData==null) {
			this.dimsData = new DimsDataList();
			boolean setX=false,setY=false;
			for (int i = 0; i < dataShape.length; i++) {
				dimsData.add(new DimsData(i));
				if (dataShape[i]>1) {
					if (!setX) {
						dimsData.getDimsData(i).setAxis(0);
						setX = true;
					}else  if (!setY) {
						dimsData.getDimsData(i).setAxis(1);
						setY = true;
					}
				}
			}
		}
	}

	/**
	 * Method ensures that one x and on y are defined.
	 * @param data
	 */
	protected boolean synchronizeSliceData(final DimsData data) {
				
		final int usedAxis = data!=null ? data.getAxis() : -2;
		
		for (int i = 0; i < dimsData.size(); i++) {
			if (dimsData.getDimsData(i).equals(data)) continue;
			if (dimsData.getDimsData(i).getAxis()==usedAxis) dimsData.getDimsData(i).setAxis(-1);
		}
		
		boolean isY = false, isX = false;
		for (int i = 0; i < dimsData.size(); i++) {
			if (dimsData.getDimsData(i).getAxis()==0) isX = true;
			if (dimsData.getDimsData(i).getAxis()==1) isY = true;
		}
		if (!isX && !isY) {
			errorLabel.setText("Please set the X and Y axes.");
		} if (!isX) {
			errorLabel.setText("Please set a X axis.");
		} if (!isY) {
			errorLabel.setText("Please set a Y axis.");
		}
		GridUtils.setVisible(errorLabel,         !(isX&&isY));
		//getButton(IDialogConstants.OK_ID).setEnabled(isX&&isY);
		GridUtils.setVisible(updateAutomatically, (isX&&isY));
		errorLabel.getParent().layout(new Control[]{errorLabel,updateAutomatically});
		
		return isX&&isY;
	}

	private ICellModifier createModifier(final TableViewer viewer) {
		return new ICellModifier() {
			
			@Override
			public boolean canModify(Object element, String property) {
				final DimsData data = (DimsData)element;
				final int       col  = COLUMN_PROPERTIES.indexOf(property);
				if (col==0) return false;
				if (col==1) return true;
				if (col==2) {
					if (dataShape[data.getDimension()]<2) return false;
					return data.getAxis()<0;
				}
				return false;
			}

			@Override
			public void modify(Object item, String property, Object value) {

				final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
				final int       col   = COLUMN_PROPERTIES.indexOf(property);
				if (col==0) return;
				if (col==1) data.setAxis((Integer)value);
				if (col==2) {
					if (value instanceof Integer) {
						data.setSlice((Integer)value);
					} else {
						data.setSliceRange((String)value);
					}
				}
				final boolean isValidData = synchronizeSliceData(data);
				viewer.cancelEditing();
				viewer.refresh();
				
				if (isValidData) slice(false);
			}
			
			@Override
			public Object getValue(Object element, String property) {
				final DimsData data = (DimsData)element;
				final int       col  = COLUMN_PROPERTIES.indexOf(property);
				if (col==0) return data.getDimension();
				if (col==1) return data.getAxis();
				if (col==2) {
					// Set the bounds
					final SpinnerCellEditorWithPlayButton editor = (SpinnerCellEditorWithPlayButton)viewer.getCellEditors()[2];
					editor.setMaximum(dataShape[data.getDimension()]-1);
					return data.getSliceRange() != null ? data.getSliceRange() : data.getSlice();
				}
				return null;
			}
		};
	}

	private CellEditor[] createCellEditors(final TableViewer viewer) {
		
		final CellEditor[] editors  = new CellEditor[3];
		editors[0] = null;
		editors[1] = new CComboCellEditor(viewer.getTable(), new String[]{"X","Y"});
		final CCombo combo = ((CComboCellEditor)editors[1]).getCombo();
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!((CComboCellEditor)editors[1]).isActivated()) return;
				final String   value = combo.getText();
				final String[] items = ((CComboCellEditor)editors[1]).getItems();
				if (items!=null) for (int i = 0; i < items.length; i++) {
					if (items[i].equalsIgnoreCase(value)) {
						((CComboCellEditor)editors[1]).applyEditorValueAndDeactivate(i);
						return;
					}
				}
			}
		});

		editors[2] = new SpinnerCellEditorWithPlayButton(viewer, "Play through slices", AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.PLAY_SPEED));
		((SpinnerCellEditorWithPlayButton)editors[2]).addValueListener(new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
                final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
                if (e.getValue() instanceof Number) {
                	data.setSlice(((Number)e.getValue()).intValue());
                	data.setSliceRange(null);
                } else {
                	if (((RangeBox)e.getSource()).isError()) return;
                	data.setSliceRange((String)e.getValue());
                }
         		if (synchronizeSliceData(data)) slice(false);
			}
			
		});

			
		return editors;
	}

	private void createColumns(final TableViewer viewer) {
		
		final TableViewerColumn dim   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		dim.getColumn().setText("Dim");
		dim.getColumn().setWidth(48);
		dim.setLabelProvider(new SliceColumnLabelProvider(0));
		
		final TableViewerColumn axis   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		axis.getColumn().setText("Axis");
		axis.getColumn().setWidth(48);
		axis.setLabelProvider(new SliceColumnLabelProvider(1));

		final TableViewerColumn slice   = new TableViewerColumn(viewer, SWT.RIGHT, 2);
		slice.getColumn().setText("Slice");
		slice.getColumn().setWidth(100);
		slice.setLabelProvider(new SliceColumnLabelProvider(2));
		
	}

	private class SliceColumnLabelProvider extends ColumnLabelProvider {

		private int col;
		public SliceColumnLabelProvider(int i) {
			this.col = i;
		}
		@Override
		public String getText(Object element) {
			final DimsData data = (DimsData)element;
			switch (col) {
			case 0:
				return (data.getDimension()+1)+"";
			case 1:
				final int axis = data.getAxis();
				return axis==0 ? "X" : axis==1 ? "Y" : "";
			case 2:
				if (data.getSliceRange()!=null) return data.getSliceRange();
				final int slice = data.getSlice();
				return slice>-1 ? slice+"" : "";
			default:
				return "";
			}
		}
	}
//	
//	@Override
//	protected void createButtonsForButtonBar(Composite parent) {
//		
//		final Button plot = createButton(parent, IDialogConstants.OK_ID,    "Slice",  true);
//		plot.setImage(AnalysisRCPActivator.getImage("icons/chart_curve_go.png"));
//		createButton(parent, IDialogConstants.CANCEL_ID, "Close",  false);
//	}
//	
//	@Override
//	protected void okPressed() {
//		setReturnCode(OK);
//		slice(true);
//	}
	
	/**
	 * Call this method to show the slice dialog.
	 * 
	 * This non-modal dialog allows the user to slice
	 * data out of n-D data sets into a 2D plot.
	 */
	public void setData(final String     name,
				        final String     filePath,
				        final int[]      dataShape,
				        final PlotWindow plotWindow) {
		
		interrupt();
		saveSettings();

		final SliceObject object = new SliceObject();
		object.setPath(filePath);
		object.setName(name);
		setSliceObject(object);
		setDataShape(dataShape);
		setPlotWindow(plotWindow);
		
		explain.setText("Create a slice of "+sliceObject.getName()+".\nIt has the shape "+Arrays.toString(dataShape));
		((SpinnerCellEditorWithPlayButton)viewer.getCellEditors()[2]).setRangeDialogTitle("Range for slice in '"+sliceObject.getName()+"'");
		createDimsData();
    	createSliceQueue();
    	viewer.refresh();
    	
		synchronizeSliceData(null);
		slice(true);
	}
	
	/**
	 * Does slice in monitored job
	 */
	protected void slice(final boolean force) {
		
		if (!force) {
		    if (!autoUpdate) return;
		}

		// Generate the slice info and record it.
		try {
			final SliceObject currentSlice = SliceUtils.createSliceObject(dimsData, dataShape, sliceObject);
			sliceQueue.clear();
			sliceQueue.add(currentSlice);
		} catch (Exception e) {
			logger.error("Cannot generate slices", e);
		}
	}
	
	public void dispose() {
			
		interrupt();
		saveSettings();
	}
	
	private void saveSettings() {
		
		if (sliceObject == null) return;
		final File dataFile     = new File(sliceObject.getPath());
		final File lastSettings = new File(SDAUtils.getSdaHome()+dataFile.getName()+"."+sliceObject.getName()+".xml");
		if (!lastSettings.getParentFile().exists()) lastSettings.getParentFile().mkdirs();
	
		XMLEncoder encoder=null;
		try {
			encoder = new XMLEncoder(new FileOutputStream(lastSettings));
			for (int i = 0; i < dimsData.size(); i++) encoder.writeObject(dimsData.getDimsData(i));
		} catch (Exception ne) {
			logger.error("Cannot load slice data from last settings!", ne);
		} finally  {
			if (encoder!=null) encoder.close();
		}
	}

	private void interrupt() {
		if (sliceQueue!=null) sliceQueue.clear();
		if (sliceJob!=null) {
			if (sliceQueue!=null) sliceQueue.add(new SliceObject()); // Add nameless slice to stop the queue.
			if (sliceJob!=null) sliceJob.cancel();
			try {
				if (sliceJob!=null) sliceJob.join();
			} catch (InterruptedException e) {
				logger.error("Cannot join", e);
			}
		}
		sliceJob   = null;
	}
	
	/**
	 * A queue to protect the nexus API from lots of thread updates, it will fall over @see NexusLoaderSliceThreadTest 
	 */
	private void createSliceQueue() {

		if (sliceJob!=null) return;
		/**
		 * Tricky to get right thread stuff here. Want to make slice fast to change
		 * but also leave last slice plotted. Change only after testing and running
		 * the regression tests. The use of a queue also minimizes threads (there's only
		 * one) and multiple threads break nexus and are inefficient.
		 */
		this.sliceJob = new Job("Slice(s) of "+sliceObject.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					logger.debug("Slice service started.");
					while (viewer!=null && !viewer.getControl().isDisposed()) {
						
						if (monitor.isCanceled()) return Status.CANCEL_STATUS;

						final SliceObject slice = sliceQueue.take(); // Blocks when no slice.
						if (slice.getName()==null) return Status.OK_STATUS; // Any nameless slice results in stopping the queue
						
						try {
							SliceUtils.plotSlice(slice, dataShape, getGuiPlotMode(), plotWindow, monitor);
						} catch (Exception e) {
							logger.error("Cannot slice "+slice.getName(), e);
						}
					}
					return Status.OK_STATUS;
					
				} catch (InterruptedException ne) {
					logger.error("Slice queue exiting ", ne);
					return Status.CANCEL_STATUS;
				} finally {
					logger.debug("Slice service exited.");
				}
			}

		};
		sliceJob.setPriority(Job.LONG);
		sliceJob.setUser(false); // Popup form not allowed but job appears in status bar.
		sliceJob.setSystem(true);
		sliceJob.schedule();
		
	}


	protected GuiPlotMode getGuiPlotMode() {
		final Integer mode = (Integer)plotType;
		switch(mode) {
			case 0   : return GuiPlotMode.TWOD;
			case 1   : return GuiPlotMode.SURF2D;
			default  : return GuiPlotMode.TWOD;
		}
	}

	public void setSliceObject(SliceObject sliceObject) {
		this.sliceObject = sliceObject;
	}

	public void setDataShape(int[] shape) {
		this.dataShape = shape;
	}

	public void setPlotWindow(PlotWindow plotWindow) {
		this.plotWindow = plotWindow;
	}

	/**
	 * Throws exception if GUI disposed.
	 * @param vis
	 */
	public void setVisible(final boolean vis) {
		area.setVisible(vis);
		area.getParent().layout(new Control[]{area});
		
		if (!vis) {
			interrupt();
			saveSettings();
		}
	}
}
