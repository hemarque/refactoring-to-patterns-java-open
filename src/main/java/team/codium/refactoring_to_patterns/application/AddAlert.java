package team.codium.refactoring_to_patterns.application;

import com.google.gson.Gson;
import team.codium.refactoring_to_patterns.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AddAlert {
    private final String alertsFile;
    private final String usersFile;
    private final Logger logger;
    private final boolean addDateToLogger;

    public AddAlert(String alertsFile, String usersFile, Logger logger, boolean addDateToLogger) {
        this.alertsFile = alertsFile;
        this.usersFile = usersFile;
        this.logger = logger;
        this.addDateToLogger = addDateToLogger;
    }

    private static String readJSONFileContent(String file) {
        try {
            return Files.readString(Paths.get(file));
        } catch (IOException e) {
            return "[]";
        }
    }

    private static boolean isAlertTypeValid(String alertType) {
        try {
            AlertType.valueOf(alertType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void execute(AddAlertCommand addAlertCommand) throws InvalidPostalCodeException, InvalidPriceException, InvalidUserIdException, InvalidAlertTypeException {
        new PostalCode(addAlertCommand.postalCode());
        new Price(addAlertCommand.minimumPrice());
        new PriceRange(addAlertCommand.minimumPrice(), addAlertCommand.maximumPrice());

        if (!isAlertTypeValid(addAlertCommand.alertType())) {
            throw new InvalidAlertTypeException("The alert type " + addAlertCommand.alertType() + " does not exist");
        }
        String usersAsString = readJSONFileContent(usersFile);
        User[] users = new Gson().fromJson(usersAsString, User[].class);
        boolean userExists = Arrays.stream(users).anyMatch(user -> user.getId() == addAlertCommand.userId());
        if (!userExists) {
            throw new InvalidUserIdException("The user " + addAlertCommand.userId() + " does not exist");
        }
        ArrayList<Alert> alerts = readAlerts();
        Alert alert = new Alert(addAlertCommand.userId(), addAlertCommand.alertType(), addAlertCommand.postalCode(), addAlertCommand.minimumPrice(), addAlertCommand.maximumPrice(), addAlertCommand.minimumRooms(), addAlertCommand.maximumRooms(), addAlertCommand.minimumSquareMeters(), addAlertCommand.maximumSquareMeters());
        alerts.add(alert);
        try {
            Files.writeString(Paths.get(alertsFile), new Gson().toJson(alerts));
        } catch (IOException ignored) {
        }

        if (logger != null) {
            HashMap<String, Object> data = new HashMap<>() {{
                put("userId", addAlertCommand.userId());
                put("alertType", addAlertCommand.alertType());
                put("postalCode", addAlertCommand.postalCode());
                put("minimumPrice", addAlertCommand.minimumPrice());
                put("maximumPrice", addAlertCommand.maximumPrice());
                put("minimumRooms", addAlertCommand.minimumRooms());
                put("maximumRooms", addAlertCommand.maximumRooms());
                put("minimumSquareMeters", addAlertCommand.minimumSquareMeters());
                put("maximumSquareMeters", addAlertCommand.maximumSquareMeters());
            }};
            if (addDateToLogger) {
                data.put("date", LocalDate.now());
            }
            logger.log(data);
        }
    }

    private ArrayList<Alert> readAlerts() {
        String content = readJSONFileContent(alertsFile);
        return new ArrayList<>(Arrays.asList(new Gson().fromJson(content, Alert[].class)));
    }
}
