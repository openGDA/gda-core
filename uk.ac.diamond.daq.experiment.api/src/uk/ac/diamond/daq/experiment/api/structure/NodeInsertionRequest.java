package uk.ac.diamond.daq.experiment.api.structure;

import static java.util.Collections.unmodifiableMap;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.status.Status;

/**
 * A request for inserting URLs in {@link #getChildren()}
 * as children of node in {@link #getNodeLocation()},
 * which may or may not already exist.
 */
public class NodeInsertionRequest extends IdBean {

	private static final long serialVersionUID = 5720169548682575663L;

	private URL location;
	private Map<String, URL> children;

	private Status status = Status.NONE;
	private String message;


	/**
	 * Returns the URL of the node file
	 */
	public URL getNodeLocation() {
		return location;
	}

	public void setNodeLocation(URL experimentLocation) {
		this.location = experimentLocation;
	}

	/**
	 * Returns the URLs of each leaf file
	 */
	public Map<String, URL> getChildren() {
		return unmodifiableMap(children);
	}

	public void setChildren(Map<String, URL> children) {
		this.children = unmodifiableMap(children);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(children, location, message, status);
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
		NodeInsertionRequest other = (NodeInsertionRequest) obj;
		return Objects.equals(children, other.children) && Objects.equals(location, other.location)
				&& Objects.equals(message, other.message) && status == other.status;
	}

}
