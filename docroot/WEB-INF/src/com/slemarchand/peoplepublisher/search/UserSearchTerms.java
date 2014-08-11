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

import com.liferay.portal.kernel.dao.search.DAOParamUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import javax.portlet.PortletRequest;

/**
 * @author Sebastien Le Marchand
 */
public class UserSearchTerms extends UserDisplayTerms {

	public UserSearchTerms(PortletRequest portletRequest) {
		super(portletRequest);

		description = DAOParamUtil.getString(portletRequest, DESCRIPTION);
		groupId = ParamUtil.getLong(portletRequest, GROUP_ID);
		title = DAOParamUtil.getString(portletRequest, TITLE);
		userName = DAOParamUtil.getString(portletRequest, USER_NAME);
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

}