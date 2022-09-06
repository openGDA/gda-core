package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gda.device.ScannableMotion;
import gda.factory.Finder;

/**
 * Drop-down box with field assist showing names of all {@link ScannableMotion} instances
 * as well as configurable 'priority' items shown at the top.
 */
public class ScannableMotionNamesCombo extends ComboViewer {
	
	private Set<String> priorityItems = Collections.emptySet();

	public ScannableMotionNamesCombo(Composite parent) {
		super(parent, SWT.BORDER);
		setContentProvider(ArrayContentProvider.getInstance());
		setComparator(new ReadoutsSorter());
		populate();
	}
	
	/** These will appear at the top of the list */
	public void setPriorityItems(Set<String> readouts) {
		this.priorityItems = readouts;
		if (!getCombo().isDisposed()) {
			populate();
		}
	}
	
	private void populate() {
		var scannables = new HashSet<>(Finder.getFindablesOfType(ScannableMotion.class).keySet());
		scannables.addAll(priorityItems);
		
		setInput(scannables);
		
		new AutoCompleteField(getCombo(), new ComboViewerContentAdapter(), scannables.toArray(new String[0]));
	}
	
	/** Simply sets selection on the viewer, which triggers selection events */
	private class ComboViewerContentAdapter extends ComboContentAdapter {
		
		@Override
		public void setControlContents(Control control, String text, int cursorPosition) {
			super.setControlContents(control, text, cursorPosition);
			ScannableMotionNamesCombo.this.setSelection(new StructuredSelection(text), true);
		}
	}
	
	/** Puts priorityItems above the rest */ 
	private class ReadoutsSorter extends ViewerComparator {
		
		@Override
		public int category(Object element) {
			if (priorityItems.contains(element)) {
				return 0;
			} else {
				return 1;
			}
		}
	}
}
