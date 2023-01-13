package uk.ac.gda.devices.hatsaxs.ui;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class HatsaxsMenu<T> implements MenuDetectListener, SelectionListener {

	public static final String UPDATE_METHOD = "update";
	
	private final TableViewer viewer;
	private final Menu menu;
	
	private ViewerCell cell;
	
	public HatsaxsMenu(TableViewer tableViewer) {
		requireNonNull(tableViewer, "TableViewer must not be null");
		viewer = tableViewer;
		menu = new Menu(viewer.getTable());
		viewer.getTable().setMenu(menu);
		viewer.getTable().addMenuDetectListener(this);
		
		MenuItem copyToRange = new MenuItem(menu, SWT.NONE);
		copyToRange.setText("Copy to selection");
		
		copyToRange.addSelectionListener(this);
	}
	
	private Table getTable() {
		return viewer.getTable();
	}
	
	@Override
	public void menuDetected(MenuDetectEvent e) {
		TableItem[] selection = getTable().getSelection();
		Point rel = getTable().toControl(e.x, e.y);
		cell = viewer.getCell(rel);
		if (selection == null || selection.length <= 1 || cell == null) {
			e.doit = false;
		}
	}
	
	@SuppressWarnings("unchecked") // Several unchecked casts - should only be used for valid tables
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (cell == null) return;

		TableItem[] selection = getTable().getSelection();
		
		int index = cell.getColumnIndex();
		ViewerRow row = cell.getViewerRow();
		TableColumn column = getTable().getColumn(index);
		Object data = column.getData(UPDATE_METHOD);
		if (data == null) return;
		BiConsumer<T, T> update = (BiConsumer<T, T>) data;
		T src = (T) row.getItem().getData();
		Arrays.stream(selection).forEach(s -> update.accept(src, (T) s.getData()));
		viewer.refresh();
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}
}
