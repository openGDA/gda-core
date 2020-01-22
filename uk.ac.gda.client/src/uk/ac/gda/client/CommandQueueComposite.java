/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandDetailsPath;
import gda.commandqueue.CommandId;
import gda.commandqueue.Queue;
import gda.commandqueue.QueueChangeEvent;
import gda.commandqueue.QueuedCommandSummary;
import gda.commandqueue.SimpleCommandSummary;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import uk.ac.gda.richbeans.xml.string.StringInput;
import uk.ac.gda.richbeans.xml.string.StringStorage;

public class CommandQueueComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(CommandQueueComposite.class);
	private Table table;
	IWorkbenchPartSite iWorkbenchPartSite;
	private TableViewer tableViewer;
	Queue queue;
	Clipboard clipboard;

	private Clipboard getClipboard() {
		if (clipboard == null) {
			clipboard = new Clipboard(iWorkbenchPartSite.getShell().getDisplay());
		}
		return clipboard;
	}

	public CommandQueueComposite(Composite parent, int style, final IWorkbenchPartSite iWorkbenchPartSite,
			final Queue queue) {
		super(parent, style);
		this.iWorkbenchPartSite = iWorkbenchPartSite;
		this.queue = queue;

		// layout the GUI
		setLayout(new FormLayout());

		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SHADOW_ETCHED_IN);
		table = tableViewer.getTable();
		FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100);
		fd_table.right = new FormAttachment(100);
		fd_table.left = new FormAttachment(0);
		fd_table.top = new FormAttachment(0);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);

		final TableViewerColumn rowNumViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		final TableColumn rowNumColumn = rowNumViewerColumn.getColumn();
		rowNumColumn.setText("#");
		rowNumColumn.setWidth(30);
		rowNumColumn.setResizable(true);
		rowNumColumn.setMoveable(false);
		rowNumViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final NumberedQueueEntry item = (NumberedQueueEntry) element;
				final QueuedCommandSummary entry = item.entry;
				return (entry.id == CommandId.noneCommand) ? "" : Integer.toString(item.index);
			}
		});

		// add Label and Content providers for Description column
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		TableColumn column = tableViewerColumn.getColumn();
		column.setText("Description");
		column.setWidth(500);
		column.setResizable(true);
		column.setMoveable(true);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				final NumberedQueueEntry item = (NumberedQueueEntry) element;
				final QueuedCommandSummary entry = item.entry;
				return entry.getDescription();
			}

		});

		tableViewer.setContentProvider(new QueueContentProvider());

		// create delete action and enable if rows are selected
		final Action deleteAction = new Action("Delete") {

			@Override
			public void run() {
				if (InterfaceProvider.getBatonStateProvider().amIBatonHolder()) {
					try {
						List<CommandId> ids = getSelectedCommandIds();
						if (!ids.isEmpty()) {
							queue.remove(ids);
							tableViewer.refresh();
						}
					} catch (Exception e) {
						logger.error("Error in run", e);
					}
				} else {
					logger.warn("You cannot delete items in the queue as you do not hold the baton");
				}
			}

		};
		deleteAction.setEnabled(false);

		final Action detailsAction = new Action("Show Details") {

			@Override
			public void run() {
				List<CommandId> ids = getSelectedCommandIds();
				if (!ids.isEmpty()) {
					final CommandId id = ids.get(0);
					iWorkbenchPartSite.getShell().getDisplay().asyncExec(() -> {
						try {
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getActivePage();
							CommandDetails commandDetails = queue.getCommandDetails(id);
							if (commandDetails instanceof CommandDetailsPath) {
								String path = ((CommandDetailsPath) commandDetails).getPath();

								File fileToOpen = new File(path);

								try {
									IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
									IEditorPart part1 = IDE.openEditorOnFileStore(page, fileStore);
									part1.addPropertyListener(new CommandDetailsEditorListener(tableViewer));
								} catch (PartInitException e) {
									logger.error("Error opening file " + path, e);
								}
							} else {
								String description = queue.getCommandSummary(id).getDescription();
								IStorage storage = new StringStorage(commandDetails.getSimpleDetails(), description);
								IStorageEditorInput input = new StringInput(storage);
								if (page != null) {
									IEditorPart part2 = page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
									part2.addPropertyListener(new CommandDetailsEditorListener(tableViewer));
								}
							}
						} catch (Exception ex) {
							logger.error("Error showing command details", ex);
						}
					});
				}

			}

		};
		detailsAction.setEnabled(false);

		// create Move to head of queue action and enable if rows are selected
		final Action moveToHeadAction = new Action("Move to head of queue") {
			@Override public void run() {
				if (InterfaceProvider.getBatonStateProvider().amIBatonHolder()) {
					try {
						List<CommandId> ids = getSelectedCommandIds();
						if (!ids.isEmpty()) {
							queue.moveToHead(ids);
							tableViewer.refresh();
						}
					} catch (Exception e) {
						logger.error("Error in run", e);
					}
				} else {
					logger.warn("You cannot move items to the head of the queue as you do not hold the baton");
				}
			}
		};
		moveToHeadAction.setEnabled(false);

		tableViewer.addSelectionChangedListener(event -> {
			boolean enableOnSelection = !event.getSelection().isEmpty();
			moveToHeadAction.setEnabled(enableOnSelection);
			deleteAction.setEnabled(enableOnSelection);
			detailsAction.setEnabled(enableOnSelection);
		});

		// add context menu
		MenuManager mgr = new MenuManager();
		mgr.add(moveToHeadAction);
		mgr.add(detailsAction);
		mgr.add(deleteAction);
		mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = mgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		iWorkbenchPartSite.registerContextMenu(mgr, tableViewer);
		iWorkbenchPartSite.setSelectionProvider(tableViewer);

		// add Drag and Drop
		// currently only support Drag and Drop to and from itself so this could be simplified
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		final QueueByteArrayTransfer dndTransfer = new QueueByteArrayTransfer();

		Transfer[] dropTransfers = new Transfer[] { dndTransfer }; // restrict drops to supplier of
		// QueueByteArrayTransfer - myself only
		Transfer[] dragTransfers = new Transfer[] { dndTransfer, FileTransfer.getInstance(), TextTransfer.getInstance() };
		tableViewer.addDropSupport(ops, dropTransfers, new ViewerDropAdapter(tableViewer) {

			@Override
			public boolean performDrop(Object data) {
				/*
				 * get selected CommandIds and move to before the row under the mouse
				 */
				if (InterfaceProvider.getBatonStateProvider().amIBatonHolder()) {
					try {
						final NumberedQueueEntry item = (NumberedQueueEntry) getCurrentTarget();
						final QueuedCommandSummary target = item.entry;
						CommandId targetId = target.id;
						List<CommandId> ids = getSelectedCommandIds();
						if (!ids.isEmpty()) {
							logger.info("performDrop {} {}", targetId, ids);
							queue.moveToBefore(targetId, ids);
							tableViewer.refresh();
						}
						return true;
					} catch (Exception e) {
						logger.error("Error in performDrop");
						logger.debug("Error in performDrop", e);
					}
				} else {
					logger.warn("You cannot manipulate the queue as you do not hold the baton");
				}
				return false;

			}

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return true;
			}

		});

		tableViewer.addDragSupport(ops, dragTransfers, new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				event.doit = true;
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = null;
				event.doit = false;
				try {
					if (FileTransfer.getInstance().isSupportedType(event.dataType)
							|| TextTransfer.getInstance().isSupportedType(event.dataType)) {
						// return true if a single item is selected that supports FileTransfer
						List<CommandId> ids = getSelectedCommandIds();
						if (ids.size() == 1) {
							final CommandId id = ids.get(0);
							CommandDetails commandDetails;
							commandDetails = queue.getCommandDetails(id);
							if (FileTransfer.getInstance().isSupportedType(event.dataType)
									&& commandDetails instanceof CommandDetailsPath) {
								event.data = new String[] { ((CommandDetailsPath) commandDetails).getPath() };
								event.doit = true;
							}
							if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
								event.data = new String[] { commandDetails.getSimpleDetails() };
								event.doit = true;
							}
						}
					} else {
						event.doit = true;
					}
				} catch (Exception e) {
					logger.error("Error",e);
				}
			}
		});

		if (queue != null) {
			tableViewer.setInput(queue);
		}
	}

	private List<CommandId> getSelectedCommandIds() {
		List<CommandId> ids = new ArrayList<>();
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;

			for (@SuppressWarnings("unchecked")
			Iterator<Object> iterator = sel.iterator(); iterator.hasNext();) {
				Object obj = iterator.next();
				if (obj instanceof NumberedQueueEntry) {
					final NumberedQueueEntry item = (NumberedQueueEntry) obj;
					final QueuedCommandSummary cqs = item.entry;
					if (cqs.id != CommandId.noneCommand) {
						ids.add(cqs.id);
					}
				}
			}
		}
		return ids;
	}

	/*
	 * make details available to clipboard
	 */
	public void handleCopy(@SuppressWarnings("unused") ExecutionEvent event) throws Exception {
		List<CommandId> ids = getSelectedCommandIds();
		if (!ids.isEmpty()) {
			final CommandId id = ids.get(0);
			Transfer[] transfer;
			Object[] contents;
			CommandDetails commandDetails = queue.getCommandDetails(id);
			if (commandDetails instanceof CommandDetailsPath) {
				String path = ((CommandDetailsPath) commandDetails).getPath();
				contents = new Object[] { new String[] { path }, path };
				transfer = new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() };
			} else {
				contents = new Object[] { commandDetails.getSimpleDetails() };
				transfer = new Transfer[] { TextTransfer.getInstance() };
			}
			getClipboard().setContents(contents, transfer);
		}

	}
}

