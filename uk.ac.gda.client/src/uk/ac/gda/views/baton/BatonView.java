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

package uk.ac.gda.views.baton;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import uk.ac.gda.preferences.PreferenceConstants;

public class BatonView extends ViewPart implements IObserver{

	private static final Logger logger = LoggerFactory.getLogger(BatonView.class);

	public static final String ID = "gda.rcp.views.baton.BatonView"; //$NON-NLS-1$

	protected TableViewer userTable;

	private static enum columnType {USER, NAME, VISIT, CLIENT_NUMBER, HOSTNAME}
	private static String[] columnToolTip = {"User", "Name", "Visit", "Client #", "Hostname"};

	public BatonView() {
		try {
			GDAClientActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.KEEP_BATON);
			GDAClientActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.BATON_REQUEST_TIMEOUT);
		} catch (Exception e) {
			logger.error("Cannot connect to JythonServerFacade from BatonView", e);
		}
	}

	@Override
	public void init(final IViewSite site) throws PartInitException {
		super.init(site);
		try {
			InterfaceProvider.getJSFObserver().addIObserver(this);
		} catch (Exception e) {
			throw new PartInitException("Cannot attach to Jython Server", e);
		}
	}

	/**
	 * Create contents of the view part.
	 */
	@Override
	public void createPartControl(Composite parent) {

		if (!LocalProperties.isBatonManagementEnabled()) {
			final Label error = new Label(parent, SWT.NONE);
			error.setText("Baton control is not enabled for this beam line.");
			return;
		}

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());

		final Table    table    = new Table(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		final GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		this.userTable = new TableViewer(table);
		userTable.setUseHashlookup(true);
		createTableColumns();
		createContentProvider();


		getSite().setSelectionProvider(userTable);
		try {
			userTable.setInput(InterfaceProvider.getBatonStateProvider().getMyDetails());
		} catch (Exception e) {
			logger.error("Cannot connect to Jython Server Facade from "+getClass().getName());
		}

		createRightClickMenu();
		initializeToolBar();
		initializeMenu();
	}


	private void createRightClickMenu() {
	    final MenuManager menuManager = new MenuManager();
	    userTable.getControl().setMenu (menuManager.createContextMenu(userTable.getControl()));
		getSite().registerContextMenu(menuManager, userTable);
	}

	private void createContentProvider() {
		userTable.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
		        try {
					final ClientDetails[] ca = InterfaceProvider.getBatonStateProvider().getOtherClientInformation();
					final ClientDetails   d  = InterfaceProvider.getBatonStateProvider().getMyDetails();
					final ClientDetails[] ret = new ClientDetails[ca!=null?ca.length+1:1];
					ret[0] = d;
					if (ca!=null) for (int i = 0; i < ca.length; i++) ret[i+1] = ca[i];
					return ret;
				} catch (Exception e) {
					logger.error("Cannot retrieve client information from Jython Server Facade.", e);
					return new Object[]{new Object()};
				}
			}
		});
	}

	private void createTableColumns() {

		ColumnViewerToolTipSupport.enableFor(userTable,ToolTip.NO_RECREATE);

		int user = columnType.USER.ordinal();
		final TableViewerColumn userColumn = new TableViewerColumn(userTable, SWT.NONE, user);
		userColumn.getColumn().setText(columnToolTip[user]);
		userColumn.getColumn().setWidth(120);
		userColumn.setLabelProvider(new BatonColumnLabelProvider(user));

		int name = columnType.NAME.ordinal();
		final TableViewerColumn nameColumn = new TableViewerColumn(userTable, SWT.NONE, name);
		nameColumn.getColumn().setText(columnToolTip[name]);
		nameColumn.getColumn().setWidth(200);
		nameColumn.setLabelProvider(new BatonColumnLabelProvider(name));

		int visit = columnType.VISIT.ordinal();
		final TableViewerColumn visitColumn = new TableViewerColumn(userTable, SWT.NONE, visit);
		visitColumn.getColumn().setText(columnToolTip[visit]);
		visitColumn.getColumn().setWidth(90);
		visitColumn.setLabelProvider(new BatonColumnLabelProvider(visit));

		int clientNumber = columnType.CLIENT_NUMBER.ordinal();
		final TableViewerColumn clientNumberColumn = new TableViewerColumn(userTable, SWT.NONE, clientNumber);
		clientNumberColumn.getColumn().setText(columnToolTip[clientNumber]);
		clientNumberColumn.getColumn().setWidth(70);
		clientNumberColumn.setLabelProvider(new BatonColumnLabelProvider(clientNumber));

		int hostname = columnType.HOSTNAME.ordinal();
		final TableViewerColumn hostnameColumn = new TableViewerColumn(userTable, SWT.NONE, hostname);
		hostnameColumn.getColumn().setText(columnToolTip[hostname]);
		hostnameColumn.getColumn().setWidth(175);
		hostnameColumn.setLabelProvider(new BatonColumnLabelProvider(hostname));
	}

	private class BatonColumnLabelProvider extends ColumnLabelProvider {

		private int columnIndex;
		BatonColumnLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}

	    final Image flagGreen     = SWTResourceManager.getImage(BatonView.class,   "/flag_green.png");

	    @Override
		public Image getImage(Object element) {
			if (!(element instanceof ClientDetails)) return super.getImage(element);
			if (columnIndex!=columnType.USER.ordinal()) return null;
			final ClientDetails detail = (ClientDetails)element;
			if (detail.isHasBaton()) {
				return flagGreen;
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (!(element instanceof ClientDetails)) return super.getText(element);
			final ClientDetails detail = (ClientDetails)element;
			if (columnIndex==columnType.CLIENT_NUMBER.ordinal()) {
				return detail.getIndex()+"";
			} else if (columnIndex==columnType.USER.ordinal()) {
				String user = detail.getUserID();
				final ClientDetails me = InterfaceProvider.getBatonStateProvider().getMyDetails();
				if (me.getIndex()==detail.getIndex()) {
					user += "*";
				}
				user += " (" + Integer.toString(detail.getAuthorisationLevel()) + ")";
				return user;
			} else if (columnIndex == columnType.NAME.ordinal()) {
				return detail.getFullName();
			} else if (columnIndex==columnType.HOSTNAME.ordinal()) {
				return detail.getHostname();
			} else if (columnIndex==columnType.VISIT.ordinal()) {
				return detail.getVisitID();
			}
			return null;
		}

		@Override
		public String getToolTipText(Object element) {
			if (!(element instanceof ClientDetails)) return super.getToolTipText(element);
			final ClientDetails detail = (ClientDetails)element;
			if (columnIndex==columnType.CLIENT_NUMBER.ordinal()) {
				return null;
			} else if (columnIndex==columnType.USER.ordinal()) {
				String userMessage;
				final ClientDetails me = InterfaceProvider.getBatonStateProvider().getMyDetails();
				if (me.getUserID().equals(detail.getUserID())) {
					userMessage = "You are currently logged on as '" + detail.getUserID() + "'";
				} else {
					userMessage = "The user '" + detail.getUserID() + "'";
				}
				userMessage += " - authorisation level " + detail.getAuthorisationLevel();
				final ClientDetails batonHolder = InterfaceProvider.getBatonStateProvider().getBatonHolder();
				if (batonHolder.getUserID().equals(detail.getUserID())) {
					userMessage +=  " (baton holder)";
				}
				return userMessage;
			} else if (columnIndex==columnType.HOSTNAME.ordinal()) {
				return "The hostname of this user.";
			} else if (columnIndex==columnType.VISIT.ordinal()) {
				return "The visit in current use.";
			}
			return null;
		}

	}

	private void initializeToolBar() {
		@SuppressWarnings("unused")
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	private void initializeMenu() {
		@SuppressWarnings("unused")
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		if (userTable==null||userTable.getTable()==null||userTable.getTable().isDisposed()) return;
		userTable.getTable().setFocus();
	}

	@Override
	public void update(Object theObserved, final Object changeCode) {
		if (changeCode instanceof BatonChanged) {
			getSite().getShell().getDisplay().asyncExec(new Runnable()  {
				@Override
				public void run() {
					userTable.refresh();
				}
			});
		}
	}

	@Override
	public void dispose() {
		try {
			InterfaceProvider.getJSFObserver().deleteIObserver(this);
			if(userTable != null && userTable.getTable() != null)
				userTable.getTable().dispose();
		} catch (Exception e) {
			logger.error("Cannot reomve BatonView from JythonServerFacade", e);
		}
	}

	public void refresh() {
		userTable.refresh();
	}

}
