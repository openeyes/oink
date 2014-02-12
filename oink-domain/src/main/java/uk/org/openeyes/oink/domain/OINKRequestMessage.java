package uk.org.openeyes.oink.domain;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class OINKRequestMessage extends OINKMessage {

	private String resourcePath;
	private HttpMethod method;
	private HttpHeaders headers;
	private byte[] body;

	public OINKRequestMessage() {

	}

	public OINKRequestMessage(String resourcePath, HttpMethod method,
			HttpHeaders headers, byte[] body) {
		this.resourcePath = resourcePath;
		this.method = method;
		this.headers = headers;
		if (body != null) {
			this.body = body.clone();
		}
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public byte[] getBody() {
		if (body != null) {
			return body.clone();
		} else {
			return null;
		}
	}

	public void setBody(byte[] body) {
		if (body != null) {
			this.body = body.clone();
		} else {
			this.body = null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(body);
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result
				+ ((resourcePath == null) ? 0 : resourcePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OINKRequestMessage other = (OINKRequestMessage) obj;
		if (!Arrays.equals(body, other.body))
			return false;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (method != other.method)
			return false;
		if (resourcePath == null) {
			if (other.resourcePath != null)
				return false;
		} else if (!resourcePath.equals(other.resourcePath))
			return false;
		return true;
	}

}
