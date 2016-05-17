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
                request.setAttribute( "test", new org.displaytag.test.ShuffledNumberedList());
            ]]> </jsp:scriptlet>
      <display:table name="requestScope.test" defaultsort="1">
        <display:column property="number" />
        <display:column property="number" />
        <display:caption><jsp:text>property: ascending</jsp:text></display:caption>
      </display:table>
      <display:table name="requestScope.test" defaultsort="1">
        <display:column property="number" decorator="org.displaytag.decorator.PercentualColumnDecorator"/>
        <display:column property="number" />
        <display:caption><jsp:text>decorated property: ascending</jsp:text></display:caption>
      </display:table>
      <display:table name="requestScope.test" defaultsort="1">
        <display:column property="number" sortProperty="number" decorator="org.displaytag.decorator.PercentualColumnDecorator"/>
        <display:column property="number" />
        <display:caption><jsp:text>decorated property + sortProperty: ascending</jsp:text></display:caption>
      </display:table>
      <display:table name="requestScope.test" defaultsort="1">
        <display:column sortProperty="number"><jsp:text>abc</jsp:text></display:column>
        <display:caption><jsp:text>body + sortProperty: ascending</jsp:text></display:caption>
        <display:column property="number" />
      </display:table>
    </body>
  </html>
</jsp:root>