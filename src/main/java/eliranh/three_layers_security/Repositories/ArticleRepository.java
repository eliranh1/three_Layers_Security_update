package eliranh.three_layers_security.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import eliranh.three_layers_security.Classes.Article;

@Repository
public interface ArticleRepository extends MongoRepository<Article,Long> 
{
   public List<Article> findAllByAuthorId(Long authorId);

   @Query("filter: {name:1}")
   public List<Article> findAllByClassLevelBetween(int from, int to);
}
