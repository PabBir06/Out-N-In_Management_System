public class Ingredient {
    private int id;
    private String name;
    private String unit;
    private double quantityInStock;
    private double unitPrice;

    public Ingredient(int id, String name, String unit, double quantityInStock, double unitPrice) {
        this.id = id;
        setName(name);
        setUnit(unit);
        setQuantityInStock(quantityInStock);
        setUnitPrice(unitPrice);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Checks.text(name, "Ingredient name"); }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = Checks.text(unit, "Unit"); }
    public double getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(double quantityInStock) { this.quantityInStock = Checks.nonNegative(quantityInStock, "Stock"); }
    public void takeQuantityInStock(int take) { takeQuantityInStock((double) take); }
    public void takeQuantityInStock(double take) {
        Checks.positive(take, "Consumed quantity");
        if (take > quantityInStock + 1e-9) throw new IllegalStateException("Not enough stock for " + name);
        quantityInStock = Math.max(0, quantityInStock - take);
    }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = Checks.nonNegative(unitPrice, "Unit price"); }
    public boolean isLowStock(double threshold) { return quantityInStock <= threshold; }
    @Override public String toString() { return name + " (" + id + ")"; }
}
