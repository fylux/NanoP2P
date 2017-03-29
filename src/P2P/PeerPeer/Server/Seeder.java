package P2P.PeerPeer.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import P2P.PeerPeer.Client.Downloader;
import P2P.util.PeerDatabase;

/**
 * Servidor que se ejecuta en un hilo propio.
 * Creará objetos {@link SeederThread} cada vez que se conecte un cliente.
 */
public class Seeder implements Runnable {
	public static final int SEEDER_FIRST_PORT = 10000;
	public static final int SEEDER_LAST_PORT = 10100;
	public Downloader currentDownloader;

	/**
	 * Base de datos de ficheros locales compartidos por este peer.
	 */
	protected PeerDatabase database;

    public Seeder(short chunkSize)
    {
    	//TODO
    	//hay que sincronizar la asignacion de puertos(probar hasta que pueda en escuchar en uno)
    }

    //Pone al servidor a escuchar en un puerto libre del rango y devuelve cuál es dicho puerto
    public int getAvailablePort() {
    	//TODO
    
    	return 0;
    }
    
    /** 
	 * Función del hilo principal del servidor. 	
	 */
	public void run()
	{
		//TODO
		//creados para quitar el warning
		ServerSocket serverSocket=null;
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			serverSocket.bind(new InetSocketAddress(10001));
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int chunkSize=0;
		//while forever
		//En algún momento se llamará a
		new SeederThread(clientSocket, database, currentDownloader, (short)chunkSize).start();
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    	//TODO
    }
    
    public int getSeederPort() {
    	//TODO
    	return 0;
    }
}
 