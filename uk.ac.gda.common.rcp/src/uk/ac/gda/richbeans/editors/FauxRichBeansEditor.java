/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

/**
 * This class intercepts methods which would otherwise trigger RichBeans binding events.
 * Consequently, binding is now your responsibility. Remember to call {@link #beanChanged()} after
 * your bean is modified.
 * <p>
 * Bear in mind that if this part is instantiated by a subclass of {@link RichBeanMultiPageEditorPart},
 * you may want to override createPage1() in said subclass returning {@code null} to suppress creation of XML page.
 * This is because that view will still rely on RichBeans bindings and therefore will not represent your model.
 * @param <B> type of bean this editor edits
 */
public abstract class FauxRichBeansEditor<B> extends RichBeanEditorPart {

	public FauxRichBeansEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, B editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		bean = editingBean;
	}

	private B bean;

	protected B getBean() {
		return bean;
	}

	/**
	 * UI parts should call this method whenever the bean is modified
	 * to mark the editor as dirty and therefore allow saving of underlying XML file
	 */
	protected void beanChanged() {
		dirtyContainer.setDirty(true);
	}

	// RichBeans intercepts

	@Override
	public void linkUI(boolean isPageChange) {
		// no rich beans thank you
	}

	@Override
	protected void uiToBean() throws Exception {
		// no rich beans thank you
	}

	@Override
	protected void beanToUI() throws Exception {
		// no rich beans thank you
	}

	@Override
	public Object updateFromUIAndReturnEditingBean() throws Exception {
		// no rich beans thank you
		return bean;
	}

	@Override
	protected void updateUiFromOtherBean(Object otherBean) throws Exception {
		// no rich beans thank you
	}
}
