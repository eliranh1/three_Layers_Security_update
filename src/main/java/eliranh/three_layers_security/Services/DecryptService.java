package eliranh.three_layers_security.Services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
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
      public void displayFile(String plainText, ByteArrayInputStream resource);
    }

    public DecryptService()
    {
      this.chaCha = new ChaCha();
      this.steg = new Steganography();
      this.aes = new Aes(); 
    }

    public void toDecrypt(Article article, boolean isEdited, ProcessNotifyListener listener) throws IOException
    {
      new Thread(()->{
      listener.waiting();
      plainText = aes.decrypt(article.getProjectData(), article.getKey().getAesKey(), article.getKey().getAesIv());
      byte[] fileBytes = plainText;

      
      ByteArrayInputStream resource = new ByteArrayInputStream(fileBytes);
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
