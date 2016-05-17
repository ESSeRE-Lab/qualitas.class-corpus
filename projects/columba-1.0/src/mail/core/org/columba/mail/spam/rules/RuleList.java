// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.spam.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.columba.mail.folder.IMailbox;
import org.macchiato.maps.ProbabilityMap;
import org.macchiato.maps.ProbabilityMapImpl;
import org.macchiato.tokenizer.Token;

/**
 * Managing list of Rules
 * <p>
 * This is currently a hack. Adding new rules should happen more dynamic in the
 * future.
 * 
 * @author fdietz
 * 
 */
public class RuleList {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.spam.rules");

	private List list;

	private static RuleList instance;

	public RuleList() {
		list = new ArrayList();

		addRule(new SubjectWhitespaceRule());
		addRule(new OnlyHTMLMimepartRule());
		addRule(new SubjectIsAllCapitalsRule());
		addRule(new MixedCharactersAddressRule());
		addRule(new MissingToHeaderRule());
		addRule(new SubjectContainsSpamRule());
	}

	public static RuleList getInstance() {
		if (instance == null)
			instance = new RuleList();

		return instance;
	}

	public ProbabilityMap getProbabilities(IMailbox folder, Object uid)
			throws Exception {

		ProbabilityMap map = new ProbabilityMapImpl();

		Iterator it = list.iterator();

		while (it.hasNext()) {
			Rule rule = (Rule) it.next();
			LOG.info("rule " + rule.getName());

			float score = rule.score(folder, uid);
			LOG.info("score=" + score);

			map.addToken(new Token(rule.getName()), score);
		}

		return map;
	}

	public void addRule(Rule rule) {
		list.add(rule);
	}
}
