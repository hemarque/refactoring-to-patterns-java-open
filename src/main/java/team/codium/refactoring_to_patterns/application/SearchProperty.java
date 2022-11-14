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

    public Property[] search(SearchQuery searchQuery) throws InvalidPostalCodeException, InvalidPriceException {
        Property[] properties;
        new PostalCode(searchQuery.postalCode());
        new Price(searchQuery.minimumPrice());
        PriceRange priceRange = new PriceRange(searchQuery.minimumPrice(), searchQuery.maximumPrice());
        RoomRange roomRange = new RoomRange(searchQuery.minimumRooms(), searchQuery.maximumRooms());
        SquareMetersRange squareMetersRange = new SquareMetersRange(searchQuery.minimumSquareMeters(), searchQuery.maximumSquareMeters());
        String propertiesAsString = readPropertiesFile();
        Property[] allProperties = new Gson().fromJson(propertiesAsString, Property[].class);
        properties = Arrays.stream(allProperties)
                .filter(property -> property.getPostalCode().equals(searchQuery.postalCode()))
                .filter(property -> priceRange.isInRange(property))
                .filter(property -> roomRange.isInRange(property))
                .filter(property -> squareMetersRange.isInRange(property))
                .toArray(Property[]::new);


        if (logger != null) {
            HashMap<String, Object> data = new HashMap<>() {{
                put("postalCode", searchQuery.postalCode());
                put("minimumPrice", searchQuery.minimumPrice());
                put("maximumPrice", searchQuery.maximumPrice());
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
