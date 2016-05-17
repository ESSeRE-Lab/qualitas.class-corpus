<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:display="urn:jsptld:http://displaytag.sf.net/el">
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
            <display:table name="${requestScope.test}" id="table">
                <display:column maxLength="10">
                    <jsp:text>123"567890"123</jsp:text>
                </display:column>
                <display:column maxWords="3">
                    <jsp:text>Lorem ipsum dolor sit amet</jsp:text>
                </display:column>
                <display:column maxLength="3" />
                <display:column maxWords="3" />
            </display:table>
        </body>
    </html>
</jsp:root>