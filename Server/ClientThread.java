import java.net.*;
import java.io.*;
import java.util.*;


public class ClientThread implements Runnable{

	private Socket connectionFromClient;
	private BufferedReader headerReader;
	private BufferedWriter headerWriter;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private int index;

	public ClientThread(Socket clientSocket, int clientIndex) throws IOException{
		this.connectionFromClient = clientSocket;
		this.index = clientIndex;
		InputStream in = connectionFromClient.getInputStream();
		OutputStream out = connectionFromClient.getOutputStream();
		headerReader = new BufferedReader(new InputStreamReader(in));
		headerWriter = new BufferedWriter(new OutputStreamWriter(out));
		dataIn = new DataInputStream(in);
		dataOut = new DataOutputStream(out);
	}


	public void receive_file(DataInputStream dataIn, String fileName, int fileSize, String path) throws Exception{
		byte[] space = new byte[fileSize];
		try{
			dataIn.readFully(space);
		}catch(Exception e){
			System.out.println(e);
		}
		FileOutputStream fileOut = new FileOutputStream(path + fileName);
		fileOut.write(space, 0, fileSize);
		fileOut.close();
	}

	public boolean delete_file(String fileName, String path){
		File toDelete = new File(path + fileName);
		if (toDelete.delete()) {
			System.out.println("\n\tDeleting " + fileName + " from " + path + " ...");
			return true;
        }
        return false;
	}

	@Override
	public void run() {
		try {
			System.out.println("Server got a connection from a client whose port is: " + this.connectionFromClient.getPort());

			String header = headerReader.readLine() + "_Backup" + this.index;
			String path = header + "/";
			File dir = new File(path);
			if(!dir.exists()){
				boolean bool = dir.mkdir();
			}
			//Receive the number of files to be uploaded (no support for subfolders)
			header = headerReader.readLine();		//Expected: "N files will be sent\n"
			StringTokenizer strtok = new StringTokenizer(header, " ");
			int filesCount = Integer.parseInt(strtok.nextToken());
			for(int i = 0; i < filesCount; i++){
				header = headerReader.readLine();
				strtok = new StringTokenizer(header, " ");
				String cmd = strtok.nextToken();
				String fileName = strtok.nextToken();
				String tempSize = strtok.nextToken();
	        	int fileSize = Integer.parseInt(tempSize);
	        	header = "Send\n";
	        	headerWriter.write(header, 0, header.length());
				headerWriter.flush();
	        	if(fileSize != -1 && fileName != null){
					System.out.println("\n\tDownloading " + fileName + " to " + path + " ...");
	        		receive_file(dataIn, fileName, fileSize, path);
	        	}
	        	fileSize = -1;
	        	fileName = null;
			}

			System.out.println("\nChecking for updates...");

			while(true){
				//The user already has some files in the backup folder. 
				//Other requests should be issued: Upload (for a newly added file), Update, Delete
				header = headerReader.readLine();
				while(header == null){
					header = headerReader.readLine();
				}
				strtok = new StringTokenizer(header, " ");
        		String cmd = strtok.nextToken();
	        	String fileName = strtok.nextToken();
	        	if(cmd.equals("Upload")){
	        		String tempSize = strtok.nextToken();
	        		int fileSize = Integer.parseInt(tempSize);
	        		header = "Send\n";
		        	headerWriter.write(header, 0, header.length());
					headerWriter.flush();
					System.out.println("\n\tDownloading " + fileName + " to " + path + " ...");
	        		receive_file(dataIn, fileName, fileSize, path);
	        	}
	        	else if(cmd.equals("Delete")){
	        		boolean bool = delete_file(fileName, path);
	        		if(!bool)
	        			System.out.println("Something went wrong while deleting " + fileName + " from " + path + "!");
	        	}
	        	else {
					System.out.println("Connection got from an incompatible client");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try{
				connectionFromClient.close();
			}
			catch(IOException e){
				System.out.println("Something went wrong while closing the connection!");
			}
		}
	}
}