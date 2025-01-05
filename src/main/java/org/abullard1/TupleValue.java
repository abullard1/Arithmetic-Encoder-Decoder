package main.java.org.abullard1;

import java.math.BigDecimal;

/**
 * TupleValue class for holding two BigDecimal values.
 *
 */
public class TupleValue {
    public BigDecimal v1;
    public BigDecimal v2;

    public TupleValue(BigDecimal v1, BigDecimal v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
}
