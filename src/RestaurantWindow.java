import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Four main Swing tabs with generic searchable, sortable multi-select tables. */
public class RestaurantWindow extends JFrame {
    private final RestaurantService service;
    private final RestaurantStore store;
    private final List<Section> sections=new ArrayList<>();
    public RestaurantWindow(RestaurantService service,String mode) {
        super("Out-n-In Restaurant Management — " + mode);
        this.service=service; this.store=service.store();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180,720);setLocationRelativeTo(null);
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Stock",section("ingredient"));
        JSplitPane menuTab=new JSplitPane(JSplitPane.VERTICAL_SPLIT,section("dish"),section("menu"));
        menuTab.setResizeWeight(.55);
        tabs.addTab("Menus",menuTab);
        tabs.addTab("Employees",section("employee"));
        tabs.addTab("Customers",section("customer"));
        add(tabs,BorderLayout.CENTER);
        JLabel footer=new JLabel("  Search any column · Ctrl/Shift selects multiple rows · Low stock: ≤ " + RestaurantService.LOW_STOCK_THRESHOLD + " units");
        footer.setBorder(BorderFactory.createEmptyBorder(7,8,7,8));add(footer,BorderLayout.SOUTH);
        refreshAll();
    }
    private Section section(String entity) {Section s=new Section(entity);sections.add(s);return s;}
    private void refreshAll() {for(Section s:sections) s.reload();}
    private static String money(double v) {return String.format(Locale.US,"%.2f",v);}

    private class Section extends JPanel {
        private final String entity;
        private final DefaultTableModel model;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter;
        private final List<Object> objects=new ArrayList<>();
        Section(String entity) {
            super(new BorderLayout(6,6));this.entity=entity;
            setBorder(BorderFactory.createTitledBorder(entity.substring(0,1).toUpperCase()+entity.substring(1)+"s"));
            String[] cols=switch(entity) {
                case "ingredient"->new String[]{"ID","Name","Unit","In stock","Unit price","Status"};
                case "dish"->new String[]{"ID","Name","Category","Price","Recipe","Available"};
                case "menu"->new String[]{"ID","Name","Price","Dishes","Available"};
                case "employee"->new String[]{"ID","Name","Phone","Email","Role","Salary","Hired"};
                default->new String[]{"ID","Name","Phone","Email","Registered","Subscription"};
            };
            model=new DefaultTableModel(cols,0){
                @Override public boolean isCellEditable(int row,int col){return false;}
                @Override public Class<?> getColumnClass(int col) {
                    if(col==0)return Integer.class;
                    if((entity.equals("ingredient") && (col==3 || col==4)) ||
                       (entity.equals("dish") && col==3) ||
                       (entity.equals("menu") && col==2) ||
                       (entity.equals("employee") && col==5)) return Double.class;
                    return String.class;
                }
            };
            table=new JTable(model);
            table.setRowHeight(24);table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            sorter=new TableRowSorter<>(model);table.setRowSorter(sorter);
            if(entity.equals("ingredient")) {
                DefaultTableCellRenderer lowStockRenderer=new DefaultTableCellRenderer(){
                @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean selected,boolean focused,int row,int column) {
                    Component c=super.getTableCellRendererComponent(t,value,selected,focused,row,column);
                    int index=t.convertRowIndexToModel(row);
                    if(!selected && index<objects.size() && ((Ingredient)objects.get(index)).isLowStock(RestaurantService.LOW_STOCK_THRESHOLD))c.setBackground(new Color(255,225,220));
                    else c.setBackground(selected?t.getSelectionBackground():Color.WHITE);
                    return c;
                }
                };
                table.setDefaultRenderer(String.class,lowStockRenderer);
                table.setDefaultRenderer(Number.class,lowStockRenderer);
                table.setDefaultRenderer(Integer.class,lowStockRenderer);
                table.setDefaultRenderer(Double.class,lowStockRenderer);
            }
            add(new JScrollPane(table),BorderLayout.CENTER);
            JPanel tools=new JPanel(new FlowLayout(FlowLayout.LEFT,5,2));
            JTextField search=new JTextField(18);search.setToolTipText("Search all columns");
            tools.add(new JLabel("Search:"));tools.add(search);
            search.getDocument().addDocumentListener(new DocumentListener(){
                private void filter() {String value=search.getText().trim();sorter.setRowFilter(value.isEmpty()?null:RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(value)));}
                public void insertUpdate(DocumentEvent e){filter();}
                public void removeUpdate(DocumentEvent e){filter();}
                public void changedUpdate(DocumentEvent e){filter();}
            });
            button(tools,"Add",()->edit(null));button(tools,"Edit",()->{
                Object selected=one();if(selected!=null)edit(selected);
            });
            button(tools,"Delete selected",()->deleteSelected());button(tools,"Refresh",()->reload());
            if(entity.equals("dish")) button(tools,"Prepare dish",()->prepare());
            if(entity.equals("customer")) {
                button(tools,"Renew",()->renew());
                button(tools,"Quote menu",()->quote());
            }
            add(tools,BorderLayout.NORTH);
        }
        private Object one() {
            int[] chosen=table.getSelectedRows();
            if(chosen.length!=1) {JOptionPane.showMessageDialog(RestaurantWindow.this,"Please select exactly one row.");return null;}
            return objects.get(table.convertRowIndexToModel(chosen[0]));
        }
        private int id(Object o) {
            if(o instanceof Ingredient x)return x.getId();
            if(o instanceof Dish x)return x.getId();
            if(o instanceof Menu x)return x.getId();
            if(o instanceof Employee x)return x.getId();
            return ((Customer)o).getId();
        }
        void reload() {
            try {
                List<?> values=switch(entity){
                    case "ingredient"->store.ingredients();case "dish"->store.dishes();
                    case "menu"->store.menus();case "employee"->store.employees();
                    default->store.customers();
                };
                objects.clear();objects.addAll(values);model.setRowCount(0);
                for(Object o:objects)model.addRow(row(o));
            } catch(Exception ex){ErrorHandler.show(RestaurantWindow.this,ex);}
        }
        private Object[] row(Object o) {
            if(o instanceof Ingredient i)return new Object[]{i.getId(),i.getName(),i.getUnit(),i.getQuantityInStock(),i.getUnitPrice(),i.isLowStock(RestaurantService.LOW_STOCK_THRESHOLD)?"LOW":"OK"};
            if(o instanceof Dish d)return new Object[]{d.getId(),d.getName(),d.getCategory(),d.getPrice(),d.getIngredients().size()+" ingredients",d.isAvailable()?"YES":"NO"};
            if(o instanceof Menu m)return new Object[]{m.getId(),m.getName(),m.getPrice(),m.getDishes().size()+" dishes",m.isAvailable()?"YES":"NO"};
            if(o instanceof Employee e)return new Object[]{e.getId(),e.getFullName(),e.getPhone(),e.getEmail(),e.getRole(),e.getSalary(),e.getHireDate()};
            Customer c=(Customer)o;
            String status=c.hasActiveSubscription()?"ACTIVE":c.getSubscriptions().isEmpty()?"NONE":
                c.getSubscriptions().stream().anyMatch(s->LocalDate.parse(s.getStartDate()).isAfter(LocalDate.now()))?"FUTURE":"EXPIRED";
            return new Object[]{c.getId(),c.getFullName(),c.getPhone(),c.getEmail(),c.getRegistrationDate(),status};
        }
        void deleteSelected() {
            int[] rows=table.getSelectedRows();
            if(rows.length==0) {JOptionPane.showMessageDialog(RestaurantWindow.this,"Select one or more rows to delete.");return;}
            if(JOptionPane.showConfirmDialog(RestaurantWindow.this,"Delete " + rows.length + " selected " + entity + " record(s)?","Confirm deletion",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            List<Integer> ids=new ArrayList<>();for(int row:rows)ids.add(id(objects.get(table.convertRowIndexToModel(row))));
            execute(()->store.delete(entity,ids));
        }
        void edit(Object item) {
            try {
                switch(entity) {
                    case "ingredient"->ingredientEditor((Ingredient)item);
                    case "dish"->dishEditor((Dish)item);
                    case "menu"->menuEditor((Menu)item);
                    case "employee"->employeeEditor((Employee)item);
                    case "customer"->customerEditor((Customer)item);
                }
            } catch(Exception ex){ErrorHandler.show(RestaurantWindow.this,ex);}
        }
        private void prepare() {
            Object selected=one();if(selected==null)return;
            Dish dish=(Dish)selected;
            if(JOptionPane.showConfirmDialog(RestaurantWindow.this,"Prepare one " + dish.getName()+" and deduct recipe quantities from stock?","Prepare dish",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
                execute(()->store.prepareDish(dish.getId()));
        }
        private void renew() {
            Object selected=one();if(selected==null)return;
            String input=JOptionPane.showInputDialog(RestaurantWindow.this,"Renew for how many months (1–36)?","1");
            if(input==null)return;
            execute(()->service.renew((Customer)selected,Integer.parseInt(input.trim())));
        }
        private void quote() {
            Object selected=one();if(selected==null)return;
            try {
                List<Menu> available=store.menus();
                if(available.isEmpty()){JOptionPane.showMessageDialog(RestaurantWindow.this,"Create a menu first.");return;}
                Menu chosen=(Menu)JOptionPane.showInputDialog(RestaurantWindow.this,"Choose a menu:","Menu quote",JOptionPane.PLAIN_MESSAGE,null,available.toArray(),available.get(0));
                if(chosen!=null)JOptionPane.showMessageDialog(RestaurantWindow.this,"Price for " + ((Customer)selected).getFullName()+": $" +money(service.priceForCustomer(chosen,(Customer)selected)) +" (subscription discount " + ( ((Customer)selected).hasActiveSubscription()?"10%":"0%")+")");
            }catch(Exception ex){ErrorHandler.show(RestaurantWindow.this,ex);}
        }
    }

    @FunctionalInterface private interface Work {void run() throws Exception;}
    private void execute(Work work) {try{work.run();refreshAll();}catch(Exception e){ErrorHandler.show(this,e);}}
    private static void button(JPanel panel,String title,Runnable action) {
        JButton b=new JButton(title);b.addActionListener(e->action.run());panel.add(b);
    }
    private static JTextField field(String text){return new JTextField(text==null?"":text,18);}
    private static void add(JPanel p,String label,JComponent c){p.add(new JLabel(label));p.add(c);}
    private static JPanel form(){return new JPanel(new GridLayout(0,2,6,6));}
    private boolean confirm(JComponent content,String title) {
        return JOptionPane.showConfirmDialog(this,content,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION;
    }
    private void ingredientEditor(Ingredient original) {
        JPanel p=form();
        JTextField name=field(original==null?"":original.getName()),unit=field(original==null?"piece":original.getUnit()),
            stock=field(original==null?"0":money(original.getQuantityInStock())),price=field(original==null?"0":money(original.getUnitPrice()));
        add(p,"Name",name);add(p,"Unit",unit);add(p,"Stock quantity",stock);add(p,"Unit price",price);
        if(!confirm(p,"Ingredient"))return;
        execute(()->store.saveIngredient(new Ingredient(original==null?0:original.getId(),name.getText(),unit.getText(),Checks.number(stock.getText(),"Stock"),Checks.number(price.getText(),"Price"))));
    }
    private void employeeEditor(Employee original) {
        JPanel p=form();
        JTextField first=field(original==null?"":original.getName()),last=field(original==null?"":original.getLastName()),
            phone=field(original==null?"":original.getPhone()),email=field(original==null?"":original.getEmail()),
            role=field(original==null?"":original.getRole()),salary=field(original==null?"0":money(original.getSalary())),
            hired=field(original==null?LocalDate.now().toString():original.getHireDate());
        add(p,"First name",first);add(p,"Last name",last);add(p,"Phone",phone);add(p,"Email",email);add(p,"Role",role);add(p,"Salary",salary);add(p,"Hire date (yyyy-MM-dd)",hired);
        if(!confirm(p,"Employee"))return;
        execute(()->store.saveEmployee(new Employee(original==null?0:original.getId(),first.getText(),last.getText(),phone.getText(),email.getText(),role.getText(),Checks.number(salary.getText(),"Salary"),hired.getText())));
    }
    private void customerEditor(Customer original) {
        JPanel p=form();
        JTextField first=field(original==null?"":original.getName()),last=field(original==null?"":original.getLastName()),
            phone=field(original==null?"":original.getPhone()),email=field(original==null?"":original.getEmail()),
            reg=field(original==null?LocalDate.now().toString():original.getRegistrationDate());
        add(p,"First name",first);add(p,"Last name",last);add(p,"Phone",phone);add(p,"Email",email);add(p,"Registered (yyyy-MM-dd)",reg);
        if(!confirm(p,"Customer"))return;
        execute(()->store.saveCustomer(new Customer(original==null?0:original.getId(),first.getText(),last.getText(),phone.getText(),email.getText(),reg.getText())));
    }
    private void dishEditor(Dish original) throws SQLException {
        List<Ingredient> inventory=store.ingredients();
        if(inventory.isEmpty()){JOptionPane.showMessageDialog(this,"Add ingredients first.");return;}
        JPanel top=form();
        JTextField name=field(original==null?"":original.getName()),category=field(original==null?"Burger":original.getCategory()),price=field(original==null?"0":money(original.getPrice()));
        add(top,"Name",name);add(top,"Category",category);add(top,"Price",price);
        DefaultTableModel recipe=new DefaultTableModel(new String[]{"ID","Ingredient","Unit","Required quantity (0 = unused)"},0){
            @Override public boolean isCellEditable(int row,int col){return col==3;}
        };
        for(Ingredient ingredient:inventory) recipe.addRow(new Object[]{ingredient.getId(),ingredient.getName(),ingredient.getUnit(),
            original==null?"0":String.valueOf(original.getRecipeQuantities().getOrDefault(ingredient.getId(),0.0))});
        JTable recipeTable=new JTable(recipe);recipeTable.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
        JPanel content=new JPanel(new BorderLayout(4,8));content.add(top,BorderLayout.NORTH);
        content.add(new JScrollPane(recipeTable),BorderLayout.CENTER);content.setPreferredSize(new Dimension(600,390));
        if(!confirm(content,"Dish and recipe"))return;
        if(recipeTable.isEditing())recipeTable.getCellEditor().stopCellEditing();
        execute(()->{
            Dish d=new Dish(original==null?0:original.getId(),name.getText(),category.getText(),Checks.number(price.getText(),"Price"));
            for(int r=0;r<recipe.getRowCount();r++) {
                double amount=Checks.nonNegative(Checks.number(String.valueOf(recipe.getValueAt(r,3)),"Recipe quantity"),"Recipe quantity");
                if(amount>0)d.addIngredient(inventory.get(r),amount);
            }
            store.saveDish(d);
        });
    }
    private void menuEditor(Menu original) throws SQLException {
        List<Dish> dishes=store.dishes();
        if(dishes.isEmpty()){JOptionPane.showMessageDialog(this,"Add dishes first.");return;}
        JTextField name=field(original==null?"":original.getName());
        JList<Dish> list=new JList<>(dishes.toArray(new Dish[0]));list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if(original!=null) {
            List<Integer> indices=new ArrayList<>();
            for(int i=0;i<dishes.size();i++)for(Dish d:original.getDishes())if(dishes.get(i).getId()==d.getId())indices.add(i);
            list.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
        }
        JPanel p=new JPanel(new BorderLayout(4,8));p.add(name,BorderLayout.NORTH);
        p.add(new JScrollPane(list),BorderLayout.CENTER);
        p.add(new JLabel("Select one or more dishes (Ctrl/Shift). Price is the sum of dish prices."),BorderLayout.SOUTH);
        p.setPreferredSize(new Dimension(480,310));
        if(!confirm(p,"Menu name and dishes"))return;
        execute(()->{
            Menu menu=new Menu(original==null?0:original.getId(),name.getText());
            for(Dish d:list.getSelectedValuesList())menu.addDish(d);
            store.saveMenu(menu);
        });
    }
}
