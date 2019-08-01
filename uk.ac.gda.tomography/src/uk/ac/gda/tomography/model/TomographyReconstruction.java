package uk.ac.gda.tomography.model;

/**
 * The base class for Tomography Acquisition metadata.
 * This class DOES NOT specify a tomographyReconstruction but only a set of metadata that the users can add
 * before the tomography acquisition starts.
 *
 * @author Maurzio Nagni
 */
public class TomographyReconstruction implements AcquisitionConfiguration {

	private int pixelSizeX;
	private int pixelSizeY;

	public TomographyReconstruction() {
		super();
	}

	public TomographyReconstruction(TomographyReconstruction reconstruction) {
		super();
		this.pixelSizeX = reconstruction.getPixelSizeX();
		this.pixelSizeY = reconstruction.getPixelSizeY();
	}

	public int getPixelSizeX() {
		return pixelSizeX;
	}

	public void setPixelSizeX(int pixelSizeX) {
		this.pixelSizeX = pixelSizeX;
	}

	public int getPixelSizeY() {
		return pixelSizeY;
	}

	public void setPixelSizeY(int pixelSizeY) {
		this.pixelSizeY = pixelSizeY;
	}
}
