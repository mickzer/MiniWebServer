import java.util.HashMap;


public class HttpResponseBank {

	private static HashMap<Integer, String> responseCodes = new HashMap<Integer, String>();
	static {
		responseCodes.put(200, "OK");
		responseCodes.put(400, "Bad Request");
		responseCodes.put(404, "Not Found");
		responseCodes.put(500, "Internal Server Error");
		responseCodes.put(501, "Not Implemented");
	}
	
	public static String getMessage(int code) {
		return responseCodes.get(code);
	}
	
}
