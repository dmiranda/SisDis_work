
import java.rmi.*;
import java.sql.ResultSet;

interface ServicioJuego extends Remote {
    void 	alta(hundir_flota_interface c) throws RemoteException;
    void 	baja(hundir_flota_interface c) throws RemoteException;
	boolean hello() throws RemoteException;
	boolean comprueba_nick (String nombre) throws RemoteException;
	boolean nuevo_nick(String nombre, String pass) throws RemoteException;
	boolean nick_registrado(String nombre, String pass) throws RemoteException;
	String [] getLista() throws RemoteException;
}
