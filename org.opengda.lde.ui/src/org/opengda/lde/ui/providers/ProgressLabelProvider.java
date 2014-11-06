package org.opengda.lde.ui.providers;

import gda.device.detector.pixium.events.ScanPointStartEvent;
import gda.device.detector.pixium.events.ScanStartEvent;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

public class ProgressLabelProvider extends OwnerDrawLabelProvider implements IObserver {
	private Scriptcontroller eventAdmin;
	private TableViewer tableViewer;
	// Total Units to be executed
	int totalUnits = 0;

	// Identify the completed units by reading specific information
	int completedUnits = 100;

	
	public ProgressLabelProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	@Override
	protected void erase(Event event, Object element) {
		//no-op
	}
	@Override
	protected void measure(Event event, Object element) {
	}

	@Override
	protected void paint(Event event, Object element) {

		// Calculate Percentage
		int percentage = (completedUnits * 100 / totalUnits);

		Color foreground = event.gc.getForeground();
		Color background = event.gc.getBackground();
		event.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		event.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));

		Rectangle bounds = ((TableItem) event.item).getBounds(event.index);

		int width = (bounds.width - 1) * percentage / 100;
		event.gc.fillGradientRectangle(event.x, event.y, width, event.height, true);
		Rectangle rect2 = new Rectangle(event.x, event.y, width - 1,event.height - 1);
		event.gc.drawRectangle(rect2);
		event.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		String text = percentage + "%";
		Point size = event.gc.textExtent(text);
		int offset = Math.max(0, (event.height - size.y) / 2);
		event.gc.drawText(text, event.x + 2, event.y + offset, true);
		
		event.gc.setForeground(background);
		event.gc.setBackground(foreground);
	}
	@Override
	public void update(ViewerCell cell) {
		// TODO Auto-generated method stub
		super.update(cell);
	}
	@Override
	public void update(Object source, Object arg) {
		if (source==eventAdmin) {
			if (arg instanceof ScanStartEvent) {
				ScanStartEvent event = ((ScanStartEvent)arg);
				totalUnits = (int) event.getNumberOfPoints();
			} else if (arg instanceof ScanPointStartEvent) {
				completedUnits=(int) ((ScanPointStartEvent)arg).getCurrentPointNumber();
			}
		}
		tableViewer.update(tableViewer.getSelection(), new String[] {SampleTableConstants.STATUS});
		//		update(cell);
	}

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

}
