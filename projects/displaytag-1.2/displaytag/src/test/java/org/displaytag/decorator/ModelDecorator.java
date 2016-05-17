package org.displaytag.decorator;

/**
 * @author Jorge Barroso
 * @version $Id: ModelDecorator.java 1081 2006-04-03 20:26:34Z fgiust $
 */
public class ModelDecorator extends TableDecorator
{

    public static final String DECORATED_VALUE = "decoratedValue";

    public String getDecoratedValue()
    {
        return DECORATED_VALUE;
    }

}