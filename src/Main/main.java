package Main;

import config.dbConnect;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class main {

// ----------------------
// Utility view methods
// ----------------------
public static void viewAvailableCars(dbConnect con) {
    String Query = "SELECT car_id, brand, model, daily_rate, status FROM tbl_cars";
    String[] carHeaders = {"Car ID", "Brand", "Model", "Daily Rate", "Status"};
    String[] carColumns = {"car_id", "brand", "model", "daily_rate", "status"};
    con.viewRecords(Query, carHeaders, carColumns);
}

public static void viewAvailableOnly(dbConnect con) {
    String Query = "SELECT car_id, brand, model, daily_rate FROM tbl_cars WHERE status = 'Available'";
    String[] carHeaders = {"Car ID", "Brand", "Model", "Daily Rate"};
    String[] carColumns = {"car_id", "brand", "model", "daily_rate"};
    con.viewRecords(Query, carHeaders, carColumns);
}

public static void viewRentals(dbConnect con) {
    String Query = "SELECT r.rental_id, u.u_name AS Renter, c.brand, c.model, r.rental_date, r.return_date, r.total_cost, r.is_returned "
            + "FROM tbl_rentals r "
            + "JOIN tbl_users u ON r.u_id = u.u_id "
            + "JOIN tbl_cars c ON r.car_id = c.car_id ";
    String[] rentalHeaders = {"Rental ID", "Renter Name", "Car Brand", "Car Model", "Rent Date", "Return Date", "Total Cost", "Returned?"};
    String[] rentalColumns = {"rental_id", "Renter", "brand", "model", "rental_date", "return_date", "total_cost", "is_returned"};
    con.viewRecords(Query, rentalHeaders, rentalColumns);
}

public static void viewActiveRentals(dbConnect con) {
    String Query = "SELECT r.rental_id, u.u_name AS Renter, c.brand, c.model, r.rental_date, r.return_date, r.total_cost "
            + "FROM tbl_rentals r "
            + "JOIN tbl_users u ON r.u_id = u.u_id "
            + "JOIN tbl_cars c ON r.car_id = c.car_id "
            + "WHERE r.is_returned = 0";
    String[] rentalHeaders = {"Rental ID", "Renter Name", "Car Brand", "Car Model", "Rent Date", "Est. Return Date", "Total Cost"};
    String[] rentalColumns = {"rental_id", "Renter", "brand", "model", "rental_date", "return_date", "total_cost"};
    con.viewRecords(Query, rentalHeaders, rentalColumns);
}

public static void viewUsers(dbConnect con) {
    String Query = "SELECT u_id, u_name, u_email, u_type, u_status FROM tbl_users";
    String[] userHeaders = {"ID", "Name", "Email", "Type", "Status"};
    String[] userColumns = {"u_id", "u_name", "u_email", "u_type", "u_status"};
    con.viewRecords(Query, userHeaders, userColumns);
}

public static void viewPendingUsers(dbConnect con) {
    String Query = "SELECT u_id, u_name, u_email, u_type FROM tbl_users WHERE u_status = 'Pending'";
    String[] userHeaders = {"ID", "Name", "Email", "Type"};
    String[] userColumns = {"u_id", "u_name", "u_email", "u_type"};
    con.viewRecords(Query, userHeaders, userColumns);
}

public static void viewApprovedCustomers(dbConnect con) {
    String Query = "SELECT u_id, u_name, u_email FROM tbl_users WHERE u_type = 'User' AND u_status = 'Approved'";
    String[] userHeaders = {"ID", "Name", "Email"};
    String[] userColumns = {"u_id", "u_name", "u_email"};
    con.viewRecords(Query, userHeaders, userColumns);
}

// ----------------------
// Rental operations
// ----------------------
public static void addRental(int userId, Scanner sc, dbConnect con) {
    System.out.println("\n--- NEW CAR RENTAL ---");
    viewAvailableOnly(con);

    int carId;
    while (true) {
        System.out.print("Enter Car ID to Rent (or 0 to cancel): ");
        if (sc.hasNextInt()) {
            carId = sc.nextInt();
            sc.nextLine();
            if (carId == 0) return;
            List<Map<String,Object>> carRes = con.fetchRecords("SELECT car_id, daily_rate, status FROM tbl_cars WHERE car_id = ?", carId);
            if (carRes.isEmpty()) {
                System.out.println("❌ Car ID not found. Try again.");
            } else if (!carRes.get(0).get("status").toString().equalsIgnoreCase("Available")) {
                System.out.println("❌ Car is not available. Choose another car.");
            } else break;
        } else {
            System.out.println("❌ Invalid input.");
            sc.nextLine();
        }
    }

    int rentalDays;
    while (true) {
        System.out.print("Enter number of rental days: ");
        if (sc.hasNextInt()) {
            rentalDays = sc.nextInt();
            sc.nextLine();
            if (rentalDays > 0) break;
            System.out.println("❌ Rental days must be at least 1.");
        } else {
            System.out.println("❌ Invalid input.");
            sc.nextLine();
        }
    }

    List<Map<String,Object>> rateResult = con.fetchRecords("SELECT daily_rate FROM tbl_cars WHERE car_id = ?", carId);
    if (rateResult.isEmpty()) {
        System.out.println("❌ Error: Could not retrieve car rate. Rental cancelled.");
        return;
    }
    double dailyRate = Double.parseDouble(rateResult.get(0).get("daily_rate").toString());
    double totalCost = dailyRate * rentalDays;

    String rentalDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, rentalDays);
    String returnDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

    con.addRecord("INSERT INTO tbl_rentals (u_id, car_id, rental_date, return_date, total_cost, is_returned) VALUES (?, ?, ?, ?, ?, 0)",
            userId, carId, rentalDate, returnDateStr, totalCost);

    con.updateRecord("UPDATE tbl_cars SET status = 'Rented' WHERE car_id = ?", carId);
    System.out.println("✅ Rental recorded! Car ID " + carId + " rented for " + rentalDays + " days.");
}

