/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.progress.WorkbenchJob;

import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.INexusPathProvider;
import uk.ac.diamond.tomography.reconstruction.ImageConstants;
import uk.ac.diamond.tomography.reconstruction.NexusFilter;
import uk.ac.diamond.tomography.reconstruction.ui.NexusFilterAction;
import uk.ac.diamond.tomography.reconstruction.ui.NexusSortAction;

public class NexusNavigator extends CommonNavigator {

	private static final int JOB_REFRESH_DELAY = 200;

	public static final String ID = "uk.ac.diamond.tomography.reconstruction.NexusNavigator";

	private static final String INITIAL_TEXT = "type filter string";

	private static final long SOFT_MAX_EXPAND_TIME = 200;

	private final Image sortIcon = Activator.getImage("sort-alphabet.png");

	private final Image addFilterIcon = Activator.getImage("funnel--arrow.png");

	private final Image filterIcon = Activator.getImage("funnel.png");

	private Text filterText;

	private NexusSortPathProvider sortPathProvider = new NexusSortPathProvider();

	private NexusFilterPathProvider filterPathProvider = new NexusFilterPathProvider();

	private PatternFilter patternFilter = new PatternFilter();

	private WorkbenchJob refreshJob;

	private Control clearButtonControl;

	/**
	 * Convenience method to return the text of the filter control. If the text widget is not created, then null is
	 * returned.
	 *
	 * @return String in the text, or null if the text does not exist
	 */
	protected String getFilterString() {
		return filterText != null ? filterText.getText() : null;
	}

