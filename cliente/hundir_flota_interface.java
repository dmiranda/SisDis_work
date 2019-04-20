
import java.rmi.*;

interface hundir_flota_interface extends Remote {
	public int tiro (int casilla) throws RemoteException;					//Recibe la casilla que pulsa el jugador contrincante y comprueba su resultado
	public void empieza_partida() throws RemoteException;					//Se avisa al jugador, que tiene un oponente
	public void listo() throws RemoteException;								//Se avisa al jugador, de que su oponente ha colocado todos sus barcos
	public void fin_partida(boolean fin) throws RemoteException;			//Avisa al jugador del fin de la partida
	public void asigna_partida(Partida miPartida) throws RemoteException;	//Asigna al objeto, la referencia al objeto Partida que gestionará el juego
	public void Turno() throws RemoteException;								//Avisa al jugador, de que es su turno
	public String getNombre() throws RemoteException;						//Devuelve un string con el nombre del usuario que ha iniciado este objeto 
}