package uk.ac.diamond.daq.service.rest.exception;

import javax.servlet.http.HttpServletResponse;

import uk.ac.gda.common.exception.GDAException;

/**
 * Represents a generic exception for an http component
 * 
 * @author ooy64565
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

	public GDAHttpException(int status) {
		this.status = status;
	}
	
	public GDAHttpException(String message, int status) {
		super(message);
		this.status = status;
	}
	
	public final int getStatus() {
		return status;
	}
}
