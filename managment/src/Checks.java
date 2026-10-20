import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Shared validation for forms and domain objects. */
public final class Checks {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private Checks() {}
    public static String text(String s, String label) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(label + " cannot be empty.");
        return s.trim();
    }
    public static double nonNegative(double n, String label) {
        if (!Double.isFinite(n) || n < 0) throw new IllegalArgumentException(label + " must be a non-negative number.");
        return n;
    }
    public static double positive(double n, String label) {
        if (!Double.isFinite(n) || n <= 0) throw new IllegalArgumentException(label + " must be greater than zero.");
        return n;
    }
    public static LocalDate date(String s, String label) {
        try { return LocalDate.parse(text(s, label)); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException(label + " must use yyyy-MM-dd."); }
    }
    public static String email(String s) {
        s = text(s, "Email");
        if (!EMAIL.matcher(s).matches()) throw new IllegalArgumentException("Invalid email address.");
        return s;
    }
    public static double number(String s, String label) {
        try { return Double.parseDouble(text(s, label).replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(label + " must be a number."); }
    }
}
