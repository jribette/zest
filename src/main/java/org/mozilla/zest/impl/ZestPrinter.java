/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.zest.impl;

import org.mozilla.zest.core.v1.ZestAction;
import org.mozilla.zest.core.v1.ZestActionFail;
import org.mozilla.zest.core.v1.ZestActionIntercept;
import org.mozilla.zest.core.v1.ZestActionInvoke;
import org.mozilla.zest.core.v1.ZestActionPrint;
import org.mozilla.zest.core.v1.ZestActionScan;
import org.mozilla.zest.core.v1.ZestActionSleep;
import org.mozilla.zest.core.v1.ZestAssertion;
import org.mozilla.zest.core.v1.ZestAssignCalc;
import org.mozilla.zest.core.v1.ZestAssignRegexDelimiters;
import org.mozilla.zest.core.v1.ZestAssignReplace;
import org.mozilla.zest.core.v1.ZestAssignStringDelimiters;
import org.mozilla.zest.core.v1.ZestAssignment;
import org.mozilla.zest.core.v1.ZestAuthentication;
import org.mozilla.zest.core.v1.ZestComment;
import org.mozilla.zest.core.v1.ZestConditional;
import org.mozilla.zest.core.v1.ZestExpressionAnd;
import org.mozilla.zest.core.v1.ZestExpressionElement;
import org.mozilla.zest.core.v1.ZestExpressionEquals;
import org.mozilla.zest.core.v1.ZestExpressionIsInteger;
import org.mozilla.zest.core.v1.ZestExpressionLength;
import org.mozilla.zest.core.v1.ZestExpressionOr;
import org.mozilla.zest.core.v1.ZestExpressionRegex;
import org.mozilla.zest.core.v1.ZestExpressionResponseTime;
import org.mozilla.zest.core.v1.ZestExpressionStatusCode;
import org.mozilla.zest.core.v1.ZestExpressionURL;
import org.mozilla.zest.core.v1.ZestHttpAuthentication;
import org.mozilla.zest.core.v1.ZestLoop;
import org.mozilla.zest.core.v1.ZestLoopFile;
import org.mozilla.zest.core.v1.ZestLoopInteger;
import org.mozilla.zest.core.v1.ZestLoopString;
import org.mozilla.zest.core.v1.ZestLoopTokenStringSet;
import org.mozilla.zest.core.v1.ZestRequest;
import org.mozilla.zest.core.v1.ZestScript;
import org.mozilla.zest.core.v1.ZestStatement;

public class ZestPrinter {

	private static String cleanStr(String str) {
		if (str != null) {
			return str;
		}
		return "";
	}

	public static void summary(ZestScript zs) {
		if (zs == null) {
			System.out.println("Null Zest script");
			return;
		}
		System.out.println("About:         " + zs.getAbout());
		System.out.println("Version:       " + zs.getZestVersion());
		System.out.println("Generated by:  " + cleanStr(zs.getGeneratedBy()));
		System.out.println("Type:          " + cleanStr(zs.getType()));
		System.out.println("Author:        " + cleanStr(zs.getAuthor()));
		System.out.println("Title:         " + cleanStr(zs.getTitle()));
		System.out.println("Description:   " + cleanStr(zs.getDescription()));
		System.out.println("Prefix:        " + cleanStr(zs.getPrefix()));
		System.out.println("Parameters:");
		for (String[] tokens : zs.getParameters().getVariables()) {
			System.out.println("    " + tokens[0] + "=" + tokens[1]);
		}
		for (ZestAuthentication za : zs.getAuthentication()) {
			if (za instanceof ZestHttpAuthentication) {
				ZestHttpAuthentication zha = (ZestHttpAuthentication) za;
				System.out.println("HTTP Authentication:");
				printIndent(1);
				System.out.println("Site:      " + cleanStr(zha.getSite()));
				printIndent(1);
				System.out.println("Realm:     " + cleanStr(zha.getRealm()));
				printIndent(1);
				System.out.println("Username:  " + cleanStr(zha.getUsername()));
				printIndent(1);
				System.out.println("Password:  " + cleanStr(zha.getPassword()));
			} else {
				System.out.println("Authentication not supported: "
						+ za.getElementType());
			}
		}
	}

	private static void printIndent(int indent) {
		printIndent(indent, -1);
	}

	private static void printIndent(int indent, int lineNumber) {
		if (lineNumber >= 0) {
			System.out.format("%3d:", lineNumber);

		} else {
			System.out.print("    ");
		}
		for (int i = 0; i < indent; i++) {
			System.out.print("    ");
		}
	}

