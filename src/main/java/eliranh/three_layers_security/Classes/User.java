package eliranh.three_layers_security.Classes;

import org.springframework.data.mongodb.core.mapping.Document;
import com.vaadin.flow.component.template.Id;

@Document(collection = "users")
public class User 
{
    @Id
    private Long id;
    private String name;
    private String password;
    private int classLevel;
    private boolean isAdmin;
    private String avatarPath;

    public User(Long id, String name, String password, int classLevel, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.classLevel = classLevel;
        this.isAdmin = isAdmin;
        if(classLevel == 1)
        this.avatarPath = "soldier_image.png";
        if(classLevel == 2)
        this.avatarPath = "comannder_image.png";
        if(classLevel == 3)
        this.avatarPath = "lutenent_image.png";
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public int getClassLevel() {
        return classLevel;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getAvatarPath() {
        return avatarPath;
    }    

    
    
}
