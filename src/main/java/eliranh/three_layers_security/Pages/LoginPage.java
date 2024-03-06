package eliranh.three_layers_security.Pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import eliranh.three_layers_security.Classes.User;
import eliranh.three_layers_security.Services.UserService; 

@Route("") @PageTitle("Login")
public class LoginPage extends HorizontalLayout
{
    private UserService userService;

    public LoginPage(UserService userService)
    {
        this.userService = userService;

        setSizeFull();
      setSpacing(true);
      getStyle().set("background", "center / cover "+"url('https://www.pcworld.com/wp-content/uploads/2023/05/shutterstock-encryption-logo.jpg')");

      //----------------------------------------------
      // designing the login form
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
      login.getElement().getThemeList().add("dark");
      login.setI18n(i18n);
      login.getStyle().setPadding("150px 50px 250px");
      
      login.addLoginListener(e->{

        Long id = null;
        try {id = Long.parseLong(e.getUsername());}
        catch(NumberFormatException e1){login.setError(true);}

        if(userService.isUserExistsById(id))
        {
          User user = userService.findUserById(id);
          if(user.getPassword().equals(e.getPassword()))
          {
            VaadinSession.getCurrent().getSession().setAttribute("id",id);
            VaadinSession.getCurrent().getSession().setAttribute("name",user.getName());
            VaadinSession.getCurrent().getSession().setAttribute("avatar",user.getAvatarPath());
            VaadinSession.getCurrent().getSession().setAttribute("classification",user.getClassLevel());            
            UI.getCurrent().navigate(HomePage.class); 
          }
          else login.setError(true);
        }
        else login.setError(true);
      });

      add(login);
    }

}
