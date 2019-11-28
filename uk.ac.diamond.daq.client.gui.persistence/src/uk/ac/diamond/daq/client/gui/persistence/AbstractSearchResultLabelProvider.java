package uk.ac.diamond.daq.client.gui.persistence;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

import uk.ac.diamond.daq.application.persistence.data.SearchResultHeading;
import uk.ac.diamond.daq.application.persistence.data.SearchResultRow;

public abstract class AbstractSearchResultLabelProvider extends BaseLabelProvider implements ILabelProvider, IStyledLabelProvider {
	private String columnName;
	private String headingTitle;
	private boolean primary;
	private SearchResultHeading heading;
	private SearchResultViewerComparator viewerComparator;
	
	public AbstractSearchResultLabelProvider(String columnName, String headingTitle, boolean primary) {
		this.columnName = columnName;
		this.headingTitle = headingTitle;
		this.primary = primary;
		this.viewerComparator = new SearchResultViewerComparator(SWT.UP);
	}
	
	public abstract String convertValueToText (Object value);
	
	private class SearchResultViewerComparator extends ViewerComparator {
		private int direction;

		private SearchResultViewerComparator (int direction) {
			this.direction = direction;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			String lhsValue = getText(e1);
			String rhsValue = getText(e2);
			int result = lhsValue.compareTo(rhsValue);
			if (direction == SWT.DOWN) {
				result = 0 - result;
			}
			return result;
		}
	}
	
	public ViewerComparator getViewerComparator(int direction) {
		viewerComparator.direction = direction;
		return viewerComparator;
	}
	
	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(getText(element));
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof SearchResultRow) {
			SearchResultRow row = (SearchResultRow)element;
			return convertValueToText(row.getValues().get(heading));
		}
		return "";
	}
	
	public String getColumnName() {
		return columnName;
	}

	public SearchResultHeading getHeading() {
		return heading;
	}
	
	public void setHeading(SearchResultHeading heading) {
		this.heading = heading;
	}
	
	public String getHeadingTitle() {
		return headingTitle;
	}
	
	public boolean isPrimary() {
		return primary;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}
}
