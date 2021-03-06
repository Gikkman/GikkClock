package com.gikk.clock.types;

import javafx.util.StringConverter;

/**Cudos to fabian at Stackoverflow: https://stackoverflow.com/a/35313351/1943559
 *
 */
public class IntRangeStringConverter extends StringConverter<Integer> {
    private final int min;
    private final int max;

    public IntRangeStringConverter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString(Integer object) {
        return String.format("%02d", object);
    }

    @Override
    public Integer fromString(String string) {
        int integer = Integer.parseInt(string);
        if (integer > max || integer < min) {
            throw new IllegalArgumentException();
        }
        return integer;
    }
}
