package eliranh.three_layers_security.Pages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
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
import eliranh.three_layers_security.Services.ArticleService.LoadAritclesListener;
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
    private H1 IMPORT = new H1("Importing Articles...");
    private ProgressBar progressBar;
    private HorizontalLayout navigateBar;
    private Div displayDiv;
    private Button saveBtn;
    private Dialog dialog;
    private TextArea artiName,artiContent; 
    private Checkbox isEditable;
    public ByteArrayInputStream editResource;
    private Article editedArticle;
    public final Icon PROGRESS_BAR = VaadinIcon.PROGRESSBAR.create();

    public HomePage(ArticleService articleService, DecryptService decryptService, EncryptService encryptService) throws IOException
    {
        this.articleService = articleService;
        this.decryptService = decryptService;
        this.encryptService = encryptService;
        this.listenerMessage = new H1(" ");
        this.navigateBar = new HorizontalLayout();
        this.displayDiv = new Div();
        displayDiv.setHeight("150 px");
        displayDiv.setWidth("150 px");
        this.title = new H1("Welcome "+getAttribute("name")+" !");
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
        upperLayout.add(new Avatar("avatar",(String)getAttribute("avatar")),title,navigateBar);
        add(upperLayout);
        
        artiContent = new TextArea("Article's content");
        artiName = new TextArea("Article's name");
        isEditable = new Checkbox("Allow others to edit your article");
        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        
        buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize(50*1024*1024); // 16MB
        uploadSize = new Paragraph("Maximum file size: 16MB");
        upload.setAcceptedFileTypes("audio/WAV,image/jpeg");

        Button newArticle = new Button("New Article",VaadinIcon.PLUS_SQUARE_O.create(),event->{

        dialog = new Dialog();
            
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(cancelEvent->dialog.close());

        saveBtn = new Button("Save",VaadinIcon.PLUS.create());
        saveBtn.getStyle().setBackgroundColor("blue");
        saveBtn.getStyle().setColor("white");
        saveBtn.addDoubleClickListener(buttonEvent->{
        if(artiContent.isEmpty()||!isUploaded||artiName.isEmpty())
          {
            Notification.show("One or more fields are empty",5000,Position.MIDDLE);
          } 
        else
         {
            saveBtn.setEnabled(false);
            dialog.close();
            doEncrypt();
            
         }
        });
    
        upload.addSucceededListener(uploadEvent->{
    
        if(uploadEvent.getMIMEType().equals("image/jpeg"))
          {
            //Notification.show("image uploaded");
            fileType = "jpeg";
            isUploaded = true;
          }
        if(uploadEvent.getMIMEType().equals("audio/wav"))
          {
            //Notification.show("wave uploaded");
            fileType = "wav";
            isUploaded = true;
          } 
          });
          VerticalLayout uploadLayout = new VerticalLayout();
          uploadLayout.setWidth("200 px");

          HorizontalLayout buttonsLayout = new HorizontalLayout();
          buttonsLayout.add(saveBtn,cancelBtn);
          uploadLayout.add(artiName,artiContent,uploadSize,upload,isEditable,buttonsLayout); 

          dialog.setHeaderTitle("New Article");
          dialog.add(uploadLayout);
          dialog.open();
        }); 

        add(IMPORT,progressBar);

        articleService.findProjects((int)getAttribute("classification"),new LoadAritclesListener() {
          
          public void finished(ArrayList<Article> list)
          { 
            UI ui = getUI().orElseThrow();
            ui.access(()->{
              remove(IMPORT,progressBar);

              articlesList = list;
              if(articlesList.size() == 0)
               {
                 add(newArticle,SEARCH_ERROR,listenerMessage);
               }
               else
               {
                 createGrid();
                 add(newArticle,grid,listenerMessage,displayDiv);
               }
            });
          }
        });
    }
    private void createGrid() 
    {
        grid = new Grid<>();
        grid.addColumn(Article::getName).setHeader("Article's name");
        grid.addColumn(Article::getDate).setHeader("Uploading date");
        grid.addColumn(Article::getAuthor).setHeader("Author");
        grid.addComponentColumn(article -> {
                if(article.getFileType().equals("jpeg"))
                {
                    Icon editIcon = VaadinIcon.PICTURE.create();
                    return editIcon;
                }
                return VaadinIcon.MUSIC.create(); 
            }).setHeader("Type");

        grid.addComponentColumn(article -> {
              if(article.isAllowEdit())
              {
                if(article.getClassLevel() <= (int)getAttribute("classification")|| (boolean)getAttribute("admin"))
                {
                  return VaadinIcon.EDIT.create();
                }
              } 
              return VaadinIcon.LOCK.create();
           }).setHeader("is editable");

        grid.setItems(articlesList);
        
        grid.addItemClickListener(event->{

          if(event.getItem().isAllowEdit())
          {
            Article article = event.getItem();
            dialog = new Dialog();
            Button decryptBtn = new Button("see article",VaadinIcon.EYE.create(),e -> {
              doDecrypt(event.getItem(), false);
              dialog.close();
            });
            decryptBtn.getStyle().setBackground("green");
            decryptBtn.getStyle().setColor("white");

            Button editBtn = new Button("edit article",VaadinIcon.EDIT.create(),e -> {
              dialog.close();
              dialog = new Dialog();

              editedArticle = article;
              artiName.setValue(article.getName());
              doDecrypt(article, true);
              Button saveChange = new Button("Save changes",VaadinIcon.DOWNLOAD.create(),e2->{
                if(artiContent.isEmpty()||artiName.isEmpty())
                {
                  Notification.show("One or more fields are empty",5000,Position.MIDDLE);
                }
                else
                {
                  dialog.close();
                  
                  doEditEncrypt();
                  artiName.setValue(""); 
                  artiContent.setValue("");
                }
              });
              dialog.add(artiName, artiContent, saveChange);
              dialog.open();
            });
            editBtn.getStyle().setBackground("blue");
            editBtn.getStyle().setColor("white");
            HorizontalLayout dialogLayout = new HorizontalLayout();
            dialogLayout.add(decryptBtn, editBtn);
            dialog.add(dialogLayout);
            dialog.open();
          }
          else doDecrypt(event.getItem(),false);
          });
    }

    public void doEncrypt()
    {
      try 
      {
          encryptService.toEncrypt(buffer.getInputStream(), artiContent.getValue().getBytes(), artiName.getValue(), fileType, new EProcessNotifyListener() {
          @Override
          public void finished(Article article)
          {
            UI ui = getUI().orElseThrow();
            ui.access(()->{
            listenerMessage.removeAll();
            listenerMessage.add("Encryption finished!");
            remove(progressBar);
            saveBtn.setEnabled(true);
            article.setAuthor((Long)getAttribute("id"),(String)getAttribute("name"));
            article.setClassLevel((int)getAttribute("classification"));
            article.setAllowEdit(isEditable.getValue());
            encryptService.addProject(article);

            if(articlesList.isEmpty())
            {
              articlesList.add(article);
              createGrid();
              remove(SEARCH_ERROR);  
              add(grid);
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
              artiContent.setValue("");
              artiName.setValue("");
              listenerMessage.removeAll();
              displayDiv.removeAll();
              listenerMessage.add("Encrypting...");
              add(progressBar);
            });
          }
        });
      } catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
        e.printStackTrace();
      }
      
    }

    public void doDecrypt(Article article, boolean isEdited)
    {
      try {
        decryptService.toDecrypt(article, isEdited, new ProcessNotifyListener() {

            public void waiting()
            {
                UI ui = getUI().orElseThrow();
                ui.access(()->{
                  if(!isEdited)
                  {
                    listenerMessage.removeAll();
                    listenerMessage.add("Decrypting...");
                    displayDiv.removeAll();
                    add(listenerMessage,progressBar);
                  }
                  else 
                  {
                    artiContent.setValue("decrypting...");
                    artiContent.setReadOnly(true);
                  }
                });
            }

            public void displayFile(String plainText, ByteArrayInputStream resource)
            {
                UI ui = getUI().orElseThrow();
                ui.access(()->{
                  if(!isEdited)
                  {
                   System.out.println("display file");
                   listenerMessage.removeAll();
                   remove(progressBar);

                   StreamResource streamResource = new StreamResource("fileBytes", ()->resource);
                   if(article.getFileType().equals("jpeg"))
                   {
                    displayDiv.add(new Image(streamResource, "image"));
                   }
                   else
                   {
                    AudioPlayer audioPlayer = new AudioPlayer();
                    audioPlayer.setSource(streamResource);
                    displayDiv.add(audioPlayer);
                   }
                   displayDiv.add(plainText);
                  }
                  else 
                  {
                    editResource = resource;
                    artiContent.setValue(plainText);
                    artiContent.setReadOnly(false);
                  }
                });
            }
          });
  } catch (IOException e) {
    e.printStackTrace();
  }
    }

  private void doEditEncrypt()
  {
    try 
      {
          encryptService.toEncrypt(editResource, artiContent.getValue().getBytes(), artiName.getValue(), editedArticle.getFileType(), new EProcessNotifyListener() {
          @Override
          public void finished(Article article)
          {
            UI ui = getUI().orElseThrow();
            ui.access(()->{
            listenerMessage.removeAll();
            listenerMessage.add("Encryption finished!");
            remove(progressBar);

            editedArticle.setProjectData(article.getProjectData());
            articleService.updateArticle(editedArticle);

            articlesList.add(article);
            VaadinSession.getCurrent().lock();
            grid.setItems(articlesList);
            VaadinSession.getCurrent().unlock();
            });
          }
          public void waiting()
          {
            UI ui = getUI().orElseThrow();
            ui.access(()->{
              artiContent.setValue("");
              artiName.setValue("");
              listenerMessage.removeAll();
              displayDiv.removeAll();
              listenerMessage.add("Encrypting...");
              add(progressBar);
            });
          }
        });
      } catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
        e.printStackTrace();
      }
  }

  private Object getAttribute(String name)
  {
    return VaadinSession.getCurrent().getAttribute(name);
  }
}
