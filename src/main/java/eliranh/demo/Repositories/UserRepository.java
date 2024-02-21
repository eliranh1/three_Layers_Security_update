package eliranh.demo.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import eliranh.demo.Classes.User;

@Repository
public interface UserRepository extends MongoRepository<User,Long>
{
    public User findUserById(Long id);
}
