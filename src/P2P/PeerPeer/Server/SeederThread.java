package P2P.PeerPeer.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
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
    
    //EnvÃ­a por el socket el chunk solicitado por el DownloaderThread
    protected void sendChunk(int chunkNumber, String fileHashStr)  {
    }

    //MÃ©todo principal que coordina la recepciÃ³n y envÃ­o de mensajes
    public void run() {
    	
    	byte[] buf_list = new byte[Message.SIZE_REQ_LIST];
    	try {
			dis.read(buf_list);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	
    	MessageHash fileRequested = Message.makeReqList(buf_list);
    	fileHash = fileRequested.getHash();
    	
    	FileInfo fileData = isFileReachable();
    	if (fileData != null)
    	{
	    	MessageChunkList chunkList=Message.makeChunkList(5,1,2,3,4,5);
	    	
	        try {
	    		dos.write(chunkList.toByteArray());
	    		dos.flush();
	    	}catch (IOException e) {
	    		e.printStackTrace();
	    	}
	        
	        byte[] buf_chunk = new byte[Message.SIZE_REQ_DATA];
	    	try {
				dis.read(buf_chunk);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	    	
	    	MessageChunk requestedChunk = Message.makeReqData(buf_chunk);
	    	int index = requestedChunk.getIndex();
	    	
	    	System.out.println("index: "+index);
	   
	    	
	    	long size=fileData.fileSize;
			String path=Peer.db.lookupFilePath(fileData.fileHash);
			
	    	toByteArray(1,getnChunksFile(size));
    	}
    }
	
	private FileInfo isFileReachable() {
		FileInfo fi[] = Peer.db.getLocalSharedFiles();
    	for (FileInfo f : fi)
			if (f.fileHash.equals(fileHash))
				return f;

		return null;
	}
	
	private byte[] toByteArray(int index, long[] parts){
		if (index>(parts[0]+1)) System.err.println("parte inexistente");
	//TODO relacionar el hash con la ruta del fichero
		//que hay que abrir
	
	
	//TODO relacionar index con la partes del fichero
		
	// dado un tamaño de trozo
	int chunkSize=2;
	int pos=0;
	if (index<(parts[0]+1))
		pos =index*chunkSize; // y una posición dentro del fichero
	else pos= Math.toIntExact(parts[0])*chunkSize+1;
	
	//primero probamos a leer todo el archivo
	pos=0;
	byte data[] = new byte[29];
	int lengUtil=0;
	File file = new File("C:/Users/daniel/Desktop/share/P2/share_2.txt");
	RandomAccessFile rfi;
	try {
		rfi = new RandomAccessFile(file,"r");
		rfi.seek(pos);//Nos situamos en la posición
		lengUtil=rfi.read(data); //Leemos el trozo
		rfi.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} 
	System.out.println("utilbyts: "+lengUtil);
	File f2 = new File("C:/Users/daniel/Desktop/share/P2/funciona_2.txt");
		try {
		//	if (!f2.exists())
				f2.createNewFile();
			RandomAccessFile rfo;
			rfo = new RandomAccessFile(f2,"rw");
			rfo.seek(pos);
			//rfo.write(data,0,29);
			rfo.write(data);
			
			
			rfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	
		File f = new File("C:/Users/daniel/Desktop/share/P2/funciona_3.txt");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
			
	return data;
}

/**
 * 
 * @return the size of last part of file, only if this part 
 * is smaller than the default chunk size 
 */
long[] getnChunksFile(long size){
		
		long result[]=new long[2];
		
		System.out.println("size: " + size);
		int chunkSize=2;
		long parts=size/chunkSize;
		
		if (parts!=0){
		result[0]=parts;
		result[1]=size-(parts*chunkSize); //bytes sobrantes
		}else
			{ 
				result[0]=0; 
				result[1]=size;
			}
		return result;
	}
}
/**
-como consigo el trozo que me han pedido
**/