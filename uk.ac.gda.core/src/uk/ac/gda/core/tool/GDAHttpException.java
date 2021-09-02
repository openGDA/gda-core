package uk.ac.gda.core.tool;

import javax.servlet.http.HttpServletResponse;

import uk.ac.gda.common.exception.GDAException;

/**
 * Represents a generic exception for an http component
 *
 * @author Maurizio Nagni
 *
 */
public class GDAHttpException extends GDAException {

	/**
	 *
	 */
	private static final long serialVersionUID = 2668865925369474176L;
	/**
	 * The HttpServletResponse status
	 */
	private int status = HttpServletResponse.SC_PRECONDITION_FAILED;

	/**
	 * Any GDA internal code to identify a specific error type
	 */
	private final String type;

	public GDAHttpException(int status) {
		this.status = status;
		this.type = null;
	}

	public GDAHttpException(String message, int status) {
		super(message);
		this.status = status;
		this.type = null;
	}

	public GDAHttpException(String message, int status, String type) {
		super(message);
		this.status = status;
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}
}
