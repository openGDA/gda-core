/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DataSearch extends DialogPage implements ISearchPage {
	
//	private static final Logger logger = LoggerFactory.getLogger(DataSearch.class);

	public static String ID = "uk.ac.diamond.scisoft.analysis.rcp.results.navigator.search.dataSearch";
	
//	private ISearchPageContainer container;
	private ComboViewer          searchViewer;

	protected String searchString;
	protected boolean isCaseSensitive=true, isRegularExpression=false;
	
    protected static List<String> previousSearches;
	/**
	 * @wbp.parser.constructor
	 */
	public DataSearch() {
		super("Data");
		if (previousSearches==null) previousSearches=new ArrayList<String>(7);
	}

	@Override
	public boolean performAction() {
		
		// Search for the data not using a project but using the DataNavigator
		// model directly.
        //final DataNavigator nav = (DataNavigator)EclipseUtils.getActivePage().findView(DataNavigator.ID);
		NewSearchUI.runQueryInBackground(createQuery());
        
        return true;
	}


	@Override
	public void setContainer(ISearchPageContainer container) {
//		this.container = container;

	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		
		initializeDialogUnits(parent);
		
		Composite data= new Composite(parent, SWT.NONE);
		data.setLayout(new GridLayout(2, false));
		
		Label lblNexusFileName = new Label(data, SWT.NONE);
		lblNexusFileName.setText("Nexus File Name:");
		new Label(data, SWT.NONE);
		
		this.searchViewer = new ComboViewer(data, SWT.BORDER);
		searchViewer.add(previousSearches.toArray(new String[previousSearches.size()]));
		Combo combo = searchViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		searchViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				searchString = searchViewer.getCombo().getText();
			}
		});
		combo.addModifyListener(new ModifyListener() {		
			@Override
			public void modifyText(ModifyEvent e) {
				searchString = searchViewer.getCombo().getText();
			}
		});
		
		final Button btnCaseSensitive = new Button(data, SWT.CHECK);
		btnCaseSensitive.setSelection(true);
		btnCaseSensitive.setText("Case sensitive");
		btnCaseSensitive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isCaseSensitive = btnCaseSensitive.getSelection();
			}
		});
		
		Label lblAny = new Label(data, SWT.NONE);
		lblAny.setText("(* = any string, ? = any character, \\ = escape for literals)");
		
		final Button btnRegularExpression = new Button(data, SWT.CHECK);
		btnRegularExpression.setSelection(false);
		btnRegularExpression.setText("Regular Expression");
		btnRegularExpression.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isRegularExpression = btnRegularExpression.getSelection();
			}
		});
		
		setControl(data);

		Dialog.applyDialogFont(data);
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(data, IJavaHelpContextIds.JAVA_SEARCH_PAGE);

	}

	private ISearchQuery createQuery() {
		saveSearchString();
        return new DataSearchQuery(getSearchString(), isCaseSensitive(), isRegularExpression());
	}

	private void saveSearchString() {
		this.searchViewer.add(searchString);
		if (previousSearches.contains(searchString)) return;
		previousSearches.add(searchString);
		if (previousSearches.size()>50) previousSearches.remove(previousSearches.size()-1);
	}

	private String getSearchString() {
		return searchString;
	}

    private boolean isCaseSensitive() {
    	return isCaseSensitive;
    }
    private boolean isRegularExpression() {
    	return isRegularExpression;
    }
} 
