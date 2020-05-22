package uk.ac.diamond.daq.mapping.ui.diffraction.base;

import java.util.HashSet;
import java.util.Set;

import uk.ac.diamond.daq.mapping.api.document.DetectorDocument;
import uk.ac.diamond.daq.mapping.ui.diffraction.model.MutatorType;
import uk.ac.diamond.daq.mapping.ui.diffraction.model.ShapeType;
import uk.ac.gda.api.acquisition.AcquisitionParameters;

/**
 * The base class for describe a diffraction acquisition.
 *
 *  @author Maurzio Nagni
 */
public class DiffractionParameters implements AcquisitionParameters {

	private String name;
	private ShapeType shapeType;

	private int points;

	private Set<MutatorType> mutators = new HashSet<>();

	private DetectorDocument detector;

	public DiffractionParameters() {
		super();
	}

	public DiffractionParameters(DiffractionParameters configuration) {
		super();
		this.name = configuration.getName();
		this.shapeType = configuration.getShapeType();
		this.detector = configuration.getDetector();
		this.points = configuration.getPoints();
		this.mutators = configuration.getMutators();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public Set<MutatorType> getMutators() {
		return mutators;
	}

	public void setMutators(Set<MutatorType> mutators) {
		this.mutators = mutators;
	}

	public DetectorDocument getDetector() {
		return detector;
	}

	public void setDetector(DetectorDocument detector) {
		this.detector = detector;
	}

	@Override
	public String toString() {
		return "DiffractionParameters [name=" + name + ", shapeType=" + shapeType + ", points=" + points + ", mutators="
				+ mutators + ", detector=" + detector + "]";
	}
}
