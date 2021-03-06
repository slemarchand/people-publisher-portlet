/**
 * Copyright (c) 2014 Sebastien Le Marchand All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.slemarchand.peoplepublisher.search;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.model.User;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

/**
 * @author Sebastien Le Marchand
 */
public class UserSearch extends SearchContainer<User> {

	static List<String> headerNames = new ArrayList<String>();

	static {
		headerNames.add("title");
		headerNames.add("description");
		headerNames.add("user-name");
		headerNames.add("modified-date");
		headerNames.add("scope");
	}

	public static final String EMPTY_RESULTS_MESSAGE = "there-are-no-results";

	public UserSearch(
		PortletRequest portletRequest, int delta, PortletURL iteratorURL) {

		super(
			portletRequest, new UserDisplayTerms(portletRequest),
			new UserSearchTerms(portletRequest), DEFAULT_CUR_PARAM, delta,
			iteratorURL, headerNames, EMPTY_RESULTS_MESSAGE);

		UserDisplayTerms displayTerms = (UserDisplayTerms)getDisplayTerms();

		iteratorURL.setParameter(
			UserDisplayTerms.DESCRIPTION, displayTerms.getDescription());
		iteratorURL.setParameter(
			UserDisplayTerms.GROUP_ID,
			String.valueOf(displayTerms.getGroupId()));
		iteratorURL.setParameter(
			UserDisplayTerms.TITLE, displayTerms.getTitle());
		iteratorURL.setParameter(
			UserDisplayTerms.USER_NAME, displayTerms.getUserName());
	}

	public UserSearch(PortletRequest portletRequest, PortletURL iteratorURL) {
		this(portletRequest, DEFAULT_DELTA, iteratorURL);
	}

}