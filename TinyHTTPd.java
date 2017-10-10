import java.io.*;
import java.net.*;
import java.util.*;

public class TinyHTTPd{
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
			try{
				BufferedReader in = new BufferedReader(
						new InputStreamReader(
							client.getInputStream(),"8859_1"));

				PrintWriter out = new PrintWriter(
						new OutputStreamWriter(
							client.getOutputStream(),"8859_1"),true);

				String[] request = in.readLine().split(" ");

				if(request.length > 2){
					if(Arrays.asList(request).contains("GET")){
						System.out.println(sb.append("New GET from: ")
								.append(client.getInetAddress()
									.getHostAddress()));
						if(request[1].equalsIgnoreCase("/")){
							request[1]=sb.append(System.getProperty("user.dir"))
								.append(request[1])
								.append("index.html")
								.toString();

							out.write(this.getRequestedFileAsString(request[1]));
							out.flush();

							// }else{
							// System.out.println(sb.append("Illegal directory request from: ")
							// .append(client.getInetAddress()
							// .getHostAddress()));
							// Thread.currentThread().join();
						}
					}else{
						System.out.println(sb.append("Unsupported request from :")
								.append(client.getInetAddress()
									.getHostAddress()));
					}
				}else{
					System.out.println(sb.append("Malformed request from :")
							.append(client.getInetAddress()
								.getHostAddress()));
				}

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

		private String getRequestedFileAsString(String request) throws IOException,
			FileNotFoundException{
				//System.out.println(new File(request).getAbsolutePath());
				BufferedReader br = new BufferedReader(
						new FileReader(request));
				StringBuilder localsb = new StringBuilder();
				String data = null;
				while((data=br.readLine()) != null){
					localsb.append(data);
				}
				return localsb.toString();
		}
	}
}
