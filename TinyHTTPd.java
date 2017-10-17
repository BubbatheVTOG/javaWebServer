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
 *	https://tools.ietf.org/html/rfc7231
 *	https://tools.ietf.org/html/rfc2295
 *	https://tools.ietf.org/html/rfc2296
 * How it works:
 *	1) The server is started.
 *	2) The client connects to the server.
 *	3) A instance of ConnectedClient is created and started.
 *	4) The client sends us their request.
 *	5) Based on the clients request we decide what to do. (handleRequest())
 *	6) If the client requested a file, we load the file into memory as
 *		an ArrayList of bytes. (handleGET())
 *	7) We construct our response containing the HTTP header, data (if need),
 *		and tail. (generateResponse())
 *	8) We send out our respose back to the client.
 *	9) The ClientConnection has ran its course and is now finished.
 *	10) Done.
 *
 * A Java HTTP Server
 * Copyright (C) 2017 Zachary "Bubba" Lichvar

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

public class TinyHTTPd{

	static final private File ROOTPATH = new File(System.getProperty("user.dir"));

	// private String ServerName = "http://foo.bar.com/";
	private String ServerVer = "Server: BubbasBadWebServer/1.0.0";
	private ServerSocket boundSocket;
	private static boolean verboseOutput = false;
	// private static boolean veryVerboseOutput = false;

	public static void main(String[] args){
		if(args.length == 0){
			new TinyHTTPd(16789);
		}else if(args.length>0){
			//ArrayList of args (Its easier use builtin api for parsing.)
			List<String> argsList = Arrays.asList(args);
			verboseOutput = argsList.contains("-v");
			// veryVerboseOutput = argsList.contains("-vv");

			new TinyHTTPd(16789);
		}
	}

