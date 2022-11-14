package team.codium.refactoring_to_patterns.domain;

public record SearchQuery(String postalCode, Integer minimumPrice, Integer maximumPrice, Integer minimumRooms,
                          Integer maximumRooms, Integer minimumSquareMeters, Integer maximumSquareMeters) {
}