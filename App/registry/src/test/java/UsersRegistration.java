import models.Gender;
import models.User;
import models.UserRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import services.RegistryService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersRegistration extends RegistryBase {

    private final List<String> userIds = Arrays.asList("driver1", "driver2", "driver3");
    private String supervisorToken;
    private final RegistryService registryService = this.initService();

    @BeforeAll
    public void setupSupervisor() {
        if (registryService == null) {
            System.out.println("[Registry Tests] Failed to run: Service is null.");
            System.exit(-1);
        }

        supervisorToken = registryService.generateToken();
        User supervisor = new User();
        supervisor.setAge(25);
        supervisor.setGender(Gender.MALE);
        supervisor.setUsername("supervisor1");
        supervisor.setName("Supervisor 1");
        supervisor.setRole(UserRole.SUPERVISOR);
        supervisor.setPassword("something");

        registryService.insertUserToken(supervisor, supervisorToken);
        System.out.println("[Registry Tests] Creating supervisor account...");
    }

    @Test
    public void registerUsers() {
        User user = registryService.registerUser(
                userIds.get(0),
                userIds.get(0),
                25,
                Gender.MALE,
                UserRole.DRIVER,
                "something"
        );

        User user2 = registryService.registerUser(
                userIds.get(1),
                userIds.get(1),
                25,
                Gender.MALE,
                UserRole.DRIVER,
                "something"
        );

        assertEquals("driver1", user.getUsername());
        assertEquals(25, user.getAge());

        assertEquals("driver2", user2.getUsername());
        assertEquals(25, user2.getAge());
    }

    @Test
    public void registerUserWithWrongAge() {
        User user = registryService.registerUser(
                userIds.get(2),
                userIds.get(2),
                15,
                Gender.MALE,
                UserRole.DRIVER,
                "something"
        );

        assertNull(user);
    }

    @Test
    public void registerUserWithEmptyName() {
        User user = registryService.registerUser(
                "",
                "",
                15,
                Gender.MALE,
                UserRole.DRIVER,
                "something"
        );

        assertNull(user);
    }

    @Test
    public void registerUserWithEmptyGender() {
        User user = registryService.registerUser(
                "",
                "",
                15,
                null,
                UserRole.DRIVER,
                "something"
        );

        assertNull(user);
    }

    @Test
    public void registerUserWithEmptyRole() {
        User user = registryService.registerUser(
                "",
                "",
                15,
                Gender.MALE,
                null,
                "something"
        );

        assertNull(user);
    }

    @Test
    public void registerUserWithEmptyPassword() {
        User user = registryService.registerUser(
                "",
                "",
                15,
                Gender.MALE,
                UserRole.DRIVER,
                ""
        );

        assertNull(user);
    }

    @Test
    public void registerUserThatAlreadyExists() {
        User user = registryService.registerUser(
                userIds.get(0),
                userIds.get(0),
                25,
                Gender.MALE,
                UserRole.DRIVER,
                "something"
        );

        assertNull(user);
    }

    @Test
    public void authenticateUser() {
        String token = registryService.authenticateUser(userIds.get(0), "something");
        registryService.logoutUser(token);

        assertNotNull(token);
    }

    @Test
    public void authenticateNonexistentUser() {
        String token = registryService.authenticateUser("hello", "something");

        assertNull(token);
    }

    @Test
    public void authenticateUserWithWrongPassword() {
        String token = registryService.authenticateUser(userIds.get(0), "wrongpassword");

        assertNull(token);
    }

    @Test
    public void authorizeUserWithInvalidToken() {
        UserRole userRole = registryService.authorizeUser(registryService.generateToken());

        assertNull(userRole);
    }

    @Test
    public void authorizeUser() {
        String token = registryService.authenticateUser(userIds.get(0), "something");
        UserRole userRole = registryService.authorizeUser(token);
        registryService.logoutUser(token);

        assertEquals(userRole, UserRole.DRIVER);
    }

    @Test
    public void logoutUser() {
        String token = registryService.authenticateUser(userIds.get(0), "something");
        boolean result = registryService.logoutUser(token);

        assertTrue(result);
    }

    @Test
    public void logoutNonexistentUser() {
        boolean result = registryService.logoutUser(registryService.generateToken());

        assertFalse(result);
    }

    @Test
    public void deleteUserAsNonSupervisor() {
        boolean result = registryService.deleteUser(registryService.generateToken(), userIds.get(0));

        assertFalse(result);
    }

    @Test
    public void deleteNonexistentUser() {
        boolean result = registryService.deleteUser(supervisorToken, "hello");

        assertFalse(result);
    }

    @AfterAll
    public void removeUsers() {
        for (String userId: userIds) {
            boolean result = registryService.deleteUser(supervisorToken, userId);
            if (!result) {
                System.out.println("[Registry Tests] Failed to remove user " + userId
                        + " from Hyperledger Fabric Network.");
            }
        }
    }
}
