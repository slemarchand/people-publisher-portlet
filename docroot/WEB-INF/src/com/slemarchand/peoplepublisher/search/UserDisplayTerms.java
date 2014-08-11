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

import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.util.ParamUtil;

import javax.portlet.PortletRequest;

/**
 * @author Sebastien Le Marchand
 */
public class UserDisplayTerms extends DisplayTerms {

	public static final String DESCRIPTION = "description";

	public static final String GROUP_ID = "groupId";

	public static final String TITLE = "title";

	public static final String USER_NAME = "user-name";

	public UserDisplayTerms(PortletRequest portletRequest) {
		super(portletRequest);

		description = ParamUtil.getString(portletRequest, DESCRIPTION);
		groupId = ParamUtil.getLong(portletRequest, GROUP_ID);
		title = ParamUtil.getString(portletRequest, TITLE);
		userName = ParamUtil.getString(portletRequest, USER_NAME);
	}

	public String getDescription() {
		return description;
	}

	public long getGroupId() {
		return groupId;
	}

	public String getTitle() {
		return title;
	}

	public String getUserName() {
		return userName;
	}

	protected String description;
	protected long groupId;
	protected String title;
	protected String userName;

}