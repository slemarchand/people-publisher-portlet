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

package com.slemarchand.peoplepublisher.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.permission.GroupPermissionUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.comparator.UserLastNameComparator;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.sites.util.SitesUtil;
import com.slemarchand.peoplepublisher.model.PeopleQuery;
import com.slemarchand.peoplepublisher.model.PeopleResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Sebastien Le Marchand
 */
public class PeoplePublisherImpl implements PeoplePublisher {

	public static final String SCOPE_ID_CHILD_GROUP_PREFIX = "ChildGroup_";

	public static final String SCOPE_ID_GROUP_PREFIX = "Group_";

	public static final String SCOPE_ID_LAYOUT_PREFIX = "Layout_";

	public static final String SCOPE_ID_LAYOUT_UUID_PREFIX = "LayoutUuid_";

	public static final String SCOPE_ID_PARENT_GROUP_PREFIX = "ParentGroup_";

	public PeoplePublisherImpl() {
	}

	public void addAndStoreSelection(
			PortletRequest portletRequest, long selectedUserId,
			int userOrder)
		throws Exception {

		String referringPortletResource = ParamUtil.getString(
			portletRequest, "referringPortletResource");

		if (Validator.isNull(referringPortletResource)) {
			return;
		}

		String rootPortletId = PortletConstants.getRootPortletId(
			referringPortletResource);

		if (!rootPortletId.equals(com.slemarchand.peoplepublisher.util.PortletKeys.PEOPLE_PUBLISHER)) {
			return;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Layout layout = LayoutLocalServiceUtil.getLayout(
			themeDisplay.getRefererPlid());

		PortletPreferences portletPreferences =
			PortletPreferencesFactoryUtil.getStrictPortletSetup(
				layout, referringPortletResource);

		String selectionStyle = portletPreferences.getValue(
			"selectionStyle", "dynamic");

		if (selectionStyle.equals("dynamic")) {
			return;
		}

		addSelection(
			themeDisplay, portletPreferences, referringPortletResource,
			selectedUserId, userOrder);

		portletPreferences.store();
	}

	public void addSelection(
			PortletRequest portletRequest,
			PortletPreferences portletPreferences, String portletId)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long selectedUserId = ParamUtil.getLong(portletRequest, "userId");
		int selectedUserOrder = ParamUtil.getInteger(
			portletRequest, "userOrder");

		addSelection(
			themeDisplay, portletPreferences, portletId, selectedUserId,
			selectedUserOrder);
	}

	public void addSelection(
			ThemeDisplay themeDisplay, PortletPreferences portletPreferences,
			String portletId, long selectedUserId, int selectedUserOrder)
		throws Exception {

		User user = UserLocalServiceUtil.getUser(selectedUserId);

		String[] userXmls = portletPreferences.getValues(
			"userXml", new String[0]);

		String userXml = _getUserXml(user.getScreenName());

		if (!ArrayUtil.contains(userXmls, userXml)) {
			if (selectedUserOrder > -1) {
				userXmls[selectedUserOrder] = userXml;
			}
			else {
				userXmls = ArrayUtil.append(userXmls, userXml);
			}

			portletPreferences.setValues("userXml", userXmls);
		}
	}

