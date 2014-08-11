package com.slemarchand.peoplepublisher;

import com.liferay.portal.kernel.exception.PortalException;
import com.slemarchand.peoplepublisher.model.UserQueryRule;

public class DuplicateQueryRuleException extends PortalException {
	
		private UserQueryRule _queryRule;

		public DuplicateQueryRuleException(UserQueryRule _queryRule) {
			super();
			this._queryRule = _queryRule;
		}
		
}
