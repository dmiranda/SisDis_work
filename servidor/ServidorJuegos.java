import java.rmi.*;
import java.rmi.server.*;


class ServidorJuegos  {
    static public void main (String args[]) {
       if (args.length!=1) {
            System.err.println("Uso: ServidorJuegos numPuertoRegistro");
            return;
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        try {
            ServicioJuegoImpl srv = new ServicioJuegoImpl();
            Naming.rebind("rmi://localhost:" + args[0] + "/Juegos", srv);
        }
        catch (RemoteException e) {
            System.err.println("Error de comunicacion: " + e.toString());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println("Excepcion en ServidorJuegos:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