	public long getGroupIdFromScopeId(
			String scopeId, long siteGroupId, boolean privateLayout)
		throws PortalException, SystemException {

		if (scopeId.startsWith(SCOPE_ID_CHILD_GROUP_PREFIX)) {
			String scopeIdSuffix = scopeId.substring(
				SCOPE_ID_CHILD_GROUP_PREFIX.length());

			long childGroupId = GetterUtil.getLong(scopeIdSuffix);

			Group childGroup = GroupLocalServiceUtil.getGroup(childGroupId);

			if (!childGroup.isChild(siteGroupId)) {
				throw new PrincipalException();
			}

			return childGroupId;
		}
		else if (scopeId.startsWith(SCOPE_ID_GROUP_PREFIX)) {
			String scopeIdSuffix = scopeId.substring(
				SCOPE_ID_GROUP_PREFIX.length());

			if (scopeIdSuffix.equals(GroupConstants.DEFAULT)) {
				return siteGroupId;
			}

			return GetterUtil.getLong(scopeIdSuffix);
		}
		else if (scopeId.startsWith(SCOPE_ID_LAYOUT_UUID_PREFIX)) {
			String layoutUuid = scopeId.substring(
				SCOPE_ID_LAYOUT_UUID_PREFIX.length());

			Layout scopeIdLayout =
				LayoutLocalServiceUtil.getLayoutByUuidAndGroupId(
					layoutUuid, siteGroupId, privateLayout);

			Group scopeIdGroup = null;

			if (scopeIdLayout.hasScopeGroup()) {
				scopeIdGroup = scopeIdLayout.getScopeGroup();
			}
			else {
				scopeIdGroup = GroupLocalServiceUtil.addGroup(
					PrincipalThreadLocal.getUserId(),
					GroupConstants.DEFAULT_PARENT_GROUP_ID,
					Layout.class.getName(), scopeIdLayout.getPlid(),
					GroupConstants.DEFAULT_LIVE_GROUP_ID,
					String.valueOf(scopeIdLayout.getPlid()), null, 0, true,
					GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, null, false,
					true, null);
			}

			return scopeIdGroup.getGroupId();
		}
		else if (scopeId.startsWith(SCOPE_ID_LAYOUT_PREFIX)) {

			// Legacy portlet preferences

			String scopeIdSuffix = scopeId.substring(
				SCOPE_ID_LAYOUT_PREFIX.length());

			long scopeIdLayoutId = GetterUtil.getLong(scopeIdSuffix);

			Layout scopeIdLayout = LayoutLocalServiceUtil.getLayout(
				siteGroupId, privateLayout, scopeIdLayoutId);

			Group scopeIdGroup = scopeIdLayout.getScopeGroup();

			return scopeIdGroup.getGroupId();
		}
		else if (scopeId.startsWith(SCOPE_ID_PARENT_GROUP_PREFIX)) {
			String scopeIdSuffix = scopeId.substring(
				SCOPE_ID_PARENT_GROUP_PREFIX.length());

			long parentGroupId = GetterUtil.getLong(scopeIdSuffix);

			Group parentGroup = GroupLocalServiceUtil.getGroup(parentGroupId);

			if (!SitesUtil.isContentSharingWithChildrenEnabled(parentGroup)) {
				throw new PrincipalException();
			}

			Group group = GroupLocalServiceUtil.getGroup(siteGroupId);

			if (!group.isChild(parentGroupId)) {
				throw new PrincipalException();
			}

			return parentGroupId;
		}
		else {
			throw new IllegalArgumentException("Invalid scope ID " + scopeId);
		}
	}

	@Override
	public long[] getGroupIds(
		PortletPreferences portletPreferences, long scopeGroupId,
		Layout layout) {

		String[] scopeIds = portletPreferences.getValues(
			"scopeIds", new String[] {SCOPE_ID_GROUP_PREFIX + scopeGroupId});

		List<Long> groupIds = new ArrayList<Long>();

		for (String scopeId : scopeIds) {
			try {
				long groupId = getGroupIdFromScopeId(
					scopeId, scopeGroupId, layout.isPrivateLayout());

				groupIds.add(groupId);
			}
			catch (Exception e) {
				continue;
			}
		}

		return ArrayUtil.toLongArray(groupIds);
	}

	@Override
	public PeopleQuery getPeopleQuery(
			PortletPreferences portletPreferences, long[] scopeGroupIds)
		throws PortalException, SystemException {

		PeopleQuery peopleQuery = new PeopleQuery();

		// NOT YET IMPLEMENTED

		return peopleQuery;
	}

	@Override
	public String getScopeId(Group group, long scopeGroupId)
		throws PortalException, SystemException {

		String key = null;

		if (group.isLayout()) {
			Layout layout = LayoutLocalServiceUtil.getLayout(
				group.getClassPK());

			key = SCOPE_ID_LAYOUT_UUID_PREFIX + layout.getUuid();
		}
		else if (group.isLayoutPrototype() ||
				 (group.getGroupId() == scopeGroupId)) {

			key = SCOPE_ID_GROUP_PREFIX + GroupConstants.DEFAULT;
		}
		else {
			Group scopeGroup = GroupLocalServiceUtil.getGroup(scopeGroupId);

			if (scopeGroup.hasAncestor(group.getGroupId()) &&
				SitesUtil.isContentSharingWithChildrenEnabled(group)) {

				key = SCOPE_ID_PARENT_GROUP_PREFIX + group.getGroupId();
			}
			else if (group.hasAncestor(scopeGroup.getGroupId())) {
				key = SCOPE_ID_CHILD_GROUP_PREFIX + group.getGroupId();
			}
			else {
				key = SCOPE_ID_GROUP_PREFIX + group.getGroupId();
			}
		}

		return key;
	}

	@Override
	public List<User> getUsers(
			PortletRequest portletRequest,
			PortletPreferences portletPreferences, String[] userXmls,
			boolean deleteMissingUsers)
		throws Exception {

		long companyId = PortalUtil.getCompanyId(portletRequest);

		List<User> users = new ArrayList<User>();

		List<String> missingUserScreenNames = new ArrayList<String>();

		for (String userXml : userXmls) {
			Document document = SAXReaderUtil.read(userXml);

			Element rootElement = document.getRootElement();

			String screenName = rootElement.elementText("screen-name");

			User user = null;

			user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);

			if (user == null) {
				if (deleteMissingUsers) {
					missingUserScreenNames.add(screenName);
				}

				continue;
			}

			users.add(user);
		}

