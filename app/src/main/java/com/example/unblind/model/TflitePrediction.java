package com.example.unblind.model;

/**
 * TflitePrediction is the class that holds information from the TfliteClassifier's best prediction.
 * This class determines the spoken label based on the label's certainty and the set certainty threshold.
 *
 * Authors: Lucy Pugh & Katerina Sova (team 3)
 * Last Edit: 12/10/2021
 *
 * */
public class TflitePrediction {

    private String predictionLabel;
    private float certaintyScore;
    private final static double CERTAINTY_THRESHOLD = 0.35;
    private final static String UNCERTAIN_MSG = "Unknown icon";

    /**
     * Constructor for TflitePrediction
     * @param predictionLabel: the predicted label
     * @param certaintyScore: the certainty fraction of the predicted label (0-1)
     */
    public TflitePrediction(String predictionLabel, float certaintyScore) {
        this.predictionLabel = predictionLabel;
        this.certaintyScore = certaintyScore;
    }

    /**
     * Gets the predicted label
     * @return the original predicted label
     */
    protected String getPredictedLabel() {
        return this.predictionLabel;
    }

    /**
     * Returns the label to be spoken based on the certainty
     * @return label to be spoken
     */
    public String getLabel() {
        if (this.getCertainty() < CERTAINTY_THRESHOLD) {
            return UNCERTAIN_MSG;
        }
        return this.getPredictedLabel();
    }

    /**
     * Gets the certainty of the predicted label
     * @return the label's certainty (0-1)
     */
    protected float getCertainty() {
        return this.certaintyScore;
    }
}
