package org.argouml.language.csharp.importer.csparser.enums;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 12:42:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreprocessorID
	{
		public final static byte Empty	= 0x00;
		public final static byte Define	= 0x01;
		public final static byte Undef	= 0x02;
		public final static byte If		= 0x03;
		public final static byte Elif	= 0x04;
		public final static byte Else	= 0x05;
		public final static byte Endif	= 0x06;
		public final static byte Line	= 0x07;
		public final static byte Error	= 0x08;
		public final static byte Warning	= 0x09;
		public final static byte Region	= 0x0A;
		public final static byte Endregion = 0x0B;
		public final static byte Pragma	= 0x0C;
	}