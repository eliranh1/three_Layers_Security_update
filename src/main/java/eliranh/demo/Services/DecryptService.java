package eliranh.demo.Services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;

import eliranh.demo.Aes.Aes;
import eliranh.demo.ChaCha.ChaCha;
import eliranh.demo.Classes.Project;
import eliranh.demo.Steganography.Steganography;
import javassist.bytecode.ByteArray;

@Service
public class DecryptService 
{
    private Aes aes;
    private ChaCha chaCha;
    private Steganography steg;
    private byte[] plainText;

    public interface ProcessNotifyListener
    {
      public void waiting();
      public void displayFile();
      public void finished(byte[] hiddenData);
    }

    public DecryptService()
    {
      this.chaCha = new ChaCha();
      this.steg = new Steganography();
      this.aes = new Aes(); 
    }

    public void toDecrypt(Project project, ProcessNotifyListener listener) throws IOException
    {
      Thread thread = new Thread(()->{
      listener.waiting();
      plainText = aes.decrypt(project.getProjectData(), project.getKey().getAesKey(), project.getKey().getAesIv());
      byte[] fileBytes = plainText;
      if(project.getFileType().equals("jpeg"))
      {
        File jpegFile = new File("C:\\Users\\elira\\OneDrive\\שולחן העבודה\\VSCodeProjects\\demo\\src\\main\\resources\\META-INF\\resources\\jpegFile.jpg");
        try {
          Files.copy(new ByteArrayInputStream(fileBytes),jpegFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else
      {
        File wavFile = new File("C:\\Users\\elira\\OneDrive\\שולחן העבודה\\VSCodeProjects\\demo\\src\\main\\resources\\META-INF\\resources\\wavFile.wav");
        try {
          Files.copy(new ByteArrayInputStream(fileBytes), wavFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      listener.displayFile();
      try
      {  
        plainText = steg.decrypt(plainText, project.getFileType(), project.getContentLength());
        plainText = chaCha.decrypt_text(project.getKey().getChachaKey(), project.getKey().getChachaNonce(), plainText);
        System.out.println("text decryption finished!");
      } catch (IOException e) {
        e.printStackTrace();
      }
      listener.finished(plainText);
      });
      thread.start();
    }
}
