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

package com.slemarchand.peoplepublisher.template;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.template.TemplateVariableGroup;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.util.PortalUtil;
import com.slemarchand.peoplepublisher.util.PortletKeys;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Sebastien Le Marchand
 */
public class PeoplePublisherPortletDisplayTemplateHandler
	extends BasePortletDisplayTemplateHandler {

	private static final String PEOPLE_PUBLISHER_DISPLAY_TEMPLATES_CONFIG = "com/slemarchand/peoplepublisher/dependencies/portlet-display-templates.xml";

	@Override
	public String getClassName() {
		return User.class.getName();
	}

	@Override
	public String getName(Locale locale) {
		String portletTitle = PortalUtil.getPortletTitle(
			PortletKeys.PEOPLE_PUBLISHER, locale);

		return portletTitle.concat(StringPool.SPACE).concat(
			LanguageUtil.get(locale, "template"));
	}

	@Override
	public String getResourceName() {
		return PortletKeys.PEOPLE_PUBLISHER;
	}

	@Override
	public Map<String, TemplateVariableGroup> getTemplateVariableGroups(
			long classPK, String language, Locale locale)
		throws Exception {

		Map<String, TemplateVariableGroup> templateVariableGroups =
			super.getTemplateVariableGroups(classPK, language, locale);

		String[] restrictedVariables = getRestrictedVariables(language);

		TemplateVariableGroup peoplePublisherUtilTemplateVariableGroup =
			new TemplateVariableGroup(
				"people-publisher-util", restrictedVariables);

		templateVariableGroups.put(
			"people-publisher-util", peoplePublisherUtilTemplateVariableGroup);

		TemplateVariableGroup fieldsTemplateVariableGroup =
			templateVariableGroups.get("fields");

		fieldsTemplateVariableGroup.empty();

		fieldsTemplateVariableGroup.addCollectionVariable(
			"users", List.class, "users", "user", User.class, "curUser",
			"getFullName()");

		fieldsTemplateVariableGroup.addVariable(
			"user", User.class, "user", "getFullName()");

		TemplateVariableGroup userServicesTemplateVariableGroup =
			new TemplateVariableGroup("user-services", restrictedVariables);

		userServicesTemplateVariableGroup.setAutocompleteEnabled(false);

		userServicesTemplateVariableGroup.addServiceLocatorVariables(
			UserLocalService.class);

		templateVariableGroups.put(
			userServicesTemplateVariableGroup.getLabel(),
			userServicesTemplateVariableGroup);

		return templateVariableGroups;
	}

	@Override
	protected String getTemplatesConfigPath() {
		return PeoplePublisherPortletDisplayTemplateHandler.PEOPLE_PUBLISHER_DISPLAY_TEMPLATES_CONFIG;
	}

}