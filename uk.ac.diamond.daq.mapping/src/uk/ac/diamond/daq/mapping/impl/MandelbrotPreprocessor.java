package uk.ac.diamond.daq.mapping.impl;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.eclipse.scanning.example.detector.MandelbrotModel;

import gda.factory.Findable;

/**
 * A preprocessor which sets the axis names used by the MandelbrotDetector to determine its position
 *
 * @author Colin Palmer
 *
 */
public class MandelbrotPreprocessor implements IPreprocessor, Findable {

	private String name;
	private String realAxisName;
	private String imaginaryAxisName;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getRealAxisName() {
		return realAxisName;
	}

	public void setRealAxisName(String realAxisName) {
		this.realAxisName = realAxisName;
	}

	public String getImaginaryAxisName() {
		return imaginaryAxisName;
	}

	public void setImaginaryAxisName(String imaginaryAxisName) {
		this.imaginaryAxisName = imaginaryAxisName;
	}

	@Override
	public <T> ScanRequest<T> preprocess(ScanRequest<T> req) throws ProcessingException {

		for (IDetectorModel detectorModel : req.getDetectors().values()) {
			if (detectorModel instanceof MandelbrotModel) {
				MandelbrotModel mandelbrotModel = (MandelbrotModel) detectorModel;
				mandelbrotModel.setRealAxisName(realAxisName);
				mandelbrotModel.setImaginaryAxisName(imaginaryAxisName);
			}
		}
		return req;
	}
}
