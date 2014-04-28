package uk.org.openeyes.oink.facade;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

public interface RoutingService {
	
	public RabbitRoute getRouting(String path, HttpMethod method);
	
	public String getReplyRoutingKey(String path, HttpMethod method);

}