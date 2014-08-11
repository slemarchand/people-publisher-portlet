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
PortletURL configurationRenderURL = (PortletURL)request.getAttribute("configuration.jsp-configurationRenderURL");
String redirect = (String)request.getAttribute("configuration.jsp-redirect");
String rootPortletId = (String)request.getAttribute("configuration.jsp-rootPortletId");
String selectScope = (String)request.getAttribute("configuration.jsp-selectScope");
String selectStyle = (String)request.getAttribute("configuration.jsp-selectStyle");
String eventName = "_" + HtmlUtil.escapeJS(portletResource) + "_selectUser";
%>

<liferay-ui:tabs
	formName="fm"
	names="user-selection,display-settings"
	param="tabs2"
	refresh="<%= false %>"
>
	<liferay-ui:section>
		<liferay-ui:error-marker key="errorSection" value="user-selection" />

		<%= selectStyle %>

		<aui:fieldset cssClass="hidden" label="scope">
			<%= selectScope %>
		</aui:fieldset>

		<aui:fieldset label="users">

			<%
			List<User> users = peoplePublisher.getUsers(renderRequest, portletPreferences, userXmls, true);
			%>

			<liferay-ui:search-container
				emptyResultsMessage="no-users-selected"
				iteratorURL="<%= configurationRenderURL %>"
				total="<%= users.size() %>"
			>
				<liferay-ui:search-container-results
					results="<%= users.subList(searchContainer.getStart(), searchContainer.getResultEnd()) %>"
				/>

				<liferay-ui:search-container-row
					className="com.liferay.portal.model.User"
					escapedModel="<%= true %>"
					keyProperty="userId"
					modelVar="curUser"
				>

					<liferay-ui:search-container-column-text name="full-name">
						<div class="portrait" style='display: inline-block; min-height: 1em; min-width: 2em; background-repeat: no-repeat; background-size: contain; background-psoition: left-center; background-image: url("<%= curUser.getPortraitURL(themeDisplay) %>")' >&nbsp;</div><%= curUser.getFullName() %>
					</liferay-ui:search-container-column-text>

					<liferay-ui:search-container-column-text
						name="screen-name"
						value="<%= curUser.getScreenName() %>"
					/>

					<liferay-ui:search-container-column-text
						name="email-address"
						value="<%= curUser.getEmailAddress() %>"
					/>

					<liferay-ui:search-container-column-jsp
						align="right"
						path="/people_publisher/user_selection_action.jsp"
					/>
				</liferay-ui:search-container-row>

				<liferay-ui:search-iterator paginate="<%= total > SearchContainer.DEFAULT_DELTA %>" />
			</liferay-ui:search-container>

			<c:if test='<%= SessionMessages.contains(renderRequest, "deletedMissingUsers") %>'>
				<div class="alert alert-info">
					<liferay-ui:message key="the-selected-users-have-been-removed-from-the-list-because-they-do-not-exist-anymore" />
				</div>
			</c:if>

			<%
			String portletId = portletResource;

			PortletURL userBrowserURL = PortletURLFactoryUtil.create(request, PortletKeys.PEOPLE_PUBLISHER, PortalUtil.getControlPanelPlid(company.getCompanyId()), PortletRequest.RENDER_PHASE);

			userBrowserURL.setParameter("mvcPath", "/people_publisher/user_browser.jsp");
			userBrowserURL.setParameter("eventName", eventName);
			userBrowserURL.setPortletMode(PortletMode.VIEW);
			userBrowserURL.setWindowState(LiferayWindowState.POP_UP);

			Map<String, Object> data = new HashMap<String, Object>();

			data.put("href", userBrowserURL.toString());
			data.put("title", LanguageUtil.format(pageContext, "select-x", LanguageUtil.get(pageContext, "user")));
			%>

			<div class="select-user-selector">
				<div class="lfr-meta-actions edit-controls">

					<aui:button
						cssClass="user-selector"
						data="<%= data %>"
						icon="add"
						iconAlign="left"
						id='<%= portletId + "userSelector" %>'
						value="select"
					/>

				</div>
			</div>

		</aui:fieldset>
	</liferay-ui:section>
	<liferay-ui:section>
		<liferay-ui:error-marker key="errorSection" value="display-settings" />

		<%@ include file="/people_publisher/display_settings.jspf" %>
	</liferay-ui:section>
</liferay-ui:tabs>

<aui:button-row>
	<aui:button onClick='<%= renderResponse.getNamespace() + "saveSelectBoxes();" %>' type="submit" />
</aui:button-row>

<aui:script use="aui-base">
	function selectUser(userId) {
		document.<portlet:namespace />fm.<portlet:namespace /><%= Constants.CMD %>.value = 'add-selection';
		document.<portlet:namespace />fm.<portlet:namespace />userId.value = userId;
		submitForm(document.<portlet:namespace />fm);
	}

	A.getBody().delegate(
		'click',
		function(event) {
			event.preventDefault();

			var currentTarget = event.currentTarget;

			Liferay.Util.selectEntity(
				{
					dialog: {
						constrain: true,
						modal: true,
						width: 900
					},
					eventName: '<%= eventName %>',
					id: '<%= eventName %>' + currentTarget.attr('id'),
					title: currentTarget.attr('data-title'),
					uri: currentTarget.attr('data-href')
				},
				function(event) {
					selectUser(event.userid);
				}
			);
		},
		'button.user-selector'
	);
</aui:script>