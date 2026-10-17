public class Ingredient {
    private int id;
    private String name;
    private String unit;
    private double quantityInStock;
    private double unitPrice;

    public Ingredient(int id, String name, String unit, double quantityInStock, double unitPrice) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.quantityInStock = quantityInStock;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(double quantityInStock) { this.quantityInStock = quantityInStock; }

    /** Removes a quantity from stock (cannot go below zero). */
    public void takeQuantityInStock(int take) {
        if (take < 0) throw new IllegalArgumentException("take must be >= 0");
        if (take > quantityInStock) throw new IllegalStateException("Not enough stock for " + name);
        quantityInStock -= take;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}