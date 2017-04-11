package P2P.PeerPeer.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.SynchronousQueue;

import javax.swing.plaf.basic.BasicTreeUI.TreeHomeAction;

import P2P.PeerPeer.Client.Downloader;
import P2P.PeerPeer.Message.Message;
import P2P.PeerPeer.Message.MessageFileChunk;
import P2P.util.PeerDatabase;

public class SeederThread extends Thread {
    private Socket socket;
	private Downloader downloader;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private short chunkSize;
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
    
    //Env√≠a por el socket el chunk solicitado por el DownloaderThread
    protected void sendChunk(int chunkNumber, String fileHashStr)  {
    }

    //M√©todo principal que coordina la recepci√≥n y env√≠o de mensajes
    public void run() {
    /*	System.out.println("Seed talking");
    	try {
			System.out.println(dis.readUTF());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	*/
    	byte[] buf=new byte[1000];
    	Message m=null;
       	try {
    			dis.read(buf);
    			m=processMessageReceived(buf);
    			
       		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        	try {
    			dos.write(m.toByteArray());
    			dos.flush();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}	
    }
    
    private Message processMessageReceived(byte[] buf){
  
    	if (buf==null) throw new NullPointerException();
  
    	switch ((int)buf[0])
    	{
	    	case Message.TYPE_REQ_LIST :
	    	{
	    		//consultar si el fichero requerido
	    		//esta siendo descargado
	    		//en caso afirmativo, compartir las partes
	    		
	    		//TODO COMO sÈ que tengo el fichero que
	    		//me piden?ø?ø?
	    		
	    		return Message.makeChunkList(-1,-1);
	    	}
	    	
	    	case Message.TYPE_REQ_DATA :{		
	    		System.out.println("send data");
	    		break;
	    	}
	    	default: return null;
    	}
		return null;
    }
}
    


