package uk.org.openeyes.oink.fhir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Converter;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.hl7.fhir.instance.model.AtomFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class FhirConverter {

	private final JsonParser jsonParser;
	private final XmlParser xmlParser;
	
	private static final Logger log = LoggerFactory.getLogger(FhirConverter.class);

	public FhirConverter() {
		xmlParser = new XmlParser();
		jsonParser = new JsonParser();
	}

	@Converter
	public AtomFeed fromJsonOrXml(String input) throws FhirConversionException {
		AtomFeed f = null;
		try {
			InputStream is = new ByteArrayInputStream(input.getBytes());
			f = xmlParser.parseGeneral(is).getFeed();
		} catch (Exception e) {
			InputStream is = new ByteArrayInputStream(input.getBytes());
			try {
				ResourceOrFeed resOrFeed = jsonParser.parseGeneral(is);
				f = resOrFeed.getFeed();
			} catch (Exception e1) {
				log.error(e1.toString());
				throw new FhirConversionException();
			}
		}
		return f;
	}
	
	public AtomFeed fromXmlToBundle(String xml) throws Exception {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		AtomFeed f = xmlParser.parseGeneral(is).getFeed();
		return f;
	}

}
