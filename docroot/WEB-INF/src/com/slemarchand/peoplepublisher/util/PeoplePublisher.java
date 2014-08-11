package com.slemarchand.peoplepublisher.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;

import com.slemarchand.peoplepublisher.model.PeopleQuery;
import com.slemarchand.peoplepublisher.model.PeopleResults;

import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;
public interface PeoplePublisher {

	public static final String SCOPE_ID_CHILD_GROUP_PREFIX = "ChildGroup_";

	public static final String SCOPE_ID_GROUP_PREFIX = "Group_";

	public static final String SCOPE_ID_LAYOUT_PREFIX = "Layout_";

	public static final String SCOPE_ID_LAYOUT_UUID_PREFIX = "LayoutUuid_";

	public static final String SCOPE_ID_PARENT_GROUP_PREFIX = "ParentGroup_";

	public long[] getGroupIds(PortletPreferences portletPreferences, long scopeGroupId, Layout layout);

	public abstract PeopleQuery getPeopleQuery(PortletPreferences portletPreferences, long[] scopeGroupIds)
			throws PortalException, SystemException;

	public abstract String getScopeId(Group group, long scopeGroupId)
			throws PortalException, SystemException;

	public abstract List<User> getUsers(PortletRequest portletRequest, PortletPreferences portletPreferences, String[] userXmls,
			boolean deleteMissingUsers) throws Exception;

	public boolean isScopeIdSelectable(PermissionChecker permissionChecker, String scopeId,
			long companyGroupId, Layout layout) throws PortalException, SystemException;

	public abstract PeopleResults search(HttpServletRequest request, PeopleQuery q, int start,
			int end) throws Exception;
}