<%--
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
--%>

<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.portal.NoSuchModelException"  %><%@

page import="com.slemarchand.peoplepublisher.DuplicateQueryRuleException" %><%@

page import="com.slemarchand.peoplepublisher.model.PeopleQuery" %><%@
page import="com.slemarchand.peoplepublisher.model.PeopleResults"  %><%@
page import="com.slemarchand.peoplepublisher.search.UserDisplayTerms"  %><%@
page import="com.slemarchand.peoplepublisher.search.UserSearch" %><%@
page import="com.slemarchand.peoplepublisher.search.UserSearchTerms" %><%@
page import="com.slemarchand.peoplepublisher.template.DefaultDDMTemplatesManager" %><%@
page import="com.slemarchand.peoplepublisher.util.PeoplePublisher" %><%@
page import="com.slemarchand.peoplepublisher.util.PeoplePublisherUtil" %>

<%
String portletResource = ParamUtil.getString(request, "portletResource");

String selectionStyle = GetterUtil.getString(portletPreferences.getValue("selectionStyle", null), "manual");

String displayStyle = GetterUtil.getString(portletPreferences.getValue("displayStyle", DISPLAY_STYLE_DEFAULT));
long displayStyleGroupId = GetterUtil.getLong(portletPreferences.getValue("displayStyleGroupId", null), themeDisplay.getScopeGroupId());

PeoplePublisher peoplePublisher = PeoplePublisherUtil.getPeoplePublisher();

long[] groupIds = peoplePublisher.getGroupIds(portletPreferences, scopeGroupId, layout);

PeopleQuery userQuery = new PeopleQuery();

String ddmStructureDisplayFieldValue = StringPool.BLANK;
String ddmStructureFieldLabel = StringPool.BLANK;
String ddmStructureFieldName = StringPool.BLANK;
Serializable ddmStructureFieldValue = null;

if (selectionStyle.equals("dynamic")) {

	if (!ArrayUtil.contains(groupIds, scopeGroupId)) {
		userQuery = peoplePublisher.getPeopleQuery(portletPreferences, ArrayUtil.append(groupIds, scopeGroupId));
	}
	else {
		userQuery = peoplePublisher.getPeopleQuery(portletPreferences, groupIds);
	}
}

String orderByColumn1 = GetterUtil.getString(portletPreferences.getValue("orderByColumn1", "modifiedDate"));
String orderByColumn2 = GetterUtil.getString(portletPreferences.getValue("orderByColumn2", "title"));
String orderByType1 = GetterUtil.getString(portletPreferences.getValue("orderByType1", "DESC"));
String orderByType2 = GetterUtil.getString(portletPreferences.getValue("orderByType2", "ASC"));

int delta = GetterUtil.getInteger(portletPreferences.getValue("delta", null), SearchContainer.DEFAULT_DELTA);

String paginationType = GetterUtil.getString(portletPreferences.getValue("paginationType", "none"));

userQuery.setPaginationType(paginationType);

String[] userXmls = portletPreferences.getValues("userXml", new String[0]);

boolean showPortletWithNoResults = false;

Map<String, PortletURL> addPortletURLs = null;

Format dateFormatDate = FastDateFormatFactoryUtil.getDate(locale, timeZone);
%>

<%!private static String DISPLAY_STYLE_DEFAULT = "basic"; %>