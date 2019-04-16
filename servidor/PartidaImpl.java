import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import java.io.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;


class PartidaImpl extends UnicastRemoteObject implements Partida {
	
	//Variables necesarias
    hundir_flota_interface jugadores[] = {null , null};
	int jugador_turno = 0;
	String ID_players [] = new String [2];
	
	//Para escribir el fichero del log, iniciamos un PrintWriter
	PrintWriter log_partida;
	
	//Constructor, que recibe los dos jugadores, y el numero de partida, que servirá para crear el fichero del log
    public PartidaImpl(hundir_flota_interface p1, hundir_flota_interface p2,int num) throws RemoteException {
		try{
			//Configuramos el jugador 1
			jugadores[0] = p1;
			ID_players[0] = p1.getNombre();
			jugadores[0].empieza_partida();
			
			//Configuramos el jugador 2
			jugadores[1] = p2; 
			ID_players[1] = p2.getNombre();			
			jugadores[1].empieza_partida();

			//Abrimos el fichero donde escribir
			log_partida = new PrintWriter(new FileWriter("partida"+num));
		}
		catch(RemoteException re){
			System.out.println("No se ha podido realizar conexion");
		}
		
		catch (IOException e) {
            System.err.println(e);
        }
		
		catch(Exception excp){
			System.out.println("Problemas en el servidor");
		}
		
    }
	
	//Método encargado de eliminar la partida, pues un jugador decide salir de ella
	public void salida(String user) throws RemoteException{
		try{
			//Comprobamos cuál de los dos jugadores es, y avisamos a su oponente de esta acción, enviando un false a su método "fin_partida"
			if(ID_players[0].equals(user))
				jugadores[1].fin_partida(false);
			else
				jugadores[0].fin_partida(false);
		}
		catch(Exception ex){
			System.out.println("No se puede conectar con el jugador");
		}
    }
	
	//Método que gestiona los tiros de un jugador:
	/*	- Recibe la casilla seleccionada
		- Manda la casilla al oponente
		- Recibe el resultado del tiro
		- Lo almacena en el log
		- Lo manda de vuelta al jugador que ha relizado el tiro
	*/
	public int	tiro(String user, int casilla) throws RemoteException{
		
		//Variables necesarias
		String result;
		int jugador_oponente;
		
		//Configuramos el jugador al que consultar el resultado del tiro
		if(jugador_turno == 1) jugador_oponente = 0;
		else jugador_oponente = 1;
		
		//Si efectivamente, el jugador que efectúa el tiro, es el que tiene el turno
		if(user.equals(ID_players[jugador_turno])){
			try{
				int resultado = jugadores[jugador_oponente].tiro(casilla);		//Comprobamos el resultado del tiro
				
				//Creamos un string para escribir en el log, en funcion del valor devuelto por el jugador oponente
				if(resultado == 0)	
					result = new String("Agua");
				else if(resultado == 1)
					result = new String("Tocado");
				else
					result = new String("Hundido");
				
				//Escribimos en el log, la acción y el resultado
				log_partida.println("> " + user + " lanza un disparo a casilla " + casilla + " -> " + result);
				log_partida.flush();
				
				if(resultado == 4)								//Si el resultado ha sido fin de partida (4), efectuamos el procedimiento antes de salir
					fin_partida(jugador_oponente);
				
				else											//Permutamos el turno del jugador
				{
					if(jugador_turno == 1) jugador_turno = 0;
					else jugador_turno = 1;
				
					jugadores[jugador_turno].Turno();			//Avisamos al jugador que tiene el turno, de este hecho
				}
				
				
				return resultado;
			}
			catch(Exception re){
				System.out.println(re.toString());
				throw new RemoteException();
			}
		}
		
		else
			throw new RemoteException("Este jugador no tiene el turno");
		
	}
	
