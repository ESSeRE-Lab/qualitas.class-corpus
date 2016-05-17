package org.argouml.language.csharp.importer.csparser.enums;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 12:41:02 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ParseState
	{
		Start,

		Using_Ident,
		Using_Target,
		Using_Semi,

		Namespace_Start,
		Namespace_Ident,
		Namespace_Open,
		Namespace_Close,

		Class_Start,
		Class_Ident,
		Class_Base,
		Class_Interface,
		Class_Open,
		Class_Member,
		Class_Close,
		Class_End,

		Member_Start,
		Member_Default,
		Member_Qualified,
		Member_End,

		Ctor_Start,
		Ctor_Params,
		Ctor_Colon,
		Ctor_Base,
		Ctor_This,
		Ctor_End,

		Method_Start,
		Method_Params,
		Method_Open,
		Method_Statements,
		Method_Close,
		Method_End,

		Field_Start,
		Field_Equal,
		Field_Ident,
		Field_Equal2,
		Field_Value,
		Field_End,

		Property_Start,
		Property_Block,
		Property_Get,
		Property_Set,
		Property_End,

		Event_Start,
		Event_Type,
		Event_Name,
		Event_End,

		Const_Start,
		Const_Type,
		Const_Equal,
		Const_Equal2,
		Const_Value,
		Const_End,

		ParamList_Start,
		ParamList_Ref,
		ParamList_Out,
		ParamList_Params,
		ParamList_Type,
		ParamList_Name,
		ParamList_Comma,
		ParamList_End,

		ParamDecl_Start,
		ParamDecl_Type,
		ParamDecl_Name,
		ParamDecl_End,

		Args_Start,
		Args_Ref,
		Args_Out,
		Args_Expression,
		Args_Comma,
		Args_End,

		Accessor_Start,
		Accessor_Kind,
		Accessor_Open,
		Accessor_Close,
		Accessor_End,

		ConstExpr_Start,
		ConstExpr_End,

		Modifiers_Start,
		Modifiers_End,

		QualifiedIdentifier_Start,
		QualifiedIdentifier_Name,
		QualifiedIdentifier_Dot,
		QualifiedIdentifier_Name2,
		QualifiedIdentifier_End,

		Type_Start,
		Type_TypeName,
		Type_ArrayOpen,
		Type_ArrayComma,
		Type_ArrayClose,
		Type_MultiArray,
		Type_End,

		Block_Open,
		Block_Statements,
		Block_End,
	}
