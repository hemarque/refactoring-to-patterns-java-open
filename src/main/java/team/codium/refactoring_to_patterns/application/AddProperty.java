package team.codium.refactoring_to_patterns.application;

import com.google.gson.Gson;
import team.codium.refactoring_to_patterns.domain.*;
import team.codium.refactoring_to_patterns.infrastructure.EmailSender;
import team.codium.refactoring_to_patterns.infrastructure.PushSender;
import team.codium.refactoring_to_patterns.infrastructure.SmsSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

public class AddProperty {
    private final String propertiesFile;
    private final String usersFile;
    private final EmailSender emailSender;
    private final String alertsFile;
    private final SmsSender smsSender;
    private final PushSender pushSender;
    private final Logger logger;
    private final boolean addDateToLogger;

    public AddProperty(String propertiesFile, String usersFile, EmailSender emailSender, String alertsFile,
                       SmsSender smsSender, PushSender pushSender, Logger logger, boolean addDateToLogger) {
        this.propertiesFile = propertiesFile;
        this.usersFile = usersFile;
        this.emailSender = emailSender;
        this.alertsFile = alertsFile;
        this.smsSender = smsSender;
        this.pushSender = pushSender;
        this.logger = logger;
        this.addDateToLogger = addDateToLogger;
    }

    private static boolean hasToSendTheAlert(Property property, Alert alert) {
        return alert.postalCode().equals(property.getPostalCode()) &&
                (alert.minimumPrice() == null || alert.minimumPrice() <= property.getPrice()) &&
                (alert.maximumPrice() == null || alert.maximumPrice() >= property.getPrice()) &&
                (alert.minimumRooms() == null || alert.minimumRooms() <= property.getNumberOfRooms()) &&
                (alert.maximumRooms() == null || alert.maximumRooms() >= property.getNumberOfRooms()) &&
                (alert.minimumSquareMeters() == null || alert.minimumSquareMeters() <= property.getSquareMeters()) &&
                (alert.maximumSquareMeters() == null || alert.maximumSquareMeters() >= property.getSquareMeters());
    }

    private static String readJSONFileContent(String file) {
        try {
            return Files.readString(Paths.get(file));
        } catch (IOException e) {
            return "[]";
        }
    }

    public void execute(int id, String description, String postalCode, int price, int numberOfRooms, int squareMeters,
                        int ownerId) throws InvalidPostalCodeException, InvalidPriceException, InvalidUserIdException {
        Property property;
        if (Pattern.matches("^\\d{5}$", postalCode)) {
            if (price >= 0) {
                String usersAsString = readJSONFileContent(usersFile);
                User[] users = new Gson().fromJson(usersAsString, User[].class);
                Optional<User> user = Arrays.stream(users).filter(u -> u.getId() == ownerId).findFirst();
                if (!user.isPresent()) {
                    throw new InvalidUserIdException("The owner " + ownerId + " does not exist");
                }
                String propertiesAsString = readJSONFileContent(propertiesFile);
                ArrayList<Property> allProperties =
                        new ArrayList<>(Arrays.asList(new Gson().fromJson(propertiesAsString, Property[].class)));
                property = new Property(id, description, postalCode, price, numberOfRooms, squareMeters, ownerId);
                allProperties.add(property);
                writePropertiesFile(allProperties);
                ArrayList<Alert> alerts =
                        new ArrayList<>(Arrays.asList(new Gson().fromJson(readJSONFileContent(alertsFile), Alert[].class)));
                for (Alert alert : alerts) {
                    if (hasToSendTheAlert(property, alert)) {
                        Optional<User> userToAlert = Arrays.stream(users).filter(u -> u.getId() == alert.userId()).findFirst();
                        if (alert.alertType().toUpperCase().equals(AlertType.EMAIL.name())) {
                            emailSender.sendEmail(new Email("noreply@codium.team", userToAlert.get().getEmail(), "There is a new property at " + property.getPostalCode(), "More information at https://properties.codium.team/" + property.getId()));
                        }
                        if (alert.alertType().toUpperCase().equals(AlertType.SMS.name())) {
                            smsSender.sendSMSAlert(new SmsMessage(userToAlert.get().getPhoneNumber(), "There is a new property at " + property.getPostalCode() + ". More information at https://properties.codium.team/" + property.getId()));
                        }
                        if (alert.alertType().toUpperCase().equals(AlertType.PUSH.name())) {
                            pushSender.sendPushNotification(new PushMessage(userToAlert.get().getPhoneNumber(), "There is a new property at " + property.getPostalCode() + ". More information at https://properties.codium.team/" + property.getId()));
                        }
                    }
                }

            } else {
                throw new InvalidPriceException("Price cannot be negative");
            }
        } else {
            throw new InvalidPostalCodeException(postalCode + " is not a valid postal code");
        }

        if (logger != null) {
            HashMap<String, Object> data = new HashMap<>() {{
                put("id", property.getId());
                put("description", property.getDescription());
                put("postalCode", property.getPostalCode());
                put("price", property.getPrice());
                put("numberOfRooms", property.getNumberOfRooms());
                put("squareMeters", property.getSquareMeters());
                put("ownerId", property.getOwnerId());
            }};
            if (addDateToLogger) {
                data.put("date", LocalDate.now());
            }
            logger.log(data);
        }

    }

    private void writePropertiesFile(ArrayList<Property> allProperties) {
        try {
            Files.writeString(Paths.get(propertiesFile), new Gson().toJson(allProperties));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
