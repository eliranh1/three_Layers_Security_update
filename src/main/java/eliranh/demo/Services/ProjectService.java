package eliranh.demo.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import eliranh.demo.Classes.Project;
import eliranh.demo.Repositories.ProjectRepository;

@Service
public class ProjectService 
{
    private ProjectRepository projectRepo;
    
    public ProjectService(ProjectRepository projectRepository)
    {
        this.projectRepo = projectRepository;
    }

    public ArrayList<Project> findProjects(Long authorId)
    {
        return (ArrayList<Project>)projectRepo.findAllByAuthor(authorId);
    }

    public void addProject(Project project)
    {
       projectRepo.insert(project);
    }
}
