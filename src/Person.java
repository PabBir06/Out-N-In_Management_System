public abstract class Person {
    private int id;
    private String name;
    private String lastName;
    private String phone;
    private String email;
    public Person(int id, String name, String lastName, String phone, String email) {
        this.id = id;
        setName(name); setLastName(lastName); setPhone(phone); setEmail(email);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return name + " " + lastName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Checks.text(name, "First name"); }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = Checks.text(lastName, "Last name"); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = Checks.text(phone, "Phone"); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = Checks.email(email); }
    public abstract String getDescription();
}
