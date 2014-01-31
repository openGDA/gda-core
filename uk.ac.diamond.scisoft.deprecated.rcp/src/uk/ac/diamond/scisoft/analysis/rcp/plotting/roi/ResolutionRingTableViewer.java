/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.roi;

import java.util.List;

import org.dawb.common.ui.plot.roi.ResolutionRing;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

/**
 * Class that extends a table viewer for linear regions of interests
 */
public final class ResolutionRingTableViewer {
	
	private TableViewer tViewer; 

 	private static final int CHECKCOLNUM = 0;

 	private Menu contextMenu;

	public MenuItem deleteItem;

	public MenuItem editItem;

 	/**
 	 * Edit selection in menu
 	 */
 	//public static final int ROITABLEMENU_EDIT= 0;
 	/**
 	 * Delete selection in menu
 	 */
 	public static final int ROITABLEMENU_DELETE = 2;
	
	/**
	 * @param parent
	 * @param slistener
	 * @param clistener
	 */
	public ResolutionRingTableViewer(Composite parent, SelectionListener slistener,
			ICellEditorListener clistener) {

 		tViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
 		String[] titles = getTitles();
 		String[] tiptext = getTipTexts();
 		int[] widths = getWidths();

 		for (int i = 0; i < titles.length; i++) {
 			TableViewerColumn tVCol = new TableViewerColumn(tViewer, SWT.NONE);
 			TableColumn tCol = tVCol.getColumn();
 			tCol.setText(titles[i]);
 			tCol.setToolTipText(tiptext[i]);
 			tCol.setWidth(widths[i]);
 			tCol.setMoveable(true);
 			tVCol.setEditingSupport(new ResRingEditingSupport(tViewer, i, clistener));
 		}

 		final Table table = tViewer.getTable();

 		table.setHeaderVisible(true);
 		tViewer.setContentProvider(new ResRingContentProvider());
 		tViewer.setLabelProvider(new ResRingLabelProvider());

 		contextMenu = new Menu(tViewer.getControl());
 		// NB preserve this order according to constants above
// 		editItem = new MenuItem(contextMenu, SWT.PUSH);
// 		editItem.addSelectionListener(slistener);
// 		editItem.setText("Edit");
 		deleteItem = new MenuItem(contextMenu, SWT.PUSH);
 		deleteItem.addSelectionListener(slistener);
 		deleteItem.setText("Delete");
 		table.setMenu(contextMenu);
 	}

	String content(Object element, int columnIndex) {
		String msg = null;
		
		ResolutionRing resRingData = (ResolutionRing) element;
		if (resRingData != null) {
			
			switch (columnIndex) {
			case 1:
				msg = String.format("%.2f", resRingData.getResolution());
				break;
			case 2:
				Color c = resRingData.getColour();
				msg = String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
				break;
			}
		}
		return msg;
	}

	String[] getTitles() {
		return new String[] { "Visible", "Resolution","Colour" };
	}

	
	int[] getWidths() {
		return new int[] { 50, 80, 60 };
	}

	String[] getTipTexts() {
		return new String[] { "Visible", "Resolution in Angstrom", "Colour of ring"};
	}


	/**
	 * @return context menu
	 */
	public Menu getContextMenu() {
		return contextMenu;
	}

	/**
	 * Refresh table viewer
	 */
	final public void refresh() {
		tViewer.refresh();
	}

	/**
	 * @param obj
	 */
	final public void setInput(Object obj) {
		tViewer.setInput(obj);
	}

	/**
	 * @return a selection
	 */
	final public ISelection getSelection() {
		return tViewer.getSelection();
	}

	final public int getSelectionIndex() {
		return tViewer.getTable().getSelectionIndex();
	}
	

	final private class ResRingEditingSupport extends EditingSupport {
		private CheckboxCellEditor editor;
		private int column;

		public ResRingEditingSupport(ColumnViewer viewer, int column, ICellEditorListener listener) {
			super(viewer);
			if (column == CHECKCOLNUM) {
				editor = new CheckboxCellEditor(null, SWT.CHECK);
				editor.addListener(listener);
			}
			this.column = column;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			ROIData lrdata = (ROIData) element;
			if (column == CHECKCOLNUM) {
				return lrdata.isPlot();
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			ResolutionRing lrdata = (ResolutionRing) element;
			if (column == CHECKCOLNUM) {
				lrdata.setVisible((Boolean) value);
			}
			getViewer().update(element, null);
		}
	}

	final private class ResRingContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) {
				return null;
			}
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	final private class ResRingLabelProvider implements ITableLabelProvider {
		private final Image CROSSED = AnalysisRCPActivator.getImageDescriptor("icons/cross.png").createImage();

		private final int height = CROSSED.getImageData().height;
		private final int width = CROSSED.getImageData().width;
		private final int linehalfthickness = 2;
		private final RGB white = new RGB(255, 255, 255);

		private Image drawImage(Color colour) {
			PaletteData palette = new PaletteData(new RGB[] { white, colour.getRGB() });
			ImageData data = new ImageData(width, height, 2, palette);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					data.setAlpha(x, y, 0);
				}
			}

			for (int y = -linehalfthickness; y < linehalfthickness; y++) {
				for (int x = 0; x < width; x++) {
					data.setPixel(x, height / 2 + y, 1);
					data.setAlpha(x, height / 2 + y, 255);
				}
			}
			Image image = new Image(CROSSED.getDevice(), data);
			return image;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			ResolutionRing resRing = (ResolutionRing) element;
			if (resRing != null) {
				if (columnIndex == CHECKCOLNUM) {
					if (resRing.isVisible()) {
						return drawImage(resRing.getColour());
					}
					return CROSSED;
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return content(element, columnIndex);
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}
}


