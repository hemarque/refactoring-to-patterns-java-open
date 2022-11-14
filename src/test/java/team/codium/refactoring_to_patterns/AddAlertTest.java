package team.codium.refactoring_to_patterns;

import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import team.codium.refactoring_to_patterns.application.AddAlert;
import team.codium.refactoring_to_patterns.domain.*;
import team.codium.refactoring_to_patterns.infrastructure.InMemoryLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AddAlertTest {
    private static final String ALERTS_FILE = "src/test/resources/tmpTestAlerts.json";
    public static final int NON_EXISTING_USER = 99999999;
    private static final String USERS_FILE = "src/test/resources/testUsers.json";

    @Test
    public void can_add_an_alert_with_all_the_searchable_fields() throws Exception {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        addAlert.execute(1, "email", "08030", 0, 100_000, 0, 3, 30, 200);

        String content = Files.readString(Paths.get((ALERTS_FILE)));
        ArrayList<Alert> alerts = new ArrayList<>(Arrays.asList(new Gson().fromJson(content, Alert[].class)));
        assertThat(alerts.size(), is(1));
        Alert alert = alerts.get(0);
        assertThat(alert.userId(), is(1));
        assertThat(alert.alertType(), is("email"));
        assertThat(alert.postalCode(), is("08030"));
        assertThat(alert.minimumPrice(), is(0));
        assertThat(alert.maximumPrice(), is(100_000));
        assertThat(alert.minimumRooms(), is(0));
        assertThat(alert.maximumRooms(), is(3));
        assertThat(alert.minimumSquareMeters(), is(30));
        assertThat(alert.maximumSquareMeters(), is(200));
    }

    @Test
    public void can_add_an_alert_only_with_postal_code() throws Exception {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        addAlert.execute(1, "email", "08030", null, null, null, null, null, null);

        String content = Files.readString(Paths.get(ALERTS_FILE));
        ArrayList<Alert> alerts = new ArrayList<>(Arrays.asList(new Gson().fromJson(content, Alert[].class)));
        assertThat(alerts.size(), is(1));
        Alert alert = alerts.get(0);
        assertThat(alert.minimumPrice(), is(nullValue()));
        assertThat(alert.maximumPrice(), is(nullValue()));
        assertThat(alert.minimumRooms(), is(nullValue()));
        assertThat(alert.maximumRooms(), is(nullValue()));
        assertThat(alert.minimumSquareMeters(), is(nullValue()));
        assertThat(alert.maximumSquareMeters(), is(nullValue()));
    }

    @Test
    public void can_store_more_than_one_alert() throws Exception {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        addAlert.execute(1, "email", "08030", null, null, null, null, null, null);
        addAlert.execute(1, "email", "08030", null, null, null, null, null, null);

        String content = Files.readString(Paths.get(ALERTS_FILE));
        ArrayList<Alert> alerts = new ArrayList<>(Arrays.asList(new Gson().fromJson(content, Alert[].class)));
        assertThat(alerts.size(), is(2));
    }

    @Test
    public void fails_when_the_postal_code_is_not_valid() throws Exception {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        InvalidPostalCodeException exception = Assertions.assertThrows(InvalidPostalCodeException.class, () ->
                addAlert.execute(1, "email", "080300", null, null, null, null, null, null)
        );

        assertThat(exception.getMessage(), Matchers.is("080300 is not a valid postal code"));
    }

    @Test
    public void fails_when_minimum_price_is_negative() {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        InvalidPriceException exception = Assertions.assertThrows(InvalidPriceException.class, () ->
                addAlert.execute(1, "email", "08030", -1, null, null, null, null, null)
        );

        assertThat(exception.getMessage(), Matchers.is("Price cannot be negative"));
    }

    @Test
    public void fails_when_minimum_price_is_bigger_than_maximum_price() {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        InvalidPriceException exception = Assertions.assertThrows(InvalidPriceException.class, () ->
                addAlert.execute(1, "email", "08030", 100_001, 100_000, null, null, null, null)
        );

        assertThat(exception.getMessage(), Matchers.is("The minimum price should be bigger than the maximum price"));
    }

    @Test
    public void fails_when_the_user_does_not_exist() {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        InvalidUserIdException exception = Assertions.assertThrows(InvalidUserIdException.class, () ->
                addAlert.execute(NON_EXISTING_USER, "email", "08030", null, null, null, null, null, null)
        );

        assertThat(exception.getMessage(), Matchers.is("The user " + NON_EXISTING_USER + " does not exist"));
    }

    @Test
    public void fails_when_the_notification_type_is_not_valid() {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        InvalidAlertTypeException exception = Assertions.assertThrows(InvalidAlertTypeException.class, () ->
                addAlert.execute(1, "asdf", "08030", null, null, null, null, null, null)
        );

        assertThat(exception.getMessage(), Matchers.is("The alert type asdf does not exist"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "sms", "push"})
    public void succeed_with_any_alert_type(String alertType) throws Exception {
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, null, false);

        addAlert.execute(1, alertType, "08030", null, null, null, null, null, null);
    }

    @Test
    public void logs_the_request() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, logger, false);

        addAlert.execute(1, "email", "08030", 0, 100_000, 0, 3, 30, 200);

        assertThat(logger.getLoggedData().size(), Matchers.is(1));
        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.get("userId"), Matchers.is(1));
        assertThat(loggedData.get("alertType"), Matchers.is("email"));
        assertThat(loggedData.get("postalCode"), Matchers.is("08030"));
        assertThat(loggedData.get("minimumPrice"), Matchers.is(0));
        assertThat(loggedData.get("maximumPrice"), Matchers.is(100_000));
        assertThat(loggedData.get("minimumRooms"), Matchers.is(0));
        assertThat(loggedData.get("maximumRooms"), Matchers.is(3));
        assertThat(loggedData.get("minimumSquareMeters"), Matchers.is(30));
        assertThat(loggedData.get("maximumSquareMeters"), Matchers.is(200));
    }

    @Test
    public void the_logged_request_contains_the_date_when_required() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, logger, true);

        addAlert.execute(1, "email", "08030", 0, 100_000, 0, 3, 30, 200);

        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.containsKey("date"), Matchers.is(true));
    }

    @Test
    public void the_logged_request_does_not_contain_the_date_when_not_required() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        AddAlert addAlert = new AddAlert(ALERTS_FILE, USERS_FILE, logger, false);

        addAlert.execute(1, "email", "08030", 0, 100_000, 0, 3, 30, 200);

        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.containsKey("date"), Matchers.is(false));
    }


    @AfterEach
    public void tearDown() {
        try {
            Files.delete(Paths.get(ALERTS_FILE));
        } catch (IOException ignored) {
        }
    }
}
