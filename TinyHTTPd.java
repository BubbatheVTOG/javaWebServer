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

	private String ServerName = "http://foo.bar.com/";
	private String ServerVer = "Server: BubbasBadWebServer/1.0.0";
	private ServerSocket boundSocket;

	public static void main(String[] args){
		new TinyHTTPd();
	}

	public TinyHTTPd(){
		try{
			boundSocket = new ServerSocket(16789);
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


				ArrayList<Byte> response = new ArrayList<Byte>();
				response.addAll(this.handleRequest(requestString.split(" ")));

				byte[] responseBytes = new byte[response.size()];
				for(int i=0; i < response.size(); i++){
					responseBytes[i]=response.get(i);
				}

				client.getOutputStream()
					.write(responseBytes);

			}catch(IOException ioe){
				System.err.println("IOException!\nIssue at TinyHTTPd.ClientConnection.run()");
			}catch(Exception E){
				System.err.println("We don't know how we got here. TinyHTTPd.ClientConnection().\n");
				E.printStackTrace();
			}
		}

		private ArrayList<Byte> handleRequest(String[] request){

			ArrayList<Byte> totalResponseData;

			switch(request[0]){
				case "GET":
					try{
						totalResponseData = this.generateResponse(200,"OK",this.handleGET(request[1]));
					}catch(FileNotFoundException fnf){
						totalResponseData = this.generateResponse(401,"File not Found!",null);
					}catch(IOException ioe){
						totalResponseData = this.generateResponse(403,"Unauthorized",null);
					}catch(Exception e){
						totalResponseData = this.generateResponse(501,"Internal Error",null);
					}
					break;
				case "POST":
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "HEAD":
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "PUT":
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "OPTIONS":
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "DELETE":
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
				case "CONNECT":
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
				default:
					totalResponseData = this.generateResponse(501,"Unsupported Request!",null);
					break;
			}
			assert totalResponseData.size() == 0;
			return totalResponseData;
		}

		private byte[] handleGET(String request)throws FileNotFoundException, IOException{

			File accessFile = null;

			if(request.equalsIgnoreCase("/")){
				accessFile = new File(ROOTPATH.getAbsolutePath()+"/index.html");
			}else{
				accessFile = new File(ROOTPATH.getAbsolutePath()+"/"+request);
			}

			DataInputStream dis = new DataInputStream(new FileInputStream(accessFile));
			byte[] fileData = new byte[(int)accessFile.length()];
			int i=0;
			while(dis.available()>0){
				fileData[i]=dis.readByte();
				i++;
			}
			assert fileData.length == 0;
			return fileData;
		}

		private ArrayList<Byte> generateResponse(int status, String responseCode, byte[] data){

			//I'll form the head.
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss z");
			ArrayList<Byte> byteList = new ArrayList<Byte>();
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
					contentLength+data.length+IFS+
					IFS;
			}else{
				header = http1+status+" "+responseCode+IFS+
					"Date: "+date+IFS+
					ServerVer+IFS+
					IFS;
			}

			byte[] headerBytes = header.getBytes();
			//Simple http return statuses.
			//TODO:418 "I'm a teapot!" RFC 2324
			switch(status){
				case 200:
					for(int i=0; i < headerBytes.length; i++){
						byteList.add(headerBytes[i]);
					}
					for(int i=0; i < data.length; i++){
						byteList.add(data[i]);
					}
					break;
				case 401:
					for(int i=0; i < headerBytes.length; i++){
						byteList.add(headerBytes[i]);
					}
					break;
				case 404:
					for(int i=0; i < headerBytes.length; i++){
						byteList.add(headerBytes[i]);
					}
					break;
				case 500:
					for(int i=0; i < headerBytes.length; i++){
						byteList.add(headerBytes[i]);
					}
					break;
				case 501:
					for(int i=0; i < headerBytes.length; i++){
						byteList.add(headerBytes[i]);
					}
					break;
				default:
					//HOW DID YOU GET HERE???
					break;
			}
			return byteList;

		}
	}
}
