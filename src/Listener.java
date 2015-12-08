import java.io.IOException;
import java.net.ServerSocket;

public class Listener extends Thread {
	
	int port;
	
	public Listener(int port) {
		this.port = port;
	}
	
	public void run() {
		try {
			ServerSocket listener = new ServerSocket(port);
			while(true)  {
				new RequestHandler(listener.accept()).start();
				System.out.println("Received Request");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
}
