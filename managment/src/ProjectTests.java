import java.time.LocalDate;
import java.util.List;

/** No JUnit or DB required: java -ea -cp build ProjectTests */
public class ProjectTests {
    private static int tests=0;
    private static void check(boolean ok,String message) {
        tests++;
        if(!ok)throw new AssertionError(message);
    }
    private static void expectFailure(Runnable task,String label) {
        boolean failed=false;
        try{task.run();}catch(IllegalArgumentException|IllegalStateException ex){failed=true;}
        check(failed,"Expected rejection: " + label);
    }
    private static void equal(double a,double b,String message) {check(Math.abs(a-b)<1e-8,message+" ("+a+" vs "+b+")");}
    public static void main(String[] args)throws Exception {
        DemoRestaurantStore db=new DemoRestaurantStore();
        RestaurantService svc=new RestaurantService(db);
        check(db.ingredients().size()==4,"Seeded ingredients");
        check(db.dishes().size()==2,"Seeded dishes");
        check(db.menus().size()==1,"Seeded menu");
        check(db.employees().size()==1,"Seeded employee");
        check(db.customers().size()==1,"Seeded customer");
        Ingredient cheese=db.ingredients().stream().filter(i->i.getName().equals("Cheese")).findFirst().orElseThrow();
        Dish cheeseburger=db.dishes().stream().filter(d->d.getName().equals("Cheeseburger")).findFirst().orElseThrow();
        check(cheeseburger.isAvailable(),"Recipe initially available");
        equal(cheeseburger.getRecipeQuantities().get(cheese.getId()),1.0,"Recipe amount recorded");
        Menu menu=db.menus().get(0);
        equal(menu.getPrice(),19.40,"Menu price sums dishes");
        Customer customer=db.customers().get(0);
        check(customer.hasActiveSubscription(),"Active subscription detected");
        equal(svc.priceForCustomer(menu,customer),17.46,"10% subscriber discount");
        equal(svc.priceForCustomer(menu,null),19.40,"No subscriber discount");
        Subscription renewed=svc.renew(customer,1);
        check(LocalDate.parse(renewed.getStartDate()).equals(LocalDate.parse(customer.getSubscriptions().get(0).getEndDate()).plusDays(1)),"Renewal begins the day after current expiry");
        check(customer.getSubscriptions().size()==2,"Renewal persisted");
        expectFailure(()->db.subscribe(customer,new Subscription(0,LocalDate.now().toString(),LocalDate.now().plusDays(10).toString())),"overlap rejection");
        expectFailure(()->new Employee(0,"A","B","123","a@x.com","Cook",-1,"2025-01-01"),"negative salary");
        expectFailure(()->new Subscription(0,"2027-03-01","2027-02-01"),"backwards dates");
        expectFailure(()->new Ingredient(0,"Bad","piece",-2,1),"negative stock");
        expectFailure(()->new Customer(0,"Bad","Data","1","broken","2025-01-01"),"bad email");
        expectFailure(()->db.saveCustomer(new Customer(0,"Taylor","Dup","123","taylor@example.com","2025-01-01")),"duplicate customer");
        check(Checks.number("1,25","Amount")==1.25,"Comma decimal accepted");
        Ingredient sample=new Ingredient(0,"Test-gram","g",0.5,0.01);db.saveIngredient(sample);
        Dish small=new Dish(0,"Small sample","Side",1.50);small.addIngredient(sample,0.25);db.saveDish(small);
        db.prepareDish(small.getId());equal(sample.getQuantityInStock(),0.25,"Fractional stock deduction");
        db.prepareDish(small.getId());equal(sample.getQuantityInStock(),0.0,"Second stock deduction");
        expectFailure(()->db.prepareDish(small.getId()),"insufficient stock");
        equal(sample.getQuantityInStock(),0.0,"Stock unchanged on failure");
        expectFailure(()->db.delete("ingredient",List.of(cheese.getId(),sample.getId())),"blocked batch delete is atomic");
        check(db.ingredients().stream().anyMatch(x->x.getId()==sample.getId()),"No partial delete");
        db.prepareDish(cheeseburger.getId());db.prepareDish(cheeseburger.getId());db.prepareDish(cheeseburger.getId());
        check(!cheeseburger.isAvailable(),"Out of stock makes dish unavailable");
        check(!menu.isAvailable(),"Unavailable dish makes menu unavailable");
        expectFailure(()->svc.priceForCustomer(menu,customer),"menu cannot be sold while unavailable");
        db.delete("menu",List.of(menu.getId()));
        db.delete("dish",List.of(cheeseburger.getId(),db.dishes().stream().filter(d->d.getName().equals("Classic Burger")).findFirst().orElseThrow().getId()));
        check(db.dishes().size()==1,"Batch dish deletion");
        Employee newEmployee=new Employee(0,"Jane","Example","555-3000","jane@example.com","Manager",3400,"2026-01-01");db.saveEmployee(newEmployee);
        check(db.employees().size()==2,"Employee added");
        Employee update=new Employee(newEmployee.getId(),"Jane","Example","555-3000","jane@example.com","Lead",3600,"2026-01-01");db.saveEmployee(update);
        check(db.employees().stream().anyMatch(x->x.getRole().equals("Lead")),"Employee updated");
        db.delete("employee",List.of(newEmployee.getId()));
        check(db.employees().size()==1,"Employee deleted");
        System.out.println("PASS: " + tests + " assertions. All domain and in-memory integration tests succeeded.");
    }
}
