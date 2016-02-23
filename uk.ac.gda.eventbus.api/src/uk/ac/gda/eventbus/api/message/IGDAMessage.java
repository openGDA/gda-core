package uk.ac.gda.eventbus.api.message;

import java.io.Serializable;

import com.google.common.base.Optional;

public interface IGDAMessage extends Serializable {
	
	GDAMessageCategory getCategory();

	String getMessage();

	Optional<?> getSourceToken();

	void setCategory(GDAMessageCategory category);

	void setMessage(String message);

	void setSourceToken(Object sourceToken);

}