import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantManagementSystem {
    private Connection connection;

    public TenantManagementSystem() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/yourdatabase", "username", "password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tenant class
    public class Tenant {
        private int tenantID;
        private String name;
        private String phoneNumber;
        private String email;
        private Date checkInDate;
        private Date checkOutDate;
        private int apartmentNumber;

        public Tenant(int tenantID, String name, String phoneNumber, String email, Date checkInDate, Date checkOutDate, int apartmentNumber) {
            this.tenantID = tenantID;
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.apartmentNumber = apartmentNumber;
        }
    }

    // MaintenanceRequest class
    public class MaintenanceRequest {
        private int requestID;
        private int apartmentNumber;
        private String area;
        private String problemDescription;
        private Timestamp requestDateTime;
        private String photoPath;
        private String status;

        public MaintenanceRequest(int requestID, int apartmentNumber, String area, String problemDescription, Timestamp requestDateTime, String photoPath, String status) {
            this.requestID = requestID;
            this.apartmentNumber = apartmentNumber;
            this.area = area;
            this.problemDescription = problemDescription;
            this.requestDateTime = requestDateTime;
            this.photoPath = photoPath;
            this.status = status;
        }
    }

    // StaffMember class
    public class StaffMember {
        // Method to browse maintenance requests with filters
        public List<MaintenanceRequest> browseMaintenanceRequests(int apartmentNumber, String area, Date startDate, Date endDate, String status) {
            List<MaintenanceRequest> requests = new ArrayList<>();
            try {
                // Build the SQL query based on filters
                StringBuilder queryBuilder = new StringBuilder("SELECT * FROM MaintenanceRequest WHERE 1=1");

                if (apartmentNumber > 0) {
                    queryBuilder.append(" AND apartmentNumber = ").append(apartmentNumber);
                }

                if (area != null && !area.isEmpty()) {
                    queryBuilder.append(" AND area = '").append(area).append("'");
                }

                if (startDate != null) {
                    queryBuilder.append(" AND requestDateTime >= '").append(startDate).append("'");
                }

                if (endDate != null) {
                    queryBuilder.append(" AND requestDateTime <= '").append(endDate).append("'");
                }

                if (status != null && !status.isEmpty()) {
                    queryBuilder.append(" AND status = '").append(status).append("'");
                }

                // Execute the query
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(queryBuilder.toString());

                // Process the results
                while (resultSet.next()) {
                    MaintenanceRequest request = new MaintenanceRequest(
                            resultSet.getInt("requestID"),
                            resultSet.getInt("apartmentNumber"),
                            resultSet.getString("area"),
                            resultSet.getString("problemDescription"),
                            resultSet.getTimestamp("requestDateTime"),
                            resultSet.getString("photoPath"),
                            resultSet.getString("status")
                    );
                    requests.add(request);
                }

                // Close resources
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return requests;
        }

        // Method to update the status of a maintenance request
        public void updateMaintenanceRequestStatus(int requestID, String newStatus) {
            try {
                // Update the status in the database
                String updateQuery = "UPDATE MaintenanceRequest SET status = ? WHERE requestID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, newStatus);
                preparedStatement.setInt(2, requestID);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Manager class
    public class Manager {
        // Method to add a new tenant
        public void addTenant(String name, String phoneNumber, String email, Date checkInDate, Date checkOutDate, int apartmentNumber) {
            try {
                // Insert a new tenant into the database
                String insertQuery = "INSERT INTO Tenant (name, phoneNumber, email, checkInDate, checkOutDate, apartmentNumber) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, phoneNumber);
                preparedStatement.setString(3, email);
                preparedStatement.setDate(4, checkInDate);
                preparedStatement.setDate(5, checkOutDate);
                preparedStatement.setInt(6, apartmentNumber);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Method to move a tenant to another apartment
        public void moveTenantToAnotherApartment(int tenantID, int newApartmentNumber) {
            try {
                // Update the apartment number in the database
                String updateQuery = "UPDATE Tenant SET apartmentNumber = ? WHERE tenantID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setInt(1, newApartmentNumber);
                preparedStatement.setInt(2, tenantID);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Method to delete a tenant
        public void deleteTenant(int tenantID) {
            try {
                // Delete the tenant from the database
                String deleteQuery = "DELETE FROM Tenant WHERE tenantID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setInt(1, tenantID);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Close the database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TenantManagementSystem system = new TenantManagementSystem();
        // Example usage
        Manager manager = system.new Manager();
        manager.addTenant("John Doe", "1234567890", "john.doe@email.com", Date.valueOf("2023-01-01"), Date.valueOf("2023-12-31"), 101);

        StaffMember staffMember = system.new StaffMember();
        List<MaintenanceRequest> maintenanceRequests = staffMember.browseMaintenanceRequests(101, "kitchen", Date.valueOf("2023-01-01"), Date.valueOf("2023-12-31"), "pending");
        System.out.println("Maintenance Requests: " + maintenanceRequests);

        if (!maintenanceRequests.isEmpty()) {
            MaintenanceRequest firstRequest = maintenanceRequests.get(0);
            staffMember.updateMaintenanceRequestStatus(firstRequest.requestID, "completed");
        }

        system.closeConnection();
    }
}
