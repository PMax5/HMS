package models;

enum UserRole {
    DRIVER,
    SUPERVISOR,
    HEALTH_STAFF
}

public class User {
    UserRole role;
    String name;
    String username;
    String password;
}
