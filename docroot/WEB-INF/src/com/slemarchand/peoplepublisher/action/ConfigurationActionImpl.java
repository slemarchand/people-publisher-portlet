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

package com.slemarchand.peoplepublisher.action;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Layout;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.util.DDMUtil;
import com.slemarchand.peoplepublisher.DuplicateQueryRuleException;
import com.slemarchand.peoplepublisher.model.UserQueryRule;
import com.slemarchand.peoplepublisher.util.PeoplePublisher;
import com.slemarchand.peoplepublisher.util.PeoplePublisherImpl;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 * @author Sebastien Le Marchand
 */
public class ConfigurationActionImpl extends DefaultConfigurationAction {

	PeoplePublisherImpl peoplePublisher = new PeoplePublisherImpl();

	@Override
	public void processAction(
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		String portletResource = ParamUtil.getString(
			actionRequest, "portletResource");

		PortletPreferences preferences = actionRequest.getPreferences();

		if (cmd.equals(Constants.TRANSLATE)) {
			super.processAction(portletConfig, actionRequest, actionResponse);
		}
		else if (cmd.equals(Constants.UPDATE)) {
			try {
				String selectionStyle = getParameter(
					actionRequest, "selectionStyle");

				if (selectionStyle.equals("dynamic")) {
					updateQueryLogic(actionRequest, preferences);
				}

				super.processAction(
					portletConfig, actionRequest, actionResponse);
			}
			catch (DuplicateQueryRuleException e) {
				SessionErrors.add(actionRequest, e.getClass(), e);
			}
		}
		else {
			if (cmd.equals("add-scope")) {
				addScope(actionRequest, preferences);
			}
			else if (cmd.equals("add-selection")) {
				peoplePublisher.addSelection(
					actionRequest, preferences, portletResource);
			}
			else if (cmd.equals("move-selection-down")) {
				moveSelectionDown(actionRequest, preferences);
			}
			else if (cmd.equals("move-selection-up")) {
				moveSelectionUp(actionRequest, preferences);
			}
			else if (cmd.equals("remove-selection")) {
				removeSelection(actionRequest, preferences);
			}
			else if (cmd.equals("remove-scope")) {
				removeScope(actionRequest, preferences);
			}
			else if (cmd.equals("select-scope")) {
				setScopes(actionRequest, preferences);
			}
			else if (cmd.equals("selection-style")) {
				setSelectionStyle(actionRequest, preferences);
			}

			if (SessionErrors.isEmpty(actionRequest)) {
				preferences.store();

				SessionMessages.add(
					actionRequest,
					PortalUtil.getPortletId(actionRequest) +
						SessionMessages.KEY_SUFFIX_REFRESH_PORTLET,
					portletResource);

				SessionMessages.add(
					actionRequest,
					PortalUtil.getPortletId(actionRequest) +
						SessionMessages.KEY_SUFFIX_UPDATED_CONFIGURATION);
			}

			String redirect = PortalUtil.escapeRedirect(
				ParamUtil.getString(actionRequest, "redirect"));

			if (Validator.isNotNull(redirect)) {
				actionResponse.sendRedirect(redirect);
			}
		}
	}

	@Override
	public void serveResource(
			PortletConfig portletConfig, ResourceRequest resourceRequest,
			ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String cmd = ParamUtil.getString(resourceRequest, Constants.CMD);

		if (!cmd.equals("getFieldValue")) {
			return;
		}

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			resourceRequest);

		long structureId = ParamUtil.getLong(resourceRequest, "structureId");

		Fields fields = (Fields)serviceContext.getAttribute(
			Fields.class.getName() + structureId);

		if (fields == null) {
			String fieldsNamespace = ParamUtil.getString(
				resourceRequest, "fieldsNamespace");

			fields = DDMUtil.getFields(
				structureId, fieldsNamespace, serviceContext);
		}

