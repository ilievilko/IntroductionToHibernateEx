import entities.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Engine implements Runnable {
    private final EntityManager entityManager;
    private final BufferedReader reader;

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }


    public void run() {
        //2. Remove Objects
        //removeObjectEx();

        //3. Contains Employee
        //try {
        //    containsEmployeeEx();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        //4. Employees with Salary Over 50 000
        //employeesWithSalaryOver50000Ex();

        //5. Employees from Department
        //employeesFromDepartment();

        //6. Adding a New Address and Updating Employee
        //try {
        //    addingNewAddressAndUpdatingEmployeeEx();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        //7. Addresses with Employee Count
        //addressesWithEmployeeCountEx();

        //8. Get Employee with Project
        //try {
        //    getEmployeeWithProjectEx();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        //9. Find Latest 10 Projects
        //findLatestTenProjects();

        //10. Increase Salaries
        //increaseSalariesEx();

        //11. Remove Towns
        //try {
        //    removeTownsEx();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        // 12. Find Employees by First Name
        //try {
        //    findEmployeesByFirstNameEx();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        //13. Employees Maximum Salaries
        //employeesMaximumSalariesEx();
    }

    private void employeesMaximumSalariesEx() {

        List<Object[]> employees = this.entityManager.createQuery
                ("SELECT e.department.name, MAX(e.salary) FROM Employee AS e" +
                        "  WHERE e.salary NOT BETWEEN 30000 AND 70000 GROUP BY e.department.id", Object[].class).getResultList();

        for (Object[] employee : employees) {
            System.out.printf("%s %.2f%n", employee[0], employee[1]);
        }


    }

    private void findEmployeesByFirstNameEx() throws IOException {
        System.out.println("Enter the first two letters:");
        String firstLetters = reader.readLine();

        List<Employee> employees = this.entityManager.createQuery
                ("SELECT e FROM Employee AS e WHERE e.firstName LIKE CONCAT(:chars,'%')", Employee.class)
                .setParameter("chars", firstLetters).getResultList();

        for (Employee employee : employees) {
            System.out.printf("%s %s - %s - ($%.2f)%n",
                    employee.getFirstName(), employee.getLastName(), employee.getJobTitle(), employee.getSalary());
        }

    }

    private void removeTownsEx() throws IOException {
        System.out.println("Enter town name: ");
        String townName = reader.readLine();

        List<Address> address = this.entityManager.createQuery
                ("SELECT a FROM Address AS a WHERE a.town.name = :name", Address.class)
                .setParameter("name", townName).getResultList();
        int numberAddresses = address.size();
        List<Employee> employee = this.entityManager.createQuery
                ("SELECT e FROM Employee AS e WHERE e.address.town.name =:name", Employee.class)
                .setParameter("name", townName).getResultList();
        Town town = this.entityManager.createQuery
                ("SELECT t FROM Town AS t WHERE t.name = :name", Town.class)
                .setParameter("name", townName).getSingleResult();

        this.entityManager.getTransaction().begin();
        employee.forEach(this.entityManager::remove);
        address.forEach(this.entityManager::remove);
        this.entityManager.remove(town);
        this.entityManager.getTransaction().commit();
        if (numberAddresses > 1) {
            System.out.printf("%d addresses in %s deleted", numberAddresses, townName);
        } else {
            System.out.printf("%d address in %s deleted", numberAddresses, townName);
        }

    }

    private void increaseSalariesEx() {
        List<Employee> employee = this.entityManager.createQuery
                ("SELECT e FROM Employee AS e WHERE e.department.id = 1 or e.department.id = 2 or e.department.id = 4",
                        Employee.class).getResultList();
        this.entityManager.getTransaction().begin();
        for (Employee employee1 : employee) {
            BigDecimal updateSalary = BigDecimal.valueOf(employee1.getSalary().doubleValue() * 1.12);
            employee1.setSalary(updateSalary);
            this.entityManager.merge(employee1);
        }
        this.entityManager.getTransaction().commit();

        for (Employee employee1 : employee) {
            System.out.printf("%s %s ($%.2f)%n", employee1.getFirstName(), employee1.getLastName(), employee1.getSalary());
        }


    }

    private void findLatestTenProjects() {
        List<Project> projects = this.entityManager.createQuery
                ("SELECT p FROM Project AS p ORDER BY p.startDate DESC", Project.class)
                .setMaxResults(10).getResultList();

        projects.sort(Comparator.comparing(Project::getName));

        projects.forEach(project -> System.out.printf("Project name: %s%n        Project Description: %s%n        " +
                        "Project Start Date: %s%n        Project End Date: %s%n", project.getName(), project.getDescription(),
                project.getStartDate(), project.getEndDate()));
    }

    private void getEmployeeWithProjectEx() throws IOException {
        System.out.println("Enter employee id:");
        int employeeId = Integer.parseInt(reader.readLine());

        List<Employee> employee = this.entityManager.createQuery("SELECT e FROM Employee AS e WHERE e.id = :id", Employee.class)
                .setParameter("id", employeeId).getResultList();

        List<Set<Project>> e = employee.stream().map(Employee::getProjects).collect(Collectors.toList());

        employee.forEach(employee1 -> System.out.printf("%s %s - %s%n", employee1.getFirstName(), employee1.getLastName(),
                employee1.getJobTitle()));
        List<String> toSort = e.stream().flatMap(Collection::stream).map(Project::getName).sorted().collect(Collectors.toList());
        toSort.stream().map(name -> "      " + name).forEach(System.out::println);

    }

    private void addressesWithEmployeeCountEx() {
        List<Address> addresses = this.entityManager.createQuery
                ("SELECT a FROM Address AS a ORDER BY a.employees.size DESC ", Address.class).setMaxResults(10).getResultList();

        addresses.forEach(address -> System.out.printf("%s, %s - %d employees%n", address.getText(), address.getTown().getName(), address.getEmployees().size()));

    }

    private void addingNewAddressAndUpdatingEmployeeEx() throws IOException {
        System.out.println("Enter last name: ");
        String lastName = reader.readLine();


        Employee employee = this.entityManager.createQuery
                ("SELECT e FROM Employee AS e WHERE e.lastName = :name", Employee.class)
                .setParameter("name", lastName).getSingleResult();

        Address address = new Address();
        address.setText("Vitoshka 15");
        address.setTown(this.entityManager.createQuery
                ("SELECT t FROM Town AS t WHERE t.id = 32", Town.class).getSingleResult());

        this.entityManager.getTransaction().begin();
        this.entityManager.persist(address);
        this.entityManager.detach(employee);
        employee.setAddress(address);
        this.entityManager.merge(employee);
        this.entityManager.flush();
        this.entityManager.getTransaction().commit();
    }

    private void employeesFromDepartment() {
        List<Employee> employees = this.entityManager.createQuery
                ("SELECT e FROM  Employee AS e WHERE e.department.id = 6 ORDER BY e.salary, e.id", Employee.class)
                .getResultList();

        for (Employee employee : employees) {
            System.out.printf("%s %s from %s - $%.2f%n", employee.getFirstName(),
                    employee.getLastName(), employee.getDepartment().getName(), employee.getSalary());
        }

    }

    private void employeesWithSalaryOver50000Ex() {
        List<Employee> employees = this.entityManager.createQuery
                ("SELECT e FROM Employee AS e WHERE e.salary > 50000.00", Employee.class).getResultList();
        for (Employee employee : employees) {
            System.out.println(employee.getFirstName());
        }

    }

    private void containsEmployeeEx() throws IOException {
        System.out.println("Enter employee name:");
        String fullName = this.reader.readLine();

        try {
            Employee employee = this.entityManager.createQuery
                    ("SELECT e FROM Employee AS e WHERE CONCAT(e.firstName, ' ', e.lastName) = :name", Employee.class)
                    .setParameter("name", fullName).getSingleResult();
            System.out.println("Yes");
        } catch (NoResultException nre) {
            System.out.println("No");
        }
    }

    private void removeObjectEx() {
        List<Town> towns = this.entityManager.createQuery
                ("SELECT t FROM Town AS t WHERE length(t.name) > 5", Town.class).getResultList();

        this.entityManager.getTransaction().begin();
        towns.forEach(this.entityManager::detach);
        List<Town> townList = this.entityManager.createQuery("SELECT t FROM Town AS t", Town.class).getResultList();
        townList.forEach(town -> town.setName(town.getName().toLowerCase()));
        towns.forEach(this.entityManager::merge);
        this.entityManager.flush();
        this.entityManager.getTransaction().commit();
    }
}
