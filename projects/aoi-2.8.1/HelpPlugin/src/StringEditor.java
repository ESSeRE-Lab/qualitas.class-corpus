/*  StringEditor.java  */

package nik777.xlate;

/*
 * StringEditor: apply stored edit commands to strings.
 *
 * Copyright (C) 2000-2006, Nik Trevallyn-Jones, Sydney Australia
 *
 * Author: Nik Trevallyn-Jones, nik777@users.sourceforge.net
 * $Id: Exp $
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See version 2 of the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with this program. If not, version 2 of the license is available
 * from the GNU project, at http://www.gnu.org.
 */

import java.util.*;
import java.util.regex.*;

/**
 *  the string editor class.
 *
 *  Provides efficient, repeated editing functions specified by
 *  commands which can include regular expressions.
 *
 *  The editing command syntax is based loosely on the 'sed' commands, and uses
 *  the java.util.regex regular expression handler in
 *  {@link java.util.regex#Pattern} - see {@link #compile(String)} for details.
 *
 *  To invoke the editor, see
 *  {@link #edit(java.lang.String)}.
 */
public class StringEditor
{
    protected Pattern pattern;
    protected String value;
    protected char op;
    protected boolean global;

    protected StringEditor next;

    public static final StringEditor EMPTY = new StringEditor() {
	    public String edit(String str)
	    { return str; }

	    public void compile()
	    { next = null; }
	};

    public StringEditor()
    { next = null; }

    public StringEditor(String cmd)
    {
	next = null;
	compile(cmd);
    }

    /**
     *  return the number of edit rules in this editor
     */
    public int size()
    {
	int result = (pattern != null ? 1 : 0);
	for (StringEditor nx = next; nx != null; nx = nx.next) result++;

	return result;
    }

    /**
     *  edit a string.
     *
     *  @return the result of applying this editor to <i>str</i>.
     */
    public String edit(String str)
    {
	if (pattern == null) return str;

	String result = "";

	int pos=0, group=0, max;
	StringBuffer sb;
	Matcher matcher = pattern.matcher(str);

	switch (op) {
	case 's':
	case 'S':
	case 'd':
	case 'D':
	    // s,d - replace matched text with value or null
	    if (global)
		result = matcher.replaceAll(value);
	    else
		result = matcher.replaceFirst(value);
	    break;

	case 'a':
	case 'A':
	case 'i':
	case 'I':
	    // a,i append/insert value
	    sb = new StringBuffer(str.length()*2);

	    while (matcher.find()) {
		matcher.appendReplacement(sb, value);

		if (!global) break;
	    }

	    matcher.appendTail(sb);
	    result = sb.toString();
	    break;

	case 'p':
	case 'P':
	    // p return the specified matched group
	    sb = (global ? new StringBuffer(str.length()) : null);

	    // get the group number
	    max = value.length();
	    while (pos < max && Character.isDigit(value.charAt(pos++)))
		group = (group*10) + Character.digit(value.charAt(pos), 10);
	    
	    while (matcher.find()) {
		result = matcher.group(group);

		if (!global) break;

		sb.append(result);
	    }

	    if (global) result = sb.toString();
	}

	return (next != null ? next.edit(result) : result);
    }

    /**
     *  add an editor to this editor. The added editor will be invoked by
     *  this.edit() <i>after</i> all this editor's edits have been applied.
     */
    public void add(StringEditor editor)
    {
	if (next != null) next.add(editor);
	else next = editor;
    }

    /**
     *  add the specified editing commands to this editor.
     *
     *  The command(s) will be compiled and then executed <i>after</i> this
     *  editor's edits (if any) have been applied.
     */
    public void add(String cmd)
    {
	if (pattern == null) compile(cmd);
	else add(new StringEditor(cmd));
    }

