import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.*;
import java.rmi.server.*;

import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class palmares extends javax.swing.JFrame {
	
	ServicioJuego server;
	
	//Constructor
	//	- Recibe la referencia al servidor
    public palmares(ServicioJuego srv) throws RemoteException {
		
		//Almacena la referencia al servidor
        server = srv;
		
		//Dibuja la tabla y componentes de la ventana
		initComponents();
		
		//Obtiene una referencia para rellenar la tabla
		DefaultTableModel modTabla = (DefaultTableModel) tabla.getModel();
	
		
		try{
			//Obtiene el string con toda la información de la base de datos
			String [] lista_jugadores = srv.getLista();
			
			//Recorre el string, rellenando la tabla
			for(int i = 0; i<(lista_jugadores.length/3);i++){
				String [] row = {lista_jugadores[(i*3)], lista_jugadores[(i*3)+1], lista_jugadores[(i*3)+2]};
				modTabla.addRow(row);
			}
		}
		catch (Exception excep){
			System.out.println(excep.toString());
			throw new RemoteException("No se ha podido obtener la lista");
		}
		
    }
                         
	//Método que construye la interfaz gráfica
	//		(Construido con NetBeans)
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        salir = new javax.swing.JButton();
		actualizar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Jugador", "Partidas Jugadas", "Partidas Ganadas"
            }
        ));
        jScrollPane1.setViewportView(tabla);

        jLabel1.setText("Tabla de jugadores");

        salir.setText("Salir");
        salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salirActionPerformed(evt);
            }
        });
		
		actualizar.setText("Actualizar");
        actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(130, 130, 130)
                                .addComponent(jLabel1)))
                        .addGap(0, 1, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(actualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(salir)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 22, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(salir)
                    .addComponent(actualizar))
                .addContainerGap())
        );
        pack();
    }                       

	//Método que gestiona la pulsación del botón "Salir"
	//	Cierra la ventana creada
    private void salirActionPerformed(java.awt.event.ActionEvent evt) {                                         
        this.dispose();
    }  

	//Método que gestiona la pulsación del botón "Actualizar"
	//	Limpia la tabla, y vuelve a cargar la información
    private void actualizarActionPerformed(java.awt.event.ActionEvent evt) {                                         
		
		//Obtiene una referencia para rellenar la tabla
		DefaultTableModel modTabla = (DefaultTableModel) tabla.getModel();
		
		while(modTabla.getRowCount() != 0)
			modTabla.removeRow(0);
	
		
		try{
			//Obtiene el string con toda la información de la base de datos
			String [] lista_jugadores = server.getLista();
			
			//Recorre el string, rellenando la tabla
			for(int i = 0; i<(lista_jugadores.length/3);i++){
				String [] row = {lista_jugadores[(i*3)], lista_jugadores[(i*3)+1], lista_jugadores[(i*3)+2]};
				modTabla.addRow(row);
			}
		}
		catch (Exception excep){
			System.out.println(excep.toString());
		}
    }  	

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(palmares.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(palmares.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(palmares.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(palmares.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton salir;
	private javax.swing.JButton actualizar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabla;
    // End of variables declaration                   
}
