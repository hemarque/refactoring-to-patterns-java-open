package team.codium.refactoring_to_patterns;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import team.codium.refactoring_to_patterns.application.SearchProperty;
import team.codium.refactoring_to_patterns.domain.InvalidPostalCodeException;
import team.codium.refactoring_to_patterns.domain.InvalidPriceException;
import team.codium.refactoring_to_patterns.domain.Property;
import team.codium.refactoring_to_patterns.domain.SearchQuery;
import team.codium.refactoring_to_patterns.infrastructure.InMemoryLogger;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SearchPropertyTest {
    private static final String PROPERTIES = "src/test/resources/testProperties.json";

    @Test
    public void find_properties_of_a_postal_code() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        Property[] properties = searchProperty.search(new SearchQuery("08030", null, null, null, null, null, null));

        assertThat(properties.length, is(1));
        assertThat(properties[0].getDescription(), is("Flat in Barcelona"));
    }

    @Test
    public void find_properties_within_a_price_range() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        Property[] properties = searchProperty.search(new SearchQuery("04600", 10_000, 100_000, null, null, null, null));

        assertThat(properties.length, is(1));
        assertThat(properties[0].getDescription(), is("Cheap flat"));
    }

    @Test
    public void find_properties_within_room_number() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        Property[] properties = searchProperty.search(new SearchQuery("04600", null, null, 1, 2, null, null));

        assertThat(properties.length, is(1));
        assertThat(properties[0].getDescription(), is("Cheap flat"));
    }

    @Test
    public void find_properties_within_a_square_meters() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        Property[] properties = searchProperty.search(new SearchQuery("04600", null, null, null, null, 80, 120));

        assertThat(properties.length, is(1));
        assertThat(properties[0].getDescription(), is("Cheap flat"));
    }

    @Test
    public void fails_when_the_postal_code_is_not_valid() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        InvalidPostalCodeException exception = Assertions.assertThrows(InvalidPostalCodeException.class, () ->
                searchProperty.search(new SearchQuery("046000", null, null, null, null, 0, 0))
        );

        assertThat(exception.getMessage(), is("046000 is not a valid postal code"));
    }

    @Test
    public void fails_when_minimum_price_is_negative() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        InvalidPriceException exception = Assertions.assertThrows(InvalidPriceException.class, () ->
                searchProperty.search(new SearchQuery("04600", -1, null, null, null, 0, 0))
        );

        assertThat(exception.getMessage(), is("Price cannot be negative"));
    }

    @Test
    public void fails_when_minimum_price_is_bigger_than_maximum_price() throws Exception {
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, null, false);

        InvalidPriceException exception = Assertions.assertThrows(InvalidPriceException.class, () ->
                searchProperty.search(new SearchQuery("04600", 100_000, 99_999, null, null, 0, 0))
        );

        assertThat(exception.getMessage(), is("The minimum price should be bigger than the maximum price"));
    }

    @Test
    public void logs_the_request_when_there_is_a_logger() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, logger, false);

        searchProperty.search(new SearchQuery("04600", 100_000, 200_000, 0, 999, null, null));

        assertThat(logger.getLoggedData().size(), is(1));
        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.get("postalCode"), is("04600"));
        assertThat(loggedData.get("minimumPrice"), is(100_000));
        assertThat(loggedData.get("maximumPrice"), is(200_000));
    }

    @Test
    public void the_logged_request_contains_the_date_when_required() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, logger, true);

        searchProperty.search(new SearchQuery("04600", 100_000, 200_000, 0, 999, null, null));

        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.containsKey("date"), is(true));
    }

    @Test
    public void the_logged_request_does_not_contain_the_date_when_not_required() throws Exception {
        InMemoryLogger logger = new InMemoryLogger();
        SearchProperty searchProperty = new SearchProperty(PROPERTIES, logger, false);

        searchProperty.search(new SearchQuery("04600", 100_000, 200_000, 0, 999, null, null));

        HashMap<String, Object> loggedData = logger.getLoggedData().get(0);
        assertThat(loggedData.containsKey("date"), is(false));
    }
}
