package edu.AnastasiiaTkachuk.junit.service;

import edu.AnastasiiaTkachuk.junit.dto.User;
import edu.AnastasiiaTkachuk.junit.paramresolver.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {
    private UserService userService;
    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    UserServiceTest(TestInfo testInfo){
        System.out.println();
    }
    @BeforeAll
    void init(){
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(UserService userService){
        System.out.println("Before each: " + this);
        this.userService = userService;
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
    void usersConvertedToMapById(){
        userService.add(IVAN, PETR);
        Map<Integer, User> users = userService.getAllConvertedById();
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @ParameterizedTest
//    @ArgumentsSource()
//    @NullSource
//    @EmptySource
//    @ValueSource(strings = {
//            "Ivan", "Petr"
//    })
//    @EnumSource
//    @NullAndEmptySource
    @MethodSource("getArgumentsForLoginTest")
//    @CsvFileSource(resources = "/login-test-data.csv", numLinesToSkip = 1)
//    @CsvSource({
//            "Ivan", "123",
//            "Petr", "111"
//    })
    void loginParametrizedTest(String username, String password, Optional<User> user){
        userService.add(IVAN, PETR);
        Optional<User> maybeUser = userService.login(username, password);
        assertThat(maybeUser).isEqualTo(user);
    }
    static Stream<Arguments> getArgumentsForLoginTest(){
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Ivan", "sfdfsd", Optional.empty()),
                Arguments.of("sdsf", "123", Optional.empty())
        );
    }
    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool(){
        System.out.println("After all: " + this);
    }

    @Nested
    @Tag("login")
    class LoginTest {
        @Test
        void loginFailIfPasswordIsNotCorrect(){
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "jdfjkfk");
            assertThat(maybeUser).isEmpty();
        }
        @Test
        void loginFailIfUserDoesNotExist(){
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login("Anastasiia", IVAN.getPassword());
            assertThat(maybeUser).isEmpty();
        }
        @Test
        void loginSuccessIfUserExists(){
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
        }
        @Test
        void throwExceptionIfUsernameOrPasswordIsNull(){
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> userService.login(null, "1234")),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> userService.login("Ivan", null))
            );
        }
    }
}
