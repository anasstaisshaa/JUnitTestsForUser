package edu.AnastasiiaTkachuk.junit.service;

import edu.AnastasiiaTkachuk.junit.dto.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {
    private UserService userService;
    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    @BeforeAll
    void init(){
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(){
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded(){
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertThat(users).isEmpty();
    }
    @Test
    void usersSizeIfUserAdded(){
        System.out.println("Test 2: " + this);
        userService.add(IVAN, PETR);

        List<User> users = userService.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("login")
    void loginSuccessIfUserExists(){
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
        assertThat(maybeUser).isPresent();
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
    }
    @Test
    @Tag("login")
    void throwExceptionIfUsernameOrPasswordIsNull(){
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> userService.login(null, "1234")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> userService.login("Ivan", null))
        );
    }

    @Test
    void usersConvertedToMapById(){
        userService.add(IVAN, PETR);
        Map<Integer, User> users = userService.getAllConvertedById();
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }
    @Test
    @Tag("login")
    void loginFailIfPasswordIsNotCorrect(){
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), "jdfjkfk");
        assertThat(maybeUser).isEmpty();
    }
    @Test
    @Tag("login")
    void loginFailIfUserDoesNotExist(){
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login("Anastasiia", IVAN.getPassword());
        assertThat(maybeUser).isEmpty();
    }

    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool(){
        System.out.println("After all: " + this);
    }
}
