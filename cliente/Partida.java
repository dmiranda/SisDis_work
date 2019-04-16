
import java.rmi.*;

interface Partida extends Remote {
    int		tiro(String user, int casilla) throws RemoteException;				//Recibe una casilla de un jugador, y comprueba su resultado con el oponente
	void 	listo(String user, int b1[], int b2[], int b3[], int b4[]) throws RemoteException;	//Un jugador avisa de que ha colocado sus barcos, y envía las posiciones
	void 	salida(String user) throws RemoteException;						//Un jugador avisa de que abandona la partida
	boolean	getTurno(String user) throws RemoteException;					//Un jugador comprueba si es su turno
}