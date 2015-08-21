package org.opengda.lde.model.editor.ui.provider;

import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class CDateTimeCellEditor extends org.eclipse.nebula.jface.cdatetime.CDateTimeCellEditor {

	public CDateTimeCellEditor(Composite parent) {
		super(parent);
	}
	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}
	@Override
	protected Control createControl(Composite parent) {
		final CDateTime cdt = (CDateTime) super.createControl(parent);
		cdt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!cdt.isOpen()) {
					fireApplyEditorValue();
					deactivate();
				}
			}
		});
		return cdt;
	}

}
