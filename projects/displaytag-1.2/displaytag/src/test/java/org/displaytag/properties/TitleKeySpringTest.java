package org.displaytag.properties;

import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.I18nSpringAdapter;
import org.displaytag.localization.LocaleResolver;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * I18n test with Spring adapter.
 * @author Fabrizio Giustina
 * @version $Revision: 1081 $ ($Author: fgiust $)
 */
public class TitleKeySpringTest extends AbstractTitleKeyTest
{

    /**
     * @see org.displaytag.test.DisplaytagCase#getJspName()
     */
    public String getJspName()
    {
        return super.getJspName() + ".spring";
    }

    /**
     * @see org.displaytag.properties.AbstractTitleKeyTest#getExpectedSuffix()
     */
    protected String getExpectedSuffix()
    {
        return " spring";
    }

    /**
     * @see org.displaytag.properties.AbstractTitleKeyTest#getI18nResourceProvider()
     */
    protected I18nResourceProvider getI18nResourceProvider()
    {
        return new I18nSpringAdapter();
    }

    /**
     * @see org.displaytag.properties.AbstractTitleKeyTest#getResolver()
     */
    protected LocaleResolver getResolver()
    {
        return new I18nSpringAdapter();
    }

    /**
     * @see org.displaytag.test.DisplaytagCase#doTest(java.lang.String)
     */
    public void doTest(String jspName) throws Exception
    {
        this.runner.registerServlet("*.spring", DispatcherServlet.class.getName());
        super.doTest(jspName);
    }

}
