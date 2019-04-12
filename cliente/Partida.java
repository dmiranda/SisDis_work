
import java.rmi.*;

interface Partida extends Remote {
    int		tiro(String user, int casilla) throws RemoteException;
	void 	listo(String user, int b1[], int b2[], int b3[], int b4[]) throws RemoteException;
	void 	salida(String user) throws RemoteException;
	boolean	getTurno(String user) throws RemoteException;
	void	fin_partida(int id) throws RemoteException;
}