	public static void list(ZestStatement stmt, int indent) {
		if (stmt instanceof ZestRequest) {
			ZestRequest req = (ZestRequest) stmt;
			printIndent(indent, stmt.getIndex());
			System.out.println(req.getMethod() + " " + req.getUrl());
			if (req.getHeaders() != null && req.getHeaders().length() > 0) {
				printIndent(indent + 1);
				System.out.println("Headers: " + req.getHeaders());
			}
			if (req.getData() != null && req.getData().length() > 0) {
				printIndent(indent + 1);
				System.out.println("Data: " + req.getData());
			}
			for (ZestAssertion za : req.getAssertions()) {
				printIndent(indent + 1);
				System.out.println("Assert: " + za.getElementType());
			}
		} else if (stmt instanceof ZestConditional) {
			ZestConditional zc = (ZestConditional) stmt;
			printIndent(indent, stmt.getIndex());
			System.out.print("IF ");
			printExpression(zc.getRootExpression(), 0);
			System.out.println();
			for (ZestStatement ifStmt : zc.getIfStatements()) {
				list(ifStmt, indent + 1);
			}
			printIndent(indent);
			System.out.println("ELSE");
			for (ZestStatement elseStmt : zc.getElseStatements()) {
				list(elseStmt, indent + 1);
			}
		} else if (stmt instanceof ZestAction) {
			ZestAction za = (ZestAction) stmt;
			printIndent(indent, stmt.getIndex());
			if (za instanceof ZestActionFail) {
				ZestActionFail zaf = (ZestActionFail) za;
				System.out.println("Action Fail: " + zaf.getPriority() + " : "
						+ zaf.getMessage());
			} else if (za instanceof ZestActionScan) {
				ZestActionScan zas = (ZestActionScan) za;
				System.out.println("Action Scan: " + zas.getTargetParameter());
			} else if (za instanceof ZestActionIntercept) {
				System.out.println("Action Intercept");
			} else if (za instanceof ZestActionInvoke) {
				ZestActionInvoke zas = (ZestActionInvoke) za;
				System.out.print("Action Invoke: "+ zas.getVariableName() + " = " + zas.getScript() + "(");
				for (String [] param : zas.getParameters()) {
					System.out.print(param[0] + "=" + param[1] + " ");
				}
				System.out.println(")");
				
			} else if (za instanceof ZestActionSleep) {
				ZestActionSleep zas = (ZestActionSleep) za;
				System.out.println("Action Sleep: " + zas.getMilliseconds());
			} else if (za instanceof ZestActionPrint) {
				ZestActionPrint zas = (ZestActionPrint) za;
				System.out.println("Action Print: " + zas.getMessage());
			} else {
				System.out.println("(Unknown action: " + stmt.getElementType() + ")");
			}
		} else if (stmt instanceof ZestAssignment) {
			ZestAssignment za = (ZestAssignment) stmt;
			printIndent(indent, stmt.getIndex());
			if (za instanceof ZestAssignRegexDelimiters) {
				ZestAssignRegexDelimiters zas = (ZestAssignRegexDelimiters) za;
				System.out.println("Set Variable: " + zas.getVariableName());
			} else if (za instanceof ZestAssignStringDelimiters) {
				ZestAssignStringDelimiters zas = (ZestAssignStringDelimiters) za;
				System.out.println("Set Variable: " + zas.getVariableName());
			} else if (za instanceof ZestAssignReplace) {
				ZestAssignReplace zas = (ZestAssignReplace) za;
				System.out.println("Set Variable: " + zas.getVariableName() + 
						" Replace " + zas.getReplace() + " With " + zas.getReplace());
			} else if (za instanceof ZestAssignCalc) {
				ZestAssignCalc zas = (ZestAssignCalc) za;
				System.out.println("Set Variable: " + zas.getVariableName() + 
						" " + zas.getOperandA() + " " + zas.getOperation() + " " + zas.getOperandB());
			} else {
				System.out.println("(Unknown assignment: " + stmt.getElementType() + ")");
			}
		} else if (stmt instanceof ZestLoop) {
			ZestLoop<?> loop = (ZestLoop<?>) stmt;
			printIndent(indent, loop.getIndex());
			if (stmt instanceof ZestLoopString) {
				ZestLoopString loopString = (ZestLoopString) loop;
				System.out.print("FOR tokens IN [");
				ZestLoopTokenStringSet set = loopString.getSet();
				for (int i = 0; i < set.size() - 1; i++) {
					System.out.print(set.getToken(i) + ",");
				}
				System.out.println(set.getToken(set.size() - 1) + "] DO");
			} else if (stmt instanceof ZestLoopFile) {
				ZestLoopFile loopFile = (ZestLoopFile) loop;
				System.out.println("FOR tokens IN FILE "
						+ loopFile.getFile().getAbsolutePath() + " DO");
			} else if (stmt instanceof ZestLoopInteger) {
				ZestLoopInteger loopInteger = (ZestLoopInteger) loop;
				System.out.println("FOR tokens FROM " + loopInteger.getStart()
						+ " TO " + loopInteger.getEnd() + " DO");
			} else {
				System.out.println("(Unknown loop: " + stmt.getElementType()
						+ ")");
			}
			for (ZestStatement stmtInLoop : loop.getStatements()) {
				list(stmtInLoop, indent + 1);
			}
		} else if (stmt instanceof ZestComment) {
			ZestComment zc = (ZestComment) stmt;
			printIndent(indent, stmt.getIndex());
			System.out.println("Comment: " + zc.getComment());

		} else {
			printIndent(indent, stmt.getIndex());
			System.out.println("(Unknown: " + stmt.getElementType() + ")");
		}

	}

