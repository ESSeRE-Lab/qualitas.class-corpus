package org.argouml.language.csharp.importer.csparser.enums;


/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7; 2008
 * Time: 12:30:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Modifier {
        public static long Empty		= 0x0000000;
        public static long New			= 0x0000001;
        public static long Public		= 0x0000002;
        public static long Protected	= 0x0000004;
        public static long Internal	= 0x0000008;
        public static long Private		= 0x0000010;
        public static long Abstract	= 0x0000020;
        public static long Sealed		= 0x0000040;
        public static long Partial	= 0x0000080;

        public static long Static		= 0x0000100;
        public static long Virtual		= 0x0000200;
        public static long Override	= 0x0000400;
        public static long Extern		= 0x0000800;
        public static long Readonly	= 0x0001000;
        public static long Volatile	= 0x0002000;

        public static long Ref			= 0x0008000;
        public static long Out			= 0x0010000;
        public static long Params		= 0x0020000;

        public static long Assembly	= 0x0040000;
        public static long Field		= 0x0080000;
        public static long Event		= 0x0100000;
        public static long Method		= 0x0200000;
        public static long Param		= 0x0400000;
        public static long Property	= 0x0800000;
        public static long Return		= 0x1000000;
        public static long Type		= 0x2000000;
        public static long Module		= 0x4000000;

        public static long ClassMods		= New | Public | Protected | Internal | Private | Abstract | Sealed | Partial | Static;
        public static long ConstantMods	= New | Public | Protected | Internal | Private;
        public static long FieldMods		= New | Public | Protected | Internal | Private | Static | Readonly | Volatile;
        public static long MethodMods		= New | Public | Protected | Internal | Private | Static | Virtual | Sealed | Override | Abstract | Extern;
        public static long ParamMods		= Ref | Out;
        public static long PropertyMods	= New | Public | Protected | Internal | Private | Static | Virtual | Sealed | Override | Abstract | Extern;
        public static long EventMods		= New | Public | Protected | Internal | Private | Static | Virtual | Sealed | Override | Abstract | Extern;
        public static long IndexerMods		= New | Public | Protected | Internal | Private | Static | Virtual | Sealed | Override | Abstract | Extern;
        public static long OperatorMods	= Public | Static | Extern;
        public static long ConstructorMods = Public | Protected | Internal | Private | Extern;
        public static long DestructorMods	= Extern;
        public static long StructMods		= New | Public | Protected | Internal | Private;
        public static long InterfaceMods	= New | Public | Protected | Internal | Private;
        public static long EnumMods		= New | Public | Protected | Internal | Private;
        public static long DelegateMods	= New | Public | Protected | Internal | Private;
        public static long AttributeMods	= Field | Event | Method | Param | Property | Return | Type | Module;
        public static long GlobalAttributeMods	= Assembly;

}