package uk.ac.diamond.daq.client.gui.persistence;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.application.persistence.data.SearchResult;
import uk.ac.diamond.daq.application.persistence.data.SearchResultHeading;
import uk.ac.diamond.daq.application.persistence.data.SearchResultRow;

/**
 * {@link SearchResult} in the persistence service can be selected from this dialog
 * 
 * It can be optionally used as a open or save dialog.  Results can be optionally filtered.
 * If {@link AbstractSearchResultLabelProvider} are provided then the search result columns can
 * be customised.
 */
public class SearchResultViewDialog extends Dialog implements SearchResultSelectionListener {
	public static final int INVALID_ID = 0;
	
	private static final int minHeight = 400;
	private static final int minWidth = 800;

	private SearchResultTable searchResultTable;
	private Text searchText = null;

	private SearchResult searchResult;

	private long itemId;
	private String newName;
	private String title;
	private boolean showId;
	private boolean showVersion;
	private String searchColumn;
	private String searchColumnValue;
	private SearchResultViewDialogMode mode;
	private List<AbstractSearchResultLabelProvider> labelProviders;
	
	private class SearchTextKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			//do nothing
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (searchText != null && searchResultTable != null) {
				searchResultTable.filter(searchText.getText());
				newName = null;
				itemId = INVALID_ID;
			}
		}
	}
	
	/**
	 * @param parent hosting item
	 * @param searchResult results from the search to be viewed
	 * @param title dialog title
	 * @param showId show the ID column
	 * @param showVersion show the version column
	 * @param searchColumn name of the column that will be used by the user text filter
	 * @param mode load or save
	 * @param labelProviders customise the search result columns
	 */
	public SearchResultViewDialog (Shell parent, SearchResult searchResult, String title, boolean showId, 
			boolean showVersion, String searchColumn, SearchResultViewDialogMode mode, 
			List<AbstractSearchResultLabelProvider> labelProviders) {
		super (parent);
		
		this.searchResult = searchResult;
		this.title = title;
		this.showId = showId;
		this.showVersion = showVersion;
		this.searchColumn = searchColumn;
		this.mode = mode;
		this.labelProviders = labelProviders;
		
		itemId = INVALID_ID;
		newName = null;	
	}
	
	/**
	 * Simple dialog in load mode
	 * 
	 * @param parent owning item
	 * @param searchResult results to be displayed
	 */
	public SearchResultViewDialog(Shell parent, SearchResult searchResult) {
		this(parent, searchResult, null, false, false, null, SearchResultViewDialogMode.load, null);
	}
	
	/**
	 * @return the selected item returns {@link INVALID_ID} if no selection is made
	 */
	public long getItemId() {
		return itemId;
	}
	
	/**
	 * @return the text in the user filter text
	 */
	public String getNewName() {
		return newName;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		createSearchBoxComposite (composite);
		
		createTableComposite(composite);
		
		composite.pack();
		
		return composite;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		if (title != null && !"".equals(title)) {
			newShell.setText(title);
		} else if (mode == SearchResultViewDialogMode.load) {
			newShell.setText("Load Item");
		} else {
			newShell.setText("Save Item");
		}
	}
	
	private void createSearchBoxComposite (Composite parent) {
		if (searchColumn == null) {
			return;
		}
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Search by: " + searchColumn);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(label);
		
		searchText = new Text(composite, SWT.NONE);
		if (searchColumnValue != null) {
			searchText.setText(searchColumnValue);
		}
		searchText.addKeyListener(new SearchTextKeyListener());
		
		GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).applyTo(searchText);
	}

	private void createTableComposite (Composite parent) {
		searchResultTable = new SearchResultTable(parent, showId, showVersion, labelProviders);
		searchResultTable.addListener(this);
		GridDataFactory.fillDefaults().grab(true, true).hint(minWidth, minHeight).applyTo(searchResultTable);
		
		SearchResultHeading filterHeading = null;
		if (searchColumn != null) {
			for (SearchResultHeading heading : searchResult.getHeadings()) {
				if (searchColumn.equals(heading.getTitle())) {
					filterHeading = heading;
				}
			}
		}
		if (filterHeading != null) {
			searchResultTable.setSearchResult(searchResult, Collections.singletonList(filterHeading));
		} else {
			searchResultTable.setSearchResult(searchResult);
		}
	}

	@Override
	protected void okPressed() {
		if (searchText != null && mode == SearchResultViewDialogMode.save) {
			newName = searchText.getText();
		}
		setReturnCode(OK);
		close();
	}
	
	private SearchResultRow findRow (long id) {
		for (SearchResultRow row : searchResult.getRows()) {
			if (row.getPersistenceId() == id) {
				return row;
			}
		}
		return null;
	}

	@Override
	public void itemSelected(SearchResultSelectionEvent event) {
		SearchResultRow row = findRow(event.getId());
		if (row != null) {
			itemId = row.getPersistenceId();
			if (searchColumn != null) {
				newName = row.getValue(searchColumn);
				if (searchText != null) {
					searchText.setText(newName);
				}
			}
		} else {
			itemId = INVALID_ID;
			newName = null;
		}
	}

	@Override
	public void itemDoubleClicked(SearchResultSelectionEvent event) {
		SearchResultRow row = findRow(event.getId());
		if (row != null) {
			itemId = row.getPersistenceId();
			if (searchColumn != null) {
				newName = row.getValue(searchColumn);
			}
			setReturnCode(OK);
		} else {
			itemId = INVALID_ID;
			newName = null;
			setReturnCode(CANCEL);
		}
		close();
	}
}
