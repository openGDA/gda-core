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

package uk.ac.gda.richbeans.xml;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.file.FileBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.components.wrappers.PrintfWrapper;
import uk.ac.gda.richbeans.components.wrappers.RadioWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 * Convenience class for accessing the current recommended implementation
 * of IFieldWiget for a particular field.
 */
public class XMLFieldWidgetFactory {

	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createScaleBox(final Composite parent, final int style) {
		return new ScaleBox(parent, style);
	}
	
	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createFileBox(final Composite parent, final int style) {
		return new FileBox(parent, style);
	}
	
	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createBoolean(final Composite parent, final int style) {
		return new BooleanWrapper(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @param visibleChoices 
	 * @return IFieldWidget
	 */
	public static FieldComposite createComboAndNumber(final Composite parent, final int style, List<String> visibleChoices) {
		return new ComboAndNumberWrapper(parent, style, visibleChoices);
	}
	
	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createCombo(final Composite parent, final int style) {
		return new ComboWrapper(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createLabel(final Composite parent, final int style) {
		return new LabelWrapper(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createPrintf(final Composite parent, final int style) {
		return new PrintfWrapper(parent, style);
	}
	

	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createSpinner(final Composite parent, final int style) {
		return new SpinnerWrapper(parent, style);
	}


	/**
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createText(final Composite parent, final int style) {
		return new TextWrapper(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @param items 
	 * @return IFieldWidget
	 */
	public static Group createRadio(final Composite parent, final int style, final String[] items) {
		return new RadioWrapper(parent, style, items);
	}

	/**
	 * NOTE: The editor returned is not ready and requires further setup such as 
	 * providing the bean and the component to edit the bean.
	 * 
	 * @param parent
	 * @param style
	 * @return IFieldWidget
	 */
	public static FieldComposite createBeanList(final Composite parent, final int style) {
		return new VerticalListEditor(parent, style);
	}
	
	/**
	 * NOTE: The editor returned is not ready and requires further setup such as 
	 * providing the bean and the component to edit the bean.
	 * 
	 * @param parent
	 * @param style
	 * @param cols 
	 * @param rows 
	 * @return IFieldWidget
	 */
	public static FieldComposite createGirdList(final Composite parent, final int style, final int cols, final int rows) {
		return new GridListEditor(parent, style, cols, rows);
	}

}