// class used in Drag and Drop to convert objects from native To Java and back
// currently only support Drag and Drop to and from itself so this could be simplified
class QueueByteArrayTransfer extends ByteArrayTransfer {
	private static final Logger logger = LoggerFactory.getLogger(QueueByteArrayTransfer.class);

	public static final String TYPE_NAME = "gda.queue-transfer-format";
	private final int TYPEID = Transfer.registerType(TYPE_NAME);

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		// we need to fill transferData in with a byte array
		logger.info("javaToNative {}", transferData.getClass());
		// currently do not actually transfer anything - the target and source are the same view
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		logger.info("nativeToJava {}", transferData);
		return super.nativeToJava(transferData);
	}
}

/**
 * class that handles a Queue as a model. Adds observer to the Queue and in update method tells table to refresh
 * getElements returns the QueueCommandSummary list from the Queue
 */
class QueueContentProvider implements IStructuredContentProvider {
	private static final Logger logger = LoggerFactory.getLogger(QueueContentProvider.class);

	private Queue currentQueue;
	private IObserver queueObserver;

	private void removeObserverFromQueue() {
		if (currentQueue != null && queueObserver != null) {
			currentQueue.deleteIObserver(queueObserver);
			currentQueue = null;
		}
	}

	@Override
	public void dispose() {
		removeObserverFromQueue();
	}

