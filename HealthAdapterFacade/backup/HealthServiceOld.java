package it.unibo.HealthAdapterFacade;
import java.io.StringReader;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
/*
 * ------------------------------------------------------------------------
 * Used by the RestController HealthAdapterMIController 
 * ------------------------------------------------------------------------
 */
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
 

@Service
public class HealthServiceOld {
	
	public static enum HealthCenterType{ FHIR, HL7, OTHER};
	public final static FhirContext fhirctx = FhirContext.forR4();
/*
 * --------------------------------------------------------------------------
 * WARNING: these URI are used in templates\indexHealthAdapterFacade.html	
 * --------------------------------------------------------------------------
 */
	public static final String selectHealthCenterUri    ="/selectHealthCenter";

	public static final String createPatientSynchUri  	="/createPatientSynch";  //
	public static final String searchPatientUri  		="/searchPatientSynch";
	public static final String readResourceSynchUri 	="/readResourceSynch";
	public static final String deleteResourceSynchUri   ="/deleteResourceSynch";

	public static final String createResourceUri   		="/createResource";
	public static final String readResourceUri   		="/readResource";
	public static final String searchResourcetUri  		="/searchResource";
	public static final String deleteResourceUri 		="/deleteResource";
	
	/*
	 * UTILITIES : CONVERSIONS	
	 */
	 	public static String cvt( IBaseResource theResource,  boolean useJson ) {
	 		if( useJson ) return cvtJson(theResource);
	 		else return cvtXml(theResource);
	 	}
	 	public static String cvt( Bundle bundle,  boolean useJson ) {
	 		if( useJson ) return fhirctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
	 		else return fhirctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle);
	 	}

	 	public static String cvtJson( IBaseResource theResource ) {
	 		return fhirctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(theResource);
	 	}

	 	public static String cvtXml( IBaseResource theResource ) {
	 		return fhirctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(theResource);
	 	}
	 	
	 	public static String prettyJson(String jsonStr) {
	 		JSONObject json;
			try {
				json = new JSONObject(jsonStr);
				return json.toString(2);
			} catch (JSONException e) {
				return "prettyJson ERROR for"+jsonStr;
 			}	 		
	 	}
	
/*
 * THE COMPONENT	
 */
	private HealthServiceInterface service;
	
	public HealthServiceOld() {	//Autoconfiguration, just to start ...		
 		System.out.println("HealthService Autoconfiguration " );
		buildHealthService( HealthCenterType.FHIR, "https://hapi.fhir.org/baseR4" );
	}
	
	public HealthServiceInterface setHealthService(String choice, String serverAddr) { 
		if( choice.equals("FHIR") ) buildHealthService( HealthCenterType.FHIR, serverAddr );
		else if( choice.equals("HL7") ) buildHealthService( HealthCenterType.HL7, serverAddr );
		return getdHealthService();
	}
	
	public HealthServiceInterface getdHealthService() { return service; }
	
	//Called by a system configuration
	public void buildHealthService( HealthCenterType hct, String serverBase ) {
		System.out.println("buildHealthService " + hct + " serverBase=" + serverBase);
		switch( hct ) { 
			case FHIR  : service =  new HealthServiceFhir( serverBase );break;  
			case HL7   : service =  new HealthServiceHL7( serverBase );break;
			default    : service =  new HealthServiceFhir( serverBase );
		}		
	}
	
}
