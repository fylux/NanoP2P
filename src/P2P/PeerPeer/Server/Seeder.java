package P2P.PeerPeer.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import P2P.PeerPeer.Client.Downloader;
import P2P.util.PeerDatabase;


public class Seeder implements Runnable {
	public static final int SEEDER_FIRST_PORT = 10000;
	public static final int SEEDER_LAST_PORT = 10100;
	public Downloader currentDownloader;
	private int port;
	private ServerSocket serverSocket;
	private short chunkSize;


    public Seeder(short chunkSize) {
    	try {
			serverSocket = new ServerSocket();
		} catch (IOException e) {
			System.out.println("Error creating server socket");
		}
    	port = getAvailablePort();
    	this.chunkSize = chunkSize;
    }

    public int getAvailablePort() {
    	int portRequested = SEEDER_FIRST_PORT;
    	boolean valid;
    	do {
	    	try {
				serverSocket.bind(new InetSocketAddress(portRequested));
				valid = true;
			} catch (IOException e) {
				valid = false;
				++portRequested;
			}
    	} while(!valid && portRequested <= SEEDER_LAST_PORT );
    		
    	return portRequested;
    }
    
    /** 
	 * Función del hilo principal del servidor. 	
	 */
	public void run()
	{
		boolean alive = true;
		while (alive) {
			try {
				Socket clientSocket = serverSocket.accept();
				new SeederThread(clientSocket, currentDownloader, chunkSize).start();
			} catch (IOException e1) {
				System.out.println("Seed killed");
				alive = false;
			}
		}
	}
    
    /**
     * Inicio del hilo del servidor.
     */
    public void start()
    {
        // Inicia esta clase como un hilo
    	new Thread(this).start();
    }
    
    public void setCurrentDownloader(Downloader downloader) {
    	currentDownloader = downloader;
    }
    
    public int getSeederPort() {
    	return port;
    }
    
    public void closeSocket(){
    	try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}
 