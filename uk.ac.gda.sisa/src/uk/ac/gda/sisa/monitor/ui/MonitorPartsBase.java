package uk.ac.gda.sisa.monitor.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.client.livecontrol.LiveControl;

public class MonitorPartsBase {
	private static final Logger logger = LoggerFactory.getLogger(MonitorPartsBase.class);
	protected final MonitorConfiguration monitorConfig;
	
	public MonitorPartsBase() {
		
		try {
			monitorConfig = Finder.findLocalSingleton(MonitorConfiguration.class);
		} catch (IllegalArgumentException exception) {
			String msg = "No MonitorConfiguration was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}
	
	private List<String> getGroupNames(ControlSet controlSet) {
		return controlSet.getControls().stream().map(LiveControl::getGroup).distinct().collect(Collectors.toList());
	}
	
	protected void drawControls(Composite composite, ControlSet controlSet, int columns) {
		Group group = new Group(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(columns).spacing(10, 20).applyTo(group);
		controlSet.getControls().stream().forEachOrdered(c -> c.createControl(group));
	}
	
	protected void drawGroupedControls(Composite composite, ControlSet controlSet, int columns) {
		for (String groupName : getGroupNames(controlSet)) {
			Group group = new Group(composite, SWT.NONE);
			group.setText(groupName);
			GridLayoutFactory.swtDefaults().numColumns(columns).spacing(10, 20).applyTo(group);
			controlSet.getControls().stream().filter(c -> groupName.equals(c.getGroup())).forEachOrdered(c -> c.createControl(group));
		}
	}
	
	protected void setScrollingComposite(Composite scrollerContent, ScrolledComposite scroller) {
		// Set the child as the scrolled content of the ScrolledComposite
		scroller.setContent(scrollerContent);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		scroller.setMinSize(scrollerContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

}
