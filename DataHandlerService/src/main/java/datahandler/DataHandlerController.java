package datahandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.bson.Document;
import org.bson.types.ObjectId;
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
import org.springframework.web.util.UriUtils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

@RestController
public class DataHandlerController {
    
    @RequestMapping("/test")
    public String test() 
    {
        return "data handler service is up!";
    }
    
    @RequestMapping("/dataset")
    public String getData(@RequestParam("datasetname") String datasetName) 
    {
		byte[] data = getFile(datasetName);
		String output = new String(data, StandardCharsets.UTF_8);
        return output;
    }
    
    @RequestMapping("/list")
    public String listDatasets() 
    {
        return listMongoDBFiles();
    }
    
    @RequestMapping("/save")
    public String saveFileHandler(@RequestParam("location") String location, @RequestParam("name") String name) 
    {
    	String decoded = "empty";
    	try
    	{
    		decoded = UriUtils.decode(location, Charset.forName("UTF-8"));
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	String response = "fail";
        boolean saveFileRequest = saveFile(decoded,name);
        if(saveFileRequest)
        	response = "pass";
        return response;
    }
    
    public String listMongoDBFiles()
    {
    	MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		MongoDatabase database = mongoClient.getDatabase("testdb");
		//GridFSBucket gridFSBucket = GridFSBuckets.create(database);
		
		MongoCollection<Document> collection = database.getCollection("fs.files");
		FindIterable<Document> documents = collection.find();
		
		String init = "";
		MongoCursor<Document> iterator = documents.iterator();
		while(iterator.hasNext())
		{
			Document next = iterator.next();
			init += next.get("filename");
			init += "|";
			init += next.get("uploadDate");
			if(iterator.hasNext())
				init += "|";
		}
    	return init;
    }
    
    public byte[] getFile(String fileName)
    {
    	MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		MongoDatabase database = mongoClient.getDatabase("testdb");
		GridFSBucket gridFSBucket = GridFSBuckets.create(database);
		GridFSDownloadStream stream = gridFSBucket.openDownloadStream(fileName);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int data = stream.read();
        while (data >= 0) 
        {
            outputStream.write((char) data);
            data = stream.read();
        }
        byte[] bytesToWriteTo = outputStream.toByteArray();
        stream.close();
		mongoClient.close();
		return bytesToWriteTo;
    }
    
    public boolean saveFile(String location, String name)
    {
    	boolean status = false;
    	try 
		{
			MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
			MongoDatabase database = mongoClient.getDatabase("testdb");
			GridFSBucket gridFSBucket = GridFSBuckets.create(database);
			
			InputStream streamToUploadFrom = new FileInputStream(new File(location));
			GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(1024).metadata(new Document("type", "presentation"));
			ObjectId fileId = gridFSBucket.uploadFromStream(name, streamToUploadFrom, options);
			mongoClient.close();
			status = true;
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
    	
    	return status;
    }
}
