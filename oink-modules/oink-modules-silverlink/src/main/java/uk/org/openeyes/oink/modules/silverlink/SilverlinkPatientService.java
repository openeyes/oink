package uk.org.openeyes.oink.modules.silverlink;

import uk.org.openeyes.oink.domain.OINKResponseMessage;


public interface SilverlinkPatientService {
	
	OINKResponseMessage executeOINKRequest(OINKResponseMessage request); 

}
