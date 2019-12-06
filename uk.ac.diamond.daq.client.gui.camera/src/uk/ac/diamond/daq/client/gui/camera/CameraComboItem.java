package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.widgets.Combo;

/**
 * Represents an camera item in a {@link Combo}.
 * <br/>
 * <bold>IMPORTANT</bold>  
 * For now instances of this class are created by {@link CameraHelper#getCameraComboItems()}
 * consequently the {@link #getIndex()} returns the same index as in the client configuration file. 
 * 
 * @author Maurizio Nagni
 */
public class CameraComboItem {

	/**
	 * The item index in the combo.
	 */
	private final int index;
	/**
	 * The label used in the combo
	 */
	private final String name;

	CameraComboItem(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}
}