public class Employee extends Person {
    private String role;
    private double salary;
    private String hireDate;
    public Employee(int id, String name, String lastName, String phone, String email,
                    String role, double salary, String hireDate) {
        super(id, name, lastName, phone, email);
        setRole(role); setSalary(salary); setHireDate(hireDate);
    }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = Checks.text(role, "Role"); }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = Checks.nonNegative(salary, "Salary"); }
    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = Checks.date(hireDate, "Hire date").toString(); }
    @Override public String getDescription() { return "Employee " + getFullName() + " - " + role + " (hired " + hireDate + ")"; }
}
