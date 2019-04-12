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
    hundir_flota_interface jugador_espera = null;
	int numPartida = 0;
	
    ServicioJuegoImpl() throws RemoteException {
    }
	
    public void alta(hundir_flota_interface c) throws RemoteException {
		if(jugador_espera==null){
			jugador_espera = c;
			System.out.println("Jugador en espera");
		}
		else{
			try{
				System.out.println("Comenzamos partida");
				PartidaImpl partida = new PartidaImpl(jugador_espera, c, numPartida);	
				jugador_espera.asigna_partida(partida);
				c.asigna_partida(partida);
				numPartida++;
			}
			catch(RemoteException re){
				System.out.println(re.toString());
			}
			catch(Exception excp){
				System.out.println(excp.toString());
			}
			finally{
				jugador_espera=null;
			}
		}
    }
	
    public void baja(hundir_flota_interface c) throws RemoteException {
		jugador_espera=null;
    }
	
	public boolean hello () throws RemoteException {
		return true;
	}
	
	public boolean nick_registrado(String nombre, String pass) throws RemoteException {
		try {
				boolean valido = false;
								
				Class.forName("org.postgresql.Driver");
				Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT password FROM jugadores WHERE apodo=\'" + nombre + "\'");
				
				if(rs.next()){					
					if(pass.equals(rs.getString(1).replaceAll("\\s","")))
						valido = true;
				}
				
				return valido;
				
		} 
		catch (Exception ex) {
			System.out.println(ex.toString());
			throw new RemoteException("Servicio no disponible");
		}
	}
	
	public boolean comprueba_nick (String nombre) throws RemoteException {
		try{
			boolean valido = false;
								
				Class.forName("org.postgresql.Driver");
				Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM jugadores WHERE apodo=\'" + nombre + "\'");
				
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
	
	public boolean nuevo_nick(String nombre, String pass) throws RemoteException {
		try {
				boolean valido = false;
								
				Class.forName("org.postgresql.Driver");
				Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM jugadores WHERE apodo=\'" + nombre + "\'");
				
				if(!(rs.next())){					
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
	
	public String [] getLista() throws RemoteException{
		try{
				Class.forName("org.postgresql.Driver");
				Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/","salas","salas");
				Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = stmt.executeQuery("SELECT * FROM tabla_partidas ORDER BY ganadas desc");
				
				
				rs.last();
				int num_filas = rs.getRow();
				String [] cadena = new  String [num_filas*3];
				
				rs.first();
				int i = 0;
				do{
					for(int j = 0;j<3; j++)
						cadena[j + (i*3)] = rs.getString(j+1);
					
					i++;
				}while(rs.next());
				
				return cadena;
		}
		catch (Exception exth){
			System.out.println(exth.toString());
			throw new RemoteException();
		}
	}

}
