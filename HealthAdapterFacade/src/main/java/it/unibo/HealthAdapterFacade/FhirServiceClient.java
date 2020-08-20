/*
 * ------------------------------------------------------------------------
 * Utility to be used internally (within the HealthProduct or appl)
 * ------------------------------------------------------------------------
 */
package it.unibo.HealthAdapterFacade;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;	
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
 

public class FhirServiceClient {
	private String serverBase=""; //"https://hapi.fhir.org/baseR4";  http://example.com/fhirBaseUrl
	private FhirContext fhirctx ;  
    // Create a client. See https://hapifhir.io/hapi-fhir/docs/client/generic_client.html
 	IGenericClient client ; //= ctx.newRestfulGenericClient(serverBase);

 /*
     REFERENCES:	
 	 For WebClient, see https://howtodoinjava.com/spring-webflux/webclient-get-post-example/ 	
	 In each method, the type of result  is MonoFlatMapMany, described in
	 https://www.javatips.net/api/reactor-core-master/src/main/java/reactor/core/publisher/MonoFlatMapMany.java	 
  */
 	
    /*
    -------------------------- 
    CLIENTS
    -------------------------- 
    */ 

 	private WebClient webClient = WebClient
	    	  .builder()
//	    	  .filters(exchangeFilterFunctions -> {
//	    	      exchangeFilterFunctions.add(logRequest());
//	    	      exchangeFilterFunctions.add(logResponse());
//	    	  })
	    	  .build();
  	
 	public FhirServiceClient( String serverBase ) {
 		this.serverBase = serverBase;
  		fhirctx         = HealthService.fhirctx;  //to avoid redundant creation FhirContext.forR4();
 		client          = fhirctx.newRestfulGenericClient(serverBase);
		System.out.println("FhirServiceClient created for " + serverBase  );
  	}

 	public FhirContext  getFhirContext() {
 		return fhirctx; 		
 	}
 	
/*
* =========================================================================
* CRUD - ASYNCH   
* =========================================================================
*/	
    /*
    -------------------------- 
    CREATE
    -------------------------- 
    */ 
	public  Flux<String> createAsynch(  String resJson  )  {			
		String resourceType = HealthService.getResourceType(resJson);  
		System.out.println( "FhirServiceClient createAsynch resourceType=" + resourceType );
		String addr = serverBase+"/"+resourceType;  
    	System.out.println("FhirServiceClient | createAsynch addr=" + addr);
     	
		Flux<String> result = webClient.post()
				.uri( addr )
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body( Mono.just(resJson), String.class )	//Since we send a string
 				.retrieve()
				.bodyToFlux(String.class);	//Since we receive a string
		return	result;		
		//return selfMadeAsynch( resourceType, "POST",   resJson );  //An old version, before to understand webClient
 	}
	
    /*
    -------------------------- 
    READ
    -------------------------- 
    */ 
	public Flux<String> readResourceAsynch( String resourceType, String id ) {
		String addr = serverBase+"/"+resourceType+"/"+id;  
    	System.out.println("FhirServiceClient | readPatientAsynch addr=" + addr);
    	Flux<String> result = webClient.get()
				.uri( addr )   
                .retrieve() 
                .bodyToFlux(String.class);
  		return result; 
 	}
	
    /*
    -------------------------- 
    SEARCH
    -------------------------- 
    */ 
 	public Flux<String> searchResourceAsynch(String resourceType, String queryStr) {
		//GET [base]/[type]?name=value&...{&_format=[mime-type]}}
		String addr = serverBase+"/"+resourceType+"/?"+queryStr;  
	   	System.out.println("FhirServiceClient | searchResourceAsynch addr=" + addr);
	    Flux<String> result = webClient.get()
				.uri( addr )   
                .retrieve() 
                .bodyToFlux(String.class);
	   	System.out.println("FhirServiceClient | searchResourceAsynch result=" + result); //result -> MonoFlatMapMany
 		return result; 
	}
    /*
    -------------------------- 
    UPDATE
    -------------------------- 
    */ 
 	public Flux<String> updateResourceAsynch( String newresourceJsonStr ) {
		String resourceType = HealthService.getResourceType(newresourceJsonStr);  
		System.out.println( "FhirServiceClient updateResourceAsynch resourceType=" + resourceType );
		String addr = serverBase+"/"+resourceType;  
    	System.out.println("FhirServiceClient | updateResourceAsynch addr=" + addr);
     	
		Flux<String> result = webClient.put()
				.uri( addr )
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body( Mono.just(newresourceJsonStr), String.class )	//Since we send a string
 				.retrieve()
				.bodyToFlux(String.class);	//Since we receive a string
		return	result;		
 		
 	}
 	
    /*
    -------------------------- 
    DELETE
    -------------------------- 
    */ 
	public Flux<String> deleteResourceAsynch( String resourceType, String id ){
//		return doAsynch( resourceType, "DELETE",   id );
		String addr = serverBase+"/"+resourceType+"/"+id;  
    	System.out.println("FhirServiceClient | deleteResourceAsynch addr=" + addr);
    	Flux<String> result = webClient.delete()
				.uri( addr )   
                .retrieve() 
                .bodyToFlux(String.class);
  		return result; 
		
	}
	
/*
 * 	A user-defined asynch interaction, by AN
 */
	public  Flux<String> selfMadeAsynch( String resourceType, String method, String body  )  { //method=POST/DELETE
		String uri         = serverBase+"/"+resourceType;
		String contentType = "application/json";  //utf-8" "plain/text; utf-8"
//		System.out.println( "post " + uri +" body=" + resJson + " contentType=" + contentType);
		System.out.println( "FhirServiceClient doAsynch " + uri + " contentType=" + contentType);
		try {
			URL url = new URL(uri);
			final HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod( method );
			con.setRequestProperty("Content-Type", contentType);
 			con.setRequestProperty("Accept", contentType);
			con.setDoOutput(true);
			System.out.println( "FhirServiceClient postAsynch " + url +" con=" + con);
//create a flux for the body that reads the answer and propagates it	 
		 Flux<String> myflux = Flux.push( (Consumer<? super FluxSink<String>>) sink -> {
		   new Thread() {
			public void run() {
				try {
					//write the body
		 			OutputStream os = con.getOutputStream();
					byte[] input    = body.getBytes("utf-8");
					os.write(input, 0, input.length);

					int status = con.getResponseCode();
					System.out.println( "FhirServiceClient postAsynch " + url +" status=" + status);
//READ THE ANSWER
					BufferedReader in = new BufferedReader(new InputStreamReader( con.getInputStream(), "utf-8"));
					String inputLine; 
					while ((inputLine = in.readLine()) != null) {
						//System.out.println( "createAsynch inputLine " + inputLine );
//PROPAGATE
						sink.next( inputLine );
					}//while
					//in.close();	
		 	  	    sink.complete();
				}catch(Exception e) {
					System.out.println( "FhirServiceClient postAsynch thread ERROR:" +e.getMessage() );			
				}		
			}//run
		   }.start();	
		 });
		 return myflux;
		}catch(Exception e) {
			System.out.println( "FhirServiceClient postAsynch ERROR" +e.getMessage() );
 			return null;
		}		
	}

}