public static void returnRental(int userId, Scanner sc, dbConnect con) {
    System.out.println("\n--- RETURN A RENTED CAR ---");
    List<Map<String,Object>> active = con.fetchRecords("SELECT r.rental_id, r.car_id, c.brand, c.model, r.rental_date, r.return_date, r.total_cost FROM tbl_rentals r JOIN tbl_cars c ON r.car_id = c.car_id WHERE r.u_id = ? AND r.is_returned = 0", userId);
    if(active.isEmpty()) { System.out.println("You have no active rentals."); return; }

    System.out.println("Active rentals:");
    for(Map<String,Object> r : active)
        System.out.printf("Rental ID: %s | Car ID: %s | %s %s | Rent: %s | Due: %s | Total: %s\n",
                r.get("rental_id"), r.get("car_id"), r.get("brand"), r.get("model"),
                r.get("rental_date"), r.get("return_date"), r.get("total_cost"));

    int rentalId;
    while(true) {
        System.out.print("Enter Rental ID to return (0 to cancel): ");
        if(sc.hasNextInt()) {
            rentalId = sc.nextInt(); sc.nextLine();
            if(rentalId==0) return;
            List<Map<String,Object>> chk = con.fetchRecords("SELECT rental_id, car_id FROM tbl_rentals WHERE rental_id = ? AND u_id = ? AND is_returned = 0", rentalId, userId);
            if(chk.isEmpty()) System.out.println("❌ Rental ID not found or already returned.");
            else {
                Object carIdObj = chk.get(0).get("car_id");
                int carId;
                if (carIdObj instanceof Integer) {
                    carId = (Integer) carIdObj;
                } else if (carIdObj instanceof Long) {
                    carId = ((Long) carIdObj).intValue();
                } else {
                    carId = Integer.parseInt(carIdObj.toString());
                }
                con.updateRecord("UPDATE tbl_rentals SET is_returned = 1 WHERE rental_id = ?", rentalId);
                con.updateRecord("UPDATE tbl_cars SET status = 'Available' WHERE car_id = ?", carId);
                System.out.println("✅ Rental returned. Car ID " + carId + " is now Available.");
                return;
            }
        } else { System.out.println("❌ Invalid input."); sc.nextLine(); }
    }
}

