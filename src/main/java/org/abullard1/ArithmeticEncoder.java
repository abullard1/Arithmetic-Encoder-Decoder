package org.abullard1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

/**
 * ArithmeticEncoder class for encoding a message using arithmetic encoding.
 */
public class ArithmeticEncoder {
    /**
     * Method to encode the given message, specifying a custom precision scale.
     *
     * @param message        The message to encode
     * @param precisionScale How many digits of precision to use when calculating probabilities and final encoded value
     * @return The arithmetic-encoded BigDecimal value
     */
    public static BigDecimal encodeMessage(String message, int precisionScale) {
        if (precisionScale < 1) {
            throw new IllegalArgumentException("Precision scale must be greater than 0");
        }

        // 1. Fills a TreeMap with character counts from the message
        TreeMap<Character, Integer> characterCountsTreemap = fillCharacterCountsTreemap(message);

        // 2. Converts the counts to probabilities in a TreeMap using the given precision scale
        TreeMap<Character, BigDecimal> characterProbabilitiesTreemap = fillCharacterProbabilitiesTreemap(
                characterCountsTreemap,
                message.length(),
                precisionScale
        );

        // 3. Builds intervals from the probabilities
        TreeMap<Character, TupleValue> characterIntervalsTreemap = fillCharacterIntervalsTreemap(characterProbabilitiesTreemap);

        // 4. Narrows down the interval recursively to get the final encoding value
        BigDecimal arithmeticEncodingValue = calculateArithmeticEncodingValue(
                message,
                characterIntervalsTreemap,
                0,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                precisionScale
        );

        return arithmeticEncodingValue;
    }

    /**
     * Method to fill a TreeMap with character counts for each distinct character in the message
     *
     * @param message The string to parse
     * @return A TreeMap from characters to their frequency counts
     */
    private static TreeMap<Character, Integer> fillCharacterCountsTreemap(String message) {
        TreeMap<Character, Integer> characterCountsTreemap = new TreeMap<>();
        for (Character character : message.toCharArray()) {
            characterCountsTreemap.put(character, characterCountsTreemap.getOrDefault(character, 0) + 1);
        }
        return characterCountsTreemap;
    }

    /**
     * Converts the character counts to probabilities with a specified precision scale.
     *
     * @param characterCountsTreemap The frequency counts of each character
     * @param messageLength The total length of the message
     * @param precisionScale How many digits of precision to use
     * @return A TreeMap mapping each character to its probability
     */
    private static TreeMap<Character, BigDecimal> fillCharacterProbabilitiesTreemap(
            TreeMap<Character, Integer> characterCountsTreemap,
            int messageLength,
            int precisionScale
    ) {
        TreeMap<Character, BigDecimal> characterProbabilitiesTreemap = new TreeMap<>();
        BigDecimal lengthAsBigDecimal = BigDecimal.valueOf(messageLength);

        for (Map.Entry<Character, Integer> entry : characterCountsTreemap.entrySet()) {
            // Probability = count / total length
            BigDecimal probability = BigDecimal.valueOf(entry.getValue())
                    .divide(lengthAsBigDecimal, precisionScale, RoundingMode.HALF_UP);
            characterProbabilitiesTreemap.put(entry.getKey(), probability);
        }

        return characterProbabilitiesTreemap;
    }

    /**
     * Builds a TreeMap of intervals (lower and upper bounds) from the probabilities
     *
     * @param probabilitiesTreeMap A TreeMap of character probabilities
     * @return A TreeMap mapping characters to a TupleValue containing lowerBound and upperBound
     */
    private static TreeMap<Character, TupleValue> fillCharacterIntervalsTreemap(TreeMap<Character, BigDecimal> probabilitiesTreeMap) {
        TreeMap<Character, TupleValue> characterIntervalsTreemap = new TreeMap<>();
        BigDecimal lowerBound = BigDecimal.ZERO;
        BigDecimal upperBound;

        for (Map.Entry<Character, BigDecimal> entry : probabilitiesTreeMap.entrySet()) {
            char currentChar = entry.getKey();
            BigDecimal probability = entry.getValue();

            upperBound = lowerBound.add(probability);
            characterIntervalsTreemap.put(currentChar, new TupleValue(lowerBound, upperBound));
            lowerBound = upperBound;
        }

        return characterIntervalsTreemap;
    }

    /**
     * Recursive method to calculate the final arithmetic encoding value
     *
     * @param messageToEncode The full message to encode
     * @param characterIntervalsTreemap Map of intervals for each character
     * @param currentIndex The current character index in the message
     * @param lowerBound Current lower bound
     * @param upperBound Current upper bound
     * @param precisionScale User-specified number of digits for final midpoint
     * @return The final arithmetic encoding value as a BigDecimal
     */
    private static BigDecimal calculateArithmeticEncodingValue(
            String messageToEncode,
            TreeMap<Character, TupleValue> characterIntervalsTreemap,
            int currentIndex,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            int precisionScale
    ) {
        // Stops the recursion when the end of the message is reached
        if (currentIndex == messageToEncode.length()) {
            return (lowerBound.add(upperBound))
                    .divide(BigDecimal.valueOf(2), precisionScale, RoundingMode.HALF_UP);
        }

        // Identifies the current character
        char currentChar = messageToEncode.charAt(currentIndex);
        TupleValue currentInterval = characterIntervalsTreemap.get(currentChar);

        BigDecimal range = upperBound.subtract(lowerBound);

        // New calculated bounds
        BigDecimal newLowerBound = lowerBound.add(range.multiply(currentInterval.v1));
        BigDecimal newUpperBound = lowerBound.add(range.multiply(currentInterval.v2));

        // Recursively executes itself and encodes the message until the end of the message is reached
        return calculateArithmeticEncodingValue(
                messageToEncode,
                characterIntervalsTreemap,
                currentIndex + 1,
                newLowerBound,
                newUpperBound,
                precisionScale
        );
    }
}