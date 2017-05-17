package P2P.PeerPeer.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import P2P.PeerPeer.Client.Downloader;


public class Seeder implements Runnable {
	public static final int SEEDER_FIRST_PORT = 10000;
	public static final int SEEDER_LAST_PORT = 10100;
	public Downloader currentDownloader;
	private int port;
	private ServerSocket serverSocket;
	private short chunkSize;
	private LinkedList<SeederThread> threads;

    public Seeder(short chunkSize) {
    	try {
			serverSocket = new ServerSocket();
		} catch (IOException e) {
			System.out.println("Error creating server socket");
		}
    	port = getAvailablePort();
    	this.threads = new LinkedList<SeederThread>();
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
    
	public void run() {
		boolean alive = true;
		while (alive) {
			try {
				Socket clientSocket = serverSocket.accept();
				SeederThread newThread = new SeederThread(clientSocket, currentDownloader, chunkSize);
				newThread.start();
				threads.add(newThread);
			} catch (IOException e1) {
				System.out.println("Seed killed");
				alive = false;
				for (SeederThread s : threads)
					s.closeSocket();
			}
		}
	}
    
    public void start() {
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
 