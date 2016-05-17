package org.displaytag.jsptests;

import org.displaytag.tags.TableTagParameters;
import org.displaytag.test.DisplaytagCase;
import org.displaytag.util.ParamEncoder;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**
 * Test for DISPL-213 - Page Listing Incorrect.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Displ213Test extends DisplaytagCase
{

    /**
     * @see org.displaytag.test.DisplaytagCase#getJspName()
     */
    public String getJspName()
    {
        return "DISPL-213.jsp";
    }

    /**
     * The last page should show up in the list.
     * @param jspName jsp name, with full path
     * @throws Exception any axception thrown during test.
     */
    public void doTest(String jspName) throws Exception
    {
        WebRequest request = new GetMethodWebRequest(jspName);
        ParamEncoder encoder = new ParamEncoder("table");
        request.setParameter(encoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE), "9");

        WebResponse response = runner.getResponse(request);

        if (log.isDebugEnabled())
        {
            log.debug(response.getText());
        }

        HTMLElement pagination = response.getElementWithID("pagination");
        assertNotNull("Paging banner not found.", pagination);
        assertEquals("Pagination links are not as expected.", "6, 7, 8, [9], 10", pagination.getText());

    }

}