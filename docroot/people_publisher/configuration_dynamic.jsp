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

		<liferay-ui:panel-container extended="<%= true %>" id="peoplePublisherDynamicSelectionUserSelectionPanelContainer" persistState="<%= true %>">
			<liferay-ui:panel collapsible="<%= true %>" extended="<%= true %>" id="peoplePublisherSourcePanel" persistState="<%= true %>" title="source">
				<aui:fieldset label="scope">
					<%= selectScope %>
				</aui:fieldset>
			</liferay-ui:panel>

			<%--Not yet implemented --%>

			<%--
			<liferay-ui:panel collapsible="<%= true %>" extended="<%= true %>" id="peoplePublisherQueryRulesPanelContainer" persistState="<%= true %>" title="filter[action]">
				
			</liferay-ui:panel>
			--%>

			<%--Not yet implemented --%>

			<%--

			<liferay-ui:panel collapsible="<%= true %>" extended="<%= true %>" id="peoplePublisherOrderingPanel" persistState="<%= true %>" title="ordering">
				<aui:fieldset>
					<span class="field-row">
						<aui:select inlineField="<%= true %>" inlineLabel="left" label="order-by" name="preferences--orderByColumn1--">
							<aui:option label="title" selected='<%= orderByColumn1.equals("title") %>' />
							<aui:option label="create-date" selected='<%= orderByColumn1.equals("createDate") %>' value="createDate" />
							<aui:option label="modified-date" selected='<%= orderByColumn1.equals("modifiedDate") %>' value="modifiedDate" />
							<aui:option label="publish-date" selected='<%= orderByColumn1.equals("publishDate") %>' value="publishDate" />
							<aui:option label="expiration-date" selected='<%= orderByColumn1.equals("expirationDate") %>' value="expirationDate" />
							<aui:option label="priority" selected='<%= orderByColumn1.equals("priority") %>' value="priority" />
						</aui:select>

						<aui:select inlineField="<%= true %>" label="" name="preferences--orderByType1--">
							<aui:option label="ascending" selected='<%= orderByType1.equals("ASC") %>' value="ASC" />
							<aui:option label="descending" selected='<%= orderByType1.equals("DESC") %>' value="DESC" />
						</aui:select>
					</span>

					<span class="field-row">
						<aui:select inlineField="<%= true %>" inlineLabel="left" label="and-then-by" name="preferences--orderByColumn2--">
							<aui:option label="title" selected='<%= orderByColumn2.equals("title") %>' />
							<aui:option label="create-date" selected='<%= orderByColumn2.equals("createDate") %>' value="createDate" />
							<aui:option label="modified-date" selected='<%= orderByColumn2.equals("modifiedDate") %>' value="modifiedDate" />
							<aui:option label="publish-date" selected='<%= orderByColumn2.equals("publishDate") %>' value="publishDate" />
							<aui:option label="expiration-date" selected='<%= orderByColumn2.equals("expirationDate") %>' value="expirationDate" />
							<aui:option label="priority" selected='<%= orderByColumn2.equals("priority") %>' value="priority" />

						</aui:select>

						<aui:select inlineField="<%= true %>" label="" name="preferences--orderByType2--">
							<aui:option label="ascending" selected='<%= orderByType2.equals("ASC") %>' value="ASC" />
							<aui:option label="descending" selected='<%= orderByType2.equals("DESC") %>' value="DESC" />
						</aui:select>
					</span>

				</aui:fieldset>
			</liferay-ui:panel>
			--%>
		</liferay-ui:panel-container>
	</liferay-ui:section>

	<liferay-ui:section>
		<liferay-ui:error-marker key="errorSection" value="display-settings" />

		<%@ include file="/people_publisher/display_settings.jspf" %>
	</liferay-ui:section>

</liferay-ui:tabs>

<aui:button-row>
	<aui:button onClick='<%= renderResponse.getNamespace() + "saveSelectBoxes();" %>' type="submit" />
</aui:button-row>