package uk.ac.gda.lookuptable.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors. Responsible for the redirection of
 * global actions to the active editor. Multi-page contributor replaces the contributors for the individual editors in
 * the multi-page editor.
 */
public class LookupTableEditorContributor extends MultiPageEditorActionBarContributor {
	private IEditorPart activeEditorPart;
	private Action addRowAction;
	private Action deleteRowAction;

	/**
	 * Creates a multi-page contributor.
	 */
	public LookupTableEditorContributor() {
		super();
		createActions();
	}

	/**
	 * Returns the action registed with the given text editor.
	 * 
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	/*
	 * (non-JavaDoc) Method declared in AbstractMultiPageEditorActionBarContributor.
	 */

	@Override
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part)
			return;

		activeEditorPart = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null) {
			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part : null;
			if (editor != null) {

				actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
						getAction(editor, ITextEditorActionConstants.DELETE));
				actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
						getAction(editor, ITextEditorActionConstants.UNDO));
				actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
						getAction(editor, ITextEditorActionConstants.REDO));
				actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
						getAction(editor, ITextEditorActionConstants.CUT));
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
						getAction(editor, ITextEditorActionConstants.COPY));
				actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
						getAction(editor, ITextEditorActionConstants.PASTE));
				actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
						getAction(editor, ITextEditorActionConstants.SELECT_ALL));
				actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
						getAction(editor, ITextEditorActionConstants.FIND));
				actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
						getAction(editor, IDEActionFactory.BOOKMARK.getId()));
				actionBars.updateActionBars();
			} else if (part instanceof LookupTableEditor) {
				actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
						((LookupTableEditor) part).getAction(ActionFactory.UNDO.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
						((LookupTableEditor) part).getAction(ActionFactory.REDO.getId()));
			}
		}
	}

	private void createActions() {
		addRowAction = new Action() {
			@Override
			public void run() {
				MessageDialog.openInformation(null, "i12 Plug-in", "Row added at the end");
			}
		};
		addRowAction.setText("Add Row");
		addRowAction.setToolTipText("Add Row at the end");
		addRowAction
				.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_ADD));

		deleteRowAction = new Action() {
			@Override
			public void run() {
				MessageDialog.openInformation(null, "i12 Plug-in", "selected row removed.");
			}
		};
		deleteRowAction.setText("Delete Row");
		deleteRowAction.setToolTipText("Delete selected row");
		deleteRowAction.setImageDescriptor(Activator.getDefault().getImageRegistry()
				.getDescriptor(ImageConstants.IMG_DEL));
	}

	@Override
	public void contributeToMenu(IMenuManager manager) {
		IMenuManager menu = new MenuManager("Editor &Menu");
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
	}

	@Override
	public void contributeToToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		// manager.add(addRowAction);
		// manager.add(deleteRowAction);
	}
}
