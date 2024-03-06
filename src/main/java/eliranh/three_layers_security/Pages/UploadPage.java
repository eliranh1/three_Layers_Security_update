package eliranh.three_layers_security.Pages;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import eliranh.three_layers_security.Classes.Article;
import eliranh.three_layers_security.Services.DecryptService;
import eliranh.three_layers_security.Services.EncryptService;
import eliranh.three_layers_security.Services.EncryptService.EProcessNotifyListener;
@Route("/upload")
public class UploadPage extends VerticalLayout
{
  private H1 title, listenerMessage;
  private Paragraph uploadSize;
  private MemoryBuffer buffer;
  private EncryptService encryptService;
  private DecryptService decryptService;
  private static boolean isUploaded;
  private static String fileType;

    public UploadPage(EncryptService encryptService,DecryptService decryptService)
    {
        this.encryptService = encryptService;
        this.decryptService = decryptService;

        title = new H1("Welcome " + VaadinSession.getCurrent().getSession().getAttribute("name") + " !");
        listenerMessage = new H1();
        TextArea projContent = new TextArea("content");
        TextArea projName = new TextArea("name");
        Button encrypt = new Button("encrypt");
        Button homeBtn = new Button("Back to Home page");

        buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize(50*1024*1024); // 16MB
        uploadSize = new Paragraph("Maximum file size: 16MB");
        upload.setAcceptedFileTypes("audio/WAV,image/jpeg");
        encrypt.addClickListener(event->{
          if(projContent.isEmpty()||!isUploaded)
          {
            Notification.show("One or more fields are empty",5000,Position.MIDDLE);
          } 
          else
            try {
              encryptService.toEncrypt(buffer.getInputStream(), projContent.getValue().getBytes(),projName.getValue(), fileType, new EProcessNotifyListener() {
                @Override
                public void finished(Article article)
                {
                  UI ui = getUI().orElseThrow();
                  ui.access(()->{
                  remove(listenerMessage);
                  listenerMessage = new H1("Encryption finished!");
                  add(listenerMessage);
                  article.setAuthor((Long)VaadinSession.getCurrent().getSession().getAttribute("id"),(String)VaadinSession.getCurrent().getSession().getAttribute("name"));
                  encryptService.addProject(article);
                  });
                }
                public void waiting()
                {
                  UI ui = getUI().orElseThrow();
                  ui.access(()->{
                    listenerMessage = new H1("Encrypting...");
                    add(listenerMessage);
                  });
                }
              }); 
            } catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
              e.printStackTrace();
            }
        });

        homeBtn.addClickListener(event->{
          UI.getCurrent().navigate(HomePage.class);
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
        add(title,homeBtn,projName,projContent,encrypt,uploadSize,upload,listenerMessage);
    } 
}
