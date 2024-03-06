package eliranh.three_layers_security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import eliranh.three_layers_security.Classes.User;

@Repository
public interface UserRepository extends MongoRepository<User,Long>
{
    public User findUserById(Long id);
}
