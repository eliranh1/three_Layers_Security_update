package eliranh.three_layers_security.Services;

import java.util.ArrayList;
import org.springframework.stereotype.Service;

import eliranh.three_layers_security.Classes.Article;
import eliranh.three_layers_security.Repositories.ArticleRepository;

@Service
public class ArticleService 
{
    private ArticleRepository articleRepo;

    public ArticleService(ArticleRepository articleRepository)
    {
        this.articleRepo = articleRepository;
    }

    public ArrayList<Article> findProjects(int classLevel)
    {
        return (ArrayList<Article>)articleRepo.findAllByClassLevelBetween(0,classLevel+1);
    }

    public void addProject(Article article)
    {
       articleRepo.insert(article);
    }
}
