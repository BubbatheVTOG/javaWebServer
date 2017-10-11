import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Zachary "Bubba" Lichvar
 */

public class TinyHTTPd{

	static final File ROOTPATH = new File(System.getProperty("user.dir"));

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
			System.err.println("We don't know how we got here.\n");
			E.printStackTrace();
		}
	}

	class ClientConnection extends Thread{
		private Socket client;
		private StringBuilder sb = new StringBuilder();

		public ClientConnection(Socket client){
			this.client = client;
		}

		public void run(){

			String[] request;

			try{
				BufferedReader in = new BufferedReader(
						new InputStreamReader(
							client.getInputStream(),"8859_1"));

				System.out.println(this.handleRequest(in.readLine().split(" ")));

				client.getOutputStream()
					.write(this.handleRequest(in.readLine().split(" ")).getBytes());

				client.close();

			}catch(FileNotFoundException fnf){
				System.err.println("File not found!\nIssue at TinyHTTPd.ClientConnection.run()");
			}catch(IOException ioe){
				System.err.println("IOException!\nIssue at TinyHTTPd.ClientConnection.run()");
			}catch(Exception E){
				System.err.println("We don't know how we got here.\n");
				E.printStackTrace();
			}
		}

		private String handleRequest(String[] request){

			String data = null;

			// for(String r: request){
			// System.out.println(r);
			// }

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
			return data;
		}

		private String handleGET(String request){

			StringBuilder sb = new StringBuilder();
			String data = null;

			try{
				File accessFile = null;
				if(request.equalsIgnoreCase("/")){
					accessFile = new File(sb.append(ROOTPATH.getAbsolutePath())
							.append("/")
							.append("index.html")
							.toString());
				}else{
					accessFile = new File(sb.append(ROOTPATH.getAbsolutePath())
							.append("/")
							.append(request)
							.toString());
				}

				BufferedReader br = new BufferedReader(new FileReader(accessFile));
				while((data = br.readLine()) != null){
					data += br.readLine();
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
			return this.generateResponse(200,"OK",data);
		}

		private String generateResponse(int status, String responseCode, String data){

			StringBuilder sb = new StringBuilder();

			//I'll form the head.
			String response = null;
			String contentLength = null;
			String header = null;
			String http1 = "HTTP/1.1";
			String contentType = "Content-type: text/html";

			if(status == 200){
				contentLength = sb.append("Content-Length: ")
					.append(data.length())
					.toString();

				sb.setLength(0);
				sb.trimToSize();

				header = sb.append(http1)
					// .append(" ")
					.append(status)
					// .append(" ")
					.append(responseCode)
					.append("\r\n")
					.append(contentType)
					// .append(" ")
					.append(contentLength)
					.append("\r\n")
					.toString();

			}else{
				header = sb.append(http1)
					// .append(" ")
					.append(status)
					// .append(" ")
					.append(responseCode)
					// .append("\n")
					.toString();
			}

			sb.setLength(0);
			sb.trimToSize();

			//Simple http return statuses.
			//TODO:418 "I'm a teapot!" RFC 2324
			switch(status){
				case 200:
					response = sb.append(header)
						.append(data)
						.toString();
					break;
				case 401:
					response = sb.append(header)
						.toString();
					break;
				case 404:
					response = sb.append(header)
						.toString();
					break;
				case 500:
					response = sb.append(header)
						.toString();
					break;
				case 501:
					response = sb.append(header)
						.toString();
					break;
				default:
					//HOW DID YOU GET HERE???
					break;
			}
			return response;
		}
	}
}

