package controllers.askde;

import controllers.raven.BaseAuthentication;
import controllers.raven.LoginForm;
import play.data.Form;
import play.mvc.Result;
import views.html.*;

public class AuthenticationController extends BaseAuthentication {
	
	public Result login() {
		Form<LoginForm> loginForm = ff.form(LoginForm.class);
		loginForm.fill(new LoginForm("ask elliman","Password"));
		return ok(login.render(loginForm));
	}
	
    public Result authenticate() {	
    	Form<LoginForm> loginForm = ff.form(LoginForm.class).bindFromRequest();
    	if(super.authenticate(loginForm))
    		return redirect(controllers.askde.routes.AdminPanelController.index());
    	else
    		return badRequest(login.render(loginForm));
    }
    
    public Result logout() {
    	return super.logout(controllers.askde.routes.AuthenticationController.login());
    }

}
