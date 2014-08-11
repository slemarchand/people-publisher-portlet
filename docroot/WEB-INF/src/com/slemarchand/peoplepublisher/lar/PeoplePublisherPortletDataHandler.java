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

package com.slemarchand.peoplepublisher.lar;

import com.liferay.portal.NoSuchGroupException;
import com.liferay.portal.kernel.lar.DataLevel;
import com.liferay.portal.kernel.lar.DefaultConfigurationPortletDataHandler;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;

import com.slemarchand.peoplepublisher.util.PeoplePublisher;
import com.slemarchand.peoplepublisher.util.PeoplePublisherUtil;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.portlet.PortletPreferences;

/**
 * @author Sebastien Le Marchand
 */
public class PeoplePublisherPortletDataHandler
	extends DefaultConfigurationPortletDataHandler {

	public PeoplePublisherPortletDataHandler() {
		setDataLevel(DataLevel.PORTLET_INSTANCE);
		setPublishToLiveByDefault(true);
	}

	@Override
	protected PortletPreferences doProcessExportPortletPreferences(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		return updateExportPortletPreferences(
			portletDataContext, portletId, portletPreferences);
	}

	@Override
	protected PortletPreferences doProcessImportPortletPreferences(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		return updateImportPortletPreferences(
			portletDataContext, portletId, portletPreferences);
	}

	protected PortletPreferences updateExportPortletPreferences(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		Enumeration<String> enu = portletPreferences.getNames();

		while (enu.hasMoreElements()) {
			String name = enu.nextElement();

			if (name.equals("scopeIds")) {
				updateExportScopeIds(
					portletDataContext, portletPreferences, name,
					portletDataContext.getPlid());
			}
		}

		return portletPreferences;
	}

	protected void updateExportScopeIds(
			PortletDataContext portletDataContext,
			PortletPreferences portletPreferences, String key, long plid)
		throws Exception {

		String[] oldValues = portletPreferences.getValues(key, null);

		if (oldValues == null) {
			return;
		}

		Layout layout = LayoutLocalServiceUtil.getLayout(plid);

		String companyGroupScopeId =
			PeoplePublisher.SCOPE_ID_GROUP_PREFIX +
				portletDataContext.getCompanyGroupId();

		String[] newValues = new String[oldValues.length];

		for (int i = 0; i < oldValues.length; i++) {
			String oldValue = oldValues[i];

			if (oldValue.startsWith(PeoplePublisher.SCOPE_ID_GROUP_PREFIX)) {
				newValues[i] = StringUtil.replace(
					oldValue, companyGroupScopeId,
					"[$COMPANY_GROUP_SCOPE_ID$]");
			}
			else if (oldValue.startsWith(
						PeoplePublisher.SCOPE_ID_LAYOUT_PREFIX)) {

				// Legacy preferences

				String scopeIdSuffix = oldValue.substring(
					PeoplePublisher.SCOPE_ID_LAYOUT_PREFIX.length());

				long scopeIdLayoutId = GetterUtil.getLong(scopeIdSuffix);

				Layout scopeIdLayout = LayoutLocalServiceUtil.getLayout(
					layout.getGroupId(), layout.isPrivateLayout(),
					scopeIdLayoutId);

				newValues[i] =
					PeoplePublisher.SCOPE_ID_LAYOUT_UUID_PREFIX +
						scopeIdLayout.getUuid();
			}
			else {
				newValues[i] = oldValue;
			}
		}

		portletPreferences.setValues(key, newValues);
	}

	protected PortletPreferences updateImportPortletPreferences(
			PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences)
		throws Exception {

		Company company = CompanyLocalServiceUtil.getCompanyById(
			portletDataContext.getCompanyId());

		Group companyGroup = company.getGroup();

		Enumeration<String> enu = portletPreferences.getNames();

		while (enu.hasMoreElements()) {
			String name = enu.nextElement();

			if (name.equals("scopeIds")) {
				updateImportScopeIds(
					portletPreferences, name, companyGroup.getGroupId(),
					portletDataContext.getPlid());
			}
		}

		return portletPreferences;
	}

	protected void updateImportScopeIds(
			PortletPreferences portletPreferences, String key,
			long companyGroupId, long plid)
		throws Exception {

		String[] oldValues = portletPreferences.getValues(key, null);

		if (oldValues == null) {
			return;
		}

		Layout layout = LayoutLocalServiceUtil.getLayout(plid);

		String companyGroupScopeId =
			PeoplePublisher.SCOPE_ID_GROUP_PREFIX + companyGroupId;

		List<String> newValues = new ArrayList<String>(oldValues.length);

		for (String oldValue : oldValues) {
			String newValue = StringUtil.replace(
				oldValue, "[$COMPANY_GROUP_SCOPE_ID$]", companyGroupScopeId);

			try {
				if (!PeoplePublisherUtil.getPeoplePublisher().isScopeIdSelectable(
						PermissionThreadLocal.getPermissionChecker(), newValue,
						companyGroupId, layout)) {

					continue;
				}

				newValues.add(newValue);
			}
			catch (NoSuchGroupException nsge) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Ignoring scope " + newValue + " because the " +
							"referenced group was not found");
				}
			}
			catch (PrincipalException pe) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Ignoring scope " + newValue + " because the " +
							"referenced parent group no longer allows " +
								"sharing content with child sites");
				}
			}
		}

		portletPreferences.setValues(
			key, newValues.toArray(new String[newValues.size()]));
	}

	private static Log _log = LogFactoryUtil.getLog(
		PeoplePublisherPortletDataHandler.class);

}