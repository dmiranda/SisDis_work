import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.*;
import java.rmi.server.*;


public class Pantalla_inicial extends javax.swing.JFrame {

	ServicioJuego srv;
	String servidor, puerto;
	hundir_flota user;
	String nombre;
	
    public Pantalla_inicial(String server, String port) {
        
		try{
			servidor = server;
			puerto = port;
			
			srv = (ServicioJuego) Naming.lookup("//" + servidor + ":" + puerto + "/Juegos");
			boolean ok = srv.hello();		//Este método sirve para salvaguardar el hecho de que el servidor no se encuentre disponible, saliendo de la aplicación sin seguir ejecutándose
			boolean loging = false;
			
			while(!loging){
				
				int reply = JOptionPane.showConfirmDialog(null, "¿Posee usted un nick registrado?", "REGISTRO", JOptionPane.YES_NO_CANCEL_OPTION);
				
				if( reply == JOptionPane.YES_OPTION) {
					
					nombre = JOptionPane.showInputDialog(null,"Introduzca su usuario");
					
					if(nombre != null)
					{
					
						JPasswordField pf = new JPasswordField();
						int rep = JOptionPane.showConfirmDialog(null, pf, "Introduzca Contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if(rep == JOptionPane.OK_OPTION)
						{
							if(srv.nick_registrado(nombre,new String (pf.getPassword()))) 
								loging = true;
							else
								JOptionPane.showMessageDialog(null, "Usuario y/o contraseña no validos");
						}
					}
				}
				else if(reply == JOptionPane.NO_OPTION){
				
					nombre = JOptionPane.showInputDialog(null,"Introduzca un nombre de usuario");
					
					if(nombre != null)
					{
						if(srv.comprueba_nick(nombre))
						{
							JPasswordField pf = new JPasswordField();
							int rep = JOptionPane.showConfirmDialog(null, pf, "Introduzca su Contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
							if(rep == JOptionPane.OK_OPTION)
							{
								String pass = new String (pf.getPassword());
								JPasswordField pf_rep = new JPasswordField();
								int rep1 = JOptionPane.showConfirmDialog(null, pf_rep, "Introduzca de nuevo su contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
								if(rep1 == JOptionPane.OK_OPTION)
								{
									if(pass.equals(new String (pf_rep.getPassword())))
									{
										if(srv.nuevo_nick(nombre,pass))
											loging = true;
										else
											JOptionPane.showMessageDialog(null, "Nombre de usuario no disponible");
									}
									
									else
										JOptionPane.showMessageDialog(null, "Las contraseñas no coinciden");
								}
							}
						}
						
						else
							JOptionPane.showMessageDialog(null, "El nombre que ha introducido ya está en uso, por favor, introduzca otro o inicie sesión");
					}
				}
				else
					System.exit(0);
			}
			initComponents(nombre);		

		}
		catch(Exception ret){
				System.out.println(ret.toString());
				JOptionPane.showMessageDialog(this,"No se puede conectar con el servidor");
				System.exit(0);
		}		
    }
	
	
    private void initComponents(String nombre) {


        nombre_user = new javax.swing.JLabel();
        Inicia_partida = new javax.swing.JToggleButton();
        Salir = new javax.swing.JToggleButton();
        palmares = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        nombre_user.setText("Bienvenido:" + nombre);

        Inicia_partida.setText("Iniciar partida");
		Inicia_partida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iniciar_partida(evt);
            }
        });

        Salir.setText("Salir");
		Salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salir(evt);
            }
        });

        palmares.setText("Palmarés");
		palmares.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mostrar_tabla(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Salir)
                .addGap(27, 27, 27))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nombre_user)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 131, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(Inicia_partida)
                                .addGap(124, 124, 124))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(palmares)
                                .addContainerGap())))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nombre_user)
                .addGap(9, 9, 9)
                .addComponent(palmares)
                .addGap(60, 60, 60)
                .addComponent(Inicia_partida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addComponent(Salir)
                .addGap(25, 25, 25))
        );

        pack();
    }                       

	private void iniciar_partida(java.awt.event.ActionEvent evt) {	
		try{
				user = new hundir_flota(nombre);
				srv.alta(user);
			}
			
			catch(Exception re)
			{	
				System.out.println(re.toString());
				JOptionPane.showMessageDialog(this,"No se puede conectar con el servidor");
			}
    } 
	
	private void salir(java.awt.event.ActionEvent evt) {
		try{
			if (user != null) srv.baja(user);
		}
				
		catch (Exception ra){
				System.out.println(ra.toString());
				
		}
		finally{
			System.exit(0);
		}
	}
	
	private void mostrar_tabla (java.awt.event.ActionEvent evt) {
		try{
			new palmares(srv).setVisible(true);
		}
				
		catch (Exception ra){
				System.out.println(ra.toString());
				JOptionPane.showConfirmDialog(null, "No se puede mostrar la tabla en este momento");
		}
	}

	
    public static void main(String args[]) {
		
		if (args.length!=2) {
            System.err.println("Uso: juegos hostregistro numPuertoRegistro");
            return;
        }
 
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Pantalla_inicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Pantalla_inicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Pantalla_inicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Pantalla_inicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
				
				if (System.getSecurityManager() == null)
					System.setSecurityManager(new SecurityManager());
		
				try{
					new Pantalla_inicial(args[0],args[1]).setVisible(true);
				}
				
				catch (Exception e) {
					System.err.println("Excepcion en JUEGOS:");
					e.printStackTrace();
				}
			}
        });
    }

    // Declaración de Variables                    
    private javax.swing.JLabel nombre_user;
    private javax.swing.JToggleButton Inicia_partida;
    private javax.swing.JToggleButton Salir;
    private javax.swing.JToggleButton palmares;                 
}
