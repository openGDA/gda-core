package org.opengda.lde.ui.utils;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.internal.ImageCycleFeedbackBase;

@SuppressWarnings("restriction")
public class AnimatedTableItemFeedback extends ImageCycleFeedbackBase {

	private TableItem tableItem;
	private int columnIndex;

	public AnimatedTableItemFeedback(Shell parentShell, Image[] images, TableItem tableItem, int columnIndex) {
		super(parentShell, images);
		this.tableItem=tableItem;
		this.columnIndex=columnIndex;
	}

	@Override
	public void showImage(Image image) {
		if (!tableItem.isDisposed()) {
			tableItem.setImage(columnIndex,image);
		}
	}

	@Override
	public void saveStoppedImage() {
		stoppedImage=tableItem.getImage(columnIndex);
	}

	@Override
	public void setStoppedImage(Image image) {
		tableItem.setImage(columnIndex,image);
	}

	@Override
	public void initialize(AnimationEngine animationEngine) {
		background=tableItem.getParent().getBackground();
		display=tableItem.getParent().getDisplay();
	}
}