		if (deleteMissingUsers) {
			removeAndStoreSelection(
				missingUserScreenNames, portletPreferences);

			if (!missingUserScreenNames.isEmpty()) {
				SessionMessages.add(
					portletRequest, "deletedMissingUsers",
					missingUserScreenNames);
			}
		}

		return users;
	}

	@Override
	public boolean isScopeIdSelectable(
			PermissionChecker permissionChecker, String scopeId,
			long companyGroupId, Layout layout)
		throws PortalException, SystemException {

		long groupId = getGroupIdFromScopeId(
			scopeId, layout.getGroupId(), layout.isPrivateLayout());

		if (scopeId.startsWith(SCOPE_ID_CHILD_GROUP_PREFIX)) {
			Group group = GroupLocalServiceUtil.getGroup(groupId);

			if (!group.hasAncestor(layout.getGroupId())) {
				return false;
			}
		}
		else if (scopeId.startsWith(SCOPE_ID_PARENT_GROUP_PREFIX)) {
			Group siteGroup = layout.getGroup();

			if (!siteGroup.hasAncestor(groupId)) {
				return false;
			}

			Group group = GroupLocalServiceUtil.getGroup(groupId);

			if (SitesUtil.isContentSharingWithChildrenEnabled(group)) {
				return true;
			}

			if (!PrefsPropsUtil.getBoolean(
					layout.getCompanyId(),
					PropsKeys.
					SITES_CONTENT_SHARING_THROUGH_ADMINISTRATORS_ENABLED)) {

				return false;
			}

			return GroupPermissionUtil.contains(
				permissionChecker, groupId, ActionKeys.UPDATE);
		}
		else if (groupId != companyGroupId) {
			return GroupPermissionUtil.contains(
				permissionChecker, groupId, ActionKeys.UPDATE);
		}

		return true;
	}

	public void removeAndStoreSelection(
			List<String> screenNames, PortletPreferences portletPreferences)
		throws Exception {

		if (screenNames.size() == 0) {
			return;
		}

		String[] userXmls = portletPreferences.getValues(
			"userXml", new String[0]);

		List<String> userXmlsList = ListUtil.fromArray(userXmls);

		Iterator<String> itr = userXmlsList.iterator();

		while (itr.hasNext()) {
			String userXml = itr.next();

			Document document = SAXReaderUtil.read(userXml);

			Element rootElement = document.getRootElement();

			String screenName = rootElement.elementText("screen-name");

			if (screenNames.contains(screenName)) {
				itr.remove();
			}
		}

		portletPreferences.setValues(
			"userXml",
			userXmlsList.toArray(new String[userXmlsList.size()]));

		portletPreferences.store();
	}

	@Override
	public PeopleResults search(
			HttpServletRequest request, PeopleQuery q, int start, int end)
		throws Exception {

		Company company = PortalUtil.getCompany(request);

		String keywords = null;

		LinkedHashMap<String, Object> params = _getSearchParams(company, q);

		List<User> users = UserLocalServiceUtil.search(company.getCompanyId(), keywords, WorkflowConstants.STATUS_APPROVED, params, q.getStart(), q.getEnd(), _getObc(company, q));

		int total = UserLocalServiceUtil.searchCount(company.getCompanyId(), keywords, WorkflowConstants.STATUS_APPROVED, params);

		PeopleResults results = new PeopleResults();
		results.setUsers(users);
		results.setTotal(total);

		return results;
	}

	private OrderByComparator _getObc(Company company, PeopleQuery q) throws PortalException, SystemException {

		OrderByComparator obc = new UserLastNameComparator();

		return obc;
	}

	private LinkedHashMap<String, Object> _getSearchParams(Company company, PeopleQuery q) throws PortalException, SystemException {
		LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();

		Long[] groupIds = ArrayUtil.toArray(q.getGroupIds());

		if (!ArrayUtil.contains(q.getGroupIds(), company.getGroup().getGroupId())) {
			/*
			for (int i = 0; i < groupIds.length; i++) {
				long groupId = groupIds[i];
				
				Group group = GroupLocalServiceUtil.getGroup(groupId);
				
				//group.get
				
				groupIds[i] = groupId;
			}
			*/
			params.put("usersGroups", groupIds);
			params.put("inherit", true);
		}

		return params;
	}

	private String _getUserXml(String screenName) {

		String xml = null;

		try {
			Document document = SAXReaderUtil.createDocument(StringPool.UTF8);

			Element userElement = document.addElement("user");

			Element screenNameElement = userElement.addElement("screen-name");

			screenNameElement.addText(screenName);

			xml = document.formattedString(StringPool.BLANK);
		}
		catch (IOException ioe) {
			if (_log.isWarnEnabled()) {
				_log.warn(ioe);
			}
		}

		return xml;
	}

	private static Log _log = LogFactoryUtil.getLog(PeoplePublisherImpl.class);
}