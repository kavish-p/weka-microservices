package edge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class EdgeController {
	
	static String[] algorithmServices = {
			"http://localhost:9100",
			"http://localhost:9200",
			"http://localhost:9300"
			};
	
	static String databaseService = "http://localhost:9900";
    
    @RequestMapping("/test")
    public String test() 
    {
        return "edge service is up!";
    }
    
    @RequestMapping("/list")
    public String list()
    {
    	String response = "Database service is down!";
    	try
		{
			RestTemplate restTemplate = new RestTemplate();
	        response = restTemplate.getForObject(databaseService+"/list", String.class);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        return response;
    }
    
    @RequestMapping("/compute")
    public String compute(@RequestParam("datasetname") String datasetName) 
    {
    	String results = aggregateResults(datasetName);
		return results;
    }
    
    public boolean isUp(String service)
    {
    	boolean up = true;
		try
		{
			RestTemplate restTemplate = new RestTemplate();
	        restTemplate.getForObject(service+"/test", String.class);
		}
		catch(Exception e)
		{
			up = false;
		}
		return up;
    }
    
    public String aggregateResults(String datasetName)
    {
    	//String results = "Results\n";
    	String results = "";
    	for(String service:algorithmServices)
    	{
    		if(isUp(service))
    		{
    			RestTemplate restTemplate = new RestTemplate();
    	        String response = restTemplate.getForObject(service+"/execute?datasetname="+datasetName, String.class);
    	        results += response + "\n\n";
    		}
    	}
    	return results;
    }
    
    
}
