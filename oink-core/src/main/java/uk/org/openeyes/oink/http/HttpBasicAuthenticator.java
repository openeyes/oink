package uk.org.openeyes.oink.http;

import javax.security.auth.Subject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import uk.org.openeyes.oink.security.SecurityException;

/**
 * A {@link Processor}-like bean that prepares a request for Spring Security inspection.
 * 
 * @author Oliver Wilkie
 */
public class HttpBasicAuthenticator {
	
	private Logger logger = LoggerFactory.getLogger(HttpBasicAuthenticator.class);

	public void extractAuthenticationDetailsFromHttp(Exchange exchange)
			throws SecurityException {
		// get the username and password from the HTTP header
		// http://en.wikipedia.org/wiki/Basic_access_authentication
		String authorizationHeader = exchange.getIn().getHeader(
				"Authorization", String.class);
		if (authorizationHeader == null) {
			throw new SecurityException(
					"No HttpBasic Authorization Header was found in the request");
		}
		String basicPrefix = "Basic ";
		String userPassword = authorizationHeader.substring(basicPrefix
				.length());
		byte[] header = Base64.decodeBase64(userPassword.getBytes());
		if (header == null) {
			throw new SecurityException(
					"Invalid Http Basic Authorization Header found in the request");
		}
		String userpass = new String(header);
		String[] tokens = userpass.split(":");

		// create an Authentication object
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				tokens[0], tokens[1]);

		// wrap it in a Subject
		Subject subject = new Subject();
		subject.getPrincipals().add(authToken);

		// place the Subject in the In message
		exchange.getIn().setHeader(Exchange.AUTHENTICATION, subject);
		
		logger.debug("Found HttpBasic Authentication header");

		// Spring security will intercept this and authenticate

	}

}
