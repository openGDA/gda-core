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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;
import uk.ac.gda.monitor.ProgressMonitorWrapper;
import uk.ac.gda.util.io.SortingUtils;
import uk.ac.gda.util.list.SortNatural;

import com.swtdesigner.SWTResourceManager;

public class DataSetComparisionDialog extends Dialog {
	
	private static final Logger logger = LoggerFactory.getLogger(DataSetComparisionDialog.class);

	private final Collection<String> commonDataSets;
	private final Collection<String> allDataSets;
	
    private boolean            isAllDataSets = false;
	private DataSetPlotView    dataSetPlotView;

	public DataSetComparisionDialog(final IWorkbenchPartSite site, final Object[] files) throws Exception {
		
		super(site.getShell());

		setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		
		this.commonDataSets = new HashSet<String>(31);
		this.allDataSets    = new HashSet<String>(31);
		
		IProgressService service = (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);

		// Changed to cancellable as sometimes loading the tree takes ages and you
		// did not mean such to choose the file.
		service.run(false, true, new IRunnableWithProgress() {

			@Override
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("Extracting Data Sets from files...", 100);
					createData(files, monitor);
					monitor.done();
				} catch (Exception ne) {
					logger.error("Cannot open files", ne);
				}
			}
		});


	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
				
		final Composite container = (Composite)super.createDialogArea(parent);
		
		this.dataSetPlotView = new DataSetPlotView(true, false, true, null);
		
		CLabel label = new CLabel(container, SWT.BORDER|SWT.WRAP|SWT.SCROLL_LINE);
		label.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label.setText("Please select one or more data. The first selection is used as the x-axis when\nmore than one selection is made.");
		{
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.verticalAlignment = SWT.TOP;
			gridData.heightHint = 60;
			label.setLayoutData(gridData);
		}
		
		dataSetPlotView.setMetaData(new MetaDataAdapter(getNames(), getExpressions()));
		dataSetPlotView.createPartControl(container);

		if (isAllDataSets) {
			CLabel error = new CLabel(container, SWT.WRAP);
			error.setText("The intersection of the data sets is empty so all data are shown.\nSome nexus files selected do not contain the data and will not be plotted.");
			error.setImage(SWTResourceManager.getImage(DataSetComparisionDialog.class, "/icons/error.png"));
		}
		final Button btnShowAllData = new Button(container, SWT.CHECK);
		btnShowAllData.setToolTipText("If unchecked, only data common to all nexus files selected are shown");
		btnShowAllData.setText("Show all data");
		btnShowAllData.setSelection(isAllDataSets);
		btnShowAllData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isAllDataSets = btnShowAllData.getSelection();
				dataSetPlotView.setMetaData(new MetaDataAdapter(getNames(), getExpressions()));
				dataSetPlotView.refresh();
			}
		});
		if (isAllDataSets) btnShowAllData.setEnabled(false);
		if (commonDataSets.size()==allDataSets.size())  {
			btnShowAllData.setVisible(false);
		}

		getShell().setText("Choose Data Sets to Compare");
        getShell().setMinimumSize(500, 200);

		return container;
	}

	private Collection<String> getNames() {
		
		final Collection<String> data = isAllDataSets ? allDataSets : commonDataSets;
		final List<String>       sort = new ArrayList<String>(data.size());
		sort.addAll(data);
		Collections.sort(sort, new SortNatural<Object>(false));
		
		return data;
	}
	
	private List<Object> getExpressions() {
		DataSetPlotView curView = (DataSetPlotView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DataSetPlotView.ID);
		return (curView!=null) ? curView.getExpressions(dataSetPlotView) : null;
	}

	private void createData(final Object[] files, IProgressMonitor monitor) throws Exception {
		
		isAllDataSets = false;
		commonDataSets.clear();
		allDataSets.clear();
		
		boolean first = true;
		
		for (int i = 0; i < files.length; i++) {
			final String path = files[i] instanceof File
			                  ? ((File)files[i]).getAbsolutePath()
			                  : ((IFile)files[i]).getLocation().toOSString();
			
			monitor.worked(1);
			if (monitor.isCanceled()) return;
			
			final List<Pattern> ignored = DataSetPlotView.getIgnored();
			final IMetaData     data    = LoaderFactory.getMetaData(path, new ProgressMonitorWrapper(monitor));
			Collection<String>  names   = data != null ? data.getDataNames() : null;
			if (names == null)  {
				final DataHolder dh = LoaderFactory.getData(path, new ProgressMonitorWrapper(monitor));
				names = dh != null ? dh.getMap().keySet() : null;
			}
			SortingUtils.removeIgnoredNames(names,ignored);
			
			if (first) {
				commonDataSets.addAll(names);
				first = false;
			} else {
			    commonDataSets.retainAll(names);
			}
			allDataSets.addAll(names);
		}
		
		if (commonDataSets.isEmpty()) isAllDataSets = true;
		
	}
	
	@Override
	public int open() {
		
	    int iret = IDialogConstants.CANCEL_ID;
		try {
			currentlyOpenChooser = this;
			iret = super.open();
		} finally {
			currentlyOpenChooser = null;
		}
		
		return iret;
	}
	
	private static DataSetComparisionDialog currentlyOpenChooser = null;
	
	public static DataSetComparisionDialog getActiveDialog() {
		return currentlyOpenChooser;
	}

	/**
	 * @return Returns the dataSetPlotView.
	 */
	public DataSetPlotView getDataSetPlotView() {
		return dataSetPlotView;
	}

	public List<Object> getSelections() {
		return dataSetPlotView.getSelections();
	}
	
}
