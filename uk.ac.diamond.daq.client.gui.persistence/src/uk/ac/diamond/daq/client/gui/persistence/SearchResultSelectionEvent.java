package uk.ac.diamond.daq.client.gui.persistence;

import java.util.ArrayList;
import java.util.List;

public class SearchResultSelectionEvent {
	private List<Long> ids;
	
	public SearchResultSelectionEvent(List<Long> ids){
		this.ids = ids;
	}
	
	public SearchResultSelectionEvent(Long id){
		this.ids = new ArrayList<>();
		this.ids.add(id);
	}
	
	public Long getId() {
		return ids.get(ids.size()-1);
	}
	
	public List<Long> getIds(){
		return ids;
	}
}
