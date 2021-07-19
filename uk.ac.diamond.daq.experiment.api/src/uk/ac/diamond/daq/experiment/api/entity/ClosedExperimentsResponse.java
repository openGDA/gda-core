package uk.ac.diamond.daq.experiment.api.entity;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Returns a collection of {@link URL}s pointing to the closed experiments index files
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ClosedExperimentsResponse.Builder.class)
public class ClosedExperimentsResponse {

	private final List<URL> indexes;

	public ClosedExperimentsResponse(List<URL> indexes) {
		super();
		this.indexes = indexes;
	}

	/**
	 * The closed experiments index files
	 *
	 * @return a collection of {@link URL}, eventually empty
	 */
	public List<URL> getIndexes() {
		return indexes;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private List<URL> indexes = Collections.emptyList();

		public void withIndexes(List<URL> indexes) {
			this.indexes = Optional.ofNullable(indexes)
					.orElseGet(Collections::emptyList);
		}

	    public ClosedExperimentsResponse build() {
	        return new ClosedExperimentsResponse(indexes);
	    }
	}
}
