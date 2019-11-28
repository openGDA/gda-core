package uk.ac.diamond.daq.client.gui.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import uk.ac.diamond.daq.application.persistence.data.SearchResult;
import uk.ac.diamond.daq.application.persistence.data.SearchResultHeading;
import uk.ac.diamond.daq.application.persistence.data.SearchResultRow;

/**
 * Table component to display Persistence Service {@link SearchResult}
 *
 * The table is sortable and filterable
 */
public class SearchResultTable extends Composite {
	private static final int COLUMN_WIDTH = 100;
	private static final String ID_COLUMN_NAME = "Id";
	private static final String VERSION_COLUMN_NAME = "Version";
	
	private TableViewer viewer;
	private List<SearchResultSelectionListener> listeners = new ArrayList<>();
	
	private SearchResult searchResult;
	private boolean showId;
	private boolean showVersion;
	private List<AbstractSearchResultLabelProvider> labelProviders;
	private List<AbstractSearchResultLabelProvider> usedLabelProviders;
	private List<SearchResultHeading> filterHeadings;
	
	/**
	 * @param parent owner
	 * @param showId show the ID column
	 * @param showVersion show the version column
	 * @param labelProviders customise the search result columns
	 */
	public SearchResultTable(Composite parent, boolean showId, boolean showVersion,
			List<AbstractSearchResultLabelProvider> labelProviders) {
		super(parent, SWT.NONE);
		
		this.showId = showId;
		this.showVersion = showVersion;
		this.labelProviders = labelProviders;
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		viewer = new TableViewer(this);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		viewer.setContentProvider(new SearchResultContentProvider());

		
		viewer.addSelectionChangedListener(event -> {
			SearchResultRow row = (SearchResultRow)event.getStructuredSelection().getFirstElement();
			if (row != null) {
				for (SearchResultSelectionListener listener : listeners) {
					listener.itemSelected(new SearchResultSelectionEvent(row.getPersistenceId()));
				}
			}
		});
		
		viewer.addDoubleClickListener(event -> {
			IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			SearchResultRow row = (SearchResultRow)selection.getFirstElement();
			if (row != null) {
				for (SearchResultSelectionListener listener : listeners) {
					listener.itemDoubleClicked(new SearchResultSelectionEvent(row.getPersistenceId()));
				}
			}
		});
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
	}
	
	/**
	 * Reload the table with new Search Results
	 * @param searchResult results to be displayed
	 */
	public void setSearchResult(SearchResult searchResult) {
		setSearchResult(searchResult, null);
	}
	
	/**
	 * Reload the table with new Search Results
	 * @param searchResult results to be displayed
	 * @param filterHeadings set the columns that can be filtered by
	 */
	public void setSearchResult(SearchResult searchResult, List<SearchResultHeading> filterHeadings) {
		this.searchResult = searchResult;
		this.filterHeadings = filterHeadings;
		
		viewer.getTable().removeAll();
		
		List<SearchResultHeading> sortedHeadings = getHeadings(searchResult, showId, showVersion);
		usedLabelProviders = new ArrayList<>();
		
		for (SearchResultHeading heading : sortedHeadings) {
			boolean found = false;
			if (labelProviders != null) {
				for (AbstractSearchResultLabelProvider labelProvider : labelProviders) {
					if (labelProvider.getHeadingTitle().equals(heading.getTitle())) {
						labelProvider.setHeading(heading);
						addColumn(labelProvider, COLUMN_WIDTH);
						found = true;
					}
				}
			}
			if (!found) {
				addColumn(new DefaultSearchResultLabelProvider(heading, false), COLUMN_WIDTH);
			}
		}
		SearchResultRow[] array = new SearchResultRow[searchResult.getRows().size()];
		viewer.setInput(searchResult.getRows().toArray(array));
	}
	
	private List<SearchResultHeading> getHeadings(SearchResult searchResult, boolean showId, boolean showVersion) {
		List<SearchResultHeading> headings = new ArrayList<>();
		for (SearchResultHeading heading : searchResult.getHeadings()) {
			if ((!showId && ID_COLUMN_NAME.equals(heading.getTitle())) 
					|| (!showVersion && VERSION_COLUMN_NAME.equals(heading.getTitle()))) {
				continue;
			}
			headings.add(heading);
		}
		Collections.sort(headings, SearchResultHeading::compare);
		return headings;
	}
	
	private void addColumn (AbstractSearchResultLabelProvider labelProvider, int columnWidth) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.setLabelProvider(new DelegatingStyledCellLabelProvider(labelProvider));
		if (labelProvider.isPrimary()) {
			column.getColumn().notifyListeners(SWT.Selection, null);
		}
		column.getColumn().setWidth(columnWidth);
		column.getColumn().setText(labelProvider.getColumnName());
		column.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Table table = viewer.getTable();
				int direction = table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
				table.setSortDirection(direction);
				table.setSortColumn(column.getColumn());
		        viewer.setComparator(labelProvider.getViewerComparator(direction));
				viewer.refresh();
			}
		});
		if (filterHeadings == null) {
			usedLabelProviders.add(labelProvider);
		} else {
			for (SearchResultHeading heading : filterHeadings) {
				if (heading.equals(labelProvider.getHeading())) {
					usedLabelProviders.add(labelProvider);
				}
			}
		}
	}

	public void addListener(SearchResultSelectionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SearchResultSelectionListener listener) {
		listeners.remove(listener);
	}
	
	public void filter(String filter) {
		List<SearchResultRow> filteredRows;
		if (filter == null || "".equals(filter)) {
			filteredRows = searchResult.getRows();
		} else {
			filter = filter.toLowerCase();
			filteredRows = new ArrayList<>();
			for(SearchResultRow row : searchResult.getRows()) {
				for (AbstractSearchResultLabelProvider labelProvider : usedLabelProviders) {
					String cellValue = labelProvider.getText(row);
					if (cellValue.toLowerCase().contains(filter)) {
						filteredRows.add(row);
						break;
					}
				}
			}
		}
		
		SearchResultRow[] array = new SearchResultRow[filteredRows.size()];
		viewer.setInput(filteredRows.toArray(array));
	}
}
