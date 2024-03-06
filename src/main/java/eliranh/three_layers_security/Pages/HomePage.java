package eliranh.three_layers_security.Pages;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import eliranh.three_layers_security.Classes.Article;
import eliranh.three_layers_security.Classes.AudioPlayer;
import eliranh.three_layers_security.Services.DecryptService;
import eliranh.three_layers_security.Services.EncryptService;
import eliranh.three_layers_security.Services.ArticleService;
import eliranh.three_layers_security.Services.DecryptService.ProcessNotifyListener;
import eliranh.three_layers_security.Services.EncryptService.EProcessNotifyListener;

@Route("/Home") @PageTitle("Home")
public class HomePage extends VerticalLayout
{
    private ArticleService articleService;
    private DecryptService decryptService;
    private EncryptService encryptService;
    private static Grid<Article> grid;
    private static ArrayList<Article> articlesList;
    private H1 title,listenerMessage;
    private MemoryBuffer buffer;
    private static boolean isUploaded;
    private static String fileType;
    private Paragraph uploadSize;
    private H1 SEARCH_ERROR = new H1("No Article has found");
    private ProgressBar progressBar;
    private HorizontalLayout navigateBar;

    public HomePage(ArticleService articleService, DecryptService decryptService, EncryptService encryptService) throws IOException
    {
        this.articleService = articleService;
        this.decryptService = decryptService;
        this.encryptService = encryptService;
        this.listenerMessage = new H1(" ");
        this.navigateBar = new HorizontalLayout();
        this.title = new H1("Welcome "+VaadinSession.getCurrent().getSession().getAttribute("name")+" !");
        this.title.setSizeFull();

        Button logOutBtn = new Button("Logout",VaadinIcon.SIGN_OUT.create());
        logOutBtn.getStyle().setBackgroundColor("red");
        logOutBtn.getStyle().setColor("white");        
        logOutBtn.addClickListener(event->{
          VaadinSession.getCurrent().getSession().invalidate();   
          UI.getCurrent().getPage().setLocation("/");
        });

        Button usersBtn = new Button("Users",VaadinIcon.USERS.create(),e->UI.getCurrent().navigate(UsersPage.class));

        navigateBar.add(usersBtn,logOutBtn,VaadinIcon.HOME.create());
        navigateBar.setAlignItems(Alignment.BASELINE);

        HorizontalLayout upperLayout = new HorizontalLayout();
        upperLayout.getStyle().setBackgroundColor("lightblue");
        upperLayout.setSizeFull();
        upperLayout.setAlignItems(Alignment.BASELINE);
        upperLayout.add(new Avatar("avatar",(String)VaadinSession.getCurrent().getSession().getAttribute("avatar")),title,navigateBar);
        add(upperLayout);

        HorizontalLayout layout = new HorizontalLayout();
        
        TextArea artiContent = new TextArea("Article's content");
        TextArea artiName = new TextArea("Article's name");
        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);

        VerticalLayout uploadLayout = new VerticalLayout();
        uploadLayout.setWidth("200 px");
        
        buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize(50*1024*1024); // 16MB
        uploadSize = new Paragraph("Maximum file size: 16MB");
        upload.setAcceptedFileTypes("audio/WAV,image/jpeg");

        Button encrypt = new Button("Save");
        encrypt.getStyle().setBackgroundColor("blue");
        encrypt.getStyle().setColor("white");
        encrypt.addClickListener(event->{
          if(artiContent.isEmpty()||!isUploaded)
          {
            Notification.show("One or more fields are empty",5000,Position.MIDDLE);
          } 
          else
            try {
              encrypt.setEnabled(false);
              encryptService.toEncrypt(buffer.getInputStream(), artiContent.getValue().getBytes(), artiName.getValue(), fileType, new EProcessNotifyListener() {
                @Override
                public void finished(Article article)
                {
                  UI ui = getUI().orElseThrow();
                  ui.access(()->{
                  listenerMessage.removeAll();
                  listenerMessage.add("Encryption finished!");
                  remove(progressBar);
                  encrypt.setEnabled(true);
                  artiContent.setValue("");
                  artiName.setValue("");
                  upload.clearFileList();
                  article.setAuthor((Long)VaadinSession.getCurrent().getSession().getAttribute("id"),(String)VaadinSession.getCurrent().getSession().getAttribute("name"));
                  article.setClassLevel((int)VaadinSession.getCurrent().getSession().getAttribute("classification"));
                  encryptService.addProject(article);

                  if(articlesList.isEmpty())
                  {
                    articlesList.add(article);
                    createGrid();
                    remove(SEARCH_ERROR);
                    layout.add(grid);
                  }
                  else
                  {
                    articlesList.add(article);
                    VaadinSession.getCurrent().lock();
                    grid.setItems(articlesList);
                    VaadinSession.getCurrent().unlock();
                  }
                  });
                }
                public void waiting()
                {
                  UI ui = getUI().orElseThrow();
                  ui.access(()->{
                    listenerMessage.removeAll();
                    listenerMessage.add("Encrypting...");
                    add(progressBar);
                  });
                }
              }); 
            } catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
              e.printStackTrace();
            }
        });

        upload.addSucceededListener(event->{

            if(event.getMIMEType().equals("image/jpeg"))
            {
              //Notification.show("image uploaded");
              fileType = "jpeg";
              isUploaded = true;
            }
            if(event.getMIMEType().equals("audio/wav"))
            {
              //Notification.show("wave uploaded");
              fileType = "wav";
              isUploaded = true;
            } 
          });

        uploadLayout.add(new H3("Create new Article"),artiName,artiContent,uploadSize,upload,encrypt);
        layout.add(uploadLayout);
        layout.setSizeFull();

        articlesList = articleService.findProjects((int)VaadinSession.getCurrent().getSession().getAttribute("classification"));
        if(articlesList.size() == 0)
        {
            add(SEARCH_ERROR);
            add(layout,listenerMessage);
        }
        else
        {
          createGrid();
          layout.add(grid);
          layout.setSizeFull();
          add(layout,listenerMessage);
        }
    }

    private void createGrid() 
    {
        grid = new Grid<>();
        grid.addColumn(Article::getName).setHeader("Article's name");
        grid.addColumn(Article::getDate).setHeader("Uploading date");
        grid.addColumn(Article::getAuthor).setHeader("Author");
        grid.setItems(articlesList);
        grid.addItemClickListener(event->{
          try {
                decryptService.toDecrypt(event.getItem(), new ProcessNotifyListener() {

                    public void waiting()
                    {
                        UI ui = getUI().orElseThrow();
                        ui.access(()->{
                            listenerMessage.removeAll();
                            listenerMessage.add("Decrypting...");
                            add(listenerMessage,progressBar);
                        });
                    }

                    public void displayFile(String plainText, StreamResource resource)
                    {
                        UI ui = getUI().orElseThrow();
                        ui.access(()->{
                           System.out.println("display file");
                           listenerMessage.removeAll();
                           remove(progressBar);
                           if(event.getItem().getFileType().equals("jpeg"))
                           {
                            Image image = new Image(resource, "image");
                            add(image);
                           }
                           else
                           {
                            AudioPlayer audioPlayer = new AudioPlayer();
                            audioPlayer.setSource(resource);
                            add(audioPlayer);
                           }
                           add(plainText);
                        });
                    }
                  });
          } catch (IOException e) {
            e.printStackTrace();
          }
          });
    }
}
