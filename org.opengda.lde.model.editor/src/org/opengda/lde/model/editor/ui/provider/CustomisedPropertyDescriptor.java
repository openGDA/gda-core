package org.opengda.lde.model.editor.ui.provider;

import java.util.Date;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.nebula.widgets.datechooser.DateChooserComboCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * provide customised property editor for {@link Date} defined in an EMF model as {@link EDataType}.
 * It uses Nebula's {@linkplain DateChooserComboCellEditor} to set date value.
 * @author fy65
 *
 */
public class CustomisedPropertyDescriptor extends PropertyDescriptor {

	public CustomisedPropertyDescriptor(Object object, IItemPropertyDescriptor itemPropertyDescriptor) {
		super(object, itemPropertyDescriptor);
	}

	@Override
	protected CellEditor createEDataTypeCellEditor(EDataType eDataType, Composite composite) {
		if (eDataType.getInstanceClass()==Date.class) {
			DateChooserComboCellEditor dateChooserComboCellEditor = new DateChooserComboCellEditor(composite);
			DateChooserCombo combo = dateChooserComboCellEditor.getCombo();
			combo.setFooterVisible(true);
//			combo.setWeeksVisible(true);
			return dateChooserComboCellEditor;
		}
		return super.createEDataTypeCellEditor(eDataType, composite);
	}
}
