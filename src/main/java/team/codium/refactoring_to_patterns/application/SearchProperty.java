package team.codium.refactoring_to_patterns.application;

import com.google.gson.Gson;
import team.codium.refactoring_to_patterns.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;

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
        new PostalCode(postalCode);
        new Price(minimumPrice);
        PriceRange priceRange = new PriceRange(minimumPrice, maximumPrice);
        RoomRange roomRange = new RoomRange(minimumRooms, maximumRooms);

        String propertiesAsString = readPropertiesFile();
        Property[] allProperties = new Gson().fromJson(propertiesAsString, Property[].class);
        properties = Arrays.stream(allProperties)
                .filter(property -> property.getPostalCode().equals(postalCode))
                .filter(property -> priceRange.isInRange(property))
                .filter(property -> roomRange.isInRange(property))
                .filter(property -> (minimumSquareMeters == null || property.getSquareMeters() >= minimumSquareMeters) &&
                        (maximumSquareMeters == null || property.getSquareMeters() <= maximumSquareMeters))
                .toArray(Property[]::new);


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
