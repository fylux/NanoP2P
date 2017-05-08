package P2P.PeerPeer.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.security.acl.LastOwnerException;
import java.util.concurrent.SynchronousQueue;

import P2P.App.Peer;
import P2P.PeerPeer.Client.Downloader;
import P2P.PeerPeer.Message.Message;
import P2P.PeerPeer.Message.MessageChunk;
import P2P.PeerPeer.Message.MessageChunkList;
import P2P.PeerPeer.Message.MessageData;
import P2P.PeerPeer.Message.MessageHash;
import P2P.util.FileInfo;
import P2P.util.PeerDatabase;

public class SeederThread extends Thread {
    private Socket socket;
	private Downloader downloader;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private short chunkSize;
	private String fileHash;
	
	//borrar
	int pos=0;
	/* Global buffer for performance reasons */
    private byte[] chunkDataBuf;

    public SeederThread(Socket socket, Downloader downloader, short chunkSize) {
    	this.socket = socket;
    	this.downloader = downloader;
    	this.chunkSize = chunkSize;
    	
    	try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error creating Seeder Thread");
		}
		
    }

	//Devuelve la lista de trozos que tiene del fichero solicitado
    public void sendChunkList(String fileHashStr) {	
    }
    
    //Envía por el socket el chunk solicitado por el DownloaderThread
    protected void sendChunk(int chunkNumber, String fileHashStr)  {
    }

    //Método principal que coordina la recepción y envío de mensajes
    public void run() {
    	
    	//bucle infinito
    while(true){
	    	byte[] buf_list = new byte[Message.SIZE_REQ_LIST];
	    	try {
				dis.readFully(buf_list);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.err.println("SeederThread Cerrado.");
				break;
			}
	    	
	    	MessageHash fileRequested = Message.makeReqList(buf_list);
	    	fileHash = fileRequested.getHash();
	    	
	    	FileInfo fileData = isFileReachable();
	    	
	    	if (fileData != null)
	    	{
		    	MessageChunkList chunkList=Message.makeChunkList(-1);
		    	
		        try {
		    		dos.write(chunkList.toByteArray());
		    		dos.flush();
		    	}catch (IOException e) {
		    		e.printStackTrace();
		    	}
	    	
		        
	    	
	    	
		        byte[] buf_chunk = new byte[Message.SIZE_REQ_DATA];
		    	try {
					dis.readFully(buf_chunk);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		    	
		    	
		    	MessageChunk requestedChunk = Message.makeReqData(buf_chunk);
		    	int index = requestedChunk.getIndex();
		
				
				
				
				byte[] data=readDataFile(index,fileData);
				
				//leer la parte index del fichero en data 
				
				MessageData dataChunk = Message.makeChunkData(data,index);
			
				try {
		    		dos.write(dataChunk.toByteArray());
		    		dos.flush();
		    	}catch (IOException e) {
		    		e.printStackTrace();
		    	}
				
				
	    	}
    	}	
    
    	System.out.println("fueraaa");
    }
	
	private FileInfo isFileReachable() {
		FileInfo fi[] = Peer.db.getLocalSharedFiles();
    	for (FileInfo f : fi)
			if (f.fileHash.equals(fileHash))
				return f;

		return null;
	}
	
	private byte[] readDataFile(int index,FileInfo fileData){
		
		//TODO relacionar index con la partes del fichero
			
		// dado un tama�o de trozo
		int parts=getTotalChunks(fileData);
		int lastSize=getSizeLastChunk(fileData);
		
		
		int dataSize=chunkSize;
		if (index==parts)
		{
			dataSize=lastSize;
		}
		
		String path=Peer.db.lookupFilePath(fileData.fileHash);
			
		byte data[] = new byte[dataSize];
		int lengUtil=0;
		File file = new File(path);
		RandomAccessFile rfi;
		try {
			rfi = new RandomAccessFile(file,"r");
			rfi.seek(pos);//Nos situamos en la posici�n
			lengUtil=rfi.read(data,0,data.length); //Leemos el trozo
			rfi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		System.out.println("data.length: "+ data.length);
		System.out.println("utilbyts: "+lengUtil);
		System.out.println("pos: " + pos);
		printByteArray(data);
		pos+=dataSize;
				
		return data;
	}
	
	public int getTotalChunks(FileInfo file) {
		int result=(int)Math.ceil(Float.valueOf(file.fileSize)/Float.valueOf(chunkSize));
		if (result==0)
			return 1;
		return result;
	}
	
	public int getSizeLastChunk(FileInfo file) {
		int size = (int)(file.fileSize%(long)chunkSize);
		if (size == 0)
			return chunkSize;
		else
			return size;
	}
	
	 private void printByteArray(byte[] data){
	    	String file_string = "";

			for(int i = 0; i < data.length; i++)
			   {
					file_string += (char)data[i];
			   }
			System.out.println("cadena: " + file_string);
	 }
}