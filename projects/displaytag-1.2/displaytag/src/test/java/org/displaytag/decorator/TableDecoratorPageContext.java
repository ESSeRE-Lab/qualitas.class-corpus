package org.displaytag.decorator;

/**
 * Test decorator used in tests.
 * @author Fabrizio Giustina
 * @version $Revision: 1084 $ ($Author: fgiust $)
 */
public class TableDecoratorPageContext extends TableDecorator
{

    public String getUsePageContext()
    {
        return this.getPageContext() != null ? "OK" : "ko";
    }

}