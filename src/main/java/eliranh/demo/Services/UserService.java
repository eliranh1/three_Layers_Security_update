package eliranh.demo.Services;

import org.springframework.stereotype.Service;

import eliranh.demo.Classes.User;
import eliranh.demo.Repositories.UserRepository;

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
    
}
