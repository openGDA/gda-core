package uk.ac.gda.beans.exafs;

public interface IExperimentDetectorParameters {

	Double getWorkingEnergy();

	String getDetectorType();

	boolean isCollectDiffractionImages();

	double getMythenEnergy();

	double getMythenTime();

	int getMythenFrames();

}
