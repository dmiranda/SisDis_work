
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.*;
import java.rmi.server.*;

public class hundir_flota extends UnicastRemoteObject implements hundir_flota_interface {
		
	//Variables necesarias para la comunicacion con el objeto partida
	Partida partida = null;
	String nombre_user = "NONE";
	private boolean contricante_listo = false;
	boolean espera = false;
	

	//Variables para la creacion del tablero en formato grafico
	private int N_botones=100;
	private int nColumnasMapa=10;
	private int MiPartida = 60;
	private int CPUPartida = 610;
	private int ListaBarcos = CPUPartida +	nColumnasMapa*50 + 20;

	//Conjunto de elementos utilizados
	private JButton mi_partida[]= new JButton[N_botones];
	private JButton mi_mapa[]= new JButton[N_botones];
	private Label titulo1,titulo2;
	private JButton c1,c2,c3,c4;
	private JButton comenzar, salir, instrucciones;
	private Frame ventana,ventana_espera;
	private int barco_seleccionado=1;
	
	//Clases que gestionan los botones
	PulsaMapaPartida pmp = new PulsaMapaPartida();
	PulsaMiMapa pmc = new PulsaMiMapa();
	Coloca_barcos colocar = new Coloca_barcos();
	
	//Posiciones de los barcos
	private int salvavidas []= {200};
	private int buque []= {200,200};
	private int acorazado []= {200,200,200};
	private int portaviones []= {200,200,200,200,200};
	
	//Situacion de los barcos 
	private boolean barcos_colocados[] = {false,false,false,false};
	private boolean barcos_destruidos[] = {false,false,false,false};
	
	//iconos mapa
	private Icon ic_agua=new ImageIcon("./iconos/water_drop_mini.png");
	private Icon ic_int = new ImageIcon("./iconos/interrogante_mini.jpg");
	private Icon ic_tocado=new ImageIcon("./iconos/touch_mini.jpg");
	private Icon ic_extremo=new ImageIcon("./iconos/extremo_mini.png");
	private Icon ic_medio = new ImageIcon("./iconos/medio_mini.png");
	
