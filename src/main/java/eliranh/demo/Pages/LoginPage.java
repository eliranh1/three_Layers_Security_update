package eliranh.demo.Pages;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import eliranh.demo.Services.UserService;

@Route("") @PageTitle("Login")
public class LoginPage extends VerticalLayout
{
    private UserService userService;


    public LoginPage(UserService userService)
    {
        this.userService = userService;

        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("Login");
        i18nForm.setUsername("Id");
        i18nForm.setPassword("Password");
        i18nForm.setSubmit("Submit");
        i18nForm.setForgotPassword("Forgot Password?");
        i18n.setForm(i18nForm); 
      
        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("incorrect Id or Password");
        i18nErrorMessage.setMessage("Check that you have entered the correct Id and Password and try again.");
        i18n.setErrorMessage(i18nErrorMessage);
      //----------------------------------------------- 

      LoginForm login = new LoginForm();
      login.setI18n(i18n);

      login.addLoginListener(e->{

        Long id = null;
        try {id = Long.parseLong(e.getUsername());}
        catch(NumberFormatException e1){login.setError(true);}

        if(userService.isUserExistsById(id))
        {
          if(userService.findUserById(id).password.equals(e.getPassword()))
          {
            VaadinSession.getCurrent().getSession().setAttribute("id",id);
            VaadinSession.getCurrent().getSession().setAttribute("name",userService.findUserById(id).name);
            UI.getCurrent().navigate(HomePage.class); 
          }
          else login.setError(true);
        }
        else login.setError(true);
      });

      add(login);
    }

}
