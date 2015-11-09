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

package uk.ac.gda.richbeans.editors;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.api.reflection.IBeanService;
import org.eclipse.richbeans.api.widget.IFieldProvider;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.reflection.BeansFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ServiceGrabber;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * This class is designed to be extended and then the extending class edited with RCP developer. By naming the data entry fields the same as the fields in the
 * bean, this class will automatically save and open the bean values into the UI.
 *
 * @author Matthew Gerring
 */
public abstract class RichBeanEditorPart extends EditorPart implements ValueListener, IReusableEditor, IFieldProvider {

	protected static final Logger logger = LoggerFactory.getLogger(RichBeanEditorPart.class);

	/**
	 * The bean used for state
	 */
	protected volatile Object editingBean;

	/**
	 * A bean used in the undo stack assigned when the editor changes IEditorInput (normally at start up)
	 */
	protected volatile Object previousUndoBean;

	/**
	 * An interface which provides for if the editor is dirty.
	 */
	protected final DirtyContainer dirtyContainer;

	/**
	 * The file path which can be null
	 */
	protected String path;

	/**
	 * The URL used in in saving the editing bean
	 */
	protected final URL mappingURL;

	private boolean undoStackActive = true;
	private boolean isDisposed = false;

	/**
	 * Controller used to map ui <-> bean
	 */
	protected IBeanController controller;

	/**
	 * Return the name that the editor should be referred as in error messages and in the multi-editor view.
	 *
	 * @return string
	 */
	protected abstract String getRichEditorTabText();

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public RichBeanEditorPart(final String path, final URL mappingURL, final DirtyContainer dirtyContainer, final Object editingBean) {

		this.path = path;
		this.mappingURL = mappingURL;
		this.dirtyContainer = dirtyContainer;
		this.editingBean = editingBean;

		if (getEditorUI() != null) {
			createDataBindingController();
		}

		/**
		 * The final undo state is recorded by cloning the editing bean and not editing it further when this editor is
		 */
		try {
			this.previousUndoBean = BeansFactory.deepClone(editingBean);
		} catch (Exception e) {
			try {
				logger.error("Cannot clone editing bean.", e);
				this.previousUndoBean = editingBean.getClass().newInstance();
			} catch (Exception e1) {
				logger.error("Cannot instantiate editing bean.", e1);
			}
		}
	}

