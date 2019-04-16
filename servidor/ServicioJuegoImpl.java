import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

class ServicioJuegoImpl extends UnicastRemoteObject implements ServicioJuego {
    hundir_flota_interface jugador_espera = null;	//Guarda la referencia al objeto hundir_flota
	int numPartida = 0;								//Cuenta el numero de partidas jugadas en el servidor
	
	//Constructor
    ServicioJuegoImpl() throws RemoteException {
    }
	
	//Método que ejecuta cuando un jugador solicita una nueva partida
	//	- Recibe como parámetro la referencia al objeto
    public void alta(hundir_flota_interface c) throws RemoteException {
		
		
		if(jugador_espera==null) 							//Si no hay jugadores en espera, se guarda su referencia
		{
			jugador_espera = c;
			//System.out.println("Jugador en espera");
		}
		else												//Si ya hay un jugador esperando
		{	
			try{
				//System.out.println("Comenzamos partida");
				PartidaImpl partida = new PartidaImpl(jugador_espera, c, numPartida);	//Se crea un objeto partida, con ambos jugadores, y el num de partida
				jugador_espera.asigna_partida(partida);									//Se pasa la referencia del objeto partida al jugador en espera
				c.asigna_partida(partida);												//Se pasa la referencia tambien, al otro jugador, oponente del anterior
				numPartida++;															//Se incrementa el numero de partidas
			}
			catch(RemoteException re){
				System.out.println(re.toString());
			}
			catch(Exception excp){
				System.out.println(excp.toString());
			}
			finally{
				jugador_espera=null;													//Si no ha habido ningún error, se elimina el jugador en espera
			}
		}
    }
	
	
	//Método que elimina el jugador en espera, si es el mismo que lo ha solicitado
    public void baja(hundir_flota_interface c) throws RemoteException {
		if(jugador_espera==c) jugador_espera = null;
    }
	
	//Método auxiliar para comprobar conexión
	public boolean hello () throws RemoteException {
		return true;
	}
	
	//Método que gestiona el inicio de sesion de un usuario
	// 	- Recibe el nombre y la contraseña
	//	- Comprueba que ambos son correctos y devuelve true en caso positivo, y false si no son correctos
	public boolean nick_registrado(String nombre, String pass) throws RemoteException {
		try {
			boolean valido = false;
								
			/**********CONSULTA BASE DE DATOS*****************/	
			//Comprueba si existe la clase necesaria, y se conecta a la base de datos
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
			Statement stmt = con.createStatement();
			
			//Comprueba si hay algun usuario cuyo nombre coincida con el nombre pasado como parametro, y obtiene su contraseña asignada
			ResultSet rs = stmt.executeQuery("SELECT password FROM jugadores WHERE apodo=\'" + nombre + "\'");
			
			/***********FIN DE CONSULTA***********************/
				
			if(rs.next())													//Si se han obtenido datos en la consulta								
			{					
				if(pass.equals(rs.getString(1).replaceAll("\\s","")))		//Comprueba si la contraseña pasada por parametros es la que existe en la BDD
					valido = true;
			}
				
			return valido;
				
		} 
		catch (Exception ex) {
			System.out.println(ex.toString());
			throw new RemoteException("Servicio no disponible");
		}
	}
	
	//Método que comprueba si el nombre pasado como parametro, existe en la base de datos
	//	- Devuelve true en caso de que no exista, y false en caso de que exista una entrada
	public boolean comprueba_nick (String nombre) throws RemoteException {
		try{
			boolean valido = false;
								
			/**********CONSULTA BASE DE DATOS*****************/	
			//Comprueba si existe la clase necesaria, y se conecta a la base de datos
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
			Statement stmt = con.createStatement();
			
			//Busca en la tabla de usuarios aquella entrada que coincida con el nombre pasada como parametro
			ResultSet rs = stmt.executeQuery("SELECT * FROM jugadores WHERE apodo=\'" + nombre + "\'");
				
			/***********FIN DE CONSULTA***********************/
			
			//Si no ha habido coincidencias, devuelve true
			if(!rs.next()){					
				valido = true;
			}
				
			return valido;
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
			throw new RemoteException("Servicio no disponible");
		}
	}
	
	//Método que recoge nombre y contraseña de un nuevo usuario, e intenta añadirlo a la base de datos
	//	- Devuelve true en caso de que no hay habido ningun problema, false en caso contrario
	public boolean nuevo_nick(String nombre, String pass) throws RemoteException {
		try {
			boolean valido = false;
								
			/**********CONSULTA BASE DE DATOS*****************/	
			//Comprueba si existe la clase necesaria, y se conecta a la base de datos
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
			Statement stmt = con.createStatement();
			
			//Busca en la base de datos si existe una entrada con el nombre obtenido como parametro
			ResultSet rs = stmt.executeQuery("SELECT * FROM jugadores WHERE apodo=\'" + nombre + "\'");
			
			/*************FIN DE CONSULTA********************/
				
			//En caso de que no existan entradas, inserta una fila con los datos
			if(!(rs.next()))									 
			{					
				stmt.executeUpdate("INSERT INTO jugadores " + "VALUES (\'" + nombre + "\',\'" + pass + "\')");
				valido = true;
			}
				
			return valido;
				
		} 
		catch (Exception ex) {
			System.out.println(ex.toString());
			throw new RemoteException("Servicio no disponible");
		}
	}
	
	//Método que devuelve un array con todos los datos que obtiene de la base de datos
	public String [] getLista() throws RemoteException{
		try{
			
			/**********CONSULTA BASE DE DATOS*****************/	
			//Comprueba si existe la clase necesaria, y se conecta a la base de datos
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			
			//Obtiene todos los datos almacenados en la tabla de partidas de la base de datos, ordenadas por partidas ganadas en orden ascendiente
			ResultSet rs = stmt.executeQuery("SELECT * FROM tabla_partidas ORDER BY ganadas desc");
				
			/*************FIN DE CONSULTA**********************/
			
			//Obtenemos el numero de filas
			rs.last();
			int num_filas = rs.getRow();
			
			String [] cadena = new  String [num_filas*3];						//Creamos un arrray con el tamaño suficiente
			
			rs.first();															//Movemos el ResultSet para que indique al primer elemento
			int i = 0;
			
			do{																	//Creamos un bucle que recorra el ResultSet, y almacene los elementos en cadena
				for(int j = 0;j<3; j++)
					cadena[j + (i*3)] = rs.getString(j+1);
				
				i++;
			}while(rs.next());
				
			return cadena;														//Devolvemos el array con todos los datos
		}
		catch (Exception exth){
			System.out.println(exth.toString());
			throw new RemoteException();
		}
	}

}
