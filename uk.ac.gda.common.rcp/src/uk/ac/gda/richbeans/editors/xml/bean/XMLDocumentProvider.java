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

package uk.ac.gda.richbeans.editors.xml.bean;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.io.FileUtils;


/**
 * @author fcp94556
 *
 */
public class XMLDocumentProvider extends FileDocumentProvider {

	private Document document;

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		
		this.document = (Document)createEmptyDocument();		
	    setupDocument(element, document);
		if (document != null) {
			IDocumentPartitioner partitioner = new XMLPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	class XMLPartitioner extends FastPartitioner {
		
		XMLPartitioner() {
			super(  new XMLPartitionScanner(),
					new String[] {
						XMLPartitionScanner.XML_TAG,
						XMLPartitionScanner.XML_COMMENT });
		}
	}

	@Override
	public IDocument getDocument(final Object element) {
		if (element == null) return document;
		return super.getDocument(element);
	}
	
	@Override
	public boolean isModifiable(Object element) {
		return true;
	}
	@Override
	public boolean isReadOnly(Object element) {
		return false;
	}
	
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof FileStoreEditorInput) {

			final IEditorInput input = (IEditorInput) element;
            final String       path  = EclipseUtils.getFilePath(input);
            try {
				FileUtils.write(new File(path), getDocument(null).get(), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
            	
		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}

}