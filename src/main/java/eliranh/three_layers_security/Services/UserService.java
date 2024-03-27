package eliranh.three_layers_security.Services;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import eliranh.three_layers_security.Classes.User;
import eliranh.three_layers_security.Repositories.UserRepository;

@Service
public class UserService 
{
    private UserRepository userRepo;

    public UserService(UserRepository userRepository)
    {
      this.userRepo = userRepository;
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

    public void generatePassword() throws IOException
    {
        for(int i=0; i < 10; i++)
        {
            
        }
    }      
}