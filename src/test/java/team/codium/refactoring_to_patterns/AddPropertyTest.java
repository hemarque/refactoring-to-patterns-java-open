package team.codium.refactoring_to_patterns;

import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import team.codium.refactoring_to_patterns.application.AddProperty;
import team.codium.refactoring_to_patterns.domain.*;
import team.codium.refactoring_to_patterns.infrastructure.EmailSender;
import team.codium.refactoring_to_patterns.infrastructure.InMemoryLogger;
import team.codium.refactoring_to_patterns.infrastructure.PushSender;
import team.codium.refactoring_to_patterns.infrastructure.SmsSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AddPropertyTest {

    private static final String PROPERTIES = "src/test/resources/tmpAddPropertyProperties.json";
    private static final String ALERTS = "src/test/resources/tmpTestAlerts.json";
    public static final int NON_EXISTING_OWNER = 999999;
    private static final String USERS_FILE = "src/test/resources/testUsers.json";

    @Test
    public void new_valid_property_can_be_retrieved() throws Exception {
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), null, false);

        addProperty.execute(123, "New property", "04600", 140_000, 3, 160, 1);

        String propertiesAsString = Files.readString(Paths.get(PROPERTIES));
        Property[] allProperties = new Gson().fromJson(propertiesAsString, Property[].class);
        assertThat(allProperties.length, is(1));
        Property property = allProperties[0];
        assertThat(property.getId(), is(123));
        assertThat(property.getDescription(), is("New property"));
        assertThat(property.getPostalCode(), is("04600"));
        assertThat(property.getPrice(), is(140_000));
        assertThat(property.getNumberOfRooms(), is(3));
        assertThat(property.getSquareMeters(), is(160));
        assertThat(property.getOwnerId(), is(1));
    }

    @Test
    public void can_store_more_than_one_property() throws Exception {
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), null, false);

        addProperty.execute(1, "New property", "04600", 140_000, 3, 160, 1);
        addProperty.execute(2, "New property", "04600", 140_000, 3, 160, 1);

        String propertiesAsString = Files.readString(Paths.get(PROPERTIES));
        Property[] allProperties = new Gson().fromJson(propertiesAsString, Property[].class);
        assertThat(allProperties.length, is(2));
    }

    @Test
    public void fails_when_the_postal_code_is_not_valid() throws Exception {
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), null, false);

        InvalidPostalCodeException exception = Assertions.assertThrows(InvalidPostalCodeException.class, () ->
                addProperty.execute(1, "New property", "046000", 140_000, 3, 160, 1)
        );

        assertThat(exception.getMessage(), Matchers.is("046000 is not a valid postal code"));
    }

    @Test
    public void fails_when_minimum_price_is_negative() throws Exception {
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), null, false);

        InvalidPriceException exception = Assertions.assertThrows(InvalidPriceException.class, () ->
                addProperty.execute(1, "New property", "04600", -1, 3, 160, 1)
        );

        assertThat(exception.getMessage(), Matchers.is("Price cannot be negative"));
    }

    @Test
    public void fails_when_owner_does_not_exist() throws Exception {
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), null, false);

        InvalidUserIdException exception = Assertions.assertThrows(InvalidUserIdException.class, () ->
                addProperty.execute(1, "New property", "04600", 100_000, 3, 160, NON_EXISTING_OWNER)
        );

        assertThat(exception.getMessage(), Matchers.is("The owner " + NON_EXISTING_OWNER + " does not exist"));
    }

    @Test
    public void send_alert_by_email_when_email_alert() throws Exception {
        Alert alert = new Alert(2, "email", "04600", null, null, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(alert)));
        EmailSender emailSender = mock(EmailSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, mock(SmsSender.class), mock(PushSender.class), null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verify(emailSender).sendEmail(new Email("noreply@codium.team", "rDeckard@email.com", "There is a new property at 04600", "More information at https://properties.codium.team/1"));
    }

    @Test
    public void send_alert_by_sms_when_sms_alert() throws Exception {
        Alert alert = new Alert(2, "sms", "04600", null, null, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(alert)));
        SmsSender smsSender = mock(SmsSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, smsSender, mock(PushSender.class), null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verify(smsSender).sendSMSAlert(new SmsMessage("673777555", "There is a new property at 04600. More information at https://properties.codium.team/1"));
    }

    @Test
    public void send_alert_by_push_when_push_alert() throws Exception {
        Alert alert = new Alert(2, "push", "04600", null, null, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(alert)));
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verify(pushSender).sendPushNotification(new PushMessage("673777555", "There is a new property at 04600. More information at https://properties.codium.team/1"));
    }

    @Test
    public void sends_the_right_amount_of_alerts() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, null, null, null, null, null);
        Alert smsAlert = new Alert(2, "sms", "04600", null, null, null, null, null, null);
        Alert pushAlert = new Alert(2, "push", "04600", null, null, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verify(emailSender, times(1)).sendEmail(any());
        verify(smsSender, times(1)).sendSMSAlert(any());
        verify(pushSender, times(1)).sendPushNotification(any());
    }

    @Test
    public void do_not_send_alerts_in_the_other_postal_code() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, null, null, null, null, null);
        Alert smsAlert = new Alert(2, "sms", "04600", null, null, null, null, null, null);
        Alert pushAlert = new Alert(2, "push", "04600", null, null, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "00001", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void do_not_send_alerts_when_property_price_is_lower_than_min_price() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", 100_001, null, null, null, null, null);
        Alert smsAlert = new Alert(2, "sms", "04600", 100_001, null, null, null, null, null);
        Alert pushAlert = new Alert(2, "push", "04600", 100_001, null, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void do_not_send_alerts_when_price_is_over_max_price() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, 99_999, null, null, null, null);
        Alert smsAlert = new Alert(2, "sms", "04600", null, 99_999, null, null, null, null);
        Alert pushAlert = new Alert(2, "push", "04600", null, 99_999, null, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void do_not_send_alerts_when_property_rooms_is_lower_than_alert_minimum_rooms() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, null, 4, null, null, null);
        Alert smsAlert = new Alert(2, "sms", "04600", null, null, 4, null, null, null);
        Alert pushAlert = new Alert(2, "push", "04600", null, null, 4, null, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void do_not_send_alerts_when_property_rooms_is_over_maximum_rooms() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, null, null, 2, null, null);
        Alert smsAlert = new Alert(2, "sms", "04600", null, null, null, 2, null, null);
        Alert pushAlert = new Alert(2, "push", "04600", null, null, null, 2, null, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void do_not_send_alerts_when_property_meters_is_lower_minimum_meters() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, null, null, null, 161, null);
        Alert smsAlert = new Alert(2, "sms", "04600", null, null, null, null, 161, null);
        Alert pushAlert = new Alert(2, "push", "04600", null, null, null, null, 161, null);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void do_not_send_alerts_when_property_meters_is_over_maximum_meters() throws Exception {
        Alert emailAlert = new Alert(2, "email", "04600", null, null, null, null, null, 159);
        Alert smsAlert = new Alert(2, "sms", "04600", null, null, null, null, null, 159);
        Alert pushAlert = new Alert(2, "push", "04600", null, null, null, null, null, 159);
        Files.writeString(Paths.get(ALERTS), new Gson().toJson(List.of(emailAlert, smsAlert, pushAlert)));
        EmailSender emailSender = mock(EmailSender.class);
        SmsSender smsSender = mock(SmsSender.class);
        PushSender pushSender = mock(PushSender.class);
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, emailSender, ALERTS, smsSender, pushSender, null, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        verifyNoInteractions(emailSender);
        verifyNoInteractions(smsSender);
        verifyNoInteractions(pushSender);
    }

    @Test
    public void logs_the_request_when_there_is_a_logger() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), logger, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        assertThat(logger.getLoggedData().size(), is(1));
        HashMap<String, Object> log = logger.getLoggedData().get(0);
        assertThat(log.get("id"), is(1));
        assertThat(log.get("description"), is("New property"));
        assertThat(log.get("postalCode"), is("04600"));
        assertThat(log.get("price"), is(100_000));
        assertThat(log.get("numberOfRooms"), is(3));
        assertThat(log.get("squareMeters"), is(160));
        assertThat(log.get("ownerId"), is(2));
    }

    @Test
    public void the_logged_request_contains_the_date_when_required() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), logger, true);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.containsKey("date"), Matchers.is(true));
    }

    @Test
    public void the_logged_request_does_not_contain_the_date_when_not_required() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        AddProperty addProperty = new AddProperty(PROPERTIES, USERS_FILE, mock(EmailSender.class), ALERTS, mock(SmsSender.class), mock(PushSender.class), logger, false);

        addProperty.execute(1, "New property", "04600", 100_000, 3, 160, 2);

        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.containsKey("date"), Matchers.is(false));
    }

    @AfterEach
    public void tearDown() {
        try {
            Files.delete(Paths.get(PROPERTIES));
            Files.delete(Paths.get(ALERTS));
        } catch (IOException ignored) {
        }
    }
}
