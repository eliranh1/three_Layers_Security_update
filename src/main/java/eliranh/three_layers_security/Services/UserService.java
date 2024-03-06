package eliranh.three_layers_security.Services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import eliranh.three_layers_security.Classes.User;
import eliranh.three_layers_security.Repositories.UserRepository;

@Service
public class UserService 
{
    private ArrayList<String> queries;
    private int counter;
    private String queryWord;
    private String API_URL;
    private UserRepository userRepo;

    public UserService(UserRepository userRepository)
    {
      this.userRepo = userRepository;
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
    }

    public boolean isUserExistsById(Long id) 
    {
        return userRepo.existsById(id);
    }

    public User findUserById(Long id) 
    {
        return userRepo.findUserById(id);
    }

    public void addUser(User user)
    {
        userRepo.insert(user);
    }

    public List<User> findAllUsers() 
    {
       return userRepo.findAll();
    }

    public void deleteUser(User user) 
    {
        userRepo.delete(user);
    }

    public void updateUser(User user) 
    {
        userRepo.save(user);
    }

    // public void generatePassword() throws IOException
    // {
    //     Random rand = new Random();
    //     counter = rand.nextInt(10);
    //     queryWord = queries.get(counter);
    //     updateUrl();
    //     URL url = new URL(API_URL); 
    //     HttpURLConnection con = (HttpURLConnection)url.openConnection();
    //     con.setRequestMethod("GET");
    //     byte[] jsonData = con.getInputStream().readAllBytes();
    //     JSONObject jsonObject = new JSONObject(new String(jsonData));
    //     System.out.println("name: " + jsonObject.get("name").toString());  
    //     System.out.println("wind degree : " + jsonObject.getJSONObject("wind").get("deg"));
    //     System.out.println("wind speed: " + jsonObject.getJSONObject("wind").get("speed"));  
    //     System.out.println("temp: "+jsonObject.getJSONObject("main").get("temp"));
    //     System.out.println("humidity: "+jsonObject.getJSONObject("main").get("humidity"));
    //     System.out.println("-----------------------------"); 

    //     double windDeg = Double.parseDouble(jsonObject.getJSONObject("wind").get("deg").toString());
    //     double windSpeed = Double.parseDouble(jsonObject.getJSONObject("wind").get("speed").toString());
    //     double temp = Double.parseDouble(jsonObject.getJSONObject("main").get("temp").toString());
    //     double humidity = Double.parseDouble(jsonObject.getJSONObject("main").get("humidity").toString());
    //     Long num = Long.parseLong(windDeg + windSpeed + temp + humidity+"");
    //     System.out.println("number that generated: "+num);
    // }

    // public void updateUrl()    
    // {  
    //     this.API_URL = new String("https://api.openweathermap.org/data/2.5/weather?q="+this.queryWord+"&appid=af28b478f062516c8b63c14ad796999e");
    // } 
}
