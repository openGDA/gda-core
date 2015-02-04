/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A bean editor part that delegates to a composite.
 * 
 * You must call setEditorClass(...) and setRichEditorTabText(...) 
 * just after the part is instantiated.
 * 
  <usage>
  DelegatingRichBeanEditorPart ed = new DelegatingRichBeanEditorPart(path, getMappingUrl(), this, editingBean);
  ed.setEditorClass(uiClass);
  ed.setRichEditorTabText(editorTabName);
  </usage>
 * 
 */
public class DelegatingRichBeanEditorPart extends RichBeanEditorPart {

	private Class<? extends Composite> editorClass;
	private Composite editorUI;
	private String    tabText;
	private boolean enableScrolling = false;
	
	public DelegatingRichBeanEditorPart(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}
	
	/**
	 * Please call shortly after creation
	 * @param delegateClass
	 */
	public void setEditorClass(Class<? extends Composite> delegateClass) {
		this.editorClass = delegateClass;
	}
	
	/**
	 * Please call shortly after creation
	 */	
	public void setScrollable(boolean enableScrolling){
		this.enableScrolling = enableScrolling;
	}
	
	/**
	 * Please call shortly after creation.
	 * @param tabText
	 */
	public void setRichEditorTabText(String tabText) {
		this.tabText = tabText;
	}

	@Override
	protected String getRichEditorTabText() {
		return tabText;
	}

	@Override
	public void createPartControl(Composite parent) {
		try {		
			if (enableScrolling){
				final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
				scrolledComposite.setExpandHorizontal(true);
				scrolledComposite.setExpandVertical(true);
				instantiateComposite(scrolledComposite);
				scrolledComposite.setContent(editorUI);
				editorUI.layout();
				scrolledComposite.setMinSize(editorUI.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			} else {
				instantiateComposite(parent);
			}
		} catch(Exception e) {
			logger.error("Cannot create instance of editing composite", e);
		}
	}

	private void instantiateComposite(Composite parent) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		parent.setLayout(new FillLayout());
		editorUI = editorClass.getConstructor(Composite.class, int.class).newInstance(parent, SWT.BORDER);
	}

	@Override
	public void setFocus() {
		editorUI.setFocus();
	}

	@Override
	protected Object getEditorUI() {
		return editorUI;
	}

}