	public static void printExpression(ZestExpressionElement element, int indent) {
		if (element.isInverse())
			System.out.print("NOT ");
		if (element.isLeaf()) {
			if (element instanceof ZestExpressionLength) {
				ZestExpressionLength lengthExpr = (ZestExpressionLength) element;
				System.out.print("Length: " + lengthExpr.getVariableName() + " " + lengthExpr.getLength()
						+ " approx: " + lengthExpr.getApprox());
			} else if (element instanceof ZestExpressionEquals) {
				ZestExpressionEquals eqExpr = (ZestExpressionEquals) element;
				System.out.print("Regex: " + eqExpr.getVariableName() + " " + 
						(eqExpr.isCaseExact() ? " caseExact " : " caseIgnore ") +
						eqExpr.getValue());
			} else if (element instanceof ZestExpressionRegex) {
				ZestExpressionRegex regexExpr = (ZestExpressionRegex) element;
				System.out.print("Regex: " + regexExpr.getVariableName() + " "
						+ regexExpr.getRegex());
			} else if (element instanceof ZestExpressionResponseTime) {
				ZestExpressionResponseTime timeExpr = (ZestExpressionResponseTime) element;
				System.out.print("Response Time: "
						+ (timeExpr.isGreaterThan() ? ">" : "<=")
						+ timeExpr.getTimeInMs() + " ");
			} else if (element instanceof ZestExpressionStatusCode) {
				ZestExpressionStatusCode codeExpr = (ZestExpressionStatusCode) element;
				System.out.print("Status Code: " + codeExpr.getCode());
			} else if (element instanceof ZestExpressionIsInteger) {
				ZestExpressionIsInteger codeExpr = (ZestExpressionIsInteger) element;
				System.out.print("Is Integer: " + codeExpr.getVariableName());
			} else if (element instanceof ZestExpressionURL) {
				// ZestExpressionURL urlExpr=(ZestExpressionURL)element;
				System.out.print("URL ");
			}
		} else {
			printIndent(indent);
			int lastChildPrinted;
			if (element instanceof ZestExpressionAnd) {
				ZestExpressionAnd andElement = (ZestExpressionAnd) element;
				System.out.println();
				printIndent(indent);
				System.out.print("AND: (");
				for (lastChildPrinted = 0; lastChildPrinted < andElement
						.getChildrenCondition().size() - 1; lastChildPrinted++) {
					printExpression(andElement.getChild(lastChildPrinted),
							indent + 1);
					System.out.print(" && ");
				}
				printExpression(andElement.getChild(lastChildPrinted),
						indent + 1);
				System.out.println(")");
				printIndent(indent);
			} else if (element instanceof ZestExpressionOr) {
				ZestExpressionOr orElement = (ZestExpressionOr) element;
				System.out.println();
				printIndent(indent);
				System.out.print("OR: (");
				for (lastChildPrinted = 0; lastChildPrinted < orElement
						.getChildrenCondition().size() - 1; lastChildPrinted++) {
					printExpression(orElement.getChild(lastChildPrinted),
							indent + 1);
					System.out.print(" || ");
				}
				printExpression(orElement.getChild(lastChildPrinted),
						indent + 1);
				// System.out.println();
				// printIndent(indent);
				System.out.println(")");
				printIndent(indent);
			}
			// System.out.println();
		}
	}

	public static void list(ZestScript zs) {
		summary(zs);

		if (zs != null) {
			System.out.println("Statements:");
			for (ZestStatement stmt : zs.getStatements()) {
				list(stmt, 1);
			}
		}
	}

}
