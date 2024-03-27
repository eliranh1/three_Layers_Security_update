package eliranh.three_layers_security.Services;

import java.util.ArrayList;
import org.springframework.stereotype.Service;

import eliranh.three_layers_security.Classes.Article;
import eliranh.three_layers_security.Repositories.ArticleRepository;

@Service
public class ArticleService 
{
    private ArticleRepository articleRepo;

    public interface LoadAritclesListener
    {
      public void finished(ArrayList<Article> articles);
    }

    public ArticleService(ArticleRepository articleRepository)
    {
        this.articleRepo = articleRepository;
    }

    public void findProjects(int classLevel,LoadAritclesListener listener)
    {
        Thread thread = new Thread(()->{
        ArrayList<Article> articles = (ArrayList<Article>)articleRepo.findAllByClassLevelBetween(0,classLevel+1);
        listener.finished(articles);
        });
        thread.start();
    }

    public void updateArticle(Article article)
    {
        articleRepo.save(article);
    }
    public void addProject(Article article)
    {
       articleRepo.insert(article);
    }
}
