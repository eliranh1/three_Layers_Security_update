package eliranh.demo.Pages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import eliranh.demo.Classes.AudioPlayer;
import eliranh.demo.Classes.Project;
import eliranh.demo.Services.DecryptService;
import eliranh.demo.Services.EncryptService;
import eliranh.demo.Services.EncryptService.ProcessNotifyListener;
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
              encryptService.toEncrypt(buffer.getInputStream(), projContent.getValue().getBytes(),projName.getValue(), fileType, new ProcessNotifyListener() {
                @Override
                public void finished(Project project)
                {
                  UI ui = getUI().orElseThrow();
                  ui.access(()->{
                  remove(listenerMessage);
                  listenerMessage = new H1("Encryption finished!");
                  add(listenerMessage);
                  project.setAuthor((Long)VaadinSession.getCurrent().getSession().getAttribute("id"));
                  encryptService.addProject(project);
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
        upload.addSucceededListener(event->{
          
          if(event.getMIMEType().equals("image/jpeg"))
          {
            Notification.show("image uploaded");
            fileType = "jpeg";
            isUploaded = true;
          }
          if(event.getMIMEType().equals("audio/wav"))
          {
            Notification.show("wave uploaded");
            fileType = "wav";
            isUploaded = true;
          } 
        });
        add(title,projName,projContent,encrypt,uploadSize,upload,listenerMessage);
    } 
}
