import java.awt.GraphicsEnvironment;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainDB {
    public static void main(String[] args) {
        boolean demo=false;
        for(String a:args)if(a.equals("--demo"))demo=true;
        if(GraphicsEnvironment.isHeadless()) {
            System.err.println("Swing needs a graphical desktop. For automated checks, run ProjectTests.");
            return;
        }
        final boolean demoRequested=demo;
        SwingUtilities.invokeLater(()->{
            RestaurantStore store;
            String mode;
            if(demoRequested) {store=new DemoRestaurantStore();mode="DEMO (in-memory)";}
            else {
                try(Connection ignored=DBConnection.getConnection()) {
                    store=new SqlRestaurantStore();mode="MySQL";
                } catch(Exception e) {
                    int option=JOptionPane.showConfirmDialog(null,"Could not connect to MySQL. Check schema.sql, the JDBC driver, and db.properties.\nStart in demo mode instead?","Database unavailable",JOptionPane.YES_NO_OPTION);
                    if(option!=JOptionPane.YES_OPTION)return;
                    store=new DemoRestaurantStore();mode="DEMO (in-memory)";
                }
            }
            new RestaurantWindow(new RestaurantService(store),mode).setVisible(true);
        });
    }
}
