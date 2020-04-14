package uk.ac.diamond.daq.experiment.api.structure;

import static java.util.Collections.unmodifiableSet;

import java.net.URL;
import java.util.Set;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.status.Status;

/**
 * A request for the creation of a node file which consists of a single entry
 * composed of links to the given children
 */
public class NodeFileCreationRequest extends IdBean {

	private static final long serialVersionUID = 5720169548682575663L;

	private URL location;
	private Set<URL> children;

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
	public Set<URL> getChildren() {
		return unmodifiableSet(children);
	}

	public void setChildren(Set<URL> children) {
		this.children = unmodifiableSet(children);
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
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		NodeFileCreationRequest other = (NodeFileCreationRequest) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return status == other.status;
	}

}
