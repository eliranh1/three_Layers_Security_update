package eliranh.demo.Classes;

import org.springframework.data.mongodb.core.mapping.Document;

import com.vaadin.flow.component.template.Id;

@Document(collection = "users")
public class User 
{
    @Id
    public Long id;
    public String name;
    public String password;
}
