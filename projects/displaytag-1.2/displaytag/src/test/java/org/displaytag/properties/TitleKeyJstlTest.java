package org.displaytag.properties;

import org.displaytag.localization.I18nJstlAdapter;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.LocaleResolver;


/**
 * I18n test with JSTL adapter.
 * @author Fabrizio Giustina
 * @version $Revision: 1081 $ ($Author: fgiust $)
 */
public class TitleKeyJstlTest extends AbstractTitleKeyTest
{

    /**
     * @see org.displaytag.properties.AbstractTitleKeyTest#getExpectedSuffix()
     */
    protected String getExpectedSuffix()
    {
        return "";
    }

    /**
     * @see org.displaytag.properties.AbstractTitleKeyTest#getI18nResourceProvider()
     */
    protected I18nResourceProvider getI18nResourceProvider()
    {
        return new I18nJstlAdapter();
    }

    /**
     * @see org.displaytag.properties.AbstractTitleKeyTest#getResolver()
     */
    protected LocaleResolver getResolver()
    {
        return new I18nJstlAdapter();
    }

}
