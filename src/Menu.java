import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A menu consists of selected dishes; its price is the sum of dish prices. */
public class Menu {
    private int id;
    private String name;
    private final List<Dish> dishes = new ArrayList<>();
    public Menu(int id, String name) { this.id = id; setName(name); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Checks.text(name, "Menu name"); }
    public void addDish(Dish dish) {
        if (dish == null) throw new IllegalArgumentException("Dish is required.");
        if (dishes.stream().noneMatch(d -> d.getId() == dish.getId())) dishes.add(dish);
    }
    public List<Dish> getDishes() { return Collections.unmodifiableList(dishes); }
    public double getPrice() { return dishes.stream().mapToDouble(Dish::getPrice).sum(); }
    public boolean isAvailable() { return !dishes.isEmpty() && dishes.stream().allMatch(Dish::isAvailable); }
    @Override public String toString() { return name + " ($" + String.format("%.2f", getPrice()) + ")"; }
}
