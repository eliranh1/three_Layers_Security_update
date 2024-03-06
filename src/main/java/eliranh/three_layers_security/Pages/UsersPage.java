package eliranh.three_layers_security.Pages;

import java.awt.Color;
import java.io.IOException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import eliranh.three_layers_security.Classes.User;
import eliranh.three_layers_security.Services.UserService;

@Route("/Users") @PageTitle("Users")
public class UsersPage extends VerticalLayout 
{
    private UserService userService;
    private H1 title;
    private HorizontalLayout navigateBar,upperLayout;

    public UsersPage(UserService userService) throws IOException
    {
        this.userService = userService;
        this.upperLayout = new HorizontalLayout();
        upperLayout.setSizeFull();
        upperLayout.getStyle().setBackgroundColor("lightblue");
        upperLayout.setAlignItems(Alignment.BASELINE);
        this.title = new H1("Welcome "+VaadinSession.getCurrent().getSession().getAttribute("name")+" !");
        title.setSizeFull();
        upperLayout.add(new Avatar("Avatar",(String)VaadinSession.getCurrent().getSession().getAttribute("avatar")),title);
        this.navigateBar = new HorizontalLayout();
        Button homeBtn = new Button("Home",VaadinIcon.HOME.create(),event->{UI.getCurrent().navigate(HomePage.class);});

//        userService.generatePassword();

        Button logOutBtn = new Button("Logout",VaadinIcon.SIGN_OUT.create());
        logOutBtn.getStyle().setBackgroundColor("red");
        logOutBtn.getStyle().setColor("white");        
        logOutBtn.addClickListener(event->{
          VaadinSession.getCurrent().getSession().invalidate();   
          UI.getCurrent().getPage().setLocation("/");
        });

        navigateBar.add(homeBtn,logOutBtn,VaadinIcon.USERS.create());
        upperLayout.add(navigateBar);
        add(upperLayout);

        Grid<User> usersGrid = new Grid<>();
        usersGrid.setItems(userService.findAllUsers());
        usersGrid.addColumn(createEmployeeRenderer()).setHeader("Name");
        usersGrid.addColumn(User::getId).setHeader("Id");
        usersGrid.addColumn(User::getClassLevel).setHeader("Classification level");
        if(userService.findUserById((Long)VaadinSession.getCurrent().getSession().getAttribute("id")).isAdmin())
        {
            usersGrid.addComponentColumn(user -> {
                if(!user.isAdmin())
                {
                    Icon editIcon = VaadinIcon.EDIT.create();
                    return editIcon;
                }
                return VaadinIcon.LOCK.create(); 
            }).setHeader("Edition availability");

            usersGrid.addItemClickListener(gridEvent->{
                if(!gridEvent.getItem().isAdmin())
                {
                    Dialog dialog = new Dialog();
                    dialog.setHeaderTitle("User details");

                    VerticalLayout dialogLayout = new VerticalLayout();

                    TextField nameField = new TextField("Name");
                    nameField.setValue(gridEvent.getItem().getName());
                    nameField.setReadOnly(true);
                    dialogLayout.add(nameField);

                    TextField idField = new TextField("Id");
                    idField.setValue(gridEvent.getItem().getId()+"");
                    idField.setReadOnly(true);
                    dialogLayout.add(idField);

                    TextField passwordField = new TextField("Password");
                    passwordField.setValue(gridEvent.getItem().getPassword());
                    dialogLayout.add(passwordField);

                    TextField classLevelField = new TextField("Classification level");
                    classLevelField.setValue(gridEvent.getItem().getClassLevel()+"");
                    dialogLayout.add(classLevelField);

                    Checkbox doAdmin = new Checkbox("Become Admin");
                    dialogLayout.add(doAdmin);

                    HorizontalLayout buttons = new HorizontalLayout();

                    Button deleteBtn = new Button("Delete User",VaadinIcon.ARROWS_CROSS.create());
                    deleteBtn.getStyle().setBackgroundColor("red");
                    deleteBtn.getStyle().setColor("white");

                    deleteBtn.addDoubleClickListener(event->{
                        userService.deleteUser(gridEvent.getItem());
                        Notification.show("User deleted!",3000,Position.MIDDLE);
                        usersGrid.setItems(userService.findAllUsers());
                        dialog.close();
                    });

                    Button saveBtn = new Button("Save",VaadinIcon.DOWNLOAD.create());
                    saveBtn.getStyle().setBackgroundColor("blue");
                    saveBtn.getStyle().setColor("white");
                    saveBtn.addDoubleClickListener(event->{
                        if(doAdmin.getValue() == true)
                        {
                            User user = new User(Long.parseLong(idField.getValue()),nameField.getValue(),passwordField.getValue(),Integer.parseInt(classLevelField.getValue()),true);
                            userService.updateUser(user);
                            usersGrid.setItems(userService.findAllUsers());
                            dialog.close();
                        }
                        else
                        {
                            User user = new User(Long.parseLong(idField.getValue()),nameField.getValue(),passwordField.getValue(),Integer.parseInt(classLevelField.getValue()),false);
                            userService.updateUser(user);
                            usersGrid.setItems(userService.findAllUsers());
                            dialog.close();
                        }
                    });

                    Button cancelBtn = new Button("Cancel",event->{dialog.close();});

                    buttons.add(cancelBtn,saveBtn,deleteBtn);
                    dialogLayout.add(buttons);
                    dialog.add(dialogLayout);
                    add(dialog);
                    dialog.open();
                }
            });
            Button addUserBtn = new Button("Add User",VaadinIcon.PLUS_CIRCLE.create());
            addUserBtn.addClickListener(event->{
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("New User");

                VerticalLayout dialogLayout = new VerticalLayout();

                TextField nameField = new TextField("Name");
                dialogLayout.add(nameField);

                TextField idField = new TextField("Id");
                dialogLayout.add(idField);

                TextField passwordField = new TextField("Password");
                dialogLayout.add(passwordField);

                TextField classLevelField = new TextField("Classification level");
                dialogLayout.add(classLevelField);

                Checkbox doAdmin = new Checkbox("Become Admin");
                dialogLayout.add(doAdmin);

                HorizontalLayout buttonsLayout = new HorizontalLayout();

                Button addBtn = new Button("Add",VaadinIcon.PLUS_CIRCLE.create());
                Button cancelBtn = new Button("Cancel",e->{dialog.close();});
                addBtn.getStyle().setColor("white");
                addBtn.getStyle().setBackgroundColor("blue");
                addBtn.addDoubleClickListener(addEvent->{
                    if(nameField.isEmpty()||idField.isEmpty()||passwordField.isEmpty()||classLevelField.isEmpty())
                    {
                        Notification.show("One or more fields are empty", 5000, Position.MIDDLE);
                    }
                    else
                    {
                        Long id = null;
                        try 
                        {
                            id = Long.parseLong(idField.getValue());
                            if(id < 100000000)
                            {
                                idField.setInvalid(true);
                                idField.setErrorMessage("Id must be 9 digits");
                            }
                            if(id > 100000000)
                            {
                                if(userService.findUserById(id) == null)
                                {
                                    String name = nameField.getValue().toLowerCase();
                                    int classLevel = Integer.parseInt(classLevelField.getValue());
                                    if(classLevel < 1 || classLevel > 3)
                                    {
                                      classLevelField.setInvalid(true);
                                      classLevelField.setErrorMessage("Invalid classification level");
                                    }
                                    else
                                    {
                                      String password = passwordField.getValue();
                                      User user;
                                      if(doAdmin.getValue() == true)
                                      {
                                        user = new User(id, name, password, classLevel, true);
                                      }
                                      else 
                                      {
                                        user = new User(id, name, password, classLevel, false);
                                      }
                                      userService.addUser(user);
                                      usersGrid.setItems(userService.findAllUsers());
                                      dialog.close();
                                    }
                                }
                                else Notification.show("User already exist with the given Id",5000,Position.MIDDLE);
                            }
                        }
                        catch(NumberFormatException e1){idField.setInvalid(true); idField.setErrorMessage("Invalid id");}

                    }
                });
                buttonsLayout.add(cancelBtn,addBtn);
                dialogLayout.add(buttonsLayout);
                dialog.add(dialogLayout);
                add(dialog);
                dialog.open();
            });
            add(addUserBtn);
        }
        //grid.setSizeFull();
        add(usersGrid);
    }

    private static Renderer<User> createEmployeeRenderer() {
    return LitRenderer.<User> of(
            "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                    + "<vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\" alt=\"User avatar\"></vaadin-avatar>"
                    + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                    + "    <span> ${item.fullName} </span>"
                    + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                    + "      ${item.email}" + "    </span>"
                    + "  </vaadin-vertical-layout>"
                    + "</vaadin-horizontal-layout>")
            .withProperty("pictureUrl", User::getAvatarPath)
            .withProperty("fullName", User::getName);
    }
}
