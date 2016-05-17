/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.objectweb.cjdbc.common.stream.encoding;

/**
 * This class provides encode/decode for RFC 2045 Base64 as defined by RFC 2045,
 * N. Freed and N. Borenstein. RFC 2045: Multipurpose Internet Mail Extensions
 * (MIME) Part One: Format of Internet Message Bodies. Reference 1996 Available
 * at: http://www.ietf.org/rfc/rfc2045.txt This class is used by XML Schema
 * binary format validation This implementation does not encode/decode streaming
 * data. You need the data that you will encode/decode already on a byte arrray.
 * 
 * @author Jeffrey Rodriguez
 * @author Sandy Gao
 * @version $Id: Base64.java,v 1.4 2005/06/18 02:47:39 cecchet Exp $
 */
public final class Base64
{
  private static final int     BASELENGTH             = 255;
  private static final int     LOOKUPLENGTH           = 64;
  private static final int     TWENTYFOURBITGROUP     = 24;
  private static final int     EIGHTBIT               = 8;
  private static final int     SIXTEENBIT             = 16;
  private static final int     FOURBYTE               = 4;
  private static final int     SIGN                   = -128;
  private static final char    PAD                    = '=';
  private static final boolean F_DEBUG                = false;
  private static final byte[]  BASE64_ALPHABET        = new byte[BASELENGTH];
  private static final char[]  LOOKUP_BASE64_ALPHABET = new char[LOOKUPLENGTH];

  static
  {

    for (int i = 0; i < BASELENGTH; i++)
    {
      BASE64_ALPHABET[i] = -1;
    }
    for (int i = 'Z'; i >= 'A'; i--)
    {
      BASE64_ALPHABET[i] = (byte) (i - 'A');
    }
    for (int i = 'z'; i >= 'a'; i--)
    {
      BASE64_ALPHABET[i] = (byte) (i - 'a' + 26);
    }

    for (int i = '9'; i >= '0'; i--)
    {
      BASE64_ALPHABET[i] = (byte) (i - '0' + 52);
    }

    BASE64_ALPHABET['+'] = 62;
    BASE64_ALPHABET['/'] = 63;

    for (int i = 0; i <= 25; i++)
      LOOKUP_BASE64_ALPHABET[i] = (char) ('A' + i);

    for (int i = 26, j = 0; i <= 51; i++, j++)
      LOOKUP_BASE64_ALPHABET[i] = (char) ('a' + j);

    for (int i = 52, j = 0; i <= 61; i++, j++)
      LOOKUP_BASE64_ALPHABET[i] = (char) ('0' + j);
    LOOKUP_BASE64_ALPHABET[62] = '+';
    LOOKUP_BASE64_ALPHABET[63] = '/';
  }

  protected static boolean isWhiteSpace(char octect)
  {
    return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
  }

  protected static boolean isPad(char octect)
  {
    return (octect == PAD);
  }

  protected static boolean isData(char octect)
  {
    return (BASE64_ALPHABET[octect] != -1);
  }

  protected static boolean isBase64(char octect)
  {
    return (isWhiteSpace(octect) || isPad(octect) || isData(octect));
  }

