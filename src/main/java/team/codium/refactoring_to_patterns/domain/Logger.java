package team.codium.refactoring_to_patterns.domain;

import java.util.ArrayList;
import java.util.HashMap;

public interface Logger {
    void log(HashMap<String, Object> data);

    ArrayList<HashMap<String, Object>> getLoggedData();
}
