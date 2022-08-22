package uk.ac.diamond.daq.experiment.api.plan;

public interface PayloadService {


	<P extends Payload> P wrap(Object rawPayload);


	<P extends Payload> Object handle(P payload);

}
