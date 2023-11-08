import java.sql.*;
import java.util.*;
import java.util.Comparator;
class Item {
    private String name;
    private int quantity;
    private double cost;

    public Item(String name, int quantity, double cost) {
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "Item: " + name + ", Quantity: " + quantity + ", Cost: " + cost;
    }
}

class InventoryManager {
    private Connection connection;

    public InventoryManager(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    public void addItem(String name, int quantity, double cost) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO inventory (name, quantity, cost) VALUES (?, ?, ?)");
            statement.setString(1, name);
            statement.setInt(2, quantity);
            statement.setDouble(3, cost);
            statement.executeUpdate();
            System.out.println("Item added to inventory.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewInventory() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM inventory");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                double cost = resultSet.getDouble("cost");
                System.out.println("Item: " + name + ", Quantity: " + quantity + ", Cost: " + cost);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateQuantity(String itemName, int newQuantity) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE inventory SET quantity = ? WHERE name = ?");
            statement.setInt(1, newQuantity);
            statement.setString(2, itemName);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Quantity updated successfully!");
            } else {
                System.out.println("Item not found in inventory.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS inventory (name TEXT, quantity INT, cost DOUBLE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setItemCost(String itemName, double cost) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE inventory SET cost = ? WHERE name = ?");
            statement.setDouble(1, cost);
            statement.setString(2, itemName);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Cost updated successfully!");
            } else {
                System.out.println("Item not found in inventory.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getItemCost(String itemName) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT cost FROM inventory WHERE name = ?");
            statement.setString(1, itemName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("cost");
            } else {
                System.out.println("Item not found in inventory.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void generateBill(List<Item> items) {
        double totalCost = 0.0;

        System.out.println("\n--- Bill ---");
        for (Item item : items) {
            double itemCost = getItemCost(item.getName());
            System.out.println(item.toString() + ", Total Cost: " + (itemCost * item.getQuantity()));
            totalCost += (itemCost * item.getQuantity());
        }

        System.out.println("Total Bill: " + totalCost);
    }

    public void deleteItem(String itemName) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM inventory WHERE name = ?");
            statement.setString(1, itemName);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Item deleted from inventory.");
            } else {
                System.out.println("Item not found in inventory.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM inventory");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                double cost = resultSet.getDouble("cost");
                items.add(new Item(name, quantity, cost));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    public List<Item> getSortedInventory(Comparator<Item> comparator) {
        List<Item> items = getAllItems();
        items.sort(comparator);
        return items;
    }
}

public class InventoryManagementApp {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rainy", "root", "root");
            InventoryManager inventoryManager = new InventoryManager(connection);

            Scanner scanner = new Scanner(System.in);
            int choice = 0;

            while (choice != 9) {
                printMenu();
                choice = getUserChoice(scanner);

                switch (choice) {
                    case 1:
                        addItem(inventoryManager, scanner);
                        break;
                    case 2:
                        viewInventory(inventoryManager);
                        break;
                    case 3:
                        updateQuantity(inventoryManager, scanner);
                        break;
                    case 4:
                        searchItem(inventoryManager, scanner);
                        break;
                    case 5:
                        deleteItem(inventoryManager, scanner);
                        break;
                    case 6:
                        viewTotalInventoryCost(inventoryManager);
                        break;
                    case 7:
                        generateBill(inventoryManager, scanner);
                        break;
                    case 8:
                        sortInventory(inventoryManager, scanner);
                        break;
                    case 9:
                        System.out.println("Exiting Inventory Management System...");
                        break;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            }

            scanner.close();
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addItem(InventoryManager inventoryManager, Scanner scanner) {
        System.out.print("Enter Item Name: ");
        String itemName = scanner.nextLine();
        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter Cost: ");
        double cost = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        inventoryManager.addItem(itemName, quantity, cost);
    }

    private static void viewInventory(InventoryManager inventoryManager) {
        System.out.println("\n--- Inventory ---");
        inventoryManager.viewInventory();
    }

    private static void updateQuantity(InventoryManager inventoryManager, Scanner scanner) {
        System.out.print("Enter Item Name to Update Quantity: ");
        String updateItemName = scanner.nextLine();
        System.out.print("Enter New Quantity: ");
        int newQuantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        inventoryManager.updateQuantity(updateItemName, newQuantity);
    }

    private static void searchItem(InventoryManager inventoryManager, Scanner scanner) {
        System.out.print("Enter Item Name to Search: ");
        String searchItemName = scanner.nextLine();
        double itemCost = inventoryManager.getItemCost(searchItemName);
        if (itemCost > 0) {
            System.out.println("Item: " + searchItemName + ", Cost: " + itemCost);
        } else {
            System.out.println("Item not found in inventory.");
        }
    }

    private static void deleteItem(InventoryManager inventoryManager, Scanner scanner) {
        System.out.print("Enter Item Name to Delete: ");
        String deleteItemName = scanner.nextLine();
        inventoryManager.deleteItem(deleteItemName);
    }

    private static void viewTotalInventoryCost(InventoryManager inventoryManager) {
        double totalInventoryCost = calculateTotalInventoryCost(inventoryManager);
        System.out.println("Total Inventory Cost: " + totalInventoryCost);
    }

    private static void generateBill(InventoryManager inventoryManager, Scanner scanner) {
        System.out.println("\n--- Generate Bill ---");
        List<Item> selectedItems = new ArrayList<>();

        Thread inputThread = new Thread(() -> {
            while (true) {
                System.out.print("Enter Item Name to add to bill (or 'done' to finish): ");
                String billItemName = scanner.nextLine();
                if (billItemName.equalsIgnoreCase("done")) {
                    break;
                }

                double itemCostForBill = inventoryManager.getItemCost(billItemName);
                if (itemCostForBill > 0) {
                    System.out.print("Enter Quantity: ");
                    int itemQuantity = scanner.nextInt();
                    scanner.nextLine();

                    selectedItems.add(new Item(billItemName, itemQuantity, itemCostForBill));
                }
            }
        });

        inputThread.start();
        try {
            inputThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!selectedItems.isEmpty()) {
            Thread billThread = new Thread(() -> inventoryManager.generateBill(selectedItems));
            billThread.start();
            try {
                billThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No items selected for the bill.");
        }
    }

    private static double calculateTotalInventoryCost(InventoryManager inventoryManager) {
        List<Item> allItems = inventoryManager.getAllItems();
        double totalCost = 0.0;
        for (Item item : allItems) {
            totalCost += item.getCost();
        }
        return totalCost;
    }
 private static void sortInventory(InventoryManager inventoryManager, Scanner scanner) {
        System.out.println("\n--- Sort Inventory ---");
        System.out.println("1. Sort by Quantity");
        System.out.println("2. Sort by Name");
        System.out.println("3. Sort by Cost");
        System.out.print("Enter your choice (1-3): ");

        int sortChoice = getUserChoice(scanner);

        Comparator<Item> comparator = null;
        
        switch (sortChoice) {
            case 1:
                comparator = Comparator.comparingInt(Item::getQuantity);
                break;
            case 2:
                comparator = Comparator.comparing(Item::getName);
                break;
            case 3:
                comparator = Comparator.comparingDouble(Item::getCost);
                break;
            default:
                System.out.println("Invalid choice! Sorting cancelled.");
                return;
        }

        List<Item> sortedInventory = inventoryManager.getSortedInventory(comparator);

        System.out.println("\n--- Sorted Inventory ---");
        for (Item item : sortedInventory) {
            System.out.println(item);
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Inventory Management Menu ---");
        System.out.println("1. Add Item");
        System.out.println("2. View Inventory");
        System.out.println("3. Update Quantity");
        System.out.println("4. Search Item");
        System.out.println("5. Delete Item");
        System.out.println("6. View Total Inventory Cost");
        System.out.println("7. Generate Bill");
        System.out.println("8. Sorting");
         System.out.println("9. Exit");
        System.out.print("Enter your choice (1-8): ");
    }

    private static int getUserChoice(Scanner scanner) {
        int choice = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                choice = scanner.nextInt();
                validInput = true;
            } catch (Exception e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine();
            }
        }
        scanner.nextLine();
        return choice;
    }
    
}