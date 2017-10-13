import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Zachary "Bubba" Lichvar
 */

public class TinyHTTPd{

	static final private File ROOTPATH = new File(System.getProperty("user.dir"));
	private boolean debug = true;

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
			System.out.println("Got connection from: "+client.getLocalAddress().getHostAddress());
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String requestString="";
				while(br.ready()){
					requestString += br.readLine();
				}

				String[] requestArray = requestString.split(" ");

				if(debug){
					for(String s : requestArray){
						System.out.println(s);
					}
				}

				if(debug){
					System.out.println(this.handleRequest(requestArray));
				}

				client.getOutputStream().write(this.handleRequest(requestArray).getBytes());

			}catch(FileNotFoundException fnf){
				System.err.println("File not found!\nIssue at TinyHTTPd.ClientConnection.run()");
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

			String data = "";

			try{
				File accessFile = null;
				if(request.equalsIgnoreCase("/")){
					accessFile = new File(ROOTPATH.getAbsolutePath()+"/index.html");
				}else{
					accessFile = new File(ROOTPATH.getAbsolutePath()+"/"+request);
				}

				BufferedReader br = new BufferedReader(new FileReader(accessFile));

				while(br.ready()){
					data += br.readLine();
				}

				if(debug){
					System.out.println(data);
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
			assert !data.equalsIgnoreCase("");
			return this.generateResponse(200,"OK",data);
		}

		private String generateResponse(int status, String responseCode, String data){

			//I'll form the head.
			String response = "";
			String contentLength = "";
			String header = "";
			String http1 = "HTTP/1.1 ";
			String contentType = "Content-type: text/html";

			if(data != null){
				header = http1+status+" "+responseCode+"\r\n"+contentType+" Content-Length: "+data.length()+"\r\n";
			}else{
				header = http1+status+" "+responseCode+"\r\n";
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
			System.out.println(response);
			assert !response.equalsIgnoreCase("");
			return response;
		}
	}
}