	//Método que gestiona el "fijar mapa" del jugador
	// Este método, recibe la lista de las casillas donde se encuentran los barcos del jugador, y los almacena en un log
	public void listo(String user, int b1[], int b2[], int b3[], int b4[]) throws RemoteException{
		int jug;
		
		//En cada caso, avisamos al jugador oponente de que su contricante está listo
		if(user.equals(ID_players[0]))
		{
			jugadores[1].listo();
			jug = 1;
		}
		
		else if (user.equals(ID_players[1]))
		{
			jugadores[0].listo();
			jug = 2;
		}
		
		else
			throw new RemoteException ("Jugador no valido");
		
		//Imprimimos en el log, la posicion de los barcos
		log_partida.println("Disposicion de los barcos del jugador " + user);
		log_partida.println("\tSalvavidas " + b1[0]);
		log_partida.println("\tBuque " + b2[0] + "," + b2[1]);
		log_partida.println("\tAcorazado " + b3[0] + "," + b3[1] + "," + b3[2]);
		log_partida.println("\tPortaviones " + b4[0] + "," + b4[1] + "," + b4[2] + "," + b4[3] + "," + b4[4]);
		log_partida.flush();
	}
	
	//Método que devuelve un true si el jugador que la solicita tiene el turno, o false en caso de que sea su contrincante
	public boolean 	getTurno(String user) throws RemoteException{
		
		if(user.equals(ID_players[jugador_turno]))
			return true;
		
		else
			return false;
	}
	
	//Método que gestiona el fin de partida
	/*	- Recibe el ID del jugador perdedor
		- Llama al método en el oponente que le indica que ha ganado
		- Almacena la información en el log y en la base de datos
	*/
	public void fin_partida(int id) {
		String ganador = "NONE";
		String perdedor = "NONE";
		
		jugadores[id].fin_partida(true);				//Se avisa al perdedor, de que ha finalizado la partida
														//En este codigo, el propio cliente emite un mensaje por pantalla en caso de haber resultado ganador
		
		//Guardamos el nombre del user ganador en el string auxiliar
		if(id == 0) ganador = ID_players[1];
		else ganador = ID_players[0];
		
		//Guardamos en el String el nombre del perdedor
		perdedor = ID_players[id];
		
		//Escribimos en el log, el resultado de la partida
		log_partida.println("El ganador ha sido el jugador " + ganador);
		log_partida.flush();
		
		//Guardamos el resultado en la base de datos
		try {
			
			/**************BASE DE DATOS**********************/
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
			Statement stmt = con.createStatement();
			/*************************************************/
				
			//Obtiene el palmarés del jugador ganador
			ResultSet rs_gan = stmt.executeQuery("SELECT partidas,ganadas FROM tabla_partidas WHERE apodo=\'" + ganador + "\'");
			
			//Si existe en la base de datos
			if(rs_gan.next()){
				int [] partidas_gan = {rs_gan.getInt(1) + 1, rs_gan.getInt(2) + 1};	//Actualizamos el palmarés
				
				stmt.executeUpdate("UPDATE tabla_partidas SET " + "partidas = " + partidas_gan[0] + ", ganadas = " + partidas_gan[1] + " WHERE apodo = \'" + ganador + "\'" );	//Actualizamos en la base de datos
			}
			
			else				//Si no hay entradas para este jugador, insertamos una fila nueva
				stmt.executeUpdate("INSERT INTO tabla_partidas VALUES (\'" + ganador + "\'," + 1 + "," + 1 + ")");
			
			//Hacemos lo mismo con el jugador perdedor
			ResultSet rs_perd = stmt.executeQuery("SELECT partidas FROM tabla_partidas WHERE apodo=\'" + perdedor + "\'");
			
			//Si hay entradas, actualizamos su numero de partidas jugadas, si no, insertamos la fila nueva
			if(rs_perd.next()){
				int partidas_perd = rs_perd.getInt(1) + 1;
					
				stmt.executeUpdate("UPDATE tabla_partidas SET " + "partidas = " + partidas_perd  + " WHERE apodo = \'" + perdedor + "\'" );
			}
			
			else
				stmt.executeUpdate("INSERT INTO tabla_partidas VALUES (\'" + perdedor + "\'," + 1 + "," + 0 + ")");
				
		} 
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
