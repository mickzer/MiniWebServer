import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.nio.file.NoSuchFileException;


public class HttpResponse {
	
	private Integer statusCode;
	private String statusLine;
	private HashMap<String, String> headers;
	private String body;
	
	public HttpResponse() {		
		
		headers = new HashMap<String, String>();
		//create date header
		headers.put("Date", getServerTime());
		headers.put("Server", "Awesome Server 0.1");
		
		body = "";
		
	}
	
	public HttpResponse(int code, String contentType) {
		this();
		setStatusCode(code);
		setContentType(contentType);
		setBody(body);
	}
	
	public void setStatusCode(int code) throws InvalidParameterException {
		this.statusCode = code;
		String s = HttpResponseBank.getMessage(statusCode);
		if(s == null)
			throw new InvalidParameterException("Invalid response code");
		statusLine = "HTTP/1.1 " + statusCode + " " + s;
	}
	
	//explicity writing a function for this header for resuse
	private void setContentType(String contentType) {
		headers.put("Content-Type", contentType);
	}
	
	public void setBodyFromFile(String filePath) throws NoSuchFileException, Exception {
		//get the file
		byte[] encoded = Files.readAllBytes(Paths.get(filePath));
		//tell the client it's size
		headers.put("Content-Length", ""+encoded.length);
		//unreliably get the MimeType of the file thanks to Servoy development
		String mimeType = MimeTypes.getContentType(encoded);
		//if it can't determine the mime type, default to text/plain
		//as this is a pretty way of doing things anyway 
		if(mimeType == null) mimeType = "text/plain";
		setContentType(mimeType);
		body = new String(encoded, StandardCharsets.UTF_8);
	}
	
	public void setBody(String s)  {
		body = s;
	}
	
	private String getServerTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    return dateFormat.format(calendar.getTime());
	}
	
	//return the response as a string for transmission
	public String toString() throws IllegalStateException {
		//status must be set to convert the response to a string
		if(statusCode == null)
			throw new IllegalStateException();
		String newLine = "\r\n";
		//add statusLine
		String response = statusLine + newLine;
		//add headers
		for(String key : headers.keySet()) 
			response += key + ": " + headers.get(key) + newLine;
		//add new line to end head section
		response += newLine;
		//add body
		return response + body;
	}
	
}
