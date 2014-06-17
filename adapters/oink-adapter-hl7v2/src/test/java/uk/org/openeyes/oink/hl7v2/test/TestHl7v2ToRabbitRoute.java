/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.hl7v2.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.JSONException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.test.RabbitTestUtils;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2ToRabbitRoute extends Hl7TestSupport {
	
	@BeforeClass
	public static void before() throws IOException {
		Properties props = new Properties();
		InputStream is = TestHl7v2ToRabbitRoute.class.getResourceAsStream("/hl7v2-test.properties");
		props.load(is);
		Assume.assumeTrue("No RabbitMQ Connection detected", RabbitTestUtils.isRabbitMQAvailable(props));
	}
	
	@Before
	public void setUp() throws IOException {
		setProperties("/hl7v2-test.properties");
	}
	
	@Ignore
	@Test
	public void testIncomingA28IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/example-messages/hl7v2/A28-1.txt", "/example-messages/oinkrequestmessages/A28-1.json");
	}
	
	@Ignore
	@Test
	public void testIncomingA01IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A01.txt", "/oinkrequestmessages/A01.json");
	}

	@Ignore
	@Test
	public void testIncomingA05IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A05.txt", "/oinkrequestmessages/A05.json");
	}

	@Ignore
	@Test
	public void testIncomingA31IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A31-2.txt", "/oinkrequestmessages/A31-2.json");
	}
	

}
