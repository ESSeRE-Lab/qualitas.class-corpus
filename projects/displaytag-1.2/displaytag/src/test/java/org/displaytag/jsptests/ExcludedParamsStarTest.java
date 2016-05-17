package org.displaytag.jsptests;

import org.displaytag.test.DisplaytagCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**
 * Testcase for "excludedParams" table attribute.
 * @author Fabrizio Giustina
 * @version $Revision: 1081 $ ($Author: fgiust $)
 */
public class ExcludedParamsStarTest extends DisplaytagCase
{

    /**
     * @see org.displaytag.test.DisplaytagCase#getJspName()
     */
    public String getJspName()
    {
        return "excludedparams-star.jsp";
    }

    /**
     * Checks generated pagination links.
     * @param jspName jsp name, with full path
     * @throws Exception any axception thrown during test.
     */
    public void doTest(String jspName) throws Exception
    {

        WebRequest request = new GetMethodWebRequest(jspName);
        request.setParameter("foo", "foovalue");
        request.setParameter("bar", "barvalue");
        request.setParameter("zoo", "zoovalue");

        WebResponse response = runner.getResponse(request);

        if (log.isDebugEnabled())
        {
            log.debug("RESPONSE: " + response.getText());
        }

        WebLink[] links = response.getLinks();

        for (int j = 0; j < links.length; j++)
        {
            String linkUrl = links[j].getURLString();
            assertTrue("Link contains the excluded parameter foo.", linkUrl.indexOf("foo") == -1);
            assertTrue("Link contains the excluded parameter bar.", linkUrl.indexOf("bar") == -1);
            assertTrue("Link contains the excluded parameter zoo.", linkUrl.indexOf("zoo") == -1);
        }

    }
}