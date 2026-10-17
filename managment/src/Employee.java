public class Employee extends Person {
    private String role;
    private double salary;
    private String hireDate;

    public Employee(int id, String name, String lastName, String phone, String email,
                    String role, double salary, String hireDate) {
        super(id, name, lastName, phone, email);
        this.role = role;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }

    @Override
    public String getDescription() {
        return "Employee " + getFullName() + " - " + role + " (hired " + hireDate + ")";
    }
}