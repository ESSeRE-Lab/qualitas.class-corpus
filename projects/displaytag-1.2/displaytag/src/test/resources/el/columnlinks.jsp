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
      <display:table name="${requestScope.test}" id="table" requestURI="/goforit">
        <display:column property="ant" url="/dynlink" paramId="param" paramProperty="ant" />
        <display:column property="ant" href="/context/dynlink" paramId="param" paramProperty="ant" />
        <display:column property="ant" href="dynlink" paramId="param" paramProperty="ant" />
        <display:column property="ant" href="http://something/dynlink" paramId="param" paramProperty="ant" />
        <display:column property="ant" href="http://something/dynlink" />
        <display:column property="ant" url="/dynlink" />
      </display:table>
    </body>
  </html>
</jsp:root>