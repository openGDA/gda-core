package uk.ac.diamond.daq.experiment.ui.driver;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;


public class MultiSegmentComposite extends Composite {
	
	private VerticalListEditor<DriverProfileSection> segments;
	private LinearSegmentComposite segmentComposite;
	
	public MultiSegmentComposite(Composite parent, int style) {
		super(parent, style);
		
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		
		segments = new VerticalListEditor<>(composite, SWT.NONE);
		segments.setMinItems(1);
		segments.setMaxItems(1000000);
		segments.setEditorClass(DriverProfileSection.class);
		segments.setBeanConfigurator((bean, previous, context)->contiguous(bean, previous));
		segments.setTemplateName("Segment");
		
		GridLayoutFactory.fillDefaults().applyTo(segments);
		
		segmentComposite = new LinearSegmentComposite(composite);
		segments.setEditorUI(segmentComposite);
		
		GridLayoutFactory.swtDefaults().applyTo(segmentComposite);
	}

	private void contiguous(DriverProfileSection bean, DriverProfileSection previous) {
		bean.setStart(previous == null ? 0 : previous.getStop());
		if (previous == null) bean.setStop(1);
		bean.setDuration(previous == null ? 1 : previous.getDuration());
	}
	
	public VerticalListEditor<DriverProfileSection> getSegments() {
		return segments;
	}
	
	public void setUnits(String units) {
		segmentComposite.setUnits(units);
	}

}
