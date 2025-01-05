package org.abullard1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

/**
 * ArithmeticDecoder class for decoding a message using arithmetic decoding.
 */
public class ArithmeticDecoder {
    /**
     * Decodes an arithmetic-encoded value into a message, with optional stop word and precision.
     *
     * @param decodingValue  the arithmetic-encoded BigDecimal
     * @param charProbMap    a TreeMap of characters to their probabilities
     * @param stopWord       an optional string that ends decoding once encountered
     * @param precisionScale the integer precision/scale for dividing the decodingValue
     * @return               the decoded message as a String
     */
    public static String decodeMessage(BigDecimal decodingValue,
                                       TreeMap<Character, BigDecimal> charProbMap,
                                       String stopWord,
                                       int precisionScale) {
        // 1. Builds intervals from the probability map
        TreeMap<Character, TupleValue> intervals = fillCharacterIntervalsTreemap(charProbMap);

        // 2. Recursively decodes the encoded message until the stopWord is found or maxIterations is reached
        return decodeArithmeticallyEncodedMessageRecursive(
                decodingValue,
                intervals,
                0,
                Integer.parseInt(ConfigLoader.getProperty("decode.max.iterations")),
                new StringBuilder(),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                stopWord,
                precisionScale
        );
    }

    /**
     * Builds the character intervals (lower and upper bounds) from probabilities.
     */
    private static TreeMap<Character, TupleValue> fillCharacterIntervalsTreemap(TreeMap<Character, BigDecimal> probabilitiesHashmap) {
        TreeMap<Character, TupleValue> characterIntervalsTreemap = new TreeMap<>();
        BigDecimal lowerBound = BigDecimal.ZERO;
        BigDecimal upperBound = BigDecimal.ZERO;

        for(Map.Entry<Character, BigDecimal> entry : probabilitiesHashmap.entrySet()) {
            char currentChar = entry.getKey();
            upperBound = lowerBound.add(entry.getValue());
            characterIntervalsTreemap.put(currentChar, new TupleValue(lowerBound, upperBound));
            lowerBound = upperBound;
        }
        return characterIntervalsTreemap;
    }

    /**
     * Recursively decode, stopping if we hit the stopWord or reach maxIterations.
     *
     * @param decodingValue    current arithmetic-encoded value
     * @param intervals        character-to-interval map
     * @param currentIndex     current character position
     * @param maxIterations    failsafe maximum decode length
     * @param decodedMessage   building the decoded string
     * @param lowerBound       current lower bound
     * @param upperBound       current upper bound
     * @param stopWord         optional string to stop decoding
     * @param precisionScale   the scale for dividing decodingValue
     * @return                 the decoded string
     */
    private static String decodeArithmeticallyEncodedMessageRecursive(
            BigDecimal decodingValue,
            TreeMap<Character, TupleValue> intervals,
            int currentIndex,
            int maxIterations,
            StringBuilder decodedMessage,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            String stopWord,
            int precisionScale
    ) {
        // Base cases for stopping the recursion (maxIterations or stopWord)
        if (currentIndex == maxIterations) {
            return decodedMessage.toString();
        }
        if (stopWord != null && !stopWord.isEmpty()) {
            if (decodedMessage.toString().endsWith(stopWord)) {
                return decodedMessage.toString();
            }
        }

        // 1. Identifies which character interval contains the current decodingValue
        char currentChar = 0;
        for(Map.Entry<Character, TupleValue> entry : intervals.entrySet()) {
            TupleValue tuple = entry.getValue();
            // Check [tuple.v1, tuple.v2)
            if (decodingValue.compareTo(tuple.v1) >= 0 && decodingValue.compareTo(tuple.v2) < 0) {
                currentChar = entry.getKey();
                decodedMessage.append(currentChar);
                break;
            }
        }

        // 2. Retrieves the interval for the found character
        TupleValue currentInterval = intervals.get(currentChar);

        // 3. Calculates new bounds (lower and upper)
        BigDecimal range = upperBound.subtract(lowerBound);
        BigDecimal newLowerBound = lowerBound.add(range.multiply(currentInterval.v1));
        BigDecimal newUpperBound = lowerBound.add(range.multiply(currentInterval.v2));

        // 4. Rescales the decodingValue
        BigDecimal intervalSize = currentInterval.v2.subtract(currentInterval.v1);
        BigDecimal newDecodingValue = decodingValue
                .subtract(currentInterval.v1)
                .divide(intervalSize, precisionScale, RoundingMode.HALF_UP);

        // 5. Recursively executes for the next character until we hit the stopWord or maxIterations
        return decodeArithmeticallyEncodedMessageRecursive(
                newDecodingValue,
                intervals,
                currentIndex + 1,
                maxIterations,
                decodedMessage,
                newLowerBound,
                newUpperBound,
                stopWord,
                precisionScale
        );
    }
}