		String fieldName = ParamUtil.getString(resourceRequest, "name");

		Field field = fields.get(fieldName);

		Serializable fieldValue = field.getValue(themeDisplay.getLocale(), 0);

		DDMStructure ddmStructure = field.getDDMStructure();

		String type = ddmStructure.getFieldType(fieldName);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		Serializable displayValue = DDMUtil.getDisplayFieldValue(
			themeDisplay, fieldValue, type);

		jsonObject.put("displayValue", String.valueOf(displayValue));

		if (fieldValue instanceof Boolean) {
			jsonObject.put("value", (Boolean)fieldValue);
		}
		else if (fieldValue instanceof Date) {
			DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
				"yyyyMMddHHmmss");

			jsonObject.put("value", dateFormat.format(fieldValue));
		}
		else if (fieldValue instanceof Double) {
			jsonObject.put("value", (Double)fieldValue);
		}
		else if (fieldValue instanceof Float) {
			jsonObject.put("value", (Float)fieldValue);
		}
		else if (fieldValue instanceof Integer) {
			jsonObject.put("value", (Integer)fieldValue);
		}
		else if (fieldValue instanceof Number) {
			jsonObject.put("value", String.valueOf(fieldValue));
		}
		else {
			jsonObject.put("value", (String)fieldValue);
		}

		resourceResponse.setContentType(ContentTypes.APPLICATION_JSON);

		PortletResponseUtil.write(resourceResponse, jsonObject.toString());
	}

	protected void addScope(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String[] scopeIds = preferences.getValues(
			"scopeIds",
			new String[] {
				PeoplePublisher.SCOPE_ID_GROUP_PREFIX + GroupConstants.DEFAULT
			});

		String scopeId = ParamUtil.getString(actionRequest, "scopeId");

		checkPermission(actionRequest, scopeId);

		if (!ArrayUtil.contains(scopeIds, scopeId)) {
			scopeIds = ArrayUtil.append(scopeIds, scopeId);
		}

		preferences.setValues("scopeIds", scopeIds);
	}

	protected void checkPermission(ActionRequest actionRequest, String scopeId)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Layout layout = themeDisplay.getLayout();

		if (!peoplePublisher.isScopeIdSelectable(
				themeDisplay.getPermissionChecker(), scopeId,
				themeDisplay.getCompanyGroupId(), layout)) {

			throw new PrincipalException();
		}
	}

	protected UserQueryRule getQueryRule(ActionRequest actionRequest, int queryRulesIndex) {
		 return new UserQueryRule(); 
	}

	protected void moveSelectionDown(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int userOrder = ParamUtil.getInteger(
			actionRequest, "userOrder");

		String[] manualEntries = preferences.getValues(
			"userXml", new String[0]);

		if ((userOrder >= (manualEntries.length - 1)) ||
			(userOrder < 0)) {

			return;
		}

		String temp = manualEntries[userOrder + 1];

		manualEntries[userOrder + 1] = manualEntries[userOrder];
		manualEntries[userOrder] = temp;

		preferences.setValues("userXml", manualEntries);
	}

	protected void moveSelectionUp(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int userOrder = ParamUtil.getInteger(
			actionRequest, "userOrder");

		String[] manualEntries = preferences.getValues(
			"userXml", new String[0]);

		if ((userOrder >= manualEntries.length) ||
			(userOrder <= 0)) {

			return;
		}

		String temp = manualEntries[userOrder - 1];

		manualEntries[userOrder - 1] = manualEntries[userOrder];
		manualEntries[userOrder] = temp;

		preferences.setValues("userXml", manualEntries);
	}

	protected void removeScope(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String[] scopeIds = preferences.getValues(
			"scopeIds",
			new String[] {
				PeoplePublisher.SCOPE_ID_GROUP_PREFIX + GroupConstants.DEFAULT
			});

		String scopeId = ParamUtil.getString(actionRequest, "scopeId");

		scopeIds = ArrayUtil.remove(scopeIds, scopeId);

		if (scopeId.startsWith(PeoplePublisher.SCOPE_ID_PARENT_GROUP_PREFIX)) {
			scopeId = scopeId.substring("Parent".length());

			scopeIds = ArrayUtil.remove(scopeIds, scopeId);
		}

		preferences.setValues("scopeIds", scopeIds);
	}

	protected void removeSelection(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int userOrder = ParamUtil.getInteger(
			actionRequest, "userOrder");

		String[] oldManualUsers = preferences.getValues(
			"userXml", new String[0]);

		if (userOrder >= oldManualUsers.length) {
			return;
		}

		String[] newManualUsers = new String[oldManualUsers.length -1];

		int i = 0;
		int j = 0;

		for (; i < oldManualUsers.length; i++) {
			if (i != userOrder) {
				newManualUsers[j++] = oldManualUsers[i];
			}
		}

		preferences.setValues("userXml", newManualUsers);
	}

	protected void setScopes(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String[] scopeIds = StringUtil.split(
			getParameter(actionRequest, "scopeIds"));

		preferences.setValues("scopeIds", scopeIds);
	}

	protected void setSelectionStyle(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String selectionStyle = getParameter(actionRequest, "selectionStyle");
		String displayStyle = getParameter(actionRequest, "displayStyle");

		preferences.setValue("selectionStyle", selectionStyle);

		if (selectionStyle.equals("manual") ||
			selectionStyle.equals("view-count")) {

			preferences.setValue("enableRss", String.valueOf(false));
			preferences.setValue("showQueryLogic", Boolean.FALSE.toString());

			preferences.reset("rssDelta");
			preferences.reset("rssDisplayStyle");
			preferences.reset("rssFormat");
			preferences.reset("rssName");
		}

		if (!selectionStyle.equals("view-count") &&
			displayStyle.equals("view-count-details")) {

			preferences.setValue("displayStyle", "full-content");
		}
	}
	
	protected void updateQueryLogic(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getSiteGroupId();

		int[] queryRulesIndexes = StringUtil.split(
			ParamUtil.getString(actionRequest, "queryLogicIndexes"), 0);

		int i = 0;

		List<UserQueryRule> queryRules = new ArrayList<UserQueryRule>();

		for (int queryRulesIndex : queryRulesIndexes) {
			UserQueryRule queryRule = getQueryRule(
				actionRequest, queryRulesIndex);

			validateQueryRule(userId, groupId, queryRules, queryRule);

			queryRules.add(queryRule);

			// NOT YET IMPLEMENTED
			
			/*
			setPreference(
				actionRequest, "queryContains" + i,
				String.valueOf(queryRule.isContains()));
			setPreference(
				actionRequest, "queryAndOperator" + i,
				String.valueOf(queryRule.isAndOperator()));
			setPreference(actionRequest, "queryName" + i, queryRule.getName());
			setPreference(
				actionRequest, "queryValues" + i, queryRule.getValues());
			 */
			
			i++;
		}

		// Clear previous preferences that are now blank

		String[] values = preferences.getValues(
			"queryValues" + i, new String[0]);

		while (values.length > 0) {
			setPreference(actionRequest, "queryContains" + i, StringPool.BLANK);
			setPreference(
				actionRequest, "queryAndOperator" + i, StringPool.BLANK);
			setPreference(actionRequest, "queryName" + i, StringPool.BLANK);
			setPreference(actionRequest, "queryValues" + i, new String[0]);

			i++;

			values = preferences.getValues("queryValues" + i, new String[0]);
		}
	}

	protected void validateQueryRule(
			long userId, long groupId, List<UserQueryRule> queryRules,
			UserQueryRule queryRule)
		throws Exception {

		if (queryRules.contains(queryRule)) {
			throw new DuplicateQueryRuleException(queryRule);
		}
	}

}