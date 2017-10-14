import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * @author Zachary "Bubba" Lichvar
 * List of resources:
 * HTTP/1.1 Stuff:
 *	https://en.wikipedia.org/wiki/List_of_HTTP_header_fields
 *	http://www.and.org/texts/server-http
 *
 */

public class TinyHTTPd{

	static final private File ROOTPATH = new File(System.getProperty("user.dir"));

	private String ServerName = "http://foo.bar.edu/";
	private String ServerVer = "Server: BubbasBadWebServer/1.0.0";

	public static void main(String[] args){
		new TinyHTTPd();
	}

	public TinyHTTPd(){
		try{
			ServerSocket boundSocket = new ServerSocket(16789);
			while(true){
				new ClientConnection(boundSocket.accept()).start();
			}
		}catch(BindException be){
			System.err.println("BindException! Please select a different port.");
		}catch(Exception E){
			System.err.println("We don't know how we got here. TinyHTTPd().\n");
			E.printStackTrace();
		}
	}

	class ClientConnection extends Thread{
		private Socket client;

		public ClientConnection(Socket client){
			this.client = client;
		}

		public void run(){
			// System.out.println("Got connection from: "+client.getLocalAddress().getHostAddress());
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String requestString="";
				while(br.ready()){
					requestString += br.readLine();
				}

				// String[] requestArray = requestString.split(" ");

				client.getOutputStream()
					.write(
							this.handleRequest(
								requestString.split(" "))
							.getBytes());

			}catch(IOException ioe){
				System.err.println("IOException!\nIssue at TinyHTTPd.ClientConnection.run()");
			}catch(Exception E){
				System.err.println("We don't know how we got here. TinyHTTPd.ClientConnection().\n");
				E.printStackTrace();
			}
		}

		private String handleRequest(String[] request){

			String data = "";

			switch(request[0]){
				case "GET":
					data = this.generateResponse(200,"OK",this.handleGET(request[1]));
					break;
				case "POST":
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "HEAD":
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "PUT":
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "OPTIONS":
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "DELETE":
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "CONNECT":
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
				default:
					data = this.generateResponse(501,"Unsupported Request!",null);
					break;
			}
			assert !data.equalsIgnoreCase("");
			return data;
		}

		private String handleGET(String request){

			String fileData = "";

			try{
				File accessFile = null;
				if(request.equalsIgnoreCase("/")){
					accessFile = new File(ROOTPATH.getAbsolutePath()+"/index.html");
				}else{
					accessFile = new File(ROOTPATH.getAbsolutePath()+"/"+request);
				}

				BufferedReader br = new BufferedReader(new FileReader(accessFile));

				while(br.ready()){
					fileData += br.readLine();
				}

			}catch(FileNotFoundException fnf){
				return this.generateResponse(404,"File not Found",null);
			}catch(IOException ioe){
				return this.generateResponse(403,"Permission denied!",null);
			}catch(Exception e){
				System.err.println("Hit unexpected error!");
				e.printStackTrace();
				return this.generateResponse(500,"Unknown Issue!",null);
			}
			assert !fileData.equalsIgnoreCase("");
			return fileData;
		}

		private String generateResponse(int status, String responseCode, String data){

			//I'll form the head.
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss z");
			String response = "";
			String header = "";
			String http1 = "HTTP/1.1 ";
			String contentLength = "Content-Length: ";
			String contentType = "Content-type: text/html";
			String date = sdf.format(new Date());
			String lastModified = "Last-Modified: "+date;
			String IFS = "\r\n";


			if(data != null){
				header = http1+status+" "+responseCode+IFS+
					"Date: "+date+IFS+
					ServerVer+IFS+
					lastModified+IFS+
					contentType+IFS+
					contentLength+data.length()+IFS+
					IFS;
			}else{
				header = http1+status+" "+responseCode+IFS;
			}

			//Simple http return statuses.
			//TODO:418 "I'm a teapot!" RFC 2324
			switch(status){
				case 200:
					response = header+data;
					break;
				case 401:
					response = header;
					break;
				case 404:
					response = header;
					break;
				case 500:
					response = header;
					break;
				case 501:
					response = header;
					break;
				default:
					//HOW DID YOU GET HERE???
					break;
			}
			assert !response.equalsIgnoreCase("");
			System.out.println(response);
			return response;
		}
	}
}

