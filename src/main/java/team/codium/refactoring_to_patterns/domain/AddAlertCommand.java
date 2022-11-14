package team.codium.refactoring_to_patterns.domain;

public record AddAlertCommand(Integer userId, String alertType, String postalCode, Integer minimumPrice,
                              Integer maximumPrice, Integer minimumRooms, Integer maximumRooms,
                              Integer minimumSquareMeters, Integer maximumSquareMeters) {
}