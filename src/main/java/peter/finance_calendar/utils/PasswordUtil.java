package peter.finance_calendar.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // Generate a hashed password
    public static String hashPassword(String plainPassword) {
        try {
            String salt = BCrypt.gensalt();
            return BCrypt.hashpw(plainPassword, salt);
        } catch (IllegalArgumentException e) {
            // Handle the exception gracefully, e.g., log it or return a default value
            System.err.println("Error hashing password: " + e.getMessage());
            return null; // or handle it as needed
        }
    }

    // Verify the password
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
