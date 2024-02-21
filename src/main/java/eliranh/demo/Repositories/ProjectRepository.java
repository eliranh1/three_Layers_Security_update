package eliranh.demo.Repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import eliranh.demo.Classes.Project;

@Repository
public interface ProjectRepository extends MongoRepository<Project,Long> 
{
   public List<Project> findAllByAuthor(Long authorId);
}
