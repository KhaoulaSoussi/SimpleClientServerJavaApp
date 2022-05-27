# Client Server Java App inspired from Google Drive

## Desription

This project is a minimal cloud drive application inspired from Google Drive. It allows the user to automatically back-up his/her files on the cloud. The client is the process used by the end user; it keeps track of the changes happening to the local folder and it sends those changes to the server side which plays the role of the "cloud".

## Protocol

Once the connection between the client and the server is established, the client sends a folder name to the server (this is the folder name that the user behind the client wishes to replicate on the cloud.) The server checks if the folder already exists or not, if not it creates the backup folder and then the communication between the client and the server proceeds as follows.
- The client is supposed to send all the files on the local folder to the server. The client shall first send a header specifying the number of files that will be sent:
“[Number of files][one space]will be sent[line feed]”
The client shall then send the files one by one using the upload request as specified in the Requests section of the protocol.
- Once all the files in the local folder are uploaded to the respective client’s cloud folder, the client will then send upload/delete requests for any file that has been added, updated, or deleted according to the Requests section below.

## Requests

- Upload (handles adding and updating files)
If a file was added or modified on the client side, the client should send the following header request:    
`Upload[one space][file name][one space][file size][line feed]`
Once receiving this request, the server should respond by the header: `Send[Line feed]`
denoting that it is ready to receive the raw bytes of the file.
Only after receiving the "Send" message from the server, the client sends the actual raw bytes of the file to by uploaded.
- Delete
The client can also send a delete request to the server as follows:
`Delete[one space][file name][line feed]`

## User Manual

1. Create a local folder within the same directory as CloudClient.java.
2. Populate your local folder with the files you'd like to backup on server side (subfolders are not supported).
3. After making sure the server is listening, run the CloudClient.java class and provide the
folder name as argument. You can run as many client instances as you want by changing the `maxClients` attribute in the `CloudServer.java` file from the default 3 to your desired number.
4. While the connection between the client and the server is held, you can add files to the folder, modify existing files, and delete files, all these changes will be automatically reflected on the server.
5. In the case of deletion, you will receive a question from the client asking you to confirm the permanent deletion of the file from the cloud. If you wish to keep the file in the cloud, answer by “N”, otherwise answer by “Y”. The file on the cloud won’t be deleted unless you answered by “Y”.
