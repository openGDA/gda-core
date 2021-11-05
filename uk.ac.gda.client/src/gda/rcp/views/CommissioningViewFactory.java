package gda.rcp.views;

import java.util.List;
import java.util.Map;

import org.eclipse.ui.part.ViewPart;

// Based on uk.ac.gda.beamline.i19.shared.views.CommissioningViewFactory commit 1ee5e754467119ffdef03e848d39913c60b4cbc7

public class CommissioningViewFactory extends FindableViewFactoryBase {

	private List<Map<String, Object>> sections;
	private String header;
	private String title;

	public void setHeader(String header) {
		this.header = header;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setSections(List<Map<String, Object>> sections) {
		this.sections = sections;
	}

	@Override
	public ViewPart createView() {
		CommissioningView commview = new CommissioningView();
		ViewUtils.setViewName(commview, title);
		commview.setHeader(header);
		commview.setSections(sections);
		return commview;
	}

}