// ----------------------
// Car admin operations
// ----------------------
public static void adminAddCar(Scanner sc, dbConnect con) {
    System.out.println("\n--- ADD NEW CAR ---");
    System.out.print("Brand: "); String brand = sc.nextLine();
    System.out.print("Model: "); String model = sc.nextLine();
    double rate;
    while(true) {
        System.out.print("Daily Rate: "); if(sc.hasNextDouble()){ rate=sc.nextDouble(); sc.nextLine(); if(rate>0) break;} else{ sc.nextLine(); } 
    }
    con.addRecord("INSERT INTO tbl_cars (brand, model, daily_rate, status) VALUES (?, ?, ?, 'Available')", brand, model, rate);
    System.out.println("✅ Car added: "+brand+" "+model);
}

public static void adminDeleteCar(Scanner sc, dbConnect con) {
    System.out.println("\n--- DELETE A CAR ---");
    viewAvailableCars(con);
    int carId;
    while(true){
        System.out.print("Enter Car ID to delete (0 to cancel): ");
        if(sc.hasNextInt()){ carId=sc.nextInt(); sc.nextLine(); if(carId==0) return; break; } else{ sc.nextLine(); } 
    }
    List<Map<String,Object>> r=con.fetchRecords("SELECT car_id, status FROM tbl_cars WHERE car_id=?", carId);
    if(!r.isEmpty() && !r.get(0).get("status").toString().equalsIgnoreCase("Rented")){
        con.deleteRecord("DELETE FROM tbl_cars WHERE car_id=?", carId);
        System.out.println("✅ Car deleted (ID "+carId+")");
    } else System.out.println("❌ Car is rented or not found.");
}

// ----------------------
// Admin dashboard
// ----------------------
public static void adminDashboard(Scanner sc, dbConnect con){
    int choice;
    do {
        System.out.println("\n--- ADMIN DASHBOARD ---");
        System.out.println("1. Approve / Reject Pending Users");
        System.out.println("2. View All Active Rentals");
        System.out.println("3. View All Cars");
        System.out.println("4. Add Car");
        System.out.println("5. Delete Car");
        System.out.println("6. View Approved Customers");
        System.out.println("7. View All Users");
        System.out.println("8. Logout");
        System.out.print("Enter choice: ");
        choice = sc.hasNextInt()?sc.nextInt():-1; sc.nextLine();
        switch(choice){
            case 1:
                viewPendingUsers(con);
                System.out.print("ID to Approve (0 skip): "); int aid=sc.nextInt(); sc.nextLine(); if(aid>0) con.updateRecord("UPDATE tbl_users SET u_status='Approved' WHERE u_id=?", aid);
                System.out.print("ID to Reject (0 skip): "); int rid=sc.nextInt(); sc.nextLine(); if(rid>0) con.updateRecord("UPDATE tbl_users SET u_status='Rejected' WHERE u_id=?", rid);
                break;
            case 2: viewActiveRentals(con); break;
            case 3: viewAvailableCars(con); break;
            case 4: adminAddCar(sc, con); break;
            case 5: adminDeleteCar(sc, con); break;
            case 6: viewApprovedCustomers(con); break;
            case 7: viewUsers(con); break;
            case 8: System.out.println("Logging out..."); break;
            default: System.out.println("❌ Invalid choice.");
        }
    } while(choice!=8);
}

