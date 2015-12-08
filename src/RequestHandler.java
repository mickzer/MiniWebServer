import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.nio.file.NoSuchFileException;

public class RequestHandler extends Thread {

	Socket s;
	BufferedReader in;
	BufferedWriter out;
	String method;
	String uri;
	String version;
	HashMap<String, String> headers;
	String body;
	
	static final String basePath = "html";
	static final List<String> allowedRequestMethods = Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE");
	
	public RequestHandler(Socket s) {
		
		this.s = s;
		
		//Get the input stream
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//get the output stream
		try {
			out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//initalize stuff
		headers = new HashMap<String, String>();
		
	}
	
	public void run() {
		
		try {
			parseRequest();
		} catch (BadRequestException e) {
			returnResponse(new HttpResponse(400, "text/plain"));
		} catch(NotImplementedException e) {
			returnResponse(new HttpResponse(501, "text/plan"));
		} catch (Exception e) {
			returnResponse(new HttpResponse(500, "text/plain"));
		}
	
		//do the thing
		try {
			switch(method) {
				case "GET":
					doGet();
					break;
				case "POST":
					doPost();
					break;
				case "PUT":
					doPut();
					break;
				case "DELETE":
					doDelete();
					break;
			}
		} catch(MethodNotAllowedException e) {
			returnResponse(new HttpResponse(405, "text/plain"));
		} catch(Exception e) {
			e.printStackTrace();
			returnResponse(new HttpResponse(500, "text/plan"));
		}
				
	}
	
	public void doGet() throws Exception {
		HttpResponse r; 
		try {
			//get file
			r = new HttpResponse();
			r.setBodyFromFile(uri);
			r.setStatusCode(200);
			returnResponse(r);
		} catch (NoSuchFileException e) {
			returnResponse(new HttpResponse(404, "text/plain"));
		}
		
	}
	
	public void doPost() throws Exception {
		for(String key : headers.keySet()) 
			System.out.println(key + " -> " + headers.get(key));
		//read the body
		readBody();
		HttpResponse r; 
		//get file
		r = new HttpResponse();
		r.setStatusCode(200);
		
		//silly simple example for form data
		if(headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
			HashMap<String, String> params = parseFormData(body);
			String str = "Received Data:\n";
			for(String key : params.keySet()) {
				str += key + " -> " + params.get(key);
			}
			r.setBody(str);
		}
		
		returnResponse(r);
		
	}
	
	public void doPut() throws Exception {
		throw new MethodNotAllowedException("PUT is not allowed for this endpoint.");
	}
	
	public void doDelete() throws Exception {
		throw new MethodNotAllowedException("DELETE is not allowed for this endpoint.");
	}
		
	public void returnResponse(HttpResponse response) {
		try {
			out.write(response.toString());
			out.flush();
			out.close();
			s.close();
		} catch (IOException e) {
			System.out.println("IT BLEW UP:");
			e.printStackTrace();
		}
	}
	
	public void parseRequest() throws IOException, NotImplementedException, BadRequestException {
		
		parseRequestLine();
		if(allowedRequestMethods.contains(method)) {
			parseHeaders();
		}else {
			throw new NotImplementedException("Method not supported");
		}
	
	}
	
	public void parseRequestLine() throws BadRequestException {
		try {
			//splits string on whitespace characters
			String[] requestLine = in.readLine().split("\\s+");
			if(requestLine.length != 3) 
				throw new BadRequestException("Invalid request line");
			this.method = requestLine[0];
			//redirect / to /index.html
			if(requestLine[1].charAt(requestLine[1].length()-1) == '/') 
				uri = basePath + "/index.html";
			else
				this.uri = basePath + requestLine[1];
			this.version = requestLine[2];
		} catch (IOException e) {
			throw new BadRequestException("Request is empty");
		}
	}
	
	public void parseHeaders() throws BadRequestException {
		try {
			//read up until we hit an empty line to get the headers
			String header;
			header = in.readLine();
			while(header.length() > 0) {
				//add the header
				addHeader(header);
				header = in.readLine();
			}
			
		} catch (IOException e) {
			throw new BadRequestException("Invalid headers");
		}
		
	}
	
	public void addHeader(String header) throws BadRequestException {
		try {
			int split = header.indexOf(":");
			if(split > -1)
				headers.put(header.substring(0, split), header.substring(split+2, header.length())); //+2 to get rid of whitespace
			else
				throw new Exception();
		} catch (Exception e) {
			throw new BadRequestException("Invalid header: " + header);
		}
		
	}
	
	public void readBody() throws Exception {
		body = "";
		for(int i=0; i<Integer.parseInt(headers.get("Content-Length")); ++i) 
			body += (char)in.read();
		System.out.println(body);
	}
	
	public static HashMap<String, String> parseFormData(String str) throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] pairs = str.split("&");
		for(String pair : pairs) 
			map.put(pair.split("=")[0], pair.split("=")[1]);
		return map;
	}
	
}
