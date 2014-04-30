package uk.org.openeyes.oink.facade;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:camel-context-test.xml" })
public class TestFacadeRoute {

	private static Properties testProperties;

	private static ConnectionFactory factory;

	private final static String THIRD_PARTY_QUEUE_NAME = "siteB";

	@Autowired
	CamelContext camelCtx;

	@BeforeClass
	public static void setUp() throws IOException {
		// Load properties
		testProperties = new Properties();
		InputStream is = TestFacadeRoute.class
				.getResourceAsStream("/facade-test.properties");
		testProperties.load(is);

		// Prepare RabbitMQ Client
		factory = new ConnectionFactory();
		factory.setHost(testProperties.getProperty("rabbit.host"));
		factory.setPort(Integer.parseInt(testProperties
				.getProperty("rabbit.port")));
		factory.setUsername(testProperties.getProperty("rabbit.username"));
		factory.setPassword(testProperties.getProperty("rabbit.password"));
		factory.setVirtualHost(testProperties.getProperty("rabbit.vhost"));

	}

	@Test
	@DirtiesContext
	public void testRequestFailsOnMissingAuthenticationHeader()
			throws HttpException, IOException {

		// Prepare request
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");

		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, method.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testRequestFailsOnInvalidCredentials() throws HttpException,
			IOException {

		// Prepare request
		HttpClient client = new HttpClient();

		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				"wrongusernameandformat",
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));
		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, method.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testAuthenticationCanSucceed() throws HttpException,
			IOException {

		// Prepare request
		HttpClient client = new HttpClient();

		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				testProperties.getProperty("testUser.username"),
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));

		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertNotEquals(HttpStatus.SC_UNAUTHORIZED,
				method.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testSimplePatientGet() throws HttpException,
			IOException, ShutdownSignalException, ConsumerCancelledException,
			InterruptedException {

		// Mock third party service
		IncomingMessageVerifier v = new IncomingMessageVerifier() {
			@Override
			public boolean isValid(OINKRequestMessage incoming) {
				return true;
			}
		};
		OINKResponseMessage mockResponse = new OINKResponseMessage(200);
		
		SimulatedThirdParty thirdp = new SimulatedThirdParty(v, mockResponse);
		thirdp.start();

		// Prepare request
		HttpClient client = new HttpClient();

		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				testProperties.getProperty("testUser.username"),
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));

		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		thirdp.close();

		Assert.assertEquals(HttpStatus.SC_OK, method.getStatusCode());
	}
	
	private interface IncomingMessageVerifier {
		public boolean isValid(OINKRequestMessage incoming);
	}

	private class SimulatedThirdParty extends Thread {
		
		private IncomingMessageVerifier verifier;
		private OINKResponseMessage messageToReplyWith;
		
		public SimulatedThirdParty(IncomingMessageVerifier verifier, OINKResponseMessage messageToReplyWith) {
			super();
			this.verifier = verifier;
			this.messageToReplyWith = messageToReplyWith;
		}

		boolean isRunning = true;

		@Override
		public void run() {
			try {
				simulateThirdParty();
			} catch (Exception e) {

			}
		}

		public void close() {
			isRunning = false;
		}

		public void simulateThirdParty() throws IOException,
				ShutdownSignalException, ConsumerCancelledException,
				InterruptedException {
			// Build consumer
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(THIRD_PARTY_QUEUE_NAME, false, false, true,
					null);
			channel.queueBind(THIRD_PARTY_QUEUE_NAME,
					testProperties.getProperty("rabbit.defaultExchange"),
					THIRD_PARTY_QUEUE_NAME);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(THIRD_PARTY_QUEUE_NAME, true, consumer);

			while (isRunning) {
				// Get delivery (timeout if necessary)
				QueueingConsumer.Delivery delivery = consumer
						.nextDelivery(5000);

				if (delivery != null) {
					isRunning = false;
				} else {
					continue;
				}

				BasicProperties props = delivery.getProperties();

				// Extract request message
				OINKRequestMessage message = camelCtx
						.getTypeConverter()
						.convertTo(OINKRequestMessage.class, delivery.getBody());
				
				// Check is valid
				Assert.assertTrue(verifier.isValid(message));

				// Prepare an empty response
				com.rabbitmq.client.AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();

				byte[] responseBody = camelCtx.getTypeConverter().convertTo(
						byte[].class, messageToReplyWith);

				channel.basicPublish(
						testProperties.getProperty("rabbit.defaultExchange"),
						props.getReplyTo(), replyProps, responseBody);
			}
			connection.close();
		}

	}

}
