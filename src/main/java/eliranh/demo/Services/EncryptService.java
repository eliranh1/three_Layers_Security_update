package eliranh.demo.Services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.springframework.stereotype.Service;

import eliranh.demo.Aes.Aes;
import eliranh.demo.ChaCha.ChaCha;
import eliranh.demo.Classes.Key;
import eliranh.demo.Classes.Project;
import eliranh.demo.Repositories.ProjectRepository;
import eliranh.demo.Steganography.Steganography;


@Service
public class EncryptService 
{
    private ChaCha chaCha;
    private Steganography steg;
    private Aes aes;
    private byte[] encryptedFile;
    private ProjectRepository projectRepo;


    public interface ProcessNotifyListener
    {
      public void finished(Project project);
      public void waiting();
    }
    

  public EncryptService(ProjectRepository projectRepository)
  {
     this.aes = new Aes();
     this.steg = new Steganography();
     this.chaCha = new ChaCha();
     this.projectRepo = projectRepository;
  }

  public void toEncrypt(InputStream fileData, byte[] plaintext, String projName, String fileType, ProcessNotifyListener listener) throws IOException, UnsupportedAudioFileException, InterruptedException
  {
    listener.waiting();
    SecureRandom secureRandom = new SecureRandom();// secured random generation of bytes
    byte[] keyBytes = new byte[32];// 256 bit key
    byte[] nonceBytes = new byte[12];// 96 bit nonce(initialization vector)

    secureRandom.nextBytes(keyBytes);
    secureRandom.nextBytes(nonceBytes);

    int[] chachaKey = new int[8];// key as int array
    int[] chachaNonce = new int[3];// nonce as int array

    ByteBuffer.wrap(keyBytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(chachaKey);// convertion from byte[] to int[]
    ByteBuffer.wrap(nonceBytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(chachaNonce);// convertion from byte[] to int[] 

    byte[] cipherText =  chaCha.encrypt_text(plaintext, chachaKey, chachaNonce);
    
    encryptedFile = steg.encrypt(fileData, cipherText, fileType);

    byte[] aesKey = new byte[32];
    byte[] aesIv = new byte[16];
    
    secureRandom.nextBytes(aesKey);
    secureRandom.nextBytes(aesIv);

    Key key = new Key(chachaKey, chachaNonce, aesKey, aesIv);
    int contentLength;
    if(fileType.equals("wav")) contentLength = plaintext.length;
    else contentLength = -1;
    
    Thread thread = new Thread(()->{

       encryptedFile = aes.encrypt(encryptedFile, aesKey, aesIv);
       System.out.println("aes ended");

       listener.finished(new Project(encryptedFile, projName, fileType, key, contentLength));
    });

    thread.start();
  }

  public void addProject(Project project)
  {
     projectRepo.insert(project);
  }
}
