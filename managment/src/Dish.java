import java.util.ArrayList;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private String category;
    private double price;
    private final List<Ingredient> ingredients = new ArrayList<>();

    public Dish(int id, String name, String category, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public void addIngredient(Ingredient ingredient) { ingredients.add(ingredient); }
    public List<Ingredient> getIngredients() { return ingredients; }

    public boolean isAvailable() {
        if (ingredients.isEmpty()) return false;
        for (Ingredient i : ingredients) {
            if (i.getQuantityInStock() <= 0) return false;
        }
        return true;
    }
}