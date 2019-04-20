
import java.rmi.*;
import java.sql.ResultSet;

interface ServicioJuego extends Remote {
    void 	alta(hundir_flota_interface c) throws RemoteException;				//Un jugador solicita una nueva partida
    void 	baja(hundir_flota_interface c) throws RemoteException;				//Un jugador abandona el servidor
	boolean hello() throws RemoteException;										//El servidor responde, como medida para comprobar conexion
	boolean comprueba_nick (String nombre) throws RemoteException;				//Comprueba la base de datos en busca del nick facilitado
	boolean nuevo_nick(String nombre, String pass) throws RemoteException;		//Se registra un nuevo nick, junto con su contraseña
	boolean nick_registrado(String nombre, String pass) throws RemoteException;	//Iniciar una nueva sesion con el nombre y contraseña
	String [] getLista() throws RemoteException;								//Devuelve los un string con todos los datos de la base de datos
}