// ----------------------
// Main program
// ----------------------
public static void main(String[] args) {
    dbConnect con = new dbConnect();
    java.sql.Connection testConn = dbConnect.connectDB();
    if (testConn == null) {
        System.out.println("❌ Failed to connect to database. Please check your database configuration.");
        System.exit(1);
    }
    try {
        testConn.close();
    } catch (java.sql.SQLException e) {
        // Ignore
    }
    Scanner sc = new Scanner(System.in);
    int choice;
    String cont;

    System.out.println("=== CAR RENTAL SYSTEM ===");

    do {
        System.out.println("\nMain Menu:\n1. Login\n2. Register\n3. Exit\nEnter choice: ");
        choice = sc.hasNextInt()?sc.nextInt():-1; sc.nextLine();

        switch(choice){
            case 1:
                System.out.print("Email: "); String email=sc.nextLine();
                System.out.print("Password: "); String password=sc.nextLine();
                List<Map<String,Object>> result=con.fetchRecords("SELECT u_id,u_name,u_status,u_type FROM tbl_users WHERE u_email=? AND u_pass=?", email,password);
                if(result.isEmpty()){ System.out.println("❌ Invalid credentials"); break;}
                Map<String,Object> user=result.get(0);
                Object userIdObj = user.get("u_id");
                int currentUserId;
                if (userIdObj instanceof Integer) {
                    currentUserId = (Integer) userIdObj;
                } else if (userIdObj instanceof Long) {
                    currentUserId = ((Long) userIdObj).intValue();
                } else {
                    currentUserId = Integer.parseInt(userIdObj.toString());
                }
                String type=user.get("u_type").toString();
                String status=user.get("u_status").toString();
                if(status.equalsIgnoreCase("Pending")||status.equalsIgnoreCase("Rejected")){ System.out.println("⚠️ Account "+status); break; }
                System.out.println("✅ Welcome "+user.get("u_name")+" ("+type+")");

                // dashboard
                int dashChoice;
                do{
                    if(type.equalsIgnoreCase("Admin")){ adminDashboard(sc, con); break; }
                    else if(type.equalsIgnoreCase("User")){
                        System.out.println("\n--- USER DASHBOARD ---");
                        System.out.println("1. View Available Cars");
                        System.out.println("2. Rent a Car");
                        System.out.println("3. Return a Car");
                        System.out.println("4. Logout");
                        System.out.print("Enter choice: "); dashChoice=sc.hasNextInt()?sc.nextInt():-1; sc.nextLine();
                        switch(dashChoice){
                            case 1: viewAvailableOnly(con); break;
                            case 2: addRental(currentUserId, sc, con); break;
                            case 3: returnRental(currentUserId, sc, con); break;
                            case 4: System.out.println("Logging out..."); break;
                            default: System.out.println("❌ Invalid choice.");
                        }
                    } else dashChoice=4;
                } while(dashChoice!=4);
                break;

            case 2: // register
                System.out.print("Name: "); String name=sc.nextLine();
                String emailReg;
                while(true){
                    System.out.print("Email: "); emailReg=sc.nextLine();
                    if(con.fetchRecords("SELECT u_id FROM tbl_users WHERE u_email=?", emailReg).isEmpty()) break;
                    System.out.println("❌ Email already exists.");
                }
                System.out.print("Type (1-Admin /2-User): "); int typeInt=sc.hasNextInt()?sc.nextInt():2; sc.nextLine();
                typeInt=(typeInt<1||typeInt>2)?2:typeInt; String tp=(typeInt==1)?"Admin":"User";
                System.out.print("Password: "); String pass=sc.nextLine();
                con.addRecord("INSERT INTO tbl_users (u_name,u_email,u_type,u_status,u_pass) VALUES (?,?,?,?,?)", name,emailReg,tp,"Pending",pass);
                System.out.println("✅ Registration successful! Pending approval.");
                break;

            case 3: System.out.println("Exiting..."); System.exit(0); break;
            default: System.out.println("❌ Invalid choice.");
        }

        System.out.print("\nReturn to main menu? (Y/N): "); cont=sc.nextLine().trim();
    } while(cont.equalsIgnoreCase("Y"));

    System.out.println("👋 Thank you for using the Car Rental System.");
    sc.close();
}


}
