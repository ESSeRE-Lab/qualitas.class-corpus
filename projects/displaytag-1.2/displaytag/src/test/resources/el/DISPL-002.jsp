<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:display="urn:jsptld:http://displaytag.sf.net/el"
    xmlns:c="urn:jsptld:../WEB-INF/tld/c.tld">
    <jsp:text> <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]> </jsp:text>
    <jsp:directive.page contentType="text/html; charset=UTF8"/>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
        <head>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
            <title>Displaytag unit test</title>
        </head>
        <body>
            <jsp:scriptlet> <![CDATA[
              java.util.List testData = new java.util.ArrayList();
              testData.add(new org.displaytag.test.KnownValue());
              request.setAttribute( "test", testData);
            ]]> </jsp:scriptlet>

            <display:table id="table" name="test" sort="list">
                <display:column property="ant" />
                <display:column><c:out value="${table.bee}" /></display:column>
            </display:table>

						<c:set var="variableid" value="table2" />
            <display:table id="${variableid}" name="test" sort="list">
                <display:column property="ant" />
                <display:column><c:out value="${table2.bee}" /></display:column>
            </display:table>

						<c:set var="variableid" value="1" />
            <display:table id="table${variableid}" name="test" sort="list">
                <display:column property="ant" />
                <display:column><c:out value="${table1.bee}" /></display:column>
            </display:table>

            <display:table id="try" name="test" sort="list">
                <display:column property="ant" />
                <display:column><c:out value="${try.bee}" /></display:column>
            </display:table>
        </body>
    </html>
</jsp:root>