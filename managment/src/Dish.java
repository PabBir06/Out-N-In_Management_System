import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dish {
    private int id;
    private String name;
    private String category;
    private double price;
    /** Ingredient ID -> ingredient and amount per serving. */
    private final Map<Integer, Ingredient> ingredients = new LinkedHashMap<>();
    private final Map<Integer, Double> quantities = new LinkedHashMap<>();

    public Dish(int id, String name, String category, double price) {
        this.id = id;
        setName(name);
        setCategory(category);
        setPrice(price);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Checks.text(name, "Dish name"); }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = Checks.text(category, "Category"); }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = Checks.nonNegative(price, "Dish price"); }
    public void addIngredient(Ingredient ingredient) { addIngredient(ingredient, 1.0); }
    public void addIngredient(Ingredient ingredient, double amount) {
        if (ingredient == null) throw new IllegalArgumentException("Ingredient is required.");
        Checks.positive(amount, "Recipe quantity");
        ingredients.put(ingredient.getId(), ingredient);
        quantities.put(ingredient.getId(), amount);
    }
    public void clearIngredients() { ingredients.clear(); quantities.clear(); }
    public List<Ingredient> getIngredients() { return Collections.unmodifiableList(new ArrayList<>(ingredients.values())); }
    public Map<Integer, Double> getRecipeQuantities() { return Collections.unmodifiableMap(quantities); }
    public boolean isAvailable() {
        if (ingredients.isEmpty()) return false;
        for (Ingredient i : ingredients.values()) {
            if (i.getQuantityInStock() + 1e-9 < quantities.get(i.getId())) return false;
        }
        return true;
    }
    @Override public String toString() { return name + " ($" + String.format("%.2f", price) + ")"; }
}