  /**
   * Encodes hex octects into Base64
   * 
   * @param binaryData Array containing binaryData
   * @return Encoded Base64 array
   */
  public static String encode(byte[] binaryData)
  {

    if (binaryData == null)
      return null;

    int lengthDataBits = binaryData.length * EIGHTBIT;
    if (lengthDataBits == 0)
    {
      return "";
    }

    int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
    int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
    int numberQuartet = fewerThan24bits != 0
        ? numberTriplets + 1
        : numberTriplets;
    int numberLines = (numberQuartet - 1) / 19 + 1;
    char[] encodedData = null;

    encodedData = new char[numberQuartet * 4 + numberLines];

    byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

    int encodedIndex = 0;
    int dataIndex = 0;
    int i = 0;
    if (F_DEBUG)
    {
      System.out.println("number of triplets = " + numberTriplets);
    }

    for (int line = 0; line < numberLines - 1; line++)
    {
      for (int quartet = 0; quartet < 19; quartet++)
      {
        b1 = binaryData[dataIndex++];
        b2 = binaryData[dataIndex++];
        b3 = binaryData[dataIndex++];

        if (F_DEBUG)
        {
          System.out.println("b1= " + b1 + ", b2= " + b2 + ", b3= " + b3);
        }

        l = (byte) (b2 & 0x0f);
        k = (byte) (b1 & 0x03);

        byte val1 = ((b1 & SIGN) == 0)
            ? (byte) (b1 >> 2)
            : (byte) ((b1) >> 2 ^ 0xc0);

        byte val2 = ((b2 & SIGN) == 0)
            ? (byte) (b2 >> 4)
            : (byte) ((b2) >> 4 ^ 0xf0);
        byte val3 = ((b3 & SIGN) == 0)
            ? (byte) (b3 >> 6)
            : (byte) ((b3) >> 6 ^ 0xfc);

        if (F_DEBUG)
        {
          System.out.println("val2 = " + val2);
          System.out.println("k4   = " + (k << 4));
          System.out.println("vak  = " + (val2 | (k << 4)));
        }

        encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val1];
        encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val2 | (k << 4)];
        encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[(l << 2) | val3];
        encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[b3 & 0x3f];

        i++;
      }
      encodedData[encodedIndex++] = 0xa;
    }

    for (; i < numberTriplets; i++)
    {
      b1 = binaryData[dataIndex++];
      b2 = binaryData[dataIndex++];
      b3 = binaryData[dataIndex++];

      if (F_DEBUG)
      {
        System.out.println("b1= " + b1 + ", b2= " + b2 + ", b3= " + b3);
      }

      l = (byte) (b2 & 0x0f);
      k = (byte) (b1 & 0x03);

      byte val1 = ((b1 & SIGN) == 0)
          ? (byte) (b1 >> 2)
          : (byte) ((b1) >> 2 ^ 0xc0);

      byte val2 = ((b2 & SIGN) == 0)
          ? (byte) (b2 >> 4)
          : (byte) ((b2) >> 4 ^ 0xf0);
      byte val3 = ((b3 & SIGN) == 0)
          ? (byte) (b3 >> 6)
          : (byte) ((b3) >> 6 ^ 0xfc);

      if (F_DEBUG)
      {
        System.out.println("val2 = " + val2);
        System.out.println("k4   = " + (k << 4));
        System.out.println("vak  = " + (val2 | (k << 4)));
      }

      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val1];
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val2 | (k << 4)];
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[(l << 2) | val3];
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[b3 & 0x3f];
    }

    // form integral number of 6-bit groups
    if (fewerThan24bits == EIGHTBIT)
    {
      b1 = binaryData[dataIndex];
      k = (byte) (b1 & 0x03);
      if (F_DEBUG)
      {
        System.out.println("b1=" + b1);
        System.out.println("b1<<2 = " + (b1 >> 2));
      }
      byte val1 = ((b1 & SIGN) == 0)
          ? (byte) (b1 >> 2)
          : (byte) ((b1) >> 2 ^ 0xc0);
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val1];
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[k << 4];
      encodedData[encodedIndex++] = PAD;
      encodedData[encodedIndex++] = PAD;
    }
    else if (fewerThan24bits == SIXTEENBIT)
    {
      b1 = binaryData[dataIndex];
      b2 = binaryData[dataIndex + 1];
      l = (byte) (b2 & 0x0f);
      k = (byte) (b1 & 0x03);

      byte val1 = ((b1 & SIGN) == 0)
          ? (byte) (b1 >> 2)
          : (byte) ((b1) >> 2 ^ 0xc0);
      byte val2 = ((b2 & SIGN) == 0)
          ? (byte) (b2 >> 4)
          : (byte) ((b2) >> 4 ^ 0xf0);

      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val1];
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[val2 | (k << 4)];
      encodedData[encodedIndex++] = LOOKUP_BASE64_ALPHABET[l << 2];
      encodedData[encodedIndex++] = PAD;
    }

    encodedData[encodedIndex] = 0xa;

    return new String(encodedData);
  }

  /**
   * Decodes Base64 data into octects
   * 
   * @param encoded String encoded in Base64
   * @return Byte array containing decoded data.
   */
  public static byte[] decode(String encoded)
  {

    if (encoded == null)
      return null;

    char[] base64Data = encoded.toCharArray();
    // remove white spaces
    int len = removeWhiteSpace(base64Data);

    if (len % FOURBYTE != 0)
    {
      return null;//should be divisible by four
    }

    int numberQuadruple = (len / FOURBYTE);

    if (numberQuadruple == 0)
      return new byte[0];

    byte[] decodedData = null;
    byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
    char d1 = 0, d2 = 0, d3 = 0, d4 = 0;

    int i = 0;
    int encodedIndex = 0;
    int dataIndex = 0;
    decodedData = new byte[(numberQuadruple) * 3];

    for (; i < numberQuadruple - 1; i++)
    {
      d1 = base64Data[dataIndex++];
      d2 = base64Data[dataIndex++];
      d3 = base64Data[dataIndex++];
      d4 = base64Data[dataIndex++];
      if (!isData(d1) || !isData(d2) || !isData(d3) || !isData(d4))
        return null;//if found "no data" just return null

      b1 = BASE64_ALPHABET[d1];
      b2 = BASE64_ALPHABET[d2];
      b3 = BASE64_ALPHABET[d3];
      b4 = BASE64_ALPHABET[d4];

      decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
      decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
      decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
    }

    d1 = base64Data[dataIndex++];
    d2 = base64Data[dataIndex++];
    if (!isData(d1) || !isData(d2))
    {
      return null;//if found "no data" just return null
    }

    b1 = BASE64_ALPHABET[d1];
    b2 = BASE64_ALPHABET[d2];

    d3 = base64Data[dataIndex++];
    d4 = base64Data[dataIndex++];
    if (!isData((d3)) || !isData((d4)))
    {//Check if they are PAD characters
      if (isPad(d3) && isPad(d4))
      { //Two PAD e.g. 3c[Pad][Pad]
        if ((b2 & 0xf) != 0)//last 4 bits should be zero
          return null;
        byte[] tmp = new byte[i * 3 + 1];
        System.arraycopy(decodedData, 0, tmp, 0, i * 3);
        tmp[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
        return tmp;
      }
      else if (!isPad(d3) && isPad(d4))
      { //One PAD e.g. 3cQ[Pad]
        b3 = BASE64_ALPHABET[d3];
        if ((b3 & 0x3) != 0)//last 2 bits should be zero
          return null;
        byte[] tmp = new byte[i * 3 + 2];
        System.arraycopy(decodedData, 0, tmp, 0, i * 3);
        tmp[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
        tmp[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
        return tmp;
      }
      else
      {
        return null;//an error like "3c[Pad]r", "3cdX", "3cXd", "3cXX" where X
        // is non data
      }
    }
    else
    { //No PAD e.g 3cQl
      b3 = BASE64_ALPHABET[d3];
      b4 = BASE64_ALPHABET[d4];
      decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
      decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
      decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);

    }

    return decodedData;
  }

  /**
   * remove WhiteSpace from MIME containing encoded Base64 data.
   * 
   * @param data the byte array of base64 data (with WS)
   * @return the new length
   */
  protected static int removeWhiteSpace(char[] data)
  {
    if (data == null)
      return 0;

    // count characters that's not whitespace
    int newSize = 0;
    int len = data.length;
    for (int i = 0; i < len; i++)
    {
      if (!isWhiteSpace(data[i]))
        data[newSize++] = data[i];
    }
    return newSize;
  }
}