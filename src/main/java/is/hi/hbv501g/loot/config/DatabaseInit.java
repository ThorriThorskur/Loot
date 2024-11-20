package is.hi.hbv501g.loot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInit implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        String sql = "ALTER TABLE user_entity ADD COLUMN role VARCHAR(255) DEFAULT 'USER'";

        try {
            jdbcTemplate.execute(sql);
            System.out.println("Role column added successfully!");
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate column name")) {
                System.out.println("Role column already exists. Skipping ALTER TABLE.");
            } else {
                System.err.println("Error executing SQL: " + e.getMessage());
            }
        }
    }
}