	@Override
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		// TODO Auto-generated method stub
		super.handleDoubleClick(anEvent);
	}

	protected WorkbenchJob doCreateRefreshJob() {
		return new WorkbenchJob("Refresh Filter") {//$NON-NLS-1$

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (getCommonViewer().getControl().isDisposed()) {
					return Status.CANCEL_STATUS;
				}

				String text = getFilterString();
				if (text == null) {
					return Status.OK_STATUS;
				}

				boolean initial = INITIAL_TEXT.equals(text);
				if (initial) {
					patternFilter.setPattern(null);
				}
				patternFilter.setPattern(text);

				Control redrawFalseControl = getCommonViewer().getControl();
				try {
					// don't want the user to see updates that will be made to
					// the tree
					// we are setting redraw(false) on the composite to avoid
					// dancing scrollbar
					redrawFalseControl.setRedraw(false);
					getCommonViewer().refresh(true);

					if (text.length() > 0 && !initial) {
						/*
						 * Expand elements one at a time. After each is expanded, check to see if the filter text has
						 * been modified. If it has, then cancel the refresh job so the user doesn't have to endure
						 * expansion of all the nodes.
						 */
						TreeItem[] items = getCommonViewer().getTree().getItems();
						int treeHeight = getCommonViewer().getTree().getBounds().height;
						int numVisibleItems = treeHeight / getCommonViewer().getTree().getItemHeight();
						long stopTime = SOFT_MAX_EXPAND_TIME + System.currentTimeMillis();
						boolean cancel = false;
						if (items.length > 0
								&& recursiveExpand(items, monitor, stopTime, new int[] { numVisibleItems })) {
							cancel = true;
						}

						// enabled toolbar - there is text to clear
						// and the list is currently being filtered
						updateToolbar(true);

						if (cancel) {
							return Status.CANCEL_STATUS;
						}
					} else {
						// disabled toolbar - there is no text to clear
						// and the list is currently not filtered
						updateToolbar(false);
					}
				} finally {
					// done updating the tree - set redraw back to true
					TreeItem[] items = getCommonViewer().getTree().getItems();
					if (items.length > 0 && getCommonViewer().getTree().getSelectionCount() == 0) {
						getCommonViewer().getTree().setTopItem(items[0]);
					}
					redrawFalseControl.setRedraw(true);
				}
				return Status.OK_STATUS;
			}

			/**
			 * Returns true if the job should be canceled (because of timeout or actual cancellation).
			 *
			 * @param items
			 * @param monitor
			 * @param cancelTime
			 * @param numItemsLeft
			 * @return true if canceled
			 */
			private boolean recursiveExpand(TreeItem[] items, IProgressMonitor monitor, long cancelTime,
					int[] numItemsLeft) {
				boolean canceled = false;
				for (int i = 0; !canceled && i < items.length; i++) {
					TreeItem item = items[i];
					boolean visible = numItemsLeft[0]-- >= 0;
					if (monitor.isCanceled() || (!visible && System.currentTimeMillis() > cancelTime)) {
						canceled = true;
					} else {
						Object itemData = item.getData();
						if (itemData != null) {
							if (!item.getExpanded()) {
								// do the expansion through the viewer so that
								// it can refresh children appropriately.
								getCommonViewer().setExpandedState(itemData, true);
							}
							TreeItem[] children = item.getItems();
							if (items.length > 0) {
								canceled = recursiveExpand(children, monitor, cancelTime, numItemsLeft);
							}
						}
					}
				}
				return canceled;
			}

		};
	}

	protected void setupFilterText() {
		filterText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				String filterTextString = filterText.getText();
				if (filterTextString.length() == 0 || filterTextString.equals(INITIAL_TEXT)) {
					e.result = INITIAL_TEXT;
				} else {
					// e.result = NLS.bind(WorkbenchMessages.FilteredTree_AccessibleListenerFiltered, new String[] {
					// filterTextString, String.valueOf(getFilteredItemsCount()) });
				}
			}

		});

		filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (filterText.getText().equals(INITIAL_TEXT)) {
					setFilterText(""); //$NON-NLS-1$
					textChanged();
				}
			}
		});

		filterText.addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDown(MouseEvent e) {
				if (filterText.getText().equals(INITIAL_TEXT)) {
					// XXX: We cannot call clearText() due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=260664
					setFilterText(""); //$NON-NLS-1$
					textChanged();
				}
			}
		});

		filterText.addKeyListener(new KeyAdapter() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				// on a CR we want to transfer focus to the list
				boolean hasItems = getCommonViewer().getTree().getItemCount() > 0;
				if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
					getCommonViewer().getTree().setFocus();
					return;
				}
			}
		});

		filterText.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			@Override
			public void modifyText(ModifyEvent e) {
				textChanged();
			}
		});

		// if we're using a field with built in cancel we need to listen for
		// default selection changes (which tell us the cancel button has been
		// pressed)
		if ((filterText.getStyle() & SWT.ICON_CANCEL) != 0) {
			filterText.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * @see
				 * org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if (e.detail == SWT.ICON_CANCEL)
						clearText();
				}
			});
		}

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		// if the text widget supported cancel then it will have it's own
		// integrated button. We can take all of the space.
		if ((filterText.getStyle() & SWT.ICON_CANCEL) != 0)
			gridData.horizontalSpan = 2;
		filterText.setLayoutData(gridData);
	}

	/**
	 * Update the receiver after the text has changed.
	 */
	protected void textChanged() {
		// Ravi Somayaji - this is crap, should have been only necessary from the createPartControl - but is mainly
		// because I'm unable to exclude the workingset filters and actions.
		if (!containsPatternFilter()) {
			getCommonViewer().addFilter(patternFilter);
		}
		// cancel currently running job first, to prevent unnecessary redraw
		refreshJob.cancel();
		refreshJob.schedule(JOB_REFRESH_DELAY);
	}

	private boolean containsPatternFilter() {
		ViewerFilter[] filters = getCommonViewer().getFilters();

		for (ViewerFilter viewerFilter : filters) {
			if (viewerFilter.equals(patternFilter)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the text in the filter control.
	 *
	 * @param string
	 */
	protected void setFilterText(String string) {
		if (filterText != null) {
			filterText.setText(string);
			filterText.selectAll();
		}
	}

	protected void clearText() {
		setFilterText(""); //$NON-NLS-1$
		textChanged();
	}

	protected void updateToolbar(boolean visible) {
		if (clearButtonControl != null) {
			clearButtonControl.setVisible(visible);
		}
	}

	/**
	 * Create the button that clears the text.
	 *
	 * @param parent
	 *            parent <code>Composite</code> of toolbar button
	 */
	private void createClearButton(Composite parent) {
		// only create the button if the text widget doesn't support one
		// natively
		if ((filterText.getStyle() & SWT.ICON_CANCEL) == 0) {
			final Image clearImage = Activator.getDefault().getImageRegistry().get(ImageConstants.ICON_CLEAR_TEXT);

			final Label clearButton = new Label(parent, SWT.NONE);
			clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			clearButton.setImage(clearImage);
			clearButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			clearButton.setToolTipText("Clear filter text");
			clearButton.addMouseListener(new MouseAdapter() {
				private MouseMoveListener fMoveListener;

				@Override
				public void mouseDown(MouseEvent e) {
					fMoveListener = new MouseMoveListener() {
						private boolean fMouseInButton = true;

						@Override
						public void mouseMove(MouseEvent e) {
							boolean mouseInButton = isMouseInButton(e);
							if (mouseInButton != fMouseInButton) {
								fMouseInButton = mouseInButton;
							}
						}
					};
					clearButton.addMouseMoveListener(fMoveListener);
				}

				@Override
				public void mouseUp(MouseEvent e) {
					if (fMoveListener != null) {
						clearButton.removeMouseMoveListener(fMoveListener);
						fMoveListener = null;
						boolean mouseInButton = isMouseInButton(e);
						if (mouseInButton) {
							clearText();
							filterText.setFocus();
						}
					}
				}

				private boolean isMouseInButton(MouseEvent e) {
					Point buttonSize = clearButton.getSize();
					return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
				}
			});
			this.clearButtonControl = clearButton;
		}
	}

	@Override
	public void createPartControl(org.eclipse.swt.widgets.Composite aParent) {
		Composite projExplorerComposite = new Composite(aParent, SWT.None);
		projExplorerComposite.setBackground(ColorConstants.black);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		projExplorerComposite.setLayout(layout);

		Composite txtComposite = new Composite(projExplorerComposite, SWT.None);
		txtComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtComposite.setBackground(ColorConstants.white);
		txtComposite.setLayout(new GridLayout(4, false));

		filterText = new Text(txtComposite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createClearButton(txtComposite);

// WIP - The Filtering is not fully implemented
//		final ImageHyperlink filterButton = new ImageHyperlink(txtComposite, SWT.LEFT);
//		filterButton.setImage(filterIcon);
//		filterButton.setToolTipText("Select Active Filters");
//		filterButton.setBackground(ColorConstants.white);
//		final NexusFilterAction filterAction = new NexusFilterAction(filterPathProvider);
//		filterButton.addHyperlinkListener(new IHyperlinkListener() {
//			@Override
//			public void linkActivated(HyperlinkEvent e) {
//				filterAction.getMenu(filterButton).setVisible(true);
//			}
//
//			@Override
//			public void linkEntered(HyperlinkEvent e) {
//				filterButton.setImage(addFilterIcon);
//			}
//
//			@Override
//			public void linkExited(HyperlinkEvent e) {
//				filterButton.setImage(filterIcon);
//			}
//		});

		final ImageHyperlink sortButton = new ImageHyperlink(txtComposite, SWT.LEFT);
		sortButton.setImage(sortIcon);
		sortButton.setToolTipText("Sort");
		sortButton.setBackground(ColorConstants.white);

		final NexusSortAction sortAction = new NexusSortAction(sortPathProvider);
		sortButton.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				sortAction.getMenu(sortButton).setVisible(true);
			}

			@Override
			public void linkEntered(HyperlinkEvent e) {
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
			}
		});

		Composite navigatorComposite = new Composite(projExplorerComposite, SWT.None);
		navigatorComposite.setLayout(new FillLayout());
		navigatorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		super.createPartControl(navigatorComposite);

		setupFilterText();
		getCommonViewer().addFilter(patternFilter);
		refreshJob = doCreateRefreshJob();
	}

	@Override
	public void saveState(IMemento aMemento) {
		sortPathProvider.saveSortHistory();
		filterPathProvider.saveFilterHistory();
		super.saveState(aMemento);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		sortPathProvider.restoreSortHistory();
		filterPathProvider.restoreFilterHistory();
		super.init(site);
	}

	@Override
	public void dispose() {
		sortIcon.dispose();
		filterIcon.dispose();
		addFilterIcon.dispose();
		super.dispose();
	}

	public INexusPathProvider getSortPathProvider() {
		return sortPathProvider;
	}

	/*
	 * Manages the nexus paths related to the filter
	 */
	private class NexusFilterPathProvider implements INexusPathProvider {
		private String filterPath;
		private static final String NEXUS_FILTER_HISTORY_KEY = "NEXUS_FILTER_HISTORY";
		private Queue<String> nexusPathHistoryQueue = new LinkedList<String>();
		private int queueLength = 6;
		private NexusFilter lastSetFilter;

		@Override
		public void setNexusPath(String path) {
			if (path == null)
				return;
			filterPath = path;
			addToHistory(path);

			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			prefs.put(Activator.PREF_NEXUS_FILTER_PATH, path);

			updateViewerFilters(path);
		}

		private void addToHistory(String path) {
			if (path.isEmpty())
				return;
			if (!nexusPathHistoryQueue.contains(path)) {
				if (nexusPathHistoryQueue.size() == queueLength) {
					nexusPathHistoryQueue.remove();
				}
				nexusPathHistoryQueue.add(path);
			}
		}

		@Override
		public String getNexusPath() {
			return filterPath;
		}

		@Override
		public String[] getNexusPathHistory() {
			String[] history = nexusPathHistoryQueue.toArray(new String[nexusPathHistoryQueue.size()]);
			return history;
		}

		@Override
		public String getSuggestedPath() {
			return getFilterString();
		}

		public void saveFilterHistory() {
			Activator.getDefault().getDialogSettings().put(NEXUS_FILTER_HISTORY_KEY, getNexusPathHistory());
		}

		public void restoreFilterHistory() {
			String[] array = Activator.getDefault().getDialogSettings().getArray(NEXUS_FILTER_HISTORY_KEY);
			if (array != null) {
				for (int i = 0; i < array.length; i++) {
					nexusPathHistoryQueue.add(array[i]);
				}
			}

			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			filterPath = prefs.get(Activator.PREF_NEXUS_FILTER_PATH, "");
		}

		private void updateViewerFilters(String path) {
			if (path.isEmpty()) {
				if (lastSetFilter != null) {
					getCommonViewer().removeFilter(lastSetFilter);
					lastSetFilter = null;
				}
			} else {
				NexusFilter nexusFilter = new NexusFilter(path, false);
				if (lastSetFilter == null) {
					getCommonViewer().addFilter(nexusFilter);
				} else {
					if (!nexusFilter.equals(lastSetFilter)) {
						ViewerFilter[] filters = getCommonViewer().getFilters();
						ArrayList<ViewerFilter> installedFilters = new ArrayList<ViewerFilter>(Arrays.asList(filters));
						installedFilters.add(nexusFilter);
						installedFilters.remove(lastSetFilter);
						getCommonViewer().setFilters(
								installedFilters.toArray(new ViewerFilter[installedFilters.size()]));
					}
				}
				lastSetFilter = nexusFilter;
			}
		}
	}

	/*
	 * Manages the nexus path that is currently being used to sort files in this view
	 */
	private class NexusSortPathProvider implements INexusPathProvider {
		private static final String NEXUS_SORT_HISTORY_KEY = "NEXUS_SORT_HISTORY";
		private Queue<String> nexusPathHistoryQueue = new LinkedList<String>();
		private int queueLength = 6;
		private String sortPath;

		@Override
		public void setNexusPath(String path) {
			if (path == null)
				return;

			sortPath = path;
			addToSortHistory(path);

			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			prefs.put(Activator.PREF_NEXUS_SORT_PATH, path);

			BusyIndicator.showWhile(getCommonViewer().getControl().getShell().getDisplay(), new Runnable() {

				@Override
				public void run() {
					getCommonViewer().refresh(false);
				}
			});
		}

		@Override
		public String getNexusPath() {
			return sortPath;
		}


		@Override
		public String[] getNexusPathHistory() {
			String[] history = nexusPathHistoryQueue.toArray(new String[nexusPathHistoryQueue.size()]);
			return history;
		}

		/*
		 * Add the path to the sort history this view maintains
		 */
		private void addToSortHistory(String path) {
			if (path.isEmpty())
				return;
			if (!nexusPathHistoryQueue.contains(path)) {
				if (nexusPathHistoryQueue.size() == queueLength) {
					nexusPathHistoryQueue.remove();
				}
				nexusPathHistoryQueue.add(path);
			}
		}

		/**
		 * Save this history with dialog settings
		 */
		public void saveSortHistory() {
			Activator.getDefault().getDialogSettings().put(NEXUS_SORT_HISTORY_KEY, getNexusPathHistory());
		}

		/**
		 * Restore saved history
		 */
		public void restoreSortHistory() {
			String[] array = Activator.getDefault().getDialogSettings().getArray(NEXUS_SORT_HISTORY_KEY);
			if (array != null) {
				for (int i = 0; i < array.length; i++) {
					nexusPathHistoryQueue.add(array[i]);
				}
			}

			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			sortPath = prefs.get(Activator.PREF_NEXUS_SORT_PATH, "");
		}

		@Override
		public String getSuggestedPath() {
			return getFilterString();
		}
	}

}