	//Constructor de toda la interfaz grafica
	public void showButton(){

		/*************************************************
			Construimos los mapas
		*************************************************/
		//Construimos el mapa a la izquierda de la pantalla
		int x=MiPartida; 
		int y=80;
		int j=0;
		
		//Creamos el titulo del mapa del enemigo
		titulo1 = new Label("Mapa enemigo");		
		titulo1.setBounds(MiPartida,20,100,50);
		ventana.add(titulo1);
		
		
		//Creamos el mapa de la izquierda, que nos servirá para encontrar los barcos de nuestro oponente
		for(int i=0;i<=N_botones-1;i++,x+=50,j++){
			mi_partida[i]=new JButton();
			if(j==nColumnasMapa)
				{j=0; y+=50; x=MiPartida;}
			
			mi_partida[i].setBounds(x,y,50,50);
			mi_partida[i].setIcon(ic_int);
			ventana.add(mi_partida[i]);
		}
		
		//Pasamos a construir el mapa a la derecha de la pantalla
		x=CPUPartida; 
		y=80; 
		j=0;
		
		//Asignamos su titulo
		titulo2 = new Label("Tu mapa");		
		titulo2.setBounds(CPUPartida+((nColumnasMapa*50)-60),20,100,50);
		ventana.add(titulo2);
		
		
		//Creamos el mapa de la derecha, que servirá para colocar nuestra flota
		for(int i=0;i<=N_botones-1;i++,x+=50,j++){
			mi_mapa[i]=new JButton();
			if(j==nColumnasMapa)
				{j=0; y+=50; x=CPUPartida;}
			
			mi_mapa[i].setBounds(x,y,50,50);
			ventana.add(mi_mapa[i]);
			mi_mapa[i].addActionListener(pmc);
		}
		
		//Creamos la lista de los barcos que debemos colocar en el mapa
		// Esta lista se irá clickando para seleccionar el barco que colocamos en cada momento
		c1=new JButton("Bote salvavidas (1 casilla)");
		c2=new JButton("Buque (2 casillas)");
		c3=new JButton("Acorazado (3 casillas)");
		c4=new JButton("Portaviones (5 casillas)");
		Label lista = new Label("Barcos a colocar:");
		lista.setBounds(ListaBarcos,80,200,20);
		ventana.add(lista);
		c1.setBounds(ListaBarcos,130,215,20);
		c2.setBounds(ListaBarcos,160,165,20);
		c3.setBounds(ListaBarcos,190,190,20);
		c4.setBounds(ListaBarcos,220,200,20);
		c1.addActionListener(colocar);
		c2.addActionListener(colocar);
		c3.addActionListener(colocar);
		c4.addActionListener(colocar);
		ventana.add(c1); 
		ventana.add(c2);
		ventana.add(c3);
		ventana.add(c4);
		
		/*******************************
		Creamos los botones en la parte baja de la pantalla
		*******************************/
		
		//COMENZAR PARTIDA (habrá que pulsar este botón para que nuestros barcos queden fijados en el mapa e iniciar la partida contra la CPU)
		comenzar=new JButton("Comenzar partida");
		comenzar.setBounds(500,y+100,200,50);
		ventana.add(comenzar);
		comenzar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Fija_mapa(evt);
            }
        });
		
		//Cerrar la ventana
		salir=new JButton("Salir de la partida");
		salir.setBounds(800,y+100,200,50);
		ventana.add(salir);
		salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CerrarPartida(evt);
            }
        });
		
		//Leer las instrucciones para jugar
		instrucciones=new JButton("Cómo jugar");
		instrucciones.setBounds(100,y+100,150,50);
		ventana.add(instrucciones);
		instrucciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Instrucciones(evt);
            }
        });
		

	}
	
	
	/*	Permite clickar sobre el mapa del contrincante para iniciar la partida
			- Se encarga de activar los botones del mapa donde tendremos que clickar para lanzar misiles al contrincante
	*/
	public void Mapa_enemigo(){

		//Recorremos el mapa y asignamos el método
		for(int i=0;i<=N_botones-1;i++){
			mi_partida[i].addActionListener(pmp);
		}
		
		try{
			//Consultamos con el objeto partida, si el turno pertenece a este jugador
			if(!partida.getTurno(nombre_user)){			
				muestra_ventana_turno();
						
				//System.out.println("Esperando turno");
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
	}
	
	//Método auxiliar para abrir la ventana de espera de turno
	private void muestra_ventana_turno(){
		ventana_espera = new Frame("Turno del oponente");
		ventana_espera.setSize(200,200);
		JLabel mensajito = new JLabel("Espera tu turno");
		mensajito.setBounds(100,100,50,50);
		ventana_espera.add(mensajito);
		ventana_espera.setVisible(true);
	}
	
	//Método que elimina la pantalla de espera de la interfaz, y permite al jugador realizar un tiro
	//	-	Este método es llamado por el objeto partida que gestiona la propia partida, para permitir al jugador realizar un tiro
	public void Turno() throws RemoteException {
		ventana_espera.setVisible(false);
		ventana_espera.dispose();
		//System.out.println("Turno mio");
	}
	
	//Crea la ventana y llama al constructor de la interfaz grafica
	//	Recibe como parámetro, el nombre del usuario que solicita iniciar la partida
	public hundir_flota(String user) throws RemoteException{
		
		//Asignamos a este objeto el nombre del usuario que solicita partida
		nombre_user = user;
		
		//Antes de crear el tablero de la partida, se crea una ventana que indica que se está a la espera del jugador oponente
		ventana_espera = new Frame("Espere....");
		ventana_espera.setSize(300,100);
		ventana_espera.setLocationRelativeTo(null);
		
		//Configuramos el procedimiento al cerrar la ventana de espera
		//	-	Indicamos al objeto partida para que elmine la referencia a este jugador
		//	-	Y si, se ha creado el tablero de la partida, se cerrará
		ventana_espera.addWindowListener(new WindowListener(){
            public void windowOpened(WindowEvent e){}
            public void windowActivated(WindowEvent e){}
            public void windowDeactivated(WindowEvent e){}
            public void windowIconified(WindowEvent e){}
            public void windowDeiconified(WindowEvent e){}
            public void windowClosed(WindowEvent e){}
            public void windowClosing(WindowEvent e){
                ventana_espera.dispose();
				try{
					if(partida != null)	partida.salida(nombre_user);
				}
				catch(Exception ex){
					System.out.println(ex.toString());
				}
				if(ventana != null) ventana.dispose();
            }
        });

		JLabel mensajito = new JLabel("Esperando a oponente");
		mensajito.setBounds(10,20,20,20);
		ventana_espera.add(mensajito);
		
		ventana_espera.setVisible(true);
	}
		
	//Método que asigna la partida a la que pertenece este jugador
	/*	- Es llamado desde la clase juegos, que recibe la partida desde el servidor
	*/
	public void asigna_partida(Partida miPartida) throws RemoteException{
		partida = miPartida;
	}
	
	//Método que se encarga de construir la interfaz de usuario
	/*	- Se crea el frame y se activan todas las acciones
		- Se llama al método showButton, que construye todos los botones de la interfaz gráfica
	*/
	public void iniciar_juego (){
		
		ventana = new Frame("Hundir la flota");
		
		/*Configuramos el procedimiento a seguir al cerrar la ventana
			- Avisamos al objeto partida
			- Si hay alguna ventana de espera activada, se cerrará también
		*/	 
		ventana.addWindowListener(new WindowListener(){
            public void windowOpened(WindowEvent e){}
            public void windowActivated(WindowEvent e){}
            public void windowDeactivated(WindowEvent e){}
            public void windowIconified(WindowEvent e){}
            public void windowDeiconified(WindowEvent e){}
            public void windowClosed(WindowEvent e){}
            public void windowClosing(WindowEvent e){
                ventana.dispose();
				
				try{
					if(partida != null)	partida.salida(nombre_user);
				}
				catch(Exception ex){
					System.out.println(ex.toString());
				}
				
				if(ventana_espera != null) ventana_espera.dispose();
				
            }
        });
		
		ventana.setLayout(null);
		
		// Dibujamos el tablero de la partida, llamando al método
		showButton();
		
		ventana.setSize(1500,1400);
		ventana.setVisible(true);
	}
	
	//Método para gestionar la acción tiro por parte del oponente
	/*
		- Este método es llamado desde el objeto partida
		- Recibe la casilla a la se ha lanzado el tiro
		- Comprueba si corresponde con alguno de los barcos, recorriendo todos los barcos
		- Devuelve:
			0 -> Agua
			1 -> Tocado
			2 -> Tocado y Hundido
			4 -> Tocado, Hundido y fin de partida
			
		- Comprueba si, tras el tiro, se han hundido todos los barcos. En caso afirmativo, llama al metodo fin_partida para indicar que se ha acabado la partida.
	*/
	public int tiro (int casilla) throws RemoteException{
		int tocado = 0;
		
		if(barcos_destruidos[0]==false)
		{
			for(int i=0;i<1;i++)
			{
				if(salvavidas[i]==casilla)
				{
					tocado = 2;					//Valor (2) debido a que ya se ha hundido todo el barco
					salvavidas[i]=300;			//Cambiamos el valor en el array que guarda la posicion de este barco
					barcos_destruidos[0]=true;	//Indicamos que este barco ya ha sido hundido
				}
			}
		}
		
		//Antes de recorrer el siguiente barco, comprobamos que no se ha encontrado ya en los anteriores
		//	O que este barco aún no se ha hundido completamente
		if((tocado == 0) && (barcos_destruidos[1]==false))
		{
			for(int i=0;i<2;i++)
			{
				if(buque[i]==casilla)
				{
					tocado = 1;
					buque[i]=300;
					
					if((buque[0]==300) && (buque[1]==300)){
						barcos_destruidos[1]=true;
						tocado = 2;
					}
				}
			}
		}
		
		//Antes de recorrer el siguiente barco, comprobamos que no se ha encontrado ya en los anteriores	
		//	O que este barco aún no se ha hundido completamente		
		if ((tocado == 0) && (barcos_destruidos[2]==false))
		{
			for(int i=0;i<3;i++)
			{
				if(acorazado[i]==casilla)
				{
					tocado = 1;
					acorazado[i]=300;
					
					if((acorazado[0]==300) && (acorazado[1]==300) && (acorazado[2]==300)){
						tocado = 2;
						barcos_destruidos[2]=true;
					}
				}
			}
		}
			
		//Antes de recorrer el siguiente barco, comprobamos que no se ha encontrado ya en los anteriores
		//	O que este barco aún no se ha hundido completamente
		if((tocado == 0) && (barcos_destruidos[3]==false))
		{
			for(int i=0;i<5;i++)
			{
				if(portaviones[i]==casilla)
				{
					tocado = 1;
					portaviones[i]=300;
					
					if((portaviones[0]==300) && (portaviones[1]==300) && (portaviones[2]==300) && (portaviones[3]==300) && (portaviones[4]==300)){
						tocado = 2;
						barcos_destruidos[3]=true;
					}
				}
			}
		}
		
		//Cambiamos el icono en el tablero, dependiendo del resultado de la búsqueda
		if(tocado != 0)
			mi_mapa[casilla].setIcon(ic_tocado);
		else
			mi_mapa[casilla].setIcon(ic_agua);
		
		//Comprobamos si aún tenemos barcos sin localizar en nuestro tablero
		// En caso contrario, devolvemos el valor consecuente (4)
		if(barcos_destruidos[0] && barcos_destruidos[1] && barcos_destruidos[2] && barcos_destruidos[3]){
			
			ventana_espera.setVisible(false);
			ventana_espera.dispose();				
			tocado = 4;			
		}
		
		return tocado;
	}
	
	/*Método que indica al jugador que su contrincante ya está listo
		- Este metodo es llamado por el objeto partida
		- Cierra la ventana de espera
		- Activa el mapa de la derecha
	*/
	public void listo() throws RemoteException{
		contricante_listo = true;
		//System.out.println("Contrincante listo");
		if(espera){
			ventana_espera.setVisible(false);
			ventana_espera.dispose();
			
			Mapa_enemigo();		//Activa el mapa de la derecha para que el jugador pueda clickar en él
		}
	}
	
	//Método que indica el fin de la partida por parte del oponente
	/*	- El objeto partida, llama a este método, indicando como  paramétro un boolean que indica:
				false -> La partida no ha finalizado, pero se ha perdido la conexion con el otro jugador
				true -> La partida ha finalizado, y por tanto, este jugador ha perdido
	*/
	public void fin_partida(boolean fin){
		
		
		if(ventana_espera != null) ventana_espera.dispose();		//Comprobamos que no haya ventana de espera abierta en la pantalla del jugador
		
		//Configuramos una ventana de espera que indique al jugador que ha finalizado la partida
		ventana_espera = new Frame("FIN partida");
		ventana_espera.setSize(300,150);
		ventana_espera.setLocationRelativeTo(null);
		
		ventana_espera.addWindowListener(new WindowListener(){
            public void windowOpened(WindowEvent e){}
            public void windowActivated(WindowEvent e){}
            public void windowDeactivated(WindowEvent e){}
            public void windowIconified(WindowEvent e){}
            public void windowDeiconified(WindowEvent e){}
            public void windowClosed(WindowEvent e){}
            public void windowClosing(WindowEvent e){
				
				//Configuramos la ventana para que al cerrarlo se cierra el tablero de la partida
                ventana_espera.dispose();
				try{
					if(partida != null)	partida.salida(nombre_user);	//Antes de salir, avisamos al objeto partida para que borre la referencia de este objeto
				}
				catch(Exception ex){
					System.out.println(ex.toString());
				}
				
				if(ventana != null) ventana.dispose();		//Cerramos el tablero de la partida
            }
        });

		//Pasamos a configurar el mensaje que se muestra en función del parámetro de entrada
		//	true -> fin de la partida
		//	false -> partida acabada inesperadamente
		JLabel mensajito;
		if(fin)
			mensajito = new JLabel("¡¡LO SIENTO, HAS PERDIDO!!");
		
		else
			mensajito = new JLabel("El oponente ha salido de la partida");
		
		//Incluimos el mensaje en la ventana de espera
		mensajito.setBounds(10,20,20,20);
		ventana_espera.add(mensajito);
		
		//Mostramos la ventana
		ventana_espera.setVisible(true);

	}
	
	//Método que indica el comienzo de la partida
	/*	- El objeto Partida llama a este método para indicar al jugador que ya hay un oponente
		- Se llama al método iniciar_juego antes de salir
	*/
	public void empieza_partida() throws RemoteException{
		
		ventana_espera.removeAll();
		ventana_espera.setVisible(false);
		ventana_espera = null;
		
		iniciar_juego();	//Llama al método que se encarga de construir el tablero
	}
	
	//Gestiona las pulsaciones sobre el mapa de la izquierda, es decir, el mapa del contrincante
	/* 	- Al pulsar sobre el mapa, y se captura la casilla pulsada
		- El objeto partida nos devuelve:
			0 -> Dibujamos un icono de agua
			1 -> Dibujamos un icono de tocado
			2 -> Indicamos al usuario que se ha hundido uno de los barcos del oponente
	*/
	class PulsaMapaPartida implements ActionListener{
        public void actionPerformed(ActionEvent e){
			
			//Obtenemos el botón que ha ocasiado la llamada de este método
            JButton boton_pulsado = (JButton)e.getSource();
			
			//Recorremos los botones, para localizarlo
			for(int j=0; j<N_botones; j++){
				if(mi_partida[j]==boton_pulsado){
					//System.out.println("Boton presionado: " + j);
					
					try{
						//Antes de iniciar ninguna gestión, comprobamos si el jugador que intenta realizar un tiro, es el que tiene el turno
						if(partida.getTurno(nombre_user))
						{
							mi_partida[j].removeActionListener(pmp);	//Eliminamos la acción al botón
							
							int tocado = partida.tiro(nombre_user,j);	//Consultamos el resultado del tiro, a través del objeto partida
							
							
							if((tocado == 1) || (tocado == 4))			//Si el resultado ha sido tocado(1) o fin de partida(4), solo cambiamos el icono	
								mi_partida[j].setIcon(ic_tocado);
							
							
							else if(tocado == 2){						//Si el resultado es tocado y hundido(2), cambiamos el icono y mostramos mensaje 
								mi_partida[j].setIcon(ic_tocado);
								
								//Para avisar al jugador de que ha hundido un barco, creamos una pequeña ventana de aviso
								// Usamos una vetana, y no un JOption para que siga funcionando con normalidad pese a no cerrar la ventana
								JFrame ventanita = new JFrame("Tocado y hundido");
								ventanita.addWindowListener(new WindowListener(){
									public void windowOpened(WindowEvent e){}
									public void windowActivated(WindowEvent e){}
									public void windowDeactivated(WindowEvent e){}
									public void windowIconified(WindowEvent e){}
									public void windowDeiconified(WindowEvent e){}
									public void windowClosed(WindowEvent e){}
									public void windowClosing(WindowEvent e){
										ventanita.dispose();
									}
								});
								
								JLabel mensaje = new JLabel("¡Barco hundido!");
								mensaje.setBounds(10,20,20,20);
								
								ventanita.add(mensaje);
								ventanita.setSize(300,150);
								ventanita.setLocationRelativeTo(null);
								ventanita.setVisible(true);
								
							}
					
							else										//En caso contrario, el resultado es agua (0)
								mi_partida[j].setIcon(ic_agua);
						
							//Mostramos ventana de espera de turno, Si y solo Si no se ha acabado la partida
							if(tocado != 4){
								//Antes de mostrarla, debemos comprobar que no es nuestro turno, para que no haya problemas con los turnos
								if(!partida.getTurno(nombre_user))
									muestra_ventana_turno();
							}
							
							else {
								//Si el resultado es 4, mostramos mensaje de victoria, y cerramos la ventana
								JOptionPane.showMessageDialog(ventana, "¡ENHORABUENA, HAS GANADO!");
								ventana.dispose();
							}
						}
						
						else			//Si no es turno de este jugador, lo indicamos al jugador
							JOptionPane.showMessageDialog(ventana,"Turno del contrincante");
						
					}
					catch(RemoteException fail){
						JOptionPane.showMessageDialog(ventana,"PERDIDA LA CONEXIÓN CON EL CONTRINCANTE");
						ventana.dispose();
					}
				}
			}
		}
    }
	
	//Gestiona todo el mapa de la derecha, donde el jugador tiene que colocar los barcos de su mapa
	/* 	- Se obtiene el barco seleccionado de la lista, para saber qué barco se coloca a cada momento
		- Se marca la posición seleccionada, y se comprueba en cada instante que es una posición válida. Esto quiere decir que sean dos celdas consecutivas en horizontal o vertical
	*/
	class PulsaMiMapa implements ActionListener{
        public void actionPerformed(ActionEvent e){
			
            JButton boton_pulsado = (JButton)e.getSource();		//obtenemos el botón que ha llamado a este método
			int casilla=200;									//Iniciamos una variable auxiliar
			boolean nueva_posicion=false;						//Iniciamos un flag
			
			//En primer lugar, miramos qué boton se ha pulsado
			for(int j=0; j<N_botones; j++){
				if(mi_mapa[j]==boton_pulsado){
					//System.out.println("Boton presionado en la CPU: " + j);
					casilla = j;							//Guardamos el numero de casilla
					break;									//Salimos del bucle
				}
			}
			
			
			if((barco_seleccionado == 1) && (mi_mapa[casilla].getIcon() == null))			//Si el barco a colocar es el salvavidas (1 casilla) y la casilla esta vacia
			{
				
				if(salvavidas[0] != 200)		//Si tiene casilla asignada, se borra el icono en dicha casilla
				{
					mi_mapa[salvavidas[0]].setIcon(null);
				}

				salvavidas[0] = casilla;						//Almacenamos la casilla asignada
				boton_pulsado.setIcon(ic_extremo);				//La marcamos con el icono correspondiente
				barcos_colocados[barco_seleccionado-1]=true;	//Marcamos el salvavidas como colocado
				//System.out.println("Salvavidas en " + salvavidas[0]);	
			}
			
			else if(barco_seleccionado == 2)	//Si el barco a colocar es el buque (2 casillas)
			{
				if(mi_mapa[casilla].getIcon() != null) //Si la casilla no esta vacia
				{
					if(buque[0] != casilla && buque[1]!=casilla)	//Comprobamos si coincide con alguna casilla ya asignada al buque
						for(int i=0; i<2;i++)		//Se limpia todos los iconos cambiados asignados al buque
							if(buque[i]!=200)
							{
								mi_mapa[buque[i]].setIcon(null);
								buque[i]=200;
							}
				}
				
				else if(buque[0] != 200)		//Si la primera casilla está asignada
				{
					if(buque[1] != 200)			//Si la segunda casilla también está asignada
					{
						nueva_posicion=true;	//Se marca el flag para cambiar todas las casillas
					}
					
					else						//Si la segunda casilla no se ha asignado
					{
						if((buque[0]+10==casilla) || (buque[0]-10==casilla) || ((((buque[0]+1)%10)!=0) && (buque[0]+1==casilla)) || ((((buque[0])%10)!=0) && (buque[0]-1==casilla)))	//Se mira si es una casilla válida para colocar
						{
							buque[1]=casilla;	//Se asigna la casilla al segundo pin del barco	
							boton_pulsado.setIcon(ic_extremo);	//Se cambia el icono
							barcos_colocados[barco_seleccionado-1]=true; //Marcamos el buque como colocado
						}
						
						else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
							nueva_posicion=true;
							
					}
				}
				
				else							//Si la primera casilla no se ha asignado, se marca para nueva posicion
					nueva_posicion = true;
				
				if(nueva_posicion)				//Si tras las comprobaciones, la posicion del barco es nueva
				{
					for(int i=0; i<2;i++)		//Se limpia todos los iconos cambiados asignados al buque
						if(buque[i]!=200)
							mi_mapa[buque[i]].setIcon(null);
					
					buque[0]=casilla;			//Se asigna el primer pin a la casilla pulsada
					mi_mapa[casilla].setIcon(ic_extremo);	//Se cambia el icono
					buque[1]=200;				//Se limpia el resto de pines
					barcos_colocados[barco_seleccionado-1]=false; //Desmarcamos el buque como colocado
				}
					
				
				//System.out.println("Buque en {" + buque[0] +"," + buque[1] + "}");
			}
			
			
			else if(barco_seleccionado == 3)	//Si el barco a colocar es el acorazado (3 casillas)
			{
				if(mi_mapa[casilla].getIcon() != null) //Si la casilla no esta vacia
				{
					if(acorazado[0] != casilla && acorazado[1]!=casilla && acorazado[2]!=casilla)	//Comprobamos si coincide con alguna casilla ya asignada al acorazado
						for(int i=0; i<3;i++)		//Se limpia todos los iconos cambiados asignados al acorazado
							if(acorazado[i]!=200)
							{
								mi_mapa[acorazado[i]].setIcon(null);
								acorazado[i]=200;
							}
				}
				
				else if(acorazado[0] != 200)				//Si la primera casilla está asignada
				{
					if(acorazado[1] != 200)			//Si la segunda casilla también está asignada
					{
						if(acorazado[2] != 200)			//Si la tercera casilla también está asignada
						{
							nueva_posicion=true;	//Se marca el flag para cambiar todas las casillas
						}
						
						else						//Si la tercera casilla no se ha asignado
						{
							if((acorazado[1]+10==casilla) || (acorazado[1]-10==casilla) || ((((acorazado[1]+1)%10)!=0) && (acorazado[1]+1==casilla)) || ((((acorazado[1])%10)!=0) && (acorazado[1]-1==casilla)))	//Se mira si es una casilla válida para colocar
							{
								acorazado[2]=casilla;	//Se asigna la casilla al segundo pin del barco	
								boton_pulsado.setIcon(ic_extremo);	//Se cambia el icono
								barcos_colocados[barco_seleccionado-1]=true; //Marcamos el acorazado como colocado
							}
						
							else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
								nueva_posicion=true;
							
						}
					}
					
					else						//Si la segunda casilla no se ha asignado
					{
						if((acorazado[0]+10==casilla) || (acorazado[0]-10==casilla) || ((((acorazado[0]+1)%10)!=0) && (acorazado[0]+1==casilla)) || ((((acorazado[0])%10)!=0) && (acorazado[0]-1==casilla)))	//Se mira si es una casilla válida para colocar
						{
							acorazado[1]=casilla;	//Se asigna la casilla al segundo pin del barco	
							boton_pulsado.setIcon(ic_medio);	//Se cambia el icono
						}
						
						else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
							nueva_posicion=true;
							
					}
				}
				
				else							//Si la primera casilla no se ha asignado, se marca para nueva posicion
					nueva_posicion = true;
				
				if(nueva_posicion)				//Si tras las comprobaciones, la posicion del barco es nueva
				{
					for(int i=0; i<3;i++)		//Se limpia todos los iconos cambiados asignados al buque
						if(acorazado[i]!=200)
							mi_mapa[acorazado[i]].setIcon(null);
					
					acorazado[0]=casilla;			//Se asigna el primer pin a la casilla pulsada
					mi_mapa[casilla].setIcon(ic_extremo);	//Se cambia el icono
					acorazado[1]=200;				//Se limpia el resto de pines
					acorazado[2]=200;
					barcos_colocados[barco_seleccionado-1]=false; //Desmarcamos el acorazado como colocado
				}
					
				
				//System.out.println("Acorazado en {" + acorazado[0] +"," + acorazado[1] +"," + acorazado[2] + "}");
			}
			
			else if(barco_seleccionado == 4)	//Si el barco a colocar es el acorazado (3 casillas)
			{
				if(mi_mapa[casilla].getIcon() != null) //Si la casilla no esta vacia
				{
					if(portaviones[0] != casilla && portaviones[1]!=casilla && portaviones[2]!=casilla && portaviones[3]!=casilla && portaviones[4]!=casilla)	//Comprobamos si coincide con alguna casilla ya asignada al portaviones
						for(int i=0; i<5;i++)		//Se limpia todos los iconos cambiados asignados al portaviones
							if(portaviones[i]!=200)
							{
								mi_mapa[portaviones[i]].setIcon(null);
								portaviones[i]=200;
							}
				}
				
				else if(portaviones[0] != 200)				//Si la primera casilla está asignada
				{
					if(portaviones[1] != 200)			//Si la segunda casilla también está asignada
					{
						if(portaviones[2] != 200)			//Si la tercera casilla también está asignada
						{
							if(portaviones[3] != 200)			//Si la cuarta casilla también está asignada
							{
								if(portaviones[4] != 200)			//Si la quinta casilla también está asignada
								{
									nueva_posicion=true;	//Se marca el flag para cambiar todas las casillas
								}
						
								else						//Si la quinta casilla no se ha asignado
								{
									if((portaviones[3]+10==casilla) || (portaviones[3]-10==casilla) || ((((portaviones[3]+1)%10)!=0) && (portaviones[3]+1==casilla)) || ((((portaviones[3])%10)!=0) && (portaviones[3]-1==casilla)))	//Se mira si es una casilla válida para colocar
									{
										portaviones[4]=casilla;	//Se asigna la casilla al segundo pin del barco	
										boton_pulsado.setIcon(ic_extremo);	//Se cambia el icono
										barcos_colocados[barco_seleccionado-1]=true; //Marcamos el portaviones como colocado
									}
						
									else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
										nueva_posicion=true;
							
								}
							}
						
							else						//Si la cuarta casilla no se ha asignado
							{
								if((portaviones[2]+10==casilla) || (portaviones[2]-10==casilla) || ((((portaviones[2]+1)%10)!=0) && (portaviones[2]+1==casilla)) || ((((portaviones[2])%10)!=0) && (portaviones[2]-1==casilla)))	//Se mira si es una casilla válida para colocar
								{
									portaviones[3]=casilla;	//Se asigna la casilla al segundo pin del barco	
									boton_pulsado.setIcon(ic_medio);	//Se cambia el icono
								}
						
								else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
									nueva_posicion=true;
							
							}
						}
						
						else						//Si la tercera casilla no se ha asignado
						{
							if((portaviones[1]+10==casilla) || (portaviones[1]-10==casilla) || ((((portaviones[1]+1)%10)!=0) && (portaviones[1]+1==casilla)) || ((((portaviones[1])%10)!=0) && (portaviones[1]-1==casilla)))	//Se mira si es una casilla válida para colocar
							{
								portaviones[2]=casilla;	//Se asigna la casilla al segundo pin del barco	
								boton_pulsado.setIcon(ic_medio);	//Se cambia el icono
							}
						
							else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
								nueva_posicion=true;
							
						}
					}
					
					else						//Si la segunda casilla no se ha asignado
					{
						if((portaviones[0]+10==casilla) || (portaviones[0]-10==casilla) || ((((portaviones[0]+1)%10)!=0) && (portaviones[0]+1==casilla)) || ((((portaviones[0])%10)!=0) && (portaviones[0]-1==casilla)))	//Se mira si es una casilla válida para colocar
						{
							portaviones[1]=casilla;	//Se asigna la casilla al segundo pin del barco	
							boton_pulsado.setIcon(ic_medio);	//Se cambia el icono
						}
						
						else					//Si no es una casilla válida, se marca para cambiar todos los pines del barco
							nueva_posicion=true;
							
					}
				}
				
				else							//Si la primera casilla no se ha asignado, se marca para nueva posicion
					nueva_posicion = true;
				
				if(nueva_posicion)				//Si tras las comprobaciones, la posicion del barco es nueva
				{
					for(int i=0; i<5;i++)		//Se limpia todos los iconos cambiados asignados al buque
						if(portaviones[i]!=200)
							mi_mapa[portaviones[i]].setIcon(null);
					
					portaviones[0]=casilla;			//Se asigna el primer pin a la casilla pulsada
					mi_mapa[casilla].setIcon(ic_extremo);	//Se cambia el icono
					portaviones[1]=200;				//Se limpia el resto de pines
					portaviones[2]=200;
					portaviones[3]=200;
					portaviones[4]=200;
					barcos_colocados[barco_seleccionado-1]=false; //Desmarcamos el portaviones como colocado
				}
					
				
				//System.out.println("Portaviones en {" + portaviones[0] +"," + portaviones[1] +"," + portaviones[2] +"," + portaviones[3] +"," + portaviones[4] + "}");
			}
			
		}
    }
	
	//Método que gestiona la salida del jugador de la partida, para evitar errores
	private void CerrarPartida(java.awt.event.ActionEvent evt) {

		if(ventana_espera != null) ventana_espera.dispose();
		try{
			partida.salida(nombre_user);
		}
		catch (Exception re){}
		
		ventana.dispose();
	}
	
	//Devuelve un String con el nombre de usuario asignado a este jugador
	public String getNombre() throws RemoteException{
		return nombre_user;
	}
	
	//Método que gestiona la pantalla de Instrucciones si se pulsa el botón correspondiente
	private void Instrucciones(java.awt.event.ActionEvent evt) {
			JOptionPane.showMessageDialog(ventana,"\tPrimero coloca tus barcos en el mapa de la derecha.\n\t\tTras ello, pulsa \"Comenzar partida\" e intenta encontrar los barcos de la CPU clicando en el mapa de la izquierda.\n\t\tIntenta acabar antes que tu contrincante. ¡¡Suerte!!");
	}
	
	//Método que gestiona el pulsado de "Comenzar Partida"
	/*	- Si el jugador no ha colocado sus barcos, se le advierte de que tiene que hacerlo
		- Una vez pulsado el botón y colocados los barcos, se desactiva la opción de colocar los barcos de nuevo, ni modificarlos
		- Se abre una ventana que indica que se ha de esperar a que el oponente coloque todos sus barcos
	*/
	private void Fija_mapa (java.awt.event.ActionEvent evt) {
		//Si todos los barcos están colocados
		if((barcos_colocados[0]==true) && (barcos_colocados[1]==true) && (barcos_colocados[2]==true) && (barcos_colocados[3]==true)) 	
		{
			for(int i=0;i<=N_botones-1;i++)	//Desactivamos el mapa de la derecha
			{
				mi_mapa[i].removeActionListener(pmc);
			}
				
				
			try{
				//Indicamos al objeto partida que hemos acabado de colocar los barcos, al igual que la posición de los mismos, para el log de la partida
				partida.listo(nombre_user, salvavidas, buque, acorazado, portaviones);
					
				//Si el oponente aún no ha acabado, mostramos una ventana de espera
				if(!contricante_listo){
						
					ventana_espera = new Frame("Esperando a contrincante");
					ventana_espera.setSize(200,200);
					ventana_espera.setLocationRelativeTo(null);
					JLabel mensajito = new JLabel("Esperando a que tu contrincante coloque sus barcos");
					mensajito.setBounds(100,100,10,10);
					ventana_espera.add(mensajito);
					ventana_espera.setVisible(true);
						
					//System.out.println("Contrincante espera");
						
					espera = true;
				}
					
				else{
					//Si el oponente está listo, llamamos al método que activa el mapa de la izquierda
					Mapa_enemigo();
					//System.out.println("Contrincante listo");				
				}
				
			}
			catch(Exception io){
				JOptionPane.showMessageDialog(ventana,"Error con la conexión, vuelva a iniciar partida");
				ventana.dispose();
			};
			
			//Ocultamos el botón de "Comenzar partida"
			comenzar.setVisible(false);
		}	

		// Si todos los barcos no están colocados, indicamos al jugador de ello
		else
			JOptionPane.showMessageDialog(ventana,"¡TIENES QUE COLOCAR TODOS LOS BARCOS PARA COMENZAR!");
				
		
		
	}
	
	//Método que gestiona la seleccion del barco a colocar, de la lista que aparece a la derecha de la pantalla
	//	- Obtenemos el tipo seleccionado, y lo indicamos en una variable global
	class Coloca_barcos implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == c1)
				barco_seleccionado = 1;
			
			else if(e.getSource() == c2)
				barco_seleccionado = 2;
			
			else if(e.getSource() == c3)
				barco_seleccionado = 3;
			
			else
				barco_seleccionado = 4;
		}
	}

}
		