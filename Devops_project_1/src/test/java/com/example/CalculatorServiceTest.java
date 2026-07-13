package com.example.calculator.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorServiceTest {

    CalculatorService calculator = new CalculatorService();

    @Test
    void testAddition() {
        assertEquals(15, calculator.add(10,5));
    }

    @Test
    void testSubtraction() {
        assertEquals(5, calculator.subtract(10,5));
    }

    @Test
    void testMultiplication() {
        assertEquals(50, calculator.multiply(10,5));
    }

    @Test
    void testDivision() {
        assertEquals(2, calculator.divide(10,5));
    }

    @Test
    void testDivideByZero() {

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.divide(10,0)
        );

        assertEquals("Cannot divide by zero", exception.getMessage());

    }

}
