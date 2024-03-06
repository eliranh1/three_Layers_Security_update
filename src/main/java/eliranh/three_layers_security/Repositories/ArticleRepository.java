package eliranh.three_layers_security.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import eliranh.three_layers_security.Classes.Article;

@Repository
public interface ArticleRepository extends MongoRepository<Article,Long> 
{
   public List<Article> findAllByAuthorId(Long authorId);
   public List<Article> findAllByClassLevelBetween(int from, int to);
}
