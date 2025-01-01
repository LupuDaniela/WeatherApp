import java.sql.*;
import java.util.*;

public class DatabaseService {
    private Connection connection;

    public DatabaseService() {
        try {
            connection = DriverManager.getConnection(
                    Constants.DB_URL,
                    Constants.DB_USER,
                    Constants.DB_PASSWORD
            );
            createTables();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    private void createTables() throws SQLException{
        String createUsersTable= """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(100) NOT NULL,
                role VARCHAR(10) NOT NULL
            )
        """;

        String createLocationsTable= """
                CREATE TABLE IF NOT EXISTS locations (
                                name VARCHAR(100) NOT NULL,
                                latitude DOUBLE PRECISION NOT NULL,
                                longitude DOUBLE PRECISION NOT NULL,
                                PRIMARY KEY (latitude, longitude)
                            )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createUsersTable);
            statement.execute(createLocationsTable);
        }
    }

    public Optional<User>authenticateUser(String username, String password){
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement preparedStatement= connection.prepareStatement(sql)){
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,password);
            ResultSet resultSet= preparedStatement.executeQuery();

            if(resultSet.next()){
                return Optional.of(new User(
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        UserRole.valueOf(resultSet.getString("role"))
                ));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Add methods for location operations
}
