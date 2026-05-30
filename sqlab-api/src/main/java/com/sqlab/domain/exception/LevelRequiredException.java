package com.sqlab.domain.exception;

public class LevelRequiredException extends RuntimeException {
    private final int requiredLevel;
    private final int currentLevel;

    public LevelRequiredException(int requiredLevel, int currentLevel) {
        super("Level " + requiredLevel + " required (current: " + currentLevel + ")");
        this.requiredLevel = requiredLevel;
        this.currentLevel = currentLevel;
    }

    public int getRequiredLevel() { return requiredLevel; }
    public int getCurrentLevel() { return currentLevel; }
}
