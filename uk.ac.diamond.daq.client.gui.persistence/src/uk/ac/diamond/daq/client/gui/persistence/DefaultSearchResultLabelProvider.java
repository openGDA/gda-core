package uk.ac.diamond.daq.client.gui.persistence;

import uk.ac.diamond.daq.application.persistence.data.SearchResultHeading;

public class DefaultSearchResultLabelProvider extends AbstractSearchResultLabelProvider {

	public DefaultSearchResultLabelProvider(SearchResultHeading heading, boolean primary) {
		super(heading.getTitle(), heading.getTitle(), primary);
		
		setHeading(heading);
	}

	@Override
	public String convertValueToText(Object value) {
		if (value != null) {
			return value.toString();
		}
		return "";
	}
}
