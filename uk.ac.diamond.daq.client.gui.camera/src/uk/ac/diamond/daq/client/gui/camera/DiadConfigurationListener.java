package uk.ac.diamond.daq.client.gui.camera;

public interface DiadConfigurationListener<T extends DiadConfigurationModel> {
	T getModel();

	void setModel(T data);
}
