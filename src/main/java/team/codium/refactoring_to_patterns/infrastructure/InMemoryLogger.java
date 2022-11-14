package team.codium.refactoring_to_patterns.infrastructure;

import team.codium.refactoring_to_patterns.domain.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryLogger implements Logger {
    private final ArrayList<HashMap<String, Object>> loggedData = new ArrayList<>();

    @Override
    public void log(HashMap<String, Object> data) {
        loggedData.add(data);
    }

    @Override
    public ArrayList<HashMap<String, Object>> getLoggedData() {
        return loggedData;
    }
}
