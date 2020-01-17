package entity;

import java.util.Random;

public class Cell {
    private Integer tag;
    private String value;

    public Cell(Integer tag) {
        this.tag = tag;
        this.value = generateRandomValue();
    }

    private String generateRandomValue() {
        return generateRandomInteger().toString()
                + generateRandomCharacter()
                + generateRandomInteger().toString()
                + generateRandomCharacter();
    }

    private Integer generateRandomInteger() {
        Random random = new Random();
        return random.nextInt(9);
    }

    private Character generateRandomCharacter() {
        Random random = new Random();
        return (char) (random.nextInt(26) + 'A');
    }

    public boolean tagFound(Integer tagToFind) {
        return tag.equals(tagToFind);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "| " + String.format("%04d", getTag()) + " | " + getValue() + " |";
    }
}
