import com.liferay.portal.*
import com.liferay.portal.service.*
import com.liferay.portal.util.*

usersData = [
	["brianchan","Brian","Chan","http://cdn.www.liferay.com/image/user_male_portrait?img_id=127809&t=1407667058810"],	
	["jamesfalkner","James","Falkner","https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=6182018&t=1407594819822"],
	["olafkock","Olaf","Kock","https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=1355782&t=1407594819812"],
	["juanfernandez","Juan","Fernández","https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=3329903&t=1407666807554"],
	["brunofarache","Bruno","Farache","https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=128002&t=1407667035388"],
	["raymondauge","Raymond","Augé","https://cdn.lfrs.sl/www.liferay.com/image/user_male_portrait?img_id=127869&t=1407667001062"]
]

themeDisplay = actionRequest.getAttribute(WebKeys.THEME_DISPLAY)
userId = themeDisplay.getUserId()
companyId = themeDisplay.getCompanyId()



serviceContext = new ServiceContext();

try {
	usersData.eachWithIndex { item, index ->
		
		autoScreenName = false

		screenName = item[0]
		email = screenName + "@liferay.com"
		firstName = item[1]
		lastName = item[2]
		
		
		portraitUrl = item[3]

		println "Adding user..."
		
		
		try {
			u = UserLocalServiceUtil.getUserByScreenName(companyId, screenName)
			
			UserLocalServiceUtil.deleteUser(u.getUserId());
			
		} catch(NoSuchUserException e) {
			
		}
		
		/*
		 long creatorUserId,
		 long companyId, boolean autoPassword, java.lang.String password1,
		 java.lang.String password2, boolean autoScreenName,
		 java.lang.String screenName, java.lang.String emailAddress,
		 long facebookId, java.lang.String openId, java.util.Locale locale,
		 java.lang.String firstName, java.lang.String middleName,
		 java.lang.String lastName, int prefixId, int suffixId, boolean male,
		 int birthdayMonth, int birthdayDay, int birthdayYear,
		 java.lang.String jobTitle, long[] groupIds, long[] organizationIds,
		 long[] roleIds, long[] userGroupIds, boolean sendEmail,
		 com.liferay.portal.service.ServiceContext serviceContext 
		*/
		
		u = UserLocalServiceUtil.addUser(
			userId, companyId, false, "password",
			"password", autoScreenName, 
			screenName, email,
			0, null, Locale.getDefault(),
			firstName, "",
			lastName, 0, 0, true,
			1,1,1970,
			"Job", null, null,
			null, null, false,
			serviceContext)
		
		portraitData = new URL(portraitUrl).getBytes()
		
		println "Updating portrait..."
		
		u = UserLocalServiceUtil.updatePortrait(u.getUserId(), portraitData)
	}
} catch(e) {
	println e
}