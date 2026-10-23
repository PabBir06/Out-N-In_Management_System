import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Seeded data store for UI demonstrations and tests without a database. */
public class DemoRestaurantStore implements RestaurantStore {
    private final Map<Integer,Ingredient> ingredients=new LinkedHashMap<>();
    private final Map<Integer,Dish> dishes=new LinkedHashMap<>();
    private final Map<Integer,Menu> menus=new LinkedHashMap<>();
    private final Map<Integer,Employee> employees=new LinkedHashMap<>();
    private final Map<Integer,Customer> customers=new LinkedHashMap<>();
    private int nextId=1;
    public DemoRestaurantStore() {
        Ingredient bun=new Ingredient(0,"Bun","piece",20,0.8);
        Ingredient patty=new Ingredient(0,"Patty","piece",12,2.5);
        Ingredient cheese=new Ingredient(0,"Cheese","slice",3,0.5);
        Ingredient lettuce=new Ingredient(0,"Lettuce","g",500,0.01);
        saveIngredient(bun);saveIngredient(patty);saveIngredient(cheese);saveIngredient(lettuce);
        Dish burger=new Dish(0,"Classic Burger","Burger",8.90);
        burger.addIngredient(bun,1);burger.addIngredient(patty,1);burger.addIngredient(lettuce,40);saveDish(burger);
        Dish cheeseburger=new Dish(0,"Cheeseburger","Burger",10.50);
        cheeseburger.addIngredient(bun,1);cheeseburger.addIngredient(patty,1);cheeseburger.addIngredient(cheese,1);saveDish(cheeseburger);
        Menu combo=new Menu(0,"Burger Combo");combo.addDish(burger);combo.addDish(cheeseburger);saveMenu(combo);
        saveEmployee(new Employee(0,"Alex","Martin","555-1001","alex@example.com","Cook",2200,"2025-03-01"));
        Customer c=new Customer(0,"Taylor","Smith","555-1002","taylor@example.com",LocalDate.now().minusDays(10).toString());
        saveCustomer(c);
        subscribe(c,new Subscription(0,LocalDate.now().minusDays(2).toString(),LocalDate.now().plusDays(28).toString()));
    }
    @Override public List<Ingredient> ingredients() {return new ArrayList<>(ingredients.values());}
    @Override public List<Dish> dishes() {return new ArrayList<>(dishes.values());}
    @Override public List<Menu> menus() {return new ArrayList<>(menus.values());}
    @Override public List<Employee> employees() {return new ArrayList<>(employees.values());}
    @Override public List<Customer> customers() {return new ArrayList<>(customers.values());}
    @Override public void saveIngredient(Ingredient x) {
        for(Ingredient i:ingredients.values()) if(i.getName().equalsIgnoreCase(x.getName()) && i.getId()!=x.getId()) throw new IllegalArgumentException("Ingredient name already exists.");
        if(x.getId()==0) x.setId(nextId++);
        ingredients.put(x.getId(),x);
        // Replace stale references in recipes after editing an ingredient.
        for(Dish d:dishes.values()) if(d.getRecipeQuantities().containsKey(x.getId())) d.addIngredient(x,d.getRecipeQuantities().get(x.getId()));
    }
    @Override public void saveDish(Dish d) {
        if(d.getIngredients().isEmpty()) throw new IllegalArgumentException("Dish recipe cannot be empty.");
        for(Dish existing:dishes.values()) if(existing.getName().equalsIgnoreCase(d.getName()) && existing.getId()!=d.getId()) throw new IllegalArgumentException("Dish name already exists.");
        for(Ingredient item:d.getIngredients()) if(!ingredients.containsKey(item.getId())) throw new IllegalArgumentException("Ingredient missing: " + item.getName());
        if(d.getId()==0)d.setId(nextId++);
        dishes.put(d.getId(),d);
        // Menus reflect updates to the price and recipe of the edited dish.
        for(Menu m:new ArrayList<>(menus.values())) {
            if(m.getDishes().stream().anyMatch(old->old.getId()==d.getId())) {
                Menu replacement=new Menu(m.getId(),m.getName());
                for(Dish old:m.getDishes()) replacement.addDish(old.getId()==d.getId()?d:old);
                menus.put(m.getId(),replacement);
            }
        }
    }
    @Override public void saveMenu(Menu m) {
        if(m.getDishes().isEmpty()) throw new IllegalArgumentException("Menu must have dishes.");
        if(!m.isAvailable()) throw new IllegalStateException("A menu cannot include an unavailable dish.");
        for(Menu old:menus.values()) if(old.getName().equalsIgnoreCase(m.getName()) && old.getId()!=m.getId()) throw new IllegalArgumentException("Menu name already exists.");
        for(Dish d:m.getDishes()) if(!dishes.containsKey(d.getId())) throw new IllegalArgumentException("Dish is missing.");
        if(m.getId()==0)m.setId(nextId++);menus.put(m.getId(),m);
    }
    @Override public void saveEmployee(Employee e) {
        for(Employee old:employees.values()) if(old.getEmail().equalsIgnoreCase(e.getEmail())&&old.getId()!=e.getId())throw new IllegalArgumentException("Employee email already exists.");
        if(e.getId()==0)e.setId(nextId++);employees.put(e.getId(),e);
    }
    @Override public void saveCustomer(Customer c) {
        for(Customer old:customers.values()) if(old.getEmail().equalsIgnoreCase(c.getEmail())&&old.getId()!=c.getId())throw new IllegalArgumentException("Customer email already exists.");
        if(c.getId()==0)c.setId(nextId++);
        if(customers.containsKey(c.getId()) && c.getSubscriptions().isEmpty()) c.getSubscriptions().addAll(customers.get(c.getId()).getSubscriptions());
        customers.put(c.getId(),c);
    }
    @Override public void delete(String entity,List<Integer> ids) {
        for(int id:ids) {
            switch(entity) {
                case "ingredient":
                    if(dishes.values().stream().anyMatch(d->d.getRecipeQuantities().containsKey(id)))throw new IllegalStateException("Ingredient is used by a dish; unlink it first.");
                    break;
                case "dish":
                    if(menus.values().stream().anyMatch(m->m.getDishes().stream().anyMatch(d->d.getId()==id)))throw new IllegalStateException("Dish is used by a menu; unlink it first.");
                    break;
                case "menu","employee","customer": break;
                default:throw new IllegalArgumentException("Invalid type.");
            }
        }
        for(int id:ids) switch(entity) {
            case "ingredient" -> ingredients.remove(id);
            case "dish" -> dishes.remove(id);
            case "menu" -> menus.remove(id);
            case "employee" -> employees.remove(id);
            case "customer" -> customers.remove(id);
            default -> throw new IllegalArgumentException("Invalid type.");
        }
    }
    @Override public void subscribe(Customer c,Subscription s) {
        Customer saved=customers.get(c.getId());
        if(saved==null) throw new IllegalArgumentException("Customer does not exist.");
        LocalDate start=LocalDate.parse(s.getStartDate()),end=LocalDate.parse(s.getEndDate());
        for(Subscription existing:saved.getSubscriptions()) {
            LocalDate s2=LocalDate.parse(existing.getStartDate()),e2=LocalDate.parse(existing.getEndDate());
            if(!start.isAfter(e2)&&!end.isBefore(s2)) throw new IllegalArgumentException("Subscription dates overlap.");
        }
        s.setId(nextId++);saved.subscribe(s);
        if(saved!=c)c.subscribe(s);
    }
    @Override public void prepareDish(int dishId) {
        Dish dish=dishes.get(dishId);
        if(dish==null)throw new IllegalArgumentException("Dish not found.");
        // First check *every* ingredient; no stock changes until all pass.
        if(!dish.isAvailable())throw new IllegalStateException("Not enough stock to prepare " + dish.getName());
        for(Ingredient i:dish.getIngredients()) ingredients.get(i.getId()).takeQuantityInStock(dish.getRecipeQuantities().get(i.getId()));
    }
}
