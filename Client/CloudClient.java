import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CloudClient{
	public static void main(String[] args) throws Exception {
		String folderName = args[0];		//This holds the name of the local folder of the client
		String path = folderName + "/";
		try (Socket connectionToServer = new Socket("localhost", 80)) {
			InputStream in = connectionToServer.getInputStream();
			OutputStream out = connectionToServer.getOutputStream();
			BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));
			DataInputStream dataIn = new DataInputStream(in);
			DataOutputStream dataOut = new DataOutputStream(out);

			File dir = new File(path);
			File[] files = dir.listFiles();
			//The method listFiles does not guarantee any order of the files, so let's sort them as we will need that later
			Arrays.sort(files);
			List<String> fileNames = new ArrayList<String>();
			for(File file: files){
				String fileName = file.getName();
				fileNames.add(fileName);
			}
			//Send the folder name to the server
			headerWriter.write(folderName + "\n", 0, folderName.length() + 1);
			headerWriter.flush();
			int filesCount = files.length;
			if(fileNames.contains(".DS_Store")){
				filesCount--;
			}
			String header = filesCount + " will be sent\n";
			headerWriter.write(header, 0, header.length());
			headerWriter.flush();
			for(File file: files){
				String fileName = file.getName();
				if(file.isFile() && !fileName.equals(".DS_Store")){
					System.out.println("\n\tUploading " + fileName + " to the cloud...");
					upload_file(fileName, path, headerWriter, headerReader, dataOut);
				}
			}

			File[] oldFiles = files;
			List<String> oldFileNames = fileNames;
			List<Long> lastModified = new ArrayList<Long>();
			for(File file: files){
				long lm = file.lastModified();
				lastModified.add(lm);
			}
			while(true){
				File[] currentFiles = dir.listFiles();
				//Sort the currentFiles lists so that the currentFiles and oldFiles can be parallel arrays (unless a file was added/deleted)
				Arrays.sort(currentFiles);	
				List<String> currentFileNames = new ArrayList<String>();
				for(File file: currentFiles){
					String fileName = file.getName();
					currentFileNames.add(fileName);
				}

				System.out.println("\nChecking if any files were added...");
	    		for(File file: currentFiles){		//Iterate over the files in new version of the local folder
	    			String fileName = file.getName();
		    		if(!oldFileNames.contains(fileName)){
		    			try{
							System.out.println("\n\tUploading " + fileName + " to the cloud...");
							upload_file(fileName, path, headerWriter, headerReader, dataOut);
		    			}catch(Exception e){
		    				System.out.println("Something went wrong while uploading the added file: " + fileName);
		    			}
		    		}
		    	}

		    	System.out.println("\nChecking if any files were deleted...");
	    		for(File file: oldFiles){
	    			String fileName = file.getName();
		    		if(!currentFileNames.contains(fileName)){
		    			try{
							delete_file(fileName, headerWriter);
		    			}catch(Exception e){
		    				System.out.println("Something went wrong while deleting " + fileName);
		    			}
		        	}
		    	}

				int i = 0;
				System.out.println("\nChecking if any files were modified...\n");
				for(File file: currentFiles){		//Iterate over the files in previous version of the local folder
		    		String fileName = file.getName();
		    		if(i == oldFiles.length){
		    			break;
		    		}	
		    		String oldFileName = oldFiles[i].getName();
		    		while(!currentFileNames.contains(oldFileNames.get(i))){	//Meaning the file has been deleted
		    			i++;
		    		}
		    		if(!fileName.equals(oldFileName)){		//Meaning the file has just been added
		    			continue;
		    		}
		    		if(fileName.equals(".DS_Store")){
		    			i++;
		    			continue;
		    		}
		    		if(file.lastModified() != lastModified.get(i)){
		    			try{
							System.out.println("\n\tUpdating " + fileName + " on the cloud...\n");
							upload_file(fileName, path, headerWriter, headerReader, dataOut);
		    			}catch(Exception e){
		    				System.out.println("Something went wrong while updating the file: " + fileName);
		    			}
		    		}
		    		i++;
		    	}
		    	oldFiles = currentFiles;
		    	oldFileNames = currentFileNames;
		    	lastModified.removeAll(lastModified);
		    	for(File file: currentFiles){
					long lm = file.lastModified();
					lastModified.add(lm);
				} 
				TimeUnit.SECONDS.sleep(8);
				System.out.println("\t5 SECONDS LATER!");
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	

	public static void upload_file(String fileName, String path, BufferedWriter headerWriter, BufferedReader headerReader, DataOutputStream dataOut) throws Exception{
		FileInputStream fileIn = new FileInputStream(path + fileName);
		int fileSize = fileIn.available();
		String header = "Upload " + fileName + " " + fileSize + "\n";
		headerWriter.write(header, 0, header.length());
		headerWriter.flush();
		header = headerReader.readLine();
		if(header.equals("Send")){
			byte[] bytes = new byte[fileSize];
			fileIn.read(bytes);
			fileIn.close();
			dataOut.write(bytes, 0, fileSize);
		}
	}
	public static boolean delete_file(String fileName, BufferedWriter headerWriter) throws Exception{
		System.out.println("Are you sure you want to delete " + fileName + " permanently from the backup folder? (Y/N) ");
		Scanner scan = new Scanner(System.in);
		String answer = scan.next();
		if(answer.equals("Y")){
			System.out.println("\n\tDeleting " + fileName + " from the cloud...");
			String header = "Delete " + fileName + "\n";
			headerWriter.write(header, 0, header.length());
			headerWriter.flush();
			return true;
		}
		return false;
	}
}