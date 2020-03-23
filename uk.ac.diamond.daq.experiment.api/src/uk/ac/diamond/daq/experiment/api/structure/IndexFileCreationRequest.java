package uk.ac.diamond.daq.experiment.api.structure;

import java.net.URL;
import java.util.Set;

/**
 * A request to create an index NeXus file which links to all the acquisitions
 * performed dURLng the experiment.
 */
public class IndexFileCreationRequest extends ExperimentStructureJobRequest {

	private static final long serialVersionUID = 5720169548682575663L;

	private String experimentName;
	private URL experimentLocation;
	private Set<URL> acquisitions;

	private URL indexFileLocation;


	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public URL getExperimentLocation() {
		return experimentLocation;
	}

	public void setExperimentLocation(URL experimentLocation) {
		this.experimentLocation = experimentLocation;
	}

	public Set<URL> getAcquisitions() {
		return acquisitions;
	}

	public void setAcquisitions(Set<URL> acquisitions) {
		this.acquisitions = acquisitions;
	}

	public URL getIndexFileLocation() {
		return indexFileLocation;
	}

	public void setIndexFileLocation(URL indexFileLocation) {
		this.indexFileLocation = indexFileLocation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((acquisitions == null) ? 0 : acquisitions.hashCode());
		result = prime * result + ((experimentLocation == null) ? 0 : experimentLocation.hashCode());
		result = prime * result + ((experimentName == null) ? 0 : experimentName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexFileCreationRequest other = (IndexFileCreationRequest) obj;
		if (acquisitions == null) {
			if (other.acquisitions != null)
				return false;
		} else if (!acquisitions.equals(other.acquisitions))
			return false;
		if (experimentLocation == null) {
			if (other.experimentLocation != null)
				return false;
		} else if (!experimentLocation.equals(other.experimentLocation))
			return false;
		if (experimentName == null) {
			if (other.experimentName != null)
				return false;
		} else if (!experimentName.equals(other.experimentName))
			return false;
		return true;
	}

}
