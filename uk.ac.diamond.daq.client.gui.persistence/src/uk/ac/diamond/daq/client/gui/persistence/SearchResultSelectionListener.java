package uk.ac.diamond.daq.client.gui.persistence;

public interface SearchResultSelectionListener {
	void itemSelected(SearchResultSelectionEvent event);
	
	void itemDoubleClicked(SearchResultSelectionEvent event);
}