	@Override
	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
		removeObserverFromQueue();

		if (newInput instanceof Queue) {
			currentQueue = (Queue) newInput;
			if (queueObserver == null) {
				queueObserver = new IObserver() {
					private boolean refreshQueued = false;

					@Override
					public void update(Object source, Object arg) {
						if (refreshQueued)
							return;
						if (source != currentQueue)
							throw new RuntimeException(
									"Shouldn't be possible(?), it shows that we have not kept our listeners up to date");
						if( !(arg instanceof QueueChangeEvent))
							return;
						refreshQueued = true;
						viewer.getControl().getDisplay().asyncExec(() -> {
							if (!viewer.getControl().isDisposed()) {
								viewer.refresh();
								refreshQueued = false;
							}

						});

					}
				};
			}
			currentQueue.addIObserver(queueObserver);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			List<QueuedCommandSummary> summaryList = currentQueue != null ? currentQueue.getSummaryList() : null;

			if (summaryList == null || summaryList.isEmpty()) {
				final QueuedCommandSummary emptyEntry = new QueuedCommandSummary(CommandId.noneCommand, new SimpleCommandSummary("Empty"));
				summaryList = Collections.singletonList(emptyEntry);
			}

			// Wrap each QueuedCommandSummary in a NumberedQueueEntry
			final NumberedQueueEntry[] numberedEntries = new NumberedQueueEntry[summaryList.size()];
			for (ListIterator<QueuedCommandSummary> it = summaryList.listIterator(); it.hasNext(); ) {
				final int index = it.nextIndex();
				final QueuedCommandSummary entry = it.next();
				numberedEntries[index] = new NumberedQueueEntry();
				numberedEntries[index].index = (index + 1);
				numberedEntries[index].entry = entry;
			}

			return numberedEntries;

		} catch (Exception e) {
			logger.error("Error in getElements", e);
		}
		return new Object[0];
	}
}
