package com.slemarchand.peoplepublisher.template;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.template.TemplateHandlerRegistryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplateConstants;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.util.ContentUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
public class DefaultDDMTemplatesManager {

	public static DefaultDDMTemplatesManager getInstance() {
		return _instance;
	}

	public void checkDefaultDDMTemplates(long companyId, String className) throws Exception {
		Set<String> classNameSet = _addedDefaultDDMTemplates.get(companyId);

		if (classNameSet == null) {
			classNameSet = new HashSet<String>();
			_addedDefaultDDMTemplates.put(companyId, classNameSet);
		}

		if (!classNameSet.contains(className)) {

			addDefaultDDMTemplates(companyId, className);

			classNameSet.add(className);
		}
	}

	protected void addDDMTemplate(
			long userId, long groupId, long classNameId, String templateKey,
			String name, String description, String language,
			String scriptFileName, boolean cacheable,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		DDMTemplate ddmTemplate = DDMTemplateLocalServiceUtil.fetchTemplate(
			groupId, classNameId, templateKey);

		if (ddmTemplate != null) {
			return;
		}

		Map<Locale, String> nameMap = new HashMap<Locale, String>();

		Locale locale = PortalUtil.getSiteDefaultLocale(groupId);

		nameMap.put(locale, LanguageUtil.get(locale, name));

		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();

		descriptionMap.put(locale, LanguageUtil.get(locale, description));

		String script = ContentUtil.get(scriptFileName);

		DDMTemplateLocalServiceUtil.addTemplate(
			userId, groupId, classNameId, 0, templateKey, nameMap,
			descriptionMap, DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY, null,
			language, script, cacheable, false, null, null, serviceContext);
	}

	protected void addDDMTemplates(
			long userId, long groupId, ServiceContext serviceContext, String className)
		throws Exception {

		TemplateHandler templateHandler =
			TemplateHandlerRegistryUtil.getTemplateHandler(className);

		long classNameId = PortalUtil.getClassNameId(
			templateHandler.getClassName());

		List<Element> templateElements =
			templateHandler.getDefaultTemplateElements();

		for (Element templateElement : templateElements) {
			String templateKey = templateElement.elementText("template-key");

			DDMTemplate ddmTemplate =
				DDMTemplateLocalServiceUtil.fetchTemplate(
					groupId, classNameId, templateKey);

			if (ddmTemplate != null) {
				continue;
			}

			String name = templateElement.elementText("name");
			String description = templateElement.elementText("description");
			String language = templateElement.elementText("language");
			String scriptFileName = templateElement.elementText("script-file");
			boolean cacheable = GetterUtil.getBoolean(
				templateElement.elementText("cacheable"));

			addDDMTemplate(
				userId, groupId, classNameId, templateKey, name, description,
				language, scriptFileName, cacheable, serviceContext);
		}
	}

	protected void addDefaultDDMTemplates(long companyId, String className) throws Exception {
		ServiceContext serviceContext = new ServiceContext();

		Group group = GroupLocalServiceUtil.getCompanyGroup(companyId);

		serviceContext.setScopeGroupId(group.getGroupId());

		long defaultUserId = UserLocalServiceUtil.getDefaultUserId(companyId);

		serviceContext.setUserId(defaultUserId);

		addDDMTemplates(defaultUserId, group.getGroupId(), serviceContext, className);
	}

	private static DefaultDDMTemplatesManager _instance = new DefaultDDMTemplatesManager();

	private static Map<Long, Set<String>> _addedDefaultDDMTemplates = new HashMap<Long, Set<String>>();
}