package models;

import com.owlike.genson.annotation.JsonProperty;

public class Config {
    private final String hyperledgerUserId;
    private final int minHealthyBPM;
    private final int maxHealthyBPM;
    private final int maxDrowsiness;
    private final int maxProblematicShifts;

    public Config(@JsonProperty("hyperledgerUserId") String hyperledgerUserId,
                  @JsonProperty("minHealthyBPM") int minHealthyBPM,
                  @JsonProperty("maxHealthyBPM") int maxHealthyBPM,
                  @JsonProperty("maxDrowsiness") int maxDrowsiness,
                  @JsonProperty("maxProblematicShifts") int maxProblematicShifts) {
        this.hyperledgerUserId = hyperledgerUserId;
        this.minHealthyBPM = minHealthyBPM;
        this.maxHealthyBPM = maxHealthyBPM;
        this.maxDrowsiness = maxDrowsiness;
        this.maxProblematicShifts = maxProblematicShifts;
    }

    public String getHyperledgerUserId() {
        return hyperledgerUserId;
    }

    public int getMinHealthyBPM() {
        return this.minHealthyBPM;
    }

    public int getMaxHealthyBPM() {
        return this.maxHealthyBPM;
    }

    public int getMaxDrowsiness() {
        return this.maxDrowsiness;
    }

    public int getMaxProblematicShifts() {
        return this.maxProblematicShifts;
    }
}
