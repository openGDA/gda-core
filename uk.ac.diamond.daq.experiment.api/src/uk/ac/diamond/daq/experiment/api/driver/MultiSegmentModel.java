package uk.ac.diamond.daq.experiment.api.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.TypeDescriptor;

@TypeDescriptor(editor="uk.ac.diamond.daq.diad.ui.MultiSegmentComposite", bundle="uk.ac.diamond.daq.diad.ui")
public class MultiSegmentModel {
	
	private List<LinearSegment> segments;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final PropertyChangeListener segmentChanged;
	
	public MultiSegmentModel() {
		segments = new ArrayList<>();
		segmentChanged = pcs::firePropertyChange;
	}
	
	/**
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	public void clear() {
		List<LinearSegment> oldValue = segments;
		segments.forEach(segment -> segment.removePropertyChangeListener(segmentChanged));
		segments.clear();
		pcs.firePropertyChange("segments", oldValue, segments);
	}
	
	public void addLinearSegment(LinearSegment segment) {
		List<LinearSegment> oldValue = segments;
		segment.addPropertyChangeListener(segmentChanged);
		segments.add(segment);
		pcs.firePropertyChange("segmets", oldValue, segments);
	}
	
	public List<LinearSegment> getSegments() {
		return segments;
	}
	
	public void setSegments(List<LinearSegment> segments) {
		this.segments.forEach(segment -> segment.removePropertyChangeListener(segmentChanged));
		this.segments.clear();
		this.segments.addAll(segments);
		this.segments.forEach(segment -> segment.addPropertyChangeListener(segmentChanged));
		// PCS will always be fired due to null previous value
		pcs.firePropertyChange("segments", null, segments);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
}
