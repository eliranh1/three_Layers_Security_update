package eliranh.three_layers_security.Services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import com.vaadin.flow.server.StreamResource;

import eliranh.three_layers_security.Aes.Aes;
import eliranh.three_layers_security.ChaCha.ChaCha;
import eliranh.three_layers_security.Classes.Article;
import eliranh.three_layers_security.Steganography.Steganography;

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
      public void displayFile(String plainText, StreamResource resource);
    }

    public DecryptService()
    {
      this.chaCha = new ChaCha();
      this.steg = new Steganography();
      this.aes = new Aes(); 
    }

    public void toDecrypt(Article article, ProcessNotifyListener listener) throws IOException
    {
      new Thread(()->{
      listener.waiting();
      plainText = aes.decrypt(article.getProjectData(), article.getKey().getAesKey(), article.getKey().getAesIv());
      byte[] fileBytes = plainText;

      StreamResource resource = new StreamResource("fileBytes",()->new ByteArrayInputStream(fileBytes));
      try
      {  
        plainText = steg.decrypt(plainText, article.getFileType(), article.getContentLength());
        plainText = chaCha.decrypt_text(article.getKey().getChachaKey(), article.getKey().getChachaNonce(), plainText);
        System.out.println("text decryption finished!");

        listener.displayFile(new String(plainText,StandardCharsets.US_ASCII),resource);
        //listener.finished(plainText);
      } catch (IOException e) {
        e.printStackTrace();
      }
      }).start();
    }
}
