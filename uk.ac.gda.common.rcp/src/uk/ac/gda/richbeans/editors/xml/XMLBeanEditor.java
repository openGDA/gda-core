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

package uk.ac.gda.richbeans.editors.xml;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.richbeans.api.reflection.RichBeanUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.TextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.xml.bean.ColorManager;
import uk.ac.gda.richbeans.editors.xml.bean.XMLConfiguration;
import uk.ac.gda.richbeans.editors.xml.bean.XMLDocumentProvider;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;

import com.swtdesigner.SWTResourceManager;


/**
 * An XML editor for editing castor beans. Allows the user to edit the XML
 * and will validate the XML they have written.
 * 
 * @author Matthew Gerring
 *
 */
public class XMLBeanEditor extends TextEditor  {
	private static final Logger logger = LoggerFactory.getLogger(XMLBeanEditor.class);
	private ColorManager   colorManager;
	private DirtyContainer container;
	private final URL      mappingUrl, schemaUrl;
    private Object         editingBean;

	private IDocumentListener documentListener;
	
	/**
	 * @param container
	 * @param mappingUrl
	 * @param schemaUrl
	 * @param bean
	 * @throws Exception 
	 */
	public XMLBeanEditor(final DirtyContainer container,
			             final URL mappingUrl,
						 final URL schemaUrl,
			             final Object bean) throws Exception {
		super();
		this.container  = container;
		this.mappingUrl = mappingUrl;
		this.schemaUrl  = schemaUrl;
		this.editingBean= bean;
		colorManager    = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager, schemaUrl));
		final XMLDocumentProvider docProv = new XMLDocumentProvider();
		setDocumentProvider(docProv);	
	}
	
	/**
	 * @return the editingBean
	 */
	public Object getEditingBean() {
		return editingBean;
	}

	/**
	 * @param editingBean the editingBean to set
	 */
	public void setEditingBean(Object editingBean) {
		this.editingBean = editingBean;
	}

	/**
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);

        super.createPartControl(parent);
        
        final Control text = parent.getChildren()[0];
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final Composite messageContainer = new Composite(parent, SWT.NONE);
        final GridData gd_messageContainer = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        messageContainer.setLayoutData(gd_messageContainer);
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 2;
        messageContainer.setLayout(gridLayout_1);

        final Label label = new Label(messageContainer, SWT.NONE);
        label.setImage(SWTResourceManager.getImage(XMLBeanEditor.class, "/icons/page_error.png"));

        final Label messageLabel = new Label(messageContainer, SWT.NONE);
        messageLabel.setText("Comment tags cannot be saved.");
         
        messageContainer.setVisible(false);
        
        this.documentListener = new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {}

			@Override
			public void documentChanged(DocumentEvent event) {
				final IDocument doc = event.getDocument();
				try {
					final IRegion region = doc.getLineInformationOfOffset(event.getOffset());
					final String  line   = doc.get(region.getOffset(), region.getLength());
					final boolean comm   = line.indexOf("<!--")>-1;
					if(!messageContainer.isDisposed())
						messageContainer.setVisible(comm);
				} catch (BadLocationException e) {
					logger.error(e.getMessage(), e);
				}
			}
			
		};
		getDocumentProvider().getDocument(null).addDocumentListener(documentListener);

	}
		
	@Override
	public void dispose() {
		getDocumentProvider().getDocument(null).removeDocumentListener(documentListener);
		colorManager.dispose();
		super.dispose();
	}
	
	private boolean dirtyOveride = false;
	@Override
	public boolean isDirty() {
		if (dirtyOveride) return false;
		container.setDirty(super.isDirty());
		return super.isDirty();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		
		final IFile file = getIFile();
		monitor.beginTask(file.getName(), 100);
		try {
			try {
				// This way comments are lost but private fields are preserved.
				xmlToBean();

				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
							InterruptedException {
						try {
							XMLHelpers.writeToXML(mappingUrl, editingBean, file.getLocation().toFile());
							final IFile ifile = getIFile();
							if (ifile != null) {
								ifile.refreshLocal(IResource.DEPTH_ZERO, null);
							}
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				});
				container.setDirty(false);
				
			} catch (Exception e) {
				try {
					logger.debug(e.getMessage(), e);
					MessageDialog.openError(getSite().getShell(),
							              "XML Validation Error",
							              "The XML is not valid and cannot be saved.\n\n"+
							              "Error message:\n"+getSantitizedExceptionMessage(e.getMessage()));
					return;
				} catch (Throwable ne) {
					logger.error(ne.getMessage(), ne);
				}
			}
		} finally {
			monitor.done();
		}
	}
		
	/**
	 * Might be null
	 * @return iFile
	 */
	public IFile getIFile() {
		return EclipseUtils.getIFile(getEditorInput());
	}
	
	/**
	 * Should move this to a utility class at some point.
	 * @param exceptionMessage
	 * @return string
	 */
	public static String getSantitizedExceptionMessage(String exceptionMessage) {
		if (exceptionMessage== null) return null;
		final int index = exceptionMessage.indexOf(':');
		if (index>-1) exceptionMessage = exceptionMessage.substring(index+1);
        return exceptionMessage;
	}

	/**
	 * Call to send the bean to text.
	 * @throws Exception 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 */
	public void beanToXML(final List<String> privateFields) throws Exception {
		try {
			dirtyOveride = true;
			
			final IDocument    doc    = getDocumentProvider().getDocument(null);
			final StringWriter writer = new StringWriter();
			final Object       clone  = BeansFactory.deepClone(editingBean);
			if (privateFields!=null) {
				for (String fieldName : privateFields) {
					RichBeanUtils.setBeanValue(clone, fieldName, null);
				}
			}
			try {
				XMLHelpers.writeToXML(mappingUrl, clone, writer);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			doc.set(writer.toString());
		} finally {
			// The isDirty method in TextEditor is called on another thread
			// after the text changed was notified. Therefore we start listening
			// again with a separate runnable.
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					dirtyOveride = false;
				}
			});
		}
	}
	
	/**
	 * Gets the XML into the bean. If the XML is invalid, will throw exception
	 * with appropriate message.
	 * 
	 * @throws Exception
	 */
	public void xmlToBean() throws Exception {
		
		final IDocument    doc    = getDocumentProvider().getDocument(null);
		XMLHelpers.setFromXML(editingBean, mappingUrl, schemaUrl, doc.get());
	}
	
	/**
	 * Unused
	 * @param monitor

	public void doSaveOld(IProgressMonitor monitor) {
		// Check XML
		try {
			monitor.beginTask("Validating and Saving "+editingBean.getClass().getName(), 100);
			xmlToBean();
			
			final IFile ifile = getIFile();
			if (ifile!=null) {
				ifile.refreshLocal(IResource.DEPTH_ZERO, monitor);
			}
			updateState(getEditorInput());
			validateState(getEditorInput());
			performSave(true, monitor);
			
			if (ifile!=null) {
				ifile.refreshLocal(IResource.DEPTH_ZERO, monitor);
			}
			container.setDirty(false);
			
		} catch (Exception e) { // XML Cannot be parsed.
			try {
				logger.debug(e.getMessage(), e);
				MessageDialog.openError(getSite().getShell(),
						              "XML Validation Error",
						              "The XML is not valid and cannot be saved.\n\n"+
						              "Error message:\n"+getSantitizedExceptionMessage(e.getMessage()));
				return;
			} catch (Throwable ne) {
				logger.error(ne.getMessage(), ne);
			}
		} finally {
			monitor.done();
		}
	}
	 */
}
