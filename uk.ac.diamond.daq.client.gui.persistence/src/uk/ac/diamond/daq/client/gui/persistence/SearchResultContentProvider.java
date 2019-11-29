package uk.ac.diamond.daq.client.gui.persistence;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import uk.ac.diamond.daq.application.persistence.data.SearchResultRow;

public class SearchResultContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof SearchResultRow[]) {
			return (SearchResultRow[])inputElement;
		}
		return new Object[0];
	}
}
