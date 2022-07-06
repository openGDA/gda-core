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

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.richbeans.CompositeFactory;

/**
 * A bean editor part that delegates to a composite created by a CompositeFactory.
 * <p>
 * This is intended to be used in a RichBeanMultiPageEditorPart. You must call setRichEditorTabText(...) just after the part is instantiated. If you are keeping
 * a reference to the editing bean and need to respond when the bean in the editor is changed, override {@link RichBeanMultiPageEditorPart#linkUI()}, which is
 * called after an input change.
 * <p>
 * Usage:
 * <p>
 * <usage><code>
  DelegatingRichBeanEditorPart ed = new DelegatingRichBeanEditorPart(path, mappingUrl, dirtyContainer, editingBean, compositeFactory);
  ed.setRichEditorTabText(editorTabName);
  </code></usage>
 */
public class DelegatingRichBeanEditorPart extends RichBeanEditorPart {

	private CompositeFactory uiProvider;
	private Composite editorUI;
	private String tabText;
	private boolean enableScrolling = false;

	public DelegatingRichBeanEditorPart(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean, CompositeFactory uiProvider) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.uiProvider = uiProvider;
	}

	/**
	 * Please call shortly after creation
	 */
	public void setScrollable(boolean enableScrolling) {
		this.enableScrolling = enableScrolling;
	}

	/**
	 * Please call shortly after creation.
	 *
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
		if (enableScrolling) {
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
	}

	private void instantiateComposite(Composite parent) {
		parent.setLayout(new FillLayout());
		editorUI = uiProvider.createComposite(parent, SWT.NONE);
		createDataBindingController();
	}

	@Override
	public void setFocus() {
		editorUI.setFocus();
	}

	@Override
	protected Object getEditorUI() {
		return editorUI;
	}

	@Override
	public void dispose() {
		super.dispose();
		editorUI.dispose();
	}
}
