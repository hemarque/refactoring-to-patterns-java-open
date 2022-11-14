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
import java.util.regex.Pattern;

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

    public void execute(Integer userId, String alertType, String postalCode, Integer minimumPrice, Integer maximumPrice,
                        Integer minimumRooms, Integer maximumRooms, Integer minimumSquareMeters,
                        Integer maximumSquareMeters) throws InvalidPostalCodeException, InvalidPriceException, InvalidUserIdException, InvalidAlertTypeException {
        if (Pattern.matches("^\\d{5}$", postalCode)) {
            if (minimumPrice == null || minimumPrice >= 0) {
                if (minimumPrice == null || maximumPrice == null || minimumPrice <= maximumPrice) {
                    if (!isAlertTypeValid(alertType)) {
                        throw new InvalidAlertTypeException("The alert type " + alertType + " does not exist");
                    }
                    String usersAsString = readJSONFileContent(usersFile);
                    User[] users = new Gson().fromJson(usersAsString, User[].class);
                    boolean userExists = Arrays.stream(users).anyMatch(user -> user.getId() == userId);
                    if (!userExists) {
                        throw new InvalidUserIdException("The user " + userId + " does not exist");
                    }
                    ArrayList<Alert> alerts = readAlerts();
                    Alert alert = new Alert(userId, alertType, postalCode, minimumPrice, maximumPrice, minimumRooms, maximumRooms, minimumSquareMeters, maximumSquareMeters);
                    alerts.add(alert);
                    try {
                        Files.writeString(Paths.get(alertsFile), new Gson().toJson(alerts));
                    } catch (IOException e) {
                    }

                    if (logger != null) {
                        HashMap<String, Object> data = new HashMap<>() {{
                            put("userId", userId);
                            put("alertType", alertType);
                            put("postalCode", postalCode);
                            put("minimumPrice", minimumPrice);
                            put("maximumPrice", maximumPrice);
                            put("minimumRooms", minimumRooms);
                            put("maximumRooms", maximumRooms);
                            put("minimumSquareMeters", minimumSquareMeters);
                            put("maximumSquareMeters", maximumSquareMeters);
                        }};
                        if (addDateToLogger) {
                            data.put("date", LocalDate.now());
                        }
                        logger.log(data);
                    }


                } else {
                    throw new InvalidPriceException("The minimum price should be bigger than the maximum price");
                }
            } else {
                throw new InvalidPriceException("Price cannot be negative");
            }
        } else {
            throw new InvalidPostalCodeException(postalCode + " is not a valid postal code");

        }

    }

    private ArrayList<Alert> readAlerts() {
        String content = readJSONFileContent(alertsFile);
        return new ArrayList<>(Arrays.asList(new Gson().fromJson(content, Alert[].class)));
    }
}
