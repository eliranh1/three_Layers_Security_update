package eliranh.three_layers_security.Services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONObject;
import org.springframework.stereotype.Service;


import eliranh.three_layers_security.Aes.Aes;
import eliranh.three_layers_security.ChaCha.ChaCha;
import eliranh.three_layers_security.Classes.Key;
import eliranh.three_layers_security.Classes.Article;
import eliranh.three_layers_security.Repositories.ArticleRepository;
import eliranh.three_layers_security.Steganography.Steganography;


@Service
public class EncryptService 
{
    private ChaCha chaCha;
    private Steganography steg;
    private Aes aes;
    private byte[] encryptedFile;
    private ArticleRepository projectRepo;
    private byte[] aesKey,aesIv;

    private ArrayList<String> queries;
    private int counter;
    private String queryWord;
    private String API_URL;

    public interface EProcessNotifyListener
    {
      public void finished(Article article);
      public void waiting();
    }


  public EncryptService(ArticleRepository projectRepository)
  {
     this.aes = new Aes();
     this.steg = new Steganography();
     this.chaCha = new ChaCha();
     this.projectRepo = projectRepository;
     this.queries = new ArrayList<>();
      queries.add("Manaus");
      queries.add("Harare");
      queries.add("London");
      queries.add("Cradock");
      queries.add("Jerusalem");
      queries.add("Tiksi");
      queries.add("Toronto");
      queries.add("Moscow");
      queries.add("Gandhinagar");
      queries.add("Istanbul");
      queries.add("Tel Aviv");
      queries.add("Amazonas");
      queries.add("Munich");
      queries.add("Nuuk");
      queries.add("Eilat");
      queries.add("whitehorse");
      queries.add("portland");
      queries.add("manta");
      queries.add("mendoza");
      queries.add("yakutsk");
  }

  public void toEncrypt(InputStream fileData, byte[] plaintext, String projName, String fileType, EProcessNotifyListener listener) throws IOException, UnsupportedAudioFileException, InterruptedException
  {
    listener.waiting();

    byte[] keyBytes = new byte[32];// 256 bit key
    byte[] nonceBytes = new byte[12];// 96 bit nonce(initialization vector)

    keyBytes = generateBytes();
    nonceBytes = generateBytes();

    int[] chachaKey = new int[8];// key as int array
    int[] chachaNonce = new int[3];// nonce as int array

    ByteBuffer.wrap(keyBytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(chachaKey);// convertion from byte[] to int[]
    ByteBuffer.wrap(nonceBytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(chachaNonce);// convertion from byte[] to int[] 

    byte[] cipherText =  chaCha.encrypt_text(plaintext, chachaKey, chachaNonce);

    encryptedFile = steg.encrypt(fileData, cipherText, fileType);

    aesKey = new byte[32];
    aesIv = new byte[16];

    aesKey = generateBytes();
    aesIv = generateBytes();

    Key key = new Key(chachaKey, chachaNonce, aesKey, aesIv);
    int contentLength;
    if(fileType.equals("wav")) contentLength = plaintext.length;
    else contentLength = -1;

    Thread thread = new Thread(()->{

       encryptedFile = aes.encrypt(encryptedFile, aesKey, aesIv);
       System.out.println("aes ended");

       listener.finished(new Article(encryptedFile, projName, fileType, key, contentLength,LocalDate.now()));
    });

    thread.start();
  }

  public void addProject(Article article)
  {
     projectRepo.insert(article);
  }

  public byte[] generateBytes() throws IOException
  {
    Random rand = new Random();
    counter = rand.nextInt(20);
    queryWord = queries.get(counter);
    updateUrl();
    URL url = new URL(API_URL); 
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("GET");
    byte[] jsonData = con.getInputStream().readAllBytes();
    JSONObject jsonObject = new JSONObject(new String(jsonData));

    System.out.println("name of taken number: " + jsonObject.get("name").toString());   

    double windDeg = Double.parseDouble(jsonObject.getJSONObject("wind").get("deg").toString());
    double windSpeed = Double.parseDouble(jsonObject.getJSONObject("wind").get("speed").toString());
    double temp = Double.parseDouble(jsonObject.getJSONObject("main").get("temp").toString());
    double humidity = Double.parseDouble(jsonObject.getJSONObject("main").get("humidity").toString());
    double num = Double.parseDouble(windDeg + windSpeed + temp + humidity+"");
    Long multNum = (long) (num*123456789); 

    counter = rand.nextInt(20);
    queryWord = queries.get(counter);
    updateUrl();
    url = new URL(API_URL); 
    con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("GET");
    jsonData = con.getInputStream().readAllBytes();
    jsonObject = new JSONObject(new String(jsonData));

    String name = jsonObject.get("name").toString();
    System.out.println("name of taken name: "+ name);
    byte[] firstHalf = new byte[name.length()/2];
    byte[] secondHalf = new byte[name.length()/2];
    ByteArrayInputStream text = new ByteArrayInputStream(name.getBytes());
    text.read(firstHalf);
    text.read(secondHalf);
    String key = secondHalf + "" + multNum + firstHalf;
    byte[] bytes = key.getBytes();
    SecureRandom secureRandom = new SecureRandom();
    byte[] nonce = new byte[bytes.length];
    secureRandom.nextBytes(nonce);

    byte[] keyBytes = new byte[bytes.length];
    for(int r = 0; r < bytes.length; r++)
    {
         keyBytes[r] = (byte)(nonce[r]^bytes[r]);
    }
    
    if(keyBytes.length < 32)
    {
      int paddingLength = 32 - keyBytes.length;
      byte[] padding = new byte[paddingLength];
      secureRandom.nextBytes(padding);
      ByteArrayOutputStream data = new ByteArrayOutputStream();
      data.write(keyBytes);
      data.write(padding);
      keyBytes = data.toByteArray();
    }
    System.out.println("size: "+keyBytes.length);
    for (int j = 0; j < keyBytes.length; j++) {
      System.out.printf("%02X ", keyBytes[j]);
    }
    System.out.println(); 
    System.out.println();

    return keyBytes;
  }

  public void updateUrl()    
  {  
        this.API_URL = new String("https://api.openweathermap.org/data/2.5/weather?q="+this.queryWord+"&appid=af28b478f062516c8b63c14ad796999e");
  } 
}
