package eliranh.demo.Pages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import eliranh.demo.Classes.AudioPlayer;
import eliranh.demo.Classes.Project;
import eliranh.demo.Services.DecryptService;
import eliranh.demo.Services.ProjectService;
import eliranh.demo.Services.DecryptService.ProcessNotifyListener;

@Route("/Home") @PageTitle("Home")
public class HomePage extends VerticalLayout
{
    private ProjectService projectService;
    private DecryptService decryptService;
    private H1 listenerMessage;

    public HomePage(ProjectService projectService, DecryptService decryptService)
    {
        this.projectService = projectService;
        this.decryptService = decryptService;
        Button button = new Button("Create new Project");
        button.addClickListener(event->
        {
            UI.getCurrent().navigate(UploadPage.class);
        });
        add(button);

        ArrayList<Project> projectsList = projectService.findProjects((Long)VaadinSession.getCurrent().getSession().getAttribute("id"));
        if(projectsList.size() == 0)
        add(new H1("No Project has found"));
        else
        {
          Grid<Project> grid = new Grid<>();
          grid.addColumn(Project::getName).setHeader("Project's name");
          grid.setItems(projectsList);
          grid.addItemClickListener(event->{
          try {
            decryptService.toDecrypt(event.getItem(), new ProcessNotifyListener() {

                public void waiting()
                {
                    UI ui = getUI().orElseThrow();
                    ui.access(()->{
                        listenerMessage = new H1("Decrypting...");
                        add(listenerMessage);
                    });
                }

                public void displayFile()
                {
                    UI ui = getUI().orElseThrow();
                    ui.access(()->{
                       remove(listenerMessage,grid);
                       if(event.getItem().getFileType().equals("jpeg"))
                       {
                        Image image = new Image("jpegFile.jpg", "image");
                        add(image);
                       }
                       else
                       {
                        AudioPlayer audioPlayer = new AudioPlayer();
                        audioPlayer.setSource("wavFile.wav");
                        add(audioPlayer);
                       }
                    });
                }
                public void finished(byte[] plainText)
                {
                    UI ui = getUI().orElseThrow();
                    ui.access(()->{
                    remove(listenerMessage);
                    Dialog dialog = new Dialog();
                    dialog.setHeaderTitle("Project's content");
                    dialog.add(new String(plainText,StandardCharsets.US_ASCII));
                    add(dialog);
                    dialog.open();
                    });
                }
              });
        } catch (IOException e) {
            e.printStackTrace();
        }
          });
          add(grid);
        }
    }
}
