package eliranh.three_layers_security.Classes;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "articles")
public class Article
{
    private Long authorId;
    private String author;
    private byte[] data;
    private String name;
    private String fileType;
    private Key key;
    private int contentLength;
    private LocalDate date;
    private int classLevel;
    private boolean allowEdit;

    public Article(byte[] projectData, String projectName, String fileType, Key key, int contentLength, LocalDate date)
    {
       this.data = projectData;
       this.name = projectName;
       this.fileType = fileType;
       this.key = key;
       this.contentLength = contentLength;
       this.date = date;
       this.allowEdit = allowEdit;
    }

    public Article()
    {

    }

    
    
    public int getClassLevel() {
        return classLevel;
    }

    public boolean isAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public void setClassLevel(int classLevel) {
        this.classLevel = classLevel;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setAuthor(Long authorId,String author) {
        this.author = author;
        this.authorId = authorId;
    }


    public int getContentLength() {
        return contentLength;
    }


    public String getFileType() {
        return fileType;
    }


    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public byte[] getProjectData() {
        return data;
    }

    public void setProjectData(byte[] projectData) {
        this.data = projectData;
    }

    public String getName() {
        return name;
    }

    public void setName(String projectName) {
        this.name = projectName;
    }

    public Key getKey() {
        return key;
    }   
}
