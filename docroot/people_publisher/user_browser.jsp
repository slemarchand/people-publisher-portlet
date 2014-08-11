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

<%@ include file="/people_publisher/init.jsp" %>

<%
long groupId = ParamUtil.getLong(request, "groupId");
long[] selectedGroupIds = StringUtil.split(ParamUtil.getString(request, "selectedGroupIds"), 0L);
long refererUserId = ParamUtil.getLong(request, "refererUserId");
String eventName = ParamUtil.getString(request, "eventName", liferayPortletResponse.getNamespace() + "selectUser");

PortletURL portletURL = renderResponse.createRenderURL();

portletURL.setParameter("mvcPath", "/people_publisher/user_browser.jsp");
portletURL.setParameter("refererUserId", String.valueOf(refererUserId));
portletURL.setParameter("eventName", eventName);

request.setAttribute("view.jsp-portletURL", portletURL);
%>

<div class="user-search">

	<aui:form action="<%= portletURL %>" method="post" name="selectUserFm">

		<liferay-ui:search-container
			searchContainer="<%= new UserSearch(renderRequest, portletURL) %>"
		>
			<aui:nav-bar>
				<aui:nav-bar-search cssClass="pull-right">
					<%@ include file="/people_publisher/user_search.jspf" %>
				</aui:nav-bar-search>
			</aui:nav-bar>

			<%
			UserSearchTerms searchTerms = (UserSearchTerms)searchContainer.getSearchTerms();
			%>

			<liferay-ui:search-container-results>
				<%@ include file="/people_publisher/user_search_results.jspf" %>
			</liferay-ui:search-container-results>

			<div class="separator"><!-- --></div>

			<liferay-ui:search-container-row
				className="com.liferay.portal.model.User"
				escapedModel="<%= true %>"
				modelVar="curUser"
			>

				<liferay-ui:search-container-column-text
					name="full-name"
					value="<%= HtmlUtil.escape(curUser.getFullName()) %>"
				/>

				<liferay-ui:search-container-column-text
					name="screen-name"
					value="<%= HtmlUtil.escape(curUser.getScreenName()) %>"
				/>

				<liferay-ui:search-container-column-text
					name="email-address"
					value="<%= HtmlUtil.stripHtml(curUser.getEmailAddress()) %>"
				/>

				<liferay-ui:search-container-column-text>
					<c:if test="<%= curUser.getUserId() != refererUserId %>">

						<%
						Map<String, Object> data = new HashMap<String, Object>();

						data.put("userid", curUser.getUserId());
						%>

						<aui:button cssClass="selector-button" data="<%= data %>" value="choose" />
					</c:if>
				</liferay-ui:search-container-column-text>

			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator />
		</liferay-ui:search-container>
	</aui:form>
</div>

<aui:script use="aui-base">
	var Util = Liferay.Util;

	A.one('#<portlet:namespace />selectUserFm').delegate(
		'click',
		function(event) {
			var result = Util.getAttributes(event.currentTarget, 'data-');

			Util.getOpener().Liferay.fire('<%= HtmlUtil.escapeJS(eventName) %>', result);

			Util.getWindow().hide();
		},
		'.selector-button'
	);
</aui:script>