    /**
     *  compile an edit command.
     *
     *  Causes any previous editing command to be replaced with the
     *  newly compiled command.
     *
     *<br>The format of an editing command is:
     *<br>[qual] op delim pattern [delim arg] [delim cmd]
     *
     *<br>Eg: gs/(dog|hound)/canine/s;cat;feline
     *
     *<br>Where:
     *<br>qual is a qualifier. The only qualifier currently is <i>g</i> which
     *    causes the associated operation to be applied to <i>all</i> matches.
     *<br>op is an editing operation, and is one of
     *
     *<ul>
     *<li> s - switch; replace matched text with <i>arg</i>
     *<li> a - append; append <i>arg</i> to the matched text
     *<li> i - insert; insert <i>arg</i> to the matched text
     *<li> d - delete; delete the matched text
     *<li> p - print; return the <i>arg</i>th match group (0 => all)
     *</ul>
     *
     *<br>delim is a single character (ie the character immediately following
     *	the <i>op</i>).
     *
     *<br>pattern is a regular expression for matching substrings in the text
     *	For an explanation of the valid regular expressions see
     *	{@link java.util.regex.Pattern}.
     *
     *<br>arg is an argument for the operation.
     *<br>Note also that the 's,a,i' commands allows <i>arg</i> to contain
     *  references to sequences of the matched data
     *  (see {@link java.util.regex.Matcher#appendReplacement()}).
     *  In particular the token '$0' will be replaced with the entire matched
     *  text. Hence a literal '$' char will need to be escaped as in '\$'.
     *</ul>
     *  
     *<br>As can be seen, multiple commands can be concatenated, each
     *  separated by the current delimiter char. In addition, the delimiter can
     *  be different for each command. By definition, the delimiter for each
     *  commmand is  the character <i>immediately following</i> the editing op.
     *
     *<br>So from this, the example command(s) above replace <i>all</i>
     *  occurences of 'dog' or 'hound' with 'canine, and then the first
     *  occurence of 'cat' with 'feline'. (In this example, the delimiter for
     *  the first op is '/', and for the second op is ';', although the one
     *  delimiter could just as easily be used for all commands).
     *
     *<br>Changes are applied in the same order as the commands, so
     *  if an earlier edit inserts a pieces of text which can match a later
     *  edit's pattern, then that newly added text may be modified by the
     *  later command.
     *
     *  @param cmd the editing command string.
     */
    public void compile(String cmd)
	throws PatternSyntaxException
    {
	cmd = cmd.trim();
	int len = (cmd != null ? cmd.length() : 0);

	char qual = cmd.charAt(0);
	if (qual == 'g' || qual == 'G') {
	    global = true;
	    cmd = cmd.substring(1);
	    len--;
	}

	if (len == 2) return;

	int cut, cut2;
	char delim;
	op = cmd.charAt(0);
	delim = cmd.charAt(1);

	// get the edit pattern
	cut = cmd.indexOf(delim, 2);
	if (cut < 0) pattern = Pattern.compile(cmd.substring(2));
	else pattern = Pattern.compile(cmd.substring(2, cut));

	switch (op) {
	case 's':
	case 'S':
	case 'a':
	case 'A':
	case 'i':
	case 'I':
	case 'p':
	case 'P':
	    if (cut > 0) {
		cut++;

		// find the replacement string
		cut2 = cmd.indexOf(delim, cut);
		if (cut2 > cut) value = cmd.substring(cut, cut2);
		else value = cmd.substring(cut);

		// null string
		if (value.length() == 1 && value.charAt(0) == delim)
		    value = "";

		cut = cut2;		// advance cut
	    }
	    else value = "";

	    // for insert/append, include the matched string (group-0)
	    switch ("aAiI".indexOf(op)) {
	    case 0:
	    case 1:
		value = "$0" + value;
		break;

	    case 2:
	    case 3:
		value = value + "$0";
		break;
	    }

	    break;

	case 'd':
	case 'D':
	    value = "";
	    break;

	default:
	    throw new PatternSyntaxException("Unrecognised cmd: " +
					     op, cmd, 0);
	}

	// if there is more, chain to next editor
	if (cut > 0 && cut+2 < len)
	    next = new StringEditor(cmd.substring(cut+1));

	else
	    next = null;
    }

}