	protected void createDataBindingController() {
		IBeanService service = ServiceGrabber.getBeanService();
		this.controller = service.createController(getEditorUI(), this.editingBean);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		if (path == null)
			return; // Nothing to save.
		final File file = new File(path);
		monitor.beginTask(file.getName(), 100);
		try {
			try {
				updateFromUIAndReturnEditingBean();
				WorkspaceModifyOperation saveOp = new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
						try {
							XMLHelpers.writeToXML(mappingURL, editingBean, path);

							final IFile ifile = getIFile();
							if (ifile != null) {
								ifile.refreshLocal(IResource.DEPTH_ZERO, null);
							}
						} catch (Exception e) {
							logger.error("Error - RichBeanEditorPart.doSave() failed. Path=" + path + e.getMessage());
							MessageDialog dialog = new MessageDialog(getSite().getShell(), "File didn't save", null, "Path=" + path, MessageDialog.ERROR,
									new String[] {}, 0);
							int result = dialog.open();
							System.out.println(result);
							throw new InvocationTargetException(e);
						}
					}
				};

				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(saveOp);
				notifyFileSaved(file);
				dirtyContainer.setDirty(false);

			} catch (Exception e) {
				// Saving is very important as it saves the state of the editors when switching between editors, perspectives, etc.

				logger.error("Error - RichBeanEditorPart.doSave() failed. Path=" + path + e.getMessage());
				MessageDialog dialog = new MessageDialog(getSite().getShell(), "File didn't save", null, "Path=" + path, MessageDialog.ERROR, new String[] {},
						0);
				int result = dialog.open();
				System.out.println(result);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Override to be called when a file is saved.
	 *
	 * @param file
	 */
	protected void notifyFileSaved(@SuppressWarnings("unused") File file) {

	}

	@Override
	public void doSaveAs() {
		// System.out.println("Do Save as Part");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		if (dirtyContainer != null)
			dirtyContainer.setDirty(false);
	}

	/**
	 * Might be null
	 *
	 * @return iFile
	 */
	public IFile getIFile() {
		return EclipseUtils.getIFile(getEditorInput());
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		if (input != null) {
			setPartName(input.getName());
		}
		// TODO this method should fire a property change as specified in the javadoc, but when this is implemented it will need testing
	}

	/**
	 * @return the editingBean
	 * @throws Exception
	 */
	public Object updateFromUIAndReturnEditingBean() throws Exception {
		controller.uiToBean();
		return controller.getBean();
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Should only be used internally. Do not override / change to be not final.
	 * <p>
	 * Be very careful calling this method, it risks losing synchronisation between the bean held in this editor and other references which should refer to the
	 * same object (for example in a multi-page editor).
	 *
	 * @param editingBean
	 *            the editingBean to set
	 */
	protected final void setEditingBean(Object editingBean) {
		this.editingBean = editingBean;
		createDataBindingController();
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	protected IUndoContext undoableContext;

	public void setUndoableContext(IUndoContext context) {
		this.undoableContext = context;
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {

		dirtyContainer.setDirty(true);
		recordUndoableEvent(e.getFieldName());
	}

	protected void recordUndoableEvent(final String fieldName) {

		if (!isUndoStackActive())
			return;
		try {
			if (Thread.currentThread() != getSite().getShell().getDisplay().getThread())
				return;

			// Save current bean state
			final Object previousBean = BeansFactory.deepClone(previousUndoBean);

			// Be careful about messing with this, very sensitive to make
			// this code correctly generic, try with many editors before
			// committing a change.
			Object tempNewBean = BeansFactory.deepClone(editingBean);
			updateOtherBeanFromUi(tempNewBean);
			Object newBean = BeansFactory.deepClone(tempNewBean);

			// If the values are the same as last edited, do nothing.
			if (previousUndoBean != null && previousUndoBean.equals(newBean))
				return;

			// Add operation to stack.
			final RichBeanEditorOperation undoableOperation = new RichBeanEditorOperation(fieldName, previousBean, newBean, this);
			undoableOperation.addContext(undoableContext);
			OperationHistoryFactory.getOperationHistory().add(undoableOperation);

			previousUndoBean = newBean;

			getEditorSite().getActionBars().updateActionBars();

		} catch (Exception e1) {
			logger.error("Unable to add event to stack " + editingBean.toString(), e1);
		}
	}

	private void updateOtherBeanFromUi(Object otherBean) throws Exception {
		ServiceGrabber.getBeanService().createController(getEditorUI(), otherBean).uiToBean();
	}

	@Override
	public String getValueListenerName() {
		return "DirtyListener";
	}

	@Override
	public boolean isDirty() {
		if (path == null)
			return false;
		return dirtyContainer.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	protected boolean addedListenersAndSwitchedOn = false;

	/**
	 * Extending classes should normally override this method with bounds and choice information and then call this method with super.linkUI();
	 */
	public void linkUI(@SuppressWarnings("unused") final boolean isPageChange) {

		// Call a method which assigns properties from the scan parameters bean to
		// the ui in this class. This class can be used to do this for any
		// bean and any UI object (editor etc.)
		try {
			controller.switchState(false);
			controller.beanToUI();
			controller.switchState(true);
			if (!addedListenersAndSwitchedOn) {
				controller.addValueListener(this);
				controller.recordBeanFields();
				addedListenersAndSwitchedOn = true;

				// TODO resolve this - the DawnSci widgets do not allow expressions - a licensing issue??
//				// We ensure that fields being edited which allow expressions, have the IExpressionManager
//				// available to evaluate the expressions for them.
//				BeanUI.notify(editingBean, getEditorUI(), new BeanProcessor() {
//					@Override
//					public void process(String name, Object value, IFieldWidget box) throws Exception {
//						if (box instanceof IExpressionWidget) {
//							final IExpressionWidget expressionBox = (IExpressionWidget)box;
//							if (expressionBox.isExpressionAllowed()){
//								final BeanExpressionManager man       = new BeanExpressionManager(expressionBox, RichBeanEditorPart.this);
//								man.setAllowedSymbols(getExpressionFields());
//								expressionBox.setExpressionManager(man);
//							}
//						}
//					}
//				});

			}
		} catch (Exception e) {
			logger.error("Cannot push values from bean to UI in linkUI()", e);
		}
	}

	/**
	 * Override to define a different object for editing the UI.
	 *
	 * @return the editorUI object
	 */
	protected Object getEditorUI() {
		return this;
	}

	protected List<String> expressionFields;

	/**
	 * Override this method (usually by calling it too) to add values which should be available in expressions. NOTE when overriding that after the first call
	 * the expressionFields are cached to avoid too many interogations of the bean.
	 *
	 * @return List<String> of possible expression vars.
	 * @throws Exception
	 */
	protected List<String> getExpressionFields() throws Exception {

		if (expressionFields == null) {
			expressionFields = controller.getEditingFields();
		}
		return expressionFields;
	}

	@Override
	public IFieldWidget getField(final String fieldName) throws Exception {
		return controller.getFieldWidget(fieldName);
	}

	@Override
	public Object getFieldValue(final String fieldName) throws Exception {
		return getField(fieldName).getValue();
	}

	@Override
	public void dispose() {
		setDisposed(true);
		super.dispose();

		try {
			controller.dispose();
		} catch (Exception e) {
			logger.error("Cannot dispose parts as expected", e);
		}
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	public void setDisposed(boolean isDisposed) {
		this.isDisposed = isDisposed;
	}

	/**
	 * @return Returns the undoStackActive.
	 */
	public boolean isUndoStackActive() {
		return undoStackActive;
	}

	/**
	 * @param undoStackActive
	 *            The undoStackActive to set.
	 */
	public void setUndoStackActive(boolean undoStackActive) {
		this.undoStackActive = undoStackActive;
	}

	protected void uiToBean() throws Exception {
		controller.uiToBean();
	}

	/**
	 * This method will fire the UI's value listeners - be careful to avoid recursive update loops. For an alternative which does not fire the value listeners,
	 * call {@link RichBeanEditorPart#linkUI(boolean isPageChange)}
	 *
	 * @throws Exception
	 */
	protected void beanToUI() throws Exception {
		controller.beanToUI();
	}

	protected void switchState(boolean on) throws Exception {
		controller.switchState(on);
	}

	protected void addValueListener(ValueListener l) throws Exception {
		controller.addValueListener(l);
	}

	/**
	 * Update the UI with values from the given bean. Intended to be used by, for example, undo/redo operations to update the field values without replacing the
	 * editing bean and losing synchronisation with other editor pages.
	 * <p>
	 * This will fire the UI's value listeners so other clients are aware of the change.
	 *
	 * @param otherBean
	 */
	protected void updateUiFromOtherBean(Object otherBean) throws Exception {
		ServiceGrabber.getBeanService().createController(getEditorUI(), otherBean).beanToUI();
	}
}
