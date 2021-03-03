package uk.ac.diamond.daq.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * 
 * 
 * @author Maurizio Nagni
 *
 */
@Component
public class ServiceUtils {

	/**
	 * Writes a list of documents into the {@link HttpServletResponse}
	 * @param <T>
	 * @param document
	 * @param outputStrategy
	 * @param response
	 * @throws GDAServiceException
	 */
	public <T> void writeOutput(List<T> document, OutputStrategy<T> outputStrategy,  final HttpServletResponse response) throws GDAServiceException {
		writeOutput(outputStrategy.write(document), response);
	}
	
	/**
	 * Writes a single document into the {@link HttpServletResponse} 
	 * @param <T>
	 * @param document
	 * @param outputStrategy
	 * @param response
	 * @throws GDAServiceException
	 */
	public <T> void writeOutput(T document, OutputStrategy<T> outputStrategy,  final HttpServletResponse response) throws GDAServiceException {
		writeOutput(outputStrategy.write(document), response);
	}
	
	/**
	 * Writes a {@code byte[]} into the {@link HttpServletResponse} 
	 * @param output
	 * @param response
	 * @throws GDAServiceException
	 */
	public void writeOutput(byte[] output, final HttpServletResponse response) throws GDAServiceException {
		try {
			response.getOutputStream().write(output);
		} catch (IOException e) {
			throw new GDAServiceException("Cannot write http response", e);
		}		
	}
	
}
