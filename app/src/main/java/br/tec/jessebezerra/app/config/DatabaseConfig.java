package br.tec.jessebezerra.app.config;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:agenda.db";
    private static Connection connection;
    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeFlyway();
            initialized = true;
        }
        
        if (connection == null || connection.isClosed()) {
            System.out.println("Criando nova conexão com banco de dados: " + URL);
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    private static void initializeFlyway() {
        System.out.println("=== Inicializando Flyway ===");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(URL, null, null)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();
            
            System.out.println("Executando migrations...");
            int migrationsExecuted = flyway.migrate().migrationsExecuted;
            System.out.println("Migrations executadas: " + migrationsExecuted);
            System.out.println("=== Flyway inicializado com sucesso ===");
        } catch (Exception e) {
            System.err.println("ERRO ao inicializar Flyway: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar banco de dados com Flyway", e);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