	public TinyHTTPd(int portNumber){
		try{
			//Get ServerSocket
			boundSocket = new ServerSocket(portNumber);
			//Accept client connections, create instace of a client thread, and start it.
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

		/**
		 * Default Constructor.
		 * This class handles the connected client.
		 *
		 * @param Socket The socket the client connected to us with.
		 */
		public ClientConnection(Socket client){
			this.client = client;
		}

		public void run(){
			// System.out.println("Got connection from: "+client.getLocalAddress().getHostAddress());
			try{
				//Get request from the client.
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String requestString="";
				StringBuilder requestBuilder = new StringBuilder();
				while(br.ready()){
					requestBuilder.append(br.readLine());
				}

				requestString = requestBuilder.toString();

				//Verbose Output
				if(verboseOutput){
					System.out.println("Got connection from: "+client.getInetAddress().toString());
					System.out.println("Request String: "+requestString);
				}

				//Create an ArrayList with our response.
				ArrayList<Byte> response = new ArrayList<Byte>();
				response.addAll(this.handleRequest(requestString.split(" ")));

				//We cannot send an ArrayList. We must convert it to an Array.
				byte[] responseBytes = new byte[response.size()];
				for(int i=0; i < response.size(); i++){
					responseBytes[i]=response.get(i);
				}

				//Send the client our response.
				client.getOutputStream()
					.write(responseBytes);

			}catch(IOException ioe){
				System.err.println("IOException!\nIssue at TinyHTTPd.ClientConnection.run()");
			}catch(Exception E){
				System.err.println("I don't know how you got here. TinyHTTPd.ClientConnection().\n");
				E.printStackTrace();
			}
		}

		/**
		 * This method handles the request. The request is typically
		 * the first string in the request.
		 *
		 * @param String[] The entire request from the client
		 * deliminated by spaces.
		 * @return ArrayList<Byte> The response formed from the
		 * clients request.
		 */
		private ArrayList<Byte> handleRequest(String[] request){

			//This ArrayList will hold our entire response.
			ArrayList<Byte> totalResponseData;

			//Define our action based on the client's request.
			switch(request[0]){
				case "GET":
					try{
						totalResponseData = this.generateResponse(200,"OK",this.handleGET(request[1]));
					}catch(FileNotFoundException fnf){
						totalResponseData = this.generateResponse(404,"Not Found",null);
					}catch(IOException ioe){
						totalResponseData = this.generateResponse(403,"Unauthorized",null);
					}catch(Exception e){
						totalResponseData = this.generateResponse(500,"Internal Server Error",null);
					}
					break;
				case "POST":
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
				case "HEAD":
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
				case "PUT":
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
				case "OPTIONS":
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
				case "DELETE":
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
				case "CONNECT":
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
				default:
					totalResponseData = this.generateResponse(501,"Not Implemented",null);
					break;
			}
			assert totalResponseData.size() != 0;
			return totalResponseData;
		}

		/**
		 * This gets the requested file.
		 *
		 * @param String The requested file from the client.
		 * @throws FileNotFoundException
		 * @throws IOException
		 * @return ArrayList<Byte> The requested file from the client as Bytes.
		 */
		private ArrayList<Byte> handleGET(String request)throws FileNotFoundException, IOException{

			//The file the client wants.
			File accessFile = null;
			//The file as bytes that the client wants.
			ArrayList<Byte> fileData = new ArrayList<Byte>();

			//If the client hasn't requested anything give them the index, otherwise give them what they want.
			if(request.equalsIgnoreCase("/")){
				accessFile = new File(ROOTPATH.getAbsolutePath()+"/index.html");
			}else{
				accessFile = new File(ROOTPATH.getAbsolutePath()+request);
			}

			//Read our file and put it in the data list.
			DataInputStream dis = new DataInputStream(new FileInputStream(accessFile));
			while(dis.available()>0){
				fileData.add(dis.readByte());
			}
			dis.close();
			assert fileData.size() != 0;
			return fileData;
		}

		/**
		 * This builds the response to the client based on their request
		 * and our ability to complete it. It will also contain the data
		 * the client requested if appropriate.
		 *
		 * @param int The response status.
		 * @param String The response message type.
		 * @param ArrayList<Byte> Additional data we are sending if
		 * appropriate. Can also be null.
		 * @return ArrayList<Byte> The full response of bytes we need
		 * to send to the client.
		 */
		private ArrayList<Byte> generateResponse(int status, String responseCode, ArrayList<Byte> data){

			//This is our arraylist that will contain our response.
			ArrayList<Byte> responseByteList = new ArrayList<Byte>();

			//All of this information is needed to make the HTTP header.
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss z");
			String header = "";
			String http1 = "HTTP/1.1 ";
			String contentLength = "Content-Length: ";
			String contentType = "Content-type: text/html";
			String date = sdf.format(new Date());
			String lastModified = "Last-Modified: "+date;
			String IFS = "\r\n";
			String IFStail = "\r\n\r\n";

			//Form different headers depending on if the client has asked for data.
			if(data != null){
				StringBuilder headerBuilder = new StringBuilder();
				header = headerBuilder.append(http1)
					.append(status)
					.append(" ")
					.append(responseCode)
					.append(IFS)
					.append("Date: ")
					.append(date)
					.append(ServerVer)
					.append(IFS)
					.append(lastModified)
					.append(IFS)
					.append(contentType)
					.append(IFS)
					.append(contentLength)
					.append(data.size())
					.append(IFS)
					.append(IFS)
					.toString();
			}else{
				StringBuilder headerBuilder = new StringBuilder();
				header = headerBuilder.append(http1)
					.append(status)
					.append(" ")
					.append(responseCode)
					.append(IFS)
					.append("Date: ")
					.append(date)
					.append(IFS)
					.append(ServerVer)
					.append(IFS)
					.append(IFS)
					.toString();
			}

			byte[] headerBytes = header.getBytes();
			//Simple http return statuses.
			//Build our response in an ArrayList of bytes depending on the response code.
			//TODO:418 "I'm a teapot!" RFC 2324
			switch(status){
				case 200:
					for(int i=0; i < headerBytes.length; i++){
						responseByteList.add(headerBytes[i]);
					}
					responseByteList.addAll(data);
					break;
				case 403:
					for(int i=0; i < headerBytes.length; i++){
						responseByteList.add(headerBytes[i]);
					}
					break;
				case 404:
					for(int i=0; i < headerBytes.length; i++){
						responseByteList.add(headerBytes[i]);
					}
					break;
				case 500:
					for(int i=0; i < headerBytes.length; i++){
						responseByteList.add(headerBytes[i]);
					}
					break;
				case 501:
					for(int i=0; i < headerBytes.length; i++){
						responseByteList.add(headerBytes[i]);
					}
					break;
				default:
					//HOW DID YOU GET HERE???
					break;
			}

			//We need to append a tail to our response.
			byte[] IFSTailBytes = IFStail.getBytes();
			for(int i=0; i<IFSTailBytes.length;i++){
				responseByteList.add(IFSTailBytes[i]);
			}
			assert responseByteList.size() != 0;
			return responseByteList;
		}
	}
}
