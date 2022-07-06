/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Based on table in BationView which is a more complex example of a table
 */
public class VisitIDDialog extends Dialog {
	protected TableViewer userTable;
	private String chosenVisitID = null;
	private String[][] visits;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 */
	public VisitIDDialog(Display parent, String[][] visits) {
		super(parent.getActiveShell());
		this.visits = visits;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Choose a visit");
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		final Label l = new Label(container, SWT.NULL);
		l.setText("You can collect data under any of the following visits. Please select the visit you wish to use.");
		
		// create table viewer and table
		final Table table = new Table(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		final GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		this.userTable = new TableViewer(table);
		userTable.setUseHashlookup(true);
		createTableColumns();
		createContentProvider();

		userTable.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				setChosenVisitIdFromSelectedRow();
				okPressed(); // closes dialog
			}
		});
		
		userTable.setInput(visits);

		return container;
	}

	private void createTableColumns() {

		ColumnViewerToolTipSupport.enableFor(userTable, ToolTip.NO_RECREATE);

		final TableViewerColumn visitIDCol = new TableViewerColumn(userTable, SWT.NONE, 0);
		TableColumn tableColumn = visitIDCol.getColumn();
		tableColumn.setAlignment(SWT.CENTER);
		visitIDCol.getColumn().setText("Visit ID");
		visitIDCol.getColumn().setWidth(100);
		visitIDCol.setLabelProvider(new VisitIDColumnLabelProvider(0));

//		final TableViewerColumn proposalCol = new TableViewerColumn(userTable, SWT.NONE, 1);
//		TableColumn tableColumn_1 = proposalCol.getColumn();
//		tableColumn_1.setAlignment(SWT.CENTER);
//		proposalCol.getColumn().setText("Proposal ID");
//		proposalCol.getColumn().setWidth(100);
//		proposalCol.setLabelProvider(new VisitIDColumnLabelProvider(1));

		final TableViewerColumn descriptionCol = new TableViewerColumn(userTable, SWT.NONE, 1);
		descriptionCol.getColumn().setText("Title");
		descriptionCol.getColumn().setWidth(300);
		descriptionCol.setLabelProvider(new VisitIDColumnLabelProvider(1));

	}

	private class VisitIDColumnLabelProvider extends ColumnLabelProvider {

		private int columnIndex;

		VisitIDColumnLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public String getText(Object element) {
			if ((element instanceof String[])) {
				return ((String[]) element)[columnIndex];
			}
			return super.getText(element);
		}
	}

	private void createContentProvider() {
		userTable.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return visits;
			}
		});
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(661, 300);
	}

	public String getChoosenID() {
		return chosenVisitID;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// only accept one item selected
			if (userTable.getTable().getSelectionCount() != 1) {
				return;
			}
			// FIXME
			if (userTable.getTable().getSelection().length != 1){
				return;
			}
			
			setChosenVisitIdFromSelectedRow();
		
		} else {
			chosenVisitID = null;
		}
		super.buttonPressed(buttonId);
	}

	private void setChosenVisitIdFromSelectedRow() {
		TableItem choice = userTable.getTable().getSelection()[0];
		chosenVisitID = ((String[]) choice.getData())[0];
	}

}
