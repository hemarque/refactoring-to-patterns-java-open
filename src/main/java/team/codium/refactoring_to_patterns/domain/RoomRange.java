package team.codium.refactoring_to_patterns.domain;

public class RoomRange {
    private Integer minimumRooms;
    private Integer maximumRooms;
    public RoomRange(Integer minimumRooms, Integer maximumRooms) {
        this.minimumRooms = minimumRooms;
        this.maximumRooms = maximumRooms;
    }

    public boolean isInRange(Property property) {
        return (minimumRooms == null || property.getNumberOfRooms() >= minimumRooms) &&
                (maximumRooms == null || property.getNumberOfRooms() <= maximumRooms);
    }
}
