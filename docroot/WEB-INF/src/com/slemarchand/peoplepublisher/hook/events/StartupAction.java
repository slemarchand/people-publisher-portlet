package com.slemarchand.peoplepublisher.hook.events;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.util.GetterUtil;

public class StartupAction extends SimpleAction {

	public void run(String[] ids) throws ActionException {

		long companyId = GetterUtil.getLong(ids[0]);

		try {

			// NOTHING TO DO !

		} catch (Exception e) {
			throw new ActionException(e);
		}
	}

}