import java.awt.Component;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;

public final class ErrorHandler {
    private static final Logger LOG=Logger.getLogger("OutNIn");
    static {
        try {
            FileHandler file=new FileHandler("out-n-in-errors.log",true);
            file.setFormatter(new SimpleFormatter()); LOG.addHandler(file);
        } catch(Exception e) {System.err.println("Could not initialize error log: " + e.getMessage());}
    }
    private ErrorHandler() {}
    public static void show(Component owner,Exception exception) {
        LOG.warning(exception.toString());
        String message;
        if(exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
            message=exception.getMessage();
        } else if(exception instanceof SQLException) {
            SQLException sql=(SQLException) exception;
            if(sql.getSQLState()!=null && sql.getSQLState().startsWith("23"))
                message="This change conflicts with existing data (duplicate or referenced record). Check the values or remove dependencies first.";
            else message="Database operation failed. Check your connection, SQL schema and database permissions.";
            LOG.warning("SQL error: " + sql.getMessage());
        } else message="Unexpected error. Check out-n-in-errors.log for details.";
        JOptionPane.showMessageDialog(owner,message,"Out-n-In — Action failed",JOptionPane.ERROR_MESSAGE);
    }
}
