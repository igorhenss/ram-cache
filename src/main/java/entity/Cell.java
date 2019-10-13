package entity;

import java.util.Random;

public class Cell {
    private Integer id;
    private String value;

    public Cell(Integer id) {
        this.id = id;
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

    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "| " + String.format("%04d", getId()) + " | " + getValue() + " |";
    }
}
