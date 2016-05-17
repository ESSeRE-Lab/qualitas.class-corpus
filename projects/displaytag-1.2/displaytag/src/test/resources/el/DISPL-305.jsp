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
          Object[] testData = new Object[]{new org.displaytag.test.KnownValue(),new org.displaytag.test.KnownValue()};
          pageContext.setAttribute( "test", testData);
      ]]> </jsp:scriptlet>
      <display:table name="pageScope.test">
        <display:setProperty name="css.tr.even" value="" />
        <display:setProperty name="css.tr.odd" value="" />
        <display:column property="ant"/>
      </display:table>
    </body>
  </html>
</jsp:root>