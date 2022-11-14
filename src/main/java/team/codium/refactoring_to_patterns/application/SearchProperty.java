package team.codium.refactoring_to_patterns.application;

import com.google.gson.Gson;
import team.codium.refactoring_to_patterns.domain.InvalidPostalCodeException;
import team.codium.refactoring_to_patterns.domain.InvalidPriceException;
import team.codium.refactoring_to_patterns.domain.Logger;
import team.codium.refactoring_to_patterns.domain.Property;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

final public class SearchProperty {
    private final String propertiesFile;
    private final Logger logger;
    private final boolean addDateToLogger;

    public SearchProperty(String propertiesFile, Logger logger, boolean addDateToLogger) {
        this.propertiesFile = propertiesFile;
        this.logger = logger;
        this.addDateToLogger = addDateToLogger;
    }

    public Property[] search(String postalCode, Integer minimumPrice, Integer maximumPrice, Integer minimumRooms, Integer maximumRooms, Integer minimumSquareMeters, Integer maximumSquareMeters) throws InvalidPostalCodeException, InvalidPriceException {
        Property[] properties;
        if (Pattern.matches("^\\d{5}$", postalCode)) {
            if (minimumPrice == null || minimumPrice >= 0) {
                if (minimumPrice == null || maximumPrice == null || minimumPrice <= maximumPrice) {

                    String propertiesAsString = readPropertiesFile();
                    Property[] allProperties = new Gson().fromJson(propertiesAsString, Property[].class);
                    properties = Arrays.stream(allProperties)
                            .filter(property -> property.getPostalCode().equals(postalCode))
                            .filter(property -> (minimumPrice == null || property.getPrice() >= minimumPrice) &&
                                    (maximumPrice == null || property.getPrice() <= maximumPrice))
                            .filter(property -> (minimumRooms == null || property.getNumberOfRooms() >= minimumRooms) &&
                                    (maximumRooms == null || property.getNumberOfRooms() <= maximumRooms))
                            .filter(property -> (minimumSquareMeters == null || property.getSquareMeters() >= minimumSquareMeters) &&
                                    (maximumSquareMeters == null || property.getSquareMeters() <= maximumSquareMeters))
                            .toArray(Property[]::new);
                } else {
                    throw new InvalidPriceException("The minimum price should be bigger than the maximum price");
                }
            } else {
                throw new InvalidPriceException("Price cannot be negative");
            }
        } else {
            throw new InvalidPostalCodeException(postalCode + " is not a valid postal code");
        }
        if (logger != null) {
            HashMap<String, Object> data = new HashMap<>() {{
                put("postalCode", postalCode);
                put("minimumPrice", minimumPrice);
                put("maximumPrice", maximumPrice);
            }};
            if (addDateToLogger) {
                data.put("date", LocalDate.now());
            }
            logger.log(data);
        }
        return properties;
    }

    private String readPropertiesFile() {
        try {
            return Files.readString(Paths.get(propertiesFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}