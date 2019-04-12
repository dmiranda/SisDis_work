
import java.rmi.*;

interface hundir_flota_interface extends Remote {
	public int tiro (int casilla) throws RemoteException;		//Recibe la casilla que pulsa el jugador contrincante y comprueba si es agua/tocado
	public void empieza_partida() throws RemoteException;	//Se le pasa a este objeto, la referencia de otro objeto para que se comuniquen entre ellos
	public void listo() throws RemoteException;	//Un jugador avisa al otro de que está a la espera
	public void fin_partida() throws RemoteException;	//Un jugador avisa al otro de que ha destruido todos sus barcos
	public void asigna_partida(Partida miPartida) throws RemoteException;	
	public void Turno() throws RemoteException;
	public String getNombre() throws RemoteException;
}
