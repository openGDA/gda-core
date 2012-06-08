package gda.simplescan;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.gda.richbeans.components.FieldBeanComposite;

public class ObjectListEditor extends FieldBeanComposite {

	protected TableViewer viewer;
	private String title;

	public ObjectListEditor(Composite parent, int style, String title) {
		super(parent, style);
		this.title = title;
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		setLayout(gridLayout);
		viewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns();
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.heightHint = 200;
		gridData.widthHint = 256;
		viewer.getControl().setLayoutData(gridData);
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private void createColumns() {
		TableViewerColumn col = createTableViewerColumn(title, 100);
		col.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(cell.getItem().getData().toString());
			}
		});
	}

	public void addItem(List<String> list) {
		viewer.setInput(list);
		viewer.refresh();
	}

	public int[] getSelected() {
		return viewer.getTable().getSelectionIndices();
	}
}