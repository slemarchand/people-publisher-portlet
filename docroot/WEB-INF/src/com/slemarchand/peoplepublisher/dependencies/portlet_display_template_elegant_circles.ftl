<style>

	.user {
		display: inline-block;
		margin: 1.5em 0;
		text-align: center;
		vertical-align: top;
		width: 32%;
	}

    .portrait {
        width: 100px;
        height: 100px;
     	background-position: center;
		background-repeat: no-repeat;
		background-size: cover;
		border-radius: 500px;
		margin: 0 auto 8px;
    }
    
    .name {
                
        text-weight: bold    
    }

</style>
<#if users?has_content>
	<#list users as curUser>
		
		<div class="user">
		
		    <div class="portrait" style="background-image: url(${curUser.getPortraitURL(themeDisplay)})">
			</div>
			
			<div class="name">
			${curUser.getFullName()}
			</div>
		
		</div>
		
	</#list>
</#if>