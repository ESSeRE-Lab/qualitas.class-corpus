@Title API Documentation for entire Product
@Rem $Id: RUN_doc.bat 6409 2008-10-04 00:07:46Z freyes $

rmdir /s /q API

@call documentation.bat  "..\ad\src;..\base\src;common\src;..\client\src;..\common\src;..\install\src;..\interfaces\src;..\print\src;..\serverApps\src;..\serverRoot\src;..\sqlj\src;..\tools\src;..\webCM\src;..\webStore\src" API

@pause


